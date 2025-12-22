package edu.upc.epsevg.prop.oust.players.campillos_gil;

import edu.upc.epsevg.prop.oust.IPlayer;
import edu.upc.epsevg.prop.oust.IAuto;
import edu.upc.epsevg.prop.oust.Game;
import edu.upc.epsevg.prop.oust.GameStatus;
import edu.upc.epsevg.prop.oust.PlayerMove;
import edu.upc.epsevg.prop.oust.PlayerType;
import edu.upc.epsevg.prop.oust.SearchType;
import static edu.upc.epsevg.prop.oust.SearchType.MINIMAX;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Jugador proposat amb MiniMax
 * @author Pau i Lidia
 */
public class PlayerMiniMaxIDS implements IPlayer, IAuto {
    
    PlayerMove moviment;
    boolean timeOut;
    PlayerMiniMax MinimaxActual;

    public PlayerMiniMaxIDS() {
        List<Point> millorTirada = new ArrayList<>();
        long numNodesExplorats = 0;
        int maxDepth = 0;
        SearchType tipusCerca = MINIMAX;
        this.moviment = new PlayerMove(millorTirada, numNodesExplorats, maxDepth, tipusCerca);
    }

    /**
     * Ens avisa que hem de parar la cerca en curs perquè s'ha exhaurit el temps
     * de joc.
     */
    @Override
    public void timeout() {
        // Bah! Humans do not enjoy timeouts, oh, poor beasts !
        timeOut = true;
        System.out.println("Bah! You are so slow...");
        if (MinimaxActual != null) {
            MinimaxActual.abortarMissio = true;
        }
    }

    /**
     * Decideix el moviment del jugador donat un tauler i un color de peça que
     * ha de posar.
     *
     * @param s1 
     * @return
     */
    @Override
    public PlayerMove move(GameStatus s1) {
        timeOut = false;
        
        for (int i = 1; !timeOut; i++){
            MinimaxActual = new PlayerMiniMax(i);
            PlayerMove result = MinimaxActual.move(s1);
            if (!MinimaxActual.haSigutAbortat()) {
                moviment = new PlayerMove(result.getPoints(), result.getNumerOfNodesExplored(), result.getMaxDepthReached(), result.getSearchType());
            }
        }
        if (moviment.getPoints().size()==0){
            //tirar primera possible
            List<Point> tiradesPossibles = s1.getMoves();
            if (tiradesPossibles.size() > 0) moviment.getPoints().add(tiradesPossibles.get(0));
        }

        return moviment;
    }

    /**
     * Retorna el nom del jugador que s'utlilitza per visualització a la UI
     *
     * @return Nom del jugador
     */
    @Override
    public String getName() {
        return "PlayerMiniMaxIDS";
    }

}
