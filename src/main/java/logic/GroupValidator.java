package logic;

import java.util.*;
import model.Tile;

public class GroupValidator {

    /**
     * Sprawdza, czy grupa tworzy poprawny układ według zasad Rummikub
     */
    public static boolean isValidGroup(List<Tile> tiles) {
        if (tiles.size() < 3) {
            return false;
        }

        // Oddziel jokery od zwykłych kart
        List<Tile> jokers = new ArrayList<>();
        List<Tile> regularTiles = new ArrayList<>();

        for (Tile tile : tiles) {
            if (tile.isJoker()) {
                jokers.add(tile);
            } else {
                regularTiles.add(tile);
            }
        }

        int jokerCount = jokers.size();

        // Tylko jokery nie mogą tworzyć grupy
        if (regularTiles.isEmpty()) {
            return false;
        }

        // Sprawdź, czy to zestaw kart o tej samej wartości
        if (isValidSet(regularTiles, jokerCount)) {
            return true;
        }

        // Sprawdź, czy to sekwencja kolejnych liczb
        if (isValidRun(regularTiles, jokerCount)) {
            return true;
        }

        return false;
    }

    /**
     * Sprawdza, czy karty tworzą poprawny zestaw o tej samej wartości i różnych kolorach
     */
    public static boolean isValidSet(List<Tile> regularTiles, int jokerCount) {
        if (regularTiles.isEmpty()) {
            return false;
        }

        // Wszystkie zwykłe karty muszą mieć tę samą wartość
        int value = regularTiles.get(0).getVal();
        Set<String> usedColors = new HashSet<>();

        for (Tile tile : regularTiles) {
            // Różne wartości oznaczają, że to nie jest poprawny zestaw
            if (tile.getVal() != value) {
                return false;
            }

            // Nie może być duplikatów kolorów w zestawie
            if (!usedColors.add(tile.getColor())) {
                return false;
            }
        }

        // Potrzebujemy co najmniej 3 kart w sumie (zwykłe + jokery)
        // I nie może być więcej niż 4 karty (maksymalnie 4 kolory)
        int totalSize = regularTiles.size() + jokerCount;
        return totalSize >= 3 && totalSize <= 4;
    }

    /**
     * Sprawdza, czy karty tworzą poprawną sekwencję kolejnych wartości w tym samym kolorze
     */
    public static boolean isValidRun(List<Tile> regularTiles, int jokerCount) {
        if (regularTiles.isEmpty()) {
            return false;
        }

        // Sprawdźmy, czy wszystkie karty są tego samego koloru
        String color = regularTiles.get(0).getColor();
        boolean sameColor = regularTiles.stream().allMatch(t -> t.getColor().equals(color));

        if (!sameColor) {
            return false; // Sekwencja musi być tego samego koloru
        }

        // Sortuj karty według wartości
        regularTiles.sort(Comparator.comparingInt(Tile::getVal));

        // Sprawdź, czy nie ma duplikatów
        for (int i = 1; i < regularTiles.size(); i++) {
            if (regularTiles.get(i).getVal() == regularTiles.get(i-1).getVal()) {
                return false; // Nie może być duplikatów w sekwencji
            }
        }

        // Oblicz ile jokerów potrzeba do wypełnienia luk
        int jokersNeeded = 0;
        for (int i = 1; i < regularTiles.size(); i++) {
            int gap = regularTiles.get(i).getVal() - regularTiles.get(i-1).getVal() - 1;
            if (gap < 0) {
                return false; // Negatywna luka oznacza nieprawidłową sekwencję
            }
            jokersNeeded += gap;
        }

        // Sprawdź czy mamy wystarczająco jokerów do wypełnienia luk
        return jokersNeeded <= jokerCount && (regularTiles.size() + jokerCount >= 3);
    }

    /**
     * Sprawdza, czy wszystkie karty mają tę samą wartość
     */
    public static boolean isSetGroup(List<Tile> regularTiles) {
        if (regularTiles.isEmpty()) {
            return false;
        }

        int value = regularTiles.get(0).getVal();
        return regularTiles.stream().allMatch(t -> t.getVal() == value);
    }
}