import java.awt.*;

class InventoryPanel extends UIPanel {
    private Player player;
    
    InventoryPanel(Player player) {
        super(1000, 700);
        this.player = player;
    }
    
    @Override
    public void handleClick(int x, int y) {
    }
    
    @Override
    public void draw(Graphics2D g) {
        GradientPaint bg = new GradientPaint(
            0, 0, new Color(20, 20, 40),
            0, 700, new Color(40, 40, 70)
        );
        g.setPaint(bg);
        g.fillRect(0, 0, width, height);
        
        g.setColor(new Color(255, 215, 0, 100));
        g.setFont(new Font("Serif", Font.BOLD, 40));
        g.drawString("⚔️ INVENTORY ⚔️", 352, 82);
        g.setColor(new Color(255, 215, 0));
        g.drawString("⚔️ INVENTORY ⚔️", 350, 80);
        
        drawPanel(g, 50, 120, 380, 500, new Color(60, 60, 90, 230));
        g.setColor(new Color(100, 200, 255));
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(50, 120, 380, 500, 25, 25);
        
        g.setColor(new Color(255, 215, 0));
        g.setFont(new Font("Serif", Font.BOLD, 24));
        g.drawString("📊 CHARACTER STATS", 100, 165);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 17));
        int y = 210;
        String[] stats = {
            "⭐ Level: " + player.level,
            "❤️ HP: " + player.hp + " / " + player.maxHp,
            "✦ MP: " + player.mp + " / " + player.maxMp,
            "⚔️ Attack: " + player.calculateAttack(),
            "🛡️ Defense: " + player.getTotalDefense(),
            "📈 EXP: " + player.exp + " / " + player.expToNextLevel,
            "💰 Gold: " + player.gold
        };
        
        for (String stat : stats) {
            g.drawString(stat, 90, y);
            y += 35;
        }
        
        y += 30;
        g.setColor(new Color(255, 215, 0));
        g.setFont(new Font("Serif", Font.BOLD, 22));
        g.drawString("⚡ EQUIPMENT", 140, y);
        
        y += 35;
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 15));
        g.drawString("Weapon: " + (player.equippedWeapon != null ? player.equippedWeapon.name : "None"), 90, y);
        y += 28;
        g.drawString("Armor: " + (player.equippedArmor != null ? player.equippedArmor.name : "None"), 90, y);
        
        drawPanel(g, 470, 120, 480, 500, new Color(60, 60, 90, 230));
        g.setColor(new Color(100, 200, 255));
        g.drawRoundRect(470, 120, 480, 500, 25, 25);
        
        g.setColor(new Color(255, 215, 0));
        g.setFont(new Font("Serif", Font.BOLD, 24));
        g.drawString("🎒 ITEMS", 650, 165);
        
        int itemY = 210;
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        
        for (Item item : player.inventory) {
            g.setColor(new Color(70, 70, 100, 180));
            g.fillRoundRect(500, itemY, 420, 50, 15, 15);
            
            g.setColor(new Color(150, 150, 200));
            g.setStroke(new BasicStroke(2));
            g.drawRoundRect(500, itemY, 420, 50, 15, 15);
            
            String icon = "";
            if (item.type == ItemType.WEAPON) icon = "⚔️";
            else if (item.type == ItemType.ARMOR) icon = "🛡️";
            else if (item.type == ItemType.CONSUMABLE) icon = "🧪";
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString(icon, 520, itemY + 32);
            
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString(item.name, 560, itemY + 25);
            
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.setColor(new Color(200, 200, 200));
            String details = "";
            if (item.attackBonus > 0) details += "ATK +" + item.attackBonus + " ";
            if (item.defenseBonus > 0) details += "DEF +" + item.defenseBonus + " ";
            if (item.hpRestore > 0) details += "HP +" + item.hpRestore + " ";
            if (item.mpRestore > 0) details += "MP +" + item.mpRestore + " ";
            g.drawString(details.trim(), 560, itemY + 40);
            
            itemY += 60;
        }
        
        if (player.inventory.isEmpty()) {
            g.setColor(new Color(150, 150, 150));
            g.setFont(new Font("Arial", Font.ITALIC, 18));
            g.drawString("No items in inventory", 610, 300);
        }
        
        g.setColor(new Color(180, 180, 200));
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("Press [I] or [ESC] to close inventory", 370, 680);
    }
}
