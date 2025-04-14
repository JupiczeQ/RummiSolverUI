package model;

import java.util.Objects;

public class Tile {
    private int val;
    private String color;
    private boolean isJoker;

    public Tile(int val, String color) {
        this.val = val;
        this.color = color;
        this.isJoker = false;
    }

    public Tile(int val, String color, boolean isJoker) {
        this.val = val;
        this.color = color;
        this.isJoker = isJoker;
    }

    public int getVal() {
        return val;
    }

    public String getColor() {
        return color;
    }

    public boolean isJoker() {
        return isJoker;
    }

    @Override
    public String toString() {
        if (isJoker) {
            return "JR";
        }
        return val + color.substring(0, 1).toUpperCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tile)) return false;
        Tile tile = (Tile) o;
        return val == tile.val &&
                Objects.equals(color, tile.color) &&
                isJoker == tile.isJoker;
    }

    @Override
    public int hashCode() {
        return Objects.hash(val, color, isJoker);
    }
}