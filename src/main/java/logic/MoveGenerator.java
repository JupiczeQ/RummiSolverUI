package logic;

import java.util.*;
import model.*;
import utils.ScoreCalculator;

public class MoveGenerator {
    private GameLogic gameLogic;
    private TableManipulator tableManipulator;
    private ScoreCalculator scoreCalculator;

    public MoveGenerator(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
        this.tableManipulator = new TableManipulator();
        this.scoreCalculator = new ScoreCalculator();
    }

    /**
     * Generuje wszystkie możliwe ruchy dla danego stanu gry
     */
    public List<Move> generatePossibleMoves(GameState gameState) {
        List<Move> possibleMoves = new ArrayList<>();

        if (gameState.isFirstMove()) {
            // Użyj istniejącej logiki dla pierwszego ruchu
            possibleMoves.addAll(generateFirstMovesAsMoves(gameState));
        } else {
            // Generuj wszystkie możliwe manipulacje stołem
            possibleMoves.addAll(generateTableManipulations(gameState));

            // Generuj wszystkie możliwe sposoby wyłożenia kart z ręki
            possibleMoves.addAll(generatePlaysFromHand(gameState));
        }

        // Oblicz i ustaw ocenę dla każdego ruchu
        evaluateAndSortMoves(possibleMoves, gameState);

        return possibleMoves;
    }



    /**
     * Konwertuje rozwiązania pierwszego ruchu na obiekty Move
     */
    private List<Move> generateFirstMovesAsMoves(GameState gameState) {
        List<Move> moves = new ArrayList<>();
        List<Tile> hand = gameState.getCurrentPlayer().getHand();

        // Znajdź rozwiązania pierwszego ruchu
        List<List<Group>> solutions = generateFirstMoveSolutions(hand);

        // Przekształć rozwiązania na ruchy
        for (List<Group> solution : solutions) {
            // Początkowy stan stołu (pusty)
            List<Group> initialGroups = new ArrayList<>();

            // Stan wynikowy to rozwiązanie
            List<Group> resultingGroups = new ArrayList<>(solution);

            // Karty zagrane to wszystkie karty w rozwiązaniu
            List<Tile> tilesPlayed = new ArrayList<>();
            for (Group group : solution) {
                tilesPlayed.addAll(group.getTiles());
            }

            Move move = new Move(initialGroups, resultingGroups, tilesPlayed);
            moves.add(move);
        }

        return moves;
    }

    /**
     * Generuje rozwiązania dla pierwszego ruchu
     */
    public List<List<Group>> generateFirstMoveSolutions(List<Tile> hand) {
        List<Group> validGroups = gameLogic.findValidGroups(hand);

        // Ogranicz liczbę grup, aby uniknąć eksplozji kombinatorycznej
        int maxGroupsToConsider = Math.min(validGroups.size(), 50);
        if (validGroups.size() > maxGroupsToConsider) {
            // Sortuj według rozmiaru malejąco, aby priorytetowo traktować większe grupy
            validGroups.sort((g1, g2) -> Integer.compare(g2.getTiles().size(), g1.getTiles().size()));
            validGroups = validGroups.subList(0, maxGroupsToConsider);
        }

        List<List<Group>> solutions = new ArrayList<>();
        Set<String> solutionSignatures = new HashSet<>();

        // Wypróbuj kombinacje grup
        generateFirstMoveSolutionsHelper(validGroups, new ArrayList<>(), new HashSet<>(), 0, solutions, solutionSignatures);

        // Filtruj rozwiązania o wartości >= 30
        return solutions.stream()
                .filter(solution -> scoreCalculator.calculateTotalValue(solution) >= 30)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Metoda pomocnicza do generowania rozwiązań pierwszego ruchu
     */
    private void generateFirstMoveSolutionsHelper(
            List<Group> allGroups,
            List<Group> currentSolution,
            Set<Tile> usedTiles,
            int startIndex,
            List<List<Group>> solutions,
            Set<String> solutionSignatures) {

        // Wczesne zakończenie, jeśli mamy wystarczająco dużo rozwiązań
        if (solutions.size() >= 100) {
            return;
        }

        // Dodaj bieżące rozwiązanie, jeśli używa co najmniej jednej grupy
        if (!currentSolution.isEmpty()) {
            // Utwórz sygnaturę dla tego rozwiązania, aby uniknąć duplikatów
            String signature = generateSolutionSignature(currentSolution);
            if (!solutionSignatures.contains(signature)) {
                solutions.add(new ArrayList<>(currentSolution));
                solutionSignatures.add(signature);
            }
        }

        // Spróbuj dodać więcej grup
        for (int i = startIndex; i < allGroups.size(); i++) {
            Group group = allGroups.get(i);
            boolean canAdd = true;

            // Sprawdź, czy jakakolwiek karta jest już używana
            for (Tile tile : group.getTiles()) {
                if (usedTiles.contains(tile)) {
                    canAdd = false;
                    break;
                }
            }

            if (canAdd) {
                // Dodaj grupę do rozwiązania
                currentSolution.add(group);
                for (Tile tile : group.getTiles()) {
                    usedTiles.add(tile);
                }

                // Rekurencyjnie próbuj dodać więcej grup
                generateFirstMoveSolutionsHelper(allGroups, currentSolution, usedTiles, i + 1, solutions, solutionSignatures);

                // Backtrack - usuń grupę
                currentSolution.remove(currentSolution.size() - 1);
                for (Tile tile : group.getTiles()) {
                    usedTiles.remove(tile);
                }
            }
        }
    }

    /**
     * Generuje wszystkie możliwe manipulacje stołu (bez dodawania nowych kart)
     */
    private List<Move> generateTableManipulations(GameState gameState) {
        List<Move> moves = new ArrayList<>();
        Table table = gameState.getTable();

        // Jeśli stół jest pusty, nie ma nic do manipulacji
        if (table.isEmpty()) {
            return moves;
        }

        // Zbierz wszystkie kafelki ze stołu
        List<Tile> tableTiles = table.getAllTiles();

        // Znajdź wszystkie możliwe grupy z tych kafelków
        List<Group> allPossibleGroups = gameLogic.findValidGroups(tableTiles);

        // Znajdź wszystkie poprawne kombinacje grup, które używają dokładnie tych samych kafelków
        List<List<Group>> validRearrangements = tableManipulator.findValidRearrangements(allPossibleGroups, tableTiles);

        // Przekształć każde przestawienie w ruch
        for (List<Group> rearrangement : validRearrangements) {
            // Pomijamy identyczne ułożenie jak oryginalne
            if (isEquivalentArrangement(table.getGroups(), rearrangement)) {
                continue;
            }

            // Tworzymy nowy ruch
            Move move = new Move(table.getGroups(), rearrangement, new ArrayList<>());
            moves.add(move);
        }

        return moves;
    }

    /**
     * Sprawdza, czy dwa układy grup są równoważne (używają tych samych kafelków w tych samych grupach)
     */
    private boolean isEquivalentArrangement(List<Group> arrangement1, List<Group> arrangement2) {
        // Prosta heurystyka - jeśli liczba grup jest różna, to układy na pewno są różne
        if (arrangement1.size() != arrangement2.size()) {
            return false;
        }

        // Zbiór stringowych reprezentacji grup dla obu układów
        Set<String> groupStrings1 = new HashSet<>();
        Set<String> groupStrings2 = new HashSet<>();

        for (Group group : arrangement1) {
            groupStrings1.add(groupToString(group));
        }

        for (Group group : arrangement2) {
            groupStrings2.add(groupToString(group));
        }

        // Sprawdź, czy zbiory są identyczne
        return groupStrings1.equals(groupStrings2);
    }

    /**
     * Konwertuje grupę na unikalną reprezentację tekstową
     */
    private String groupToString(Group group) {
        List<String> tileStrings = new ArrayList<>();
        for (Tile tile : group.getTiles()) {
            tileStrings.add(tile.toString());
        }
        Collections.sort(tileStrings);
        return String.join(",", tileStrings);
    }

    /**
     * Generuje wszystkie możliwe ruchy dodające karty z ręki do istniejącego stołu
     */
    private List<Move> generatePlaysFromHand(GameState gameState) {
        List<Move> moves = new ArrayList<>();
        Table table = gameState.getTable();
        List<Tile> hand = gameState.getCurrentPlayer().getHand();

        // Jeśli ręka jest pusta, nie ma co dodawać
        if (hand.isEmpty()) {
            return moves;
        }

        // Dla każdej kombinacji kart z ręki
        for (int numCards = 1; numCards <= Math.min(hand.size(), 3); numCards++) {
            // Generuj wszystkie kombinacje kart o rozmiarze numCards
            List<List<Tile>> combinations = generateCombinations(hand, numCards);

            for (List<Tile> selectedTiles : combinations) {
                // Zbierz wszystkie kafelki - z ręki i ze stołu
                List<Tile> allTiles = new ArrayList<>(table.getAllTiles());
                allTiles.addAll(selectedTiles);

                // Znajdź wszystkie możliwe grupy z tych kafelków
                List<Group> allPossibleGroups = gameLogic.findValidGroups(allTiles);

                // Znajdź wszystkie poprawne kombinacje grup
                List<List<Group>> validArrangements = tableManipulator.findValidRearrangements(allPossibleGroups, allTiles);

                // Przekształć każde ułożenie w ruch
                for (List<Group> arrangement : validArrangements) {
                    // Sprawdź, czy wszystkie wybrane karty są używane
                    if (allTilesUsed(selectedTiles, arrangement)) {
                        Move move = new Move(table.getGroups(), arrangement, selectedTiles);
                        moves.add(move);
                    }
                }
            }
        }

        return moves;
    }

    /**
     * Sprawdza, czy wszystkie wybrane kafelki są używane w nowym układzie
     */
    private boolean allTilesUsed(List<Tile> selectedTiles, List<Group> groups) {
        // Zbierz wszystkie kafelki z grup
        Set<Tile> usedTiles = new HashSet<>();
        for (Group group : groups) {
            usedTiles.addAll(group.getTiles());
        }

        // Sprawdź, czy wszystkie wybrane kafelki są w zestawie
        for (Tile tile : selectedTiles) {
            if (!containsTile(usedTiles, tile)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Sprawdza, czy zbiór zawiera kafelek (porównanie po wartości i kolorze)
     */
    private boolean containsTile(Set<Tile> tiles, Tile searchTile) {
        for (Tile tile : tiles) {
            if (tile.equals(searchTile)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Generuje wszystkie kombinacje kart o danym rozmiarze
     */
    private List<List<Tile>> generateCombinations(List<Tile> tiles, int size) {
        List<List<Tile>> combinations = new ArrayList<>();
        generateCombinationsHelper(tiles, size, 0, new ArrayList<>(), combinations);
        return combinations;
    }


    /**
     * Metoda pomocnicza do generowania kombinacji
     */
    private void generateCombinationsHelper(List<Tile> tiles, int size, int startIndex, List<Tile> current, List<List<Tile>> result) {
        // Jeśli obecna kombinacja osiągnęła pożądany rozmiar, dodaj ją do wyników
        if (current.size() == size) {
            result.add(new ArrayList<>(current));
            return;
        }

        // Przejdź przez pozostałe elementy
        for (int i = startIndex; i < tiles.size(); i++) {
            // Dodaj obecny element do kombinacji
            current.add(tiles.get(i));

            // Rekurencyjnie generuj kombinacje dla pozostałych elementów
            generateCombinationsHelper(tiles, size, i + 1, current, result);

            // Usuń ostatni element, aby spróbować innej kombinacji (backtracking)
            current.remove(current.size() - 1);
        }
    }

    /**
     * Generuje unikalną sygnaturę dla rozwiązania
     */
    private String generateSolutionSignature(List<Group> solution) {
        List<String> groupSignatures = new ArrayList<>();

        for (Group group : solution) {
            List<String> tileStrings = group.getTiles().stream()
                    .map(Tile::toString)
                    .sorted()
                    .collect(java.util.stream.Collectors.toList());
            groupSignatures.add(String.join(",", tileStrings));
        }

        Collections.sort(groupSignatures);
        return String.join("|", groupSignatures);
    }

    /**
     * Ocenia i sortuje ruchy według ich potencjału
     */
    private void evaluateAndSortMoves(List<Move> moves, GameState gameState) {
        MoveEvaluator evaluator = new MoveEvaluator();

        for (Move move : moves) {
            double score = evaluator.evaluateMove(move, gameState);
            move.setScore((int) score);
        }

        // Sortuj ruchy według oceny (malejąco)
        moves.sort((m1, m2) -> Integer.compare(m2.getScore(), m1.getScore()));
    }

}