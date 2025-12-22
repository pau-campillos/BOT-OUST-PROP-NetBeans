
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

    public GameStatusTunned(GameStatus gs) {
        super(gs);
        inicialitzarDS();
    }

    @Override
    public void placeStone(Point point) {


        super.placeStone(point); 

        //...
        // les vostes cosetes...
        // Com que 'super.placeStone' pot haver esborrat fitxes, 
        // el DisjointSet antic ja no és vàlid (no suporta esborrats).
        // Hem de regenerar-lo mirant com ha quedat el tauler.
        inicialitzarDS();
    }

    /**
     * Aquest mètode llegeix tot el tauler actual i construeix 
     * els grups des de zero.
     */
    private void inicialitzarDS() {
        int size = this.getSquareSize(); // 
        // Assumim tauler quadrat per la matriu subjacent, o fem servir size*size
        int totalCells = size * size; 
        
        this.ds = new DisjointSet(totalCells);
        this.rows = size;
        this.cols = size;

        // Recorrem tot el tauler per crear els grups
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                PlayerType type = this.getColor(r, c); // [cite: 83]
                
                if (type != null) {
                    int indexActual = getIndex(r, c);
                    ds.setOwner(indexActual, type);
                    
                    // Unim amb els veïns que ja hem visitat 
                    // (per exemple, només cal mirar amunt i esquerra si recorrem en ordre, 
                    // però en hexagonal és més segur mirar els veïns vàlids adjacents)
                    conectarVeins(r, c, type);
                }
            }
        }
    }

    private void conectarVeins(int r, int c, PlayerType myColor) {
        // Direccions en hexagonal (depèn de la implementació del profe,
        // normalment són (0,1), (0,-1), (1,0), (-1,0), (1,-1), (-1,1) en coordenades axials/offset)
        // Has de verificar quines són les coordenades adjacents correctes al Oust.
        // Aquí poso un exemple genèric:
        int[][] directions = {{0,1}, {0,-1}, {1,0}, {-1,0}, {1,-1}, {-1,1}}; 
        
        for (int[] d : directions) {
            int nr = r + d[0];
            int nc = c + d[1];
            
            // Verifiquem límits [cite: 66] (isInBounds no surt al resum però sol existir, 
            // si no, usem try-catch o lògica de límits manual)
            Point veiP = new Point(nr, nc);
            // Nota: isInBounds surt a la Pàgina 2 [cite: 40] -> isInBounds(Point p)
            if (this.isInBounds(veiP)) { 
                if (this.getColor(veiP) == myColor) {
                    ds.union(getIndex(r, c), getIndex(nr, nc));
                }
            }
        }
    }

    // Mapeig de (Row, Col) a Index (0..N)
    public int getIndex(int r, int c) {
        return r * this.cols + c;
    }
    
    // Getter per a la teva heurística
    public DisjointSet getDS() {
        return this.ds;
    }

}
