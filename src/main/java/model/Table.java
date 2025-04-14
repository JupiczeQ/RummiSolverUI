package model;

import java.util.*;

public class Table {
    private List<Group> groups;

    public Table() {
        this.groups = new ArrayList<>();
    }

    public void addGroup(Group group) {
        groups.add(group);
    }

    public void removeGroup(Group group) {
        groups.remove(group);
    }

    public List<Group> getGroups() {
        return new ArrayList<>(groups);
    }

    /**
     * Zwraca wszystkie kafelki na stole
     */
    public List<Tile> getAllTiles() {
        List<Tile> allTiles = new ArrayList<>();
        for (Group group : groups) {
            allTiles.addAll(group.getTiles());
        }
        return allTiles;
    }

    /**
     * Tworzy głęboką kopię stołu
     */
    public Table clone() {
        Table clone = new Table();
        for (Group group : groups) {
            clone.addGroup(new Group(group.getTiles()));
        }
        return clone;
    }

    /**
     * Sprawdza czy stół jest pusty
     */
    public boolean isEmpty() {
        return groups.isEmpty();
    }

    /**
     * Usuwa wszystkie grupy ze stołu
     */
    public void clear() {
        groups.clear();
    }

    /**
     * Zastępuje wszystkie grupy nowymi
     */
    public void replaceGroups(List<Group> newGroups) {
        groups.clear();
        groups.addAll(newGroups);
    }

    @Override
    public String toString() {
        if (groups.isEmpty()) {
            return "Stół jest pusty";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < groups.size(); i++) {
            sb.append("Grupa ").append(i + 1).append(": ").append(groups.get(i)).append("\n");
        }
        return sb.toString();
    }
}