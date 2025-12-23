# Automatic Reconnection & Connection Status

## ✅ Features Implemented

The plugin now handles matchmaking server disconnections gracefully with automatic reconnection and clear UI feedback.

---

## 🔄 Automatic Reconnection

### **How It Works:**

The plugin automatically attempts to reconnect to the matchmaking server every 60 seconds if disconnected.

```
Plugin starts
    ↓
Attempts connection to matchmaking server
    ↓
Connected? 
    ├─ YES → Normal operation
    └─ NO  → Wait 60 seconds
         ↓
    Retry connection
         ↓
    (Repeats every 60 seconds until connected)
```

### **Console Logs:**

**On disconnect:**
```
[WARN] ✗ Disconnected from matchmaking server (server): Connection closed (code: 1000)
[INFO] Matchmaking server not connected. Attempting reconnection...
```

**Retry attempts:**
```
[INFO] Matchmaking server not connected. Attempting reconnection...
[INFO] Connecting to matchmaking server at ws://localhost:8000/ws...
```

**On successful reconnection:**
```
[INFO] ✓ Connected to matchmaking server at ws://localhost:8000/ws
```

### **Task Management:**

**Started automatically:**
- Reconnection task starts when plugin loads
- Checks connection every 60 seconds (1200 ticks)
- Runs until plugin is disabled

**Stopped automatically:**
- Task stops when plugin is disabled
- Prevents resource leaks
- Clean shutdown

---

## 🚫 Connection Status UI

### **Duels Menu When Disconnected:**

When the matchmaking server is not connected, the `/duels` menu shows:

**Center (Barrier Block):**
```
§c§lMatchmaking Not Connected
§7The matchmaking server is
§7currently unavailable.

§7Duels are not available
§7at this time.

§cRetrying connection...
```

**Casual Match (Gray Dye):**
```
§c§lCasual Match
§7Play for fun!

§c§lUnavailable
§7Matchmaking server not connected

§7Please wait for reconnection...
```

**Ranked Match (Gray Dye):**
```
§c§lRanked Match
§7Competitive play!

§c§lUnavailable
§7Matchmaking server not connected

§7Please wait for reconnection...
```

### **When Connected:**

Menu returns to normal with:
- Diamond in center (rating display)
- Stone Sword (casual queue)
- Netherite Sword (ranked queue)

---

## 🎮 Player Experience

### **When Server is Down:**

**Player types:** `/duels`

**Menu shows:**
```
[Center: Barrier Block]
"Matchmaking Not Connected"
"Duels are not available at this time"
"Retrying connection..."

[Left: Gray Dye]
"Casual Match - Unavailable"

[Right: Gray Dye]  
"Ranked Match - Unavailable"
```

**If player clicks:**
```
§c§lMatchmaking Not Available
§cThe matchmaking server is not connected.
§7Please wait a moment and try again.
```

### **When Server Comes Back:**

**Player types:** `/duels` again

**Menu shows:**
```
[Center: Diamond]
"Your Ranked Stats"
"Rating: 1450"
"Rank: Gold"

[Left: Stone Sword]
"Casual Match"
"Click to queue!"

[Right: Netherite Sword]
"Ranked Match"
"Click to queue!"
```

**Queue works normally!**

---

## 🔧 Technical Details

### **Reconnection System:**

**MatchmakingListener.java:**

```java
// Store URI for reconnection
private final String serverUri;
private int reconnectTaskId = -1;

// Start reconnection task on plugin enable
private void startReconnectionTask() {
    reconnectTaskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
        if (!isConnected()) {
            plugin.getLogger().info("Attempting reconnection...");
            connectToServer();
        }
    }, 1200L, 1200L).getTaskId(); // Every 60 seconds
}

// Stop on plugin disable
private void stopReconnectionTask() {
    if (reconnectTaskId != -1) {
        Bukkit.getScheduler().cancelTask(reconnectTaskId);
        reconnectTaskId = -1;
    }
}
```

### **Connection Status Check:**

**DuelsMenu.java:**

```java
public void openMainMenu(Player player) {
    MatchmakingListener listener = plugin.getMatchmakingListener();
    boolean isConnected = listener != null && listener.isConnected();
    
    if (isConnected) {
        // Show normal menu with queue buttons
    } else {
        // Show disconnected menu with barriers
    }
}
```

### **Click Handler:**

```java
public void handleMenuClick(Player player, ItemStack clickedItem, int slot) {
    boolean isConnected = listener != null && listener.isConnected();
    
    if (!isConnected) {
        player.sendMessage("§c§lMatchmaking Not Available");
        return;
    }
    
    // Normal queue logic
}
```

---

## 📊 Connection States

### **State 1: Normal Operation**

```
Status: Connected ✅
Menu: Diamond, Stone Sword, Netherite Sword
Actions: Queue works normally
Logs: Normal matchmaking messages
```

### **State 2: Server Down**

```
Status: Disconnected ❌
Menu: Barrier, Gray Dye, Gray Dye
Actions: Queue blocked with message
Logs: "Attempting reconnection..." every 60s
```

### **State 3: Reconnecting**

```
Status: Attempting connection...
Menu: Barrier, Gray Dye, Gray Dye
Actions: Queue blocked with message
Logs: "Connecting to matchmaking server..."
```

### **State 4: Reconnected**

```
Status: Connected ✅
Menu: Returns to normal
Actions: Queue works again
Logs: "✓ Connected to matchmaking server"
```

---

## 🎯 Benefits

### **For Players:**

✅ **Clear feedback** - Always know if matchmaking is available
✅ **No confusion** - Obvious when server is down
✅ **Automatic recovery** - No manual action needed
✅ **Professional UI** - Clean disconnected state

### **For Admins:**

✅ **Automatic reconnection** - No manual restart needed
✅ **Clear logs** - See connection attempts in console
✅ **Graceful degradation** - Plugin continues working
✅ **Easy monitoring** - Connection status visible

---

## 🧪 Testing

### **Test Disconnection:**

```
1. Start server with matchmaking server running
2. Stop matchmaking server
3. Wait a moment
4. Open /duels menu
5. Should show "Matchmaking Not Connected" ✓
6. Console should log reconnection attempts ✓
```

### **Test Reconnection:**

```
1. Start with matchmaking server stopped
2. Open /duels - should show disconnected ✓
3. Start matchmaking server
4. Wait up to 60 seconds
5. Console shows "✓ Connected" ✓
6. Open /duels - should show normal menu ✓
7. Queue should work ✓
```

### **Test Click When Disconnected:**

```
1. Stop matchmaking server
2. Open /duels menu
3. Click casual or ranked button
4. Should see "Matchmaking Not Available" message ✓
5. Should not queue ✓
```

---

## 📝 Configuration

### **Reconnection Interval:**

Default: **60 seconds** (1200 ticks)

To change, edit this line in `MatchmakingListener.java`:
```java
}, 1200L, 1200L).getTaskId(); // 1200 ticks = 60 seconds
```

Examples:
- 30 seconds: `600L, 600L`
- 2 minutes: `2400L, 2400L`
- 5 minutes: `6000L, 6000L`

### **Server URI:**

Set in `Macebattles.java`:
```java
String websocketUri = "ws://localhost:8000/ws";
```

Change to your matchmaking server address.

---

## 🔍 Troubleshooting

### **"Attempting reconnection..." spam in console**

**Normal behavior when server is down**
- Logs every 60 seconds
- Stops when connection succeeds
- No action needed

### **Menu still shows connected when server is down**

**Wait a moment**
- WebSocket disconnect takes a few seconds
- Reopen menu to see updated state
- Connection status updates automatically

### **Reconnection not working**

**Check these:**
1. Is matchmaking server actually running?
2. Is the URI correct? (ws://localhost:8000/ws)
3. Check firewall/network settings
4. Look for error messages in console

---

## ✅ Summary

**Features:**
- ✅ Automatic reconnection every 60 seconds
- ✅ Clear UI when disconnected (barrier + gray dye)
- ✅ Helpful error messages
- ✅ Graceful degradation
- ✅ Console logging for monitoring
- ✅ Automatic recovery

**Player Experience:**
- ✅ Always know if matchmaking is available
- ✅ Clear "Not Connected" message
- ✅ No confusion or broken queues
- ✅ Automatic return to normal when server is back

**Admin Experience:**
- ✅ Automatic reconnection (no manual intervention)
- ✅ Clear console logs
- ✅ Easy to monitor connection status
- ✅ Professional error handling

**Build Status:** ✅ SUCCESSFUL

**Your plugin now handles matchmaking server disconnections gracefully!** 🔄

