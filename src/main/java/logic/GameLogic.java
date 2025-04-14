package logic;

import java.util.*;
import model.Group;
import model.Tile;

public class GameLogic {

    /**
     * Znajduje wszystkie poprawne grupy z danych kart
     */
    public List<Group> findValidGroups(List<Tile> hand) {
        List<Group> validGroups = new ArrayList<>();

        // Znajdź wszystkie możliwe zestawy (karty o tej samej wartości)
        findValidSets(hand, validGroups);

        // Znajdź wszystkie możliwe sekwencje (kolejne liczby tego samego koloru)
        findValidRuns(hand, validGroups);

        return validGroups;
    }

    // Znajduje wszystkie poprawne zestawy (tej samej wartości, różne kolory)
    private void findValidSets(List<Tile> hand, List<Group> validGroups) {
        // Grupuj karty według wartości
        Map<Integer, List<Tile>> tilesByValue = new HashMap<>();
        List<Tile> jokers = new ArrayList<>();

        for (Tile tile : hand) {
            if (tile.isJoker()) {
                jokers.add(tile);
            } else {
                tilesByValue.computeIfAbsent(tile.getVal(), k -> new ArrayList<>()).add(tile);
            }
        }

        // Dla każdej wartości, sprawdź możliwe zestawy
        for (Map.Entry<Integer, List<Tile>> entry : tilesByValue.entrySet()) {
            List<Tile> tiles = entry.getValue();

            // Usuń duplikaty kolorów
            Map<String, Tile> uniqueColorTiles = new HashMap<>();
            for (Tile tile : tiles) {
                uniqueColorTiles.put(tile.getColor(), tile);
            }

            List<Tile> uniqueTiles = new ArrayList<>(uniqueColorTiles.values());

            // Jeśli mamy co najmniej 2 karty i możemy dodać jokery aby osiągnąć 3+
            if (uniqueTiles.size() >= 2 && uniqueTiles.size() + jokers.size() >= 3 && uniqueTiles.size() <= 4) {
                // Sprawdź, ile jokerów potrzebujemy, żeby mieć przynajmniej 3 karty
                int jokersNeeded = Math.max(0, 3 - uniqueTiles.size());

                if (jokers.size() >= jokersNeeded) {
                    List<Tile> groupTiles = new ArrayList<>(uniqueTiles);
                    // Dodaj potrzebną liczbę jokerów
                    for (int i = 0; i < jokersNeeded; i++) {
                        groupTiles.add(jokers.get(i));
                    }
                    validGroups.add(new Group(groupTiles));

                    // Jeśli mamy więcej jokerów i jest miejsce w zestawie (max 4 karty)
                    for (int i = jokersNeeded; i < jokers.size() && uniqueTiles.size() + i < 4; i++) {
                        List<Tile> extendedGroup = new ArrayList<>(groupTiles);
                        extendedGroup.add(jokers.get(i));
                        validGroups.add(new Group(extendedGroup));
                    }
                }
            }
        }
    }

    // Znajduje wszystkie poprawne sekwencje (kolejne liczby tego samego koloru)
    private void findValidRuns(List<Tile> hand, List<Group> validGroups) {
        // Grupuj karty według koloru
        Map<String, List<Tile>> tilesByColor = new HashMap<>();
        List<Tile> jokers = new ArrayList<>();

        for (Tile tile : hand) {
            if (tile.isJoker()) {
                jokers.add(tile);
            } else {
                tilesByColor.computeIfAbsent(tile.getColor(), k -> new ArrayList<>()).add(tile);
            }
        }

        // Dla każdego koloru, znajdź możliwe sekwencje
        for (Map.Entry<String, List<Tile>> entry : tilesByColor.entrySet()) {
            List<Tile> colorTiles = entry.getValue();

            // Usuń duplikaty wartości
            Map<Integer, Tile> uniqueValueTiles = new HashMap<>();
            for (Tile tile : colorTiles) {
                uniqueValueTiles.put(tile.getVal(), tile);
            }

            List<Tile> uniqueTiles = new ArrayList<>(uniqueValueTiles.values());

            // Sortuj karty według wartości
            uniqueTiles.sort(Comparator.comparingInt(Tile::getVal));

            // Jeśli mamy co najmniej 2 karty
            if (uniqueTiles.size() >= 2) {
                // Znajdź wszystkie możliwe sekwencje
                for (int start = 0; start < uniqueTiles.size(); start++) {
                    findRunsStartingAt(uniqueTiles, start, jokers, validGroups);
                }
            }
        }
    }

    // Znajduje możliwe sekwencje zaczynające się od danej pozycji
    private void findRunsStartingAt(List<Tile> sortedTiles, int startIdx, List<Tile> jokers, List<Group> validGroups) {
        // Minimum 2 karty zwykłe aby utworzyć sekwencję z jokerami
        if (startIdx >= sortedTiles.size() - 1) {
            return;
        }

        for (int endIdx = startIdx + 1; endIdx < sortedTiles.size(); endIdx++) {
            // Oblicz ile jokerów potrzeba aby wypełnić luki
            int gaps = 0;
            for (int i = startIdx + 1; i <= endIdx; i++) {
                gaps += sortedTiles.get(i).getVal() - sortedTiles.get(i-1).getVal() - 1;
            }

            // Jeśli mamy wystarczająco jokerów i łącznie będzie co najmniej 3 karty
            if (gaps <= jokers.size() && (endIdx - startIdx + 1 + gaps) >= 3) {
                List<Tile> runTiles = new ArrayList<>();

                // Dodaj pierwszą kartę
                runTiles.add(sortedTiles.get(startIdx));

                // Dodaj karty i jokery do wypełnienia luk
                for (int i = startIdx + 1; i <= endIdx; i++) {
                    int currentVal = sortedTiles.get(i-1).getVal();
                    int nextVal = sortedTiles.get(i).getVal();

                    // Dodaj jokery do wypełnienia luki
                    for (int j = 1; j < nextVal - currentVal && runTiles.size() < 13; j++) {
                        if (!jokers.isEmpty()) {
                            runTiles.add(jokers.get(0)); // Używamy pierwszego jokera
                        }
                    }

                    // Dodaj następną zwykłą kartę
                    runTiles.add(sortedTiles.get(i));
                }

                // Sprawdź czy sekwencja ma co najmniej 3 karty
                if (runTiles.size() >= 3) {
                    validGroups.add(new Group(runTiles));
                }
            }
        }
    }
}