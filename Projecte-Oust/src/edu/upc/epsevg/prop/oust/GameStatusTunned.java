
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
    public void placeStone(Point point) {

        int difFitxes = diff();

        super.placeStone(point); 
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
            PlayerType jugadorTirada = this.getColor(point);
            int indexActual = ds.calculaIndex(point);

            // Unim amb els veïns del mateix color
            conectarVeins(point.y, point.x, jugadorTirada);
        }
        //...
        // les vostes cosetes...
    }

    
    private void conectarVeins(int r, int c, PlayerType myColor) {
        // Direccions en hexagonal (depèn de la implementació del profe,
        // normalment són (0,1), (0,-1), (1,0), (-1,0), (1,-1), (-1,1) en coordenades axials/offset)
        // Has de verificar quines són les coordenades adjacents correctes al Oust.
        // Aquí poso un exemple genèric:

        for (Dir d : Dir.values()) {
            int nr = r + d[0];
            int nc = c + d[1];
            
            // Verifiquem límits
            Point veiP = new Point(nr, nc);
            if (this.isInBounds(veiP)) { 
                if (this.getColor(veiP) == myColor) {
                    ds.union(getIndex(r, c), getIndex(nr, nc));
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

                if (this.isInBounds(new Point(r, c)) == false) continue;

                PlayerType type = this.getColor(r, c);
                
                if (type != null) {
                    
                    // Unim amb els veïns que ja hem visitat 
                    // (per exemple, només cal mirar amunt i esquerra si recorrem en ordre, 
                    // però en hexagonal és més segur mirar els veïns vàlids adjacents)
                    conectarVeins(r, c, type);
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

        
    /*
    public int calculaIndex (Point p){
            // {Índice} =(fila * ancho_del_tablero) + (columna)
            return (p.y * getSquareSize) + p.x;
        }   
    */
}