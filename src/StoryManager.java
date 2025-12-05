import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class StoryManager {
    GamePanel gamePanel;
    private List<StoryScene> scenes;
    private StoryScene currentScene;
    private int currentSceneIndex = 0;
    private String displayedText = "";
    private int textIndex = 0;
    private Thread typewriterThread;
    private boolean textComplete = false;
    private List<Rectangle> choiceButtons = new ArrayList<>();
    private int hoveredChoice = -1;
    
    StoryManager(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        initStory();
    }
    
    private void initStory() {
        scenes = new ArrayList<>();
        
        // Scene 0: Forest
        StoryScene scene0 = new StoryScene("Forest", "Narrator", 
            "You wake up in a mysterious forest. Ancient ruins ahead call your name. The air is thick with magic...");
        scene0.addChoice("⚔️ Explore the ruins bravely", 1);
        scene0.addChoice("🔍 Search the area carefully", 2);
        scenes.add(scene0);
        
        // Scene 1: Ruins Battle (Goblin)
        StoryScene scene1 = new StoryScene("Ruins", "Narrator",
            "As you enter the crumbling ruins, shadows dance on ancient walls. Suddenly, a goblin leaps from the darkness!");
        scene1.setBattle(new Enemy("Goblin Warrior", 50, 12, 5, 50, 30));
        scenes.add(scene1);
        
        // Scene 2: Forest Treasure
        StoryScene scene2 = new StoryScene("Forest", "Narrator",
            "You discover a hidden treasure chest covered in moss! Inside: a shimmering Health Potion and 50 gold coins.");
        scene2.addChoice("✨ Continue to the ruins", 1);
        scenes.add(scene2);
        
        // Scene 3: Ruins Dialogue
        StoryScene scene3 = new StoryScene("Ruins", "Mysterious Voice",
            "Impressive... You possess the spirit of a true warrior. But your journey has only just begun...");
        scene3.addChoice("❓ Who are you?", 4);
        scene3.addChoice("⚡ Show yourself, coward!", 4);
        scenes.add(scene3);
        
        // Scene 4: Ruins Boss Battle
        StoryScene scene4 = new StoryScene("Ruins", "Dark Knight",
            "I am the eternal guardian of these sacred grounds. Prove your worth in combat, or perish like the others!");
        scene4.setBattle(new Enemy("Dark Knight", 120, 20, 12, 150, 100));
        scenes.add(scene4);
        
        // Scene 5: Victory
        StoryScene scene5 = new StoryScene("Ruins", "Narrator",
            "Victory! The Dark Knight's armor shatters. Ancient power flows into you. Your legend begins here...");
        // PERUBAHAN: Next scene -1 menandakan kembali ke Main Menu
        scene5.addChoice("🏆 Return to Main Menu", -1); 
        scenes.add(scene5);
        
        // Scene 6: Game Over
        StoryScene scene6 = new StoryScene("Forest", "Narrator",
            "Darkness embraces you... But legends are forged through failure. Rise again, hero!");
        scene6.addChoice("🔄 Try Again", 0);
        scenes.add(scene6);
    }
    
    int getCurrentScene() {
        return currentSceneIndex;
    }
    
    void showScene(int sceneIndex) {
        if (sceneIndex == -1) {
            gamePanel.returnToMainMenu();
            return;
        }

        if (sceneIndex >= scenes.size()) sceneIndex = 0;
        if (sceneIndex == 0) {
            gamePanel.player.resetStats();
        }
        
        currentSceneIndex = sceneIndex;
        currentScene = scenes.get(sceneIndex);
        displayedText = "";
        textIndex = 0;
        textComplete = false;
        
        if (sceneIndex == 2) {
            gamePanel.player.inventory.add(new Item("Health Potion", ItemType.CONSUMABLE, 0, 50, 0, 0, 25));
            gamePanel.player.gold += 50;
        }
        
        startTypewriter();
    }
    
    private void startTypewriter() {
        if (typewriterThread != null && typewriterThread.isAlive()) {
            typewriterThread.interrupt();
        }
        
        typewriterThread = new Thread(() -> {
            try {
                while (textIndex < currentScene.dialogue.length()) {
                    displayedText = currentScene.dialogue.substring(0, textIndex + 1);
                    textIndex++;
                    Thread.sleep(25);
                }
                textComplete = true;
            } catch (InterruptedException e) {
                displayedText = currentScene.dialogue;
                textComplete = true;
            }
        });
        typewriterThread.start();
    }
    
    void handleClick(int x, int y) {
        if (!textComplete) {
            if (typewriterThread != null) typewriterThread.interrupt();
            displayedText = currentScene.dialogue;
            textComplete = true;
            return;
        }
        
        if (currentScene.hasBattle && currentScene.choices.isEmpty()) {
            gamePanel.startBattle(currentScene.battleEnemy, currentScene.background);
            return;
        }
        
        for (int i = 0; i < choiceButtons.size(); i++) {
            if (choiceButtons.get(i).contains(x, y)) {
                showScene(currentScene.choices.get(i).nextScene);
                return;
            }
        }
    }
    
    void handleHover(int x, int y) {
        hoveredChoice = -1;
        for (int i = 0; i < choiceButtons.size(); i++) {
            if (choiceButtons.get(i).contains(x, y)) {
                hoveredChoice = i;
                break;
            }
        }
    }
    
    void continueAfterBattle(boolean victory) {
        int currentIndex = scenes.indexOf(currentScene);
        if (currentIndex == 4 && victory) {
            showScene(5);
        } else {
            showScene(victory ? currentIndex + 2 : 6);
        }
    }
    
    void draw(Graphics2D g) {
        if (currentScene == null) return;
        
        drawBackground(g, currentScene.background);
        drawDialogueBox(g);
        
        if (textComplete && !currentScene.choices.isEmpty()) {
            drawChoices(g);
        } else if (textComplete && currentScene.hasBattle) {
            drawBattlePrompt(g);
        } else if (!textComplete) {
            drawContinueIndicator(g);
        }
    }
    
    private void drawBackground(Graphics2D g, String bg) {
        Image bgImage = AssetManager.getImage(bg.toLowerCase());

        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, 1000, 700, null);
        } else {
            g.setColor(new Color(20, 20, 30));
            g.fillRect(0, 0, 1000, 700);
        }
    }
    
    private void drawDialogueBox(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 100));
        g.fillRoundRect(55, 455, 900, 200, 25, 25);
        
        GradientPaint boxGradient = new GradientPaint(
            50, 450, new Color(20, 20, 40, 240),
            50, 650, new Color(40, 40, 70, 240)
        );
        g.setPaint(boxGradient);
        g.fillRoundRect(50, 450, 900, 200, 25, 25);
        
        g.setColor(new Color(100, 150, 255, 150));
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(50, 450, 900, 200, 25, 25);
        
        if (currentScene.characterName != null) {
            GradientPaint nameGradient = new GradientPaint(
                70, 430, new Color(255, 215, 0, 230),
                70, 460, new Color(218, 165, 32, 230)
            );
            g.setPaint(nameGradient);
            g.fillRoundRect(70, 430, 250, 40, 15, 15);
            
            g.setColor(new Color(139, 0, 0));
            g.setFont(new Font("Serif", Font.BOLD, 20));
            g.drawString(currentScene.characterName, 90, 455);
        }
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Serif", Font.PLAIN, 17));
        drawWrappedText(g, displayedText, 80, 500, 840);
    }
    
    private void drawChoices(Graphics2D g) {
        choiceButtons.clear();
        int startY = 560;
        
        for (int i = 0; i < currentScene.choices.size(); i++) {
            Choice choice = currentScene.choices.get(i);
            Rectangle button = new Rectangle(100, startY + (i * 55), 800, 45);
            choiceButtons.add(button);
            
            boolean hovered = (i == hoveredChoice);
            
            g.setColor(new Color(0, 0, 0, 80));
            g.fillRoundRect(button.x + 3, button.y + 3, button.width, button.height, 15, 15);
            
            GradientPaint btnGradient;
            if (hovered) {
                btnGradient = new GradientPaint(
                    button.x, button.y, new Color(70, 130, 180),
                    button.x, button.y + button.height, new Color(100, 149, 237)
                );
            } else {
                btnGradient = new GradientPaint(
                    button.x, button.y, new Color(50, 50, 90),
                    button.x, button.y + button.height, new Color(70, 70, 110)
                );
            }
            g.setPaint(btnGradient);
            g.fillRoundRect(button.x, button.y, button.width, button.height, 15, 15);
            
            g.setColor(hovered ? new Color(135, 206, 250) : new Color(120, 120, 160));
            g.setStroke(new BasicStroke(hovered ? 3 : 2));
            g.drawRoundRect(button.x, button.y, button.width, button.height, 15, 15);
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString(choice.text, button.x + 30, button.y + 28);
        }
    }
    
    private void drawBattlePrompt(Graphics2D g) {
        long time = System.currentTimeMillis();
        int alpha = (int)(Math.abs(Math.sin(time / 300.0)) * 155 + 100);
        g.setColor(new Color(255, 100, 100, alpha));
        g.setFont(new Font("Arial", Font.BOLD, 20));
        String text = "⚔️ CLICK TO BEGIN BATTLE ⚔️";
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g.drawString(text, 500 - textWidth/2, 620);
    }
    
    private void drawContinueIndicator(Graphics2D g) {
        long time = System.currentTimeMillis();
        int alpha = (int)(Math.abs(Math.sin(time / 400.0)) * 155 + 100);
        g.setColor(new Color(200, 200, 200, alpha));
        g.setFont(new Font("Arial", Font.ITALIC, 14));
        g.drawString("▼ Click to continue...", 800, 635);
    }
    
    private void drawWrappedText(Graphics2D g, String text, int x, int y, int maxWidth) {
        FontMetrics fm = g.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int lineY = y;
        
        for (String word : words) {
            String testLine = line + word + " ";
            if (fm.stringWidth(testLine) > maxWidth) {
                g.drawString(line.toString(), x, lineY);
                line = new StringBuilder(word + " ");
                lineY += fm.getHeight() + 3;
            } else {
                line.append(word).append(" ");
            }
        }
        g.drawString(line.toString(), x, lineY);
    }
}