# Queue Error Handling - Complete Documentation

## ✅ Comprehensive Error Handling Implemented

Your MaceBattles plugin now has robust error handling for all queue-related failures!

---

## 🛡️ Error Scenarios Covered

### 1. **WebSocket Connection Failures**
**Scenario:** Matchmaking server is offline or unreachable

**Handling:**
```java
// When player tries to queue:
if (!isConnected()) {
    player.sendMessage("§c§lQueue Failed!");
    player.sendMessage("§cMatchmaking server is not connected.");
    player.sendMessage("§7Please wait a moment and try again.");
    // Prevents adding to queue
    return;
}
```

**Player sees:**
```
§c§lQueue Failed!
§cMatchmaking server is not connected.
§7Please wait a moment and try again.
```

---

### 2. **Queue Timeout (5 minutes)**
**Scenario:** Player stuck in queue with no response from server

**Handling:**
```java
// Automatic timeout after 5 minutes
scheduleQueueTimeout(playerUUID);

// After 5 minutes if still queued:
player.sendMessage("§c§lQueue Timeout");
player.sendMessage("§cYou have been removed from the queue after 5 minutes.");
player.sendMessage("§7The matchmaking server may be experiencing issues.");
```

**Player sees:**
```
§c§lQueue Timeout
§cYou have been removed from the queue after 5 minutes.
§7The matchmaking server may be experiencing issues.
§7Please try again later or contact an administrator.
```

---

### 3. **Server Error Messages**
**Scenario:** Matchmaking server returns an error

**Handling:**
```java
// Server sends error message
handleError(data);

// Player notified and removed from queue:
player.sendMessage("§c§lQueue Failed!");
player.sendMessage("§cServer error: " + errorMessage);
player.sendMessage("§7Please try again or contact an administrator...");
```

**Player sees:**
```
§c§lQueue Failed!
§cServer error: Player already in queue
§7Please try again or contact an administrator if the issue persists.
```

---

### 4. **Player Disconnects While Queued**
**Scenario:** Player logs out while in matchmaking queue

**Handling:**
```java
// PlayerDisconnectListener automatically triggers
onPlayerQuit(event) {
    if (listener.isPlayerQueued(player.getUniqueId())) {
        listener.removePlayerFromQueue(player.getUniqueId());
        // Notifies server of cancellation
    }
}
```

**Console logs:**
```
[INFO] Removed PlayerName from matchmaking queue due to disconnect
```

---

### 5. **Match Found But Player Offline**
**Scenario:** Match found but one or both players are no longer online

**Handling:**
```java
if (player1 == null || player2 == null) {
    // Notify online player(s)
    if (player1 != null) {
        player1.sendMessage("§c§lMatch Failed!");
        player1.sendMessage("§cYour opponent is no longer online.");
        player1.sendMessage("§7You have been removed from the queue.");
    }
    // Same for player2
    return;
}
```

**Player sees:**
```
§c§lMatch Failed!
§cYour opponent is no longer online.
§7You have been removed from the queue.
```

---

### 6. **Send Message Failure**
**Scenario:** Failed to send queue/cancel message to server

**Handling:**
```java
try {
    sendJson(message);
} catch (Exception e) {
    player.sendMessage("§c§lQueue Failed!");
    player.sendMessage("§cAn error occurred while joining the queue.");
    player.sendMessage("§7Error: " + e.getMessage());
    // Removes from local queue state
    queuedPlayers.remove(player.getUniqueId());
}
```

**Player sees:**
```
§c§lQueue Failed!
§cAn error occurred while joining the queue.
§7Error: Connection refused
```

---

### 7. **Server Disconnection During Queue**
**Scenario:** WebSocket connection drops while players are queued

**Handling:**
```java
onClose(code, reason, remote) {
    // Clears all queues
    clearAllQueues();
    
    // Notifies all online players
    for (Player player : Bukkit.getOnlinePlayers()) {
        player.sendMessage("§c§lMatchmaking Server Disconnected");
        player.sendMessage("§cYou have been removed from any active queues.");
        player.sendMessage("§7Please wait for reconnection or try again later.");
    }
}
```

**All players see:**
```
§c§lMatchmaking Server Disconnected
§cYou have been removed from any active queues.
§7Please wait for reconnection or try again later.
```

---

### 8. **Plugin Shutdown/Reload**
**Scenario:** Server is reloading or shutting down

**Handling:**
```java
onDisable() {
    // Clear all queues
    matchmakingListener.clearAllQueues();
    // Disconnect gracefully
    matchmakingListener.disconnect();
}
```

**Console logs:**
```
[INFO] Cleared 5 player(s) from queue
[INFO] Disconnected from matchmaking server
[INFO] Macebattles plugin disabled!
```

---

## 🔍 Error Detection Methods

### New Methods Added

#### `isConnected()`
```java
// Check if WebSocket is active
if (!listener.isConnected()) {
    // Cannot queue
}
```

#### `removePlayerFromQueue(UUID)`
```java
// Manually remove player from queue
listener.removePlayerFromQueue(playerUUID);
// Cleans up timestamps and notifies server
```

#### `clearAllQueues()`
```java
// Emergency clear all queues
listener.clearAllQueues();
// Used on shutdown/disconnect
```

#### `scheduleQueueTimeout(UUID)`
```java
// Automatic 5-minute timeout
// Runs in background
// Auto-removes on timeout
```

---

## 📊 Queue State Tracking

### New Data Structures

```java
// Track who is queued
Set<UUID> queuedPlayers = new HashSet<>();

// Track when they queued
Map<UUID, Long> queueTimestamps = new HashMap<>();

// Track last player for error association
UUID lastQueuedPlayerUUID;

// Timeout constant
static final long QUEUE_TIMEOUT = 300000; // 5 minutes
```

---

## 🚨 Error Logging

### Console Output Examples

#### Connection Success
```
[INFO] Connecting to matchmaking server at ws://localhost:8000/ws...
[INFO] ✓ Connected to matchmaking server at ws://localhost:8000/ws
```

#### Connection Failure
```
[SEVERE] ✗ Failed to initialize WebSocket client: Connection refused
[SEVERE]   Matchmaking will not be available!
```

#### Queue Success
```
[INFO] Queued player: Steve (uuid-here) for RANKED
[INFO] Queue confirmation: ranked (rating: 1450)
```

#### Queue Error
```
[WARNING] Matchmaking server error: Player already in queue
[WARNING] Cannot queue player Steve - WebSocket not connected
```

#### Timeout
```
[WARNING] Player uuid-here timed out in queue after 300000ms
```

#### Disconnect
```
[WARNING] ✗ Disconnected from matchmaking server (server): Connection lost (code: 1006)
[INFO] Cleared 3 player(s) from queue
```

---

## 🎮 Player Experience

### Successful Queue
```
> /duels queue ranked

§aYou have been added to the §6Ranked §amatchmaking queue!
§7Queue confirmed! Searching for opponents...
§7Your rating: §e1450

[Wait for match...]
§6Ranked §amatch found! Arena: cool_arena
```

### Failed Queue (Server Down)
```
> /duels queue ranked

§c§lQueue Failed!
§cMatchmaking server is not connected.
§7Please wait a moment and try again.
```

### Timeout
```
> /duels queue ranked

§aYou have been added to the §6Ranked §amatchmaking queue!
§7Queue confirmed! Searching for opponents...

[5 minutes pass...]

§c§lQueue Timeout
§cYou have been removed from the queue after 5 minutes.
§7The matchmaking server may be experiencing issues.
§7Please try again later or contact an administrator.
```

### Server Error
```
> /duels queue ranked

§aYou have been added to the §6Ranked §amatchmaking queue!

§c§lQueue Failed!
§cServer error: Invalid player data
§7Please try again or contact an administrator if the issue persists.
```

---

## 🛠️ Configuration

### Adjust Timeout Duration

**In MatchmakingListener.java:**
```java
// Current: 5 minutes
private static final long QUEUE_TIMEOUT = 300000;

// Options:
300000  // 5 minutes (default)
180000  // 3 minutes
600000  // 10 minutes
60000   // 1 minute (testing)
```

### Disable Timeout
```java
// Set to very large value
private static final long QUEUE_TIMEOUT = Long.MAX_VALUE;
// Effectively disables timeout
```

---

## 🧪 Testing Error Scenarios

### Test Connection Failure
```bash
1. Stop matchmaking server
2. Try to queue: /duels queue ranked
3. Should see: "Matchmaking server is not connected"
4. Console: "Cannot queue player - WebSocket not connected"
```

### Test Timeout
```bash
1. Start plugin with server offline
2. Modify QUEUE_TIMEOUT to 60000 (1 minute)
3. Try to queue (will fail but test manually adds to set)
4. Wait 1 minute
5. Should see timeout message
```

### Test Disconnect
```bash
1. Queue for match
2. Stop matchmaking server mid-queue
3. Should see: "Matchmaking Server Disconnected"
4. Console: "Disconnected from matchmaking server"
```

### Test Player Disconnect
```bash
1. Queue for match
2. Disconnect from server
3. Console should log: "Removed PlayerName from matchmaking queue"
```

---

## 📋 Error Recovery

### Automatic Recovery

**Queue Cleared On:**
- WebSocket disconnect
- Plugin reload/shutdown
- Individual player disconnect
- Timeout expiry

**State Cleanup On:**
- Match found (both players)
- Cancel queue (manual)
- Any error during queue

### Manual Recovery

**Admin Commands (Future Enhancement):**
```
/duels admin clearqueues   - Clear all queues
/duels admin reconnect     - Reconnect to server
/duels admin status        - Show queue status
```

---

## 🔧 Files Modified

### MatchmakingListener.java
**Added:**
- `queueTimestamps` map
- `QUEUE_TIMEOUT` constant
- `scheduleQueueTimeout()` method
- `removePlayerFromQueue()` method
- `clearAllQueues()` method
- `getQueuedPlayerCount()` method
- Enhanced error handling in all methods
- Better WebSocket event handlers
- Try-catch blocks for all network operations

### PlayerDisconnectListener.java
**New file:**
- Handles player quit events
- Auto-removes from queue
- Logs disconnect events

### Macebattles.java
**Updated:**
- Registers PlayerDisconnectListener
- Calls clearAllQueues() on disable

---

## ✅ Summary

### Error Handling Features

✅ **Connection validation** before queuing
✅ **5-minute timeout** for stuck queues
✅ **Server error** handling and display
✅ **Player disconnect** cleanup
✅ **Match failure** notifications
✅ **WebSocket disconnect** handling
✅ **Send failure** recovery
✅ **Graceful shutdown** cleanup

### Player Notifications

✅ Clear error messages
✅ Helpful suggestions
✅ Status updates
✅ Timeout warnings
✅ Connection status

### Console Logging

✅ Success indicators (✓)
✅ Error indicators (✗)
✅ Detailed error messages
✅ Stack traces for debugging
✅ State change logging

---

## 🎯 Benefits

### For Players
- Clear feedback on what went wrong
- No getting stuck in broken queues
- Automatic cleanup and recovery
- Helpful error messages

### For Admins
- Detailed console logging
- Easy troubleshooting
- Automatic state cleanup
- No manual intervention needed

### For Developers
- Comprehensive error handling
- Easy to add new error types
- Well-documented code
- Testable error scenarios

---

## 🎉 Complete!

**Your queue system now handles:**
- ✅ Connection failures
- ✅ Timeouts
- ✅ Server errors
- ✅ Player disconnects
- ✅ Match failures
- ✅ Send failures
- ✅ Graceful shutdowns

**Build Status:** ✅ SUCCESSFUL

**Error handling is production-ready!** 🛡️

