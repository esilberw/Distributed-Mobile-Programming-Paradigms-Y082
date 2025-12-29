package weKittens;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class DrawingView extends JPanel {
    private BufferedImage cachedBackgroundImage;

    private final int CARD_SIZE = 128;
    private final int CARD_SIZE_MID = 64;
    private final int CARD_MARGIN = 10;
    private final int STACK_MAX_SIZE = 3;
    private final int MAX_ANGLE = 10;
    private final Card BLANK_CARD = new Card(Card.CardType.back, "card");

    private Card topCard;
    private final Random random;
    private int played = 0;

    private int topPlayerCount = 3;
    private int leftPlayerCount = 3;
    private int rightPlayerCount = 3;

    // Set pour mémoriser les positions "mortes"
    private final Set<String> deadPositions = new HashSet<>();

    public DrawingView() {
        super();
        setBackground(Color.WHITE);
        random = new Random();
        topCard = BLANK_CARD;
    }

    public void playCard(Card card) {
        topCard = card;
        played++;
    }

    public void setTopPlayerCount(int c) { this.topPlayerCount = c; }
    public void setLeftPlayerCount(int c) { this.leftPlayerCount = c; }
    public void setRightPlayerCount(int c) { this.rightPlayerCount = c; }

    /**
     * Marque une position comme morte.
     * Le repaint() déclenchera drawStack qui cachera les cartes.
     */
    public void markDead(String position) {
        deadPositions.add(position);
        this.repaint();
    }

    public void draw(Graphics2D g) {
        int width = this.getWidth();
        int height = this.getHeight();
        double midX = width / 2.0;
        double midY = height / 2.0;

        // 1. Dessin des piles adverses
        // Si une position est marquée comme morte, drawStack ne dessinera rien.
        drawStack(g, Math.min(STACK_MAX_SIZE, topPlayerCount), (int) midX - CARD_SIZE_MID, CARD_MARGIN, 180, "TOP");
        drawStack(g, Math.min(STACK_MAX_SIZE, leftPlayerCount), CARD_MARGIN, (int) midY - CARD_SIZE_MID, 90, "LEFT");
        drawStack(g, Math.min(STACK_MAX_SIZE, rightPlayerCount), width - CARD_SIZE - CARD_MARGIN, (int) midY - CARD_SIZE_MID, -90, "RIGHT");

        // 2. Dessin du tas joué (Défausse) au centre
        if (played > 1) {
            for (int i = 0; i < Math.min(STACK_MAX_SIZE, played) - 1; i++)
                drawCardWithAngle(g, BLANK_CARD, (int) midX - CARD_SIZE_MID, (int) midY - CARD_SIZE_MID, randomAngle());
        }
        drawCardWithAngle(g, topCard, (int) midX - CARD_SIZE_MID, (int) midY - CARD_SIZE_MID, 0);
    }

    private void drawStack(Graphics2D g, int count, int x, int y, int angle, String positionName) {
        // --- MODIFICATION ICI ---
        // Si le joueur à cette position est mort, on arrête tout de suite.
        // On ne dessine ni cartes, ni croix. L'emplacement reste vide.
        if (deadPositions.contains(positionName)) {
            return;
        }

        for (int i = 0; i < count; i++) {
            drawCardWithAngle(g, BLANK_CARD, x, y, randomAngle() + angle);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Dessin du fond d'écran
        g.drawImage(getBackgroundImage(), 0, 0, getWidth(), getHeight(), null);
        draw((Graphics2D) g);
    }

    private int randomAngle() { return random.nextInt(MAX_ANGLE * 2) - MAX_ANGLE; }

    private void drawCardWithAngle(Graphics2D g, Card card, int x, int y, int angle) {
        BufferedImage img = card.getAWTImage();
        if (img == null) return;
        AffineTransform old = g.getTransform();
        g.translate(x + CARD_SIZE_MID, y + CARD_SIZE_MID);
        g.rotate(Math.toRadians(angle));
        g.drawImage(img, -CARD_SIZE_MID, -CARD_SIZE_MID, CARD_SIZE, CARD_SIZE, null);
        g.setTransform(old);
    }

    public URL getBackgroundPath() { return getClass().getResource("images/background.png"); }

    public BufferedImage getBackgroundImage() {
        if (cachedBackgroundImage == null) {
            try { cachedBackgroundImage = ImageIO.read(getBackgroundPath()); }
            catch (IOException e) { System.err.println("Failed to load background image"); }
        }
        return cachedBackgroundImage;
    }
}