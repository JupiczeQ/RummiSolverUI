package logic;

import java.util.*;
import model.*;
import utils.ScoreCalculator;

public class MoveEvaluator {
    private ScoreCalculator scoreCalculator;

    public MoveEvaluator() {
        this.scoreCalculator = new ScoreCalculator();
    }

    public double evaluateMove(Move move, GameState gameState) {
        // Score based on tiles played
        int tilesPlayed = move.tilesPlayedCount();

        // Bonus for playing high value tiles
        int valueOfTilesPlayed = 0;
        for (Tile tile : move.getTilesPlayed()) {
            valueOfTilesPlayed += tile.getVal();
        }

        // Preference for playing jokers last
        int jokerPenalty = 0;
        for (Tile tile : move.getTilesPlayed()) {
            if (tile.isJoker()) {
                jokerPenalty += 10;
            }
        }

        // Final score calculation
        return tilesPlayed * 10 + valueOfTilesPlayed - jokerPenalty;
    }

    public List<Move> rankMoves(List<Move> moves, GameState gameState) {
        // Sort moves by score
        moves.sort((m1, m2) -> Double.compare(
                evaluateMove(m2, gameState),
                evaluateMove(m1, gameState)
        ));

        return moves;
    }
}