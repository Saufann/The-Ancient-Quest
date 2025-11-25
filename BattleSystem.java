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
    
    BattleSystem(GamePanel gamePanel, Player player, DatabaseManager dbManager) {
        this.gamePanel = gamePanel;
        this.player = player;
        this.dbManager = dbManager;
    }
    
    void startBattle(Enemy enemy) {
        this.enemy = enemy;
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
            g.fillRect(0, 0, 1000, 700);
            damageFlash = Math.max(0, damageFlash - 15);
        }
        if (enemyDamageFlash > 0) {
            g.setColor(new Color(255, 100, 0, enemyDamageFlash));
            g.fillOval(600, 100, 250, 250);
            enemyDamageFlash = Math.max(0, enemyDamageFlash - 15);
        }
    }
    
    private void drawBattleBackground(Graphics2D g) {
        GradientPaint bg = new GradientPaint(
            0, 0, new Color(60, 20, 40),
            0, 700, new Color(20, 10, 30)
        );
        g.setPaint(bg);
        g.fillRect(0, 0, 1000, 700);
        
        long time = System.currentTimeMillis();
        if (time % 3000 < 100) {
            g.setColor(new Color(255, 255, 255, 100));
            g.fillRect(0, 0, 1000, 700);
        }
        
        g.setColor(new Color(100, 50, 100, 30));
        g.setStroke(new BasicStroke(2));
        for (int i = 0; i < 10; i++) {
            g.drawLine(0, i * 70, 1000, i * 70);
            g.drawLine(i * 100, 0, i * 100, 700);
        }
    }
    
    private void drawEnemy(Graphics2D g) {
        int glowSize = (int)(Math.sin(System.currentTimeMillis() / 500.0) * 10 + 180);
        g.setColor(new Color(255, 50, 50, 50));
        g.fillOval(650 - (glowSize - 150)/2, 150 - (glowSize - 150)/2, glowSize, glowSize);
        
        GradientPaint enemyGradient = new GradientPaint(
            650, 150, new Color(200, 50, 50),
            650, 300, new Color(150, 20, 20)
        );
        g.setPaint(enemyGradient);
        g.fillOval(650, 150, 150, 150);
        
        g.setColor(new Color(255, 100, 100));
        g.fillOval(670, 180, 40, 40);
        g.fillOval(740, 180, 40, 40);
        g.setColor(Color.BLACK);
        g.fillOval(685, 195, 15, 15);
        g.fillOval(755, 195, 15, 15);
        
        enemy.displayInfo(g, 660, 330);
        
        int barY = 350;
        g.setColor(new Color(40, 40, 40));
        g.fillRoundRect(600, barY, 250, 25, 12, 12);
        
        int enemyHpWidth = (int)(250.0 * enemy.hp / enemy.maxHp);
        GradientPaint hpGradient = new GradientPaint(
            600, barY, new Color(255, 50, 50),
            600 + enemyHpWidth, barY, new Color(200, 20, 20)
        );
        g.setPaint(hpGradient);
        g.fillRoundRect(600, barY, enemyHpWidth, 25, 12, 12);
        
        g.setColor(new Color(255, 255, 255, 200));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(600, barY, 250, 25, 12, 12);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        String hpText = enemy.hp + " / " + enemy.maxHp;
        FontMetrics fm = g.getFontMetrics();
        g.drawString(hpText, 725 - fm.stringWidth(hpText)/2, barY + 17);
    }
    
    private void drawPlayer(Graphics2D g) {
        int glowSize = (int)(Math.sin(System.currentTimeMillis() / 500.0) * 10 + 140);
        g.setColor(new Color(100, 200, 255, 50));
        g.fillOval(150 - (glowSize - 120)/2, 200 - (glowSize - 120)/2, glowSize, glowSize);
        
        GradientPaint playerGradient = new GradientPaint(
            150, 200, new Color(100, 200, 255),
            150, 320, new Color(50, 150, 220)
        );
        g.setPaint(playerGradient);
        g.fillOval(150, 200, 120, 120);
        
        g.setColor(new Color(255, 215, 0));
        int[] xPoints = {210, 180, 210, 240};
        int[] yPoints = {240, 260, 280, 260};
        g.fillPolygon(xPoints, yPoints, 4);
        
        player.displayInfo(g, 170, 350);
    }
    
    private void drawBattleLog(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(55, 395, 900, 90, 20, 20);
        
        GradientPaint logGradient = new GradientPaint(
            50, 390, new Color(30, 30, 50, 230),
            50, 480, new Color(50, 50, 80, 230)
        );
        g.setPaint(logGradient);
        g.fillRoundRect(50, 390, 900, 90, 20, 20);
        
        g.setColor(new Color(150, 200, 255));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(50, 390, 900, 90, 20, 20);
        
        g.setColor(new Color(0, 0, 0, 100));
        g.setFont(new Font("Serif", Font.BOLD, 18));
        g.drawString(battleLog, 82, 437);
        g.setColor(Color.WHITE);
        g.drawString(battleLog, 80, 435);
    }
    
    private void drawActionButtons(Graphics2D g) {
        actionButtons.clear();
        String[] actions = {"⚔️ Attack", "✨ Magic (20 MP)", "🧪 Use Potion"};
        Color[] colors = {
            new Color(200, 50, 50),
            new Color(100, 100, 255),
            new Color(50, 200, 100)
        };
        
        for (int i = 0; i < actions.length; i++) {
            Rectangle button = new Rectangle(80 + (i * 300), 520, 280, 70);
            actionButtons.add(button);
            
            boolean hovered = (i == hoveredButton);
            
            g.setColor(new Color(0, 0, 0, 100));
            g.fillRoundRect(button.x + 4, button.y + 4, button.width, button.height, 20, 20);
            
            GradientPaint btnGradient = new GradientPaint(
                button.x, button.y, 
                hovered ? colors[i].brighter() : colors[i],
                button.x, button.y + button.height,
                hovered ? colors[i] : colors[i].darker()
            );
            g.setPaint(btnGradient);
            g.fillRoundRect(button.x, button.y, button.width, button.height, 20, 20);
            
            g.setColor(hovered ? Color.WHITE : new Color(200, 200, 200));
            g.setStroke(new BasicStroke(hovered ? 4 : 2));
            g.drawRoundRect(button.x, button.y, button.width, button.height, 20, 20);
            
            g.setColor(new Color(0, 0, 0, 150));
            g.setFont(new Font("Arial", Font.BOLD, 18));
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(actions[i]);
            g.drawString(actions[i], button.x + (button.width - textWidth)/2 + 2, button.y + 42);
            
            g.setColor(Color.WHITE);
            g.drawString(actions[i], button.x + (button.width - textWidth)/2, button.y + 40);
        }
    }
    
    private void drawEnemyTurnIndicator(Graphics2D g) {
        long time = System.currentTimeMillis();
        int alpha = (int)(Math.abs(Math.sin(time / 300.0)) * 155 + 100);
        
        g.setColor(new Color(255, 100, 100, alpha));
        g.setFont(new Font("Arial", Font.BOLD, 26));
        String text = "⚡ ENEMY TURN ⚡";
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g.drawString(text, 500 - textWidth/2, 560);
    }
}
