package model;

import java.util.*;

public class GameState {
    private List<Player> players;
    private int currentPlayerIndex;
    private Table table;
    private boolean isFirstMove;
    private int totalMoves;

    public GameState(int playerCount) {
        this.players = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            this.players.add(new Player());
        }
        this.currentPlayerIndex = 0;
        this.table = new Table();
        this.isFirstMove = true;
        this.totalMoves = 0;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public List<Player> getAllPlayers() {
        return new ArrayList<>(players);
    }

    public Player getPlayer(int index) {
        return players.get(index);
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int index) {
        this.currentPlayerIndex = index;
    }

    public void nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    public Table getTable() {
        return table;
    }

    public boolean isFirstMove() {
        return isFirstMove;
    }

    public void setFirstMoveDone() {
        this.isFirstMove = false;
    }

    public int getTotalMoves() {
        return totalMoves;
    }

    public void incrementMoves() {
        this.totalMoves++;
    }

    /**
     * Creates a copy of the current game state
     */
    public GameState clone() {
        GameState clonedState = new GameState(players.size());

        // Copy all players' cards
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            Player clonedPlayer = clonedState.getPlayer(i);

            for (Tile tile : player.getHand()) {
                clonedPlayer.addTile(new Tile(tile.getVal(), tile.getColor(), tile.isJoker()));
            }
        }

        // Set current player
        clonedState.setCurrentPlayerIndex(this.currentPlayerIndex);

        // Copy table
        clonedState.table = table.clone();
        clonedState.isFirstMove = this.isFirstMove;
        clonedState.totalMoves = this.totalMoves;

        return clonedState;
    }
}