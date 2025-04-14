package com.example.demojavafx.components;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import model.Group;
import model.Table;
import model.Tile;

import java.util.*;

public class TablePane extends BorderPane {
    private VBox groupsContainer;
    private HandPane handPane;
    private Map<Group, GroupPane> groupPaneMap = new HashMap<>();
    private List<Tile> playedTiles = new ArrayList<>();

    public TablePane() {
        // Inicjalizacja układu
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #537D8D; -fx-border-color: #333;");

        // Kontener na grupy
        groupsContainer = new VBox(10);
        groupsContainer.setPadding(new Insets(10));

        // Scroll pane dla wielu grup
        ScrollPane scrollPane = new ScrollPane(groupsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        setCenter(scrollPane);

        // Obsługa upuszczania
        setupDropTarget();
    }

    public void updateTable(Table table) {
        groupsContainer.getChildren().clear();
        groupPaneMap.clear();

        // Dodanie grup z tabeli
        for (Group group : table.getGroups()) {
            GroupPane groupPane = new GroupPane(group);
            groupPane.setTablePane(this);
            groupPaneMap.put(group, groupPane);
            groupsContainer.getChildren().add(groupPane);
        }

        // Dodanie pustej grupy na końcu
        GroupPane emptyGroupPane = new GroupPane();
        emptyGroupPane.setTablePane(this);
        groupsContainer.getChildren().add(emptyGroupPane);
    }

    private void setupDropTarget() {
        setOnDragOver(event -> {
            if (event.getGestureSource() instanceof TileView &&
                    !((TileView) event.getGestureSource()).isOnTable()) {
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

                if (source != null && !source.isOnTable()) {
                    // Utworzenie nowej grupy z tym kafelkiem
                    Tile tile = source.getTile();

                    Group newGroup = new Group();
                    newGroup.addTile(tile);

                    GroupPane groupPane = new GroupPane(newGroup);
                    groupPane.setTablePane(this);

                    // Dodanie przed pustą grupą
                    int lastIndex = groupsContainer.getChildren().size() - 1;
                    groupsContainer.getChildren().add(lastIndex, groupPane);

                    groupPaneMap.put(newGroup, groupPane);

                    // Usunięcie kafelka z ręki
                    handPane.removeTile(tile);

                    // Śledzenie wyłożonych kafelków
                    playedTiles.add(tile);

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

            // Sprawdzenie kafelków w ręce
            for (TileView tileView : handPane.getAllTileViews()) {
                if (System.identityHashCode(tileView) == hashCode) {
                    return tileView;
                }
            }

            // Sprawdzenie kafelków na stole
            for (GroupPane groupPane : groupPaneMap.values()) {
                for (TileView tileView : groupPane.getTileViews()) {
                    if (System.identityHashCode(tileView) == hashCode) {
                        return tileView;
                    }
                }
            }
        } catch (NumberFormatException e) {
            // Nieprawidłowy format ID
        }

        return null;
    }

    public void setHandPane(HandPane handPane) {
        this.handPane = handPane;
    }

    public HandPane getHandPane() {
        return handPane;
    }

    public List<TileView> getAllTileViews() {
        List<TileView> allTileViews = new ArrayList<>();

        for (GroupPane groupPane : groupPaneMap.values()) {
            allTileViews.addAll(groupPane.getTileViews());
        }

        return allTileViews;
    }

    public void removeTileFromTable(TileView tileView) {
        // Znalezienie grupy zawierającej ten kafelek
        for (Map.Entry<Group, GroupPane> entry : groupPaneMap.entrySet()) {
            GroupPane groupPane = entry.getValue();
            Group group = entry.getKey();

            if (groupPane.removeTile(tileView.getTile())) {
                // Sprawdzenie, czy grupa jest teraz pusta
                if (group.getTiles().isEmpty()) {
                    groupsContainer.getChildren().remove(groupPane);
                    groupPaneMap.remove(group);
                }

                // Usunięcie ze śledzenia
                playedTiles.remove(tileView.getTile());

                break;
            }
        }
    }

    public List<Tile> getPlayedTiles() {
        return new ArrayList<>(playedTiles);
    }

    public void clearPlayedTiles() {
        playedTiles.clear();
    }
}