package weKittens;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

public class Deck {

    private LinkedList<Card> cards;
    private final int playerCount;

    public Deck(int players) {
        this.playerCount = players;

        makeSharingDeck();
    }

    public Card peekTopCard() {
        final Iterator<Card> itr = cards.iterator();
        Card lastElement = itr.next();
        while(itr.hasNext()) {
            lastElement = itr.next();
        }
        return lastElement;
    }

    public Card drawCard() {
        return cards.poll();
    }

    public Card takeCard(Card.CardType type) {
        // assumes the card exists...
        int pos = 0;
        Card card;

        while ((card = cards.get(pos)).getType() != type) {
            pos++;
        }
        cards.remove(pos);

        return card;
    }

    private static void addCard(LinkedList<Card> deck, Card.CardType card, String variant, int count) {
        for (int i=0; i<count; i++)
            deck.add(new Card(card, variant));
    }

    private static void addCard(LinkedList<Card> deck, Card.CardType card, String variant, int count, int current, int max) {
        if (current < max)
            addCard(deck, card, variant, count);
    }

        // deck without exploding kittens
    private void makeSharingDeck() {
        LinkedList<Card> deck = new LinkedList<>();
        //Nope
        addCard(deck, Card.CardType.nope, "card", 5);

        //Attack
        addCard(deck, Card.CardType.attack, "mine", 2);
        addCard(deck, Card.CardType.attack, "space", 2);

        //Defuse
        addCard(deck, Card.CardType.defuse, "banjo", 2);
        addCard(deck, Card.CardType.defuse, "catnip", 2);
        addCard(deck, Card.CardType.defuse, "laser", 2);

        //Cats
        addCard(deck, Card.CardType.cat, "beard", 4);
        addCard(deck, Card.CardType.cat, "potato", 4);
        addCard(deck, Card.CardType.cat, "taco", 4);
        addCard(deck, Card.CardType.cat, "rainbow", 4);
        addCard(deck, Card.CardType.cat, "cattermelon", 4);

        //See the future
        addCard(deck, Card.CardType.future, "bear", 1);
        addCard(deck, Card.CardType.future, "goggles", 1);
        addCard(deck, Card.CardType.future, "mantis", 1);
        addCard(deck, Card.CardType.future, "pig", 1);
        addCard(deck, Card.CardType.future, "pigacorn", 1);

        //Favor
        addCard(deck, Card.CardType.favor, "card", 4);

        //Skip
        addCard(deck, Card.CardType.skip, "nap", 2);
        addCard(deck, Card.CardType.skip, "sprint", 1);
        addCard(deck, Card.CardType.skip, "bunnyraptor", 1);

        //Shuffle
        addCard(deck, Card.CardType.shuffle, "litterbox", 2);
        addCard(deck, Card.CardType.shuffle, "scratch", 2);

        Collections.shuffle(deck);

        this.cards = deck;
    }

    public void addExplodingKittens() {
        int explodingCount = 0;

        for (int i=0; i<2; i++) {
            addCard(this.cards, Card.CardType.exploding, "a", 1, explodingCount++, playerCount - 1);
            addCard(this.cards, Card.CardType.exploding, "b", 1, explodingCount++, playerCount - 1);
        }

        Collections.shuffle(this.cards);
    }

}
