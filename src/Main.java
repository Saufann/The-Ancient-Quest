import javax.swing.*;

public class Main extends JFrame {
    public Main() {
        setTitle(" The Ancient Quest");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        AssetManager.loadAssets(); 
        
        GamePanel gamePanel = new GamePanel();
        add(gamePanel);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}