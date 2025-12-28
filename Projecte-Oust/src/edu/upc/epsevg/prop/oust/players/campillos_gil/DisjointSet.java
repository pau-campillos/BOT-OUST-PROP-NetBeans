package edu.upc.epsevg.prop.oust.players.campillos_gil;

import edu.upc.epsevg.prop.oust.PlayerType;
import java.awt.Point;

public class DisjointSet {

    private int[] parent;          // Pare de cada node
    private int[] size;            // Mida del grup
    public int numNodes;
    public int puntuacio;

    /**
     * Constructor per còpia.
     * Crea una rèplica exacta de l'estructura DisjointSet utilitzant còpia directa de memòria.
     * És essencial per clonar estats ràpidament dins de l'algorisme Minimax sense afectar l'original.
     * @param original L'estructura de DisjointSet que es vol duplicar.
     */
    public DisjointSet(DisjointSet original) {
        this.parent = new int[original.parent.length];
        this.size = new int[original.size.length];
        this.numNodes = original.numNodes;
        this.puntuacio = original.puntuacio;
        
        System.arraycopy(original.parent, 0, this.parent, 0, original.parent.length);
        System.arraycopy(original.size, 0, this.size, 0, original.size.length);
    }


    /**
     * Constructor principal. Inicialitza l'estructura DisjointSet per a un nombre determinat de nodes.
     * Configura l'estat inicial on cada node és el seu propi pare i forma un conjunt de mida 1.
     * @param totalNodes El nombre total d'elements (caselles del tauler).
     */
    public DisjointSet(int totalNodes) {
        parent = new int[totalNodes];
        size = new int[totalNodes];
        numNodes = 0;
        puntuacio = 0;
        for (int i = 0; i < totalNodes; i++) {
            parent[i] = i;
            size[i] = 1;
        }
    }
     
    /**
     * Troba el representant (arrel) del conjunt al qual pertany l'element 'i'.
     * @param i L'índex de l'element que volem cercar.
     * @return L'índex del node arrel que identifica el conjunt únic.
     */
    public int find(int i) {
        if (parent[i] != i) {
            parent[i] = find(parent[i]);
        }
        return parent[i];
    }

    /**
     * Fusiona els dos conjunts als quals pertanyen els elements 'i' i 'j'.
     * @param i L'índex del primer element a unir.
     * @param j L'índex del segon element a unir.
     */
    public void union(int i, int j) {
        int rootA = find(i);
        int rootB = find(j);
        if (rootA == rootB) return;
        // unim l'arbre petit al gran
        if (size[rootA] < size[rootB]) {
            int tmp = rootA;
            rootA = rootB;
            rootB = tmp;
        }
        parent[rootB] = rootA;
        size[rootA] += size[rootB];
    }

    /**
     * Retorna la mida (nombre d'elements) del conjunt al qual pertany l'element especificat.
     * @param i L'índex de l'element que volem consultar.
     * @return Un enter amb la quantitat total de nodes que formen aquest grup.
     */
    public int getSize(int i) {
        int root = find(i);
        return size[root];
    }
}


