public class Item {

    public String name;
    public ItemType type;

    public int attackBonus;
    public int hpRestore;
    public int mpRestore;
    public int defenseBonus;
    public int value;

    public Item(String name, ItemType type, int atk, int hp, int mp, int def, int value) {
        this.name = name;
        this.type = type;
        this.attackBonus = atk;
        this.hpRestore = hp;
        this.mpRestore = mp;
        this.defenseBonus = def;
        this.value = value;
    }
}
