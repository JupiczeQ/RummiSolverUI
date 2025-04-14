package com.example.demojavafx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

import model.*;
import logic.*;
import utils.*;
import com.example.demojavafx.components.*;

public class RummikubSolver extends Application {
    // Game components
    private GameState gameState;
    private TablePane tablePane;
    private HandPane playerHandPane;
    private ScorePane scorePane;
    private MoveGenerator moveGenerator;
    private GameLogic gameLogic;
    private Label statusLabel;
    private Button suggestMoveButton;
    private TilePool tilePool;

    @Override
    public void start(Stage primaryStage) {
        // Initialize game logic
        gameLogic = new GameLogic();
        moveGenerator = new MoveGenerator(gameLogic);
        tilePool = new TilePool();

        // Main layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Create setup dialog first
        setupGame();

        // Create game table
        tablePane = new TablePane();
        tablePane.setPrefHeight(400);

        // Create player hand
        playerHandPane = new HandPane();
        playerHandPane.setPrefHeight(150);

        // Connect components for drag & drop
        tablePane.setHandPane(playerHandPane);
        playerHandPane.setTablePane(tablePane);

        // Create score pane for showing player info
        scorePane = new ScorePane(gameState.getAllPlayers().size());
        scorePane.updateScores(gameState);

        // Status panel with game info
        HBox statusPanel = new HBox(15);
        statusPanel.setPadding(new Insets(5));
        statusPanel.setAlignment(Pos.CENTER_LEFT);
        statusPanel.setStyle("-fx-background-color: #EEEEEE; -fx-border-color: #CCCCCC; -fx-border-width: 0 0 1 0;");

        // Player turn indicator
        Label playerTurnLabel = new Label("Your Turn");
        playerTurnLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1976D2;");

        // First move indicator
        Label firstMoveLabel = new Label("First Move (Need 30+ pts)");
        firstMoveLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #D32F2F;");

        // Status label
        statusLabel = new Label("Game started. Waiting for your move...");
        statusLabel.setPadding(new Insets(5));
        statusLabel.setStyle("-fx-font-style: italic;");

        // Add labels to status panel
        statusPanel.getChildren().addAll(playerTurnLabel, firstMoveLabel, statusLabel);

        // Control panel
        VBox controlPanel = createControlPanel();

        // Add components to main layout
        BorderPane centerPanel = new BorderPane();
        centerPanel.setCenter(tablePane);
        centerPanel.setBottom(playerHandPane);
        centerPanel.setTop(statusPanel);

        root.setCenter(centerPanel);
        root.setRight(controlPanel);
        root.setLeft(scorePane);

        // Create scene and show window
        Scene scene = new Scene(root, 1100, 700);
        primaryStage.setTitle("Rummikub Solver");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Now that the scene is created, we can update the UI safely
        updateStatusPanel(playerTurnLabel, firstMoveLabel);

        // Update UI for initial state
        updateUI();
    }

    private void setupGame() {
        // Create dialog for game setup
        Dialog<GameSettings> setupDialog = new Dialog<>();
        setupDialog.setTitle("Rummikub Solver Setup");
        setupDialog.setHeaderText("Configure your game");

        // Set the button types
        ButtonType startButtonType = new ButtonType("Start Game", ButtonBar.ButtonData.OK_DONE);
        setupDialog.getDialogPane().getButtonTypes().addAll(startButtonType, ButtonType.CANCEL);

        // Create the form content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        // Player count input
        ComboBox<Integer> playerCountCombo = new ComboBox<>(
                FXCollections.observableArrayList(2, 3, 4));
        playerCountCombo.setValue(2);

        // Player turn input
        ComboBox<Integer> playerTurnCombo = new ComboBox<>();
        playerTurnCombo.setItems(FXCollections.observableArrayList(1, 2));
        playerTurnCombo.setValue(1);

        // Update player turn options when player count changes
        playerCountCombo.setOnAction(e -> {
            int count = playerCountCombo.getValue();
            List<Integer> turns = new ArrayList<>();
            for (int i = 1; i <= count; i++) {
                turns.add(i);
            }
            playerTurnCombo.setItems(FXCollections.observableArrayList(turns));
            playerTurnCombo.setValue(1);
        });

        // Initial hand input with visual editor button
        TextField initialHandField = new TextField();
        initialHandField.setPromptText("e.g., 1R 2R 3R 7B 7R 7G 8B 9B 10B 11O 12O 13O JR");
        initialHandField.setPrefWidth(400);

        Button openEditorButton = new Button("Visual Editor");
        openEditorButton.setOnAction(e -> {
            HandInputDialog handDialog = new HandInputDialog();
            if (initialHandField.getText() != null && !initialHandField.getText().trim().isEmpty()) {
                handDialog.getDialogPane().lookup("TextField").setUserData(initialHandField.getText());
            }

            Optional<List<Tile>> result = handDialog.showAndWait();
            result.ifPresent(tiles -> {
                // Convert tiles back to string format, ensuring correct color codes
                StringBuilder sb = new StringBuilder();
                for (Tile tile : tiles) {
                    // Explicit conversion to ensure correct color mapping
                    String tileStr;
                    if (tile.isJoker()) {
                        tileStr = "JR";
                    } else {
                        String colorCode;
                        switch (tile.getColor()) {
                            case "red": colorCode = "R"; break;
                            case "blue": colorCode = "B"; break;
                            case "black": colorCode = "G"; break; // Explicitly use G for black
                            case "orange": colorCode = "O"; break;
                            default: colorCode = "?";
                        }
                        tileStr = tile.getVal() + colorCode;
                    }
                    sb.append(tileStr).append(" ");
                }
                initialHandField.setText(sb.toString().trim());
            });
        });

        // Arrange the grid
        grid.add(new Label("Number of Players:"), 0, 0);
        grid.add(playerCountCombo, 1, 0);
        grid.add(new Label("Your Turn (Player #):"), 0, 1);
        grid.add(playerTurnCombo, 1, 1);
        grid.add(new Label("Initial Hand:"), 0, 2);

        HBox handInputBox = new HBox(10, initialHandField, openEditorButton);
        grid.add(handInputBox, 1, 2);

        // Add a help button for format
        Button helpButton = new Button("?");
        helpButton.setOnAction(e -> {
            Alert helpAlert = new Alert(Alert.AlertType.INFORMATION);
            helpAlert.setTitle("Input Format Help");
            helpAlert.setHeaderText("Tile Format Guide");
            helpAlert.setContentText(
                    "Enter tiles separated by spaces using the following format:\n\n" +
                            "- Regular tiles: [value][color]\n" +
                            "  - Value: 1-13\n" +
                            "  - Color: R (Red), B (Blue), G (Black), O (Orange)\n" +
                            "  - Example: 5R, 10B, 13G\n\n" +
                            "- Jokers: JR\n\n" +
                            "Example: 1R 2R 3R 7B 7R 7G 8B 9B 10B 11O 12O 13O JR"
            );
            helpAlert.showAndWait();
        });
        grid.add(helpButton, 2, 2);

        // Put the grid in a scroll pane to ensure all content is accessible
        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);

        setupDialog.getDialogPane().setContent(scrollPane);
        setupDialog.getDialogPane().setPrefWidth(600);
        setupDialog.getDialogPane().setPrefHeight(300);

        // Convert the result
        setupDialog.setResultConverter(dialogButton -> {
            if (dialogButton == startButtonType) {
                return new GameSettings(
                        playerCountCombo.getValue(),
                        playerTurnCombo.getValue() - 1,  // Convert to 0-based index
                        initialHandField.getText()
                );
            }
            return null;
        });

        // Show the dialog and process the result
        Optional<GameSettings> result = setupDialog.showAndWait();

        result.ifPresent(settings -> {
            // Initialize game state with the settings
            gameState = new GameState(settings.getPlayerCount());
            gameState.setCurrentPlayerIndex(settings.getPlayerTurnIndex());

            // Parse and add initial tiles to player's hand using our fixed parser
            List<Tile> initialTiles = new ArrayList<>();

            // Parse the input string
            String handInput = settings.getInitialHand();
            if (handInput != null && !handInput.trim().isEmpty()) {
                initialTiles = utils.InputParser.parseTiles(handInput);
            }

            if (initialTiles.isEmpty()) {
                // If no valid tiles are provided, add some default tiles
                addDefaultTiles();
            } else {
                for (Tile tile : initialTiles) {
                    gameState.getCurrentPlayer().addTile(tile);
                }
            }
        });

        // If dialog is canceled or no result, use defaults
        if (!result.isPresent()) {
            gameState = new GameState(2);
            gameState.setCurrentPlayerIndex(0);
            addDefaultTiles();
        }
    }

    private void addDefaultTiles() {
        // Add 14 random tiles to the player's hand
        Player player = gameState.getCurrentPlayer();
        for (int i = 0; i < 14; i++) {
            Tile tile = tilePool.drawTile();
            if (tile != null) {
                player.addTile(tile);
            }
        }
    }

    private VBox createControlPanel() {
        VBox controlPanel = new VBox(10);
        controlPanel.setPadding(new Insets(10));
        controlPanel.setAlignment(Pos.TOP_CENTER);
        controlPanel.setPrefWidth(200);

        Label titleLabel = new Label("RUMMIKUB SOLVER");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        suggestMoveButton = new Button("Suggest Best Move");
        suggestMoveButton.setMaxWidth(Double.MAX_VALUE);
        suggestMoveButton.setOnAction(e -> suggestBestMove());

        Button endTurnButton = new Button("End Turn");
        endTurnButton.setMaxWidth(Double.MAX_VALUE);
        endTurnButton.setOnAction(e -> endTurn());

        Button drawTileButton = new Button("Draw Tile");
        drawTileButton.setMaxWidth(Double.MAX_VALUE);
        drawTileButton.setOnAction(e -> drawTile());

        Button undoButton = new Button("Undo Move");
        undoButton.setMaxWidth(Double.MAX_VALUE);
        undoButton.setOnAction(e -> undoMove());

        Button otherPlayerMovedButton = new Button("Other Player Moved");
        otherPlayerMovedButton.setMaxWidth(Double.MAX_VALUE);
        otherPlayerMovedButton.setOnAction(e -> handleOtherPlayerMove());

        Button resetBoardButton = new Button("Reset Game");
        resetBoardButton.setMaxWidth(Double.MAX_VALUE);
        resetBoardButton.setOnAction(e -> resetGame());

        // Create text field for adding tiles to the table
        Label addTilesLabel = new Label("Add Tiles to Table:");
        TextField addTilesField = new TextField();
        addTilesField.setPromptText("e.g., 1R 2R 3R");

        Button addTilesButton = new Button("Add");
        addTilesButton.setMaxWidth(Double.MAX_VALUE);
        addTilesButton.setOnAction(e -> {
            String tilesText = addTilesField.getText().trim();
            if (!tilesText.isEmpty()) {
                addTilesToTable(tilesText);
                addTilesField.clear();
            }
        });

        // Add a separator
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));

        controlPanel.getChildren().addAll(
                titleLabel,
                suggestMoveButton,
                endTurnButton,
                drawTileButton,
                undoButton,
                separator,
                otherPlayerMovedButton,
                addTilesLabel,
                addTilesField,
                addTilesButton,
                resetBoardButton
        );

        return controlPanel;
    }

    private void suggestBestMove() {
        // Generate possible moves
        List<Move> possibleMoves = moveGenerator.generatePossibleMoves(gameState);

        // Filter moves for first move requirement if needed
        if (gameState.isFirstMove()) {
            List<Move> validFirstMoves = new ArrayList<>();
            ScoreCalculator calculator = new ScoreCalculator();

            for (Move move : possibleMoves) {
                int totalValue = calculator.calculateTotalValue(move.getResultingGroups());
                if (totalValue >= 30) {
                    validFirstMoves.add(move);
                }
            }

            if (validFirstMoves.isEmpty()) {
                statusLabel.setText("No valid first moves found (need 30+ points). Try drawing a tile.");

                // Show dialog with the message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No Valid First Moves");
                alert.setHeaderText(null);
                alert.setContentText("No valid first moves found that meet the 30+ points requirement. Try drawing a tile.");
                alert.showAndWait();

                return;
            }

            possibleMoves = validFirstMoves;
        } else if (possibleMoves.isEmpty()) {
            statusLabel.setText("No valid moves found. Try drawing a tile.");

            // Show dialog with the message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Moves Available");
            alert.setHeaderText(null);
            alert.setContentText("No valid moves found. Try drawing a tile.");
            alert.showAndWait();

            return;
        }

        // Show dialog with suggested moves
        SuggestionDialog suggestionDialog = new SuggestionDialog(possibleMoves);
        Optional<Move> result = suggestionDialog.showAndWait();

        // If user selected a move, apply it
        result.ifPresent(move -> {
            applyMove(move);
            statusLabel.setText("Applied suggested move: " + move.tilesPlayedCount() + " tiles played.");

            // Check for win condition
            checkWinCondition();
        });
    }

    /**
     * Checks if the current player has won the game (no tiles left in hand)
     */
    private void checkWinCondition() {
        Player currentPlayer = gameState.getCurrentPlayer();

        if (currentPlayer.getHand().isEmpty()) {
            // Player has won!
            String winMessage = "Player " + (gameState.getCurrentPlayerIndex() + 1) + " has won the game!";
            statusLabel.setText(winMessage);

            // Show win dialog
            Alert winAlert = new Alert(Alert.AlertType.INFORMATION);
            winAlert.setTitle("Game Over");
            winAlert.setHeaderText("Winner!");
            winAlert.setContentText(winMessage + "\n\nYou can start a new game by clicking 'Reset Game'.");
            winAlert.showAndWait();

            // Disable buttons that shouldn't be used after win
            suggestMoveButton.setDisable(true);

            // Find and disable other gameplay buttons
            for (javafx.scene.Node node : ((VBox)((BorderPane)((Scene)tablePane.getScene()).getRoot()).getRight()).getChildren()) {
                if (node instanceof Button) {
                    Button button = (Button)node;
                    if (button.getText().equals("End Turn") ||
                            button.getText().equals("Draw Tile") ||
                            button.getText().equals("Undo Move")) {
                        button.setDisable(true);
                    }
                }
            }
        }
    }

    private void applyMove(Move move) {
        // Remove tiles played from hand
        Player currentPlayer = gameState.getCurrentPlayer();
        for (Tile tile : move.getTilesPlayed()) {
            currentPlayer.removeTile(tile);
        }

        // Update table with new groups
        gameState.getTable().replaceGroups(move.getResultingGroups());

        // Update UI
        updateUI();

        // If this was the first move, mark it as done
        if (gameState.isFirstMove()) {
            gameState.setFirstMoveDone();
        }

        // Check for win condition
        checkWinCondition();
    }

    private void endTurn() {
        // Check if the current table state is valid
        if (!gameState.getTable().isEmpty()) {
            TableManipulator validator = new TableManipulator();
            if (!validator.isValidTableState(gameState.getTable().getGroups())) {
                statusLabel.setText("Invalid table state. Please fix the arrangement before ending your turn.");

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Move");
                alert.setHeaderText(null);
                alert.setContentText("The current table arrangement is invalid. Please fix it before ending your turn.");
                alert.showAndWait();

                return;
            }

            // Check for first move requirement (30+ points)
            if (gameState.isFirstMove()) {
                int playedTilesValue = checkFirstMoveValue();

                if (playedTilesValue < 30) {
                    statusLabel.setText("Your first move must be worth at least 30 points. Current value: " +
                            playedTilesValue + " points.");

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid First Move");
                    alert.setHeaderText(null);
                    alert.setContentText("Your first move must be worth at least 30 points. " +
                            "Current value: " + playedTilesValue + " points.\n\n" +
                            "Use the 'Suggest Best Move' button to get help finding a valid opening move.");
                    alert.showAndWait();

                    return;
                }

                // Mark first move as completed
                gameState.setFirstMoveDone();
                statusLabel.setText("First move completed successfully! Value: " + playedTilesValue + " points.");
            }
        } else if (gameState.isFirstMove()) {
            // Cannot end turn without playing tiles on first move
            statusLabel.setText("You must play tiles worth at least 30 points on your first move.");

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid First Move");
            alert.setHeaderText(null);
            alert.setContentText("You must play tiles worth at least 30 points on your first move.");
            alert.showAndWait();

            return;
        }

        // Check for win condition before ending turn
        checkWinCondition();
        if (gameState.getCurrentPlayer().getHand().isEmpty()) {
            // Player has won, no need to continue
            return;
        }

        // Move to next player
        gameState.nextPlayer();

        // Clear played tiles tracking
        tablePane.clearPlayedTiles();

        statusLabel.setText("Turn ended. Now it's Player " + (gameState.getCurrentPlayerIndex() + 1) + "'s turn.");

        // Update UI
        updateUI();

        // Check if we need to simulate other players' moves
        if (gameState.getCurrentPlayerIndex() != 0) {  // Not the human player
            // In a real implementation, you would have AI logic here
            // For simplicity, we'll just provide a button to simulate the move
            suggestMoveButton.setDisable(true);
            statusLabel.setText("Waiting for Player " + (gameState.getCurrentPlayerIndex() + 1) +
                    " to complete their turn. Click 'Other Player Moved' when ready.");
        } else {
            suggestMoveButton.setDisable(false);
        }
    }

    /**
     * Check if the first move meets the 30+ points requirement
     * @return The total value of played tiles
     */
    private int checkFirstMoveValue() {
        ScoreCalculator calculator = new ScoreCalculator();
        return calculator.calculateTotalValue(gameState.getTable().getGroups());
    }

    private void handleOtherPlayerMove() {
        if (gameState.getCurrentPlayerIndex() == 0) {
            // It's the human player's turn, no need to handle other player move
            statusLabel.setText("It's your turn now.");
            return;
        }

        // Show dialog to update the table state
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Other Player's Move");
        dialog.setHeaderText("Update the board state after Player " + (gameState.getCurrentPlayerIndex() + 1) + "'s move");

        ButtonType doneButtonType = new ButtonType("Done", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(doneButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20, 10, 10, 10));

        // Add radio button to specify if player laid tiles
        ToggleGroup actionGroup = new ToggleGroup();
        RadioButton layTilesRadio = new RadioButton("Player laid tiles on the table");
        layTilesRadio.setToggleGroup(actionGroup);
        layTilesRadio.setSelected(false);

        RadioButton drewTileRadio = new RadioButton("Player drew a tile (didn't lay any tiles)");
        drewTileRadio.setToggleGroup(actionGroup);
        drewTileRadio.setSelected(true);

        content.getChildren().addAll(
                new Label("Select what the player did:"),
                layTilesRadio,
                drewTileRadio
        );

        Label infoLabel = new Label("If the player added tiles to the table, you can update the table state below:");

        TextField tilesField = new TextField();
        tilesField.setPromptText("e.g., 1R 2R 3R");

        Button addToTableButton = new Button("Add Tiles to Table");
        addToTableButton.setOnAction(e -> {
            String tilesText = tilesField.getText().trim();
            if (!tilesText.isEmpty()) {
                addTilesToTable(tilesText);
                tilesField.clear();
            }
        });

        VBox layTilesBox = new VBox(5, infoLabel, tilesField, addToTableButton);
        layTilesBox.setDisable(!layTilesRadio.isSelected());

        // Enable/disable fields based on selected action
        layTilesRadio.setOnAction(e -> layTilesBox.setDisable(false));
        drewTileRadio.setOnAction(e -> layTilesBox.setDisable(true));

        content.getChildren().add(layTilesBox);
        dialog.getDialogPane().setContent(content);

        // Process dialog result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == doneButtonType) {
                // Return the selected action
                return layTilesRadio.isSelected() ? "laid_tiles" : "drew_tile";
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        // Move to next player's turn
        if (result.isPresent()) {
            // Move to next player without clearing human player's hand
            gameState.nextPlayer();
            updateUI();

            // Check if we're back to the human player
            if (gameState.getCurrentPlayerIndex() == 0) {
                statusLabel.setText("It's your turn now.");
                suggestMoveButton.setDisable(false);
            } else {
                statusLabel.setText("Waiting for Player " + (gameState.getCurrentPlayerIndex() + 1) +
                        " to complete their turn. Click 'Other Player Moved' when ready.");
            }
        }
    }

    private void addTilesToTable(String tilesText) {
        List<Tile> tiles = InputParser.parseTiles(tilesText);

        if (tiles.isEmpty()) {
            // Show error if no valid tiles
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Tiles");
            alert.setHeaderText(null);
            alert.setContentText("No valid tiles found in the input. Please check the format and try again.");
            alert.showAndWait();
            return;
        }

        // Create a new group with these tiles
        Group newGroup = new Group(tiles);

        // Add group to table
        gameState.getTable().addGroup(newGroup);

        // Update UI
        updateUI();
    }

    private void drawTile() {
        // Show dialog for selecting a tile
        TilePickDialog dialog = new TilePickDialog();
        dialog.setTitle("Draw a Tile");

        // Show dialog and wait for result
        java.util.Optional<Tile> result = dialog.showAndWait();

        if (result.isPresent()) {
            // User selected a specific tile
            Tile selectedTile = result.get();
            gameState.getCurrentPlayer().addTile(selectedTile);
            statusLabel.setText("Drew a tile: " + selectedTile);
        } else {
            // User selected random or canceled - draw a random tile
            Tile drawnTile = tilePool.drawTile();

            if (drawnTile == null) {
                statusLabel.setText("No more tiles in the pool.");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Tile Pool Empty");
                alert.setHeaderText(null);
                alert.setContentText("There are no more tiles in the pool.");
                alert.showAndWait();

                return;
            }

            // Add tile to player's hand
            gameState.getCurrentPlayer().addTile(drawnTile);
            statusLabel.setText("Drew a random tile: " + drawnTile);
        }

        // Update UI
        updateUI();
    }

    private void undoMove() {
        // Implement move history and undo functionality
        // For simplicity, we'll just put back any tiles that were played this turn

        List<Tile> playedTiles = tablePane.getPlayedTiles();

        if (playedTiles.isEmpty()) {
            statusLabel.setText("No moves to undo.");
            return;
        }

        // Add played tiles back to hand
        Player currentPlayer = gameState.getCurrentPlayer();
        for (Tile tile : playedTiles) {
            currentPlayer.addTile(tile);
        }

        // Clear played tiles
        tablePane.clearPlayedTiles();

        // Reset table to initial state (for simplicity)
        // In a real implementation, you would restore the previous table state
        gameState.getTable().clear();

        // Update UI
        updateUI();

        statusLabel.setText("Move undone. All played tiles returned to your hand.");
    }

    private void resetGame() {
        // Confirm reset
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Reset Game");
        confirmAlert.setHeaderText("Reset Game");
        confirmAlert.setContentText("Are you sure you want to reset the game? This will clear the table and start over.");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Create a new game setup dialog
            setupGame();

            // Reset the table
            gameState.getTable().clear();
            tablePane.clearPlayedTiles();

            // Update UI
            updateUI();

            statusLabel.setText("Game reset. Ready to start.");
            suggestMoveButton.setDisable(false);
        }
    }

    /**
     * Updates the game status panel to reflect current game state
     */
    private void updateStatusPanel(Label playerTurnLabel, Label firstMoveLabel) {
        // Update player turn indicator
        if (gameState.getCurrentPlayerIndex() == 0) {
            playerTurnLabel.setText("Your Turn");
            playerTurnLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1976D2;");
            playerTurnLabel.setVisible(true);
        } else {
            playerTurnLabel.setText("Player " + (gameState.getCurrentPlayerIndex() + 1) + "'s Turn");
            playerTurnLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #757575;");
            playerTurnLabel.setVisible(true);
        }

        // Update first move indicator
        if (gameState.isFirstMove()) {
            firstMoveLabel.setText("First Move (Need 30+ pts)");
            firstMoveLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #D32F2F;");
            firstMoveLabel.setVisible(true);

            // Enable suggestion button in first move
            suggestMoveButton.setDisable(false);
            suggestMoveButton.setText("Find Valid First Move");
        } else {
            firstMoveLabel.setVisible(false);
            suggestMoveButton.setText("Suggest Best Move");
        }
    }

    private void updateUI() {
        // Update player hand display
        playerHandPane.updateHand(gameState.getCurrentPlayer().getHand());

        // Update table display
        tablePane.updateTable(gameState.getTable());

        // Update score pane
        scorePane.updateScores(gameState);

        // Get the top of the center panel which should contain our status panel
        if (tablePane.getScene() != null) {
            try {
                HBox statusPanel = (HBox) ((BorderPane)((BorderPane)((Scene)tablePane.getScene()).getRoot()).getCenter()).getTop();
                if (statusPanel != null) {
                    Label playerTurnLabel = (Label) statusPanel.getChildren().get(0);
                    Label firstMoveLabel = (Label) statusPanel.getChildren().get(1);
                    updateStatusPanel(playerTurnLabel, firstMoveLabel);
                }
            } catch (Exception e) {
                // If we can't find the status panel yet, that's okay
                // This can happen during initialization
                System.out.println("Status panel not accessible yet: " + e.getMessage());
            }
        }

        // Update the control panel buttons based on whose turn it is
        if (suggestMoveButton != null) {
            suggestMoveButton.setDisable(gameState.getCurrentPlayerIndex() != 0);
        }
    }

    // Helper class to store game settings
    private static class GameSettings {
        private final int playerCount;
        private final int playerTurnIndex;
        private final String initialHand;

        public GameSettings(int playerCount, int playerTurnIndex, String initialHand) {
            this.playerCount = playerCount;
            this.playerTurnIndex = playerTurnIndex;
            this.initialHand = initialHand;
        }

        public int getPlayerCount() {
            return playerCount;
        }

        public int getPlayerTurnIndex() {
            return playerTurnIndex;
        }

        public String getInitialHand() {
            return initialHand;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}