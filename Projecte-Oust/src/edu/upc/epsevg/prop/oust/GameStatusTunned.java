
package edu.upc.epsevg.prop.oust;

import java.awt.Point;
import edu.upc.epsevg.prop.oust.players.campillos_gil.DisjointSet;

/**
 *
 * @author Usuari
 */
public class GameStatusTunned extends GameStatus{
    
    private DisjointSet ds;
    private int rows;
    private int cols;

    // Constructor per començar de zero (root)
    public GameStatusTunned(GameStatus gs) {
        super(gs);
        inicialitzarDS(); 
    }

    public GameStatusTunned(GameStatusTunned gs) {
        super(gs);
        if (gs.ds != null) {
            this.ds = new DisjointSet(gs.ds);
        } else {
            inicialitzarDS();
        }
    }

    @Override
    public void placeStone(Point p) {

        int difFitxes = diff();

        super.placeStone(p); 
        int difFitxesFinal = diff();
        int diferencia = Math.abs(difFitxesFinal - difFitxes);
        if (diferencia != 1) {
            //HEM  MATAT; REVISAR DISJOINT-SET

            // Com que 'super.placeStone' pot haver esborrat fitxes, 
            // el DisjointSet antic ja no és vàlid (no suporta esborrats).
            // Hem de regenerar-lo mirant com ha quedat el tauler.
            inicialitzarDS();
        } else {
            // Només hem afegit una fitxa, podem actualitzar el DisjointSet
            PlayerType jugadorTirada = this.getColor(p);

            // Unim amb els veïns del mateix color
            conectarVeins(p, jugadorTirada);
        }
        //...
        // les vostes cosetes...
    }

    
    private void conectarVeins(Point p, PlayerType myColor) {
        for (Dir d : Dir.values()) {
            //int nr = p.x + d.add(p).x;
            //int nc = p.y + d.add(p).y;

            // Verifiquem límits
            Point veiP = d.add(p);
            if (this.isInBounds(veiP)) { 
                if (this.getColor(veiP) == myColor) {
                    ds.union(getIndex(p), getIndex(veiP));
                }
            }
        }
    }

    /**
     * Aquest mètode llegeix tot el tauler actual i construeix 
     * els grups des de zero.
     */
    private void inicialitzarDS() {
        int size = this.getSquareSize();
        // Assumim tauler quadrat per la matriu subjacent, o fem servir size*size
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
                    // (per exemple, només cal mirar amunt i esquerra si recorrem en ordre, 
                    // però en hexagonal és més segur mirar els veïns vàlids adjacents)
                    ds.numNodes++;
                    conectarVeins(p, type);
                }
            }
        }
    }

    // Mapeig de (Row, Col) a Index (0..N)
    public int getIndex(Point p) {
        return p.y * this.cols + p.x;
    }
    
    // Getter per a la teva heurística
    public DisjointSet getDS() {
        return this.ds;
    }

    // {Índice} =(fila * ancho_del_tablero) + (columna)
    
}