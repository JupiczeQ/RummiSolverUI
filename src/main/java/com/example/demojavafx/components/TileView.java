package com.example.demojavafx.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import model.Tile;

public class TileView extends StackPane {
    private Tile tile;
    private boolean isOnTable;

    public TileView(Tile tile) {
        this.tile = tile;
        this.isOnTable = false;

        // Wymiary kafelka
        setPrefSize(40, 55);
        setMaxSize(40, 55);

        // Tło kafelka
        Rectangle background = new Rectangle(40, 55);
        background.setArcWidth(10);
        background.setArcHeight(10);

        // Ustawienie koloru
        if (tile.isJoker()) {
            background.setFill(Color.BLACK);
        } else {
            switch (tile.getColor()) {
                case "red": background.setFill(Color.DARKRED); break;
                case "blue": background.setFill(Color.DARKBLUE); break;
                case "black": background.setFill(Color.BLACK); break;
                case "orange": background.setFill(Color.DARKORANGE); break;
                default: background.setFill(Color.GRAY);
            }
        }
        background.setStroke(Color.BLACK);

        // Etykieta z wartością
        Label valueLabel = new Label(tile.isJoker() ? "J" : String.valueOf(tile.getVal()));
        valueLabel.setTextFill(Color.WHITE);
        valueLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        // Dodanie elementów
        getChildren().addAll(background, valueLabel);
        setAlignment(Pos.CENTER);

        // Obsługa przeciągania
        setupDragAndDrop();
    }

    private void setupDragAndDrop() {
        setOnDragDetected(event -> {
            Dragboard db = startDragAndDrop(TransferMode.MOVE);

            // Stworzenie snapshootu do pokazania podczas przeciągania
            db.setDragView(snapshot(null, null));

            // Zapisanie ID kafelka do identyfikacji
            ClipboardContent content = new ClipboardContent();
            content.putString(System.identityHashCode(this) + "");
            db.setContent(content);

            event.consume();
        });

        // Akceptacja upuszczania na ten kafelek (dla przegrupowania)
        setOnDragOver(event -> {
            if (event.getGestureSource() != this &&
                    event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
    }

    public Tile getTile() {
        return tile;
    }

    public boolean isOnTable() {
        return isOnTable;
    }

    public void setIsOnTable(boolean onTable) {
        this.isOnTable = onTable;
    }
}