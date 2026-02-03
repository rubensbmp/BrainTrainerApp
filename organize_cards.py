import os
import shutil
import re

# 1. SETUP ICON
# The user uploaded image path provided in chat metadata:
# C:/Users/ruben/.gemini/antigravity/brain/c0eff083-a403-44d4-8636-d81c00dcf5fe/uploaded_media_1770117872119.png
# We need to copy this to app/src/main/res/drawable/ic_game_poker.png

src_icon = r"C:/Users/ruben/.gemini/antigravity/brain/c0eff083-a403-44d4-8636-d81c00dcf5fe/uploaded_media_1770117872119.png"
dst_icon = r"C:\Users\ruben\.gemini\antigravity\scratch\BrainTrainerApp\app\src\main\res\drawable\ic_game_poker.png"

if os.path.exists(src_icon):
    shutil.copy2(src_icon, dst_icon)
    print(f"Copied icon to {dst_icon}")
else:
    print("Icon source not found!")

# 2. RENAME AND MOVE CARDS
# Source base: app/Cartas
# Dest: app/src/main/res/drawable
# Naming: card_[rank]_[suit].png (lowercase)

base_dir = r"C:\Users\ruben\.gemini\antigravity\scratch\BrainTrainerApp\app\Cartas"
target_dir = r"C:\Users\ruben\.gemini\antigravity\scratch\BrainTrainerApp\app\src\main\res\drawable"

rank_map = {
    "as": "a", "dois": "2", "tres": "3", "quatro": "4", "cinco": "5",
    "seis": "6", "sete": "7", "oito": "8", "nove": "9", "dez": "t",
    "valete": "j", "rainha": "q", "rei": "k"
}

suit_map = {
    "copas": "h", "coracoes": "h",
    "espadas": "s",
    "ouros": "d", "diamantes": "d",
    "paus": "c", "clubes": "c"
}

# Walk through directories
for root, dirs, files in os.walk(base_dir):
    for filename in files:
        if not filename.endswith(".png"): continue
        
        # Parse filename e.g. "as-de-copas.png" or "cinco-dos-clubes.png"
        name_lower = filename.lower()
        
        # Identify Rank
        rank = None
        for k, v in rank_map.items():
            if k in name_lower:
                rank = v
                break
        
        # Identify Suit
        suit = None
        for k, v in suit_map.items():
            if k in name_lower:
                suit = v
                break
        
        if rank and suit:
            new_name = f"card_{rank}_{suit}.png"
            src_path = os.path.join(root, filename)
            dst_path = os.path.join(target_dir, new_name)
            
            shutil.copy2(src_path, dst_path)
            print(f"Processed: {filename} -> {new_name}")
        else:
            print(f"Skipped (Could not identify): {filename}")

