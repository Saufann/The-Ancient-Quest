import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class GamePanel extends JPanel {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 700;
    
    GameState gameState = GameState.MENU;
    Player player;
    StoryManager storyManager;
    BattleSystem battleSystem;
    InventoryPanel inventoryPanel;
    MainMenu mainMenu;
    ParticleSystem particleSystem;
    DatabaseManager dbManager;
    
    private boolean showInventory = false;
    
    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(15, 15, 25));
        setFocusable(true);
        
        dbManager = new DatabaseManager();
        player = new Player();
        storyManager = new StoryManager(this);
        battleSystem = new BattleSystem(this, player, dbManager);
        inventoryPanel = new InventoryPanel(player);
        mainMenu = new MainMenu(this, dbManager);
        particleSystem = new ParticleSystem();
        
        setupMouseListener();
        setupKeyListener();

        new Thread(() -> {
            while (true) {
                particleSystem.update();
                repaint();
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    private void setupMouseListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameState == GameState.MENU) {
                    mainMenu.handleClick(e.getX(), e.getY());
                } else if (gameState == GameState.STORY) {
                    storyManager.handleClick(e.getX(), e.getY());
                } else if (gameState == GameState.BATTLE) {
                    battleSystem.handleClick(e.getX(), e.getY());
                } else if (gameState == GameState.INVENTORY) {
                    inventoryPanel.handleClick(e.getX(), e.getY());
                }
            }
        });
        
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (gameState == GameState.MENU) {
                    mainMenu.handleHover(e.getX(), e.getY());
                } else if (gameState == GameState.STORY) {
                    storyManager.handleHover(e.getX(), e.getY());
                } else if (gameState == GameState.BATTLE) {
                    battleSystem.handleHover(e.getX(), e.getY());
                }
            }
        });
    }
    
    private void setupKeyListener() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_I && gameState == GameState.STORY) {
                    toggleInventory();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (showInventory) {
                        showInventory = false;
                        gameState = GameState.STORY;
                    } else if (gameState == GameState.STORY) {
                        gameState = GameState.MENU;
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_S && e.isControlDown() && gameState == GameState.STORY) {
                    quickSave();
                }
            }
        });
    }
    
    private void toggleInventory() {
        showInventory = !showInventory;
        gameState = showInventory ? GameState.INVENTORY : GameState.STORY;
    }
    
    private void quickSave() {
        String saveName = "QuickSave_" + System.currentTimeMillis();
        dbManager.saveGame(saveName, player, storyManager.getCurrentScene());
        JOptionPane.showMessageDialog(this, "✅ Game Saved!", "Quick Save", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void startBattle(Enemy enemy) {
        gameState = GameState.BATTLE;
        battleSystem.startBattle(enemy);
    }
    
    public void endBattle(boolean victory) {
        gameState = GameState.STORY;
        storyManager.continueAfterBattle(victory);
    }
    
    public void startNewGame() {
        player = new Player();
        battleSystem = new BattleSystem(this, player, dbManager);
        inventoryPanel = new InventoryPanel(player);
        storyManager.showScene(0);
        gameState = GameState.STORY;
    }
    
    public void loadGame(int saveId) {
        SaveData save = dbManager.loadGame(saveId);
        if (save != null) {
            player.loadFromSave(save);
            storyManager.showScene(save.currentScene);
            gameState = GameState.STORY;
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        particleSystem.draw(g2d);
        
        if (gameState == GameState.MENU) {
            mainMenu.draw(g2d);
        } else if (showInventory) {
            inventoryPanel.draw(g2d);
        } else if (gameState == GameState.STORY) {
            storyManager.draw(g2d);
            drawStatusBar(g2d);
        } else if (gameState == GameState.BATTLE) {
            battleSystem.draw(g2d);
        }
    }
    
    private void drawStatusBar(Graphics2D g) {
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(0, 0, 0, 200),
            0, 50, new Color(20, 20, 40, 200)
        );
        g.setPaint(gradient);
        g.fillRect(0, 0, WIDTH, 50);
        
        g.setColor(new Color(100, 150, 255, 100));
        g.fillRect(0, 48, WIDTH, 2);
        
        drawStatusBarItem(g, 20, 15, "❤ HP", player.hp, player.maxHp, 
            new Color(220, 20, 60), new Color(178, 34, 34));
        
        drawStatusBarItem(g, 180, 15, "✦ MP", player.mp, player.maxMp, 
            new Color(65, 105, 225), new Color(25, 25, 112));
        
        g.setColor(new Color(255, 215, 0));
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("⭐ Lv." + player.level, 340, 30);
        
        int expPercent = (int)(100.0 * player.exp / player.expToNextLevel);
        g.setColor(new Color(50, 50, 50));
        g.fillRoundRect(420, 20, 120, 12, 6, 6);
        g.setColor(new Color(255, 215, 0));
        g.fillRoundRect(420, 20, (int)(120.0 * player.exp / player.expToNextLevel), 12, 6, 6);
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        g.drawString(expPercent + "%", 490, 29);
        
        g.setColor(new Color(255, 215, 0));
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("💰 " + player.gold + "G", 580, 30);
        
        g.setColor(new Color(180, 180, 200));
        g.setFont(new Font("Arial", Font.PLAIN, 11));
        g.drawString("[I] Inventory  [Ctrl+S] Save  [ESC] Menu", WIDTH - 270, 30);
    }
    
    private void drawStatusBarItem(Graphics2D g, int x, int y, String label, 
                                   int current, int max, Color color1, Color color2) {
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.setColor(Color.WHITE);
        g.drawString(label, x, y);
        
        g.setColor(new Color(40, 40, 40));
        g.fillRoundRect(x, y + 5, 140, 18, 9, 9);
        
        int barWidth = (int)(140.0 * current / max);
        GradientPaint barGradient = new GradientPaint(
            x, y + 5, color1,
            x + barWidth, y + 5, color2
        );
        g.setPaint(barGradient);
        g.fillRoundRect(x, y + 5, barWidth, 18, 9, 9);
        
        g.setColor(new Color(100, 100, 100));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(x, y + 5, 140, 18, 9, 9);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 11));
        String text = current + "/" + max;
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g.drawString(text, x + 70 - textWidth/2, y + 18);
    }
}
