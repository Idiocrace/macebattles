# Macebattles Plugin

A Minecraft plugin for competitive mace battles with matchmaking support.

## Features

- **WebSocket Matchmaking**: Connects to an external matchmaking server
- **Structure-based Arenas**: Uses NBT structure files for instant arena creation
- **3-Round Matches**: Best of 3 rounds competitive gameplay
- **Player Teleportation**: Automatically teleports players to arenas and back to spawn

## Setup

### 1. Configure WebSocket Server

Update the WebSocket URI in `Macebattles.java`:
```java
String websocketUri = "ws://your-server:8080"; // Change this to your matchmaking server
```

### 2. Add Map Structures

Place your `.nbt` structure files in:
```
plugins/macebattles/structures/
```

Example structure files:
- `map1.nbt`
- `map2.nbt`
- `map3.nbt`

Create structures using Minecraft structure blocks and export them as `.nbt` files.

### 3. Configure Available Maps

In `MatchmakingListener.java`, update the available maps:
```java
String[] availableMaps = {"map1", "map2", "map3"}; // Your structure file names
```

### 4. Adjust Spawn Points

In `MapManager.java`, configure relative spawn points:
```java
spawnPoints.put(0, new BlockVector(5, 1, 5));   // Player 1 spawn (relative to structure origin)
spawnPoints.put(1, new BlockVector(-5, 1, -5)); // Player 2 spawn
```

## Usage

### Player Commands

- `/queue` - Join the matchmaking queue

### Matchmaking Flow

1. **Queue**: Players use `/queue` to enter matchmaking
2. **Match Found**: WebSocket server sends match data with player UUIDs
3. **Arena Creation**: Plugin creates arena from random map structure
4. **Teleportation**: Both players teleported to their spawn points
5. **3 Rounds**: Match consists of 3 rounds (60 seconds each by default)
6. **Results**: Scores sent to matchmaking server after match ends
7. **Return**: Players teleported back to their original locations

### WebSocket Protocol

#### Outgoing (Plugin → Server)

**Queue Player:**
```json
{
  "type": "queue",
  "uuid": "player-uuid",
  "username": "playername"
}
```

**Dequeue Player:**
```json
{
  "type": "dequeue",
  "uuid": "player-uuid"
}
```

**Match Complete:**
```json
{
  "type": "match_complete",
  "matchId": "match-uuid",
  "scores": {
    "player1-uuid": 2,
    "player2-uuid": 1
  },
  "winner": "player1-uuid"
}
```

#### Incoming (Server → Plugin)

**Match Found:**
```json
{
  "type": "match_found",
  "matchId": "match-uuid",
  "players": ["player1-uuid", "player2-uuid"]
}
```

## Configuration

### Round Duration

Adjust round length in `MatchmakingListener.startRounds()`:
```java
60 * 20L // 60 seconds (20 ticks = 1 second)
```

### Arena Location

Configure arena spawn area in `MapManager.createRandomArena()`:
```java
int x = random.nextInt(10000) + 10000; // Between 10000 and 20000
int z = random.nextInt(10000) + 10000;
```

## Development

### Building

```bash
./gradlew build
```

### Dependencies

- Spigot API 1.21.10
- Java-WebSocket 1.5.3
- Gson 2.10.1

## Game Logic

The plugin includes a placeholder for round winner determination in `MatchmakingListener.determineRoundWinner()`. Implement your own game logic:

```java
private UUID determineRoundWinner(Player player1, Player player2) {
    // Example: Check health, kills, custom stats, etc.
    // Return UUID of winner or null for draw
}
```

You can also manually award round wins:
```java
matchmakingListener.awardRoundWin(matchId, playerUUID);
```

## Classes

- **Macebattles**: Main plugin class
- **MatchmakingListener**: WebSocket client and match management
- **MapManager**: Structure loading and arena creation
- **ActiveMatch**: Match state tracking (rounds, scores, locations)
- **ArenaInstance**: Arena data (location, spawn points)
- **QueueCommand**: `/queue` command handler

## Notes

- Players are automatically returned to their original location after match ends
- Arena instances are cleaned up after match completion
- The plugin requires an external WebSocket matchmaking server to function

