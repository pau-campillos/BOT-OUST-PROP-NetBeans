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
public class PlayerMiniMax implements IPlayer, IAuto {
    
    int profunditat; // profunditat de cerca
    PlayerMove moviment; // moviment/s a realitzar
    long nodesExplorats; // nodes explorats en la cerca
    public boolean abortarMissio; // indica si s'ha de parar la cerca ( només s'utilitza quan es crida per IDS )
    
    public PlayerMiniMax (int prof) {
        this.profunditat = prof;
        abortarMissio = false;

        List<Point> millorTirada = new ArrayList<>();
        nodesExplorats = 0;
        int maxDepth = prof;
        SearchType tipusCerca = MINIMAX;

        this.moviment = new PlayerMove(millorTirada, nodesExplorats, maxDepth, tipusCerca);
    }

    /**
     * Ens avisa que hem de parar la cerca en curs perquè s'ha exhaurit el temps
     * de joc.
     */
    @Override
    public void timeout() {
        abortarMissio = false;
    }

    /**
     * Indica si la missió de càlcul del moviment ha estat abortada
     * @return true si ha estat abortada, false en cas contrari
     */
    public boolean haSigutAbortat() {
        return abortarMissio;
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
        triaMillorMoviment(s1, profunditat);
        return moviment;
    }
    
    private void triaMillorMoviment(GameStatus t, int depth) {
        int valor = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        nodesExplorats = 0; // Reiniciem el comptador de nodes explorats

        PlayerType player = t.getCurrentPlayer(); 
        List<Point> millorSequencia = new ArrayList<>();
        List<Point> tiradesPossibles = t.getMoves();

        for (int i = 0; !abortarMissio && i < tiradesPossibles.size(); i++){
            Point p = tiradesPossibles.get(i);
            nodesExplorats++; // actu comptador dels nodes explorats

            GameStatus t_nou = new GameStatus(t);
            t_nou.placeStone(p);

            // contenidor per a les jugades extres en cas de captura
            List<Point> movimentsExtres = new ArrayList<>();
            int candidat;
            // lògica de captura: si el jugador actual és el mateix després de colocar la pedra, hi ha hagut captura
            if (t_nou.getCurrentPlayer() == player) candidat = Max_Valor(t_nou, movimentsExtres, depth, alpha, beta);
            else candidat = Min_Valor(t_nou, movimentsExtres, depth-1, alpha, beta);
            
            if (abortarMissio) break;

            if (candidat > valor){
                valor = candidat;
                millorSequencia.clear();
                millorSequencia.add(p); // afegim la jugada actual
                
                // Si era una captura (= jugador), afegim la sequencia del fill
                if (t_nou.getCurrentPlayer() == player) {
                    millorSequencia.addAll(movimentsExtres);
                }
            }
            alpha = Math.max(alpha, valor);
        }

        // guardem a la variable de classe el millor moviment trobat
        if (!abortarMissio) {
            moviment.getPoints().clear();
            moviment.getPoints().addAll(millorSequencia);
            moviment.setNumerOfNodesExplored(nodesExplorats);
        }
    }

    private int Max_Valor(GameStatus t, List<Point> moviments, int depth, int alpha, int beta){
        if (abortarMissio) return 0;
        nodesExplorats++; // actu comptador dels nodes explorats
        if (t.isGameOver()){
            PlayerType winner = t.GetWinner();
            if (winner == t.getCurrentPlayer()) {
                return Integer.MAX_VALUE; // MAX va guanyar l'últim moviment (victòria del MAX actual)
            } else {
                return Integer.MIN_VALUE; // MIN va guanyar l'últim moviment (derrota del MAX actual)
            }
        }
        else if (t.getMoves().isEmpty()) return 0;
        else if (depth == 0) return valorarEstat(t);

        int valor = Integer.MIN_VALUE;
        List<Point> tiradesPossibles = t.getMoves();
        
        for (int i = 0; !abortarMissio && i < tiradesPossibles.size(); i++) {
            Point p = tiradesPossibles.get(i);

            GameStatus t_nou = new GameStatus(t);
            t_nou.placeStone(p);

            // contenidor per a les jugades extres en cas de captura
            List<Point> movimentsExtres;
            if (depth == profunditat) movimentsExtres = new ArrayList<>();
            else movimentsExtres = null;

            int candidat;
            // lògica de captura: si el jugador actual és el mateix després de colocar la pedra, hi ha hagut captura
            if (t_nou.getCurrentPlayer() == t.getCurrentPlayer()) candidat = Max_Valor(t_nou, movimentsExtres, depth, alpha, beta);
            else candidat = Min_Valor(t_nou, movimentsExtres, depth-1, alpha, beta);
            
            if (abortarMissio) return 0;

            if (candidat > valor) {
                valor = candidat;
                if (depth == profunditat){ // només afegim els moviments inicials 
                    // actualizem l'objecte 'moviments' amb la millor jugada trobada
                    moviments.clear();
                    moviments.add(p);
                    
                    // si hi ha hagut captura, afegim els moviments extres
                    if (t_nou.getCurrentPlayer() == t.getCurrentPlayer()) moviments.addAll(movimentsExtres);
                }
            }
            // fem poda alfa-beta
            alpha = Math.max(alpha, valor);
            if (beta <= alpha) break;
        }
        return valor;
    }

    private int Min_Valor(GameStatus t, List<Point> moviments, int depth, int alpha, int beta){
        if (abortarMissio) return 0;
        nodesExplorats++; // actu comptador dels nodes explorats
        if (t.isGameOver()){
            PlayerType winner = t.GetWinner();
            if (winner == t.getCurrentPlayer()) {
                return Integer.MIN_VALUE; // MAX va guanyar l'últim moviment (victòria del MIN actual)
            } else {
                return Integer.MAX_VALUE; // MIN va guanyar l'últim moviment (derrota del MAX actual)
            }
        }
        else if (t.getMoves().isEmpty()) return 0; 
        else if (depth == 0) return valorarEstat(t);

        int valor = Integer.MAX_VALUE;
        List<Point> tiradesPossibles = t.getMoves();
        
        for (int i = 0; !abortarMissio && i < tiradesPossibles.size(); i++) {
            Point p = tiradesPossibles.get(i);

            GameStatus t_nou = new GameStatus(t);
            t_nou.placeStone(p);
            
            int candidat;
            // lògica de captura: si el jugador actual és el mateix després de colocar la pedra, hi ha hagut captura
            if (t_nou.getCurrentPlayer() == t.getCurrentPlayer()) candidat = Min_Valor(t_nou, moviments, depth, alpha, beta);
            else candidat = Max_Valor(t_nou, moviments, depth-1, alpha, beta);
            
            if (abortarMissio) return 0;

            if (candidat < valor){
                valor = candidat;
            }
            // fem poda alfa-beta
            beta = Math.min(beta, valor);
            if (beta <= alpha) break;
        }
        return valor;
    }

    private int valorarEstat(GameStatus t){
        int h = 0;
        //h = evaluarEstat(t);
        return h;
    }
    /**
     * Retorna el nom del jugador que s'utlilitza per visualització a la UI
     *
     * @return Nom del jugador
     */
    @Override
    public String getName() {
        return "PlayerMiniMax";
    }

}
