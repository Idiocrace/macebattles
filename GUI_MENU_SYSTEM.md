# ✅ GUI Menu System Implemented

## What Was Added

The `/duels` command now opens an **interactive GUI menu** showing your ranked rating, rank tier, and queue options!

---

## 🎮 New GUI Features

### Main Menu Display

When you type `/duels` with no arguments, you see:

```
╔═══════════════════════════════════════╗
║         DUELS MENU                    ║
╠═══════════════════════════════════════╣
║                                       ║
║   [Stone Sword]  [Diamond]  [Netherite║
║   CASUAL MATCH   RATING      RANKED   ║
║                  1000                  ║
║                  Gold Rank             ║
║                                       ║
╠═══════════════════════════════════════╣
║ [Dirt] [Stone] [Iron] [Gold] [Diamond]║
║ <1100  1100-   1250-  1400-  1550-    ║
║        1249    1399   1549   1699     ║
║                                       ║
║        [Netherite] [Nether Star]      ║
║        1700-1849   1850+              ║
╚═══════════════════════════════════════╝
```

---

## 📊 Rank Tiers Displayed

The menu shows all rank tiers:

| Icon | Rank | Rating Range | Color |
|------|------|--------------|-------|
| 🟫 Dirt | Dirt | < 1100 | §7Gray |
| 🪨 Stone | Stone | 1100 - 1249 | §7Gray |
| ⚪ Iron Ingot | Iron | 1250 - 1399 | §fWhite |
| 🟡 Gold Ingot | Gold | 1400 - 1549 | §6Gold |
| 💎 Diamond | Diamond | 1550 - 1699 | §bAqua |
| ⬛ Netherite | Netherite | 1700 - 1849 | §8Dark Gray |
| ⭐ Nether Star | Withered | 1850+ | §5Purple |

---

## 🎯 Menu Items

### Center: Your Ranked Stats (Diamond)
- **Displays:**
  - Your current rating (e.g., 1000)
  - Your rank tier (e.g., Gold)
  - Motivational text

### Left: Casual Match (Stone Sword)
- **Click to:**
  - Join casual matchmaking
  - No rating changes
  - Quick matches

### Right: Ranked Match (Netherite Sword)
- **Click to:**
  - Join ranked matchmaking
  - Rating-based matching
  - Affects your rank

### Bottom Row: Rank Tier Display
- Shows all 7 rank tiers
- Your current tier highlighted by your rating
- Visual progression guide

---

## 💻 Implementation Details

### New Files Created

1. **DuelsMenu.java**
   - Creates and manages the GUI inventory
   - Handles rank calculations
   - Manages rating display
   - Processes menu clicks

2. **MenuListener.java**
   - Listens for inventory click events
   - Routes clicks to DuelsMenu handler
   - Prevents item removal from menu

### Files Modified

1. **Macebattles.java**
   - Added `playerRatings` HashMap for caching
   - Added `DuelsMenu` instance
   - Registered `MenuListener` event handler
   - Added `getPlayerRating()` and `setPlayerRating()` methods

2. **DuelsCommand.java**
   - Changed no-args behavior to open GUI
   - `/duels` now opens menu instead of help text

3. **MatchmakingListener.java**
   - Added `lastQueuedPlayerUUID` tracking
   - Updated `handleQueued()` to cache ratings
   - Updated `handleResultProcessed()` to update ratings
   - Players notified of new rating after matches

---

## 🔄 Rating Caching System

### How It Works

1. **Player queues for ranked:**
   ```
   Player: /duels (opens menu) → clicks Netherite Sword
   ```

2. **Server responds with rating:**
   ```json
   {"type": "queued", "mode": "ranked", "rating": 1425}
   ```

3. **Plugin caches rating:**
   ```java
   plugin.setPlayerRating(playerUUID, 1425);
   ```

4. **Next time menu opens:**
   - Shows cached rating (1425)
   - Shows rank tier (Gold)

5. **After match completes:**
   - Server sends new ratings
   - Plugin updates cache
   - Players see: "Your new rating: 1450"

---

## 🎮 User Experience Flow

### Opening Menu
```
> /duels
[GUI Menu Opens]
Shows: "Rating: 1000 | Rank: Dirt"
```

### Selecting Casual
```
[Click Stone Sword]
§aYou have been added to the §eCasual §amatchmaking queue!
[Menu closes]
[Wait for match...]
```

### Selecting Ranked
```
[Click Netherite Sword]
§aYou have been added to the §6Ranked §amatchmaking queue!
[Menu closes]
[Wait for match...]
```

### After Ranked Match
```
[Match ends]
§7Your new rating: §e1025
§7You gained 25 rating!

> /duels
[GUI Menu Opens]
Shows: "Rating: 1025 | Rank: Dirt"
```

---

## 🎨 Visual Design

### Menu Layout (27 slots)

```
Slot:  0   1   2   3   4   5   6   7   8
       9  10  11  12  13  14  15  16  17
      18  19  20  21  22  23  24  25  26

Items:
      -   -   -   -   -   -   -   -   -
      - [CAS] -  [RATE] - [RNK]  -   -
    [DRT][STN][IRN][GLD][DIA][NTH][WTH] -  -
```

Legend:
- `CAS` = Casual queue (Stone Sword, slot 11)
- `RATE` = Your rating display (Diamond, slot 13)
- `RNK` = Ranked queue (Netherite Sword, slot 15)
- `DRT`-`WTH` = Rank tiers (slots 18-24)

---

## 🔧 Configuration

### Change Default Rating
Edit `DuelsMenu.java` line 116:
```java
return cachedRating != null ? cachedRating : 1000; // Change 1000
```

### Modify Rank Tiers
Edit `DuelsMenu.java` method `getRankGroup()`:
```java
if (rating < 1100) return "Dirt";    // Adjust thresholds
if (rating < 1250) return "Stone";
// etc...
```

### Change Rank Colors
Edit `DuelsMenu.java` method `getRankColor()`:
```java
if (rating < 1100) return "§7"; // Change color codes
```

---

## 📊 Rating Tracking

### When Ratings Are Cached

1. **On Queue Join (Ranked Only):**
   - Server responds with current rating
   - Plugin caches it immediately

2. **After Match Completion (Ranked Only):**
   - Server sends new ratings
   - Plugin updates cache for both players
   - Players notified in chat

3. **On Menu Open:**
   - Plugin checks cache
   - Displays cached rating
   - Default 1000 if no cache

### Rating Persistence

**Current Implementation:**
- Ratings stored in memory
- Lost on server restart
- Refreshed from server on next queue

**Future Enhancement:**
You could add:
```java
// Save to file/database
public void saveRatings() {
    // Save playerRatings map to config.yml
}
```

---

## 🧪 Testing Checklist

### Menu Display
- [ ] `/duels` opens GUI menu
- [ ] Rating displays correctly (default 1000)
- [ ] Rank tier displays correctly
- [ ] All 7 rank icons show in bottom row
- [ ] Stone Sword (casual) shows on left
- [ ] Netherite Sword (ranked) shows on right

### Menu Interaction
- [ ] Clicking casual sword queues for casual
- [ ] Clicking ranked sword queues for ranked
- [ ] Menu closes after clicking queue button
- [ ] Clicking other slots does nothing
- [ ] Can't remove items from menu

### Rating System
- [ ] Rating cached when queuing ranked
- [ ] Rating displays in menu after caching
- [ ] Rating updates after match completion
- [ ] New rating shown in chat after match
- [ ] Next menu open shows updated rating

### Rank Tiers
- [ ] Dirt (< 1100) displays correctly
- [ ] Stone (1100-1249) displays correctly
- [ ] Iron (1250-1399) displays correctly
- [ ] Gold (1400-1549) displays correctly
- [ ] Diamond (1550-1699) displays correctly
- [ ] Netherite (1700-1849) displays correctly
- [ ] Withered (1850+) displays correctly

---

## 🎯 Rank Progression Example

```
Starting Out:
Rating: 850
Rank: §7Dirt
Menu Shows: Gray "Dirt" with dirt block icon

After 10 Wins:
Rating: 1175
Rank: §7Stone
Menu Shows: Gray "Stone" with stone icon

Keep Climbing:
Rating: 1425
Rank: §6Gold
Menu Shows: Gold "Gold" with gold ingot icon

Pro Player:
Rating: 1875
Rank: §5Withered
Menu Shows: Purple "Withered" with nether star icon
```

---

## ✅ Build Status

**BUILD SUCCESSFUL** ✅

```bash
.\gradlew clean shadowJar
```

**Output:** `build/libs/MaceBattles-1.0-0.jar`

---

## 🎉 Summary

Your `/duels` command now:
- ✅ Opens interactive GUI menu
- ✅ Shows current rating and rank
- ✅ Displays all 7 rank tiers
- ✅ Provides visual queue buttons
- ✅ Caches ratings from server
- ✅ Updates ratings after matches
- ✅ Notifies players of rating changes
- ✅ Clean, intuitive interface

**The GUI menu system is complete and ready to use!** 🎮

