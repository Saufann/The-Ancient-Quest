import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class MainMenu extends UIPanel {
    private GamePanel gamePanel;
    private DatabaseManager dbManager;
    private List<Rectangle> menuButtons = new ArrayList<>();
    private int hoveredButton = -1;
    private boolean showLoadMenu = false;
    private boolean showStatsMenu = false;
    private List<SaveData> saves = new ArrayList<>();
    
    MainMenu(GamePanel gamePanel, DatabaseManager dbManager) {
        super(1000, 700);
        this.gamePanel = gamePanel;
        this.dbManager = dbManager;
    }
    
    @Override
    public void handleClick(int x, int y) {
        if (showLoadMenu) {
            handleLoadMenuClick(x, y);
            return;
        }
        
        if (showStatsMenu) {
            handleStatsMenuClick(x, y);
            return;
        }
        
        for (int i = 0; i < menuButtons.size(); i++) {
            if (menuButtons.get(i).contains(x, y)) {
                switch (i) {
                    case 0: 
                        gamePanel.startNewGame();
                        break;
                    case 1: 
                        showLoadMenu = true;
                        saves = dbManager.getAllSaves();
                        break;
                    case 2: 
                        showStatsMenu = true;
                        break;
                    case 3: 
                        System.exit(0);
                        break;
                }
                return;
            }
        }
    }
    
    private void handleLoadMenuClick(int x, int y) {
        Rectangle backBtn = new Rectangle(50, 620, 200, 50);
        if (backBtn.contains(x, y)) {
            showLoadMenu = false;
            return;
        }

        for (int i = 0; i < saves.size() && i < 5; i++) {
            Rectangle saveSlot = new Rectangle(200, 150 + (i * 100), 600, 80);
            if (saveSlot.contains(x, y)) {
                gamePanel.loadGame(saves.get(i).saveId);
                showLoadMenu = false;
                return;
            }
        }
    }
    
    private void handleStatsMenuClick(int x, int y) {
        Rectangle backBtn = new Rectangle(50, 620, 200, 50);
        if (backBtn.contains(x, y)) {
            showStatsMenu = false;
        }
    }
    
    void handleHover(int x, int y) {
        hoveredButton = -1;
        for (int i = 0; i < menuButtons.size(); i++) {
            if (menuButtons.get(i).contains(x, y)) {
                hoveredButton = i;
                break;
            }
        }
    }
    
    @Override
    public void draw(Graphics2D g) {
        GradientPaint bg = new GradientPaint(
            0, 0, new Color(15, 15, 35),
            0, 700, new Color(35, 15, 45)
        );
        g.setPaint(bg);
        g.fillRect(0, 0, width, height);
        
        if (showLoadMenu) {
            drawLoadMenu(g);
        } else if (showStatsMenu) {
            drawStatsMenu(g);
        } else {
            drawMainMenu(g);
        }
    }
    
    private void drawMainMenu(Graphics2D g) {
        g.setColor(new Color(255, 215, 0, 100));
        g.setFont(new Font("Serif", Font.BOLD, 60));
        g.drawString("⚔️ THE ANCIENT QUEST ⚔️", 152, 152);
        g.setColor(new Color(255, 215, 0));
        g.drawString("⚔️ THE ANCIENT QUEST ⚔️", 150, 150);
        
        g.setColor(new Color(200, 200, 220));
        g.setFont(new Font("Serif", Font.ITALIC, 20));
        g.drawString("A New Adventure", 430, 190);

        menuButtons.clear();
        String[] options = {"🎮 New Game", "📂 Load Game", "📊 Statistics", "❌ Exit"};
        
        for (int i = 0; i < options.length; i++) {
            Rectangle button = new Rectangle(350, 280 + (i * 90), 300, 70);
            menuButtons.add(button);
            
            boolean hovered = (i == hoveredButton);
            
            g.setColor(new Color(0, 0, 0, 120));
            g.fillRoundRect(button.x + 4, button.y + 4, button.width, button.height, 20, 20);
            
            GradientPaint btnGradient;
            if (hovered) {
                btnGradient = new GradientPaint(
                    button.x, button.y, new Color(100, 100, 200),
                    button.x, button.y + button.height, new Color(150, 150, 255)
                );
            } else {
                btnGradient = new GradientPaint(
                    button.x, button.y, new Color(50, 50, 90),
                    button.x, button.y + button.height, new Color(70, 70, 110)
                );
            }
            g.setPaint(btnGradient);
            g.fillRoundRect(button.x, button.y, button.width, button.height, 20, 20);
            
            g.setColor(hovered ? new Color(200, 200, 255) : new Color(150, 150, 200));
            g.setStroke(new BasicStroke(hovered ? 4 : 2));
            g.drawRoundRect(button.x, button.y, button.width, button.height, 20, 20);
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 22));
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(options[i]);
            g.drawString(options[i], button.x + (button.width - textWidth)/2, button.y + 43);
        }

        g.setColor(new Color(150, 150, 150));
        g.setFont(new Font("Arial", Font.PLAIN, 12));
    }
    
    private void drawLoadMenu(Graphics2D g) {
        g.setColor(new Color(255, 215, 0));
        g.setFont(new Font("Serif", Font.BOLD, 40));
        g.drawString("📂 LOAD GAME", 370, 100);
        
        if (saves.isEmpty()) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.ITALIC, 24));
            g.drawString("No saved games found", 380, 350);
        } else {
            for (int i = 0; i < saves.size() && i < 5; i++) {
                SaveData save = saves.get(i);
                Rectangle slot = new Rectangle(200, 150 + (i * 100), 600, 80);
                
                drawPanel(g, slot.x, slot.y, slot.width, slot.height, new Color(60, 60, 90, 200));
                g.setColor(new Color(150, 200, 255));
                g.setStroke(new BasicStroke(2));
                g.drawRoundRect(slot.x, slot.y, slot.width, slot.height, 15, 15);
                
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 18));
                g.drawString(save.saveName, slot.x + 20, slot.y + 30);
                
                g.setFont(new Font("Arial", Font.PLAIN, 14));
                g.drawString("Level: " + save.level + " | Gold: " + save.gold, slot.x + 20, slot.y + 55);
                g.drawString("Date: " + (save.saveDate != null ? save.saveDate : "Unknown"), slot.x + 20, slot.y + 70);
            }
        }

        Rectangle backBtn = new Rectangle(50, 620, 200, 50);
        g.setColor(new Color(70, 70, 100));
        g.fillRoundRect(backBtn.x, backBtn.y, backBtn.width, backBtn.height, 15, 15);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("← Back", backBtn.x + 60, backBtn.y + 32);
    }
    
    private void drawStatsMenu(Graphics2D g) {
        g.setColor(new Color(255, 215, 0));
        g.setFont(new Font("Serif", Font.BOLD, 40));
        g.drawString("📊 STATISTICS", 370, 100);
        
        Map<String, Integer> stats = dbManager.getStatistics();
        
        drawPanel(g, 200, 150, 600, 450, new Color(60, 60, 90, 200));
        g.setColor(new Color(150, 200, 255));
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(200, 150, 600, 450, 25, 25);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        int y = 220;
        
        String[] statLabels = {
            "⚔️ Total Battles: " + stats.getOrDefault("total_battles", 0),
            "🏆 Total Wins: " + stats.getOrDefault("total_wins", 0),
            "💀 Total Losses: " + stats.getOrDefault("total_losses", 0),
            "💥 Total Damage Dealt: " + stats.getOrDefault("total_damage_dealt", 0),
            "💰 Total Gold Earned: " + stats.getOrDefault("total_gold_earned", 0)
        };
        
        for (String label : statLabels) {
            g.drawString(label, 250, y);
            y += 60;
        }

        int totalBattles = stats.getOrDefault("total_battles", 0);
        int totalWins = stats.getOrDefault("total_wins", 0);
        double winRate = totalBattles > 0 ? (totalWins * 100.0 / totalBattles) : 0;
        
        g.setColor(new Color(255, 215, 0));
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString(String.format("📈 Win Rate: %.1f%%", winRate), 310, y + 20);

        Rectangle backBtn = new Rectangle(50, 620, 200, 50);
        g.setColor(new Color(70, 70, 100));
        g.fillRoundRect(backBtn.x, backBtn.y, backBtn.width, backBtn.height, 15, 15);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("← Back", backBtn.x + 60, backBtn.y + 32);
    }
}
