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

    // Liste interne
    private final ArrayList<Card> internalCardList;

    // Verrou pour empêcher les mises à jour de statut si mort
    private boolean isDead = false;

    public WeKittensApp(ATLocalInterface at) {
        this.at = at;
        JFrame frame = new JFrame("weKittens");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLayout(new BorderLayout());
        frame.setSize(480, 900);
        frame.setResizable(false);

        // 1. HEADER
        JPanel statusPanel = new JPanel();
        statusPanel.setBackground(Color.DARK_GRAY);
        statusPanel.setPreferredSize(new Dimension(480, 40));
        statusLabel = new JLabel("Connexion...");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusPanel.add(statusLabel);
        frame.add(statusPanel, BorderLayout.NORTH);

        // 2. CENTER
        drawingView = new DrawingView();
        drawingView.setBackground(new Color(60, 60, 60));
        frame.add(drawingView, BorderLayout.CENTER);

        // 3. FOOTER
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        JPanel actionPanel = new JPanel();
        actionPanel.setBackground(new Color(50, 50, 50));
        JButton btnDraw = new JButton("Draw Card (End Turn)");
        btnDraw.addActionListener(e -> {
            if (at != null) {
                if (!at.playerDrewCard()) JOptionPane.showMessageDialog(frame, "Ce n'est pas votre tour !");
            }
        });
        actionPanel.add(btnDraw);
        bottomPanel.add(actionPanel, BorderLayout.NORTH);

        // Initialisation correcte
        internalCardList = new ArrayList<>();

        handsView = new HandView(internalCardList, this);
        handsView.setLayout(new BoxLayout(handsView, BoxLayout.X_AXIS));
        handsView.setBackground(new Color(100, 100, 100));

        JScrollPane scrollPane = new JScrollPane(handsView, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(480, 180));
        scrollPane.setBorder(null);
        bottomPanel.add(scrollPane, BorderLayout.CENTER);

        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
        initializeSession(1);
    }

    public void setTurnStatus(String message, boolean isMyTurn) {
        SwingUtilities.invokeLater(() -> {
            // Si mort, on ignore les mises à jour "Not your turn"
            if (isDead) return;

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
            try {
                internalCardList.clear();
                handsView.removeAll();
            } catch (Exception e) {}
            handsView.revalidate();
            handsView.repaint();
        });
    }

    public void addCardToHand(String typeName, String variant) {
        SwingUtilities.invokeLater(() -> {
            Card.CardType type = null;
            for (Card.CardType ct : Card.CardType.values()) {
                if (ct.name().equalsIgnoreCase(typeName)) { type = ct; break; }
            }
            if (type == null && typeName.equalsIgnoreCase("exploding")) {
                for (Card.CardType ct : Card.CardType.values()) {
                    if (ct.name().toUpperCase().contains("EXPLODING")) { type = ct; break; }
                }
            }

            if (type != null) {
                try {
                    Card card = new Card(type, variant);
                    handsView.addCard(card);
                    handsView.add(Box.createHorizontalStrut(5));
                    handsView.revalidate();
                    handsView.repaint();
                } catch (Exception e) {
                    System.err.println("GUI Error creating card: " + e.getMessage());
                }
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

    // MODIFICATION ICI : On accepte la variante en paramètre
    public void showExplosionAlert(String variant) {
        SwingUtilities.invokeLater(() -> {
            JDialog d = new JDialog((JFrame) SwingUtilities.getWindowAncestor(drawingView), "ATTENTION !", true);
            d.setLayout(new BorderLayout());

            // On utilise la variante passée (ou "a" par défaut)
            String v = (variant != null && !variant.isEmpty()) ? variant : "a";
            Card bomb = new Card(Card.CardType.exploding, v);

            ImageIcon icon = new ImageIcon(bomb.getAWTImage().getScaledInstance(300, 420, Image.SCALE_SMOOTH));
            JLabel label = new JLabel(icon);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            JLabel text = new JLabel("EXPLODING KITTEN !");
            text.setFont(new Font("Arial", Font.BOLD, 24));
            text.setForeground(Color.RED);
            text.setHorizontalAlignment(SwingConstants.CENTER);
            d.add(text, BorderLayout.NORTH);
            d.add(label, BorderLayout.CENTER);
            d.setSize(400, 550);
            d.setLocationRelativeTo(null);
            d.setVisible(true);
        });
    }

    public void showDeathMessage() {
        SwingUtilities.invokeLater(() -> {
            this.isDead = true;
            statusLabel.setText("DEAD ! Spectating");
            statusLabel.setForeground(Color.ORANGE);

            try {
                internalCardList.clear();
                handsView.removeAll();
                JLabel deadLabel = new JLabel("MODE SPECTATEUR");
                deadLabel.setForeground(Color.LIGHT_GRAY);
                deadLabel.setFont(new Font("Arial", Font.BOLD, 20));
                deadLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                handsView.add(Box.createHorizontalGlue());
                handsView.add(deadLabel);
                handsView.add(Box.createHorizontalGlue());
                handsView.revalidate();
                handsView.repaint();
            } catch (Exception e) {
                System.err.println("Error clearing hand: " + e.getMessage());
            }

            JOptionPane.showMessageDialog(null, "BOOM ! You don't have Defuse Card.\nYou Are Dead.", "GAME OVER", JOptionPane.ERROR_MESSAGE);
        });
    }

    public void markOpponentDead(int relativeIndex) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("GUI MARK DEAD: " + relativeIndex);
            if (relativeIndex == 1) drawingView.markDead("LEFT");
            else if (relativeIndex == 2) drawingView.markDead("TOP");
            else if (relativeIndex == 3) drawingView.markDead("RIGHT");
            drawingView.repaint();
        });
    }

    public void showWinMessage() {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("VICTOIRE !!!");
            statusLabel.setForeground(Color.GREEN);

            JOptionPane.showMessageDialog(null,
                    "FELICITATIONS !\nVous êtes le dernier survivant.\nVOUS AVEZ GAGNE !",
                    "VICTOIRE",
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }

    public int askInsertionIndex(int deckSize) {
        final int[] result = new int[1];
        try {
            SwingUtilities.invokeAndWait(() -> {
                String input = JOptionPane.showInputDialog(null, "BOMB DEFUSE ! Where? (0-" + deckSize + ")", "0");
                try {
                    result[0] = Integer.parseInt(input);
                    if (result[0] < 0) result[0] = 0;
                    if (result[0] > deckSize) result[0] = deckSize;
                } catch (Exception e) { result[0] = 0; }
            });
        } catch (Exception e) { return 0; }
        return result[0];
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