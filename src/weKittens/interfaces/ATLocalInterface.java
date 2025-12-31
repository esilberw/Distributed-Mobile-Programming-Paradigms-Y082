package weKittens.interfaces;
import weKittens.Card;

public interface ATLocalInterface {
    // Jouer une carte simple (avec cible optionnelle pour Favor)
    boolean cardPlayed(Card card, int targetInfo);


    // targetInfo : 1=Left, 2=Top, 3=Right
    // stealIndex : Le numéro choisi par le joueur (1..N)
    boolean pairPlayed(Card card, int targetInfo, int stealIndex);

    // NOUVEAU : Demander combien de cartes a l'adversaire (pour borner le choix)
    int getOpponentHandSize(int targetInfo);

    // reqType : Le type de carte demandé (ex: "defuse")
    // reqVariant : La variante demandée (ex: "catnip")
    boolean triplePlayed(Card card, int targetInfo, String reqType, String reqVariant);

    // NOUVEAU : Jouer le combo 5 cartes différentes
    boolean special5ComboPlayed();

    // NOUVEAU : Récupérer une carte de la défausse (suite au combo)
    boolean pickFromDiscard(String type, String variant);

    boolean playerDrewCard();

    void updateTurnStatus(String message, boolean isMyTurn);
}