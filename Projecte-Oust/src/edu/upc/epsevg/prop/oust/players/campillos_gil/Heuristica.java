
package edu.upc.epsevg.prop.oust.players.campillos_gil;

import edu.upc.epsevg.prop.oust.Dir;
import edu.upc.epsevg.prop.oust.GameStatus;
import edu.upc.epsevg.prop.oust.GameStatusTunned;
import edu.upc.epsevg.prop.oust.PlayerType;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Heuristica {

    private static final int[][] dirsColindants = {{1,0}, {1,1}, {0,1}, {-1,0}, {-1,-1}, {0,-1}};
    private static final int[][] dirsIntermig = {{0,-2}, {1,-1}, {2,0}, {2,1}, {2,2}, {1,2}, {0,2}, {-1,1}, {-2,0}, {-2,-1}, {-2,-2}, {-1,-2}};
    private static final int[][] dirsExteriors = {{0,-3}, {2,-2}, {2,-1}, {3,0}, {3,1}, {3,2}, {3,3}, {2,3}, {1,3}, {0,3}, {-1,2}, {-2,1}, {-3,0}, {-3,-1}, {-3,-2}, {-3,-3}, {-2,-3}, {-1,-3}};


    public Heuristica() { }

    public int evaluarEstat(GameStatusTunned t, PlayerType jo) {

        // 1. Fi del joc
        if (t.isGameOver()) {
            PlayerType winner = t.GetWinner();
            return (winner == t.getCurrentPlayer()) ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        }

        DisjointSet ds = t.getDS(); 
        
        int size = t.getSquareSize();
        
        // 2. Deteccióde Fase
        int mida = t.getSize();
        boolean esApertura = ds.numNodes < mida * mida / 4;
        boolean esEndGame = ds.numNodes < mida * mida / 2;

        //PlayerType jo = t.getCurrentPlayer();
        // Calculem el rival
        PlayerType rival = (jo == PlayerType.PLAYER1) ? PlayerType.PLAYER2 : PlayerType.PLAYER1;

        if (esApertura) {
            return evaluarApertura(true, esEndGame, t, jo, rival, ds);
        } else {
         //   return evaluarCombate(t, ds, jo, rival);
         return evaluarApertura(false, esEndGame, t, jo, rival, ds);
        }
    }

    // --- FASE 1: APERTURA (Estratègia Posicional :) ) ---
    private int evaluarApertura(boolean esApertura, boolean esEndGame, GameStatusTunned t, PlayerType jo, PlayerType rival, DisjointSet ds) {
        int score = 0;
        int size = t.getSquareSize(); 
        int centre = size / 2;
        Point p = new Point(0, 0);

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {

                p.x = r; 
                p.y = c;

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
                    score += evaluarAmenazaInminent(esApertura, esEndGame, t, ds, p, jo);
                }
                else if (celda == rival) {
                    score -= valorPos;
                    score -= evaluarAmenazaInminent(esApertura, esEndGame, t, ds, p, jo);
                }
            }
        }
        int dif = t.diff();
        if (jo == PlayerType.PLAYER1){
            if (dif*dif != 1) score += dif*dif*dif*75;
            score += ds.puntuacio * ds.puntuacio * ds.puntuacio * ds.puntuacio * ds.puntuacio * 40;
        }
        else {
            if (dif*dif != 1) score -= dif*dif*dif*75;
            score -= ds.puntuacio * ds.puntuacio * ds.puntuacio * ds.puntuacio * ds.puntuacio * 40;
        }
        return score;
    }

    private int evaluarAmenazaInminent(boolean esApertura, boolean esEndGame, GameStatusTunned t, DisjointSet ds, Point p, PlayerType jugadorNostre) {
        
        int bonus = 0;
        PlayerType jo = t.getColor(p);
        
        int index = t.getIndex(p);
        int miTamanyo = ds.getSize(index);
        
        int contadorFitxesRivalsAmenaçants = 0;
        int contadorFitxesRivalsExteriors = 0;
        int casellesBuides = 0;
        int contadorFitxesMevesProperes = 0;
        int contadorFitxesMevesIntermig = 0;
        int contadorFitxesMevesExteriors = 0;

        List<Integer> veins = new ArrayList<>();
        for (int[] d : dirsColindants) {
            
            Point vei = new Point(p.x + d[0], p.y + d[1]);
            
            if (t.isInBounds(vei)) {
                PlayerType typeV = t.getColor(vei);
                
                if (typeV != null && typeV != jo) { // Evaluem casuistica vei (conlindant) que és fitxa rival del color que evaluem 
                    
                    contadorFitxesRivalsAmenaçants += 1;
                    int idx = t.getIndex(vei);
                    boolean nouVei = true;
                    for (int i=0; i<veins.size();i++){
                        
                        if (veins.get(i)==idx){
                            nouVei = false;
                            break;
                        }
                    }   
                    if (nouVei) veins.add(ds.find(idx));

                    int tamRival = ds.getSize(idx);
                    if (miTamanyo != tamRival) {
                        bonus += (miTamanyo - tamRival) * 75; 
                    } else {
                        if (t.getCurrentPlayer()==jugadorNostre){
                            if (jo == jugadorNostre) bonus += 50; // Podem matar al grup, tot i ser del mateix tamany
                            else bonus -= 50;
                        } else {
                            if (jo != jugadorNostre) bonus += 150; // Ens poden matar el grup, tot i ser del mateix tamany
                            else bonus -= 150;
                        }
                    }
                } else if (typeV == jo){
                    contadorFitxesMevesProperes += 1;

                    if (esEndGame){
                        bonus += miTamanyo * 5;
                    }
                }
            }
        }

        if (veins.size()!=1) bonus += veins.size()*300; // Estem permetent connectar
        
        for (int[] d : dirsIntermig) {
            
            Point vei = new Point(p.x + d[0], p.y + d[1]);
            
            if (t.isInBounds(vei)) {
                PlayerType typeV = t.getColor(vei);
                
                if (typeV != null) {
                    
                    int idx = t.getIndex(vei);
                    int tamVei = ds.getSize(idx);
                    if (typeV != jo) {
                        contadorFitxesRivalsAmenaçants += 1;
                        
                        // Lògica N+2: Si el nou grup es més gran, capturo
                        if (miTamanyo != tamVei) {
                            bonus += (miTamanyo - tamVei) * 75;
                        } else {
                            if (t.getCurrentPlayer() == jugadorNostre){
                                if (jo == jugadorNostre) bonus += 35; // Podem matar al grup, tot i ser del mateix tamany
                                else bonus -= 35;
                            } else {
                                if (jo != jugadorNostre) bonus += 200; // Podem matar al grup, tot i ser del mateix tamany
                                else bonus -= 200;
                            }
                        }
                    } else {
                        contadorFitxesMevesIntermig += 1;
                        if (esApertura){
                            bonus -= 10;
                        } else {
                            bonus -= 5;
                        }
                        
                        if (esEndGame){
                            bonus += (miTamanyo + tamVei) * 3;
                        }
                    }
                } else casellesBuides++;
            }
        }

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
                        else {
                            int idx = t.getIndex(vei);
                            int tamRival = ds.getSize(idx);
                            if (miTamanyo != tamRival) {
                                bonus += (miTamanyo - tamRival) * 5; 
                            }
                            bonus += 10;
                        }
                        contadorFitxesRivalsExteriors += 1;
                    }
                } //else casellesBuides++;
            }
        }
       

        int contadorFitxesMeves = contadorFitxesMevesProperes + contadorFitxesMevesIntermig + contadorFitxesMevesExteriors;

        if (contadorFitxesRivalsExteriors == 0 && contadorFitxesRivalsAmenaçants == 0 && contadorFitxesMeves == 0) {
            bonus += 30;
        } else if (contadorFitxesRivalsExteriors > 0 && contadorFitxesRivalsAmenaçants == 0 && contadorFitxesMeves == 0) {
            bonus += 20;
        } else if (contadorFitxesRivalsExteriors > 0 && contadorFitxesRivalsAmenaçants == 0){
            bonus += contadorFitxesRivalsExteriors * 5;
        }
        
        if (!esEndGame && (contadorFitxesMevesProperes > 0 || contadorFitxesMevesIntermig > 0 ) && contadorFitxesRivalsAmenaçants == 0 && contadorFitxesRivalsExteriors > 0){
            //malament tenir peça alla, la zona ja està defensada
            bonus -= 30;
        }   
        bonus += casellesBuides * 3;
        
        /* 
        int penalitzacioPerAlimentar = (int) (Math.pow(tamanyPotencialRival + 1, 3) * 10);
        if (jo == jugadorNostre) {
            bonus -= penalitzacioPerAlimentar;
        } else {
            bonus += penalitzacioPerAlimentar;
        }*/


        //int i = p.y * t.getSize() + p.x;
        //int gSize = ds.getSize(i);
        //if (jo==jugadorNostre) {
            //bonus += gSize * gSize * gSize * 100; // Exponencial per valorar més els grups grans
        //} else if (jo != null) {
            //bonus += gSize * gSize * gSize * 100; // Penalitzem més els grups grans enemics
        //}
        return bonus;
    }
}