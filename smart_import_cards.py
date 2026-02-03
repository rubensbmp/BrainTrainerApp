import os
import shutil
import unidecode # If available, but standard replacement is fine for known words

base_dir = r"C:\Users\ruben\.gemini\antigravity\scratch\BrainTrainerApp\app\Cartas"
target_dir = r"C:\Users\ruben\.gemini\antigravity\scratch\BrainTrainerApp\app\src\main\res\drawable"

# Mappings
rank_map = {
    "as": "a", 
    "dois": "2", "two": "2",
    "tres": "3", "three": "3", "três": "3",
    "quatro": "4", "four": "4",
    "cinco": "5", "five": "5",
    "seis": "6", "six": "6",
    "sete": "7", "seven": "7",
    "oito": "8", "eight": "8",
    "nove": "9", "nine": "9",
    "dez": "t", "ten": "t",
    "valete": "j", "jack": "j",
    "rainha": "q", "queen": "q", "dama": "q",
    "rei": "k", "king": "k"
}

suit_map = {
    "copas": "h", "coracoes": "h", "corações": "h", "heart": "h",
    "espadas": "s", "spade": "s",
    "ouros": "d", "diamantes": "d", "diamond": "d",
    "paus": "c", "clubes": "c", "club": "c", "flores": "c" # sometimes flores is used for clubs
}

print("Starting Import...")
count = 0
failed = []

for root, dirs, files in os.walk(base_dir):
    for filename in files:
        if not filename.lower().endswith(".png"): continue
        
        name_lower = filename.lower().replace("-", " ").replace("_", " ").replace(".", " ")
        # Remove (1) etc
        
        # Identify Rank
        found_rank = None
        for k, v in rank_map.items():
            # Check for exact word matches or robust inclusion
            if k in name_lower:
                found_rank = v
                break
        
        # Identify Suit
        found_suit = None
        for k, v in suit_map.items():
            if k in name_lower:
                found_suit = v
                break
        
        if found_rank and found_suit:
            new_name = f"card_{found_rank}_{found_suit}.png"
            src = os.path.join(root, filename)
            dst = os.path.join(target_dir, new_name)
            
            try:
                shutil.copy2(src, dst)
                print(f"[OK] {filename} -> {new_name}")
                count += 1
            except Exception as e:
                print(f"[ERR] Could not copy {filename}: {e}")
        else:
            failed.append(filename)

print(f"Finished. Imported {count} cards.")
if failed:
    print("Could not identify the following files:")
    for f in failed:
        print(f" - {f}")
