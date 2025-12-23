# MaceBattles Map System - Quick Reference

## 🎯 Three Key Changes

### 1. Random Map Selection
- **Old**: Hardcoded `{"map1", "map2", "map3"}`
- **New**: Reads all `.nbt` files from `structures/` folder
- **Method**: `getRandomMap()` - automatically picks one

### 2. Spawn Point Detection  
- **Old**: Fixed offsets `(5, 1, 5)` and `(-5, 1, -5)`
- **New**: Scans for `minecraft:vault` blocks
- **Method**: `detectSpawnPoints()` - finds all vaults

### 3. Map Instantiation
- **Process**: NBT file → Structure object → World blocks
- **Placement**: Random location (10k-20k coordinates)
- **Detection**: Vault blocks = spawn markers

---

## 📁 File Structure

```
plugins/macebattles/
└── structures/
    ├── map1.nbt          ← Drop your map files here
    ├── map2.nbt
    ├── cool_arena.nbt
    └── ice_arena.nbt
```

---

## 🔒 Creating Spawn Points

### Step 1: Get Vault Blocks
```
/give @s vault 2
```

### Step 2: Place in Arena
```
Place where players should spawn (2 minimum)
Must have 2+ blocks of air above
15-20 blocks apart recommended
```

### Step 3: Save Structure
```
/give @s structure_block
Right-click → SAVE mode
Include the vault blocks in selection
Click SAVE
```

### Step 4: Export
```
Copy from: world/generated/minecraft/structures/yourmap.nbt
Copy to:   plugins/macebattles/structures/yourmap.nbt
```

---

## 🔄 How It Works

```
Match Starts
    ↓
System scans: plugins/macebattles/structures/
    ↓
Finds: [map1.nbt, map2.nbt, cool_arena.nbt]
    ↓
Random pick: cool_arena.nbt
    ↓
Load NBT file
    ↓
Place at: (15247, 100, 18392)
    ↓
Scan for vault blocks
    ↓
Found 2 vaults → 2 spawn points
    ↓
Teleport players to spawns
    ↓
Match begins!
```

---

## 📊 Console Logs

### On Plugin Start
```
[INFO] Created structures directory at: plugins/macebattles/structures
[INFO] Found 3 map(s): [map1, map2, cool_arena]
```

### On Match Start
```
[INFO] Selected random map: cool_arena
[INFO] Placed structure 'cool_arena' at 15247, 100, 18392
[INFO] Scanning structure of size: 30x15x30 for vault blocks
[INFO] Found spawn point #0 at relative position (10, 1, 8)
[INFO] Found spawn point #1 at relative position (20, 1, 22)
[INFO] Detected 2 spawn point(s) from vault blocks
```

---

## ⚙️ Key Methods

### MapManager.java

```java
getAvailableMaps()
// Returns list of all .nbt files in structures/

getRandomMap()
// Picks one random map from available maps

detectSpawnPoints(location, structure)
// Scans structure for vault blocks
// Returns spawn locations

createRandomArena()
// Complete flow: pick map → load → place → detect → return arena
```

---

## 🐛 Troubleshooting

### No maps found
- Check: `plugins/macebattles/structures/` exists
- Check: Files have `.nbt` extension
- Check: Files are readable

### No spawn points detected
- Check: Vault blocks in structure
- Check: Structure block included vaults
- System uses fallback if none found

### Players stuck in blocks
- Check: Vaults have air above (2+ blocks)
- Players spawn 1 block above vault
- Rebuild map if needed

---

## ✅ Checklist

- [ ] `structures/` folder exists
- [ ] At least one `.nbt` file present
- [ ] Map includes 2+ vault blocks
- [ ] Vaults have clear space above
- [ ] Console shows map detection
- [ ] Match starts successfully
- [ ] Players spawn correctly

---

## 🎮 Testing

### Quick Test
```bash
1. Place yourmap.nbt in structures/
2. Restart plugin: /reload
3. Check console: "Found X map(s)"
4. Queue: /duels queue casual
5. Verify spawns work
```

---

## 📝 Notes

- **Unlimited maps**: Just drop files in folder
- **No code changes**: Plugin auto-detects
- **Any map size**: 20x20 to 60x60 recommended
- **Fallback system**: Works without vaults
- **Multiple spawns**: 2+ players supported

---

## 📚 Full Documentation

See detailed guides:
- `MAP_SYSTEM_DOCUMENTATION.md` - Technical details
- `MAP_CREATION_GUIDE.md` - Step-by-step creation
- `map_system_complete.md` - Complete summary

---

**Your map system is ready!** 🗺️

