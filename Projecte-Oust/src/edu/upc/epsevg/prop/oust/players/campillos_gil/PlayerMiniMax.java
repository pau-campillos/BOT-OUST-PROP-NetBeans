package edu.upc.epsevg.prop.oust.players.campillos_gil;

import edu.upc.epsevg.prop.oust.IPlayer;
import edu.upc.epsevg.prop.oust.IAuto;
import edu.upc.epsevg.prop.oust.Game;
import edu.upc.epsevg.prop.oust.GameStatus;
import edu.upc.epsevg.prop.oust.GameStatusTunned;
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
public class PlayerMiniMax extends Heuristica implements IPlayer, IAuto {
    
    int profunditat; // profunditat de cerca
    PlayerMove moviment; // moviment/s a realitzar
    long nodesExplorats; // nodes explorats en la cerca
    public boolean abortarMissio; // indica si s'ha de parar la cerca ( només s'utilitza quan es crida per IDS )
    private PlayerType jo; // canviar nom a playerIni !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

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
        GameStatusTunned s2 = new GameStatusTunned(s1);
        double prof = (double)profunditat; //NO SE SI ES NECESARIIIIIIIIIIIIIIIIIIIII
        triaMillorMoviment(s2, prof);
        return moviment;
    }

    // Classe interna per guardar moviment + valor
    private class ScoredMove implements Comparable<ScoredMove> {
        Point p;
        int score; // Puntuació del moviment (diff o heurística)
        int secondaryScore; // Per desempatar (p.ex: disjoint set size)

        public ScoredMove(Point p, int score, int secondaryScore) {
            this.p = p;
            this.score = score;
            this.secondaryScore = secondaryScore;
        }

        @Override
        public int compareTo(ScoredMove other) {
            // Ordenem de major a menor (descendent)
            int cmp = Integer.compare(other.score, this.score);
            if (cmp == 0) {
                return Integer.compare(other.secondaryScore, this.secondaryScore);
            }
            return cmp;
        }
    }

    private void Ordena(GameStatusTunned t, List<Point> tiradesPossibles) {
        List<ScoredMove> evaluatedMoves = new ArrayList<>();
        PlayerType currentPlayer = t.getCurrentPlayer();
        int currentDiff = t.diff(); // Calculem el diff base un cop

        // 1. PRE-CÀLCUL: Iterem un cop per cada moviment possible
        for (Point p : tiradesPossibles) {
            GameStatusTunned aux = new GameStatusTunned(t); // Clonem 1 cop per moviment
            aux.placeStone(p);
            
            int newDiff = aux.diff();
            int diffChange = newDiff - currentDiff; // Quan ha canviat la diferència
            
            // Ajustem el signe perquè "més és millor" sempre per a l'ordenació
            // Si sóc P1 (fitxes positives), vull que diff pugi (diffChange positiu).
            // Si sóc P2 (fitxes negatives), vull que diff baixi (diffChange negatiu).
            int primaryScore;
            if (currentPlayer == PlayerType.PLAYER1) {
                primaryScore = diffChange; // Busquem valors positius grans
            } else {
                primaryScore = -diffChange; // Busquem valors negatius grans (els convertim a positius per ordenar)
            }

            // Puntuació secundària (el teu ds.puntuacio) per desempatar
            // Assumim que aux.getDS().puntuacio ja està calculat o és accessible
            // Si no tens 'puntuacio' public al DS, usa aux.miniPuntuacio(p) o similar
            // Aquí poso 0 per defecte si no tens accés directe, canvia-ho pel teu getter
            int secondaryScore = 0; 
            // secondaryScore = aux.getDS().puntuacio; (si fos accessible)

            evaluatedMoves.add(new ScoredMove(p, primaryScore, secondaryScore));
        }

        // 2. ORDENACIÓ RÀPIDA: Només comparem enters
        java.util.Collections.sort(evaluatedMoves);

        // 3. RECONSTRUCCIÓ: Posem els punts ordenats a la llista original
        tiradesPossibles.clear();
        for (ScoredMove sm : evaluatedMoves) {
            tiradesPossibles.add(sm.p);
        }
    }
        
    /*
    private void Ordena (GameStatusTunned t, List<Point> tiradesPossibles){
        tiradesPossibles.sort((p1, p2) -> {
            GameStatusTunned aux = new GameStatusTunned(t);
            int difFitxes = aux.diff();
            aux.placeStone(p1);
            int difFitxesFinal = aux.diff();
            int v1 = difFitxes - difFitxesFinal; // Inicial - Final

            GameStatusTunned aux2 = new GameStatusTunned(t);
            int difFitxes2 = aux2.diff();
            aux2.placeStone(p2);
            int difFitxesFinal2 = aux2.diff();
            int v2 = difFitxes2 - difFitxesFinal2;
            if (v1!=v2){
                if (t.getCurrentPlayer() == PlayerType.PLAYER1) {
                    // P1: Millor captura = Valor més petit (negatiu). 
                    // compare(v1, v2) posa el més petit primer.
                    return Integer.compare(v1, v2); 
                } else {
                    // P2: Millor captura = Valor més gran (positiu).
                    // compare(v2, v1) posa el més gran primer.
                    return Integer.compare(v2, v1);
                }
            } else {
                v1 = aux.ds.puntuacio;
                v2 = aux2.ds.puntuacio;
                if (t.getCurrentPlayer() == PlayerType.PLAYER1) {
                    return Integer.compare(v2, v1); 
                } else {
                    return Integer.compare(v1, v2);
                }
            }
            
        });
    }*/

    private void triaMillorMoviment(GameStatusTunned t, double depth) {
        int valor = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        nodesExplorats = 0; // Reiniciem el comptador de nodes explorats

        PlayerType player = t.getCurrentPlayer(); 
        List<Point> millorSequencia = new ArrayList<>();
        List<Point> tiradesPossibles = t.getMoves();
        jo = t.getCurrentPlayer(); // possible millora de nom a playerIni 
        /*tiradesPossibles.sort((p1, p2) -> {
            int v1 = t.esCaptura(p1); // Inicial - Final
            int v2 = t.esCaptura(p2);
            /*GameStatusTunned aux = new GameStatusTunned(t);
            aux.placeStone(p2);
            int v1 = evaluarEstat(t, jo);
            GameStatusTunned aux2 = new GameStatusTunned(t);
            aux2.placeStone(p2);
            int v2 = evaluarEstat(aux2, jo);



            if (v1!=v2){
                if (t.getCurrentPlayer() == PlayerType.PLAYER1) {
                    // P1: Millor captura = Valor més petit (negatiu). 
                    // compare(v1, v2) posa el més petit primer.
                    return Integer.compare(v1, v2); 
                } else {
                    // P2: Millor captura = Valor més gran (positiu).
                    // compare(v2, v1) posa el més gran primer.
                    return Integer.compare(v2, v1);
                }
            } else {
                v1 = t.miniPuntuacio(p1);
                v2 = t.miniPuntuacio(p2);
                if (t.getCurrentPlayer() == PlayerType.PLAYER1) {
                    return Integer.compare(v2, v1); 
                } else {
                    return Integer.compare(v1, v2);
                }
            }
            
        }); */

        Ordena (t, tiradesPossibles);
        for (int i = 0; !abortarMissio && i < tiradesPossibles.size(); i++){
            Point p = tiradesPossibles.get(i);

            GameStatusTunned t_nou = new GameStatusTunned(t);
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

    private int Max_Valor(GameStatusTunned t, List<Point> moviments, double depth, int alpha, int beta){
        if (abortarMissio) return 0;
        
        if (t.isGameOver()){
            PlayerType winner = t.GetWinner();
            if (winner == t.getCurrentPlayer()) {
                return Integer.MAX_VALUE; // MAX va guanyar l'últim moviment (victòria del MAX actual)
            } else {
                return Integer.MIN_VALUE; // MIN va guanyar l'últim moviment (derrota del MAX actual)
            }
        }
        else if (t.getMoves().isEmpty()) return 0;
        else if (depth <= 0) return valorarEstat(t);

        int valor = Integer.MIN_VALUE;
        List<Point> tiradesPossibles = t.getMoves();
        Ordena(t, tiradesPossibles);
        for (int i = 0; !abortarMissio && i < tiradesPossibles.size(); i++) {
            Point p = tiradesPossibles.get(i);

            GameStatusTunned t_nou = new GameStatusTunned(t);
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

    private int Min_Valor(GameStatusTunned t, List<Point> moviments, double depth, int alpha, int beta){
        if (abortarMissio) return 0;
        if (t.isGameOver()){
            PlayerType winner = t.GetWinner();
            if (winner == t.getCurrentPlayer()) {
                return Integer.MIN_VALUE; // MAX va guanyar l'últim moviment (victòria del MIN actual)
            } else {
                return Integer.MAX_VALUE; // MIN va guanyar l'últim moviment (derrota del MAX actual)
            }
        }
        else if (t.getMoves().isEmpty()) return 0; 
        else if (depth <= 0) return valorarEstat(t);

        int valor = Integer.MAX_VALUE;
        List<Point> tiradesPossibles = t.getMoves();
        Ordena (t, tiradesPossibles);
        for (int i = 0; !abortarMissio && i < tiradesPossibles.size(); i++) {
            Point p = tiradesPossibles.get(i);

            GameStatusTunned t_nou = new GameStatusTunned(t);
            t_nou.placeStone(p);
            
            int candidat;
            // lògica de captura: si el jugador actual és el mateix després de colocar la pedra, hi ha hagut captura
            if (t_nou.getCurrentPlayer() == t.getCurrentPlayer()) candidat = Min_Valor(t_nou, moviments, depth-0.5, alpha, beta);
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

    private int valorarEstat(GameStatusTunned t){
        ++nodesExplorats;
        int h = 0;
        h = evaluarEstat(t, jo); // CANVIAR A TUNNEDDDDDDDDDDDD
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
