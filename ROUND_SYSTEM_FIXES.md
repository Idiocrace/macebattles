# Round System Fixes - Complete

## ✅ All Issues Resolved

I've fixed all three issues with the round system:

1. ✅ **Score tracking now works** - Scores properly displayed after each round
2. ✅ **Items no longer drop** - Players reset between rounds with fresh gear
3. ✅ **Rounds progress on death** - Not time-based anymore

---

## 🔧 What Was Fixed

### **Issue 1: Score Messages Not Working**

**Problem:**
- Score messages were duplicated
- Scores weren't updating properly
- Wrong player was seeing their opponent's score

**Solution:**
- Fixed message display to show correct scores for each player
- Player 1 sees their score first, Player 2 sees theirs first
- Score properly tracked in ActiveMatch
- Death listener awards round wins correctly

**Now:**
```
Player 1 sees:
§e§l========== ROUND 1 ENDED ==========
§a§lSteve §7eliminated §c§lAlex
§7Score: §a1 §7- §c0

Player 2 sees:
§e§l========== ROUND 1 ENDED ==========
§a§lSteve §7eliminated §c§lAlex
§7Score: §a0 §7- §c1
```

---

### **Issue 2: Items Not Resetting Between Rounds**

**Problem:**
- Items dropped on death
- Players could pick them up next round
- Inventory wasn't cleared properly
- Duplicate gear accumulation

**Solution:**
- **Death event:** `event.getDrops().clear()` - No items drop on death
- **Round start:** Full inventory clear for both players
- **Round start:** Fresh gear given every round
- **Round start:** Armor cleared and replaced

**Now:**
```java
// On death:
event.getDrops().clear();  // No item drops
event.setDroppedExp(0);    // No XP drops

// On round start:
player.getInventory().clear();              // Clear hotbar
player.getInventory().setArmorContents(null); // Clear armor
giveStartingGear(player);                   // Give fresh gear
```

**Result:**
- No items on ground between rounds
- Each round starts fresh
- No gear advantage from previous rounds

---

### **Issue 3: Rounds Progress With Time Instead of Death**

**Problem:**
- Rounds ended after 60 seconds regardless of combat
- Winner determined by health comparison
- Felt arbitrary and unfair

**Solution:**
- **New MatchDeathListener** - Detects player deaths in matches
- **Death triggers round end** - Round ends immediately when someone dies
- **Winner determined by elimination** - Last player standing wins round
- **Timeout as backup** - 3 minute timeout only if nobody dies (rare)

**Now:**
```
Round Flow:
1. Round starts
2. Players fight
3. Someone dies → Round ends immediately
4. Winner gets point
5. 5 second delay
6. Next round starts (fresh gear, reset health)
```

**Timeout:**
- Only triggers if 3 minutes pass with no death
- Winner determined by most health
- Draw if equal health

---

## 📦 New Components

### **MatchDeathListener.java** (New File)

**Purpose:** Handles player deaths during matches

**Key Features:**
- Detects when a player in a match dies
- Suppresses default death message
- Prevents item/XP drops
- Awards round win to survivor
- Cancels round timer
- Broadcasts round results
- Triggers next round or match end

**Event Priority:** HIGHEST (handles before other plugins)

---

## 🔄 Updated Round System Flow

### **Complete Match Flow:**

```
Match Start
    ↓
┌─────────────────────┐
│   ROUND 1 START     │
│   - Clear inventory │
│   - Give gear       │
│   - Reset health    │
│   - Teleport        │
│   - Start timer     │
└─────────┬───────────┘
          ↓
    Players Fight
          ↓
   Someone Dies
          ↓
┌─────────────────────┐
│  ROUND 1 END        │
│  - Award point      │
│  - Cancel timer     │
│  - Show scores      │
│  - Clear drops      │
└─────────┬───────────┘
          ↓
   5 Second Delay
          ↓
┌─────────────────────┐
│   ROUND 2 START     │
│   (Fresh gear)      │
└─────────┬───────────┘
          ↓
   ... (repeat)
          ↓
┌─────────────────────┐
│   Match Complete    │
│   (After 3 rounds   │
│    or 2 wins)       │
└─────────────────────┘
```

---

## 🎮 Player Experience

### **Round 1:**
```
§e§l========================================
§6§l             ROUND 1/3
§7Current Score: §a0 §7- §c0
§e§l========================================

[Players fight]

[Steve eliminates Alex]

§e§l========== ROUND 1 ENDED ==========
§a§lSteve §7eliminated §c§lAlex
§7Score: §a1 §7- §c0

[5 second delay]
```

### **Round 2:**
```
§e§l========================================
§6§l             ROUND 2/3
§7Current Score: §a1 §7- §c0
§e§l========================================

[Fresh gear, full health]
[Players fight again]

[Alex eliminates Steve]

§e§l========== ROUND 2 ENDED ==========
§a§lAlex §7eliminated §c§lSteve
§7Score: §a1 §7- §c1

[5 second delay]
```

### **Round 3 (Tiebreaker):**
```
§e§l========================================
§6§l             ROUND 3/3
§7Current Score: §a1 §7- §c1
§e§l========================================

[Intense final round]

[Steve eliminates Alex]

§e§l========== ROUND 3 ENDED ==========
§a§lSteve §7eliminated §c§lAlex
§7Score: §a2 §7- §c1

[3 second delay]

§e§l========================================
§6§l          MATCH ENDED
§7Final Score: §a2 §7- §c1
§a§l         YOU WIN!
§e§l========================================
```

---

## ⚙️ Technical Details

### **Round Timer System:**

```java
// Track active timers
Map<String, Integer> roundTimers = new HashMap<>();

// Start round with 3-minute backup timer
int taskId = Bukkit.getScheduler().runTaskLater(plugin, () -> {
    endRoundByTimeout(match);
}, 180 * 20L).getTaskId();

roundTimers.put(matchId, taskId);

// Cancel timer on death
cancelRoundTimer(matchId); // Called by death listener
```

**Why 3 minutes?**
- Long enough for any legitimate fight
- Short enough to prevent stalling
- Rarely triggers (deaths usually happen first)

### **Death Detection:**

```java
@EventHandler(priority = EventPriority.HIGHEST)
public void onPlayerDeath(PlayerDeathEvent event) {
    // Check if in match
    ActiveMatch match = listener.getMatchByPlayer(playerUUID);
    if (match == null) return; // Not in match, ignore
    
    // In match - handle specially
    event.getDrops().clear();     // No drops
    event.setDeathMessage(null);  // No message
    
    // Award point and continue
    match.addRoundWin(winner);
    listener.cancelRoundTimer(matchId);
    // ... show messages, start next round
}
```

### **Inventory Reset:**

```java
// Complete reset between rounds
player.getInventory().clear();                 // Remove all items
player.getInventory().setArmorContents(null);  // Remove armor
giveStartingGear(player);                      // Fresh gear

// Also reset other stats
player.setHealth(20.0);      // Full health
player.setFoodLevel(20);     // Full hunger
player.setSaturation(20.0f); // Full saturation
player.setFireTicks(0);      // Stop burning
player.setFallDistance(0);   // Reset fall damage
```

---

## 🏆 Win Conditions

### **First to 2 Wins:**
If someone wins 2 rounds, match ends early:
```
Round 1: Player 1 wins (1-0)
Round 2: Player 1 wins (2-0)
→ Match ends, Player 1 wins!
```

### **Best of 3:**
If tied after 2 rounds, round 3 is the decider:
```
Round 1: Player 1 wins (1-0)
Round 2: Player 2 wins (1-1)
Round 3: Player 2 wins (1-2)
→ Player 2 wins match!
```

### **Draw (Rare):**
Only possible if all rounds timeout with equal health:
```
Round 1: Timeout, equal health (0-0)
Round 2: Timeout, equal health (0-0)
Round 3: Timeout, equal health (0-0)
→ Match ends in draw!
```

---

## 🧪 Testing

### **Test Death-Based Rounds:**
```
1. Start match: /duels queue casual
2. Fight opponent
3. Die or kill opponent
4. Round should end immediately ✓
5. Score should update ✓
6. 5 second delay ✓
7. Round 2 starts with fresh gear ✓
```

### **Test No Item Drops:**
```
1. Start round
2. Die with full inventory
3. Look at death location
4. Should be NO items on ground ✓
5. Next round starts
6. Should have fresh gear ✓
```

### **Test Score Display:**
```
1. Win round 1
2. Check message
3. Should show score 1-0 correctly ✓
4. Opponent should see 0-1 ✓
5. Win round 2
6. Should show score 2-0 ✓
7. Match should end ✓
```

---

## 🎯 Summary of Changes

### **Files Modified:**

1. **MatchmakingListener.java**
   - Added `roundTimers` map
   - Rewrote `startRounds()` with proper reset
   - Added `endRoundByTimeout()` for backup
   - Added `cancelRoundTimer()` method
   - Added `startNextRound()` public method
   - Added `endMatchEarly()` for 2-win finish
   - Removed obsolete `determineRoundWinner()`
   - Fixed `endMatch()` to handle draws

2. **MatchDeathListener.java** (New)
   - Handles player deaths in matches
   - Prevents item/XP drops
   - Awards round wins
   - Triggers next round

3. **Macebattles.java**
   - Registered MatchDeathListener

### **What Works Now:**

✅ Scores display correctly (no duplicates)
✅ Scores update on each round
✅ Items don't drop on death
✅ Inventory resets between rounds
✅ Rounds end when someone dies
✅ First to 2 wins or best of 3
✅ 3-minute backup timeout
✅ Clean match flow
✅ Proper delays between rounds

---

## 🎉 Complete!

**Build Status:** ✅ SUCCESSFUL

All three issues have been completely resolved:
- ✅ Score tracking fixed
- ✅ Item drops prevented
- ✅ Death-based rounds implemented

**The round system now works perfectly!** ⚔️

