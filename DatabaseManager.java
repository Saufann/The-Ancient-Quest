import java.sql.*;
import java.util.*;

class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/rpg_game?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "your_password_here";

    private Connection connection;

    public DatabaseManager() {
        initDatabase();
    }

    private void initDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            createTables();

            System.out.println("✅ MySQL connected successfully!");

        } catch (Exception e) {
            System.out.println("❌ MySQL connection failed: " + e.getMessage());
        }
    }

    private void createTables() {
        try (Statement stmt = connection.createStatement()) {

            String createSaveTable = """
                CREATE TABLE IF NOT EXISTS save_data (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    save_name VARCHAR(255) NOT NULL,
                    player_level INT,
                    player_hp INT,
                    player_max_hp INT,
                    player_mp INT,
                    player_max_mp INT,
                    player_attack INT,
                    player_defense INT,
                    player_exp INT,
                    player_gold INT,
                    current_scene INT,
                    save_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

            String createInventoryTable = """
                CREATE TABLE IF NOT EXISTS inventory (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    save_id INT,
                    item_name VARCHAR(255),
                    item_type VARCHAR(50),
                    attack_bonus INT,
                    hp_restore INT,
                    mp_restore INT,
                    defense_bonus INT,
                    value INT,
                    FOREIGN KEY(save_id) REFERENCES save_data(id) ON DELETE CASCADE
                )
                """;

            String createStatsTable = """
                CREATE TABLE IF NOT EXISTS statistics (
                    id INT PRIMARY KEY,
                    total_battles INT DEFAULT 0,
                    total_wins INT DEFAULT 0,
                    total_losses INT DEFAULT 0,
                    total_damage_dealt INT DEFAULT 0,
                    total_gold_earned INT DEFAULT 0,
                    play_time INT DEFAULT 0
                )
                """;

            stmt.execute(createSaveTable);
            stmt.execute(createInventoryTable);
            stmt.execute(createStatsTable);

            stmt.execute("""
                INSERT INTO statistics (id)
                SELECT 1 FROM DUAL
                WHERE NOT EXISTS (SELECT * FROM statistics WHERE id = 1)
                """);

            System.out.println("✅ MySQL tables ready");

        } catch (SQLException e) {
            System.out.println("❌ Error creating MySQL tables: " + e.getMessage());
        }
    }

    public boolean saveGame(String saveName, Player player, int currentScene) {
        try {
            String sql = """
                INSERT INTO save_data
                (save_name, player_level, player_hp, player_max_hp,
                 player_mp, player_max_mp, player_attack, player_defense,
                 player_exp, player_gold, current_scene)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

            PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, saveName);
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
            if (rs.next()) saveId = rs.getInt(1);

            for (Item item : player.inventory) {
                saveInventoryItem(saveId, item);
            }

            System.out.println("💾 Game saved to MySQL: " + saveName);
            return true;

        } catch (SQLException e) {
            System.out.println("❌ Save failed: " + e.getMessage());
            return false;
        }
    }

    private void saveInventoryItem(int saveId, Item item) {
        try {
            String sql = """
                INSERT INTO inventory
                (save_id, item_name, item_type, attack_bonus, hp_restore,
                 mp_restore, defense_bonus, value)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, saveId);
            pstmt.setString(2, item.name);
            pstmt.setString(3, item.type.toString());
            pstmt.setInt(4, item.attackBonus);
            pstmt.setInt(5, item.hpRestore);
            pstmt.setInt(6, item.mpRestore);
            pstmt.setInt(7, item.defenseBonus);
            pstmt.setInt(8, item.value);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("❌ Inventory save error: " + e.getMessage());
        }
    }

    public SaveData loadGame(int saveId) {
        try {
            PreparedStatement pstmt = connection.prepareStatement(
                "SELECT * FROM save_data WHERE id = ?");
            pstmt.setInt(1, saveId);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) return null;

            SaveData save = new SaveData();
            save.saveId = rs.getInt("id");
            save.saveName = rs.getString("save_name");
            save.level = rs.getInt("player_level");
            save.hp = rs.getInt("player_hp");
            save.maxHp = rs.getInt("player_max_hp");
            save.mp = rs.getInt("player_mp");
            save.maxMp = rs.getInt("player_max_mp");
            save.attack = rs.getInt("player_attack");
            save.defense = rs.getInt("player_defense");
            save.exp = rs.getInt("player_exp");
            save.gold = rs.getInt("player_gold");
            save.currentScene = rs.getInt("current_scene");
            save.saveDate = rs.getString("save_date");

            save.inventory = loadInventory(saveId);

            System.out.println("📂 Loaded save: " + save.saveName);
            return save;

        } catch (SQLException e) {
            System.out.println("❌ Load failed: " + e.getMessage());
            return null;
        }
    }

    private List<Item> loadInventory(int saveId) {
        List<Item> items = new ArrayList<>();
        try {
            PreparedStatement pstmt = connection.prepareStatement(
                "SELECT * FROM inventory WHERE save_id = ?");
            pstmt.setInt(1, saveId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                items.add(new Item(
                    rs.getString("item_name"),
                    ItemType.valueOf(rs.getString("item_type")),
                    rs.getInt("attack_bonus"),
                    rs.getInt("hp_restore"),
                    rs.getInt("mp_restore"),
                    rs.getInt("defense_bonus"),
                    rs.getInt("value")
                ));
            }

        } catch (SQLException e) {
            System.out.println("❌ Inventory load error: " + e.getMessage());
        }
        return items;
    }

    public List<SaveData> getAllSaves() {
        List<SaveData> list = new ArrayList<>();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("""
                SELECT id, save_name, player_level, player_gold, save_date
                FROM save_data
                ORDER BY id DESC
                """);

            while (rs.next()) {
                SaveData data = new SaveData();
                data.saveId = rs.getInt("id");
                data.saveName = rs.getString("save_name");
                data.level = rs.getInt("player_level");
                data.gold = rs.getInt("player_gold");
                data.saveDate = rs.getString("save_date");
                list.add(data);
            }

        } catch (SQLException e) {
            System.out.println("❌ Error getting saves: " + e.getMessage());
        }
        return list;
    }

    public void updateStatistics(String field, int value) {
        try {
            PreparedStatement pstmt = connection.prepareStatement(
                "UPDATE statistics SET " + field + " = " + field + " + ? WHERE id = 1");
            pstmt.setInt(1, value);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("❌ Stats update failed: " + e.getMessage());
        }
    }

    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM statistics WHERE id = 1");

            if (rs.next()) {
                stats.put("total_battles", rs.getInt("total_battles"));
                stats.put("total_wins", rs.getInt("total_wins"));
                stats.put("total_losses", rs.getInt("total_losses"));
                stats.put("total_damage_dealt", rs.getInt("total_damage_dealt"));
                stats.put("total_gold_earned", rs.getInt("total_gold_earned"));
                stats.put("play_time", rs.getInt("play_time"));
            }

        } catch (SQLException e) {
            System.out.println("❌ Stats read failed: " + e.getMessage());
        }
        return stats;
    }

    public void close() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException ignored) {}
    }
}
