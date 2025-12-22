package edu.upc.epsevg.prop.oust.players.campillos_gil;

import edu.upc.epsevg.prop.oust.PlayerType;
import java.awt.Point;

public class DisjointSet {

    // Estructura Union-Find
    private int[] parent;          // Padre de cada nodo
    private int[] size;            // Tamaño del grupo
    private PlayerType[] owner;    // Dueño del grupo (solo válido en la raíz)

    /**
     * Constructor
     * @param totalNodes Número total de casillas del tablero
     */
    public DisjointSet(int totalNodes) {
        parent = new int[totalNodes];
        size = new int[totalNodes];
        owner = new PlayerType[totalNodes];

        for (int i = 0; i < totalNodes; i++) {
            parent[i] = i;
            size[i] = 1;
            owner[i] = null; // grupo vacío
        }
    }

    public int calculaIndex (Point p){
        // {Índice} =(fila * ancho_del_tablero) + (columna)
        return (p.y * (int)Math.sqrt(parent.length)) + p.x;
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

        // Propagamos el owner
        if (owner[rootA] == null) {
            owner[rootA] = owner[rootB];
        }
    }

    /**
     * Asigna el dueño del grupo al que pertenece i
     */
    public void setOwner(int i, PlayerType player) {
        int root = find(i);
        owner[root] = player;
    }

    /**
     * Devuelve el dueño del grupo al que pertenece i
     */
    public PlayerType getOwner(int i) {
        int root = find(i);
        return owner[root];
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


