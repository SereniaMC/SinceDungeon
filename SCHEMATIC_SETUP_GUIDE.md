# SinceDungeon: Complete Schematic Instancing Setup Guide

Welcome to the ultimate, comprehensive guide for setting up **Schematic-based Dungeons** using the SinceDungeon Premium Addon. 

This guide covers everything from building and saving your schematic, to configuring advanced gameplay stages, mob spawns, and understanding the background grid instancing system.

---

## 📖 Table of Contents
1. [Prerequisites](#-prerequisites)
2. [Building the Dungeon](#-step-1-building-the-dungeon)
3. [Selecting and Copying (CRITICAL)](#-step-2-selecting-and-copying-critical-step)
4. [Saving and Moving the Schematic](#-step-3-saving-and-moving-the-schematic)
5. [Configuring the Dungeon Template](#-step-4-configuring-the-dungeon-template)
6. [Creating Stages and Actions](#-step-5-creating-stages-and-actions)
7. [Spawning MythicMobs](#-step-6-spawning-mythicmobs)
8. [Advanced Premium Configurations](#-advanced-premium-configurations)
9. [How the Grid System Works](#-how-the-grid-system-works)
10. [Troubleshooting & FAQs](#-troubleshooting--faqs)

---

## 📋 Prerequisites

Before you begin, ensure you have the following installed on your server:
1. **SinceDungeon Core** (v1.6.4+)
2. **SinceDungeon Premium Addon** (v1.0+)
3. **FastAsyncWorldEdit (FAWE)** - Highly recommended for maximum pasting performance, and absolutely required if you are running on **Folia**.
4. **MythicMobs** (Optional, but highly recommended for custom boss battles).

---

## 🏗️ Step 1: Building the Dungeon

1. Go to your creative or building world.
2. Build your dungeon arena. It can be a massive open terrain, a closed cavern, or a simple box.
3. **Planning phase:** Keep a mental note of where you want players to spawn, where specific doors are, and where your boss will appear.

---

## ✂️ Step 2: Selecting and Copying (CRITICAL STEP)

The way you copy the schematic dictates how all coordinates will map in your SinceDungeon YAML configuration.

1. Select your dungeon using FAWE's wand (`//wand`) or by typing `//pos1` and `//pos2` at the opposite corners of your build.
2. **Stand exactly where you want the logical "Center Origin" (X=0, Z=0) of your dungeon to be.**
   - *Pro-Tip:* We highly recommend standing exactly at the center of the player's starting spawn pad. This will make your `start-location` equal to `0, 0` and make mapping other locations much easier.
3. Once you are standing in the perfect spot, type:
   ```
   //copy
   ```
   > **⚠️ IMPORTANT:** The exact block you stand on when you type `//copy` is saved as the "Clipboard Origin". SinceDungeon uses this origin as the absolute center point (`0, Y, 0`) when pasting your schematic into the shared world.

---

## 💾 Step 3: Saving and Moving the Schematic

1. While the copied dungeon is still in your clipboard, save it by typing:
   ```
   //schem save my_first_dungeon
   ```
   *(Replace `my_first_dungeon` with your desired template name).*
2. By default, FAWE saves this file to your server directory at:
   `plugins/FastAsyncWorldEdit/schematics/my_first_dungeon.schem`
3. **Moving the file:** SinceDungeon Premium needs direct access to this file. Go to your server's file manager and move/copy the `.schem` file to:
   `plugins/SinceDungeon-PremiumAddon/schematics/my_first_dungeon.schem`
   *(Create the `schematics` folder if it doesn't exist).*

---

## ⚙️ Step 4: Configuring the Dungeon Template

Now, let's tell SinceDungeon to use this schematic instead of cloning an entire world.

1. Navigate to `plugins/SinceDungeon/templates/` and create (or edit) `my_first_dungeon.yml`.
2. Configure the essential settings block:

```yaml
settings:
  # Set the provider to SCHEMATIC to activate Premium Addon features
  provider: "SCHEMATIC"
  
  # The world name doesn't matter for schematics, but it's best to keep it matching for clarity
  world: "my_first_dungeon"
  
  # The spawn location relative to where you stood during //copy
  # If you stood exactly on the spawn pad when copying, X and Z will be 0.
  # The Y coordinate should generally match the Y-level of the floor you stood on.
  start-location: "0, 64, 0"
  
  # Optional: What happens if a player dies and runs out of lives? (SPECTATE, KICK, FAIL)
  death-action: "RESPAWN"
```

### 🧭 Understanding Coordinates in Config

SinceDungeon calculates all your action coordinates relative to the "Center Origin" (the spot where you typed `//copy`).

- If you want a mob to spawn 10 blocks East (+X) of the spawn pad, you set the coordinate to `10, Y, 0`.
- If you stood at `Y=64` when copying, and you want to spawn a boss at the exact same height, you will use `Y=64` in your configs.

---

## 🛠️ Step 5: Creating Stages and Actions

A dungeon is made of stages. Let's create a simple 2-stage dungeon.
Add the `stages:` section to your `my_first_dungeon.yml`.

```yaml
stages:
  stage_1:
    - type: REACH_LOCATION
      target: "10, 64, 0"       # Target location to step on
      radius: 3.0               # The circular radius the player must enter
      start_message:
        - "&aObjective: Move to the glowing green pad!"
        
  stage_2:
    - type: SPAWN_WAVE
      mobs:
        - type: ZOMBIE
          amount: 5
          location: "0, 64, 15"
      time-limit: 120           # Players have 120 seconds to clear this wave
      start_message:
        - "&cObjective: Ambush! Defeat all zombies to survive!"
```

In the schematic provider, when players join, the premium addon will perfectly calculate these coordinates in the isolated grid of the shared world, ensuring no two parties ever interact with each other's zombies!

---

## ⚔️ Step 6: Spawning MythicMobs

SinceDungeon seamlessly integrates with MythicMobs. You can use MythicMobs to create epic boss battles within your schematic dungeon.

```yaml
stages:
  stage_3_boss:
    - type: BOSS_BATTLE
      boss-type: "MYTHICMOBS"   # Crucial: Set this to use MythicMobs instead of Vanilla
      boss-id: "SkeletonKing"   # The internal name of the mob in your MythicMobs config
      location: "0, 65, 30"
      start_message:
        - "&4[BOSS] The Skeleton King has awakened!"
```

---

## 🔧 Advanced Premium Configurations

The SinceDungeon Premium Addon has its own config file located at `plugins/SinceDungeon-PremiumAddon/premium-config.yml`.

Here are the key settings you can tweak:

```yaml
instancing:
  schematic:
    # Set to true to overwrite existing blocks with air from the schematic.
    # Set to false to only paste non-air blocks (faster, but doesn't clear terrain).
    paste-air: true
    
    # Maximum number of concurrent parties allowed in the shared schematic world.
    # Increase this if you have a large server. 
    max-concurrent-runs: 100
    
    shared-world:
      # The name of the world where all schematic dungeons will be pasted.
      name: "SDPremium_Schematic"
      
      # Distance (in blocks) between each concurrent dungeon instance.
      # Make sure this is LARGER than the size of your biggest schematic!
      grid-spacing: 1000
```

---

## 🗺️ How the Grid System Works

Instead of lagging the server by copying entire worlds, the Premium Addon generates a single flat world (default: `SDPremium_Schematic`).

When a party types `/dungeon join`, the addon:
1. Calculates the next available empty grid slot (e.g., `X: 0, Z: 0`, then `X: 1000, Z: 0`, then `X: 0, Z: 1000`).
2. Asynchronously reads the `.schem` file to prevent main-thread lag.
3. Synchronously pastes the schematic into the designated grid slot perfectly matching your `start-location` setup.
4. Generates a virtual bounding box to lock players inside.
5. When the game ends, it uses a highly-optimized FAWE operation to clear the grid slot (`//set air`), making it available for the next party.

This system guarantees **zero memory leaks** and **virtually no lag**, making it completely safe for massive Folia and Paper networks!

---

## ❓ Troubleshooting & FAQs

**Q: My player spawns in the void or falls indefinitely!**
> Your `start-location` in the `.yml` template doesn't match the Y-level of the floor. Remember that the Y-coordinate is absolute to where the schematic was pasted. Adjust the Y value in your `start-location` (e.g., change `0, 0, 0` to `0, 64, 0`) until you land cleanly on the floor.

**Q: The schematic isn't pasting, or I get FAWE errors in console.**
> Ensure you are using a compatible version of FastAsyncWorldEdit (FAWE) designed for your specific server jar (Paper/Folia). Standard WorldEdit may cause asynchronous thread violations, especially on Folia servers.

**Q: "Template schematic file not found" warning in console.**
> Double-check that the `.schem` file is inside `plugins/SinceDungeon-PremiumAddon/schematics/` and that the exact file name (without `.schem`) perfectly matches the template name in SinceDungeon.

**Q: Players are glitching or "rubberbanding" back to their location.**
> The dungeon bounding box is too small. Increase `instance-radius` in your `config.yml` or dungeon template so the anti-escape system doesn't accidentally trigger inside your arena. Also, ensure you have the latest Premium Addon version installed, which includes fixes for Folia threading.
