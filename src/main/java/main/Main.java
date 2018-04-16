package main;

import framework.Searcher;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {

        Searcher.ConstraintChecker<PipesState> checker = (state) -> {
            for (ArrayList<PipesState.Point> points : state.getPipes()) {
                if (points.size() < 2) {
                    return false;
                }
            }
            return state.isFull();
        };

        Comparator<PipesState> comparator = (o1, o2) -> {
            int h1 = o1.calculateHeuristic(null);
            int h2 = o2.calculateHeuristic(null);
            return Integer.compare(h1, h2);
        };

        if (args.length >= 3) {

            int width = Integer.parseInt(args[0]);
            int height = Integer.parseInt(args[1]);
            int pipes = Integer.parseInt(args[2]);

            PipesState initialState = new PipesState(width, height, pipes);
            Searcher.SearchResult<PipesState> result = Searcher.solveAstar(checker, comparator, initialState);
            if (result.result != null) {
                System.out.println("Nodos visitados: " + result.visitedNodes + " Tempo: " + (result.processingTimeNs / 1_000_000) + " ms");
                System.out.println(result.result.toString());
            } else {
                System.out.println("nao encontrou valor para " + result.initialState.toString());
            }

        } else if (args.length > 0) {

            char algorithm = args[0].charAt(0);

            Observable.fromArray(2, 3, 4, 5, 6)
                    .flatMap(integer -> {
                        PipesState initialState = new PipesState(integer, integer, integer);
                        switch (algorithm) {
                            case 'a':
                                return Observable.fromCallable(() -> Searcher.solveAstar(checker, comparator, initialState));
                            case 'd':
                                return Observable.fromCallable(() -> Searcher.solveDepth(checker, initialState));
                            case 'b':
                                return Observable.fromCallable(() -> Searcher.solveBreadth(checker, initialState));
                            default:
                                printUsage();
                                System.exit(0);
                                return Observable.empty();
                        }
                    })
                    .timeout(1, TimeUnit.MINUTES, Schedulers.computation())
                    .subscribeOn(Schedulers.computation())
                    .blockingSubscribe(result -> {
                        if (result.result != null) {
                            System.out.println("Nodos visitados: " + result.visitedNodes + " Tempo: " + (result.processingTimeNs / 1_000_000) + " ms");
                            System.out.println(result.result.toString());
                        } else {
                            System.out.println("nao encontrou solução para " + result.initialState.toString());
                        }
                    }, throwable -> {
                        System.out.println("TIMEOUT");
                    });
        } else {
            printUsage();
        }
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("trabalhoia width height pipes -> tenta resolver o problema especificado utiliando A*.");
        System.out.println("    ou");
        System.out.println("trabalhoia algorithm=[a, d, b] -> a = A*, d = profundidade, b = largura");
        System.out.println("Resolve problemas (2,2,2) até (6,6,6) e printa resultados. Timeout de 1 minuto.");
    }

}
