import java.awt.*;

public abstract class UIPanel {

    protected int width, height;

    public UIPanel(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public abstract void draw(Graphics2D g);
    public abstract void handleClick(int x, int y);

    protected void drawPanel(Graphics2D g, int x, int y, int w, int h, Color color) {
        GradientPaint gradient = new GradientPaint(
                x, y, color,
                x, y + h, color.darker()
        );
        g.setPaint(gradient);
        g.fillRoundRect(x, y, w, h, 25, 25);
    }
}
