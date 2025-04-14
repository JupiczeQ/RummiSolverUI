package model;

import java.util.*;
import java.util.stream.Collectors;
import logic.GroupValidator;

public class Group {
    private List<Tile> tiles;

    public Group() {
        this.tiles = new ArrayList<>();
    }

    public Group(List<Tile> tiles) {
        this.tiles = new ArrayList<>(tiles);
    }

    public void addTile(Tile tile) {
        tiles.add(tile);
    }

    public List<Tile> getTiles() {
        return new ArrayList<>(tiles);
    }

    /**
     * Określa, jaką wartość reprezentuje joker w tej grupie
     */
    public int getJokerValue(Tile joker) {
        if (!joker.isJoker()) {
            return joker.getVal();
        }

        List<Tile> regularTiles = tiles.stream()
                .filter(t -> !t.isJoker())
                .collect(Collectors.toList());

        if (regularTiles.isEmpty()) {
            return 0; // Nie można określić wartości bez zwykłych kart
        }

        // Sprawdź, czy to zestaw wartości (ta sama wartość, różne kolory)
        boolean isValueSet = GroupValidator.isSetGroup(regularTiles);

        if (isValueSet) {
            return regularTiles.get(0).getVal(); // Joker reprezentuje tę samą wartość
        } else {
            // To musi być sekwencja
            return getJokerValueInRun(joker, regularTiles);
        }
    }

    private int getJokerValueInRun(Tile joker, List<Tile> regularTiles) {
        // Sprawdź czy wszystkie karty są tego samego koloru
        String color = regularTiles.get(0).getColor();
        boolean sameColor = regularTiles.stream().allMatch(t -> t.getColor().equals(color));

        if (!sameColor) {
            // Jeśli to nie jest poprawna sekwencja, użyj pierwszej wartości
            return regularTiles.get(0).getVal();
        }

        // Sortuj karty według wartości
        regularTiles.sort(Comparator.comparingInt(Tile::getVal));

        // Szukaj luk w sekwencji
        for (int i = 1; i < regularTiles.size(); i++) {
            int gap = regularTiles.get(i).getVal() - regularTiles.get(i-1).getVal();
            if (gap > 1) {
                // Znaleziono lukę, joker ją wypełnia
                return regularTiles.get(i-1).getVal() + 1;
            }
        }

        // Nie znaleziono luk, umieść na początku lub końcu
        if (regularTiles.get(0).getVal() > 1) {
            return regularTiles.get(0).getVal() - 1; // Umieść przed
        } else {
            return regularTiles.get(regularTiles.size()-1).getVal() + 1; // Umieść po
        }
    }

    /**
     * Określa, jaki kolor reprezentuje joker w tej grupie
     */
    public String guessJokerColor() {
        List<Tile> regularTiles = tiles.stream()
                .filter(t -> !t.isJoker())
                .collect(Collectors.toList());

        if (regularTiles.isEmpty()) {
            return "red"; // Domyślny kolor
        }

        // Sprawdź, czy to zestaw wartości (ta sama wartość, różne kolory)
        boolean isValueSet = GroupValidator.isSetGroup(regularTiles);
        Set<String> usedColors = regularTiles.stream()
                .map(Tile::getColor)
                .collect(Collectors.toSet());

        if (isValueSet) {
            // Dla zestawów wartości, joker powinien być kolorem, który nie został jeszcze użyty
            for (String color : Arrays.asList("red", "blue", "black", "orange")) {
                if (!usedColors.contains(color)) {
                    return color;
                }
            }
        }

        // Dla sekwencji, użyj koloru innych kart
        return regularTiles.get(0).getColor();
    }

    public String getJokerReplacements() {
        List<Tile> jokers = tiles.stream()
                .filter(Tile::isJoker)
                .collect(Collectors.toList());

        if (jokers.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (Tile joker : jokers) {
            int val = getJokerValue(joker);
            String color = guessJokerColor();
            String shortColor = "";

            switch (color) {
                case "red": shortColor = "R"; break;
                case "blue": shortColor = "B"; break;
                case "black": shortColor = "G"; break;
                case "orange": shortColor = "O"; break;
                default: shortColor = "?"; break;
            }

            if (val > 0) {
                sb.append("JR(").append(val).append(shortColor).append(")");
                return sb.toString();
            }
        }

        return "";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < tiles.size(); i++) {
            sb.append(tiles.get(i));
            if (i < tiles.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");

        String replacements = getJokerReplacements();
        if (!replacements.isEmpty()) {
            sb.append("  →  ").append(replacements);
        }

        return sb.toString();
    }
}