package com.example.demojavafx.components;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import model.GameState;
import model.Player;
import model.Tile;

/**
 * JavaFX component that displays player scores and tile counts.
 */
public class ScorePane extends VBox {
    private Label[] playerLabels;
    private int playerCount;

    public ScorePane(int playerCount) {
        this.playerCount = playerCount;
        initialize();
    }

    private void initialize() {
        // Set up styling
        setPadding(new Insets(10));
        setSpacing(8);
        setStyle("-fx-background-color: #E0E0E0; -fx-border-color: #999; -fx-border-width: 1;");

        // Create a label for the header
        Label headerLabel = new Label("PLAYERS");
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        getChildren().add(headerLabel);

        // Create labels for each player
        playerLabels = new Label[playerCount];
        for (int i = 0; i < playerCount; i++) {
            playerLabels[i] = new Label("Player " + (i + 1) + ": 0 points (14 tiles)");
            getChildren().add(playerLabels[i]);
        }
    }

    public void updateScores(GameState gameState) {
        for (int i = 0; i < playerCount; i++) {
            Player player = gameState.getPlayer(i);
            int tileCount = player.getHand().size();
            int score = calculateScore(player);

            String playerText = "Player " + (i + 1) + ": " + score + " points (" + tileCount + " tiles)";

            // Highlight current player
            if (i == gameState.getCurrentPlayerIndex()) {
                playerLabels[i].setStyle("-fx-font-weight: bold; -fx-text-fill: #0066CC;");
            } else {
                playerLabels[i].setStyle("-fx-font-weight: normal;");
            }

            playerLabels[i].setText(playerText);
        }
    }

    private int calculateScore(Player player) {
        // In Rummikub, the score is typically negative points for tiles left in hand
        int score = 0;
        for (Tile tile : player.getHand()) {
            if (tile.isJoker()) {
                score -= 30; // Jokers are worth 30 points
            } else {
                score -= tile.getVal(); // Regular tiles are worth their face value
            }
        }
        return score;
    }
}