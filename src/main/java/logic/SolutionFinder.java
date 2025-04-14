package logic;

import java.util.*;
import java.util.stream.Collectors;
import model.Group;
import model.Tile;
import utils.ScoreCalculator;

public class SolutionFinder {
    private GameLogic gameLogic;
    private ScoreCalculator scoreCalculator;

    public SolutionFinder() {
        this.gameLogic = new GameLogic();
        this.scoreCalculator = new ScoreCalculator();
    }

    public List<List<Group>> findFirstMoveSolutions(List<Tile> hand) {
        List<Group> validGroups = gameLogic.findValidGroups(hand);

        // Ogranicz liczbę grup, aby uniknąć eksplozji kombinatorycznej
        int maxGroupsToConsider = Math.min(validGroups.size(), 100);
        if (validGroups.size() > maxGroupsToConsider) {
            // Sortuj według rozmiaru malejąco, aby priorytetowo traktować większe grupy
            validGroups.sort((g1, g2) -> Integer.compare(g2.getTiles().size(), g1.getTiles().size()));
            validGroups = validGroups.subList(0, maxGroupsToConsider);
        }

        List<List<Group>> solutions = new ArrayList<>();
        Set<String> solutionSignatures = new HashSet<>();

        // Wypróbuj kombinacje grup
        backtrackSolutions(validGroups, new ArrayList<>(), new HashSet<>(), 0, solutions, solutionSignatures);

        // Filtruj rozwiązania o wartości >= 30
        return solutions.stream()
                .filter(solution -> scoreCalculator.calculateTotalValue(solution) >= 30)
                .collect(Collectors.toList());
    }

    private void backtrackSolutions(
            List<Group> allGroups,
            List<Group> currentSolution,
            Set<Tile> usedTiles,
            int startIndex,
            List<List<Group>> solutions,
            Set<String> solutionSignatures) {

        // Wczesne zakończenie, jeśli mamy wystarczająco dużo rozwiązań
        if (solutions.size() >= 1000) {
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
                backtrackSolutions(allGroups, currentSolution, usedTiles, i + 1, solutions, solutionSignatures);

                // Backtrack - usuń grupę
                currentSolution.remove(currentSolution.size() - 1);
                for (Tile tile : group.getTiles()) {
                    usedTiles.remove(tile);
                }
            }
        }
    }

    // Metoda pomocnicza do generowania unikalnej sygnatury dla rozwiązania
    private String generateSolutionSignature(List<Group> solution) {
        // Sortuj grupy, aby zapewnić spójną kolejność
        List<String> groupSignatures = new ArrayList<>();

        for (Group group : solution) {
            List<String> tileStrings = group.getTiles().stream()
                    .map(Tile::toString)
                    .sorted()
                    .collect(Collectors.toList());
            groupSignatures.add(String.join(",", tileStrings));
        }

        Collections.sort(groupSignatures);
        return String.join("|", groupSignatures);
    }
}