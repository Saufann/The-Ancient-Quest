import java.sql.*;
import java.util.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:rpg_visual_novel.db";
    private Connection connection;
    
    public DatabaseManager() {
        initDatabase();
    }
    
    private void initDatabase() {
        try {
            // Memuat driver SQLite
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            createTables();
            System.out.println("✅ SQLite Database connected successfully!");
        } catch (Exception e) {
            System.err.println("⚠️ Database connection failed. Make sure sqlite-jdbc jar is added to the library.");
            e.printStackTrace();
        }
    }
    
    private void createTables() {
        try (Statement stmt = connection.createStatement()) {
            
            // Syntax SQLite menggunakan INTEGER PRIMARY KEY AUTOINCREMENT
            String createSaveTable = 
                "CREATE TABLE IF NOT EXISTS save_data (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "save_name TEXT NOT NULL," +
                "player_level INTEGER," +
                "player_hp INTEGER," +
                "player_max_hp INTEGER," +
                "player_mp INTEGER," +
                "player_max_mp INTEGER," +
                "player_attack INTEGER," +
                "player_defense INTEGER," +
                "player_exp INTEGER," +
                "player_gold INTEGER," +
                "current_scene INTEGER," +
                "save_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
            
            String createInventoryTable = 
                "CREATE TABLE IF NOT EXISTS inventory (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "save_id INTEGER," +
                "item_name TEXT," +
                "item_type TEXT," +
                "attack_bonus INTEGER," +
                "hp_restore INTEGER," +
                "mp_restore INTEGER," +
                "defense_bonus INTEGER," +
                "value INTEGER," +
                "FOREIGN KEY(save_id) REFERENCES save_data(id) ON DELETE CASCADE)";
            
            String createStatsTable = 
                "CREATE TABLE IF NOT EXISTS statistics (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "total_battles INTEGER DEFAULT 0," +
                "total_wins INTEGER DEFAULT 0," +
                "total_losses INTEGER DEFAULT 0," +
                "total_damage_dealt INTEGER DEFAULT 0," +
                "total_gold_earned INTEGER DEFAULT 0," +
                "play_time INTEGER DEFAULT 0)";
            
            stmt.execute(createSaveTable);
            stmt.execute(createInventoryTable);
            stmt.execute(createStatsTable);
            
            // Inisialisasi baris statistik jika belum ada
            String initStats = "INSERT INTO statistics (id) SELECT 1 WHERE NOT EXISTS (SELECT 1 FROM statistics)";
            stmt.execute(initStats);
            
            System.out.println("✅ Database tables checked/created successfully!");
        } catch (SQLException e) {
            System.out.println("⚠️ Error creating tables: " + e.getMessage());
        }
    }
    
    public boolean saveGame(String saveName, Player player, int currentScene) {
        if (connection == null) return false;
        
        try {
            String sql = "INSERT INTO save_data (save_name, player_level, player_hp, player_max_hp, " +
                        "player_mp, player_max_mp, player_attack, player_defense, player_exp, " +
                        "player_gold, current_scene) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, saveName);
            // Menggunakan akses field langsung (public) sesuai class Player.java
            pstmt.setInt(2, player.level);
            pstmt.setInt(3, player.hp);
            pstmt.setInt(4, player.maxHp);
            pstmt.setInt(5, player.mp);
            pstmt.setInt(6, player.maxMp);
            pstmt.setInt(7, player.attack);
            pstmt.setInt(8, player.defense);
            pstmt.setInt(9, player.exp);
            pstmt.setInt(10, player.gold);
            pstmt.setInt(11, currentScene);
            
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            int saveId = 0;
            if (rs.next()) {
                saveId = rs.getInt(1);
            }
            
            for (Item item : player.inventory) {
                saveInventoryItem(saveId, item);
            }
            
            System.out.println("💾 Game saved successfully: " + saveName);
            return true;
            
        } catch (SQLException e) {
            System.out.println("❌ Error saving game: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private void saveInventoryItem(int saveId, Item item) {
        try {
            String sql = "INSERT INTO inventory (save_id, item_name, item_type, attack_bonus, " +
                        "hp_restore, mp_restore, defense_bonus, value) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, saveId);
            // Menggunakan akses field langsung (public) sesuai class Item.java
            pstmt.setString(2, item.name);
            pstmt.setString(3, item.type.toString());
            pstmt.setInt(4, item.attackBonus);
            pstmt.setInt(5, item.hpRestore);
            pstmt.setInt(6, item.mpRestore);
            pstmt.setInt(7, item.defenseBonus);
            pstmt.setInt(8, item.value);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("❌ Error saving inventory: " + e.getMessage());
        }
    }
    
    public SaveData loadGame(int saveId) {
        if (connection == null) return null;
        
        try {
            String sql = "SELECT * FROM save_data WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, saveId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                SaveData saveData = new SaveData();
                saveData.saveId = rs.getInt("id"); // Penting: Set ID
                saveData.saveName = rs.getString("save_name");
                saveData.level = rs.getInt("player_level");
                saveData.hp = rs.getInt("player_hp");
                saveData.maxHp = rs.getInt("player_max_hp");
                saveData.mp = rs.getInt("player_mp");
                saveData.maxMp = rs.getInt("player_max_mp");
                saveData.attack = rs.getInt("player_attack");
                saveData.defense = rs.getInt("player_defense");
                saveData.exp = rs.getInt("player_exp");
                saveData.gold = rs.getInt("player_gold");
                saveData.currentScene = rs.getInt("current_scene");
                saveData.saveDate = rs.getString("save_date");
                
                saveData.inventory = loadInventory(saveId);
                
                System.out.println("📂 Game loaded: " + saveData.saveName);
                return saveData;
            }
        } catch (SQLException e) {
            System.out.println("❌ Error loading game: " + e.getMessage());
        }
        return null;
    }
    
    private List<Item> loadInventory(int saveId) {
        List<Item> inventory = new ArrayList<>();
        try {
            String sql = "SELECT * FROM inventory WHERE save_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, saveId);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Item item = new Item(
                    rs.getString("item_name"),
                    ItemType.valueOf(rs.getString("item_type")),
                    rs.getInt("attack_bonus"),
                    rs.getInt("hp_restore"),
                    rs.getInt("mp_restore"),
                    rs.getInt("defense_bonus"),
                    rs.getInt("value")
                );
                inventory.add(item);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error loading inventory: " + e.getMessage());
        }
        return inventory;
    }
    
    public List<SaveData> getAllSaves() {
        List<SaveData> saves = new ArrayList<>();
        if (connection == null) return saves;
        
        try {
            String sql = "SELECT id, save_name, player_level, player_gold, save_date FROM save_data ORDER BY id DESC";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                SaveData save = new SaveData();
                save.saveId = rs.getInt("id");
                save.saveName = rs.getString("save_name");
                save.level = rs.getInt("player_level");
                save.gold = rs.getInt("player_gold");
                save.saveDate = rs.getString("save_date");
                saves.add(save);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error getting saves: " + e.getMessage());
        }
        return saves;
    }
    
    public void updateStatistics(String statType, int value) {
        if (connection == null) return;
        
        try {
            // SQLite tidak mendukung sintaks "field = field + ?" di beberapa versi lama JDBC
            // Tapi syntax standar ini harusnya aman.
            String sql = "UPDATE statistics SET " + statType + " = " + statType + " + ? WHERE id = 1";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, value);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("❌ Error updating statistics: " + e.getMessage());
        }
    }
    
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        if (connection == null) return stats;
        
        try {
            String sql = "SELECT * FROM statistics WHERE id = 1";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                stats.put("total_battles", rs.getInt("total_battles"));
                stats.put("total_wins", rs.getInt("total_wins"));
                stats.put("total_losses", rs.getInt("total_losses"));
                stats.put("total_damage_dealt", rs.getInt("total_damage_dealt"));
                stats.put("total_gold_earned", rs.getInt("total_gold_earned"));
                stats.put("play_time", rs.getInt("play_time"));
            }
        } catch (SQLException e) {
            System.out.println("❌ Error getting statistics: " + e.getMessage());
        }
        return stats;
    }
    
    public void close() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}