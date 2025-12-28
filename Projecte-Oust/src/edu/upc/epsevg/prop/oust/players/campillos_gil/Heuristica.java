
package edu.upc.epsevg.prop.oust.players.campillos_gil;

import edu.upc.epsevg.prop.oust.Dir;
import edu.upc.epsevg.prop.oust.GameStatus;
import edu.upc.epsevg.prop.oust.players.campillos_gil.GameStatusTunned;
import edu.upc.epsevg.prop.oust.PlayerType;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Heuristica {

    private static final int[][] dirsColindants = {{1,0}, {1,1}, {0,1}, {-1,0}, {-1,-1}, {0,-1}}; // posicions de les caselles del cercle més proper 
    private static final int[][] dirsIntermig = {{0,-2}, {1,-1}, {2,0}, {2,1}, {2,2}, {1,2}, {0,2}, {-1,1}, {-2,0}, {-2,-1}, {-2,-2}, {-1,-2}};
    private static final int[][] dirsExteriors = {{0,-3}, {2,-2}, {2,-1}, {3,0}, {3,1}, {3,2}, {3,3}, {2,3}, {1,3}, {0,3}, {-1,2}, {-2,1}, {-3,0}, {-3,-1}, {-3,-2}, {-3,-3}, {-2,-3}, {-1,-3}};

    public Heuristica() { }

    public int evaluarEstat(GameStatusTunned t, PlayerType jo) {

        // Comprovació per si s'ha acabat el joc
        if (t.isGameOver()) {
            PlayerType winner = t.GetWinner();
            return (winner == jo) ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        }
        DisjointSet ds = t.getDS(); 
        int size = t.getSquareSize();

        // Detecció de Fase en la que estem
        int mida = t.getSize();
        boolean esApertura = ds.numNodes < mida * mida / 4;
        boolean esEndGame = ds.numNodes < mida * mida / 2;

        // Calculem qui és eñ rival
        PlayerType rival = (jo == PlayerType.PLAYER1) ? PlayerType.PLAYER2 : PlayerType.PLAYER1;

        if (esApertura) {
            return evaluarFase(true, esEndGame, t, jo, rival, ds);
        } else {
            return evaluarFase(false, esEndGame, t, jo, rival, ds);
        }
    }

    private int evaluarFase(boolean esApertura, boolean esEndGame, GameStatusTunned t, PlayerType jo, PlayerType rival, DisjointSet ds) {
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
                    } else if (distanciaX == 1 || distanciaY == 1){ // Volem caselles properes al centre
                        valorPos = 20;
                    } else if (distanciaX == 0 && distanciaY == 0){ // Volem el centre
                        valorPos = 22;
                    } else {
                        valorPos = 15; // La resta adopten valors intermedis
                    }
                }
                
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
            if (dif > 0) score += dif*dif*350;
            else score -= dif*dif*50;
            if (ds.puntuacio > 0) score += ds.puntuacio * ds.puntuacio * 20;
            else score -= ds.puntuacio * ds.puntuacio * 20;
        }
        else {
            if (dif < 0) score += dif*dif*350;
            else score -= dif*dif*50;
            if (ds.puntuacio < 0) score += ds.puntuacio * ds.puntuacio * 20;
            else score -= ds.puntuacio * ds.puntuacio * 20;
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
        int perillveins=0;
        Point vei = new Point();
        for (int[] d : dirsColindants) {
            
            vei.x = p.x + d[0];
            vei.y = p.y + d[1];
            
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
                    if (nouVei) {
                        veins.add(ds.find(idx));
                        perillveins += ds.getSize(idx);
                    }

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
                        bonus += miTamanyo * 7;
                    }
                }
            }
        }

        if (!esApertura && veins.size() > 1) bonus += veins.size()*perillveins; // Estem permetent connectar
        
        veins.clear();
        perillveins = 0;
        
        for (int[] d : dirsIntermig) {
            
            vei.x = p.x + d[0];
            vei.y = p.y + d[1];
            
            if (t.isInBounds(vei)) {
                PlayerType typeV = t.getColor(vei);
                
                if (typeV != null) {
                    
                    int idx = t.getIndex(vei);
                    int tamVei = ds.getSize(idx);
                    if (typeV != jo) {
                        contadorFitxesRivalsAmenaçants += 1;
                        
                        boolean nouVei = true;
                        for (int i=0; i<veins.size();i++){
                            
                            if (veins.get(i)==idx){
                                nouVei = false;
                                break;
                            }
                        }   
                        if (nouVei) {
                            veins.add(ds.find(idx));
                            perillveins += ds.getSize(idx);
                        }

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

        if (!esApertura && veins.size() > 1) bonus += veins.size() * perillveins * 15;

        for (int[] d : dirsExteriors) {
        
            vei.x = p.x + d[0];
            vei.y = p.y + d[1];
            
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
        return bonus;
    }
}