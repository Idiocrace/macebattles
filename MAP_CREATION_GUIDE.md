# Quick Guide: Creating a Map for MaceBattles

## 📝 Step-by-Step Map Creation

### Requirements
- Minecraft 1.21+ (for vault blocks)
- Access to structure blocks
- Your arena built in-game

---

## 🏗️ Method 1: Structure Blocks (Recommended)

### Step 1: Build Your Arena
```
Build your arena in creative mode
Make it any size you want (recommended: 20x20 to 50x50)
```

### Step 2: Place Spawn Points
```
Get vault blocks:
/give @s vault 2

Place them where players should spawn:
- Place vault #1 for Player 1
- Place vault #2 for Player 2
- Can place more (up to as many players as you want to support)
```

**Example Layout:**
```
┌─────────────────────┐
│                     │
│  🔒 (Vault)         │  ← Player 1
│                     │
│         ⚔️          │  ← Battle area
│                     │
│         🔒 (Vault)  │  ← Player 2
│                     │
└─────────────────────┘
```

### Step 3: Get Structure Block
```
/give @s structure_block
```

### Step 4: Place Structure Block
```
Place it at one corner of your arena
Right-click to open the interface
```

### Step 5: Configure Structure Block
```
1. Mode: SAVE
2. Structure Name: yourmap_name
3. Click "DETECT" or manually enter size
4. Make sure it includes:
   ✅ Your entire arena
   ✅ Both vault blocks
   ✅ All decorations
5. Click "SAVE"
```

### Step 6: Export the Structure
```
The structure is saved to:
world/generated/minecraft/structures/yourmap_name.nbt

Copy this file to:
plugins/macebattles/structures/yourmap_name.nbt
```

### Step 7: Test
```
1. Restart your plugin or server
2. Check console: "Found X map(s): [yourmap_name]"
3. Queue for a match
4. Players should spawn on the vault blocks!
```

---

## 🎨 Design Tips

### Arena Size
```
Small:  15x15 to 25x25  (Fast-paced, close combat)
Medium: 25x25 to 40x40  (Balanced)
Large:  40x40 to 60x60  (Strategic, lots of movement)
```

### Spawn Placement
```
Good:
✅ 10-20 blocks apart
✅ Clear line of sight
✅ Same elevation
✅ Not in corners (predictable)

Bad:
❌ Too close (instant combat)
❌ Obstructed views
❌ Uneven terrain
❌ One spawn has advantage
```

### Height Variations
```
✅ Platforms at different levels
✅ Stairs and slopes
✅ Jump-up spots
❌ Avoid deep pits (falling = unfair)
❌ Avoid ceiling (wind charges need space)
```

### Obstacles
```
Good obstacles:
✅ Low walls (cover but can jump over)
✅ Pillars (break line of sight)
✅ Small platforms
✅ Decorative blocks

Avoid:
❌ Complete walls (splits arena)
❌ Too many obstacles (cluttered)
❌ Exploitable spots (camping)
```

---

## 🔒 Vault Block Placement

### Rules
1. **Minimum 2 vaults** (one per player)
2. **Place at ground level** (players spawn 1 block above)
3. **Accessible location** (not in walls/blocks)
4. **Clear space above** (2-3 blocks of air)

### Good Placement
```
   Air
   Air  
 [Vault]  ← Player spawns here (1 block up)
 Ground
```

### Bad Placement
```
  Block  ← Player stuck in block!
 [Vault]
 Ground
```

---

## 🧪 Testing Your Map

### In-Game Test Commands

**Give yourself vault blocks:**
```
/give @s vault 64
```

**Teleport to test location:**
```
/tp @s 15000 100 18000
```

**Check spawn points:**
```
Stand on your vault blocks
Make sure there's space to stand
Jump test - ensure no ceiling issues
```

### Plugin Test

**After placing the .nbt file:**

1. **Restart server/plugin**
   ```
   /reload or restart server
   ```

2. **Check console logs:**
   ```
   [INFO] Found X map(s): [yourmap]
   ```

3. **Queue for match:**
   ```
   /duels queue casual
   ```

4. **Watch console:**
   ```
   [INFO] Selected random map: yourmap
   [INFO] Placed structure 'yourmap' at X, Y, Z
   [INFO] Scanning structure of size: XxYxZ
   [INFO] Found spawn point #0 at (x, y, z)
   [INFO] Found spawn point #1 at (x, y, z)
   [INFO] Detected 2 spawn point(s)
   ```

---

## 🐛 Troubleshooting

### "No maps found in structures directory!"
**Solution:**
- Check file is in `plugins/macebattles/structures/`
- File extension must be `.nbt`
- Check file permissions (readable)

### "No vault blocks found in structure!"
**Solution:**
- Re-save structure including vault blocks
- Make sure vaults are in the saved area
- Check structure block selection size
- System will use fallback spawn points

### "Failed to create arena!"
**Solution:**
- Check console for error details
- Ensure .nbt file isn't corrupted
- Try re-saving the structure
- Check world exists (default: "world")

### "Players spawn in blocks"
**Solution:**
- Vault blocks need 2+ air blocks above
- Adjust spawn offset in MapManager.java
- Rebuild map with better spawn placement

### "Players too close/far"
**Solution:**
- Adjust vault block positions in your map
- Rebuild and re-save structure
- Test spawn distance before saving

---

## 📦 Example Maps to Start With

### Simple Box Arena
```
Size: 20x10x20
Features:
- Flat ground
- 2 vaults 15 blocks apart
- No obstacles
- Easy to build, good for testing
```

### Platform Arena
```
Size: 30x15x30
Features:
- Multiple height levels
- 2 vaults on main platform
- Stairs connecting platforms
- More strategic
```

### Obstacle Course Arena
```
Size: 40x12x40
Features:
- Low walls for cover
- Pillars scattered
- 2 vaults with clear paths
- Encourages movement
```

---

## 💡 Pro Tips

### Multiple Maps
```
Create several maps with different themes:
- desert_arena.nbt
- ice_arena.nbt
- forest_arena.nbt
- void_arena.nbt

Players will see variety!
```

### Themed Builds
```
Match your arena to the theme:
- Desert: Sandstone, cactus
- Ice: Packed ice, snow
- Forest: Logs, leaves
- Void: End stone, purpur
```

### Testing Iterations
```
1. Build basic version → test
2. Add obstacles → test
3. Adjust spawn points → test
4. Polish details → final test
```

### Backup Your Maps
```
Keep copies of your .nbt files:
- In a backup folder
- On cloud storage
- With different versions
```

---

## ✅ Checklist Before Publishing

- [ ] Arena built and tested
- [ ] At least 2 vault blocks placed
- [ ] Vault blocks have clear space above
- [ ] Structure saved with structure block
- [ ] .nbt file copied to structures/ folder
- [ ] Plugin recognizes the map
- [ ] Spawn points detected correctly
- [ ] Players can move freely
- [ ] No stuck spots or exploits
- [ ] Balanced for both players
- [ ] Fun to play in!

---

## 🎉 You're Ready!

Your map is now:
✅ **Saved** as an NBT file
✅ **Installed** in the structures folder
✅ **Detected** by the plugin
✅ **Random selection** ready
✅ **Auto-spawn** configured

**Players will automatically play on your map!** 🗺️

---

## 📞 Need Help?

Check the console logs for:
- Map detection: "Found X map(s)"
- Structure loading: "Placed structure 'mapname'"
- Spawn detection: "Detected X spawn point(s)"

If issues persist, check `MAP_SYSTEM_DOCUMENTATION.md` for detailed technical info.

