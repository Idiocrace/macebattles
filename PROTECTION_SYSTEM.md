# Global Protection System

## ✅ Complete Server Protection Implemented

Your server now has comprehensive protection against all destructive actions.

---

## 🛡️ What's Protected

### **1. Block Breaking** ❌
- Players cannot break any blocks
- Exception: Ops in creative mode

### **2. Block Placement** ❌
- Players cannot place any blocks
- Exception: Ops in creative mode

### **3. Entity Damage** ❌
- Players cannot damage any entities (mobs, animals, villagers, etc.)
- **Exception: Players in active matches CAN damage each other**
- Exception: Ops in creative mode

### **4. Environmental Damage** ❌
- Players not in matches are immune to:
  - Fall damage
  - Fire/lava damage
  - Drowning
  - Suffocation
  - Explosion damage
  - All other environmental damage
- **Exception: Players in active matches take normal damage**

### **5. Item Frames & Paintings** ❌
- Players cannot break item frames
- Players cannot break paintings
- Exception: Ops in creative mode

### **6. Vehicles** ❌
- Players cannot damage minecarts
- Players cannot damage boats
- Can still ride them
- Exception: Ops in creative mode

### **7. Armor Stands** ❌
- Players cannot manipulate armor stands
- Cannot change armor or items on them
- Exception: Ops in creative mode

### **8. Entity Interactions** ❌
- Players cannot leash entities
- Players cannot use name tags on entities
- Players CAN still ride boats/minecarts/horses
- Exception: Ops in creative mode

---

## 🎮 How It Works

### **For Regular Players:**
```
✗ Cannot break blocks
✗ Cannot place blocks
✗ Cannot damage entities
✗ Cannot break item frames
✗ Cannot damage vehicles
✗ Take no environmental damage (outside matches)
✓ Can move around freely
✓ Can ride vehicles
✓ Can interact with chests, buttons, etc.
```

### **For Players in Matches:**
```
✗ Cannot break blocks
✗ Cannot place blocks
✓ CAN damage opponents (PvP enabled)
✓ CAN take damage (combat works normally)
✓ Normal match gameplay
```

### **For Ops in Creative Mode:**
```
✓ Can break blocks
✓ Can place blocks
✓ Can damage entities
✓ Can break item frames
✓ Can manipulate armor stands
✓ Full building/admin permissions
```

---

## 🔧 Technical Details

### **Event Priorities:**

All protection events use `EventPriority.LOWEST` to run first and prevent issues:

```java
@EventHandler(priority = EventPriority.LOWEST)
public void onBlockBreak(BlockBreakEvent event) {
    // Protection logic
}
```

**Why LOWEST priority?**
- Runs before other plugins
- Ensures protection takes priority
- Prevents conflicts with other systems

### **Match Integration:**

The system checks if a player is in an active match:

```java
if (plugin.getMatchmakingListener().getMatchByPlayer(player.getUniqueId()) != null) {
    // Player is in a match, allow combat
    return;
}

// Not in match, cancel damage
event.setCancelled(true);
```

**Result:**
- PvP only works during matches
- Safe spawn/lobby areas
- No accidental damage outside arenas

---

## 📊 Protection Coverage

| Action | Regular Players | In Match | Ops (Creative) |
|--------|----------------|----------|----------------|
| **Break Blocks** | ❌ Blocked | ❌ Blocked | ✅ Allowed |
| **Place Blocks** | ❌ Blocked | ❌ Blocked | ✅ Allowed |
| **Damage Players** | ❌ Blocked | ✅ Allowed | ✅ Allowed |
| **Damage Mobs** | ❌ Blocked | ❌ Blocked | ✅ Allowed |
| **Break Frames** | ❌ Blocked | ❌ Blocked | ✅ Allowed |
| **Damage Vehicles** | ❌ Blocked | ❌ Blocked | ✅ Allowed |
| **Ride Vehicles** | ✅ Allowed | ✅ Allowed | ✅ Allowed |
| **Armor Stands** | ❌ Blocked | ❌ Blocked | ✅ Allowed |
| **Take Fall Damage** | ❌ Immune | ✅ Takes Damage | ✅ Takes Damage |
| **Take Fire Damage** | ❌ Immune | ✅ Takes Damage | ✅ Takes Damage |

---

## 🎯 Use Cases

### **Spawn/Lobby Protection:**
```
Players spawn in lobby
✓ Cannot grief spawn area
✓ Cannot attack other players
✓ Cannot damage decorations
✓ Safe exploration area
```

### **Match Arena Protection:**
```
Match starts in arena
✓ Can fight opponents
✓ Can take damage
✗ Cannot break arena blocks
✗ Cannot damage arena decorations
```

### **Admin Building:**
```
Op in creative mode
✓ Can build/edit spawn
✓ Can place decorations
✓ Can break blocks
✓ Full permissions
```

---

## 🧪 Testing

### **Test Block Protection:**
```
1. Try to break a block as regular player
2. Should NOT break ✓
3. Switch to creative mode
4. Give yourself op: /op YourName
5. Try to break block
6. Should break if you're op ✓
```

### **Test Entity Protection:**
```
1. Try to hit a villager/animal
2. Should NOT damage them ✓
3. Start a match
4. Try to hit opponent
5. Should damage them ✓
6. Match ends
7. Try to hit opponent again
8. Should NOT damage them ✓
```

### **Test Environmental Damage:**
```
1. Jump from high place
2. Should NOT take fall damage ✓
3. Start a match
4. Jump from high place
5. Should take fall damage ✓
```

### **Test Item Frame Protection:**
```
1. Try to break item frame
2. Should NOT break ✓
3. Try to rotate item in frame
4. Should NOT rotate ✓
```

---

## 📝 Files Modified

### **BlockBreakProtectionListener.java** (New)
```java
+ onBlockBreak()           - Prevents block breaking
+ onBlockPlace()           - Prevents block placement
+ onEntityDamage()         - Prevents entity damage (except in matches)
+ onEntityDamageGeneral()  - Prevents environmental damage (except in matches)
+ onHangingBreak()         - Prevents item frame/painting damage
+ onVehicleDamage()        - Prevents vehicle damage
+ onArmorStandManipulate() - Prevents armor stand manipulation
+ onEntityInteract()       - Prevents harmful entity interactions
```

### **Macebattles.java**
```java
+ Registered BlockBreakProtectionListener
```

---

## 💡 Customization

### **Allow Block Breaking Globally:**
```java
// In onBlockBreak(), comment out the cancel:
// event.setCancelled(true);
```

### **Remove Environmental Damage Protection:**
```java
// In onEntityDamageGeneral(), comment out the cancel:
// event.setCancelled(true);
```

### **Allow All Players to Build:**
```java
// In onBlockBreak() and onBlockPlace(), comment out:
// event.setCancelled(true);
```

### **Disable Specific Protection:**
Simply comment out or remove the specific @EventHandler method.

---

## 🔍 Why This Approach?

### **Benefits:**

1. **Complete Protection** - All destructive actions blocked
2. **Match Integration** - Combat works in matches
3. **Admin Override** - Ops can still build/edit
4. **Clean Code** - Separate methods for each protection
5. **Easy Maintenance** - Simple to add/remove protections

### **Alternative Approaches:**

**WorldGuard Plugin:**
- ❌ Requires separate plugin
- ❌ Complex configuration
- ❌ May conflict with match system

**Command Blocks:**
- ❌ Limited functionality
- ❌ Not flexible
- ❌ Hard to maintain

**Our Approach:**
- ✅ Built-in to plugin
- ✅ Match-aware
- ✅ Simple and effective
- ✅ Easy to customize

---

## ✅ Summary

**Protection Enabled:**
1. ✅ Block breaking disabled
2. ✅ Block placement disabled
3. ✅ Entity damage disabled (except in matches)
4. ✅ Environmental damage disabled (except in matches)
5. ✅ Item frames protected
6. ✅ Vehicles protected
7. ✅ Armor stands protected
8. ✅ Harmful interactions blocked

**Exceptions:**
- ✅ Ops in creative mode bypass all protection
- ✅ Match participants can PvP normally
- ✅ Match participants take normal damage

**Build Status:** ✅ SUCCESSFUL

**Your server is now fully protected from all destructive actions!** 🛡️

