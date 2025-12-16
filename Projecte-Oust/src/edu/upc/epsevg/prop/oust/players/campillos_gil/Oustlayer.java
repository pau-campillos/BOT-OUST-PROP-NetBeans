package edu.upc.epsevg.prop.oust.players.campillos_gil;

import edu.upc.epsevg.prop.oust.IPlayer;
import edu.upc.epsevg.prop.oust.IAuto;

import edu.upc.epsevg.prop.oust.GameStatus;
import edu.upc.epsevg.prop.oust.PlayerMove;
import edu.upc.epsevg.prop.oust.PlayerType;
import edu.upc.epsevg.prop.oust.SearchType;
import static edu.upc.epsevg.prop.oust.SearchType.MINIMAX;
import java.awt.Point;
import java.util.List;

/**
 * Jugador proposat amb MiniMax
 * @author Pau i Lidia
 */
public class Oustlayer implements IPlayer, IAuto {
    
    String name;

    public Oustlayer(String name) {
        this.name = name;
    }

    /**
     * Ens avisa que hem de parar la cerca en curs perquè s'ha exhaurit el temps
     * de joc.
     */
    @Override
    public void timeout() {
        // Bah! Humans do not enjoy timeouts, oh, poor beasts !
        System.out.println("Bah! You are so slow...");
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
        //PlayerType player = s1.getCurrentPlayer(); //No va aqui, va abans de fer la tirada del max i del min
        //Despres de la tirada, torno a fer current player. Si es el mateix, he matat.
        //Si no es el mateix, passo de max a min o al revés (la tirada no ha matat a ningú i li toca al altre jugador)
        
        PlayerMove move = triaMillorMoviment(s1, 0);
        return move;
    }

    private PlayerMove triaMillorMoviment(GameStatus t, int depth)
    {
        List<Point> movRealitzats = new List<Point>();//NO SE COMMMMMMMMMMMMMM
        long numNodesExplorats = 0;
        int maxDepth = 0;
        SearchType tipusCerca=  MINIMAX;
        PlayerMove millorMoviment = new PlayerMove(movRealitzats, numNodesExplorats, maxDepth, tipusCerca);
        int valor = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        // no podem mirar si el moviment es possible però tenim getMOves que ens retorna els moviments possibles
        List<Point> tiradesPossibles = t.getMoves();
        for (int i = 0; i<tiradesPossibles.size(); i++){
            
            PlayerType player = t.getCurrentPlayer();

            GameStatus t_nou = new GameStatus(t);

            t_nou.placeStone(tiradesPossibles[i]);

            int candidat = Min_Valor(t_nou, depth-1, alpha, beta);
            if(candidat > valor){
                valor = candidat;
                millorMoviment = tiradesPossibles[i];
            }
            
            if (usaPoda) {
                alpha = Math.max(alpha, valor);
            }
        }

        if (millorMoviment == -1) {
            for(int columna = 0; columna < t.getMida(); ++columna){
                if (t.movpossible(columna)){
                    millorMoviment = columna;
                    break;
                }
            }
        }
        return millorMoviment;
    }

    
    private int Min_Valor(Tauler t, int depth, int alpha, int beta){
        // Avaluació d'estats terminals: Victòria/Derrota o profunditat màxima
        if (t.solucio(col, -color)) return Integer.MAX_VALUE; // MAX va guanyar l'últim moviment (derrota del MIN actual)
        else if (!t.espotmoure()) return 0; // Tauler ple (empat)
        else if (depth == 0) return valorarEstat(t); // Avaluació heurística

        int valor = Integer.MAX_VALUE;

        for (int columna = 0; columna < t.getMida(); columna++) {
            if (!t.movpossible(columna)) continue;

            Tauler s = new Tauler(t);
            s.afegeix(columna, color);

            valor = Math.min(valor, Max_Valor(s, depth-1, alpha, beta));

            // Poda Alpha: Si el valor MIN és més petit o igual que alpha, la branca es poda.
            if(usaPoda && valor <= alpha){
                return valor;
            }
            beta = Math.min(beta, valor);
        }

        return valor;
    }

    private int Max_Valor(Tauler t, int depth, int alpha, int beta){
        // Avaluació d'estats terminals: Victòria/Derrota o profunditat màxima
        if (t.solucio(col, -color)) return Integer.MIN_VALUE; // MIN va guanyar l'últim moviment (derrota del MAX actual)
        else if (!t.espotmoure()) return 0; // Tauler ple (empat)
        else if (depth == 0) return valorarEstat(t); // Avaluació heurística

        int valor = Integer.MIN_VALUE;
        for (int columna = 0; columna < t.getMida(); columna++) {
            if (!t.movpossible(columna)) continue;

            Tauler s = new Tauler(t);
            s.afegeix(columna, color);

            valor = Math.max(valor, Min_Valor(s, depth-1, alpha, beta));

            // Poda Beta: Si el valor MAX és més gran o igual que beta, la branca es poda.
            if(usaPoda && beta <= valor){
                return valor;
            }
            alpha = Math.max(alpha, valor);
        }
        return valor;
    }
   
    private int valorarEstat(Tauler t){
        int h = 0;
        if(usaHeur) h = evaluarEstat(t);
        return h;
    }
    /**
     * Retorna el nom del jugador que s'utlilitza per visualització a la UI
     *
     * @return Nom del jugador
     */
    @Override
    public String getName() {
        return "Oustlayer(" + name + ")";
    }

}
