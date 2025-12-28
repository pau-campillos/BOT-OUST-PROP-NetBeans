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

    /**
     * Constructor principal del agent que utilitza el algoritme de Minimax IDS.
     * Inicialitza l'objecte PlayerMove amb valors per defecte.
     */
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

    @Override
    /**
     * Mètode principal que decideix el següent moviment utilitzant IDS.
     * Executa iteracions successives de l'algorisme Minimax, augmentant la profunditat fins que s'esgota el temps.
     * Si una iteració es completa sense interrupcions, es guarda com a "millor moviment actual". Així, quan el temps s'acaba, 
     * sempre tenim disponible el resultat de la cerca més profunda completada.
     * @param s1 L'estat actual del tauler de joc.
     * @return L'objecte PlayerMove amb la millor coordenada trobada i les estadístiques de la cerca.
     */
    public PlayerMove move(GameStatus s1) {
        timeOut = false;
        long numNodesAnt = 0;
        for (int i = 1; !timeOut; i++){
            if ( moviment.getNumerOfNodesExplored() > 0 ) numNodesAnt = moviment.getNumerOfNodesExplored();
            MinimaxActual = new PlayerMiniMax(i);
            PlayerMove result = MinimaxActual.move(s1);
            // Només guardem el resultat si la cerca s'ha completat totalment
            if (!MinimaxActual.haSigutAbortat()) {
                moviment = new PlayerMove(result.getPoints(), numNodesAnt + result.getNumerOfNodesExplored(), result.getMaxDepthReached(), result.getSearchType());
            }
        }
        if (moviment.getPoints().size()==0){
            // Tirar primera possible en cas de que no s'hagi pogut ni completar la profunditat 1
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
