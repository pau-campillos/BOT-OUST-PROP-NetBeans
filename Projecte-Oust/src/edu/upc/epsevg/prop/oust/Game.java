package edu.upc.epsevg.prop.oust;

import edu.upc.epsevg.prop.oust.players.HumanPlayer;
import edu.upc.epsevg.prop.oust.players.RandomPlayer;
//import edu.upc.epsevg.prop.oust.players.H_E_X_Player;

import edu.upc.epsevg.prop.oust.players.campillos_gil.Oustlayer;
import javax.swing.SwingUtilities;

import javax.swing.SwingUtilities;

/**
 * Oust: el joc de taula.
 * @author Bernat
 */
public class Game {
    /**
     * @param args no usats
     */
    public static void main(String[] args) { 
         
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                
                //---------------------------------------------
                // Un parell de tontets jugant 
                //---------------------------------------------
                //IPlayer player1 = new RandomPlayer("Asterix");
                //IPlayer player2 = new RandomPlayer("Obelix");
                
                
                
                //---------------------------------------------
                // Deixem el tontet en mans d'una mala persona
                //---------------------------------------------
                //IPlayer player1 = new RandomPlayer("Asterix");
                //IPlayer player2 = new MalaOustiaPlayer();
                IPlayer player1 = new Oustlayer("Pau");
                
                
                //---------------------------------------------
                // Enjoy!
                //---------------------------------------------
                //IPlayer player1 = new MOustValuablePlayer();
                IPlayer player2 = new HumanPlayer("Human2");
                                
                
                //---------------------------------------------
                // Customitzeu els paràmetres
                //---------------------------------------------
                int midaCostat = 5;
                int timeoutEnSegons = 3;
                boolean pauseEnAutomatic = false;
                
                new Board(player1 , player2, midaCostat /*mida*/,  timeoutEnSegons/*s timeout*/, pauseEnAutomatic);
             }
        });
    }
}
