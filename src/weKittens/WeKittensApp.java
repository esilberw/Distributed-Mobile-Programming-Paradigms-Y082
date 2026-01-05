package weKittens;

import weKittens.interfaces.ATLocalInterface;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.*;

public class WeKittensApp implements HandAction {
    private final ATLocalInterface at;

    // MODIFICATION 1 : frame et drawingView ne sont plus 'final' pour pouvoir être réinitialisés
    private JFrame frame;
    private DrawingView drawingView;

    private final HandView handsView;
    private final JLabel statusLabel;

    // Liste réelle de la main
    private final ArrayList<Card> internalCardList;

    // Liste des cartes actuellement sélectionnées
    private final ArrayList<Card> selectedCards = new ArrayList<>();

    // Stockage des index relatifs des morts
    private final java.util.HashSet<Integer> deadOpponents = new java.util.HashSet<>();

    private boolean isDead = false;
    private boolean isVictory = false;
    private int currentTotalPlayers = 2;

    public WeKittensApp(ATLocalInterface at) {
        this.at = at;

        // MODIFICATION 2 : On utilise le champ de classe 'frame' (pas de 'JFrame frame = ...')
        frame = new JFrame("weKittens");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLayout(new BorderLayout());
        frame.setSize(480, 900);
        frame.setResizable(false);

        // 1. HEADER
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(Color.DARK_GRAY);
        statusPanel.setPreferredSize(new Dimension(480, 40));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        statusLabel = new JLabel("Connexion...", SwingConstants.CENTER);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));

        // Le bouton Quitter (Rouge)
        JButton btnLeave = new JButton("X");
        btnLeave.setBackground(Color.RED);
        btnLeave.setForeground(Color.WHITE);
        btnLeave.setFocusPainted(false);
        btnLeave.setFont(new Font("Arial", Font.BOLD, 12));
        btnLeave.setPreferredSize(new Dimension(45, 30));
        btnLeave.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(frame, "Leave session?", "Quit", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                if (at != null) {
                    if (isDead) at.leaveSpectatingMode();
                    else at.leaveSession();
                }
            }
        });

        statusPanel.add(statusLabel, BorderLayout.CENTER);
        statusPanel.add(btnLeave, BorderLayout.EAST);

        frame.add(statusPanel, BorderLayout.NORTH);

        // 2. CENTER (Table de jeu)
        drawingView = new DrawingView();
        drawingView.setBackground(new Color(60, 60, 60));
        frame.add(drawingView, BorderLayout.CENTER);

        // 3. FOOTER (Boutons + Main du joueur)
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        JPanel actionPanel = new JPanel();
        actionPanel.setBackground(new Color(50, 50, 50));

        // BOUTON PIOCHE
        JButton btnDraw = new JButton("Draw Card (End Turn)");
        btnDraw.addActionListener(e -> {
            if (at != null) {
                clearSelection();
                if (!at.playerDrewCard()) JOptionPane.showMessageDialog(frame, "Not your turn (or action blocked)!");
            }
        });

        // BOUTON JOUER LA SELECTION
        JButton btnPlay = new JButton("PLAY SELECTED");
        btnPlay.setBackground(new Color(46, 204, 113)); // Vert
        btnPlay.setForeground(Color.WHITE);
        btnPlay.addActionListener(e -> executePlaySelected());

        actionPanel.add(btnPlay);
        actionPanel.add(btnDraw);
        bottomPanel.add(actionPanel, BorderLayout.NORTH);

        internalCardList = new ArrayList<>();

        handsView = new HandView(internalCardList, this);
        handsView.setLayout(new BoxLayout(handsView, BoxLayout.X_AXIS));
        handsView.setBackground(new Color(100, 100, 100));
        handsView.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(handsView, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(480, 180));
        scrollPane.setBorder(null);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        bottomPanel.add(scrollPane, BorderLayout.CENTER);

        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
        initializeSession(1);
    }

    public void resetToLobby() {
        SwingUtilities.invokeLater(() -> {
            // 1. Remettre le texte du haut
            statusLabel.setText("Connexion...");
            statusLabel.setForeground(Color.WHITE);

            // 2. REMPLACEMENT RADICAL DU TAPIS DE JEU
            // On retire l'ancienne vue (qui contient la défausse)
            frame.remove(drawingView);

            // On en crée une toute neuve (vide)
            drawingView = new DrawingView();
            drawingView.setBackground(new Color(60, 60, 60));
            frame.add(drawingView, BorderLayout.CENTER);

            // 3. Vider la main et la sélection
            clearHand();
            selectedCards.clear();

            // 4. Réinitialiser les états internes
            isDead = false;
            isVictory = false;
            deadOpponents.clear();

            // 5. IMPORTANT : On remet le compteur d'adversaires à ZERO
            // Cela efface les 3 joueurs que vous voyiez sur l'image
            initializeSession(1);

            // 5. Force le rafraîchissement global de la fenêtre
            frame.revalidate();
            frame.repaint();
        });
    }

    // --- LOGIQUE DE SELECTION ---

    private void toggleSelection(Card card, JComponent cardComponent) {
        if (selectedCards.contains(card)) {
            selectedCards.remove(card);
            cardComponent.setBorder(null);
        } else {
            selectedCards.add(card);
            cardComponent.setBorder(BorderFactory.createLineBorder(Color.GREEN, 4));
        }
        checkAuto5Combo();
    }

    private void clearSelection() {
        selectedCards.clear();
        handsView.revalidate();
        handsView.repaint();
        SwingUtilities.invokeLater(() -> {
            for(Component c : handsView.getComponents()) {
                if(c instanceof JComponent) ((JComponent)c).setBorder(null);
            }
        });
    }

    // --- LOGIQUE DE JEU ---

    private int countUniqueCards(ArrayList<Card> cards) {
        java.util.HashSet<String> uniqueKeys = new java.util.HashSet<>();
        for (Card c : cards) {
            if (c.getType() == Card.CardType.cat) {
                uniqueKeys.add(c.getType().name() + "_" + c.getVariant());
            } else {
                uniqueKeys.add(c.getType().name());
            }
        }
        return uniqueKeys.size();
    }

    private void checkAuto5Combo() {
        if (countUniqueCards(selectedCards) >= 5) {
            int choice = JOptionPane.showConfirmDialog(null,
                    "COMBO 5 DETECTED (5 Different Cards)!\nDo you want to play the SPECIAL 5 COMBO?",
                    "Ultimate Combo", JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                if (at.special5ComboPlayed()) {
                    removeSelectedCardsFromHand();
                }
            }
        }
    }

    private void executePlaySelected() {
        int count = selectedCards.size();
        if (count == 0) {
            JOptionPane.showMessageDialog(null, "Select cards first!");
            return;
        }

        boolean allSame = true;
        Card first = selectedCards.get(0);
        for (Card c : selectedCards) {
            if (c.getType() != first.getType() || !c.getVariant().equals(first.getVariant())) {
                allSame = false;
                break;
            }
        }

        if (count >= 5) {
            if (countUniqueCards(selectedCards) >= 5) {
                if (at.special5ComboPlayed()) {
                    removeSelectedCardsFromHand();
                    return;
                }
            } else {
                JOptionPane.showMessageDialog(null, "Invalid 5-Card Combo.\nYou need 5 DIFFERENT cards.", "Invalid Combo", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (count == 3 && allSame) {
            int choice = JOptionPane.showConfirmDialog(null, "Play TRIPLE to request a card?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                int targetRel = askTargetPlayerInternal();
                if (targetRel != -1) {
                    String reqType = askDesiredType();
                    if (reqType != null) {
                        String reqVar = askDesiredVariant(reqType);
                        if (!reqVar.isEmpty()) {
                            if (at.triplePlayed(first, targetRel, reqType, reqVar)) {
                                drawingView.playCard(first);
                                drawingView.repaint();
                                removeSelectedCardsFromHand();
                            }
                        }
                    }
                }
            }
            return;
        }

        if (count == 2 && allSame) {
            int choice = JOptionPane.showConfirmDialog(null, "Play PAIR to steal a random card?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                int targetRel = askTargetPlayerInternal();
                if (targetRel != -1) {
                    int realHandSize = at.getOpponentHandSize(targetRel);
                    int stealIdx = askStealIndex(realHandSize);
                    if (at.pairPlayed(first, targetRel, stealIdx)) {
                        drawingView.playCard(first);
                        drawingView.repaint();
                        removeSelectedCardsFromHand();
                    }
                }
            }
            return;
        }

        if (count == 1) {
            if (first.getType() == Card.CardType.nope) {
                if (at.nopePlayed(first)) {
                    drawingView.playCard(first);
                    drawingView.repaint();
                    removeSelectedCardsFromHand();
                } else {
                    JOptionPane.showMessageDialog(null, "Cannot play Nope (Are you dead?)", "Error", JOptionPane.ERROR_MESSAGE);
                }
                return;
            }

            if (first.getType() == Card.CardType.cat) {
                JOptionPane.showMessageDialog(null, "Cat cards cannot be played alone!", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int targetInfo = -1;
            if (first.getType() == Card.CardType.favor) {
                targetInfo = askTargetPlayerInternal();
                if (targetInfo == -1) return;
            }

            if (at.cardPlayed(first, targetInfo)) {
                drawingView.playCard(first);
                drawingView.repaint();
                removeSelectedCardsFromHand();
            }
            return;
        }

        JOptionPane.showMessageDialog(null, "Invalid selection.", "Invalid Move", JOptionPane.ERROR_MESSAGE);
    }

    private void removeSelectedCardsFromHand() {
        SwingUtilities.invokeLater(this::clearSelection);
    }

    // --- UI HELPERS ---

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
                selectedCards.clear();
                handsView.removeAll();
            } catch (Exception e) {}
            handsView.revalidate();
            handsView.repaint();
        });
    }

    public void addCardToHand(String typeName, String variant) {
        SwingUtilities.invokeLater(() -> {
            Card.CardType type = null;
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
                    internalCardList.add(card);

                    ImageIcon icon = new ImageIcon(card.getAWTImage().getScaledInstance(100, 140, Image.SCALE_SMOOTH));
                    JLabel cardLabel = new JLabel(icon);

                    Dimension dim = new Dimension(100, 140);
                    cardLabel.setPreferredSize(dim);
                    cardLabel.setMaximumSize(dim);
                    cardLabel.setMinimumSize(dim);
                    cardLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

                    cardLabel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            toggleSelection(card, cardLabel);
                        }
                    });

                    handsView.add(cardLabel);
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

    // --- POPUPS & MESSAGES ---

    public void showExplosionAlert(String variant) {
        SwingUtilities.invokeLater(() -> {
            JDialog d = new JDialog(frame, "ATTENTION !", true);
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

    public void showSeeTheFuture(ArrayList types, ArrayList variants) {
        SwingUtilities.invokeLater(() -> {
            JDialog d = new JDialog(frame, "SEE THE FUTURE (Top 3)", true);
            d.setLayout(new GridLayout(1, 3, 10, 10));
            d.getContentPane().setBackground(new Color(50, 50, 50));

            for (int i = 0; i < types.size(); i++) {
                Object tObj = types.get(i);
                Object vObj = variants.get(i);
                String tStr = (tObj != null) ? tObj.toString().replace("\"", "") : "cat";
                String vStr = (vObj != null) ? vObj.toString().replace("\"", "") : "";

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
                    Image rawImg = c.getAWTImage();
                    if (rawImg != null) {
                        ImageIcon icon = new ImageIcon(rawImg.getScaledInstance(200, 280, Image.SCALE_SMOOTH));
                        JLabel lbl = new JLabel(icon);
                        lbl.setBorder(BorderFactory.createLineBorder(Color.WHITE));
                        d.add(lbl);
                    }
                } catch (Exception e) {}
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
                handsView.add(Box.createHorizontalGlue());
                handsView.add(deadLabel);
                handsView.add(Box.createHorizontalGlue());
                handsView.revalidate();
                handsView.repaint();
            } catch (Exception e) {}
            JOptionPane.showMessageDialog(null, "BOOM ! You don't have Defuse Card.\nYou Are Dead.", "GAME OVER", JOptionPane.ERROR_MESSAGE);
        });
    }

    public void markOpponentDead(int relativeIndex) {
        SwingUtilities.invokeLater(() -> {
            deadOpponents.add(relativeIndex);
            if (relativeIndex == 1) { drawingView.markDead("LEFT"); drawingView.setLeftPlayerCount(0); }
            else if (relativeIndex == 2) { drawingView.markDead("TOP"); drawingView.setTopPlayerCount(0); }
            else if (relativeIndex == 3) { drawingView.markDead("RIGHT"); drawingView.setRightPlayerCount(0); }
            drawingView.repaint();
        });
    }

    public void showWinMessage() {
        SwingUtilities.invokeLater(() -> {
            if (isDead) return;
            isVictory = true;
            statusLabel.setText("VICTORY !");
            statusLabel.setForeground(Color.GREEN);
            JOptionPane.showMessageDialog(null, "BRAVO !\nYou are the last survivor.\nYou WIN !", "VICTORY", JOptionPane.INFORMATION_MESSAGE);
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
                selection[0] = (String) JOptionPane.showInputDialog(null, "FAVOR against you! Give a card:", "FAVOR", JOptionPane.QUESTION_MESSAGE, null, cardNames, cardNames[0]);
            });
        } catch (Exception e) { return null; }
        return selection[0];
    }

    private int askTargetPlayerInternal() {
        int opponents = currentTotalPlayers - 1;
        ArrayList<String> options = new ArrayList<>();
        ArrayList<Integer> values = new ArrayList<>();

        if (opponents == 1) {
            if (!deadOpponents.contains(2)) {
                options.add("opponent (TOP)");
                values.add(2);
            }
        } else {
            if (opponents >= 1 && !deadOpponents.contains(1)) { options.add("LEFT Player"); values.add(1); }
            if (opponents >= 2 && !deadOpponents.contains(2)) { options.add("TOP Player");   values.add(2); }
            if (opponents >= 3 && !deadOpponents.contains(3)) { options.add("Right Player"); values.add(3); }
        }

        if (options.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No valid targets available!");
            return -1;
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

    private String askDesiredType() {
        Card.CardType[] types = Card.CardType.values();
        String[] typeNames = new String[types.length];
        for(int i=0; i<types.length; i++) typeNames[i] = types[i].name();
        return (String) JOptionPane.showInputDialog(null, "TRIPLE COMBO: Name a specific card TYPE!", "Name The Card", JOptionPane.QUESTION_MESSAGE, null, typeNames, "defuse");
    }

    private String askDesiredVariant(String selectedType) {
        String typeLower = (selectedType != null) ? selectedType.toLowerCase() : "";
        String[] options;
        switch (typeLower) {
            case "attack": options = new String[]{"mine", "space"}; break;
            case "cat": options = new String[]{"beard", "cattermelon", "potato", "rainbow", "taco"}; break;
            case "defuse": options = new String[]{"banjo", "catnip", "laser"}; break;
            case "favor": options = new String[]{"card"}; break;
            case "future": options = new String[]{"bear", "goggles", "mantis", "pig", "pigacorn"}; break;
            case "nope": options = new String[]{"card"}; break;
            case "shuffle": options = new String[]{"litterbox", "scratch"}; break;
            case "skip": options = new String[]{"bunnyraptor", "nap", "sprint"}; break;
            case "exploding": options = new String[]{"a", "b"}; break;
            default: options = new String[]{"default"}; break;
        }
        String result = (String) JOptionPane.showInputDialog(null, "TRIPLE COMBO: Select VARIANT for " + selectedType, "Select Variant", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        return (result != null) ? result : "";
    }

    public void askPickFromDiscard(ArrayList types, ArrayList variants) {
        SwingUtilities.invokeLater(() -> {
            if (types.isEmpty()) { JOptionPane.showMessageDialog(null, "Discard pile is empty!"); return; }
            String[] options = new String[types.size()];
            for (int i = 0; i < types.size(); i++) {
                String t = types.get(i).toString().replace("\"", "");
                String v = variants.get(i).toString().replace("\"", "");
                options[i] = t + " (" + v + ")";
            }
            String selected = (String) JOptionPane.showInputDialog(null, "5 COMBO! Pick from Discard:", "Graveyard", JOptionPane.QUESTION_MESSAGE, null, options, options[options.length - 1]);
            if (selected != null) {
                int index = -1;
                for(int i=0; i<options.length; i++) { if (options[i].equals(selected)) { index = i; break; } }
                if (index != -1) {
                    String t = types.get(index).toString().replace("\"", "");
                    String v = variants.get(index).toString().replace("\"", "");
                    at.pickFromDiscard(t, v);
                }
            }
        });
    }

    private int askStealIndex(int maxCards) {
        if (maxCards < 1) maxCards = 1;
        int result = 1;
        String input = JOptionPane.showInputDialog(null, "PAIR COMBO: Steal random! Enter (1-" + maxCards + ")", "1");
        try {
            result = Integer.parseInt(input);
            if (result < 1) result = 1;
            if (result > maxCards) result = maxCards;
        } catch (Exception e) { result = 1; }
        return result;
    }

    @Override
    public boolean cardPlayed(Card card) {
        return false;
    }
}