package utils;

import java.util.*;
import model.Tile;

public class InputParser {
    /**
     * Parsuje ciąg znaków reprezentujący karty do listy obiektów Tile
     */
    public static List<Tile> parseTiles(String input) {
        List<Tile> tiles = new ArrayList<>();
        String[] tileStrings = input.trim().split("\\s+");

        for (String tileStr : tileStrings) {
            Tile tile = parseTile(tileStr);
            if (tile != null) {
                tiles.add(tile);
            }
        }

        return tiles;
    }

    /**
     * Parsuje pojedynczy ciąg znaków reprezentujący kartę do obiektu Tile
     */
    public static Tile parseTile(String tileStr) {
        tileStr = tileStr.trim();

        // Obsługa jokera
        if (tileStr.equalsIgnoreCase("JR")) {
            return new Tile(0, "red", true);
        }

        // Dla zwykłych kart, format to wartość + kolor (np. 5R, 10B)
        if (tileStr.length() < 2) {
            return null;
        }

        String colorCode = tileStr.substring(tileStr.length() - 1);
        String valueStr = tileStr.substring(0, tileStr.length() - 1);

        String color;
        switch (colorCode.toUpperCase()) {
            case "R": color = "red"; break;
            case "B": color = "blue"; break;
            case "G": color = "black"; break;  // G oznacza czarne (German/Polish: Grun/Granat)
            case "O": color = "orange"; break;
            default: return null;
        }

        try {
            int value = Integer.parseInt(valueStr);
            if (value >= 1 && value <= 13) {
                return new Tile(value, color);
            }
        } catch (NumberFormatException e) {
            return null;
        }

        return null;
    }
}