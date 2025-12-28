package edu.upc.epsevg.prop.oust.players.campillos_gil;
import edu.upc.epsevg.prop.oust.*;
import java.awt.Point;
//import edu.upc.epsevg.prop.oust;
import edu.upc.epsevg.prop.oust.players.campillos_gil.DisjointSet;

/**
 * @author Pau i Lidia
 */
public class GameStatusTunned extends GameStatus{
    
    public DisjointSet ds;
    private int rows;
    private int cols;

    /**
     * Constructor que crea una nova instància de GameStatusTunned a partir d'un GameStatus base.
     * @param gs L'estat del joc original.
     */
    public GameStatusTunned(GameStatus gs) {
        super(gs);
        inicialitzarDS(); 
    }

    /**
     * Constructor per còpia. Crea una nova instància duplicant l'estat i les estructures d'un altre GameStatusTunned.
     * Serveix per simular moviments futurs sense modificar el tauler original.
     * @param gs L'estat del joc que volem copiar.
     */
    public GameStatusTunned(GameStatusTunned gs) {
        super(gs);
        if (gs.ds != null) {
            this.ds = new DisjointSet(gs.ds);
        } else {
            inicialitzarDS();
        }
    }

    @Override
    /**
     * Realitza un moviment al tauler i actualitza l'estructura DisjointSet.
     * Detecta si hi ha hagut captures per decidir si cal reconstruir tota l'estructura o només fer una actualització incremental.
     * @param p Les coordenades on es vol posar la fitxa.
     */
    public void placeStone(Point p) {
        int difFitxes = diff();
        super.placeStone(p); 
        int difFitxesFinal = diff();
        int diferencia = Math.abs(difFitxesFinal - difFitxes);
        if (diferencia != 1) { // hem matat 
            inicialitzarDS();
        } else {
            PlayerType jugadorTirada = this.getColor(p); // només hem afegit una fitxa així que actualitzem el DisjointSet
            conectarVeins(p, jugadorTirada);
        }
    }

    /**
     * Simula un moviment per calcular quantes fitxes es capturen o com canvia la puntuació.
     * @param p La coordenada del moviment a avaluar.
     * @return La diferència de punts que provoca aquest moviment (impacte de la jugada).
     */
    public int esCaptura(Point p){
        GameStatusTunned aux = new GameStatusTunned(this);
        int difFitxes = diff();
        ((GameStatus)aux).placeStone(p);
        int difFitxesFinal = aux.diff();
        return difFitxes - difFitxesFinal ;
    }

    /**
     * Uneix la fitxa de la posició donada amb els seus veïns del mateix color utilitzant el DisjointSet.
     * Serveix per actualitzar la connectivitat i la puntuació de manera incremental, sense haver de reconstruir tota l'estructura.
     * @param p La coordenada de la fitxa acabada de posar.
     * @param myColor El color del jugador que ha realitzat el moviment.
     */
    private void conectarVeins(Point p, PlayerType myColor) {
        for (Dir d : Dir.values()) {
            Point veiP = d.add(p);
            if (this.isInBounds(veiP)) { 
                if (this.getColor(veiP) == myColor) {
                    int index = getIndex(p);
                    ds.union(index, getIndex(veiP));
                    if (myColor == PlayerType.PLAYER1) ds.puntuacio += ds.getSize(index);
                    else ds.puntuacio -= ds.getSize(index);
                }
            }
        }
    }

    /**
     * Aquest mètode llegeix tot el tauler actual i construeix els grups des de zero.
     */
    private void inicialitzarDS() {
        int size = this.getSquareSize();
        int totalCells = size * size; 
        this.ds = new DisjointSet(totalCells);
        this.rows = size;
        this.cols = size;
        // Recorrem tot el tauler per crear els grups
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                
                Point p = new Point(r,c);
                if (this.isInBounds(p) == false) continue;
                PlayerType type = this.getColor(p);
                if (type != null) {
                    // Unim amb els veïns que ja hem visitat 
                    ds.numNodes++;
                    conectarVeins(p, type);
                }
            }
        }
    }

    /**
     * Transforma les coordenades 2D (x, y) en un índex lineal únic.
     * Necessari per mapejar les caselles del tauler a l'array intern de l'estructura DisjointSet.
     * @param p El punt o coordenada del tauler a convertir.
     * @return Un enter que representa la posició dins l'array lineal (0..N).
     */
    public int getIndex(Point p) {
        return p.y * this.cols + p.x;
    }
    
    /**
     * Retorna l'estructura auxiliar DisjointSet associada a aquest estat.
     * Útil per a l'avaluació heurística, permetent consultar la connectivitat de les peces sense haver de recalcular-la.
     * @return L'objecte DisjointSet actual.
     */
    public DisjointSet getDS() {
        return this.ds;
    }
}