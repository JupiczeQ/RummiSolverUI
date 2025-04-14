package com.example.demojavafx.components;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Tile;
import utils.InputParser;

import java.util.List;
import java.util.ArrayList;

/**
 * Dialog for inputting and visually previewing a player's hand.
 */
public class HandInputDialog extends Dialog<List<Tile>> {
    private TextField inputField;
    private FlowPane previewPane;
    private List<Tile> parsedTiles;

    public HandInputDialog() {
        this.parsedTiles = new ArrayList<>();
        initialize();
    }

    private void initialize() {
        setTitle("Enter Your Initial Hand");
        setHeaderText("Enter the tiles in your hand:");

        // Set the button types
        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        // Create the content area - we'll use a VBox directly, not a ScrollPane
        VBox content = new VBox(15);
        content.setPadding(new Insets(20, 10, 10, 10));

        // Add instructions
        Label instructionsLabel = new Label("Enter your tiles separated by spaces using the format:\n" +
                "- Regular tiles: [value][color] (e.g., 5R, 10B, 13G)\n" +
                "- Jokers: JR\n\n" +
                "Colors: R (Red), B (Blue), G (Black), O (Orange)");
        instructionsLabel.setWrapText(true);
        content.getChildren().add(instructionsLabel);

        // Add input field
        inputField = new TextField();
        inputField.setPromptText("e.g., 1R 2R 3R 7B 7R 7G 8B 9B 10B 11O 12O 13O JR");

        // Example button
        Button exampleButton = new Button("Use Example");
        exampleButton.setOnAction(e -> {
            inputField.setText("1R 2R 3R 7B 7R 7G 8B 9B 10B 11O 12O 13O JR");
            updatePreview();
        });

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> {
            inputField.clear();
            updatePreview();
        });

        HBox inputBox = new HBox(10, inputField, exampleButton, clearButton);
        inputBox.setPadding(new Insets(5, 0, 10, 0));
        content.getChildren().add(inputBox);

        // Add preview label
        Label previewLabel = new Label("Tile Preview:");
        previewLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        content.getChildren().add(previewLabel);

        // Add preview pane
        previewPane = new FlowPane(5, 5);
        previewPane.setPadding(new Insets(10));
        previewPane.setPrefHeight(120);
        previewPane.setStyle("-fx-background-color: #E0E0E0; -fx-border-color: #999; -fx-border-width: 1;");
        content.getChildren().add(previewPane);

        // Add validation indicator
        Label validationLabel = new Label("");
        validationLabel.setFont(Font.font("System", FontWeight.NORMAL, 11));
        content.getChildren().add(validationLabel);

        // Add update preview button
        Button updateButton = new Button("Update Preview");
        updateButton.setOnAction(e -> updatePreview());
        content.getChildren().add(updateButton);

        // Add predefined color tiles for quick addition
        Label quickAddLabel = new Label("Quick Add:");
        quickAddLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        content.getChildren().add(quickAddLabel);

        // Grid for all Rummikub tiles - always visible instead of scrollable
        GridPane tilesGrid = createQuickAddGrid();
        content.getChildren().add(tilesGrid);

        getDialogPane().setContent(content);

        // Make the dialog resizable
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.setResizable(true);
        stage.setMinHeight(700);
        stage.setMinWidth(600);

        // Set initial size
        getDialogPane().setPrefWidth(600);
        getDialogPane().setPrefHeight(700);

        // Update preview when text changes
        inputField.setOnKeyReleased(e -> {
            if (e.getCode().toString().equals("ENTER")) {
                updatePreview();
            }
        });

        // Initial preview update
        updatePreview();

        // Convert the result to a list of tiles when the confirm button is clicked
        setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return new ArrayList<>(parsedTiles);
            }
            return null;
        });
    }

    /**
     * Creates a grid layout for all Rummikub tiles, organized by color
     */
    private GridPane createQuickAddGrid() {
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
            tileButton.setOnAction(e -> addTileToInput(value + "R"));
            redPane.getChildren().add(tileButton);
        }
        grid.add(redPane, 1, 0);

        // Add blue tiles
        FlowPane bluePane = new FlowPane(5, 5);
        for (int i = 1; i <= 13; i++) {
            final int value = i;
            Button tileButton = new Button(i + "B");
            tileButton.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white;");
            tileButton.setOnAction(e -> addTileToInput(value + "B"));
            bluePane.getChildren().add(tileButton);
        }
        grid.add(bluePane, 1, 1);

        // Add black tiles
        FlowPane blackPane = new FlowPane(5, 5);
        for (int i = 1; i <= 13; i++) {
            final int value = i;
            Button tileButton = new Button(i + "G");
            tileButton.setStyle("-fx-background-color: #212121; -fx-text-fill: white;");
            tileButton.setOnAction(e -> addTileToInput(value + "G"));
            blackPane.getChildren().add(tileButton);
        }
        grid.add(blackPane, 1, 2);

        // Add orange tiles
        FlowPane orangePane = new FlowPane(5, 5);
        for (int i = 1; i <= 13; i++) {
            final int value = i;
            Button tileButton = new Button(i + "O");
            tileButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
            tileButton.setOnAction(e -> addTileToInput(value + "O"));
            orangePane.getChildren().add(tileButton);
        }
        grid.add(orangePane, 1, 3);

        // Add jokers
        FlowPane jokerPane = new FlowPane(5, 5);
        Button jokerButton = new Button("JR");
        jokerButton.setStyle("-fx-background-color: #000000; -fx-text-fill: white;");
        jokerButton.setOnAction(e -> addTileToInput("JR"));
        jokerPane.getChildren().add(jokerButton);

        Label jokersLabel = new Label("Jokers:");
        jokersLabel.setStyle("-fx-font-weight: bold;");
        grid.add(jokersLabel, 0, 4);
        grid.add(jokerPane, 1, 4);

        return grid;
    }

    private void addTileToInput(String tileStr) {
        String currentText = inputField.getText().trim();
        if (currentText.isEmpty()) {
            inputField.setText(tileStr);
        } else {
            inputField.setText(currentText + " " + tileStr);
        }
        updatePreview();
    }

    private void updatePreview() {
        // Clear preview pane
        previewPane.getChildren().clear();

        // Parse tiles
        String input = inputField.getText().trim();
        parsedTiles = InputParser.parseTiles(input);

        // Update preview
        for (Tile tile : parsedTiles) {
            TileView tileView = new TileView(tile);
            previewPane.getChildren().add(tileView);
        }

        // Update validation display
        Button confirmButton = (Button) getDialogPane().lookupButton(getDialogPane().getButtonTypes().get(0));

        // Find the validation label in the main content VBox
        VBox contentVBox = (VBox) getDialogPane().getContent();
        Label validationLabel = (Label) contentVBox.getChildren().get(4);

        if (parsedTiles.isEmpty() && !input.isEmpty()) {
            validationLabel.setText("Warning: No valid tiles found. Please check the format.");
            validationLabel.setStyle("-fx-text-fill: #D32F2F;");
            confirmButton.setDisable(true);
        } else if (parsedTiles.isEmpty()) {
            validationLabel.setText("Enter some tiles to see a preview.");
            validationLabel.setStyle("-fx-text-fill: #000000;");
            confirmButton.setDisable(true);
        } else {
            validationLabel.setText("Found " + parsedTiles.size() + " valid tiles.");
            validationLabel.setStyle("-fx-text-fill: #388E3C;");
            confirmButton.setDisable(false);
        }
    }
}