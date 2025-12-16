package weKittens;

import weKittens.interfaces.ATLocalInterface;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;

public class WeKittensApp implements HandAction {
    private final ATLocalInterface at;
    private final DrawingView drawingView;
    private final HandView handsView;
    private Deck cardDeck;

    public WeKittensApp(ATLocalInterface at) {
        this.at = at;
        JFrame frame = new JFrame("weKittens");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.setSize(480, 900);

        drawingView = new DrawingView();
        drawingView.setPreferredSize(new Dimension(480, 640));
        frame.add(drawingView);

        ArrayList<Card> cards = new ArrayList<>();
        handsView = new HandView(cards, this);
        handsView.setLayout(new BoxLayout(handsView, BoxLayout.X_AXIS));

        JScrollPane scrollPane = new JScrollPane(
                handsView,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        scrollPane.setPreferredSize(new Dimension(480, 120));
        scrollPane.setBorder(null);
        frame.add(scrollPane);

        frame.pack();
        frame.setVisible(true);

        setupGame();
    }

    private void setupGame() {
        drawingView.setLeftPlayerCount(3);
        drawingView.setTopPlayerCount(3);
        drawingView.setRightPlayerCount(3);

        int playerCount = 4;
        cardDeck = new Deck(playerCount);

        handsView.addCard(cardDeck.takeCard(Card.CardType.defuse));

        for (int i=0; i<playerCount-1; i++)
            cardDeck.takeCard(Card.CardType.defuse);

        drawCards(7);
        cardDeck.addExplodingKittens();

        drawingView.repaint();
    }

    public void drawCards(int n) {
        for (int i = 0; i < n; i++)
            handsView.addCard(cardDeck.drawCard());
    }

    @Override
    public boolean cardPlayed(Card card) {

        // Ask AT to know if legal
        boolean ok = at.cardPlayed(card);

        // if not legal -> we refuse
        if (!ok) return false; // HandView remove the card only if cardPlayer return true (valid).

        drawingView.playCard(card);
        drawingView.repaint();
        return true;
    }
}
