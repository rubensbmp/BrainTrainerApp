package com.braintrainer.app.ui.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.braintrainer.app.R
import com.braintrainer.app.databinding.FragmentSocialBinding
import com.braintrainer.app.model.Group
import com.braintrainer.app.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class SocialFragment : Fragment() {
    private var _binding: FragmentSocialBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SocialViewModel by viewModels()
    private var selectedGroup: Group? = null

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: Exception) {
            Toast.makeText(context, "Login falhou: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSocialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        if (FirebaseAuth.getInstance().currentUser != null) {
            viewModel.refreshGroups()
        }
    }

    private fun setupListeners() {
        binding.btnGoogleSignIn.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("374448667606-bcpj1l4t8an9ssqrg9lr51lebdtn3fa5.apps.googleusercontent.com") // ID real atualizado
                .requestEmail()
                .build()
            val client = GoogleSignIn.getClient(requireActivity(), gso)
            signInLauncher.launch(client.signInIntent)
        }

        binding.fabAddGroup.setOnClickListener {
            showAddGroupDialog()
        }
    }

    private fun setupObservers() {
        viewModel.isLoggedIn.observe(viewLifecycleOwner) { loggedIn ->
            binding.llLoginPrompt.visibility = if (loggedIn) View.GONE else View.VISIBLE
            binding.clLoggedSocial.visibility = if (loggedIn) View.VISIBLE else View.GONE
        }

        viewModel.userGroups.observe(viewLifecycleOwner) { groups ->
            binding.rvGroups.adapter = GroupAdapter(groups, 
                onDelete = { group -> 
                    AlertDialog.Builder(context)
                        .setTitle("Apagar Grupo")
                        .setMessage("Tem certeza que deseja apagar o grupo '${group.name}'? Todos os membros serão removidos.")
                        .setPositiveButton("Sim") { _, _ -> viewModel.deleteGroup(group.groupId) }
                        .setNegativeButton("Não", null)
                        .show()
                },
                onCopyId = { group ->
                    val clipboard = context?.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("Group ID", group.groupId)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "ID copiado: ${group.groupId}", Toast.LENGTH_SHORT).show()
                },
                onClick = { group ->
                    selectedGroup = group
                    viewModel.loadRanking(group.groupId)
                    binding.tvRankingTitle.text = "${getString(R.string.social_ranking_title)}: ${group.name}"
                }
            )
        }

        viewModel.ranking.observe(viewLifecycleOwner) { users ->
            binding.rvRanking.layoutManager = LinearLayoutManager(context)
            binding.rvRanking.adapter = RankingAdapter(users, 
                currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                isCurrentUserAdmin = selectedGroup?.ownerId == FirebaseAuth.getInstance().currentUser?.uid,
                onMemberAction = { member ->
                    val options = arrayOf("Expulsar do Grupo", "Tornar Administrador (Transferir)")
                    AlertDialog.Builder(context)
                        .setTitle("Ação: ${member.name}")
                        .setItems(options) { _, which ->
                            if (which == 0) viewModel.kickMember(selectedGroup!!.groupId, member.uid)
                            else viewModel.transferAdmin(selectedGroup!!.groupId, member.uid)
                        }
                        .show()
                }
            )
        }

        viewModel.error.observe(viewLifecycleOwner) { msg ->
            if (msg != null) Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                viewModel.checkAuthStatus()
            } else {
                Toast.makeText(context, "Erro Firebase: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddGroupDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_social_choice, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()
        
        dialogView.findViewById<View>(R.id.btnCreateOption).setOnClickListener {
            dialog.dismiss()
            showCreateDialog()
        }
        
        dialogView.findViewById<View>(R.id.btnJoinOption).setOnClickListener {
            dialog.dismiss()
            showJoinDialog()
        }
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun showCreateDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_social_group, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()
        
        val tvTitle = dialogView.findViewById<com.braintrainer.app.ui.views.ClashTextView>(R.id.tvDialogTitle)
        val tilInput = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilInput)
        val etInput = dialogView.findViewById<com.braintrainer.app.ui.views.ClashEditText>(R.id.etInput)
        val btnAction = dialogView.findViewById<com.braintrainer.app.ui.views.ClashButton>(R.id.btnAction)

        tvTitle.text = getString(R.string.social_create_group)
        tilInput.hint = getString(R.string.social_group_name_hint)
        btnAction.text = getString(R.string.social_create_btn)
        
        btnAction.setOnClickListener {
            val name = etInput.text.toString()
            if (name.isNotEmpty()) {
                viewModel.createGroup(name)
                dialog.dismiss()
            }
        }
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun showJoinDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_social_group, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()
        
        val tvTitle = dialogView.findViewById<com.braintrainer.app.ui.views.ClashTextView>(R.id.tvDialogTitle)
        val tilInput = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilInput)
        val etInput = dialogView.findViewById<com.braintrainer.app.ui.views.ClashEditText>(R.id.etInput)
        val btnAction = dialogView.findViewById<com.braintrainer.app.ui.views.ClashButton>(R.id.btnAction)

        tvTitle.text = getString(R.string.social_join_group)
        tilInput.hint = getString(R.string.social_group_id_hint)
        btnAction.text = getString(R.string.social_join_btn)
        
        btnAction.setOnClickListener {
            val id = etInput.text.toString()
            if (id.isNotEmpty()) {
                viewModel.joinGroup(id)
                dialog.dismiss()
            }
        }
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Adapters
class GroupAdapter(
    private val groups: List<Group>, 
    private val onDelete: (Group) -> Unit,
    private val onCopyId: (Group) -> Unit,
    private val onClick: (Group) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<GroupAdapter.VH>() {
    class VH(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {
        val text = v.findViewById<android.widget.TextView>(R.id.tvGroupName)
        val btnDelete = v.findViewById<android.widget.ImageButton>(R.id.btnDeleteGroup)
        val btnCopy = v.findViewById<android.widget.ImageButton>(R.id.btnCopyId)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(LayoutInflater.from(parent.context).inflate(R.layout.item_group_chip, parent, false))
    override fun onBindViewHolder(holder: VH, position: Int) {
        val group = groups[position]
        holder.text.text = group.name
        holder.itemView.setOnClickListener { onClick(group) }
        
        holder.btnCopy.setOnClickListener { onCopyId(group) }
        
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (group.ownerId == currentUserId) {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener { onDelete(group) }
        } else {
            holder.btnDelete.visibility = View.GONE
        }
    }
    override fun getItemCount() = groups.size
}

class RankingAdapter(
    private val users: List<User>,
    private val currentUserId: String,
    private val isCurrentUserAdmin: Boolean,
    private val onMemberAction: (User) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<RankingAdapter.VH>() {
    class VH(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {
        val rank = v.findViewById<android.widget.TextView>(R.id.tvRank)
        val medal = v.findViewById<android.widget.ImageView>(R.id.ivMedal)
        val avatar = v.findViewById<android.widget.ImageView>(R.id.ivUserPhoto)
        val name = v.findViewById<android.widget.TextView>(R.id.tvUserName)
        val score = v.findViewById<android.widget.TextView>(R.id.tvUserBrainAge)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(LayoutInflater.from(parent.context).inflate(R.layout.item_ranking_user, parent, false))
    override fun onBindViewHolder(holder: VH, position: Int) {
        val user = users[position]
        holder.rank.text = (position + 1).toString()
        
        // Medals logic
        when(position) {
            0 -> {
                holder.medal.visibility = View.VISIBLE
                holder.medal.setImageResource(R.drawable.ic_medal_1)
            }
            1 -> {
                holder.medal.visibility = View.VISIBLE
                holder.medal.setImageResource(R.drawable.ic_medal_2)
            }
            2 -> {
                holder.medal.visibility = View.VISIBLE
                holder.medal.setImageResource(R.drawable.ic_medal_3)
            }
            else -> {
                holder.medal.visibility = View.GONE
            }
        }

        // Avatar logic
        val iconRes = when(user.avatarId) {
            1 -> R.drawable.avatar_1
            2 -> R.drawable.avatar_2
            3 -> R.drawable.avatar_3
            4 -> R.drawable.avatar_4
            5 -> R.drawable.avatar_5
            6 -> R.drawable.avatar_6
            7 -> R.drawable.avatar_7
            8 -> R.drawable.avatar_8
            9 -> R.drawable.avatar_9
            10 -> R.drawable.avatar_10
            11 -> R.drawable.avatar_11
            12 -> R.drawable.avatar_12
            13 -> R.drawable.avatar_13
            14 -> R.drawable.avatar_14
            15 -> R.drawable.avatar_15
            else -> R.drawable.avatar_1
        }
        holder.avatar.setImageResource(iconRes)
        
        holder.name.text = user.name
        holder.score.text = holder.itemView.context.getString(R.string.social_ranking_score, user.brainAge)

        // Admin Actions
        if (isCurrentUserAdmin && user.uid != currentUserId) {
            holder.itemView.setOnLongClickListener {
                onMemberAction(user)
                true
            }
        } else {
            holder.itemView.setOnLongClickListener(null)
        }
    }
    override fun getItemCount() = users.size
}
