package model;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private List<Tile> hand;

    public Player() {
        this.hand = new ArrayList<>();
    }

    public void addTile(Tile tile) {
        hand.add(tile);
    }

    public void removeTile(Tile tile) {
        hand.remove(tile);
    }

    public List<Tile> getHand() {
        return new ArrayList<>(hand); // Zwraca kopię, aby uniknąć modyfikacji zewnętrznych
    }
}