import java.util.*;

public class AStarClosedSet {

    public static State solve(State startState, int M, int K) {
        
        // PriorityQueue that uses the compareTo method declared on State class to order each state based on f= g + h
        PriorityQueue<State> frontier = new PriorityQueue<>();

        // Closed set that saves states already visited
        Set<State> closedSet = new HashSet<>();

        // Adds the Starting State to the frontier
        frontier.add(startState);

        while (!frontier.isEmpty()) {
            // Choose the state with the minimum f value
            State currentState = frontier.poll();

            // Check if the current state is a final state
            if (currentState.isFinalState()) {
                return currentState; // A solution is found
            }

            // Adding current state to the closed set
            closedSet.add(currentState);

            // Generate all the valid successors of the current state
            List<State> successors = currentState.generateSuccessors(M, K);

            for (State successor : successors) {
                // if the successor is already in the closed set, ignore it
                if (closedSet.contains(successor)) {
                    continue;
                }

                //Check whether there is state in the frontier that is equal to the evaluating successor and has a larger g value
                State existingState = null;
                for (State state : frontier) {
                    if (state.equals(successor)) {//Found a state that is in the frontier and equal to the successor
                        existingState = state;
                        break;
                    }
                }

                if (existingState != null) {//if a state was found
                    if (successor.getG() < existingState.getG()) {//Check if the path from the starting state to the succesor is shorter
                        frontier.remove(existingState);//remove old state with longer path
                        frontier.add(successor);//add new state with shorter path
                    }
                } else {//if no state was found in the frontier equal to the successor, the successor needs to be added to the frontier
                    frontier.add(successor);
                }
            }
        }

        // No solution could be found
        return null;
    }
}
