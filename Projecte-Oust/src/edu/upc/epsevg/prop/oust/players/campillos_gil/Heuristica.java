
package edu.upc.epsevg.prop.oust.players.campillos_gil;

import edu.upc.epsevg.prop.oust.Dir;
import edu.upc.epsevg.prop.oust.GameStatus;
import edu.upc.epsevg.prop.oust.GameStatusTunned;
import edu.upc.epsevg.prop.oust.PlayerType;
import java.awt.Point;
import java.util.List;

public class Heuristica {
    public Heuristica() {

    }

    public int evaluarEstat(GameStatusTunned t) {

        // 1. Fi del joc
        if (t.isGameOver()) {
            PlayerType winner = t.GetWinner();
            return (winner == t.getCurrentPlayer()) ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        }

        GameStatusTunned gst = (GameStatusTunned) t; // probablement no calgui AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
        DisjointSet ds = t.getDS(); 
        
        int size = t.getSquareSize();
        
        // 2. Deteccióde Fase
        boolean esApertura = ds.numNodes > size * size / 6; // disseny nostre 

        PlayerType jo = t.getCurrentPlayer();
        // Calculem el rival
        PlayerType rival = (jo == PlayerType.PLAYER1) ? PlayerType.PLAYER2 : PlayerType.PLAYER1;

        if (esApertura) {
            return evaluarApertura(t, jo, rival);
        } else {
            return evaluarCombate(t, ds, jo, rival);
        }
    }

    // --- FASE 1: APERTURA (Estratègia Posicional :) ) ---
    private int evaluarApertura(GameStatusTunned t, PlayerType jo, PlayerType rival) {
        int score = 0;
        int size = t.getSquareSize(); 
        int centre = size / 2;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {

                Point p = new Point(r, c);

                if (!t.isInBounds(p)) continue; // Si esta fora del tauler, saltem

                PlayerType celda = t.getColor(p);
                
                if (celda == null) continue; // Casella buida, no puntuem

                // Calculem valor segons posició
                int valorPos = 0; 
                
                int distanciaX = Math.abs(r - centre);
                int distanciaY = Math.abs(c - centre);

                if (r == 0 || c == 0 || r == size - 1 || c == size - 1) { // Penalitzem bordes
                    valorPos -= 300;
                } else if (distanciaX > 4 || distanciaY > 4){ // No volem exteriors
                    valorPos -= 200;
                } else if (distanciaX == 4 && distanciaY == 0){ // Volem cantonades estratègiques
                    valorPos = 500;
                } else if (distanciaX == 0 && distanciaY == 4){ // Volem cantonades estratègiques
                    valorPos = 500;
                } else if (distanciaX == 4 && distanciaY == 4){ // Volem cantonades estratègiques
                    valorPos = 500;
                } else if (distanciaX == 4 || distanciaY == 4){ // Volem cantonades estratègiques
                    valorPos = 300;
                } else if (distanciaX == 1 || distanciaY == 1){  // Cerquem el centreproperes al centre positiu
                    valorPos = 300;
                } else if (distanciaX == 0 || distanciaY == 0){ // Volem el centre
                    valorPos = 1000;
                } else {
                    valorPos = 200; // La resta adopten valors intermedis
                }
                
                //HEM DE REVISAR LES CASELLES CONTIGUES. SI TENIM AMENAÇES, SERÀ DOLENT!!!!!!!!!!!
                
                // Assignem puntuació segons qui ocupa la casella
                if (celda == jo) score += valorPos;
                else if (celda == rival) score -= valorPos;
            }
        }
        return score;
    }

    // --- FASE 2: COMBAT (Tàctica + Grups) ---
    private int evaluarCombate(GameStatusTunned t, DisjointSet ds, PlayerType jo, PlayerType rival) {
        double score = 0;
        int size = t.getSquareSize();
        int totalCells = size * size;

        // EVALUACIÓ DE GRUPS EXISTENTS I D'AMENACES IMMEDIATES
        for (int i = 0; i < totalCells; i++) {

            Point p = getPointFromIndex(i, size); // Calc (x,y) a partir de l'índex
            
            if (!t.isInBounds(p)) continue; // Si esta fora del tauler, saltem

            PlayerType owner = t.getColor(p); 
            
            // Iterem sobre el DisjointSet directament
            if (ds.isRoot(i)) {
                int gSize = ds.getSize(i);
                if (gSize <= 0) continue; // Si no té fitxes, saltem
                
                if (owner != null) { //
                    if (owner == jo) {
                        score += Math.pow(gSize, 2.2) * 10; // Exponencial per valorar més els grups grans
                    } else if (owner == rival) {
                        score -= Math.pow(gSize, 2.2) * 12; // Penalitzem més els grups grans enemics
                    }
                }
            }
        
            if (owner != null){
                score += evaluarAmenazaInminent(t, ds, p, jo, size);
            }
        }

        return (int) score;
    }

    // --- HELPERS ---

    private Point getPointFromIndex(int index, int cols) {
        int y = index / cols; // Fila (Row)
        int x = index % cols; // Columna (Col)
        return new Point(x, y); // El constructor de Point es (x=col, y=row) habitualmente, o (r,c) según la API.
        // Si Point se usa como (row, col) en tu API, sería new Point(y, x). 
        // Revisa GameStatusTunned.getIndex: return p.y * cols + p.x; 
        // Si p.y es fila (row) y p.x es columna (col), entonces:
        // index / cols = p.y (fila)
        // index % cols = p.x (columna)
        // new Point(x, y) es lo estándar en AWT, pero verifica si tu GameStatus espera (r,c).
        // Si falla, prueba new Point(y, x).
    }

    private double evaluarAmenazaInminent(GameStatusTunned t, DisjointSet ds, Point p, PlayerType yo, int sizeCols) {
        
        double bonus = 0;
        
        int index = t.getIndex(p);
        int miTamanyo = ds.getSize(index);

        // 1. Calcular mi tamaño potencial
        /* for (Dir d : Dir.values()) {
            Point vecino = d.add(p);
            
            // CORRECCIÓN: Check de null antes de comparar color
            if (t.isInBounds(vecino)) {
                PlayerType colorVecino = t.getColor(vecino);
                if (colorVecino != null && colorVecino == yo) {
                    int idx = t.getIndex(vecino); 
                    miTamanyoPotencial += ds.getSize(idx); 
                }
            }
        }*/

        // Veure possibles amenaces properes
        int[][] dirs = {{1,0}, {1,1}, {0,1}, {-1,0}, {-1,-1}, {0,-1}, {0,-2}, {1,-1}, 
                        {2,0}, {2,1}, {2,2}, {1,2}, {0,2}, {-1,1}, {-2,0}, {-2,-1}, {-2,-2}, {-1,-2}};
        for (int[] d : dirs) {
            
            Point vei = new Point(p.x + d[0], p.y + d[1]);
            
            if (t.isInBounds(vei)) {
                PlayerType typeV = t.getColor(vei);
                
                if (typeV != null && typeV != yo) {
                    int idx = t.getIndex(vei);
                    int tamRival = ds.getSize(idx);
                    
                    // Lògica N+2: Si el nou grup es més gran, capturo
                    if (miTamanyo != tamRival) {
                        bonus += (miTamanyo - tamRival * 500); 
                    } else {
                        bonus += 500; // Podem matar al grup, tot i ser del mateix tamany
                    }
                }
            }
        }
        
        return bonus;
    }
}