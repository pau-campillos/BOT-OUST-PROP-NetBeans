
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

    public int evaluarEstat(GameStatusTunned t, PlayerType jo) {

        // 1. Fi del joc
        if (t.isGameOver()) {
            PlayerType winner = t.GetWinner();
            return (winner == t.getCurrentPlayer()) ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        }

        GameStatusTunned gst = (GameStatusTunned) t; // probablement no calgui AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
        DisjointSet ds = t.getDS(); 
        
        int size = t.getSquareSize();
        
        // 2. Deteccióde Fase
        int mida = t.getSize();
        boolean esApertura = ds.numNodes < mida * mida / 4; // disseny nostre 

        //PlayerType jo = t.getCurrentPlayer();
        // Calculem el rival
        PlayerType rival = (jo == PlayerType.PLAYER1) ? PlayerType.PLAYER2 : PlayerType.PLAYER1;

        if (esApertura) {
            return evaluarApertura(true, t, jo, rival, ds);
        } else {
         //   return evaluarCombate(t, ds, jo, rival);
         return evaluarApertura(false, t, jo, rival, ds);
        }
    }

    // --- FASE 1: APERTURA (Estratègia Posicional :) ) ---
    private int evaluarApertura(boolean esApertura, GameStatusTunned t, PlayerType jo, PlayerType rival, DisjointSet ds) {
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

                if (distanciaX + distanciaY == t.getSize()-1) { // Penalitzem bordes
                    if (esApertura) valorPos -= 20;
                    else valorPos -= 5;
                } else if (distanciaX > 4 || distanciaY > 4){ // No volem exteriors
                    if (esApertura) valorPos -= 10;
                    else valorPos -= 2;
                } else if (esApertura){
                    if (distanciaX == 4 && distanciaY == 0){ // Volem cantonades estratègiques
                        valorPos = 30;
                    } else if (distanciaX == 0 && distanciaY == 4){ // Volem cantonades estratègiques
                        valorPos = 30;
                    } else if (distanciaX == 4 && distanciaY == 4){ // Volem cantonades estratègiques
                        valorPos = 30;
                    } else if (distanciaX == 4 || distanciaY == 4){ // Volem borera
                        valorPos = 20;
                    } else if (distanciaX == 1 || distanciaY == 1){  // Cerquem el centreproperes al centre positiu
                        valorPos = 20;
                    } else if (distanciaX == 0 && distanciaY == 0){ // Volem el centre
                        valorPos = 22;
                    } else {
                        valorPos = 15; // La resta adopten valors intermedis
                    }
                }
                
                //HEM DE REVISAR LES CASELLES CONTIGUES. SI TENIM AMENAÇES, SERÀ DOLENT!!!!!!!!!!!
                
                // Assignem puntuació segons qui ocupa la casella
                if (celda == jo) {
                    score += valorPos;
                    score += evaluarAmenazaInminent(esApertura, t, ds, p, jo);
                }
                else if (celda == rival) {
                    score -= valorPos;
                    score -= evaluarAmenazaInminent(esApertura, t, ds, p, jo);
                }
            }
        }

        if (jo == PlayerType.PLAYER1)
            score += t.diff()*100;
        else score -= t.diff()*100;
        
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
                score += evaluarAmenazaInminent(false, t, ds, p, jo);
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

    private int evaluarAmenazaInminent(boolean esApertura, GameStatusTunned t, DisjointSet ds, Point p, PlayerType jugadorNostre) {
        
        int bonus = 0;
        
        int index = t.getIndex(p);
        int miTamanyo = ds.getSize(index);

        PlayerType jo = t.getColor(p);

        // Veure possibles amenaces properes
        int[][] dirsColindants = {{1,0}, {1,1}, {0,1}, {-1,0}, {-1,-1}, {0,-1}};
        int[][] dirsIntermig = {{0,-2}, {1,-1}, {2,0}, {2,1}, {2,2}, {1,2}, {0,2}, {-1,1}, {-2,0}, {-2,-1}, {-2,-2}, {-1,-2}};
        
        int contadorFitxesRivalsAmenaçants = 0;
        int contadorFitxesRivalsExteriors = 0;
        int casellesBuides = 0;
        int contadorFitxesMevesProperes = 0;
        int contadorFitxesMevesIntermig = 0;
        int contadorFitxesMevesExteriors = 0;

        List<Integer> grupsRivalsVists = new java.util.ArrayList<>();
        int tamanyPotencialRival = 0;

        for (int[] d : dirsColindants) {
            
            Point vei = new Point(p.x + d[0], p.y + d[1]);
            
            if (t.isInBounds(vei)) {
                PlayerType typeV = t.getColor(vei);
                
                if (typeV != null && typeV != jo) {
                    contadorFitxesRivalsAmenaçants += 1;
                    int idx = t.getIndex(vei);
                    /* 
                    //  he fet canvissss aquiiiiiiiiiiiii
                    int rootRival = ds.find(idx);

                    if(!grupsRivalsVists.contains(rootRival)){
                        grupsRivalsVists.add(rootRival);
                        int tamRival = ds.getSize(rootRival);
                        
                        tamanyPotencialRival += tamRival;
                        // finssssssssss aquiiiiiiiiiiii
                    */
                        // Lògica N+2: Si el nou grup es més gran, capturo
                        int tamRival = ds.getSize(idx);
                        if (miTamanyo != tamRival) {
                            bonus += (miTamanyo - tamRival) * 50; 
                        } else {
                            if (t.getCurrentPlayer()==jugadorNostre){
                                if (jo == jugadorNostre) bonus += 30; // Podem matar al grup, tot i ser del mateix tamany
                                else bonus -= 35;
                            } else {
                                if (jo != jugadorNostre) bonus += 30; // Podem matar al grup, tot i ser del mateix tamany
                                else bonus -= 35;
                            }
                        }
                    
                } else if (typeV == jo){
                    contadorFitxesMevesProperes += 1;
                }
            }
        }
        
        for (int[] d : dirsIntermig) {
            
            Point vei = new Point(p.x + d[0], p.y + d[1]);
            
            if (t.isInBounds(vei)) {
                PlayerType typeV = t.getColor(vei);
                
                if (typeV != null) {
                    if (typeV != jo) {
                        contadorFitxesRivalsAmenaçants += 1;
                        int idx = t.getIndex(vei);
                        int tamRival = ds.getSize(idx);
                        
                        // Lògica N+2: Si el nou grup es més gran, capturo
                        if (miTamanyo != tamRival) {
                            bonus += (miTamanyo - tamRival) * 50; 
                        } else {
                            if (t.getCurrentPlayer() == jugadorNostre){
                                if (jo == jugadorNostre) bonus += 30; // Podem matar al grup, tot i ser del mateix tamany
                                else bonus -= 35;
                            } else {
                                if (jo != jugadorNostre) bonus += 30; // Podem matar al grup, tot i ser del mateix tamany
                                else bonus -= 35;
                            }
                        }
                    } else {
                        contadorFitxesMevesIntermig += 1;
                        if (esApertura){
                            bonus -= 10;
                        } else {
                            bonus -= 5;
                        }
                    }
                } else casellesBuides++;
            }
        }

        int[][] dirsExteriors = {{0,-3}, {2,-2}, {2,-1}, {3,0}, {3,1}, {3,2}, {3,3}, {2,3}, {1,3}, {0,3}, {-1,2}, {-2,1}, {-3,0}, {-3,-1}, {-3,-2}, {-3,-3}, {-2,-3}, {-1,-3}};

        for (int[] d : dirsExteriors) {
        
            Point vei = new Point(p.x + d[0], p.y + d[1]);
            
            if (t.isInBounds(vei)) {
                PlayerType typeV = t.getColor(vei);
                
                if (typeV != null) {
                    if (typeV == jo) {
                        if (esApertura) {
                            bonus += 5;
                        } else {
                            bonus -= 10;
                        }
                        contadorFitxesMevesExteriors += 1;
                    }
                    else {
                        if (esApertura)
                            bonus += 10;
                        else bonus += 10;
                        contadorFitxesRivalsExteriors += 1;
                    }
                } //else casellesBuides++;
            }
        }
        //if (esApertura) {
            int contadorFitxesMeves = contadorFitxesMevesProperes + contadorFitxesMevesIntermig + contadorFitxesMevesExteriors;

            if (contadorFitxesRivalsExteriors == 0 && contadorFitxesRivalsAmenaçants == 0 && contadorFitxesMeves == 0) {
                bonus += 30;
            } else if (contadorFitxesRivalsExteriors > 0 && contadorFitxesRivalsAmenaçants == 0 && contadorFitxesMeves == 0) {
                bonus += 20;
            } else if (contadorFitxesRivalsExteriors > 0 && contadorFitxesRivalsAmenaçants == 0){
                bonus += contadorFitxesRivalsExteriors * 5;
            }
            if ((contadorFitxesMevesProperes > 0 || contadorFitxesMevesIntermig > 0 ) && contadorFitxesRivalsAmenaçants == 0 && contadorFitxesRivalsExteriors > 0){
                //malament tenir peça alla, la zona ja està defensada
                bonus -= 30;
            }   
        //}
        bonus += casellesBuides * 3;
        
        /* 
        int penalitzacioPerAlimentar = (int) (Math.pow(tamanyPotencialRival + 1, 3) * 10);
        if (jo == jugadorNostre) {
            bonus -= penalitzacioPerAlimentar;
        } else {
            bonus += penalitzacioPerAlimentar;
        }*/
        /* 
        int i = p.y * t.getSize() + p.x;
        int gSize = ds.getSize(i);
        if (jo==jugadorNostre) {
            bonus += Math.pow(gSize, 2.2) * 20; // Exponencial per valorar més els grups grans
        } else if (jo != null) {
            bonus += Math.pow(gSize, 2.2) * 100; // Penalitzem més els grups grans enemics
        }
        */
        return bonus;
    }
}