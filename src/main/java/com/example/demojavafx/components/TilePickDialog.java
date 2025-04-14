package com.example.demojavafx.components;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.Tile;
import utils.InputParser;

/**
 * Dialog for picking a tile to draw
 */
public class TilePickDialog extends Dialog<Tile> {
    private TextField inputField;
    private TileView previewTile;
    private Tile selectedTile;

    public TilePickDialog() {
        initialize();
    }

    private void initialize() {
        setTitle("Draw a Tile");
        setHeaderText("Select a tile to draw:");

        // Set the button types
        ButtonType drawButtonType = new ButtonType("Draw", ButtonBar.ButtonData.OK_DONE);
        ButtonType randomButtonType = new ButtonType("Random Tile", ButtonBar.ButtonData.OTHER);
        getDialogPane().getButtonTypes().addAll(drawButtonType, randomButtonType, ButtonType.CANCEL);

        // Create the content area
        VBox content = new VBox(15);
        content.setPadding(new Insets(20, 10, 10, 10));

        // Add instructions
        Label instructionsLabel = new Label("Enter a tile using the format: [value][color] (e.g., 5R, 10B) or JR for Joker\n" +
                "Or select a tile visually below.");
        instructionsLabel.setWrapText(true);
        content.getChildren().add(instructionsLabel);

        // Add input field
        inputField = new TextField();
        inputField.setPromptText("e.g., 5R or JR");

        // Preview area
        previewTile = new TileView(new Tile(1, "red"));
        previewTile.setVisible(false);

        HBox inputBox = new HBox(10, new Label("Tile:"), inputField, previewTile);
        inputBox.setPadding(new Insets(5, 0, 10, 0));
        inputBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        content.getChildren().add(inputBox);

        // Add update preview button
        Button updateButton = new Button("Preview");
        updateButton.setOnAction(e -> updatePreview());
        content.getChildren().add(updateButton);

        // Add predefined tiles for quick selection
        Label selectTileLabel = new Label("Select a tile:");
        selectTileLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        content.getChildren().add(selectTileLabel);

        // Grid for all Rummikub tiles
        GridPane tilesGrid = createTilesGrid();
        content.getChildren().add(tilesGrid);

        getDialogPane().setContent(content);

        // Make dialog resizable
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.setResizable(true);

        // Update preview when text changes
        inputField.setOnAction(e -> updatePreview());

        // Handle the result
        setResultConverter(dialogButton -> {
            if (dialogButton == drawButtonType) {
                return selectedTile;
            } else if (dialogButton == randomButtonType) {
                // Return null for random tile
                return null;
            }
            return null;
        });
    }

    private GridPane createTilesGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        // Add color labels
        Label redLabel = new Label("Red:");
        redLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;");
        grid.add(redLabel, 0, 0);

        Label blueLabel = new Label("Blue:");
        blueLabel.setStyle("-fx-text-fill: #1976D2; -fx-font-weight: bold;");
        grid.add(blueLabel, 0, 1);

        Label blackLabel = new Label("Black:");
        blackLabel.setStyle("-fx-text-fill: #212121; -fx-font-weight: bold;");
        grid.add(blackLabel, 0, 2);

        Label orangeLabel = new Label("Orange:");
        orangeLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
        grid.add(orangeLabel, 0, 3);

        // Add red tiles
        FlowPane redPane = new FlowPane(5, 5);
        for (int i = 1; i <= 13; i++) {
            final int value = i;
            Button tileButton = new Button(i + "R");
            tileButton.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white;");
            tileButton.setOnAction(e -> selectTile(value + "R"));
            redPane.getChildren().add(tileButton);
        }
        grid.add(redPane, 1, 0);

        // Add blue tiles
        FlowPane bluePane = new FlowPane(5, 5);
        for (int i = 1; i <= 13; i++) {
            final int value = i;
            Button tileButton = new Button(i + "B");
            tileButton.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white;");
            tileButton.setOnAction(e -> selectTile(value + "B"));
            bluePane.getChildren().add(tileButton);
        }
        grid.add(bluePane, 1, 1);

        // Add black tiles
        FlowPane blackPane = new FlowPane(5, 5);
        for (int i = 1; i <= 13; i++) {
            final int value = i;
            Button tileButton = new Button(i + "G");
            tileButton.setStyle("-fx-background-color: #212121; -fx-text-fill: white;");
            tileButton.setOnAction(e -> selectTile(value + "G"));
            blackPane.getChildren().add(tileButton);
        }
        grid.add(blackPane, 1, 2);

        // Add orange tiles
        FlowPane orangePane = new FlowPane(5, 5);
        for (int i = 1; i <= 13; i++) {
            final int value = i;
            Button tileButton = new Button(i + "O");
            tileButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
            tileButton.setOnAction(e -> selectTile(value + "O"));
            orangePane.getChildren().add(tileButton);
        }
        grid.add(orangePane, 1, 3);

        // Add joker
        FlowPane jokerPane = new FlowPane(5, 5);
        Button jokerButton = new Button("JR");
        jokerButton.setStyle("-fx-background-color: #000000; -fx-text-fill: white;");
        jokerButton.setOnAction(e -> selectTile("JR"));
        jokerPane.getChildren().add(jokerButton);

        Label jokersLabel = new Label("Jokers:");
        jokersLabel.setStyle("-fx-font-weight: bold;");
        grid.add(jokersLabel, 0, 4);
        grid.add(jokerPane, 1, 4);

        return grid;
    }

    private void selectTile(String tileStr) {
        inputField.setText(tileStr);
        updatePreview();
    }

    private void updatePreview() {
        String input = inputField.getText().trim();
        Tile tile = InputParser.parseTile(input);

        if (tile != null) {
            selectedTile = tile;
            previewTile.getChildren().clear();
            TileView tileView = new TileView(tile);
            previewTile.getChildren().addAll(tileView.getChildren());
            previewTile.setVisible(true);

            // Enable the OK button
            Button drawButton = (Button) getDialogPane().lookupButton(getDialogPane().getButtonTypes().get(0));
            drawButton.setDisable(false);
        } else {
            previewTile.setVisible(false);
            selectedTile = null;

            // Disable the OK button if no valid tile
            Button drawButton = (Button) getDialogPane().lookupButton(getDialogPane().getButtonTypes().get(0));
            drawButton.setDisable(true);
        }
    }
}