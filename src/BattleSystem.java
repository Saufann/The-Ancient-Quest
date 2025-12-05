import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class BattleSystem {
    private GamePanel gamePanel;
    private Player player;
    private Enemy enemy;
    private DatabaseManager dbManager;
    private boolean playerTurn = true;
    private String battleLog = "";
    private List<Rectangle> actionButtons = new ArrayList<>();
    private int hoveredButton = -1;
    private Thread battleAnimationThread;
    private boolean animating = false;
    private int damageFlash = 0;
    private int enemyDamageFlash = 0;
    private String currentBg;
    
    BattleSystem(GamePanel gamePanel, Player player, DatabaseManager dbManager) {
        this.gamePanel = gamePanel;
        this.player = player;
        this.dbManager = dbManager;
    }
    
    void startBattle(Enemy enemy, String bg) {
        this.enemy = enemy;
        this.currentBg = bg; 
        playerTurn = true;
        battleLog = "⚔️ Battle started against " + enemy.name + "!";
        damageFlash = 0;
        enemyDamageFlash = 0;
        dbManager.updateStatistics("total_battles", 1);
    }
    
    void handleClick(int x, int y) {
        if (animating || !playerTurn) return;
        
        for (int i = 0; i < actionButtons.size(); i++) {
            if (actionButtons.get(i).contains(x, y)) {
                handleAction(i);
                return;
            }
        }
    }
    
    void handleHover(int x, int y) {
        hoveredButton = -1;
        for (int i = 0; i < actionButtons.size(); i++) {
            if (actionButtons.get(i).contains(x, y)) {
                hoveredButton = i;
                break;
            }
        }
    }
    
    void handleAction(int action) {
        animating = true;
        
        battleAnimationThread = new Thread(() -> {
            try {
                if (action == 0) { // Attack
                    int damage = Math.max(1, player.calculateAttack() - enemy.defense);
                    enemy.takeDamage(damage);
                    enemyDamageFlash = 255;
                    battleLog = "⚔️ You dealt " + damage + " damage!";
                    dbManager.updateStatistics("total_damage_dealt", damage);
                    Thread.sleep(1200);
                } else if (action == 1) { // Magic
                    if (player.mp >= 20) {
                        player.mp -= 20;
                        int damage = (int)(player.calculateAttack() * 1.8);
                        enemy.takeDamage(damage);
                        enemyDamageFlash = 255;
                        battleLog = "✨ Magic blast! Dealt " + damage + " damage!";
                        dbManager.updateStatistics("total_damage_dealt", damage);
                        Thread.sleep(1200);
                    } else {
                        battleLog = "❌ Not enough MP!";
                        Thread.sleep(1200);
                        animating = false;
                        return;
                    }
                } else if (action == 2) { // Potion
                    boolean used = false;
                    for (Item item : player.inventory) {
                        if (item.name.equals("Health Potion")) {
                            player.heal(item.hpRestore);
                            player.inventory.remove(item);
                            battleLog = "🧪 Used Health Potion! Restored " + item.hpRestore + " HP!";
                            used = true;
                            break;
                        }
                    }
                    if (!used) {
                        battleLog = "❌ No potions available!";
                        Thread.sleep(1200);
                        animating = false;
                        return;
                    }
                    Thread.sleep(1200);
                }
                
                if (!enemy.isAlive()) {
                    battleLog = "🎉 VICTORY! +" + enemy.expReward + " EXP, +" + enemy.goldReward + " Gold!";
                    player.gainExp(enemy.expReward);
                    player.gold += enemy.goldReward;
                    dbManager.updateStatistics("total_wins", 1);
                    dbManager.updateStatistics("total_gold_earned", enemy.goldReward);
                    Thread.sleep(2500);
                    gamePanel.endBattle(true);
                    animating = false;
                    return;
                }
                
                playerTurn = false;
                Thread.sleep(800);
                int enemyDamage = enemy.calculateAttack();
                player.takeDamage(enemyDamage);
                damageFlash = 255;
                battleLog = "💥 " + enemy.name + " dealt " + enemyDamage + " damage!";
                Thread.sleep(1800);
                
                if (!player.isAlive()) {
                    player.hp = 0;
                    battleLog = "💀 You were defeated...";
                    dbManager.updateStatistics("total_losses", 1);
                    Thread.sleep(2500);
                    gamePanel.endBattle(false);
                    animating = false;
                    return;
                }
                
                playerTurn = true;
                animating = false;
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        battleAnimationThread.start();
    }
    
    void draw(Graphics2D g) {
        drawBattleBackground(g);
        drawEnemy(g);
        drawPlayer(g);
        drawBattleLog(g);
        
        if (playerTurn && !animating) {
            drawActionButtons(g);
        } else if (!playerTurn) {
            drawEnemyTurnIndicator(g);
        }
        
        if (damageFlash > 0) {
            g.setColor(new Color(255, 0, 0, damageFlash));
            g.fillOval(100, 384, 196, 196); 
            damageFlash = Math.max(0, damageFlash - 15);
        }

        if (enemyDamageFlash > 0) {
            g.setColor(new Color(255, 100, 0, enemyDamageFlash));
            g.fillOval(620, 280, 300, 300);
            enemyDamageFlash = Math.max(0, enemyDamageFlash - 15);
        }
    }
    
    private void drawBattleBackground(Graphics2D g) {
        Image bgImg = null;
        if (currentBg != null) {
            bgImg = AssetManager.getImage(currentBg.toLowerCase());
        }
        
        if (bgImg != null) {
            g.drawImage(bgImg, 0, 0, 1000, 700, null);
        } else {
            GradientPaint bg = new GradientPaint(
                0, 0, new Color(40, 10, 20),
                0, 700, new Color(10, 5, 10)
            );
            g.setPaint(bg);
            g.fillRect(0, 0, 1000, 700);
        }
    }
    
    private void drawUnitInfo(Graphics2D g, String name, int hp, int maxHp, int centerX, int spriteTopY) {
        int barWidth = 120;
        int barHeight = 12;
        int padding = 8;
        
        g.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        int nameWidth = fm.stringWidth(name);
        
        int contentWidth = Math.max(nameWidth, barWidth);
        int frameWidth = contentWidth + (padding * 2);
        int frameHeight = 40; 
        
        int frameX = centerX - (frameWidth / 2);
        
        int frameY = spriteTopY - frameHeight + 20; 
        
        // Frame Background
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRoundRect(frameX, frameY, frameWidth, frameHeight, 10, 10);
        
        // Border
        g.setColor(new Color(200, 200, 200, 200));
        g.setStroke(new BasicStroke(1));
        g.drawRoundRect(frameX, frameY, frameWidth, frameHeight, 10, 10);
        
        // Nama
        g.setColor(Color.WHITE);
        int textX = frameX + (frameWidth - nameWidth) / 2;
        g.drawString(name, textX, frameY + 16);
        
        // HP Bar Background
        int barX = frameX + (frameWidth - barWidth) / 2;
        int barY = frameY + 22;
        g.setColor(new Color(60, 60, 60));
        g.fillRoundRect(barX, barY, barWidth, barHeight, 4, 4);
        
        // HP Bar Foreground
        double hpPercent = (double) hp / maxHp;
        if (hpPercent > 0.5) g.setColor(new Color(50, 220, 50));
        else if (hpPercent > 0.25) g.setColor(Color.ORANGE);
        else g.setColor(Color.RED);
        
        int currentBarWidth = (int)(barWidth * hpPercent);
        g.fillRoundRect(barX, barY, currentBarWidth, barHeight, 4, 4);
        
        // HP Text
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        String hpText = hp + "/" + maxHp;
        FontMetrics fm2 = g.getFontMetrics();
        int hpTextX = barX + (barWidth - fm2.stringWidth(hpText)) / 2;
        g.drawString(hpText, hpTextX, barY + 10);
    }

    private void drawEnemy(Graphics2D g) {
        Image enemyImg = null;
        if (enemy.name.contains("Goblin")) {
            enemyImg = AssetManager.getImage("goblin");
        } else if (enemy.name.contains("Knight") || enemy.name.contains("Dark")) {
            enemyImg = AssetManager.getImage("boss");
        }

        int enemySize = 300;
        int enemyY = 280; 
        int enemyX = 620;

        if (enemyImg != null) {
            g.drawImage(enemyImg, enemyX, enemyY, enemySize, enemySize, null);
        } else {
            g.setColor(Color.RED);
            g.fillOval(enemyX, enemyY, 150, 150);
        }
        
        drawUnitInfo(g, enemy.name, enemy.hp, enemy.maxHp, enemyX + (enemySize / 2), enemyY + 30);
    }
    
    private void drawPlayer(Graphics2D g) {
        Image playerImg = AssetManager.getImage("hero");

        int playerSize = 196; 
        
        int groundY = 580; 
        int playerY = groundY - playerSize; 
        
        int playerX = 100;

        if (playerImg != null) {
            g.drawImage(playerImg, playerX, playerY, playerSize, playerSize, null);
        } else {
            g.setColor(Color.BLUE);
            g.fillOval(playerX, playerY, playerSize, playerSize);
        }
        
        drawUnitInfo(g, player.name, player.hp, player.maxHp, 
                     playerX + (playerSize / 2),
                     playerY - 20);              
    }
    
    private void drawBattleLog(Graphics2D g) {
        int logY = 590;
        int logHeight = 40; 
        
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(200, logY, 600, logHeight, 20, 20); 
        
        g.setColor(new Color(150, 200, 255));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(200, logY, 600, logHeight, 20, 20);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Serif", Font.BOLD, 15));
        FontMetrics fm = g.getFontMetrics();
        int textX = 500 - fm.stringWidth(battleLog) / 2;
        
        g.drawString(battleLog, textX, logY + 25);
    }
    
    private void drawActionButtons(Graphics2D g) {
        actionButtons.clear();
        String[] actions = {"⚔️ Attack", "✨ Magic", "🧪 Potion"};
        Color[] colors = {
            new Color(200, 50, 50),
            new Color(100, 100, 255),
            new Color(50, 200, 100)
        };
        
        int buttonY = 640;
        int btnWidth = 200;
        int btnHeight = 45;
        int spacing = 20;   
        int startX = (1000 - (3 * btnWidth + 2 * spacing)) / 2;

        for (int i = 0; i < actions.length; i++) {
            Rectangle button = new Rectangle(startX + (i * (btnWidth + spacing)), buttonY, btnWidth, btnHeight);
            actionButtons.add(button);
            
            boolean hovered = (i == hoveredButton);
            
            g.setColor(new Color(0, 0, 0, 120));
            g.fillRoundRect(button.x + 3, button.y + 3, button.width, button.height, 15, 15);
            
            GradientPaint btnGradient = new GradientPaint(
                button.x, button.y, 
                hovered ? colors[i].brighter() : colors[i],
                button.x, button.y + button.height,
                hovered ? colors[i] : colors[i].darker()
            );
            g.setPaint(btnGradient);
            g.fillRoundRect(button.x, button.y, button.width, button.height, 15, 15);
            
            g.setColor(hovered ? Color.WHITE : new Color(200, 200, 200));
            g.setStroke(new BasicStroke(hovered ? 3 : 2));
            g.drawRoundRect(button.x, button.y, button.width, button.height, 15, 15);
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(actions[i]);
            
            g.drawString(actions[i], button.x + (button.width - textWidth)/2, button.y + 28);
        }
    }
    
    private void drawEnemyTurnIndicator(Graphics2D g) {
        long time = System.currentTimeMillis();
        int alpha = (int)(Math.abs(Math.sin(time / 300.0)) * 155 + 100);
        
        g.setColor(new Color(255, 100, 100, alpha));
        g.setFont(new Font("Arial", Font.BOLD, 22));
        String text = "⚡ ENEMY TURN ⚡";
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        
        g.drawString(text, 500 - textWidth/2, 570);
    }
}