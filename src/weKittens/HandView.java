package weKittens;

import javax.swing.*;
import java.awt.*;

public class HandView extends JPanel {

    public HandView(java.util.List<Card> cards, HandAction handAction) {
        super();

        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // we don't loop on cards here because WeKittensApp.java do that
        // via clearHand() and addCardToHand()
    }

    // Need this method for resetToLobby() of WeKittensApp:
    public void removeAllCards() {
        this.removeAll();
        this.revalidate();
        this.repaint();
    }
}