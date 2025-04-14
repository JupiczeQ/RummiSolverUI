package com.example.demojavafx.components;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import model.Group;
import model.Move;
import model.Tile;

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
        ButtonType selectButtonType = new ButtonType("Select Move", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

        // Create the content area
        VBox content = new VBox(10);
        content.setPadding(new Insets(20, 10, 10, 10));

        // Create toggle group for move selection
        moveToggleGroup = new ToggleGroup();

        // Add radio buttons for each move
        for (int i = 0; i < suggestedMoves.size(); i++) {
            Move move = suggestedMoves.get(i);

            RadioButton radioButton = new RadioButton("Move " + (i + 1) + " (Score: " + move.getScore() + ")");
            radioButton.setToggleGroup(moveToggleGroup);
            radioButton.setUserData(move);

            if (i == 0) {
                radioButton.setSelected(true);
            }

            // Add description of the move
            TextArea moveDescription = new TextArea(formatMoveDescription(move));
            moveDescription.setEditable(false);
            moveDescription.setPrefRowCount(5);
            moveDescription.setWrapText(true);

            VBox moveBox = new VBox(5, radioButton, moveDescription);
            moveBox.setPadding(new Insets(5));
            moveBox.setStyle("-fx-border-color: #CCC; -fx-border-radius: 5;");

            content.getChildren().add(moveBox);
        }

        getDialogPane().setContent(content);

        // Convert the result to a Move when the select button is clicked
        setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType) {
                return (Move) moveToggleGroup.getSelectedToggle().getUserData();
            }
            return null;
        });
    }

    private String formatMoveDescription(Move move) {
        StringBuilder sb = new StringBuilder();

        if (!move.getTilesPlayed().isEmpty()) {
            sb.append("Tiles to play: ");
            for (Tile tile : move.getTilesPlayed()) {
                sb.append(tile).append(" ");
            }
            sb.append("\n\n");
        }

        sb.append("Resulting groups:\n");
        for (Group group : move.getResultingGroups()) {
            sb.append("- ");
            for (Tile tile : group.getTiles()) {
                sb.append(tile).append(" ");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}