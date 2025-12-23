# Entity Command Linking System

## ✅ Complete System Implemented

Operators can now link entities (NPCs, armor stands, villagers, etc.) to execute commands when right-clicked by players!

---

## 🎮 How It Works

### **Basic Flow:**
```
1. Op runs: /entitycmd link console duels queue ranked
2. Op right-clicks an entity (villager, armor stand, etc.)
3. Entity is now linked to that command
4. Any player right-clicks that entity
5. Command executes automatically!
```

---

## 📝 Commands

### **/entitycmd link <console|player> <command>**

Links an entity to execute a command when clicked.

**Executor Types:**
- `console` - Runs command as console (bypasses permissions)
- `player` - Runs command as the clicking player (requires player permissions)

**Examples:**
```
/entitycmd link console duels queue ranked
/entitycmd link player spawn
/entitycmd link console give %player% diamond 1
/entitycmd link player warp arena
```

**After running the command:**
1. You'll enter "linking mode"
2. Right-click any entity to link it
3. Players can now click that entity to run the command

---

### **/entitycmd unlink**

Removes the command link from an entity.

**Usage:**
```
/entitycmd unlink
[Right-click the entity you want to unlink]
```

---

### **/entitycmd list**

Shows all currently linked entities and their commands.

**Output:**
```
§e§l========== Entity Command Links ==========
§aVILLAGER §7at 100, 64, 200
  §7UUID: abc123...
  §7Command: §bduels queue ranked
  §7Executor: §eConsole

§aARMOR_STAND §7at 105, 64, 205
  §7UUID: def456...
  §7Command: §bspawn
  §7Executor: §ePlayer
```

---

## 🎯 Use Cases

### **Queue NPC for Matchmaking:**
```
/entitycmd link console duels queue ranked
[Right-click a villager]
```
**Result:** Players can click the villager to join ranked queue

### **Spawn Teleport NPC:**
```
/entitycmd link player spawn
[Right-click an armor stand]
```
**Result:** Clicking runs /spawn as the player

### **Shop NPC:**
```
/entitycmd link console shop open %player%
[Right-click a villager]
```
**Result:** Opens shop GUI for the clicking player

### **Casual Queue NPC:**
```
/entitycmd link console duels queue casual
[Right-click a different villager]
```
**Result:** Players can click to join casual queue

### **Duel Challenge NPC:**
```
/entitycmd link console give %player% stone_sword 1
[Right-click entity]
```
**Result:** Gives clicking player a stone sword

---

## 📊 Placeholders

Use these in your commands to insert player data:

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `%player%` | Player's name | `Steve` |
| `%uuid%` | Player's UUID | `abc123-def456...` |

**Example with placeholders:**
```
/entitycmd link console give %player% diamond 5
/entitycmd link console tp %player% 100 64 200
/entitycmd link console tellraw %player% {"text":"Hello!"}
```

---

## 🔧 Technical Details

### **Entity Types You Can Link:**

Works with ANY entity type:
- ✅ Villagers (perfect for NPCs)
- ✅ Armor Stands (invisible NPCs)
- ✅ Animals (cows, sheep, etc.)
- ✅ Mobs (zombies, skeletons, etc.)
- ✅ Item Frames
- ✅ Any other entity

**Recommended:** Use villagers or armor stands for best NPC experience.

### **Console vs Player Execution:**

**Console Executor:**
```
/entitycmd link console duels queue ranked
```
- Runs command as console
- Bypasses all permissions
- Use for admin commands
- More powerful but less flexible

**Player Executor:**
```
/entitycmd link player spawn
```
- Runs command as the clicking player
- Player needs permission for the command
- Use for player-specific commands
- More secure

### **Event Priority:**

The EntityCommandListener uses normal priority and runs AFTER the BlockBreakProtectionListener, so:
- ✅ Linked entities execute commands
- ✅ Non-linked entities behave normally (per protection rules)
- ✅ No conflicts with other systems

---

## 🎨 Creating NPC Setups

### **Matchmaking Lobby Setup:**

**1. Create Ranked Queue NPC:**
```
/summon villager 100 64 200 {CustomName:'{"text":"§6Ranked Queue"}',CustomNameVisible:1b,NoAI:1b}
/entitycmd link console duels queue ranked
[Right-click the villager]
```

**2. Create Casual Queue NPC:**
```
/summon villager 105 64 200 {CustomName:'{"text":"§eCasual Queue"}',CustomNameVisible:1b,NoAI:1b}
/entitycmd link console duels queue casual
[Right-click the villager]
```

**3. Create Info NPC:**
```
/summon villager 110 64 200 {CustomName:'{"text":"§bInformation"}',CustomNameVisible:1b,NoAI:1b}
/entitycmd link console tellraw %player% {"text":"Welcome to MaceBattles!"}
[Right-click the villager]
```

### **Invisible Armor Stand NPCs:**
```
/summon armor_stand 100 64 200 {CustomName:'{"text":"§6Click to Queue"}',CustomNameVisible:1b,Invisible:1b,NoGravity:1b,Marker:1b}
/entitycmd link console duels queue ranked
[Right-click the armor stand]
```

---

## 🛡️ Integration with Protection System

The entity command system integrates seamlessly:

**For Linked Entities:**
- Right-clicks execute the command
- Default entity interaction is cancelled
- Works for all players (not just ops)

**For Non-Linked Entities:**
- BlockBreakProtectionListener rules apply
- Cannot damage/interact (as configured)
- Ops can still interact normally

**In Matches:**
- Linked entities still work
- Players can click NPCs during matches
- Combat works normally (not affected)

---

## 📋 Complete Examples

### **Example 1: Matchmaking Hub**

```bash
# Create ranked queue NPC
/summon villager 100 64 200 {CustomName:'{"text":"§6§lRanked Queue"}',CustomNameVisible:1b,NoAI:1b,Profession:"cleric"}
/entitycmd link console duels queue ranked

# Create casual queue NPC
/summon villager 105 64 200 {CustomName:'{"text":"§e§lCasual Queue"}',CustomNameVisible:1b,NoAI:1b,Profession:"librarian"}
/entitycmd link console duels queue casual

# Create spawn teleport NPC
/summon villager 110 64 200 {CustomName:'{"text":"§b§lReturn to Spawn"}',CustomNameVisible:1b,NoAI:1b,Profession:"nitwit"}
/entitycmd link player spawn
```

### **Example 2: Shop System**

```bash
# Weapon shop
/summon villager 50 64 50 {CustomName:'{"text":"§c§lWeapon Shop"}',CustomNameVisible:1b,NoAI:1b,Profession:"weaponsmith"}
/entitycmd link console shop open weapons %player%

# Armor shop
/summon villager 55 64 50 {CustomName:'{"text":"§9§lArmor Shop"}',CustomNameVisible:1b,NoAI:1b,Profession:"armorer"}
/entitycmd link console shop open armor %player%
```

### **Example 3: Warp Points**

```bash
# Arena warp
/summon armor_stand 0 64 0 {CustomName:'{"text":"§6Teleport to Arena"}',CustomNameVisible:1b,Invisible:1b,NoGravity:1b}
/entitycmd link console tp %player% 1000 64 1000

# Spawn warp
/summon armor_stand 5 64 0 {CustomName:'{"text":"§aBack to Spawn"}',CustomNameVisible:1b,Invisible:1b,NoGravity:1b}
/entitycmd link player spawn
```

---

## 🧪 Testing

### **Test Basic Linking:**
```
1. /entitycmd link console say Hello from %player%
2. Right-click any entity
3. Try clicking it as a regular player
4. Should see message in console ✓
```

### **Test Unlinking:**
```
1. /entitycmd unlink
2. Right-click the linked entity
3. Try clicking it as regular player
4. Should do nothing ✓
```

### **Test List Command:**
```
1. Link a few entities
2. /entitycmd list
3. Should see all links with details ✓
```

### **Test Placeholders:**
```
1. /entitycmd link console give %player% diamond 1
2. Link to entity
3. Click as player
4. Should receive diamond ✓
```

---

## 🔍 Troubleshooting

### **"You must be an operator to use this command!"**
- Only ops can manage entity links
- Use `/op YourName` to become op

### **Entity doesn't respond to clicks**
- Check `/entitycmd list` to verify link exists
- Make sure you clicked the right entity
- Check console for errors

### **Command doesn't execute**
- Verify command syntax is correct
- Test command manually first
- Check console for command errors
- Ensure placeholders are correct

### **Entity disappeared**
- Entity may have been killed/removed
- Link will persist but entity not found
- Use `/entitycmd list` to see UUID
- Remove link with `/entitycmd unlink`

---

## 📊 Files Created

### **EntityCommandLink.java**
- Data class for entity-command links
- Stores UUID, command, executor type
- Handles placeholder replacement

### **EntityCommandListener.java**
- Handles right-clicks on linked entities
- Executes commands
- Supports console/player execution

### **EntityLinkingListener.java**
- Handles linking/unlinking mode
- Processes entity clicks while in mode
- Provides feedback to operators

### **EntityCommandCommand.java**
- `/entitycmd` command handler
- Subcommands: link, unlink, list, help
- Tab completion support

### **Macebattles.java (Modified)**
- Added entity link storage
- Added linking mode tracking
- Added management methods
- Registered listeners and commands

### **plugin.yml (Modified)**
- Added entitycmd command definition
- Aliases: ecmd, entitycommand

---

## ✅ Summary

**Features:**
- ✅ Link any entity to any command
- ✅ Console or player execution modes
- ✅ Placeholder support (%player%, %uuid%)
- ✅ Easy linking/unlinking workflow
- ✅ List all links with details
- ✅ Works with any entity type
- ✅ Integrates with protection system
- ✅ Perfect for NPCs and interactive elements

**Commands:**
- ✅ `/entitycmd link <console|player> <command>`
- ✅ `/entitycmd unlink`
- ✅ `/entitycmd list`
- ✅ Tab completion
- ✅ Help command

**Use Cases:**
- ✅ Matchmaking queue NPCs
- ✅ Teleport stations
- ✅ Shop NPCs
- ✅ Info points
- ✅ Custom interactions

**Build Status:** ✅ SUCCESSFUL

**Operators can now create interactive NPCs and command-linked entities!** 🎮

