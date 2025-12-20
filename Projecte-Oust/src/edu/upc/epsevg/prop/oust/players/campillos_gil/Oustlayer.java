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
public class Oustlayer implements IPlayer, IAuto {
    
    int profunditat;
    PlayerMove moviment;
    
    public Oustlayer(int prof) {
        this.profunditat = prof;

        List<Point> millorTirada = new ArrayList<>();
        long numNodesExplorats = 0;
        int maxDepth = prof;
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
        
        PlayerMove move = triaMillorMoviment(s1, profunditat);
        return move;
    }
    
    private PlayerMove triaMillorMoviment(GameStatus t, int depth)
    {
        int valor = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        PlayerType player = t.getCurrentPlayer(); 
        // Esta lista guardará la solución final completa
        List<Point> millorSecuencia = new ArrayList<>();
        
        List<Point> tiradesPossibles = t.getMoves();

        for (int i = 0; i < tiradesPossibles.size(); i++){
            Point p = tiradesPossibles.get(i);
            GameStatus t_nou = new GameStatus(t);
            t_nou.placeStone(p);
            
            // Creamos un contenedor vacio para que la recursión nos lo rellene
            PlayerMove childMove = new PlayerMove(new ArrayList<>(), 0, 0, MINIMAX);
            
            int candidat;
            // Lógica de captura: Si sigo siendo yo, llamo a Max
            if (t_nou.getCurrentPlayer() == player) candidat = Max_Valor(t_nou, childMove, depth, alpha, beta);
            else candidat = Min_Valor(t_nou, childMove, depth-1, alpha, beta);
            
            if (candidat > valor){ // IMPORTANTE: Usar > en lugar de >= para evitar sesgo al final
                valor = candidat;
                millorSecuencia.clear();
                millorSecuencia.add(p); // Añadimos mi piedra
                
                // Si era una captura (mismo jugador), pegamos la secuencia del hijo
                if (t_nou.getCurrentPlayer() == player) {
                    millorSecuencia.addAll(childMove.getPoints());
                }
            }
            
            alpha = Math.max(alpha, valor);
        }
        
        // Devolvemos la secuencia completa construida
        return new PlayerMove(millorSecuencia, 0, depth, MINIMAX);
    }

    private int Max_Valor(GameStatus t, PlayerMove moviments, int depth, int alpha, int beta){
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
        
        // No necesitamos copiar 'moviments' aquí, solo rellenarlo si encontramos algo mejor
        
        for (int i = 0; i < tiradesPossibles.size(); i++) {
            Point p = tiradesPossibles.get(i);
            GameStatus t_nou = new GameStatus(t);
            t_nou.placeStone(p);
            
            // Contenedor para la siguiente recursión
            PlayerMove nextMoveContainer = new PlayerMove(new ArrayList<>(), 0, 0, MINIMAX);
            int valHijo;

            if (t_nou.getCurrentPlayer() == t.getCurrentPlayer()) valHijo = Max_Valor(t_nou, nextMoveContainer, depth, alpha, beta);
            else valHijo = Min_Valor(t_nou, nextMoveContainer, depth-1, alpha, beta);
            
            if (valHijo > valor) {
                valor = valHijo;
                
                // AQUÍ ESTÁ LA SOLUCIÓN:
                // Actualizamos el objeto 'moviments' del padre con la mejor jugada encontrada aquí
                moviments.getPoints().clear();
                moviments.getPoints().add(p);
                
                // Si ha sido captura, añadimos lo que nos ha devuelto el hijo
                if (t_nou.getCurrentPlayer() == t.getCurrentPlayer()) moviments.getPoints().addAll(nextMoveContainer.getPoints());
            }
            
            alpha = Math.max(alpha, valor);
            if (beta <= alpha) break;
        }
        return valor;
    }

    private int Min_Valor(GameStatus t, PlayerMove moviments, int depth, int alpha, int beta){
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
        
        for (int i = 0; i < tiradesPossibles.size(); i++) {
            Point p = tiradesPossibles.get(i);
            GameStatus t_nou = new GameStatus(t);
            t_nou.placeStone(p);
            
            PlayerMove nextMoveContainer = new PlayerMove(new ArrayList<>(), 0, 0, MINIMAX);
            int valHijo;
            
            if (t_nou.getCurrentPlayer() == t.getCurrentPlayer()) valHijo = Min_Valor(t_nou, nextMoveContainer, depth, alpha, beta);
            else valHijo = Max_Valor(t_nou, nextMoveContainer, depth-1, alpha, beta);

            if (valHijo < valor){
                valor = valHijo;
                // Para MIN no es crítico guardar la jugada, pero mantenemos la lógica por seguridad
                moviments.getPoints().clear();
                moviments.getPoints().add(p);
                if (t_nou.getCurrentPlayer() == t.getCurrentPlayer()) {
                    moviments.getPoints().addAll(nextMoveContainer.getPoints());
                }
            }
            
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
        return "Oustlayer(" + "MiniMaxPlayer" + ")";
    }

}
