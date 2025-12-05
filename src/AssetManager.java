import javax.swing.ImageIcon;
import java.awt.Image;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AssetManager {

    // Penyimpanan memori untuk gambar
    private static Map<String, Image> images = new HashMap<>();

    public static void loadAssets() {
        System.out.println("⏳ Sedang memuat aset dari folder 'assets'...");

        // ============================================================
        // 1. BACKGROUNDS
        // Pastikan nama file di folder assets sama persis (huruf besar/kecil)
        // ============================================================
        loadImage("forest", "assets/Forrest_BG.png"); 
        loadImage("ruins", "assets/Ruins_BG.png");

        // ============================================================
        // 2. HERO CHARACTER
        // ============================================================
        loadImage("hero", "assets/Hero_KnightPNG.png");
        loadImage("hero_attack", "assets/AttackPNG.png");
        
        // ============================================================
        // 3. ENEMIES (MUSUH)
        // ============================================================
        loadImage("goblin", "assets/GoblinPNG.png");
        loadImage("boss", "assets/Dark_KnightPNG.png");

        System.out.println("✅ Semua aset selesai dimuat!");
    }

    private static void loadImage(String key, String path) {
        try {
            // Menggunakan File agar bisa membaca dari folder project root
            File file = new File(path);
            if (file.exists()) {
                Image img = new ImageIcon(path).getImage();
                images.put(key, img);
            } else {
                System.err.println("❌ FILE HILANG: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Image getImage(String key) {
        return images.get(key);
    }
}