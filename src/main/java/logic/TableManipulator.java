package logic;

import java.util.*;
import model.*;

public class TableManipulator {
    private GroupValidator validator;

    public TableManipulator() {
        this.validator = new GroupValidator();
    }

    public List<List<Group>> findValidRearrangements(List<Group> possibleGroups, List<Tile> allTiles) {
        List<List<Group>> validArrangements = new ArrayList<>();
        Set<String> arrangementSignatures = new HashSet<>();

        // Generujemy wszystkie możliwe podzbiory grup, które używają dokładnie tych kafelków
        findValidArrangementsHelper(possibleGroups, new ArrayList<>(), new HashSet<>(), 0, validArrangements,
                arrangementSignatures, new HashSet<>(allTiles));

        return validArrangements;
    }

    /**
     * Metoda pomocnicza do znajdowania poprawnych aranżacji
     */
    private void findValidArrangementsHelper(
            List<Group> allGroups,
            List<Group> currentArrangement,
            Set<Tile> usedTiles,
            int startIndex,
            List<List<Group>> validArrangements,
            Set<String> arrangementSignatures,
            Set<Tile> requiredTiles) {

        // Sprawdź, czy aktualne ułożenie używa wszystkich wymaganych kafelków
        if (usedTiles.equals(requiredTiles)) {
            String signature = generateSolutionSignature(currentArrangement);
            if (!arrangementSignatures.contains(signature)) {
                validArrangements.add(new ArrayList<>(currentArrangement));
                arrangementSignatures.add(signature);
            }
            return;
        }

        // Jeśli używamy więcej kafelków niż wymagane, to ułożenie jest niepoprawne
        if (usedTiles.size() > requiredTiles.size()) {
            return;
        }

        // Sprawdź, czy używamy jakiegoś kafelka, którego nie ma w wymaganych
        for (Tile usedTile : usedTiles) {
            boolean found = false;
            for (Tile requiredTile : requiredTiles) {
                if (usedTile.equals(requiredTile)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return;
            }
        }

        // Przejdź przez pozostałe grupy
        for (int i = startIndex; i < allGroups.size(); i++) {
            Group group = allGroups.get(i);
            boolean canAdd = true;
            Set<Tile> newTiles = new HashSet<>();

            // Sprawdź, czy jakakolwiek karta jest już używana
            for (Tile tile : group.getTiles()) {
                if (usedTiles.contains(tile)) {
                    canAdd = false;
                    break;
                }
                newTiles.add(tile);
            }

            if (canAdd) {
                // Dodaj grupę do aranżacji
                currentArrangement.add(group);
                usedTiles.addAll(newTiles);

                // Rekurencyjnie próbuj dodać więcej grup
                findValidArrangementsHelper(allGroups, currentArrangement, usedTiles, i + 1,
                        validArrangements, arrangementSignatures, requiredTiles);

                // Backtrack - usuń grupę
                currentArrangement.remove(currentArrangement.size() - 1);
                usedTiles.removeAll(newTiles);
            }
        }
    }

    /**
     * Generuje unikalną sygnaturę dla rozwiązania
     */
    private String generateSolutionSignature(List<Group> solution) {
        List<String> groupSignatures = new ArrayList<>();

        for (Group group : solution) {
            List<String> tileStrings = group.getTiles().stream()
                    .map(Tile::toString)
                    .sorted()
                    .collect(java.util.stream.Collectors.toList());
            groupSignatures.add(String.join(",", tileStrings));
        }

        Collections.sort(groupSignatures);
        return String.join("|", groupSignatures);
    }

    public boolean isValidTableState(List<Group> groups) {
        // Check if each group is valid
        for (Group group : groups) {
            if (!GroupValidator.isValidGroup(group.getTiles())) {
                return false;
            }
        }
        return true;
    }
}