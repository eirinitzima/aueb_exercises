import java.util.*;

public class Main {
    public static void main(String[] args) {
        
        int N = Integer.parseInt(args[0]); //Number of Missionaries (same for Cannibals)
        int M = Integer.parseInt(args[1]); //Boat capacity
        int K = Integer.parseInt(args[2]); // Maximum allowed crossings

        // Starting State Creation
        State startState = new State(N, N, 0, 0, 0, 0, 0, 0);

        //Time before calling A* method to solve the problem 
        long start = System.currentTimeMillis();

        // Find a solution using A* -if there is one-
        State solution = AStarClosedSet.solve(startState, M, K);
        
        //Time after the A* has finished executing
        long end = System.currentTimeMillis();

        if (solution != null) {//There is a solution
            System.out.println("A solution has been found!");
            printSolution(solution);
        } else {
            System.out.println("There is no solution.");
        }

        System.out.println("Search time:" + (double)(end - start) / 1000 + " sec.");  // total time of searching in seconds.
    }

    //Prints the solution path
    private static void printSolution(State state) {
        List<State> path = new ArrayList<>();
        //From final state to starting state
        while (state != null) {
            path.add(state);
            state = state.getParent();
        }
        //Reversing the path(from starting state to final state)
        Collections.reverse(path);
        for (State s : path) {
            System.out.println(s);//State's toString method being called 
        }
    }
}
