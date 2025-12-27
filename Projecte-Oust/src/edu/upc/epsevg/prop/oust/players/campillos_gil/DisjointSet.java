package edu.upc.epsevg.prop.oust.players.campillos_gil;

import edu.upc.epsevg.prop.oust.PlayerType;
import java.awt.Point;

public class DisjointSet {

    // Estructura Union-Find
    private int[] parent;          // Padre de cada nodo
    private int[] size;            // Tamaño del grupo
    public int numNodes;
    public int puntuacio;

    /**
     * Constructor
     * @param totalNodes Número total de casillas del tablero
     */

    public DisjointSet(DisjointSet original) {
        this.parent = new int[original.parent.length];
        this.size = new int[original.size.length]; // O rank, o el que tinguis
        this.numNodes = original.numNodes;
        this.puntuacio = original.puntuacio;
        
        // La màgia de Java: còpia de memòria a memòria (molt ràpid)
        System.arraycopy(original.parent, 0, this.parent, 0, original.parent.length);
        System.arraycopy(original.size, 0, this.size, 0, original.size.length);
        // Copia altres atributs si en tens (numSets, etc.)
    }

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
     * FIND con compresión de camino
     */
    public int find(int i) {
        if (parent[i] != i) {
            parent[i] = find(parent[i]);
        }
        return parent[i];
    }

    /**
     * UNION por tamaño
     */
    public void union(int i, int j) {
        int rootA = find(i);
        int rootB = find(j);

        if (rootA == rootB) return;

        // Unimos el árbol pequeño al grande
        if (size[rootA] < size[rootB]) {
            int tmp = rootA;
            rootA = rootB;
            rootB = tmp;
        }

        parent[rootB] = rootA;
        size[rootA] += size[rootB];
    }

    /**
     * Devuelve el tamaño del grupo al que pertenece i
     */
    public int getSize(int i) {
        int root = find(i);
        return size[root];
    }

    /**
     * Indica si i es raíz de grupo
     */
    public boolean isRoot(int i) {
        return parent[i] == i;
    }
}


