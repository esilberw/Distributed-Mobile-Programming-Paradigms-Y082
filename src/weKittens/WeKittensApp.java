package weKittens;

import weKittens.interfaces.ATLocalInterface;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;

public class WeKittensApp implements HandAction {
    private final ATLocalInterface at;
    private final DrawingView drawingView;
    private final HandView handsView;
    private final JLabel statusLabel;

    public WeKittensApp(ATLocalInterface at) {
        this.at = at;
        JFrame frame = new JFrame("weKittens");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // CORRECTION LAYOUT : BorderLayout empêche l'écrasement
        frame.setLayout(new BorderLayout());
        frame.setSize(480, 900);
        frame.setResizable(false);

        // 1. HEADER (Nord)
        JPanel statusPanel = new JPanel();
        statusPanel.setBackground(Color.DARK_GRAY);
        statusPanel.setPreferredSize(new Dimension(480, 40));
        statusLabel = new JLabel("Connexion ...");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusPanel.add(statusLabel);
        frame.add(statusPanel, BorderLayout.NORTH);

        // 2. CONTENU (Centre)
        drawingView = new DrawingView();
        drawingView.setBackground(new Color(60, 60, 60));
        frame.add(drawingView, BorderLayout.CENTER);

        // 3. FOOTER (Sud - Bouton + Main)
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        JPanel actionPanel = new JPanel();
        actionPanel.setBackground(new Color(50, 50, 50));
        JButton btnDraw = new JButton("Draw Card (End Turn)");
        btnDraw.addActionListener(e -> {
            if (at != null) {
                if (!at.playerDrewCard()) JOptionPane.showMessageDialog(frame, "It is not your turn !");
            }
        });
        actionPanel.add(btnDraw);
        bottomPanel.add(actionPanel, BorderLayout.NORTH);

        // Initialisation de la main
        ArrayList<Card> cards = new ArrayList<>();
        handsView = new HandView(cards, this);
        // On remet le BoxLayout horizontal pour les cartes
        handsView.setLayout(new BoxLayout(handsView, BoxLayout.X_AXIS));
        handsView.setBackground(new Color(100, 100, 100));

        JScrollPane scrollPane = new JScrollPane(handsView, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(480, 180)); // Hauteur garantie
        scrollPane.setBorder(null);
        bottomPanel.add(scrollPane, BorderLayout.CENTER);

        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
        initializeSession(1);
    }

    public void setTurnStatus(String message, boolean isMyTurn) {
        SwingUtilities.invokeLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText(message);
                statusLabel.setForeground(isMyTurn ? Color.GREEN : Color.RED);
            }
        });
    }

    public void initializeSession(int totalPlayers) {
        SwingUtilities.invokeLater(() -> {
            drawingView.setLeftPlayerCount(0);
            drawingView.setTopPlayerCount(0);
            drawingView.setRightPlayerCount(0);
            int opponents = totalPlayers - 1;
            if (opponents >= 1) drawingView.setTopPlayerCount(3);
            if (opponents >= 2) drawingView.setLeftPlayerCount(3);
            if (opponents >= 3) drawingView.setRightPlayerCount(3);
            drawingView.repaint();
        });
    }

    public void clearHand() {
        SwingUtilities.invokeLater(() -> {
            try { handsView.removeAll(); } catch (Exception e) {}
            handsView.revalidate();
            handsView.repaint();
        });
    }

    public void addCardToHand(String typeName, String variant) {
        SwingUtilities.invokeLater(() -> {
            Card.CardType type = null;
            // Recherche insensible à la casse
            for (Card.CardType ct : Card.CardType.values()) {
                if (ct.name().equalsIgnoreCase(typeName)) { type = ct; break; }
            }
            // Fallback pour exploding
            if (type == null && typeName.equalsIgnoreCase("exploding")) {
                for (Card.CardType ct : Card.CardType.values()) {
                    if (ct.name().toUpperCase().contains("EXPLODING")) { type = ct; break; }
                }
            }

            if (type != null) {
                try {
                    Card card = new Card(type, variant);
                    handsView.addCard(card);
                    // Espaceur pour éviter que les cartes ne se collent trop
                    handsView.add(Box.createHorizontalStrut(5));
                    handsView.revalidate();
                    handsView.repaint();
                } catch (Exception e) {
                    System.err.println("GUI Error creating card: " + e.getMessage());
                }
            } else {
                System.err.println("GUI ERROR: Enum not found for '" + typeName + "'");
            }
        });
    }

    public void playCardOpponent(String typeName, String variant) {
        SwingUtilities.invokeLater(() -> {
            for (Card.CardType ct : Card.CardType.values()) {
                if (ct.name().equalsIgnoreCase(typeName)) {
                    drawingView.playCard(new Card(ct, variant));
                    drawingView.repaint();
                    return;
                }
            }
        });
    }

    @Override
    public boolean cardPlayed(Card card) {
        if (at.cardPlayed(card)) {
            drawingView.playCard(card);
            drawingView.repaint();
            return true;
        }
        return false;
    }
}