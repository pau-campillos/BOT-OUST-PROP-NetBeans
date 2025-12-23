
package edu.upc.epsevg.prop.oust.players.campillos_gil;

import edu.upc.epsevg.prop.oust.GameStatus;
import edu.upc.epsevg.prop.oust.GameStatusTunned;
import edu.upc.epsevg.prop.oust.PlayerType;
import java.awt.Point;
import java.util.List;

public class Heuristica {
    public Heuristica() {

    }

    public int evaluarEstat(GameStatusTunned t) {

        // 1. Fin del juego
        if (t.isGameOver()) {
            PlayerType winner = t.GetWinner();
            return (winner == t.getCurrentPlayer()) ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        }

        // Hacemos el cast para usar tu estructura optimizada
        GameStatusTunned gst = (GameStatusTunned) t; // probablement no calgui AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
        DisjointSet ds = t.getDS(); 
        
        int size = t.getSquareSize();
        
        // 2. Detección de Fase
        // Miramos si el centro es NULL (vacío) para saber si estamos en apertura
        // boolean centroVacio = (t.getColor(new Point(size/2, size/2)) == null); --- foraaaa ---
        // También podemos mirar si hay pocas jugadas disponibles (tablero lleno) o muchas
        boolean esApertura = ds.numNodes > size * size / 4; // disseny nostre 

        PlayerType jo = t.getCurrentPlayer();
        // Calculamos el rival (asumiendo que solo hay PLAYER1 y PLAYER2)
        PlayerType rival = (jo == PlayerType.PLAYER1) ? PlayerType.PLAYER2 : PlayerType.PLAYER1;

        if (esApertura) {
            return evaluarApertura(t, jo, rival);
        } else {
            return evaluarCombate(t, ds, jo, rival);
        }
    }

    // --- FASE 1: APERTURA (Posicional) ---
    private int evaluarApertura(GameStatusTunned t, PlayerType jo, PlayerType rival) {
        int score = 0;
        int size = t.getSquareSize(); 
        int centre = size / 2;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Point p = new Point(r, c);
                if (!t.isInBounds(p)) continue;

                PlayerType celda = t.getColor(p);
                
                // CORRECCIÓN: Si es null, está vacía -> saltamos
                if (celda == null) continue;

                // Valorar cercanía al centro
                int dist = Math.abs(r - centre) + Math.abs(c - centre);
                int valorPos = (size - dist) * 50; 

                // Penalizar bordes extremos
                if (r == 0 || c == 0 || r == size - 1 || c == size - 1) {
                    valorPos -= 300;
                }

                if (celda == jo) score += valorPos;
                else if (celda == rival) score -= valorPos;
            }
        }
        return score;
    }

    // --- FASE 2: COMBATE (Táctica + Grupos) ---
    private int evaluarCombate(GameStatusTunned t, DisjointSet ds, PlayerType yo, PlayerType rival) {
        double score = 0;
        int size = t.getSquareSize();
        int totalCells = size * size;

        // A. EVALUACIÓN DE GRUPOS EXISTENTES
        for (int i = 0; i < totalCells; i++) {
            // Iteramos sobre el DisjointSet directamente
            if (ds.isRoot(i)) {
                int gSize = ds.getSize(i);
                if (gSize <= 0) continue; 

                // Recuperamos coordenadas para saber de quién es
                Point p = getPointFromIndex(i, size);
                
                // Verificamos límites por seguridad
                if (!t.isInBounds(p)) continue;
                
                PlayerType owner = t.getColor(p);
                
                // CORRECCIÓN: Verificamos que owner no sea null (por si acaso)
                if (owner != null) {
                    if (owner == yo) {
                        score += Math.pow(gSize, 2.2) * 10; 
                    } else if (owner == rival) {
                        score -= Math.pow(gSize, 2.2) * 12; 
                    }
                }
            }
        }

        // B. EVALUACIÓN DE AMENAZAS INMEDIATAS
        List<Point> moves = t.getMoves();
        for (Point p : moves) {
            score += evaluarAmenazaInminente(t, ds, p, yo, size);
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

    private double evaluarAmenazaInminente(GameStatusTunned t, DisjointSet ds, Point p, PlayerType yo, int sizeCols) {
        double bonus = 0;
        
        // Coordenadas axiales típicas o offsets para vecinos
        int[][] dirs = {{1,0}, {0,1}, {1,-1}, {-1,1}, {-1,0}, {0,-1}}; 

        int miTamanyoPotencial = 1;

        // 1. Calcular mi tamaño potencial
        for (int[] d : dirs) {
            Point vecino = new Point(p.x + d[0], p.y + d[1]);
            
            // CORRECCIÓN: Check de null antes de comparar color
            if (t.isInBounds(vecino)) {
                PlayerType colorVecino = t.getColor(vecino);
                if (colorVecino != null && colorVecino == yo) {
                    int idx = t.getIndex(vecino); 
                    miTamanyoPotencial += ds.getSize(idx); 
                }
            }
        }

        // 2. Ver si capturo (Oust)
        for (int[] d : dirs) {
            Point vecino = new Point(p.x + d[0], p.y + d[1]);
            
            if (t.isInBounds(vecino)) {
                PlayerType typeV = t.getColor(vecino);
                
                // CORRECCIÓN: Si typeV es null, es casilla vacía, no hay nada que comer
                if (typeV != null && typeV != yo) {
                    int idx = t.getIndex(vecino);
                    int tamRival = ds.getSize(idx);
                    
                    // Lógica N+2: Si mi nuevo grupo es mayor, capturo
                    if (miTamanyoPotencial > tamRival) {
                        bonus += (tamRival * 500) + 1000; 
                    }
                }
            }
        }
        
        return bonus;
    }
}