
package edu.upc.epsevg.prop.oust.players.campillos_gil;

import edu.upc.epsevg.prop.oust.Dir;
import edu.upc.epsevg.prop.oust.GameStatus;
import edu.upc.epsevg.prop.oust.players.campillos_gil.GameStatusTunned;
import edu.upc.epsevg.prop.oust.PlayerType;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Pau i Lidia
 */
public class Heuristica {

    // Posicions de les caselles del cercle més proper 
    private static final int[][] dirsColindants = {{1,0}, {1,1}, {0,1}, {-1,0}, {-1,-1}, {0,-1}}; 
    // Posicions de les caselles a 1 bloc de distància
    private static final int[][] dirsIntermig = {{0,-2}, {1,-1}, {2,0}, {2,1}, {2,2}, {1,2}, {0,2}, {-1,1}, {-2,0}, {-2,-1}, {-2,-2}, {-1,-2}}; 
    // Posicions de les caselles a 2 blocs de distància
    private static final int[][] dirsExteriors = {{0,-3}, {2,-2}, {2,-1}, {3,0}, {3,1}, {3,2}, {3,3}, {2,3}, {1,3}, {0,3}, {-1,2}, {-2,1}, {-3,0}, {-3,-1}, {-3,-2}, {-3,-3}, {-2,-3}, {-1,-3}};  

    /**
     * Constructor per defecte de la classe Heurística.
     * */
    public Heuristica() { }

    /**
     * Funció principal d'avaluació heurística.
     * Assigna una puntuació numèrica a l'estat actual del tauler des de la perspectiva del jugador 'jo' (nosaltres).
     * Aquesta funció actua com a distribuïdor d'estratègies:
     * 1. Comprova si la partida ha acabat (Victòria/Derrota immediata).
     * 2. Analitza la "densitat" del tauler per determinar la fase de joc (Obertura, Joc Mig o Final).
     * 3. Delega el càlcul detallat a 'evaluarFase', que aplicarà diferents pesos segons el moment de la partida.
     *
     * @param t L'estat actual del tauler.
     * @param jo El jugador per al qual estem maximitzant la puntuació.
     * @return Un enter (int) que representa la valoració de l'estat:
     * - Integer.MAX_VALUE si és victòria segura.
     * - Integer.MIN_VALUE si és derrota segura.
     * - Un valor heurístic ponderat en cas contrari.
     */
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

        // Calculem qui és el rival
        PlayerType rival = (jo == PlayerType.PLAYER1) ? PlayerType.PLAYER2 : PlayerType.PLAYER1;

        if (esApertura) {
            return evaluarFase(true, esEndGame, t, jo, rival, ds);
        } else {
            return evaluarFase(false, esEndGame, t, jo, rival, ds);
        }
    }

    /**
     * Avalua el tauler combinant estratègia posicional i recompte de material.
     * * Aquesta funció aplica diferents pesos segons si estem a l'obertura o al final de la partida.
     * * Conceptes clau:
     * 1. **Control de Zona:** Premia el centre i penalitza les vores (especialment cantonades mortes).
     * 2. **Material Quadràtic:** La diferència de fitxes s'eleva al quadrat per aumentar l'avantatge/desavantatge (joc agressiu).
     * 3. **Connectivitat:** Integra la puntuació del DisjointSet per valorar la solidesa dels grups.
     *
     * @param esApertura Indica si estem a l'inici (per prioritzar posicionament sobre captures).
     * @param esEndGame Indica si estem al final (per prioritzar material i territori definitiu).
     * @param t Estat del tauler.
     * @param jo Jugador actual (MAX).
     * @param rival Jugador contrari (MIN).
     * @param ds Estructura de conjunts disjunts per avaluar connectivitat.
     * @return Puntuació heurística total.
     */
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
        // Contemplem la diferència de peces (P1 - P2)
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

    /**
     * Analitza l'entorn immediat de la casella 'p' per detectar amenaces tàctiques.
     * Es basa en la comparació de mides de grups (DisjointSet) per determinar si:
     * 1. Podem capturar un grup rival (El nostre tamany > Tamany rival).
     * 2. Estem en perill de ser capturats (El Tamany rival > nostre tamany).
     * 3. Hi ha una situació de "vida o mort" (Tamanys iguals).
     *
     * @param esApertura Si estem a l'inici, som més conservadors.
     * @param esEndGame Si estem al final, som més agressius territorialment.
     * @param t Estat del tauler.
     * @param ds Estructura de conjunts disjunts (per saber mides de grups).
     * @param p La coordenada que estem avaluant.
     * @param jugadorNostre El jugador arrel (MAX).
     * @return Un valor (bonus o penalització) que s'afegirà a la puntuació de la casella.
     */
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
                        if (miTamanyo != tamVei) {
                            bonus += (miTamanyo - tamVei) * 75;
                        } else {
                            if (t.getCurrentPlayer() == jugadorNostre){
                                if (jo == jugadorNostre) bonus += 35; // Podem matar al grup, tot i ser del mateix tamany
                                else bonus -= 35;
                            } else {
                                if (jo != jugadorNostre) bonus += 200; // Ens poden matar al grup, tot i ser del mateix tamany
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
                } 
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
            // Valorem malament tenir peça alla, la zona ja està defensada
            bonus -= 30;
        }   
        bonus += casellesBuides * 3;
        return bonus;
    }
}