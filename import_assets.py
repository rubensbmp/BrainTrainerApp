import os
import shutil

# 1. PROCESS MAPS (paises restantes)
# Assumed format: "map_code.png" or "countryname.png".
# The user already had existing maps like "map_br.png". 
# Assume new files follow similar convention or need slight rename.
# The user wants them included.

maps_dir = r"C:\Users\ruben\.gemini\antigravity\scratch\BrainTrainerApp\app\paises restantes"
target_dir = r"C:\Users\ruben\.gemini\antigravity\scratch\BrainTrainerApp\app\src\main\res\drawable"

if os.path.exists(maps_dir):
    for filename in os.listdir(maps_dir):
        if not filename.endswith(".png"): continue
        
        # Valid drawable name check
        new_name = filename.lower().replace(" ", "_").replace("-", "_")
        
        # Ensure it has 'map_' prefix if not present? 
        # The existing ones are "map_[two_letter_code].png".
        # If the user provided files are "br.png", we should prefix "map_".
        # If they are "map_br.png", good.
        
        if not new_name.startswith("map_"):
            new_name = "map_" + new_name
            
        # Limit length/chars?
        
        src = os.path.join(maps_dir, filename)
        dst = os.path.join(target_dir, new_name)
        shutil.copy2(src, dst)
        print(f"Imported Map: {filename} -> {new_name}")

# 2. PROCESS CARDS (Cartas) with Robust Logic
# Missing cards logic. 
# rank map logic needs to match user filenames exactly.
# Filenames seen: "dez-de-espadas (1).png", "tres-de-coracoes.png", "rainha-dos-coracoes.png" "cinco-dos-clubes.png"

cards_dir = r"C:\Users\ruben\.gemini\antigravity\scratch\BrainTrainerApp\app\Cartas"

rank_map = {
    "as": "a", "dois": "2", "tres": "3", "quatro": "4", "cinco": "5",
    "seis": "6", "sete": "7", "oito": "8", "nove": "9", "dez": "t",
    "valete": "j", "rainha": "q", "rei": "k"
}

suit_map = {
    "copas": "h", "coracoes": "h", "corações": "h",
    "espadas": "s",
    "ouros": "d", "diamantes": "d",
    "paus": "c", "clubes": "c"
}

if os.path.exists(cards_dir):
    for root, dirs, files in os.walk(cards_dir):
        for filename in files:
            if not filename.lower().endswith(".png"): continue
            
            name_lower = filename.lower()
            
            # Determine Rank
            found_rank = None
            for k, v in rank_map.items():
                # check strict word boundary or simple contains?
                if k in name_lower:
                    found_rank = v
                    break
            
            # Determine Suit
            found_suit = None
            for k, v in suit_map.items():
                if k in name_lower:
                    found_suit = v
                    break
            
            if found_rank and found_suit:
                new_name = f"card_{found_rank}_{found_suit}.png"
                src = os.path.join(root, filename)
                dst = os.path.join(target_dir, new_name)
                shutil.copy2(src, dst)
                print(f"Imported Card: {filename} -> {new_name}")
            else:
                print(f"Failed to identify card: {filename}")
