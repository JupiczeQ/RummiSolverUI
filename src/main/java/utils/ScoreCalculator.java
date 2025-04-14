package utils;

import java.util.*;
import model.Group;
import model.Tile;

public class ScoreCalculator {

    /**
     * Oblicza całkowitą wartość punktową listy grup
     */
    public int calculateTotalValue(List<Group> groups) {
        int total = 0;
        Set<Tile> countedTiles = new HashSet<>();

        for (Group g : groups) {
            for (Tile t : g.getTiles()) {
                if (countedTiles.add(t)) { // Licz każdą kartę tylko raz
                    if (!t.isJoker()) {
                        total += t.getVal();
                    } else {
                        // Joker przybiera wartość zależną od grupy
                        total += g.getJokerValue(t);
                    }
                }
            }
        }

        return total;
    }

    /**
     * Liczy liczbę unikalnych kafelków w grupach
     */
    public int countUniqueTiles(List<Group> groups) {
        Set<Tile> uniqueTiles = new HashSet<>();
        for (Group g : groups) {
            uniqueTiles.addAll(g.getTiles());
        }
        return uniqueTiles.size();
    }
}