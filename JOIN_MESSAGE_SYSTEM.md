# Player Join Message System

## ✅ Feature Implemented

Players now receive a welcome message when they join the server!

---

## 📨 Join Message

When a player joins the server, they see:

```
§8§m                                                    

     §6§lWelcome to MaceBattles!

     §7Use §e/duels §7to start dueling!
     §7• §e/duels queue casual §7- Join casual matches
     §7• §e/duels queue ranked §7- Compete for rank

§8§m                                                    
```

**Visual Preview:**
```
════════════════════════════════════════════════════

     Welcome to MaceBattles!

     Use /duels to start dueling!
     • /duels queue casual - Join casual matches
     • /duels queue ranked - Compete for rank

════════════════════════════════════════════════════
```

---

## 🎨 Message Features

### **Formatting:**
- Clean borders (strikethrough lines)
- Centered title
- Clear instructions
- Color-coded text
- Professional appearance

### **Content:**
- Welcome title
- Main command hint
- Casual queue option
- Ranked queue option
- Easy to read layout

---

## 🔧 How It Works

### **Event Listener:**
```java
@EventHandler
public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    // Send welcome message
}
```

**When it triggers:**
- Every time a player joins the server
- Message sent only to the joining player (not broadcast)
- Logged in console for admins

---

## 📝 Customization

### **To Change the Message:**

Edit `PlayerJoinListener.java`:

```java
player.sendMessage("");
player.sendMessage("§8§m                                                    ");
player.sendMessage("");
player.sendMessage("     §6§lWelcome to MaceBattles!");
player.sendMessage("");
player.sendMessage("     §7Use §e/duels §7to start dueling!");
player.sendMessage("     §7• §e/duels queue casual §7- Join casual matches");
player.sendMessage("     §7• §e/duels queue ranked §7- Compete for rank");
player.sendMessage("");
player.sendMessage("§8§m                                                    ");
player.sendMessage("");
```

### **Color Codes:**

| Code | Color | Usage |
|------|-------|-------|
| `§6` | Gold | Title |
| `§7` | Gray | Text |
| `§e` | Yellow | Commands/highlights |
| `§8` | Dark Gray | Borders |
| `§l` | Bold | Title emphasis |
| `§m` | Strikethrough | Border lines |

### **Add More Lines:**

```java
player.sendMessage("     §7Visit our website: §ewww.example.com");
player.sendMessage("     §7Discord: §e/discord");
player.sendMessage("     §7Need help? §e/help");
```

### **Change Title:**

```java
player.sendMessage("     §6§lYour Server Name Here!");
```

### **Add Player Name:**

```java
player.sendMessage("     §6§lWelcome, " + player.getName() + "!");
```

### **First Time vs Returning:**

```java
if (!player.hasPlayedBefore()) {
    // First time message
    player.sendMessage("     §a§lWelcome for the first time!");
} else {
    // Returning player message
    player.sendMessage("     §e§lWelcome back!");
}
```

---

## 🎯 Example Variations

### **Minimal:**
```java
player.sendMessage("§6Welcome to MaceBattles! §7Use §e/duels §7to play.");
```

### **Detailed:**
```java
player.sendMessage("");
player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
player.sendMessage("");
player.sendMessage("     §6§l⚔ MaceBattles Server ⚔");
player.sendMessage("");
player.sendMessage("     §7Welcome, §e" + player.getName() + "§7!");
player.sendMessage("");
player.sendMessage("     §7Commands:");
player.sendMessage("     §8• §e/duels queue casual §7- Casual matches");
player.sendMessage("     §8• §e/duels queue ranked §7- Ranked matches");
player.sendMessage("     §8• §e/duels invite <player> §7- Challenge someone");
player.sendMessage("");
player.sendMessage("     §7Need help? Type §e/help");
player.sendMessage("");
player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
player.sendMessage("");
```

### **With Stats:**
```java
Integer rating = plugin.getPlayerRating(player.getUniqueId());
if (rating != null) {
    player.sendMessage("     §7Your Rating: §e" + rating);
}
```

---

## 🎮 Player Experience

### **What Players See:**

**On join:**
1. Connection successful
2. Welcome message displays
3. Clear instructions shown
4. Ready to start playing

**Console view (admins):**
```
[INFO] Steve joined the server
```

---

## 📊 Technical Details

### **File Created:**
- `PlayerJoinListener.java` - Handles join events and sends messages

### **File Modified:**
- `Macebattles.java` - Registered the join listener

### **Event Type:**
- `PlayerJoinEvent` - Fires when player connects

### **Message Type:**
- `player.sendMessage()` - Sent only to joining player
- Not broadcast to all players
- Not shown in server console (only admin log)

### **Performance:**
- Negligible impact
- Runs once per player join
- No database queries
- Instant message delivery

---

## 🔍 Testing

### **Test Join Message:**
```
1. Join the server
2. Should see welcome message immediately ✓
3. Message should be centered and formatted ✓
4. Commands should be highlighted ✓
```

### **Test Logging:**
```
1. Join the server
2. Check console
3. Should see: "[INFO] PlayerName joined the server" ✓
```

---

## 💡 Advanced Features You Can Add

### **Title Screen:**
```java
player.sendTitle("§6§lWelcome!", "§7Use /duels to start", 10, 70, 20);
```

### **Sound Effect:**
```java
player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
```

### **Action Bar:**
```java
player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
    new TextComponent("§eWelcome to MaceBattles!"));
```

### **JSON Message (Clickable):**
```java
TextComponent message = new TextComponent("§7Click here to queue! ");
message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duels queue casual"));
message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
    new ComponentBuilder("§aJoin casual matchmaking").create()));
player.spigot().sendMessage(message);
```

---

## ✅ Summary

**Features:**
- ✅ Welcome message on join
- ✅ Clear instructions
- ✅ Professional formatting
- ✅ Color-coded text
- ✅ Console logging
- ✅ Easy to customize

**What Players See:**
```
Welcome message with:
- Server name
- How to start playing
- Queue commands
- Clean formatting
```

**Build Status:** ✅ SUCCESSFUL

**Players now receive a nice welcome message when they join the server!** 📨

