import os
import shutil
import re

SOURCE_DIR = "app/Cartas"
DEST_DIR = "app/src/main/res/drawable"

# Ensure destination exists
os.makedirs(DEST_DIR, exist_ok=True)

rank_map = {
    "10": "t",
    "a": "a",
    "k": "k",
    "q": "q",
    "j": "j",
    "2": "2",
    "3": "3",
    "4": "4",
    "5": "5",
    "6": "6",
    "7": "7",
    "8": "8",
    "9": "9"
}

suit_map = {
    "clubs": "c",
    "diamonds": "d",
    "hearts": "h",
    "spades": "s"
}

count = 0

for suit_folder in os.listdir(SOURCE_DIR):
    folder_path = os.path.join(SOURCE_DIR, suit_folder)
    if not os.path.isdir(folder_path):
        continue
        
    suit_code = suit_map.get(suit_folder.lower())
    if not suit_code:
        print(f"Skipping unknown folder: {suit_folder}")
        continue
        
    print(f"Processing {suit_folder} ({suit_code})...")
    
    for filename in os.listdir(folder_path):
        if not filename.endswith(".png"):
            continue
            
        # Parse filename e.g. "10c.png", "ac.png"
        # Since we know the folder is 'clubs', the filename might explicitly have 'c'.
        # Let's extract rank by removing the suit char 'c'/'d'/'h'/'s' and extension.
        
        # Robust extraction:
        name_part = filename[:-4] # remove .png
        if name_part.endswith(suit_code):
             rank_part = name_part[:-1]
        else:
             print(f"Warning: Filename {filename} in {suit_folder} does not end with {suit_code}. Using whole name as rank?")
             rank_part = name_part
             
        # Map rank
        new_rank = rank_map.get(rank_part.lower())
        if not new_rank:
            print(f"Unknown rank: {rank_part} in {filename}")
            continue
            
        new_filename = f"card_{new_rank}_{suit_code}.png"
        src_file = os.path.join(folder_path, filename)
        dst_file = os.path.join(DEST_DIR, new_filename)
        
        print(f"Moving {filename} -> {new_filename}")
        try:
            shutil.copy2(src_file, dst_file)
            count += 1
        except Exception as e:
            print(f"Error moving {filename}: {e}")

print(f"Imported {count} cards.")
