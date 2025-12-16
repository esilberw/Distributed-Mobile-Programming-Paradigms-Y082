package weKittens;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class Card {

    public static enum CardType {
        back, //just the back side
        exploding,
        nope,
        defuse,
        attack,
        shuffle,
        favor,
        skip,
        future,
        cat
    }

    private BufferedImage cachedImage;
    private final CardType type;
    private final String variant;

    public Card(CardType type, String variant) {
        this.type = type;
        this.variant = variant;
    }

    public Card(Card c) {
        this.type = c.getType();
        this.variant = c.getVariant();
    }

    public URL getResourcePath() {
        String path = "images/back_card.png";

        if (type != CardType.back)
            path = "images/" + this.toString() + ".png";

        return getClass().getResource(path);
    }


    public BufferedImage getAWTImage() {
        if (cachedImage == null) {
            try {
                cachedImage = ImageIO.read(getResourcePath());
            } catch (IOException e) {
                System.err.println("Failed to load image: " + getResourcePath());
            }
        }
        return cachedImage;
    }

    public CardComponent getComponent() {
        return new CardComponent(this);
    }


    @Override
    public String toString() {
        return type.toString() + "_" + variant;
    }

    public CardType getType() {
        return this.type;
    }
    public String getVariant() {
        return this.variant;
    }

    public static class CardComponent extends JComponent {

        private final Card card;

        public CardComponent(Card card) {
            this.card = card;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            BufferedImage img = card.getAWTImage();
            if (img == null) return;

            g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
        }
    }
}
