package model;

import java.util.*;

public class Move {
    private List<Group> initialGroups; // Grupy przed ruchem
    private List<Group> resultingGroups; // Grupy po ruchu
    private List<Tile> tilesPlayed; // Karty zagrane z ręki
    private int score; // Ocena ruchu (im wyższa, tym lepszy ruch)

    public Move(List<Group> initialGroups, List<Group> resultingGroups, List<Tile> tilesPlayed) {
        this.initialGroups = new ArrayList<>(initialGroups);
        this.resultingGroups = new ArrayList<>(resultingGroups);
        this.tilesPlayed = new ArrayList<>(tilesPlayed);
        this.score = 0;
    }

    public List<Group> getInitialGroups() {
        return new ArrayList<>(initialGroups);
    }

    public List<Group> getResultingGroups() {
        return new ArrayList<>(resultingGroups);
    }

    public List<Tile> getTilesPlayed() {
        return new ArrayList<>(tilesPlayed);
    }

    public int tilesPlayedCount() {
        return tilesPlayed.size();
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    /**
     * Oblicza różnicę w liczbie kart na stole przed i po ruchu
     * Pozytywna wartość oznacza, że więcej kart zostało wyłożonych niż zdjętych
     */
    public int getNetTilesPlayed() {
        int initialTileCount = countTiles(initialGroups);
        int resultingTileCount = countTiles(resultingGroups);
        return resultingTileCount - initialTileCount;
    }

    private int countTiles(List<Group> groups) {
        int count = 0;
        for (Group group : groups) {
            count += group.getTiles().size();
        }
        return count;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Ruch (ocena: ").append(score).append("):\n");

        if (!tilesPlayed.isEmpty()) {
            sb.append("Zagrane karty: ");
            for (int i = 0; i < tilesPlayed.size(); i++) {
                sb.append(tilesPlayed.get(i));
                if (i < tilesPlayed.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("\n");
        }

        sb.append("Wynikowe grupy:\n");
        for (int i = 0; i < resultingGroups.size(); i++) {
            sb.append("  - ").append(resultingGroups.get(i)).append("\n");
        }

        return sb.toString();
    }
}