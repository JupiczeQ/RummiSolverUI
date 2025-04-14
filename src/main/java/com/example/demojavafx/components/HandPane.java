package     com.example.demojavafx.components;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import model.Tile;

import java.util.*;

public class HandPane extends BorderPane {
    private FlowPane tilesPane;
    private TablePane tablePane;
    private Map<Tile, TileView> tileViewMap = new HashMap<>();

    public HandPane() {
        // Inicjalizacja układu
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #E0E0E0; -fx-border-color: #999;");

        // Etykieta
        Label titleLabel = new Label("TWOJA RĘKA");
        titleLabel.setStyle("-fx-font-weight: bold;");

        // Kontener na kafelki
        tilesPane = new FlowPane(5, 5);
        tilesPane.setPadding(new Insets(10));

        // Składanie układu
        VBox topBox = new VBox(titleLabel);
        topBox.setPadding(new Insets(5));

        setTop(topBox);
        setCenter(tilesPane);

        // Obsługa upuszczania kafelków
        setupDropTarget();
    }

    public void updateHand(List<Tile> tiles) {
        tilesPane.getChildren().clear();
        tileViewMap.clear();

        // Tworzenie widoków kafelków
        for (Tile tile : tiles) {
            TileView tileView = new TileView(tile);
            tileViewMap.put(tile, tileView);
            tilesPane.getChildren().add(tileView);
        }
    }

    private void setupDropTarget() {
        setOnDragOver(event -> {
            if (event.getGestureSource() instanceof TileView &&
                    ((TileView) event.getGestureSource()).isOnTable()) {
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

                if (source != null && source.isOnTable()) {
                    // Usunięcie kafelka ze stołu
                    tablePane.removeTileFromTable(source);

                    // Dodanie z powrotem do ręki
                    Tile tile = source.getTile();
                    TileView newTileView = new TileView(tile);

                    tileViewMap.put(tile, newTileView);
                    tilesPane.getChildren().add(newTileView);

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
            for (TileView tileView : tileViewMap.values()) {
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

    public void removeTile(Tile tile) {
        TileView tileView = tileViewMap.get(tile);
        if (tileView != null) {
            tilesPane.getChildren().remove(tileView);
            tileViewMap.remove(tile);
        }
    }

    public Collection<TileView> getAllTileViews() {
        return tileViewMap.values();
    }
}