package framework;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.util.*;

public class Searcher<T extends State> {

    public interface ConstraintChecker<T> {
        boolean isValid(T state);
    }

    private Searcher() {
    }

    static public <T extends State> SearchResult<T> solveDepth(@NotNull ConstraintChecker<T> checker, @NotNull T initialState) {
        HashSet<T> evaluated = new HashSet<>();
        Stack<T> queue = new Stack<>();

        queue.add(initialState);

        SearchResult<T> result = new SearchResult<>();

        long t1 = System.nanoTime();

        while (!queue.isEmpty()) {
            T current = queue.pop();
            if (current == null) {
                break;
            }
            SearchResult<T> x = evalState(checker, evaluated, queue, result, t1, current);
            if (x != null) return x;
        }

        return result;
    }

    public static <T extends State> SearchResult<T> solveBreadth(@NotNull ConstraintChecker<T> checker, @NotNull T initialState) {
        HashSet<T> evaluated = new HashSet<>();
        Deque<T> queue = new LinkedList<>();

        queue.add(initialState);

        SearchResult<T> result = new SearchResult<>();

        long t1 = System.nanoTime();

        while (!queue.isEmpty()) {
            T current = queue.removeFirst();
            if (current == null) {
                // expanded all nodes
                break;
            }
            SearchResult<T> x = evalState(checker, evaluated, queue, result, t1, current);
            if (x != null) return x;
        }

        return result;
    }

    public static <T extends State> SearchResult<T> solveAstar(@NotNull ConstraintChecker<T> checker, @NotNull Comparator<T> heuristic, @NotNull T initialState) {
        HashSet<T> evaluated = new HashSet<>();
        PriorityQueue<T> queue = new PriorityQueue<>(heuristic);

        queue.add(initialState);

        SearchResult<T> result = new SearchResult<>();
        result.initialState = initialState;

        long t1 = System.nanoTime();

        while (!queue.isEmpty()) {
            T current = queue.poll();
            if (current == null) {
                // expanded all nodes
                break;
            }

            SearchResult<T> x = evalState(checker, evaluated, queue, result, t1, current);
            if (x != null) return x;
        }

        return result;
    }

    private static <T extends State> SearchResult<T> evalState(@NotNull ConstraintChecker<T> checker, HashSet<T> evaluated, Collection<T> queue, SearchResult<T> result, long startTime, T current) {
        result.visitedNodes += 1;

        if (checker.isValid(current)) {
            long t2 = System.nanoTime();
            result.processingTimeNs = t2 - startTime;
            result.result = current;
            return result;
        }

        evaluated.add(current);

        List<T> successors = current.generateSuccessors();
        for (T s : successors) {
            if (!evaluated.contains(s)) {
                queue.add(s);
            }
        }
        return null;
    }

    public static class SearchResult<T> {
        public T result = null;
        public int visitedNodes;
        public long processingTimeNs;
        public T initialState;
    }

}
