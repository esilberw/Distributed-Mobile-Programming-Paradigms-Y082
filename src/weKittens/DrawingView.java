package weKittens;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Random;

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

    public void setTopPlayerCount(int topPlayerCount) { this.topPlayerCount = topPlayerCount; }
    public void setLeftPlayerCount(int leftPlayerCount) { this.leftPlayerCount = leftPlayerCount; }
    public void setRightPlayerCount(int rightPlayerCount) { this.rightPlayerCount = rightPlayerCount; }

    public void draw(Graphics2D g) {
        int width = this.getWidth();
        int height = this.getHeight();

        double midX = width / 2.0;
        double midY = height / 2.0;

        // Clear
        //g.setColor(Color.WHITE);
        //g.fillRect(0, 0, width, height);

        // Draw top, left, right player's card stacks
        for (int i = 0; i < Math.min(STACK_MAX_SIZE, topPlayerCount); i++)
            drawCardWithAngle(g, BLANK_CARD, (int) midX - CARD_SIZE_MID, CARD_MARGIN, randomAngle() + 180);

        for (int i = 0; i < Math.min(STACK_MAX_SIZE, leftPlayerCount); i++)
            drawCardWithAngle(g, BLANK_CARD, CARD_MARGIN, (int) midY - CARD_SIZE_MID, randomAngle() + 90);

        for (int i = 0; i < Math.min(STACK_MAX_SIZE, rightPlayerCount); i++)
            drawCardWithAngle(g, BLANK_CARD, width - CARD_SIZE - CARD_MARGIN, (int) midY - CARD_SIZE_MID, randomAngle() - 90);

        // Draw played pile
        if (played > 1) {
            for (int i = 0; i < Math.min(STACK_MAX_SIZE, played) - 1; i++)
                drawCardWithAngle(g, BLANK_CARD, (int) midX - CARD_SIZE_MID, (int) midY - CARD_SIZE_MID, randomAngle());
        }

        // Draw the actual top card
        drawCardWithAngle(g, topCard, (int) midX - CARD_SIZE_MID, (int) midY - CARD_SIZE_MID, 0);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(getBackgroundImage(), 0, 0, getWidth(), getHeight(), null);
        draw((Graphics2D) g);
    }

    private int randomAngle() {
        return random.nextInt(MAX_ANGLE * 2) - MAX_ANGLE;
    }

    private void drawCardWithAngle(Graphics2D g, Card card, int x, int y, int angle) {
        BufferedImage img = card.getAWTImage();
        if (img == null) return;

        AffineTransform old = g.getTransform();

        g.translate(x + CARD_SIZE_MID, y + CARD_SIZE_MID);
        g.rotate(Math.toRadians(angle));
        g.drawImage(img, -CARD_SIZE_MID, -CARD_SIZE_MID, CARD_SIZE, CARD_SIZE, null);

        g.setTransform(old);
    }

    public URL getBackgroundPath() {
        String path = "images/background.png";
        return getClass().getResource(path);
    }


    public BufferedImage getBackgroundImage() {
        if (cachedBackgroundImage == null) {
            try {
                cachedBackgroundImage = ImageIO.read(getBackgroundPath());
            } catch (IOException e) {
                System.err.println("Failed to load background image");
            }
        }
        return cachedBackgroundImage;
    }
}
