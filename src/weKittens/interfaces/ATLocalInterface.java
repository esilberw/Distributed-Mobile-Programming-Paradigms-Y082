package weKittens.interfaces;
import weKittens.Card;

public interface ATLocalInterface {
    boolean cardPlayed(Card card);
    boolean playerDrewCard();

    void updateTurnStatus(String message, boolean isMyTurn);
}
