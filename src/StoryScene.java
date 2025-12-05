import java.util.ArrayList;
import java.util.List;

class StoryScene {
    String background;
    String characterName;
    String dialogue;
    List<Choice> choices;
    boolean hasBattle;
    Enemy battleEnemy;
    
    StoryScene(String bg, String name, String dialogue) {
        this.background = bg;
        this.characterName = name;
        this.dialogue = dialogue;
        this.choices = new ArrayList<>();
    }
    
    void addChoice(String text, int nextScene) {
        choices.add(new Choice(text, nextScene));
    }
    
    void setBattle(Enemy enemy) {
        this.hasBattle = true;
        this.battleEnemy = enemy;
    }
}
