# Entity Command Linking - Quick Reference

## 🎯 Quick Start

### **Link an entity to a command:**
```
/entitycmd link console duels queue ranked
[Right-click any entity]
```

### **Unlink an entity:**
```
/entitycmd unlink
[Right-click the linked entity]
```

### **List all links:**
```
/entitycmd list
```

---

## 📝 Command Format

```
/entitycmd link <console|player> <command>
```

**Executor Types:**
- `console` - Runs as console (bypasses permissions)
- `player` - Runs as clicking player (needs permissions)

**Placeholders:**
- `%player%` - Player's name
- `%uuid%` - Player's UUID

---

## 💡 Common Examples

### **Ranked Queue NPC:**
```
/summon villager ~ ~ ~ {CustomName:'{"text":"§6Ranked Queue"}',NoAI:1b}
/entitycmd link console duels queue ranked
```

### **Casual Queue NPC:**
```
/summon villager ~ ~ ~ {CustomName:'{"text":"§eCasual Queue"}',NoAI:1b}
/entitycmd link console duels queue casual
```

### **Spawn Teleport:**
```
/summon armor_stand ~ ~ ~ {CustomName:'{"text":"§aSpawn"}',Invisible:1b}
/entitycmd link player spawn
```

### **Give Item:**
```
/entitycmd link console give %player% diamond 5
```

### **Teleport Player:**
```
/entitycmd link console tp %player% 100 64 200
```

### **Send Message:**
```
/entitycmd link console tellraw %player% {"text":"Welcome!"}
```

---

## 🎮 Workflow

1. **Create NPC** (optional):
   ```
   /summon villager ~ ~ ~ {CustomName:'...',NoAI:1b}
   ```

2. **Start linking**:
   ```
   /entitycmd link console <command>
   ```

3. **Click entity** to link it

4. **Test** - Have player click entity

5. **Unlink** if needed:
   ```
   /entitycmd unlink
   [Click entity]
   ```

---

## ✅ Tips

- Use **villagers** for visible NPCs
- Use **armor stands** with Invisible:1b for hidden NPCs
- Set **NoAI:1b** to prevent entities from moving
- Use **console** executor for admin commands
- Use **player** executor for player commands
- Add **CustomNameVisible:1b** to show names above entities

---

## 🔧 Aliases

All of these work the same:
- `/entitycmd`
- `/ecmd`
- `/entitycommand`

---

## 🎯 Example Matchmaking Hub

```bash
# Ranked queue villager
/summon villager 100 64 200 {CustomName:'{"text":"§6§lRanked"}',NoAI:1b,Profession:"cleric"}
/entitycmd link console duels queue ranked

# Casual queue villager  
/summon villager 105 64 200 {CustomName:'{"text":"§e§lCasual"}',NoAI:1b,Profession:"librarian"}
/entitycmd link console duels queue casual

# Spawn teleport
/summon villager 110 64 200 {CustomName:'{"text":"§b§lSpawn"}',NoAI:1b}
/entitycmd link player spawn
```

---

**Build Status:** ✅ SUCCESSFUL
**System Ready:** Operators can now create interactive NPCs! 🎮

