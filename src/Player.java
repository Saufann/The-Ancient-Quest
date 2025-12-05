import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Player extends Character {

    public int level = 1;
    public int mp = 50, maxMp = 50;
    public int exp = 0, expToNextLevel = 100;
    public int gold = 50;

    public List<Item> inventory = new ArrayList<>();
    public Item equippedWeapon = null;
    public Item equippedArmor = null;

    public Player() {
        super("Hero", 100, 15, 10);

        inventory.add(new Item("Health Potion", ItemType.CONSUMABLE, 0, 50, 0, 0, 25));
        inventory.add(new Item("Mana Potion", ItemType.CONSUMABLE, 0, 0, 30, 0, 20));
        inventory.add(new Item("Iron Sword", ItemType.WEAPON, 10, 0, 0, 0, 100));
    }

    public void resetStats() {
        this.hp = this.maxHp;
        this.mp = this.maxMp;
    }

    @Override
    public void takeDamage(int damage) {
        int actualDamage = Math.max(1, damage - defense);
        hp = Math.max(0, hp - actualDamage);
    }

    @Override
    public int calculateAttack() {
        return attack + (equippedWeapon != null ? equippedWeapon.attackBonus : 0);
    }

    @Override
    public void displayInfo(Graphics2D g, int x, int y) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString(name + " - Lv." + level, x, y);
        g.drawString("HP: " + hp + "/" + maxHp, x, y + 20);
    }

    public void gainExp(int amount) {
        exp += amount;
        if (exp >= expToNextLevel) levelUp();
    }

    public void levelUp() {
        level++;
        exp = 0;
        expToNextLevel *= 1.5;
        maxHp += 20;
        hp = maxHp;
        maxMp += 10;
        mp = maxMp;
        attack += 5;
        defense += 3;
    }

    public void restoreMp(int amount) {
        mp = Math.min(mp + amount, maxMp);
    }

    public int getTotalDefense() {
        return defense + (equippedArmor != null ? equippedArmor.defenseBonus : 0);
    }

    public void loadFromSave(SaveData save) {
        this.level = save.level;
        this.hp = save.hp;
        this.maxHp = save.maxHp;
        this.mp = save.mp;
        this.maxMp = save.maxMp;
        this.attack = save.attack;
        this.defense = save.defense;
        this.exp = save.exp;
        this.gold = save.gold;
        this.inventory = save.inventory;
    }
}