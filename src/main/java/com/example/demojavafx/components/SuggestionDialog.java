package com.example.demojavafx.components;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.Group;
import model.Move;
import model.Tile;
import utils.ScoreCalculator;

import java.util.List;

/**
 * Dialog that displays suggested moves to the player.
 */
public class SuggestionDialog extends Dialog<Move> {
    private List<Move> suggestedMoves;
    private ToggleGroup moveToggleGroup;

    public SuggestionDialog(List<Move> suggestedMoves) {
        this.suggestedMoves = suggestedMoves;
        initialize();
    }

    private void initialize() {
        setTitle("Suggested Moves");
        setHeaderText("Select a suggested move:");

        // Set the button types
        ButtonType selectButtonType = new ButtonType("Apply Selected Move", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

        // Create the content area
        VBox content = new VBox(15);
        content.setPadding(new Insets(20, 10, 10, 10));

        // Add explanatory text
        Label explanationLabel = new Label("The solver has found " + suggestedMoves.size() +
                " possible moves, ranked by score (higher is better).");
        explanationLabel.setWrapText(true);
        explanationLabel.setPadding(new Insets(0, 0, 10, 0));
        content.getChildren().add(explanationLabel);

        // Create toggle group for move selection
        moveToggleGroup = new ToggleGroup();

        // Create scrollable container for moves
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);

        VBox movesContainer = new VBox(10);
        movesContainer.setPadding(new Insets(5));

        // Add radio buttons for top moves (limit to top 10 for usability)
        int displayLimit = Math.min(suggestedMoves.size(), 10);
        for (int i = 0; i < displayLimit; i++) {
            Move move = suggestedMoves.get(i);

            // Create container for this move option
            VBox moveBox = createMoveBox(move, i);

            // Add to container
            movesContainer.getChildren().add(moveBox);
        }

        scrollPane.setContent(movesContainer);
        content.getChildren().add(scrollPane);

        // Add info label if there are more moves not shown
        if (suggestedMoves.size() > displayLimit) {
            Label moreMovesLabel = new Label("Note: " + (suggestedMoves.size() - displayLimit) +
                    " additional lower-scoring moves are not shown.");
            moreMovesLabel.setStyle("-fx-font-style: italic;");
            content.getChildren().add(moreMovesLabel);
        }

        getDialogPane().setContent(content);
        getDialogPane().setPrefWidth(600);
        getDialogPane().setPrefHeight(600);

        // Make the dialog resizable
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.setResizable(true);
        stage.setMinHeight(400);
        stage.setMinWidth(500);

        // Select first move by default
        if (!suggestedMoves.isEmpty() && moveToggleGroup.getToggles().size() > 0) {
            moveToggleGroup.getToggles().get(0).setSelected(true);
        }

        // Convert the result to a Move when the select button is clicked
        setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType && moveToggleGroup.getSelectedToggle() != null) {
                return (Move) moveToggleGroup.getSelectedToggle().getUserData();
            }
            return null;
        });
    }

    private VBox createMoveBox(Move move, int index) {
        VBox moveBox = new VBox(5);
        moveBox.setPadding(new Insets(10));
        moveBox.setStyle("-fx-border-color: #CCC; -fx-border-radius: 5; -fx-background-color: #F8F8F8;");

        // Create header with radio button and score
        HBox header = new HBox(10);

        RadioButton radioButton = new RadioButton("Move " + (index + 1));
        radioButton.setToggleGroup(moveToggleGroup);
        radioButton.setUserData(move);

        Label scoreLabel = new Label("Score: " + move.getScore());
        scoreLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        // Add first move indicator if appropriate
        if (move.tilesPlayedCount() > 0) {
            Label tilesLabel = new Label("(" + move.tilesPlayedCount() + " tiles)");
            tilesLabel.setStyle("-fx-font-style: italic;");
            header.getChildren().addAll(radioButton, scoreLabel, tilesLabel);
        } else {
            header.getChildren().addAll(radioButton, scoreLabel);
        }

        // Add visual preview of tiles to play
        VBox previewBox = new VBox(8);
        previewBox.setPadding(new Insets(5));

        if (!move.getTilesPlayed().isEmpty()) {
            // Show tiles to be played from hand
            HBox tilesBox = new HBox(5);
            tilesBox.setPadding(new Insets(5));
            tilesBox.setStyle("-fx-background-color: #EFEFEF; -fx-border-color: #DDD; -fx-border-width: 1;");

            Label tilesPlayedLabel = new Label("Tiles to play:");
            tilesPlayedLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
            tilesBox.getChildren().add(tilesPlayedLabel);

            for (Tile tile : move.getTilesPlayed()) {
                TileView tileView = new TileView(tile);
                tileView.setPrefSize(30, 40); // Smaller size for preview
                tilesBox.getChildren().add(tileView);
            }

            previewBox.getChildren().add(tilesBox);
        } else {
            // For rearrangement moves
            Label rearrangeLabel = new Label("This move rearranges existing tiles on the table.");
            rearrangeLabel.setFont(Font.font("System", FontWeight.NORMAL, 11));
            rearrangeLabel.setStyle("-fx-font-style: italic;");
            previewBox.getChildren().add(rearrangeLabel);
        }

        // Add visual preview of resulting groups
        Label resultingGroupsLabel = new Label("Resulting Groups:");
        resultingGroupsLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
        previewBox.getChildren().add(resultingGroupsLabel);

        // Create a container for all groups
        VBox groupsContainer = new VBox(5);

        // Add each group with visual tile representation
        for (Group group : move.getResultingGroups()) {
            HBox groupBox = new HBox(3);
            groupBox.setPadding(new Insets(3));
            groupBox.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-border-color: #CCC; -fx-border-width: 1;");

            // Add visual representation of tiles in group
            for (Tile tile : group.getTiles()) {
                TileView tileView = new TileView(tile);
                tileView.setPrefSize(25, 35); // Even smaller for group preview
                groupBox.getChildren().add(tileView);
            }

            groupsContainer.getChildren().add(groupBox);
        }

        // Add scrollable container for groups if there are many
        ScrollPane groupsScrollPane = new ScrollPane(groupsContainer);
        groupsScrollPane.setFitToWidth(true);
        groupsScrollPane.setPrefHeight(Math.min(150, move.getResultingGroups().size() * 45));
        groupsScrollPane.setStyle("-fx-background-color: transparent;");
        previewBox.getChildren().add(groupsScrollPane);

        // Add score information for first move
        if (move.tilesPlayedCount() > 0) {
            // Calculate total value for first move check
            ScoreCalculator calculator = new ScoreCalculator();
            int totalValue = calculator.calculateTotalValue(move.getResultingGroups());

            Label valueLabel = new Label("Total Value: " + totalValue + " points" +
                    (totalValue >= 30 ? " ✓" : " ✗"));
            valueLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
            valueLabel.setStyle(totalValue >= 30 ?
                    "-fx-text-fill: #388E3C;" :
                    "-fx-text-fill: #D32F2F;");
            previewBox.getChildren().add(valueLabel);
        }

        // Create move details (text version for clarity)
        TextArea moveDescription = new TextArea(formatMoveDescription(move));
        moveDescription.setEditable(false);
        moveDescription.setPrefRowCount(3);
        moveDescription.setWrapText(true);
        moveDescription.setStyle("-fx-background-color: #FFFFFF;");

        // Add components to move box
        moveBox.getChildren().addAll(header, previewBox, moveDescription);

        return moveBox;
    }

    private String formatMoveDescription(Move move) {
        StringBuilder sb = new StringBuilder();

        if (!move.getTilesPlayed().isEmpty()) {
            sb.append("Tiles to play: ");
            for (Tile tile : move.getTilesPlayed()) {
                sb.append(tile).append(" ");
            }
            sb.append("\n\n");
        } else {
            sb.append("This move rearranges existing tiles on the table.\n\n");
        }

        sb.append("Resulting groups:\n");
        int groupNum = 1;
        for (Group group : move.getResultingGroups()) {
            sb.append(groupNum++).append(") ");
            for (Tile tile : group.getTiles()) {
                sb.append(tile).append(" ");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}