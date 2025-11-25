import java.awt.*;

public abstract class Character {
    protected String name;
    protected int hp, maxHp, attack, defense;

    public Character(String name, int hp, int attack, int defense) {
        this.name = name;
        this.hp = hp;
        this.maxHp = hp;
        this.attack = attack;
        this.defense = defense;
    }

    public abstract void takeDamage(int damage);
    public abstract int calculateAttack();
    public abstract void displayInfo(Graphics2D g, int x, int y);

    public boolean isAlive() {
        return hp > 0;
    }

    public void heal(int amount) {
        hp = Math.min(hp + amount, maxHp);
    }
}
