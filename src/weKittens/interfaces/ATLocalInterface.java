package weKittens.interfaces;
import weKittens.Card;

public interface ATLocalInterface {
    // play single card with optional target info for Favor:
    boolean cardPlayed(Card card, int targetInfo);

    // targetInfo : 1=Left, 2=Top, 3=Right
    // stealIndex : idx oppennentHand chosen by player
    boolean pairPlayed(Card card, int targetInfo, int stealIndex);

    // to bound the choice to steal a card
    int getOpponentHandSize(int targetInfo);

    boolean triplePlayed(Card card, int targetInfo, String reqType, String reqVariant);

    boolean special5ComboPlayed();

    boolean pickFromDiscard(String type, String variant);

    boolean nopePlayed(Card card);

    void leaveSpectatingMode();

    void leaveSession();

    boolean playerDrewCard();
}