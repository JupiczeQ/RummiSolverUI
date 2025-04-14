package com.example.demojavafx.components;

import javafx.geometry.Insets;
import javafx.scene.input.*;
import javafx.scene.layout.FlowPane;
import model.Group;
import model.Tile;

import java.util.*;

public class GroupPane extends FlowPane {
    private Group group;
    private TablePane tablePane;
    private Map<Tile, TileView> tileViewMap = new HashMap<>();

    public GroupPane() {
        // Konstruktor dla pustej grupy
        initialize();
    }

    public GroupPane(Group group) {
        this.group = group;
        initialize();
        updateTiles();
    }

    private void initialize() {
        // Ustawienie wyglądu
        setPadding(new Insets(5));
        setHgap(2);
        setVgap(2);

        // Różny styl dla pustej grupy i z kafelkami
        if (group == null || group.getTiles().isEmpty()) {
            setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-border-color: #999; " +
                    "-fx-border-width: 1; -fx-border-style: dashed; -fx-min-width: 250; -fx-min-height: 70;");
        } else {
            setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-border-color: #999; " +
                    "-fx-border-width: 1;");
        }

        // Obsługa przeciągania i upuszczania
        setupDropTarget();
    }

    public void updateTiles() {
        getChildren().clear();
        tileViewMap.clear();

        if (group == null || group.getTiles().isEmpty()) {
            // Pusty placeholder
            setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-border-color: #999; " +
                    "-fx-border-width: 1; -fx-border-style: dashed; -fx-min-width: 250; -fx-min-height: 70;");
            return;
        }

        setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-border-color: #999; -fx-border-width: 1;");

        // Dodanie widoków kafelków
        for (Tile tile : group.getTiles()) {
            TileView tileView = new TileView(tile);
            tileView.setIsOnTable(true);
            tileViewMap.put(tile, tileView);
            getChildren().add(tileView);
        }
    }

    private void setupDropTarget() {
        setOnDragOver(event -> {
            if (event.getGestureSource() instanceof TileView) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasString()) {
                // Znalezienie przeciąganego kafelka
                TileView source = findTileViewById(db.getString());

                if (source != null) {
                    Tile tile = source.getTile();

                    if (source.isOnTable()) {
                        // Przeniesienie z innej grupy
                        tablePane.removeTileFromTable(source);
                    } else {
                        // Przeniesienie z ręki
                        tablePane.getHandPane().removeTile(tile);
                        // Śledzenie wyłożonych kafelków
                        tablePane.getPlayedTiles().add(tile);
                    }

                    // Jeśli to pusta grupa, zainicjuj ją
                    if (group == null) {
                        group = new Group();
                    }

                    // Dodanie kafelka do grupy
                    group.addTile(tile);

                    // Aktualizacja widoku
                    updateTiles();

                    success = true;
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    private TileView findTileViewById(String id) {
        try {
            int hashCode = Integer.parseInt(id);

            // Sprawdzenie kafelków na stole
            for (TileView tileView : tablePane.getAllTileViews()) {
                if (System.identityHashCode(tileView) == hashCode) {
                    return tileView;
                }
            }

            // Sprawdzenie kafelków w ręce
            for (TileView tileView : tablePane.getHandPane().getAllTileViews()) {
                if (System.identityHashCode(tileView) == hashCode) {
                    return tileView;
                }
            }
        } catch (NumberFormatException e) {
            // Nieprawidłowy format ID
        }

        return null;
    }

    public void setTablePane(TablePane tablePane) {
        this.tablePane = tablePane;
    }

    public boolean removeTile(Tile tile) {
        if (group != null) {
            // Szukanie i usunięcie kafelka z grupy
            for (Tile groupTile : new ArrayList<>(group.getTiles())) {
                if (groupTile.equals(tile)) {
                    group.getTiles().remove(groupTile);
                    updateTiles();
                    return true;
                }
            }
        }
        return false;
    }

    public Collection<TileView> getTileViews() {
        return tileViewMap.values();
    }

    public Group getGroup() {
        return group;
    }
}