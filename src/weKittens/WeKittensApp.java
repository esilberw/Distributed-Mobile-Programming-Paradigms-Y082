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

    private final ArrayList<Card> internalCardList;

    private boolean isDead = false;
    private boolean isVictory = false;

    // Stockage du nombre de joueurs
    private int currentTotalPlayers = 2;

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
                if (!at.playerDrewCard()) JOptionPane.showMessageDialog(frame, "Not your turn, please wait !");
            }
        });
        actionPanel.add(btnDraw);
        bottomPanel.add(actionPanel, BorderLayout.NORTH);

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
            if (isDead || isVictory) return;
            if (statusLabel != null) {
                statusLabel.setText(message);
                statusLabel.setForeground(isMyTurn ? Color.GREEN : Color.RED);
            }
        });
    }

    public void initializeSession(int totalPlayers) {
        SwingUtilities.invokeLater(() -> {
            this.currentTotalPlayers = totalPlayers;

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
            // Nettoyage préventif
            String cleanType = typeName.replace("\"", "");
            String cleanVar = variant.replace("\"", "");

            for (Card.CardType ct : Card.CardType.values()) {
                if (ct.name().equalsIgnoreCase(cleanType)) { type = ct; break; }
            }
            if (type == null && cleanType.equalsIgnoreCase("exploding")) {
                for (Card.CardType ct : Card.CardType.values()) {
                    if (ct.name().toUpperCase().contains("EXPLODING")) { type = ct; break; }
                }
            }

            if (type != null) {
                try {
                    Card card = new Card(type, cleanVar);
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
            String cleanType = typeName.replace("\"", "");
            String cleanVar = variant.replace("\"", "");

            for (Card.CardType ct : Card.CardType.values()) {
                if (ct.name().equalsIgnoreCase(cleanType)) {
                    drawingView.playCard(new Card(ct, cleanVar));
                    drawingView.repaint();
                    return;
                }
            }
        });
    }

    public void showExplosionAlert(String variant) {
        SwingUtilities.invokeLater(() -> {
            JDialog d = new JDialog((JFrame) SwingUtilities.getWindowAncestor(drawingView), "ATTENTION !", true);
            d.setLayout(new BorderLayout());

            String v = (variant != null && !variant.isEmpty()) ? variant.replace("\"", "") : "a";
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

    // --- CORRECTION SEE THE FUTURE ---
    public void showSeeTheFuture(ArrayList types, ArrayList variants) {
        SwingUtilities.invokeLater(() -> {
            JDialog d = new JDialog((JFrame) SwingUtilities.getWindowAncestor(drawingView), "SEE THE FUTURE (Top 3)", true);
            d.setLayout(new GridLayout(1, 3, 10, 10));
            d.getContentPane().setBackground(new Color(50, 50, 50));

            for (int i = 0; i < types.size(); i++) {
                Object tObj = types.get(i);
                Object vObj = variants.get(i);

                String tStr = (tObj != null) ? tObj.toString() : "cat";
                String vStr = (vObj != null) ? vObj.toString() : "";

                // NETTOYAGE DES GUILLEMETS (Vital pour ImageIO)
                tStr = tStr.replace("\"", "");
                vStr = vStr.replace("\"", "");

                // Conversion sécurisée
                Card.CardType typeEnum = Card.CardType.cat;
                try {
                    typeEnum = Card.CardType.valueOf(tStr);
                } catch (Exception e) {
                    for (Card.CardType ct : Card.CardType.values()) {
                        if (ct.name().equalsIgnoreCase(tStr)) { typeEnum = ct; break; }
                    }
                }

                try {
                    Card c = new Card(typeEnum, vStr);
                    // Image mise à l'échelle
                    Image rawImg = c.getAWTImage();
                    if (rawImg != null) {
                        ImageIcon icon = new ImageIcon(rawImg.getScaledInstance(200, 280, Image.SCALE_SMOOTH));
                        JLabel lbl = new JLabel(icon);

                        lbl.setBorder(BorderFactory.createTitledBorder(
                                BorderFactory.createLineBorder(Color.WHITE),
                                "Card " + (i + 1),
                                javax.swing.border.TitledBorder.CENTER,
                                javax.swing.border.TitledBorder.TOP,
                                new Font("Arial", Font.BOLD, 12),
                                Color.WHITE
                        ));
                        d.add(lbl);
                    }
                } catch (Exception e) {
                    System.err.println("Error showing Future card: " + e.getMessage());
                }
            }

            d.setSize(700, 350);
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
                JLabel deadLabel = new JLabel("SPECTATOR MOD");
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

            if (relativeIndex == 1) {
                drawingView.markDead("LEFT");
                drawingView.setLeftPlayerCount(0);
            }
            else if (relativeIndex == 2) {
                drawingView.markDead("TOP");
                drawingView.setTopPlayerCount(0);
            }
            else if (relativeIndex == 3) {
                drawingView.markDead("RIGHT");
                drawingView.setRightPlayerCount(0);
            }

            drawingView.repaint();
        });
    }

    public void showWinMessage() {
        SwingUtilities.invokeLater(() -> {
            if (isDead) return;
            isVictory = true;
            statusLabel.setText("VICTORY !");
            statusLabel.setForeground(Color.GREEN);
            JOptionPane.showMessageDialog(null,
                    "BRAVO !\nYou are the last survivor.\nYou WIN !",
                    "VICTORY",
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

    public String askCardToGive() {
        if (internalCardList.isEmpty()) return null;
        String[] cardNames = new String[internalCardList.size()];
        for (int i = 0; i < internalCardList.size(); i++) {
            Card c = internalCardList.get(i);
            cardNames[i] = c.getType().toString() + " (" + c.getVariant() + ")";
        }
        final String[] selection = new String[1];
        try {
            SwingUtilities.invokeAndWait(() -> {
                selection[0] = (String) JOptionPane.showInputDialog(
                        null,
                        "FAVOR is played against you !\nYou need to give a card.\nSelect one from your hand:",
                        "FAVOR - Give a card",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        cardNames,
                        cardNames[0]
                );
            });
        } catch (Exception e) { return null; }
        return selection[0];
    }

    private int askTargetPlayerInternal() {
        int opponents = currentTotalPlayers - 1;
        ArrayList<String> options = new ArrayList<>();
        ArrayList<Integer> values = new ArrayList<>();

        if (opponents == 1) {
            options.add("opponent (TOP)");
            values.add(2);
        } else {
            if (opponents >= 1) { options.add("LEFT Player"); values.add(1); }
            if (opponents >= 2) { options.add("TOP Player");   values.add(2); }
            if (opponents >= 3) { options.add("Right Player"); values.add(3); }
        }

        int choice = JOptionPane.showOptionDialog(null,
                "FAVORITE TARGET: Who do you want to plunder?",
                "Victim choice",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options.toArray(),
                options.get(0));

        if (choice >= 0 && choice < values.size()) {
            return values.get(choice);
        }
        return -1;
    }

    @Override
    public boolean cardPlayed(Card card) {
        int targetInfo = -1;

        if (card.getType() == Card.CardType.favor) {
            targetInfo = askTargetPlayerInternal();
            if (targetInfo == -1) return false;
        }

        if (at.cardPlayed(card, targetInfo)) {
            drawingView.playCard(card);
            drawingView.repaint();
            return true;
        }
        return false;
    }
}