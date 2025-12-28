package edu.upc.epsevg.prop.oust.players.campillos_gil;

import edu.upc.epsevg.prop.oust.IPlayer;
import edu.upc.epsevg.prop.oust.IAuto;
import edu.upc.epsevg.prop.oust.Game;
import edu.upc.epsevg.prop.oust.GameStatus;
import edu.upc.epsevg.prop.oust.players.campillos_gil.GameStatusTunned;
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
    
    int profunditat; // Profunditat de la cerca
    PlayerMove moviment; // Millor/s moviment/s a realitzar
    long nodesExplorats; // Nodes explorats en la cerca
    public boolean abortarMissio; // Indica si s'ha de parar la cerca ( només s'utilitza quan es crida per IDS )
    private PlayerType playerIni; // Guardem quin jugador és el bot

    /**
     * Constructor que prepara una execució de l'algorisme Minimax per a una profunditat específica.
     * Inicialitza les variables de control per començar la cerca.
     * @param prof La profunditat màxima (nivell de l'arbre) a la qual ha d'arribar aquesta cerca.
     */
    public PlayerMiniMax (int prof) {
        this.profunditat = prof; // Guardem la profunditat de la cerca
        abortarMissio = false;
        List<Point> millorTirada = new ArrayList<>();
        this.moviment = new PlayerMove(millorTirada, 0, prof, MINIMAX);
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
     * @param s1 L'estat actual del tauler;
     * @return L'objecte PlayerMove amb el millor moviment trobat en aquesta cerca.
     */
    @Override
    public PlayerMove move(GameStatus s1) {
        GameStatusTunned s2 = new GameStatusTunned(s1);
        double prof = (double)profunditat;
        triaMillorMoviment(s2, prof);
        return moviment;
    }

    /**
     * Classe auxiliar interna utilitzada per a l'ordenació de moviments (Move Ordering).
     * Permet associar puntuacions heurístiques a cada coordenada per poder ordenar-les de millor a pitjor.
     * Això és clau per optimitzar la poda Alfa-Beta: si explorem primer els moviments més prometedors, podem descartar branques inútils molt abans.
     */
    private class ScoredMove implements Comparable<ScoredMove> {
        Point p;
        int score; // Puntuació del moviment (diff o heurística)
        int secondaryScore; // Per desempatar (p.ex: disjoint set size)

        /**
         * Constructor que inicialitza un moviment amb les mètriques d'avaluació.
         * Emmagatzema la coordenada juntament amb dues puntuacions per permetre una ordenació precisa (criteri principal + desempat).
         * @param p La coordenada del moviment candidat al tauler.
         * @param score La puntuació heurística principal.
         * @param secondaryScore La puntuació secundària per resoldre empats.
         */
        public ScoredMove(Point p, int score, int secondaryScore) {
            this.p = p;
            this.score = score;
            this.secondaryScore = secondaryScore;
        }

        @Override
        /**
         * Comparador per ordenar els moviments de manera descendent (de millor a pitjor).
         * Prioritza primer la puntuació principal (`score`) i utilitza la secundària (`secondaryScore`) per desempatar.
         * Aquest ordre és vital per a l'optimització de "Move Ordering", fent que la poda Alfa-Beta descarti branques ineficients molt més ràpidament.
         * @param other L'altre moviment amb el qual es compara l'actual.
         * @return Un enter que indica la posició relativa per a l'ordenació descendent.
         */
        public int compareTo(ScoredMove other) {
            // Ordenem de major a menor (descendent)
            int cmp = Integer.compare(other.score, this.score);
            if (cmp == 0) {
                return Integer.compare(other.secondaryScore, this.secondaryScore);
            }
            return cmp;
        }
    }

    /**
     * Ordena heurísticament la llista de moviments possibles.
     * Simula cada moviment per calcular-ne l'impacte immediat i els ordena de millor a pitjor.
     * Aquesta ordenació és crítica per a l'eficiència de la poda Alfa-Beta: si trobem els bons moviments d'hora, podrem podar grans parts de l'arbre de cerca.
     * @param t L'estat actual del tauler abans de fer cap moviment.
     * @param tiradesPossibles La llista de punts on es pot tirar, que serà reordenada.
     */
    private void Ordena(GameStatusTunned t, List<Point> tiradesPossibles) {
        List<ScoredMove> evaluatedMoves = new ArrayList<>();
        PlayerType currentPlayer = t.getCurrentPlayer();
        int currentDiff = t.diff(); // Calculem el diff base un cop
        for (Point p : tiradesPossibles) {
            GameStatusTunned aux = new GameStatusTunned(t); 
            aux.placeStone(p);
            int newDiff = aux.diff();
            int diffChange = newDiff - currentDiff; 
            // Ajustem el signe perquè "més és millor" sempre per a l'ordenació
//            // Si sóc P1 (fitxes positives), vull que diff pugi (diffChange positiu).
            // Si sóc P2 (fitxes negatives), vull que diff baixi (diffChange negatiu).
            int primaryScore;
            if (currentPlayer == PlayerType.PLAYER1) {
                primaryScore = diffChange; // Busquem valors positius grans
            } else {
                primaryScore = -diffChange; // Busquem valors negatius grans (els convertim a positius per ordenar)
            }
            int secondaryScore = 0; 
            secondaryScore = aux.getDS().puntuacio;
            evaluatedMoves.add(new ScoredMove(p, primaryScore, secondaryScore));
        }
        java.util.Collections.sort(evaluatedMoves); // ordenem 
        // Posem els punts ordenats a la llista original
        tiradesPossibles.clear();
        for (ScoredMove sm : evaluatedMoves) {
            tiradesPossibles.add(sm.p);
        }
    }
        
    /**
     * Funció arrel (Root) de l'algorisme Minimax amb poda Alfa-Beta.
     * Inicia l'exploració de l'arbre de joc per trobar la millor seqüència de moviments des de l'estat actual.
     * @param t L'estat actual del tauler a l'inici del torn.
     * @param depth La profunditat màxima assignada per a aquesta iteració de la cerca.
     */
    private void triaMillorMoviment(GameStatusTunned t, double depth) {
        int valor = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        nodesExplorats = 0; // Reiniciem el comptador de nodes explorats
        PlayerType player = t.getCurrentPlayer(); 
        List<Point> millorSequencia = new ArrayList<>();
        List<Point> tiradesPossibles = t.getMoves();
        playerIni = t.getCurrentPlayer();  

        Ordena (t, tiradesPossibles);
        for (int i = 0; !abortarMissio && i < tiradesPossibles.size(); i++){
            Point p = tiradesPossibles.get(i);
            GameStatusTunned t_nou = new GameStatusTunned(t);
            t_nou.placeStone(p);

            // Contenidor per a les jugades extres en cas de captura
            List<Point> movimentsExtres = new ArrayList<>();
            int candidat;
            // Lògica de captura: si el jugador actual és el mateix després de colocar la pedra, hi ha hagut captura
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
        // Guardem a la variable de classe el millor moviment trobat
        if (!abortarMissio) {
            moviment.getPoints().clear();
            moviment.getPoints().addAll(millorSequencia);
            moviment.setNumerOfNodesExplored(nodesExplorats);
        }
    }

    /**
     * Funció corresponent al node MAX de l'algorisme Minimax.
     * El seu objectiu és trobar la jugada que maximitza la puntuació per al jugador actual.
     * Gestiona la recursivitat i la poda Alfa-Beta. També s'encarrega de reconstruir la seqüència  completa de moviments en cas que hi hagi captures múltiples (torns extra).
     *
     * @param t L'estat del tauler a avaluar.
     * @param moviments Llista on s'acumularà la seqüència de punts en cas de torns consecutius.
     * @param depth La profunditat restant per explorar.
     * @param alpha El millor valor garantit per al jugador Maximitzador fins ara.
     * @param beta El millor valor garantit per al jugador Minimitzador fins ara.
     * @return El valor heurístic de la millor branca trobada.
     */
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

            // Contenidor per a les jugades extres en cas de captura
            List<Point> movimentsExtres;
            if (depth == profunditat) movimentsExtres = new ArrayList<>();
            else movimentsExtres = null;
            int candidat;
            // Lògica de captura: si el jugador actual és el mateix després de colocar la pedra, hi ha hagut captura
            if (t_nou.getCurrentPlayer() == t.getCurrentPlayer()) candidat = Max_Valor(t_nou, movimentsExtres, depth, alpha, beta);
            else candidat = Min_Valor(t_nou, movimentsExtres, depth-1, alpha, beta);
            
            if (abortarMissio) return 0;

            if (candidat > valor) {
                valor = candidat;
                if (depth == profunditat){ // Només afegim els moviments inicials 
                    // Actualizem l'objecte 'moviments' amb la millor jugada trobada
                    moviments.clear();
                    moviments.add(p);
                    // si hi ha hagut captura, afegim els moviments extres
                    if (t_nou.getCurrentPlayer() == t.getCurrentPlayer()) moviments.addAll(movimentsExtres);
                }
            }
            // Fem poda alfa-beta
            alpha = Math.max(alpha, valor);
            if (beta <= alpha) break;
        }
        return valor;
    }

    /**
     * Funció corresponent al node MIN de l'algorisme Minimax.
     * Simula el torn de l'oponent. El seu objectiu és trobar la jugada que minimitza la puntuació (el pitjor escenari per a nosaltres).
     *
     * @param t L'estat del tauler a avaluar.
     * @param moviments Llista per guardar seqüències. En el node MIN no sol ser necessària (l'oponent no reporta el seu camí a l'arrel), per la qual cosa es pot passar null.
     * @param depth La profunditat restant per explorar.
     * @param alpha El millor valor garantit per al jugador Maximitzador.
     * @param beta El millor valor garantit per al jugador Minimitzador.
     * @return El valor heurístic de la millor branca trobada.
     */
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
            // Lògica de captura: si el jugador actual és el mateix després de colocar la pedra, hi ha hagut captura
            if (t_nou.getCurrentPlayer() == t.getCurrentPlayer()) candidat = Min_Valor(t_nou, moviments, depth-0.5, alpha, beta);
            else candidat = Max_Valor(t_nou, moviments, depth-1, alpha, beta);
            
            if (abortarMissio) return 0;

            if (candidat < valor){
                valor = candidat;
            }
            // Fem poda alfa-beta
            beta = Math.min(beta, valor);
            if (beta <= alpha) break;
        }
        return valor;
    }

    /**
     * Mètode auxiliar que s'executa quan arribem a un node fulla (profunditat 0).
     * S'encarrega d'actualitzar les estadístiques de rendiment i de cridar a l'heurística d'avaluació.
     * @param t L'estat del tauler que cal puntuar.
     * @return Un valor enter que representa com de bo és aquest estat per al 'playerIni' (el jugador arrel).
     */
    private int valorarEstat(GameStatusTunned t){
        ++nodesExplorats;
        int h = 0;
        h = evaluarEstat(t, playerIni);
        return h;
    }

    /**
     * Retorna el nom del jugador que s'utlilitza per visualització a la UI
     * @return Nom del jugador
     */
    @Override
    public String getName() {
        return "PlayerMiniMax";
    }
}
