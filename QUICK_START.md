# MaceBattles - Quick Start Guide

## 🚀 Getting Started

### 1. Start the Matchmaking Server
```bash
python matchmaking.py
```
Server runs on: `ws://localhost:8000/ws`

### 2. Start Your Minecraft Server
The plugin will automatically connect to the matchmaking server.

### 3. Place Structure Files
Put your `.nbt` arena files in:
```
plugins/macebattles/structures/
  ├── map1.nbt
  ├── map2.nbt
  └── map3.nbt
```

---

## 🎮 Player Commands

### Queue for Matches
```
/duels queue casual    - Join casual matchmaking (no rating)
/duels queue ranked    - Join ranked matchmaking (affects rating)
/duels cancel          - Leave queue
```

### Direct Duels
```
/duels invite <player> - Challenge a specific player
/duels accept          - Accept a duel challenge
/duels deny            - Decline a duel challenge
```

### Shortcuts
```
/duel ...  or  /d ...  - Use as aliases
```

---

## ⚙️ Configuration

### Change Server Address
Edit `Macebattles.java` line 17:
```java
String websocketUri = "ws://YOUR-SERVER-IP:8000/ws";
```

### Change Available Maps
Edit `MatchmakingListener.java` line 241:
```java
String[] availableMaps = {"map1", "map2", "map3"};
```

### Change Round Duration
Edit `MatchmakingListener.java` line 271:
```java
60 * 20L  // 60 seconds per round
```

---

## 🔧 Troubleshooting

### Plugin Won't Load
- Check `java.lang.NoClassDefFoundError` → Use the shadowJar build
- Rebuild: `.\gradlew clean shadowJar`

### Can't Connect to Matchmaking Server
- Check server is running: `python matchmaking.py`
- Check firewall allows port 8000
- Verify URI: `ws://localhost:8000/ws`

### Match Not Starting
- Check both players are online
- Check console for error messages
- Verify structure files exist

### Ratings Not Updating
- Only ranked matches update ratings
- Check console for "result_processed" message
- Verify server received match results

---

## 📊 How Ratings Work

### Initial Stats
- Start: 1000 rating
- Range: 0 to unlimited
- Rating Deviation: 350 → 65 (decreases over time)

### Match Results
```
3-0 → Full win/loss    (~25-50 rating change)
2-1 → Close win/loss   (~15-30 rating change)
```

### Matchmaking
- Server pairs players with similar ratings
- Searches every 500ms
- Closest rating match wins

---

## 📝 Message Protocol

### Your Plugin Sends
```json
{"type": "queue", "player_uuid": "...", "mode": "ranked"}
{"type": "cancel_queue"}
{"type": "match_result", "match_uuid": "...", "player1_uuid": "...", "player2_uuid": "...", "player1_rounds": 2, "player2_rounds": 1}
```

### Server Sends
```json
{"type": "queued", "mode": "ranked", "rating": 1000, "message": "..."}
{"type": "match_found", "match_uuid": "...", "player1_uuid": "...", "player2_uuid": "..."}
{"type": "result_processed", "match_uuid": "...", "player1_new_rating": 1025, "player2_new_rating": 975}
{"type": "queue_cancelled", "message": "..."}
{"type": "error", "message": "..."}
```

---

## ✅ Quick Checks

### Is Everything Working?

**1. Server Console:**
```
[INFO] [macebattles] Connected to matchmaking server
[INFO] [macebattles] Macebattles plugin enabled!
```

**2. Queue Test:**
```
> /duels queue ranked
§aYou have been added to the §6Ranked §amatchmaking queue!
```

**3. Match Test:**
```
[Two players queue]
§6Ranked §amatch found! Arena: map1
[Match starts]
```

**4. Results Test:**
```
[Match ends]
[INFO] Ranked match ... completed. Winner: ... - Results sent to server
[INFO] Ranked match ... results processed. New ratings: 1025 / 975
```

---

## 🎯 Production Checklist

Before going live:

- [ ] Matchmaking server running
- [ ] Plugin connects successfully
- [ ] Structure files in place (map1.nbt, map2.nbt, map3.nbt)
- [ ] Spawn points configured correctly
- [ ] Round duration set appropriately
- [ ] Gear loadout tested
- [ ] Rating system verified
- [ ] Both casual and ranked queues tested
- [ ] Direct duels tested
- [ ] Player teleportation working
- [ ] Results being sent to server

---

## 📞 Support

### Check Logs
```
server/logs/latest.log
```

### Common Log Messages
```
"Connected to matchmaking server" → ✅ Good
"WebSocket error" → ❌ Check server
"Match found: match-..." → ✅ Match starting
"Results sent to server" → ✅ Ranked working
"No results sent" → ✅ Casual working
```

### Debug Mode
Add to console commands:
```java
plugin.getLogger().info("Debug: " + message);
```

---

## 🎉 You're Ready!

Your MaceBattles server is configured and ready for players!

**Key Points:**
- Casual = Fun matches, no ratings
- Ranked = Competitive, affects ratings
- Direct duels = Challenge friends anytime
- All matches use same gear and arenas

**Enjoy your mace battles!** ⚔️

