package com.braintrainer.app.data

import com.braintrainer.app.model.Group
import com.braintrainer.app.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SocialRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun getCurrentUser() = auth.currentUser

    suspend fun syncUserProfile(name: String, brainAge: Int, totalMatches: Int, avgGrade: String, avatarId: Int) {
        val firebaseUser = auth.currentUser ?: return
        
        // Use a map to update only specific fields, preserving groupIds
        val updates = hashMapOf<String, Any>(
            "uid" to firebaseUser.uid,
            "email" to (firebaseUser.email ?: ""),
            "photoUrl" to (firebaseUser.photoUrl?.toString() ?: ""),
            "brainAge" to brainAge,
            "totalMatches" to totalMatches,
            "avgGrade" to avgGrade
        )

        // Only update name if provided (non-empty)
        if (name.isNotEmpty()) {
            updates["name"] = name
        }

        // Only update avatar if valid (non-negative)
        if (avatarId >= 0) {
            updates["avatarId"] = avatarId
        }
        
        firestore.collection("users").document(firebaseUser.uid)
            .set(updates, com.google.firebase.firestore.SetOptions.merge()).await()
    }

    suspend fun createGroup(groupName: String): String {
        val user = auth.currentUser ?: throw Exception("Usuário não logado no Firebase")
        
        // Check if user is already in 5 groups
        val userDoc = firestore.collection("users").document(user.uid).get().await()
        val currentGroupIds = userDoc.get("groupIds") as? List<*>
        if (currentGroupIds != null && currentGroupIds.size >= 5) {
            throw Exception("Você já atingiu o limite de 5 grupos.")
        }

        val groupRef = firestore.collection("groups").document()
        val groupId = groupRef.id
        
        val group = Group(
            groupId = groupId,
            name = groupName,
            ownerId = user.uid,
            memberCount = 1
        )
        
        try {
            // First, create the group document
            groupRef.set(group).await()
            
            // Then, link it to the user.
            val userRef = firestore.collection("users").document(user.uid)
            val updateData = mapOf("groupIds" to FieldValue.arrayUnion(groupId))
            userRef.set(updateData, com.google.firebase.firestore.SetOptions.merge()).await()
            
            return groupId
        } catch (e: Exception) {
            throw Exception("Falha no Firestore: ${e.localizedMessage}")
        }
    }

    suspend fun joinGroup(groupId: String) {
        val user = auth.currentUser ?: throw Exception("User not logged in")
        
        // Check if user is already in 5 groups
        val userDoc = firestore.collection("users").document(user.uid).get().await()
        val userModel = userDoc.toObject(User::class.java)
        if (userModel != null && userModel.groupIds.size >= 5) {
            throw Exception("Você já atingiu o limite de 5 grupos.")
        }

        firestore.runBatch { batch ->
            batch.update(firestore.collection("groups").document(groupId), 
                "memberCount", FieldValue.increment(1))
            batch.update(firestore.collection("users").document(user.uid), 
                "groupIds", FieldValue.arrayUnion(groupId))
        }.await()
    }

    suspend fun getGroupRanking(groupId: String): List<User> {
        val snapshot = firestore.collection("users")
            .whereArrayContains("groupIds", groupId)
            .orderBy("brainAge") // Lowest is best
            .limit(50)
            .get()
            .await()
        return snapshot.toObjects(User::class.java)
    }

    suspend fun getUserGroups(): List<Group> {
        val user = auth.currentUser ?: return emptyList()
        val userDoc = firestore.collection("users").document(user.uid).get().await()
        val groupIds = userDoc.toObject(User::class.java)?.groupIds ?: return emptyList()
        
        if (groupIds.isEmpty()) return emptyList()
        
        val snapshot = firestore.collection("groups")
            .whereIn("groupId", groupIds)
            .get()
            .await()
        return snapshot.toObjects(Group::class.java)
    }

    suspend fun deleteGroup(groupId: String) {
        val user = auth.currentUser ?: return
        val group = firestore.collection("groups").document(groupId).get().await().toObject(Group::class.java)
        if (group?.ownerId != user.uid) throw Exception("Apenas o administrador pode apagar o grupo.")

        firestore.runBatch { batch ->
            batch.delete(firestore.collection("groups").document(groupId))
            batch.update(firestore.collection("users").document(user.uid), "groupIds", FieldValue.arrayRemove(groupId))
        }.await()
    }

    suspend fun kickMember(groupId: String, memberUid: String) {
        val user = auth.currentUser ?: return
        val group = firestore.collection("groups").document(groupId).get().await().toObject(Group::class.java)
        if (group?.ownerId != user.uid) throw Exception("Apenas o administrador pode expulsar membros.")
        if (memberUid == user.uid) throw Exception("Você não pode se expulsar. Apague o grupo se desejar sair.")

        firestore.runBatch { batch ->
            batch.update(firestore.collection("groups").document(groupId), "memberCount", FieldValue.increment(-1))
            batch.update(firestore.collection("users").document(memberUid), "groupIds", FieldValue.arrayRemove(groupId))
        }.await()
    }

    suspend fun transferAdmin(groupId: String, newAdminUid: String) {
        val user = auth.currentUser ?: return
        val group = firestore.collection("groups").document(groupId).get().await().toObject(Group::class.java)
        if (group?.ownerId != user.uid) throw Exception("Apenas o administrador pode transferir a função.")

        firestore.collection("groups").document(groupId).update("ownerId", newAdminUid).await()
    }
    suspend fun fetchUserProfile(): User? {
        val user = auth.currentUser ?: return null
        val snapshot = firestore.collection("users").document(user.uid).get().await()
        return snapshot.toObject(User::class.java)
    }

    suspend fun uploadGameResults(results: List<com.braintrainer.app.data.local.GameResult>) {
        val user = auth.currentUser ?: return
        val batch = firestore.batch()
        val collection = firestore.collection("users").document(user.uid).collection("game_results")
        
        // Optimisation: Only upload last 50 or those that are not synced?
        // Simpler for now: Upload all (or recent) and let Firestore Merge handle it if we use deterministic IDs
        // We use timestamp_type as ID
        
        results.forEach { result ->
            val docId = "${result.date}_${result.gameType}"
            val docRef = collection.document(docId)
            batch.set(docRef, result) // Overwrite is fine if identical
        }
        
        batch.commit().await()
    }

    suspend fun fetchGameResults(): List<com.braintrainer.app.data.local.GameResult> {
        val user = auth.currentUser ?: return emptyList()
        val snapshot = firestore.collection("users").document(user.uid)
            .collection("game_results")
            .get()
            .await()
            
        return snapshot.toObjects(com.braintrainer.app.data.local.GameResult::class.java)
    }
}
