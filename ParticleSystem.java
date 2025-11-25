import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class ParticleSystem {
    private List<Particle> particles = new ArrayList<>();
    private Random random = new Random();
    
    ParticleSystem() {
        for (int i = 0; i < 50; i++) {
            particles.add(new Particle(
                random.nextInt(1000),
                random.nextInt(700),
                random.nextFloat() * 0.5f + 0.2f
            ));
        }
    }
    
    void update() {
        for (Particle p : particles) {
            p.y -= p.speed;
            p.alpha = (float)(Math.sin(System.currentTimeMillis() / 1000.0 + p.x) * 0.3 + 0.5);
            
            if (p.y < -10) {
                p.y = 710;
                p.x = random.nextInt(1000);
            }
        }
    }
    
    void draw(Graphics2D g) {
        for (Particle p : particles) {
            g.setColor(new Color(200, 200, 255, (int)(p.alpha * 255)));
            g.fillOval((int)p.x, (int)p.y, 3, 3);
        }
    }
    
    class Particle {
        float x, y, speed, alpha;
        
        Particle(float x, float y, float speed) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.alpha = 0.5f;
        }
    }
}
