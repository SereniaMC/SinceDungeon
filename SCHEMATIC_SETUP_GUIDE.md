# SinceDungeon: Schematic Instancing Setup Guide

Welcome to the comprehensive guide for setting up **Schematic-based Dungeons** in SinceDungeon Premium.

Schematic instancing allows you to host multiple concurrent dungeon runs in a single "shared" world, drastically reducing server RAM usage and CPU load compared to cloning entire worlds.

---

## 📋 Prerequisites

Before you begin, ensure you have the following installed on your server:
1. **SinceDungeon Core** (v1.6.4+)
2. **SinceDungeon Premium Addon** (v1.0+)
3. **FastAsyncWorldEdit (FAWE)** (Highly recommended for performance over normal WorldEdit)

---

## 🏗️ Step 1: Building the Dungeon

1. Go to your creative/building world.
2. Build your dungeon arena. It can be as large or small as you want.
3. *Tip: Keep a mental note of where you want players to spawn, and where your mobs/objectives will be.*

---

## ✂️ Step 2: Selecting and Copying (CRITICAL STEP)

The way you copy the schematic determines how coordinates will work in your SinceDungeon configuration.

1. Select your dungeon using FAWE's wand (`//wand`) or by typing `//pos1` and `//pos2` at the corners of your build.
2. **Stand exactly where you want the "Center Origin" (X=0, Z=0) of your dungeon to be.**
   - *Example:* If you stand on the starting spawn pad, the spawn pad becomes `0, 0` in your dungeon configuration.
3. Once you are standing in the perfect spot, type:
   ```
   //copy
   ```
   > **⚠️ IMPORTANT:** The position where you stand when you type `//copy` is saved as the "Clipboard Origin". SinceDungeon uses this origin as the center point when pasting and mapping coordinates.

---

## 💾 Step 3: Saving the Schematic

1. While the copied dungeon is still in your clipboard, type:
   ```
   //schem save my_first_dungeon
   ```
   *(Replace `my_first_dungeon` with your desired template name).*
2. By default, FAWE saves this file to:
   `plugins/FastAsyncWorldEdit/schematics/my_first_dungeon.schem`

---

## 📂 Step 4: Moving the File

SinceDungeon Premium needs access to this schematic file.

1. Go to your server's file manager or FTP.
2. Navigate to `plugins/FastAsyncWorldEdit/schematics/`.
3. Copy the `my_first_dungeon.schem` file.
4. Paste it into the SinceDungeon Premium folder:
   `plugins/SinceDungeon-PremiumAddon/schematics/`
   
*(If the `schematics` folder doesn't exist yet, simply create it).*

---

## ⚙️ Step 5: Configuring the Dungeon Template

Now, tell SinceDungeon to use this schematic instead of a cloned world.

1. Navigate to `plugins/SinceDungeon/templates/` and create/open `my_first_dungeon.yml`.
2. Configure the essential settings:

```yaml
settings:
  # Set the provider to SCHEMATIC
  provider: "SCHEMATIC"
  
  # The world name doesn't matter for schematics, but keep it matching for clarity
  world: "my_first_dungeon"
  
  # The spawn location relative to where you stood during //copy
  # If you stood exactly on the spawn pad, your X and Z will be 0, 0.
  start-location: "0, 64, 0"
```

### 🧭 Understanding Coordinates in Config

SinceDungeon maps all your actions, mob spawns, and objectives relative to the "Center Origin" (where you typed `//copy`).

- If you want an objective 10 blocks North (-Z) of the origin, you set the coordinate to `0, Y, -10`.
- If you stood at `Y=64` when copying, and you want to spawn a boss at the exact same height, you will use `Y=64` in your configs.

---

## 🚀 Step 6: Testing

1. Restart or reload SinceDungeon (`/dungeon reload`).
2. Type `/dungeon join my_first_dungeon`.
3. The premium addon will automatically:
   - Find an empty slot in the shared `SDPremium_Schematic` world.
   - Synchronously paste the schematic without lagging the server (Fully Folia Compatible).
   - Teleport you to the calculated `start-location`.
   - Start the game!

---

## 🧹 How Cleanup Works

When a game finishes (win, lose, or time out), SinceDungeon Premium automatically:
1. Kicks the players out.
2. Uses FAWE to select the bounding box of the pasted schematic.
3. Sets the entire region to `AIR` (`//set air`).
4. Marks the grid slot as "Free" so the next party can reuse it.

This guarantees no world-corruption and zero memory leaks over time!

---

## 🛠️ Troubleshooting

- **Player spawns in the void / falls indefinitely:**
  Your `start-location` in the `.yml` file doesn't match the Y-level of the floor. Remember that the Y-coordinate is absolute to where the schematic was pasted. Adjust the Y value in your `start-location` until you land on the floor.
  
- **Schematic not pasting / Console errors:**
  Ensure you are using a compatible version of FastAsyncWorldEdit (FAWE) for your server jar (Paper/Folia). Standard WorldEdit may cause async thread exceptions on Folia.

- **"File not found" warning in console:**
  Double-check that the `.schem` file is inside `plugins/SinceDungeon-PremiumAddon/schematics/` and that the file name perfectly matches the template name in SinceDungeon.
