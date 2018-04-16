package main;

import framework.State;

import java.util.*;

public class PipesState implements State<PipesState> {

    private int heuristic;
    private float occupiedMultiplier;
    private int sumDifSquared;
    private double sizeMultiplier;

    enum Direction {
        RIGHT,
        DOWN,
        LEFT,
        UP
    }

    private int width;
    private int height;
    private ArrayList<ArrayList<Point>> pipes;

    public PipesState(int width, int height, int pipes) {
        this.width = width;
        this.height = height;
        this.pipes = new ArrayList<>(pipes);
        for (int i = 0; i < pipes; i++) {
            this.pipes.add(new ArrayList<>());
        }
    }

    private PipesState(int width, int height, ArrayList<ArrayList<Point>> pipes) {
        this.width = width;
        this.height = height;
        this.pipes = new ArrayList<>(pipes.size());
        for (ArrayList<Point> pipe : pipes) {
            ArrayList<Point> pipeCopy = new ArrayList<>(pipe.size());
            for (Point point : pipe) {
                pipeCopy.add(new Point(point.x, point.y));
            }
            this.pipes.add(pipeCopy);
        }
    }

    public PipesState copy() {
        return new PipesState(width, height, pipes);
    }

    @Override
    public List<PipesState> generateSuccessors() {
        ArrayList<PipesState> successors = new ArrayList<>();

        List<Integer> empties;
        if (isEmpty()) {
            // nao tem nada
            for (int i = 0; i < pipes.size(); i++) {
                for (int x = 1; x < width + 1; x++) {
                    for (int y = 1; y < height + 1; y++) {
                        PipesState pipesState = copyAddPipe(i, x, y);
                        successors.add(pipesState);
                    }
                }
            }
        } else if ((empties = getEmptyPipes()) != null && empties.size() > 0) {
            // nao tem todos os começos de canos posicionados ainda
            for (Integer pipe : empties) {
                for (int x = 1; x < width + 1; x++) {
                    for (int y = 1; y < height + 1; y++) {
                        if (!occupied(x, y)) {
                            PipesState pipesState = copyAddPipe(pipe, x, y);
                            successors.add(pipesState);
                        }
                    }
                }
            }
        } else {
            // o resto
            for (int i = 0; i < pipes.size(); i++) {
                for (Direction direction : Direction.values()) {
                    checkNeighbor(successors, i, direction);
                }
            }
        }

        return successors;
    }

    private List<Integer> getEmptyPipes() {
        ArrayList<Integer> empties = new ArrayList<>(pipes.size());
        for (int i = 0; i < pipes.size(); i++) {
            ArrayList<Point> pipe = pipes.get(i);
            if (pipe.size() == 0) {
                empties.add(i);
            }
        }
        return empties;
    }

    private PipesState copyAddPipe(int pipe, int x, int y) {
        PipesState copy = copy();
        copy.pipes.get(pipe).add(new Point(x, y));
        return copy;
    }

    private void checkNeighbor(ArrayList<PipesState> successors, int pipeN, Direction direction) {
        ArrayList<Point> pipe = pipes.get(pipeN);
        Point lastPoint = pipe.get(pipe.size() - 1);

        int x;
        int y;
        switch (direction) {
            case DOWN:
                x = lastPoint.x;
                y = lastPoint.y + 1;
                break;
            case LEFT:
                x = lastPoint.x - 1;
                y = lastPoint.y;
                break;
            case UP:
                x = lastPoint.x;
                y = lastPoint.y - 1;
                break;
            case RIGHT:
                default:
                x = lastPoint.x + 1;
                y = lastPoint.y;
        }

        if (checkNeighbor(x, y)) {

            successors.add(copyAddPipe(pipeN, x, y));
        }
    }

    private boolean checkNeighbor(int x, int y) {
        if (checkBounds(x, y) && !occupied(x, y)) {
            return true;
        }
        return false;
    }

    private ArrayList<Point> findSmallestPipe() {
        ArrayList<Point> selected = null;
        int smallest = Integer.MAX_VALUE;
        for (int i = pipes.size() - 1; i >= 0; i--) {
            ArrayList<Point> pipe = pipes.get(i);
            int size = pipe.size();
            if (size <= smallest) {
                selected = pipe;
                smallest = size;
            }
        }
        return selected;
    }

    @Override
    public int calculateHeuristic(PipesState goal) {
        boolean allBiggerThan2 = true;
        int[] sizes = new int[pipes.size()];
        int biggest = Integer.MIN_VALUE;
        for (int i = 0; i < pipes.size(); i++) {
            int size = pipes.get(i).size();
            sizes[i] = size;
            if (size < 2) {
                allBiggerThan2 = false;
            }

            if (biggest < size) {
                biggest = size;
            }
        }

        int occupied = countOccupied();
        if (occupied == 0) {
            return 0;
        }

        int squares = (width * height);
        if (occupied == squares && allBiggerThan2) {
            // alvo
            return 0;
        }

        sizeMultiplier = 1000 * Math.pow(pipes.size() * 2 + 1 - occupied, 2);
        for (ArrayList<Point> pipe : pipes) {
            if (pipe.size() < 2) {
//                sizeMultiplier -= 1000;
            }
        }
        if (allBiggerThan2) {
            sizeMultiplier = 1;
        }

        sumDifSquared = 1;
        for (int i = 0; i < sizes.length; i++) {
            sumDifSquared += Math.pow(biggest - sizes[i], 2);
        }

        occupiedMultiplier = 1000 * squares / (float) occupied;

        heuristic = (int) (sizeMultiplier * occupiedMultiplier * sumDifSquared);
        return heuristic;
    }

    public boolean isFull() {
        for (int x = 1; x < width+1; x++) {
            for (int y = 1; y < height+1; y++) {
                if (!occupied(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isEmpty() {
        for (ArrayList<Point> pipe : pipes) {
            if (pipe.size() > 0) {
                return false;
            }
        }
        return true;
    }

    private boolean checkBounds(int x, int y) {
        if (x <= 0 || x > width) return false;
        return y > 0 && y <= height;
    }

    private int countOccupied() {
        int count = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (occupied(i + 1, j + 1)) count++;
            }
        }
        return count;
    }

    public boolean occupied(int x, int y) {
        return occupied(new Point(x, y));
    }

    public boolean occupied(Point p) {
        for (ArrayList<Point> pipe : pipes) {
            for (Point p2 : pipe) {
                if (p.equals(p2)) {
                    return true;
                }
            }
        }
        return false;
    }

    public class Point {
        int x;
        int y;

        public Point() {}

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj instanceof Point) {
                Point p = (Point) obj;
                return this.x == p.x && this.y == p.y;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PipesState that = (PipesState) o;
        return width == that.width &&
                height == that.height &&
                Objects.equals(pipes, that.pipes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, pipes);
    }

    @Override
    public String toString() {
        String string = "W: " + width + " H: " + height + " Heuristic: " + heuristic + "\n";
        for (int i = 0; i < pipes.size(); i++) {
            ArrayList<Point> pipe = pipes.get(i);
            String p = "Pipe " + (i + 1) + " size " + pipe.size() + " : ";
            for (Point point : pipe) {
                p = p.concat(point.toString());
            }
            string = string.concat(p.concat("\n"));
        }
        return string;
    }

    public ArrayList<ArrayList<Point>> getPipes() {
        return pipes;
    }
}
