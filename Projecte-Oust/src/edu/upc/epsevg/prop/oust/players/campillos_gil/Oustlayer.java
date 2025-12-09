package edu.upc.epsevg.prop.oust.players.campillos_gil;

import edu.upc.epsevg.prop.oust.IPlayer;
import edu.upc.epsevg.prop.oust.IAuto;

import edu.upc.epsevg.prop.oust.GameStatus;
import edu.upc.epsevg.prop.oust.PlayerMove;
import edu.upc.epsevg.prop.oust.PlayerType;

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
        PlayerType player = s1.getCurrentPlayer(); //No va aqui, va abans de fer la tirada del max i del min
        //Despres de la tirada, torno a fer current player. Si es el mateix, he matat.
        //Si no es el mateix, passo de max a min o al revés (la tirada no ha matat a ningú i li toca al altre jugador)
        
        int tirada = triaMillorMoviment(s1, player, 0);
        return null;
    }
    
    private int triaMillorMoviment(GameStatus t, PlayerType player, int depth)
    {
        int millorMoviment = -1;
        int valor = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        int contador = 0;

        for (int columna = 0; t.espotmoure() && columna < t.getMida(); columna++) {
            if (!t.movpossible(columna)) continue;

            if (contador < 2 && t.getColor(0,columna) != 0){
              if (t.getColor(1, columna) != 0) contador+=2;
              else contador++; 
            }
            
            Tauler s = new Tauler(t);
            s.afegeix(columna, color);

            int iniJugador = color;

            int candidat = Min_Valor(s, -color, columna, depth-1, iniJugador, alpha, beta);
            if(candidat > valor){
                valor = candidat;
                millorMoviment = columna;
            }
            
            if (usaPoda) {
                alpha = Math.max(alpha, valor);
            }
        }

        if (contador < 2){
          jugadesFinalsTotals = jugadesFinalsEnCadaTorn;
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

    
    private int Min_Valor(Tauler t, PlayerType player, int col, int depth, int iniJugador, int alpha, int beta){
        // Avaluació d'estats terminals: Victòria/Derrota o profunditat màxima
        if (t.solucio(col, -color)) return Integer.MAX_VALUE; // MAX va guanyar l'últim moviment (derrota del MIN actual)
        else if (!t.espotmoure()) return 0; // Tauler ple (empat)
        else if (depth == 0) return valorarEstat(t, iniJugador); // Avaluació heurística

        int valor = Integer.MAX_VALUE;

        for (int columna = 0; columna < t.getMida(); columna++) {
            if (!t.movpossible(columna)) continue;

            Tauler s = new Tauler(t);
            s.afegeix(columna, color);

            valor = Math.min(valor, Max_Valor(s, -color, columna, depth-1, iniJugador, alpha, beta));

            // Poda Alpha: Si el valor MIN és més petit o igual que alpha, la branca es poda.
            if(usaPoda && valor <= alpha){
                return valor;
            }
            beta = Math.min(beta, valor);
        }

        return valor;
    }

    private int Max_Valor(Tauler t, PlayerType player, int col, int depth, int iniJugador, int alpha, int beta){
        // Avaluació d'estats terminals: Victòria/Derrota o profunditat màxima
        if (t.solucio(col, -color)) return Integer.MIN_VALUE; // MIN va guanyar l'últim moviment (derrota del MAX actual)
        else if (!t.espotmoure()) return 0; // Tauler ple (empat)
        else if (depth == 0) return valorarEstat(t, iniJugador); // Avaluació heurística

        int valor = Integer.MIN_VALUE;
        for (int columna = 0; columna < t.getMida(); columna++) {
            if (!t.movpossible(columna)) continue;

            Tauler s = new Tauler(t);
            s.afegeix(columna, color);

            valor = Math.max(valor, Min_Valor(s, -color, columna, depth-1, iniJugador, alpha, beta));

            // Poda Beta: Si el valor MAX és més gran o igual que beta, la branca es poda.
            if(usaPoda && beta <= valor){
                return valor;
            }
            alpha = Math.max(alpha, valor);
        }
        return valor;
    }
   
    private int valorarEstat(Tauler t, int iniJugador){
        int h = 0;
        jugadesFinalsTotals++;
        jugadesFinalsEnCadaTorn++;
        if(usaHeur) h = evaluarEstat(t, iniJugador);
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
