package com.example.demojavafx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import model.*;
import logic.*;
import com.example.demojavafx.components.*;

public class Main extends Application {
    // Główne komponenty gry
    private GameState gameState;
    private TablePane tablePane;
    private HandPane playerHandPane;

    @Override
    public void start(Stage primaryStage) {
        // Inicjalizacja stanu gry
        gameState = new GameState(2); // 2 graczy
        gameState.setCurrentPlayerIndex(0); // Indeks gracza ludzkiego

        // Stworzenie głównego układu
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Stworzenie pola gry (stołu)
        tablePane = new TablePane();
        tablePane.setPrefHeight(400);

        // Stworzenie ręki gracza
        playerHandPane = new HandPane();
        playerHandPane.setPrefHeight(150);

        // Połączenie komponentów dla obsługi drag & drop
        tablePane.setHandPane(playerHandPane);
        playerHandPane.setTablePane(tablePane);

        // Panel kontrolny
        VBox controlPanel = new VBox(10);
        controlPanel.setPadding(new Insets(10));
        controlPanel.setAlignment(Pos.TOP_CENTER);

        Label titleLabel = new Label("RUMMIKUB");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button endTurnButton = new Button("Zakończ turę");
        endTurnButton.setMaxWidth(Double.MAX_VALUE);
        endTurnButton.setOnAction(e -> endTurn());

        Button drawTileButton = new Button("Dobierz kartę");
        drawTileButton.setMaxWidth(Double.MAX_VALUE);
        drawTileButton.setOnAction(e -> drawTile());

        Button undoButton = new Button("Cofnij ruch");
        undoButton.setMaxWidth(Double.MAX_VALUE);
        undoButton.setOnAction(e -> undoMove());

        controlPanel.getChildren().addAll(titleLabel, endTurnButton, drawTileButton, undoButton);

        // Dodanie komponentów do głównego układu
        root.setCenter(tablePane);
        root.setBottom(playerHandPane);
        root.setRight(controlPanel);

        // Dodanie początkowych kafelków do ręki gracza
        addInitialTiles();

        // Aktualizacja interfejsu
        updateUI();

        // Konfiguracja i wyświetlenie okna
        Scene scene = new Scene(root, 900, 700);
        primaryStage.setTitle("Rummikub");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void addInitialTiles() {
        // Dodanie przykładowych kafelków do ręki gracza
        Player player = gameState.getCurrentPlayer();

        player.addTile(new Tile(1, "red"));
        player.addTile(new Tile(2, "red"));
        player.addTile(new Tile(3, "red"));
        player.addTile(new Tile(7, "blue"));
        player.addTile(new Tile(7, "red"));
        player.addTile(new Tile(7, "black"));
        player.addTile(new Tile(8, "blue"));
        player.addTile(new Tile(9, "blue"));
        player.addTile(new Tile(10, "blue"));
        player.addTile(new Tile(11, "orange"));
        player.addTile(new Tile(12, "orange"));
        player.addTile(new Tile(13, "orange"));
        player.addTile(new Tile(0, "red", true)); // Joker
    }

    private void updateUI() {
        // Aktualizacja wyświetlania ręki gracza
        playerHandPane.updateHand(gameState.getCurrentPlayer().getHand());

        // Aktualizacja wyświetlania stołu
        tablePane.updateTable(gameState.getTable());
    }

    private void endTurn() {
        // Sprawdzanie, czy układ na stole jest poprawny
        // Tu powinna być logika weryfikacji ruchu

        // Przejście do następnej tury
        gameState.nextPlayer();

        // W prawdziwej implementacji, tutaj byłby ruch AI

        // Powrót do gracza
        gameState.nextPlayer();

        // Aktualizacja UI
        updateUI();
    }

    private void drawTile() {
        // Symulacja dobrania kafelka
        int val = (int)(Math.random() * 13) + 1;
        String[] colors = {"red", "blue", "black", "orange"};
        String color = colors[(int)(Math.random() * colors.length)];

        gameState.getCurrentPlayer().addTile(new Tile(val, color));

        // Aktualizacja UI
        updateUI();
    }

    private void undoMove() {
        // Tutaj powinna być implementacja cofania ruchu
        // W uproszczonej wersji zresetujemy po prostu stan stołu

        // Aktualizacja UI
        updateUI();
    }

    public static void main(String[] args) {
        launch(args);
    }
}