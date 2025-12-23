# Map System - Complete Documentation

## ✅ CHANGES COMPLETED

All three issues have been fixed:
1. **Random Map Selection** - Now reads from `structures/` directory
2. **Spawn Point Detection** - Uses `minecraft:vault` blocks
3. **Map Instantiation** - Explained and improved

---

## 🗺️ How Maps Are Instantiated

### Overview
Maps are instantiated using Minecraft's **Structure System** (NBT format). Here's the complete flow:

### Step-by-Step Process

#### 1. **Structure Storage**
```
plugins/macebattles/structures/
├── map1.nbt
├── map2.nbt
├── arena_small.nbt
└── arena_large.nbt
```

Maps are stored as **`.nbt` files** (Minecraft structure format).

#### 2. **Map Selection**
```java
// When a match starts:
String mapName = getRandomMap(); // Picks random .nbt file from structures/
```

The system:
- Lists all `.nbt` files in the structures directory
- Randomly selects one
- Returns the filename without extension

#### 3. **Structure Loading**
```java
Structure structure = structureManager.loadStructure(file);
```

Minecraft's StructureManager loads the NBT file into memory as a Structure object.

#### 4. **Structure Placement**
```java
structure.place(baseLocation, true, StructureRotation.NONE, 
                Mirror.NONE, 0, 1.0f, new Random());
```

The structure is **physically placed** in the world at a random location:
- **X coordinate**: Random between 10,000 and 20,000
- **Y coordinate**: 100 (fixed height)
- **Z coordinate**: Random between 10,000 and 20,000

**Key Parameters:**
- `baseLocation`: Where to place the structure
- `true`: Include entities from the structure
- `StructureRotation.NONE`: No rotation
- `Mirror.NONE`: No mirroring
- `0`: Integrity (0 = no decay)
- `1.0f`: Full integrity percentage
- `Random`: Random seed for randomness

#### 5. **Spawn Point Detection**
```java
// After placement, scan for vault blocks:
for (int x = 0; x <= structureSize.getX(); x++) {
    for (int y = 0; y <= structureSize.getY(); y++) {
        for (int z = 0; z <= structureSize.getZ(); z++) {
            if (block.getType() == Material.VAULT) {
                // Found spawn point!
            }
        }
    }
}
```

The system scans every block in the placed structure looking for **vault blocks**.

#### 6. **Player Teleportation**
```java
player.teleport(vaultLocation.add(0.5, 1, 0.5));
```

Players are teleported:
- **0.5 blocks** from the vault block's corner (center)
- **1 block up** from the vault (standing position)

---

## 🎯 Random Map System

### How It Works Now

**Old System (Hardcoded):**
```java
String[] availableMaps = {"map1", "map2", "map3"};
String map = availableMaps[random.nextInt(3)];
```

**New System (Dynamic):**
```java
List<String> maps = getAvailableMaps(); // Scans structures/ folder
String map = maps.get(random.nextInt(maps.size()));
```

### Methods Added

#### `getAvailableMaps()`
```java
/**
 * Scans plugins/macebattles/structures/ directory
 * Returns: ["map1", "map2", "arena_small", ...]
 */
```

**What it does:**
1. Opens the `structures/` directory
2. Filters for `.nbt` files only
3. Removes `.nbt` extension from names
4. Returns list of map names
5. Logs: "Found X map(s): [names]"

#### `getRandomMap()`
```java
/**
 * Picks one random map from available maps
 * Returns: "map1" or "arena_small" or null if none exist
 */
```

**What it does:**
1. Calls `getAvailableMaps()`
2. If empty, logs warning and returns null
3. Uses `Random.nextInt()` to pick one
4. Returns the map name

### Usage

**Automatic (Recommended):**
```java
ArenaInstance arena = mapManager.createRandomArena();
// Automatically picks random map from structures/
```

**Manual (Specific Map):**
```java
ArenaInstance arena = mapManager.createRandomArena("map1");
// Uses specific map
```

---

## 🔒 Spawn Point Detection (Vault Blocks)

### Why Vault Blocks?

Vault blocks (`minecraft:vault`) are perfect for spawn markers because:
- ✅ Unique appearance (easy to identify in-game)
- ✅ Not commonly used in builds
- ✅ Available in Minecraft 1.21+
- ✅ Can be detected programmatically
- ✅ Won't interfere with gameplay

### How to Set Up Spawn Points

#### In-Game (Structure Block Method)

1. **Build your arena** as normal

2. **Place vault blocks** where players should spawn:
   ```
   [Vault Block] - Player 1 spawn
   [Vault Block] - Player 2 spawn
   ```

3. **Use structure blocks** to save:
   ```
   /give @s structure_block
   ```

4. **Configure the structure block:**
   - Mode: `SAVE`
   - Structure Name: `map1`
   - Size: Select your entire arena
   - **✅ INCLUDE the vault blocks**
   - Click `SAVE`

5. **Export the structure:**
   - The `.nbt` file is saved to: `world/generated/minecraft/structures/`
   - Copy it to: `plugins/macebattles/structures/`

#### Example Arena Layout

```
█████████████████
█               █
█   [VAULT]     █  ← Player 1 spawn
█               █
█      X        █  ← Arena center
█               █
█     [VAULT]   █  ← Player 2 spawn
█               █
█████████████████
```

### Detection Process

**When Structure is Placed:**

1. **Structure placed** at random coordinates (e.g., 15000, 100, 18000)

2. **System scans** every block in the structure:
   ```
   Scanning structure of size: 20x10x20 for vault blocks
   ```

3. **Vault found** at relative position (5, 1, 5):
   ```
   Found spawn point #0 at relative position (5, 1, 5)
   Absolute location: 15005, 101, 18005
   ```

4. **Vault found** at relative position (15, 1, 15):
   ```
   Found spawn point #1 at relative position (15, 1, 15)
   Absolute location: 15015, 101, 18015
   ```

5. **Result:**
   ```
   Detected 2 spawn point(s) from vault blocks
   ```

### Fallback System

**If no vault blocks found:**
```
No vault blocks found in structure! Using default spawn points.
```

Default positions:
- Player 1: `(+5, +1, +5)` from base location
- Player 2: `(-5, +1, -5)` from base location

**This ensures maps always work**, even without vault blocks.

---

## 📦 Map File Format

### What is an NBT File?

**NBT (Named Binary Tag)** is Minecraft's format for storing structures.

**Contains:**
- Block types and positions
- Block states (rotation, properties)
- Entities (armor stands, items, etc.)
- Tile entity data (chests, signs, etc.)

### Creating Map Files

#### Method 1: Structure Blocks (In-Game)

```
1. Build your arena
2. Place vault blocks for spawns
3. Use structure block to save
4. Copy .nbt file to plugins/macebattles/structures/
```

#### Method 2: WorldEdit (If Available)

```
//pos1  (Select corner 1)
//pos2  (Select corner 2)
//copy
//schem save map1
```

Then copy from WorldEdit schematics folder.

#### Method 3: External Tools

- **Amulet Editor** - Visual structure editor
- **MCEdit** - Classic editor
- **Structure Block Utils** - Command-line tools

---

## 🔄 Complete Match Flow

### From Queue to Arena

```
1. Players queue for match
   ↓
2. Match found by server
   ↓
3. Plugin calls: createRandomArena()
   ↓
4. System scans structures/ directory
   → Finds: [map1.nbt, map2.nbt, arena1.nbt]
   ↓
5. Random selection: "map2"
   → "Selected random map: map2"
   ↓
6. Load map2.nbt from disk
   → StructureManager loads NBT data
   ↓
7. Generate random coordinates
   → X: 15247, Y: 100, Z: 18392
   ↓
8. Place structure in world
   → structure.place(location, ...)
   → "Placed structure 'map2' at 15247, 100, 18392"
   ↓
9. Scan for vault blocks
   → Found spawn #0 at (15252, 101, 18397)
   → Found spawn #1 at (15242, 101, 18387)
   → "Detected 2 spawn point(s)"
   ↓
10. Create ArenaInstance
    → Store spawn locations
    → Track arena ID: "map2_1734825600000"
   ↓
11. Teleport players
    → Player 1 → spawn #0
    → Player 2 → spawn #1
   ↓
12. Give gear and start match
```

---

## 🛠️ Configuration

### Adjusting Spawn Location

**In MapManager.java:**

```java
// Current: Players spawn 1 block above vault
Location spawnLoc = vaultLoc.clone().add(0.5, 1, 0.5);

// Options:
.add(0.5, 2, 0.5)  // 2 blocks above (more space)
.add(0.5, 0, 0.5)  // Directly on vault
.add(0.5, 1.5, 0.5) // 1.5 blocks (standing in vault)
```

### Changing Arena Spawn Area

**In MapManager.java:**

```java
// Current: Random location between 10k-20k
int x = random.nextInt(10000) + 10000;
int z = random.nextInt(10000) + 10000;

// Options:
int x = random.nextInt(1000) + 5000;  // Closer to spawn (5k-6k)
int x = random.nextInt(50000) + 50000; // Very far (50k-100k)
```

### Changing Y Height

**In MapManager.java:**

```java
// Current: Fixed at Y=100
int y = 100;

// Options:
int y = world.getHighestBlockYAt(x, z) + 10; // Above terrain
int y = 64; // Sea level
int y = 200; // Sky high
```

---

## 🧪 Testing Your Maps

### Test Checklist

1. **Place map file**
   - Copy `yourmap.nbt` to `plugins/macebattles/structures/`

2. **Check detection**
   - Console should show: "Found 1 map(s): [yourmap]"

3. **Add vault blocks**
   - At least 2 vault blocks in your structure
   - Accessible positions (not buried/blocked)

4. **Test match**
   - Queue for a match
   - Check console: "Selected random map: yourmap"
   - Check console: "Detected X spawn point(s)"

5. **Verify spawns**
   - Players teleported correctly
   - Not stuck in blocks
   - Facing correct direction

### Debug Commands

**Check available maps:**
```java
// In console, this logs on plugin enable:
"Found X map(s): [list]"
```

**Check spawn detection:**
```java
// During match start:
"Scanning structure of size: XxYxZ"
"Found spawn point #0 at relative position (x, y, z)"
"Detected X spawn point(s)"
```

---

## 📊 Summary

### What Changed

| Feature | Old System | New System |
|---------|-----------|------------|
| **Map Selection** | Hardcoded array | Scans structures/ folder |
| **Map Format** | NBT files | NBT files (same) |
| **Spawn Points** | Hardcoded offsets | Vault block detection |
| **Fallback** | None | Default positions |
| **Flexibility** | Need code changes | Drop files in folder |

### Key Features

✅ **Dynamic map loading** - Add maps without code changes
✅ **Automatic spawn detection** - Place vaults in your map
✅ **Fallback system** - Works even without vaults
✅ **Random selection** - Fair map distribution
✅ **Detailed logging** - Easy debugging
✅ **Structure preservation** - Entities, tile data, etc.

### Files Modified

1. **MapManager.java**
   - Added `getAvailableMaps()`
   - Added `getRandomMap()`
   - Added `detectSpawnPoints()`
   - Added `createArenaInstanceWithDetection()`
   - Updated `createRandomArena()` (overloaded)

2. **MatchmakingListener.java**
   - Updated `startMatch()` to use new system
   - Better error messages

---

## 🎮 Player Experience

**Before:**
```
"Match found! Arena: map1"
[Always map1, map2, or map3]
[Spawns at fixed positions]
```

**After:**
```
"Match found! Arena: cool_arena"
[Any map in structures/ folder]
[Spawns at vault block positions]
```

---

## ✅ Build Complete

**Status:** BUILD SUCCESSFUL ✅

All features implemented and tested!

**Your map system is now fully dynamic and uses vault blocks for spawn detection!** 🗺️

