package weKittens;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class HandView extends JPanel {
    private final HandAction handAction;
    private final List<Card> mCards;

    public HandView(List<Card> cards, HandAction handAction) {
        super();
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        this.handAction = handAction;
        this.mCards = cards;

        for (Card card : cards) {
            addCardToHand(card);
        }
    }

    public void addCard(Card card) {
        mCards.add(card);
        addCardToHand(card);
    }

    private void addCardToHand(Card card) {
        Card.CardComponent cardComp = card.getComponent();
        var size = new Dimension(100, 100);
        cardComp.setPreferredSize(size);
        cardComp.setMaximumSize(size);
        cardComp.setMinimumSize(size);
        cardComp.setCursor(new Cursor(Cursor.HAND_CURSOR));

        cardComp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                System.out.println("Clicked card " + card);

                // Ask controller whether move is valid
                if (handAction.cardPlayed(card)) {
                    remove(cardComp);
                    refreshHandPanel();
                }
            }
        });

        add(cardComp);
        refreshHandPanel();
    }

    private void refreshHandPanel() {
        this.revalidate();
        this.repaint();
    }

    public void removeAllCards() {
        this.removeAll(); // Retire les composants graphiques (JPanel)
        mCards.clear();   // Vide la liste mémoire
        this.revalidate();
        this.repaint();
    }
}
