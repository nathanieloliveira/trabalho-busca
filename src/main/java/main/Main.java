package main;

import framework.Searcher;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.Callable;
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

        Observable.fromArray(2, 3, 4, 5, 6)
                .flatMap(integer -> {
                    PipesState initialState = new PipesState(integer, integer, integer);
//                    return Observable.fromCallable(() -> Searcher.solveAstar(checker, comparator, initialState));
//                    return Observable.fromCallable(() -> Searcher.solveDepth(checker, initialState));
                    return Observable.fromCallable(() -> Searcher.solveBreadth(checker, initialState));
                })
                .timeout(1, TimeUnit.MINUTES, Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .blockingSubscribe(result -> {
                    if (result.result != null) {
                        System.out.println("Nodos visitados: " + result.visitedNodes + " Tempo: " + (result.processingTimeNs / 1_000_000) + " ms");
                        System.out.println(result.result.toString());
                    } else {
                        System.out.println("nao encontrou valor para " + result.initialState.toString());
                    }
                }, throwable -> {
                    System.out.println("TIMEOUT");
                });
    }

}
