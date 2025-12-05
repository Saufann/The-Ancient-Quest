import java.awt.*;

public class Enemy extends Character {

    public int expReward;
    public int goldReward;

    public Enemy(String name, int hp, int attack, int defense, int expReward, int goldReward) {
        super(name, hp, attack, defense);
        this.expReward = expReward;
        this.goldReward = goldReward;
    }

    @Override
    public void takeDamage(int damage) {
        hp = Math.max(0, hp - damage);
    }

    @Override
    public int calculateAttack() {
        return attack + (int)(Math.random() * 5);
    }

    @Override
    public void displayInfo(Graphics2D g, int x, int y) {
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString(name, x, y);
    }
}
