package weKittens.interfaces;
import weKittens.Card;

public interface ATLocalInterface {
    // MODIFICATION : Ajout du paramètre targetInfo
    boolean cardPlayed(Card card, int targetInfo);

    boolean playerDrewCard();
    void updateTurnStatus(String message, boolean isMyTurn);
}