import java.util.*;

public class State implements Comparable<State>{
    private int missionariesLeft;
    private int cannibalsLeft;
    private int missionariesRight;
    private int cannibalsRight;
    private int boatSide; // 0 = Left, 1 = Right

    // Cost to reach this State from the Starting State
    private int g;

    // Heuristic cost from this State to the Final State
    private int h;
    
    // f = h + g 
    public int f;

    private int crossings;

    // Parent State - For path reconstruction-
    private State parent; 

    public State(int ml, int cl, int mr, int cr, int boatSide, int g, int h, int crs) {
        this.missionariesLeft = ml;
        this.cannibalsLeft = cl;
        this.missionariesRight = mr;
        this.cannibalsRight = cr;
        this.boatSide = boatSide;
        this.g = g;
        this.h = h;
        this.f = h + g;
        this.crossings = crs;
    }

    //Copy Constructor
    public State(State S){
        this(S.missionariesLeft, S.cannibalsLeft, S.missionariesRight, S.cannibalsRight, S.boatSide, S.g, S.h, S.crossings);
    }
    
    //Setters
    private void setParent(State p) {this.parent=p;}


    // Getters
    public int getG() { return this.g; }
    public int getF() { return this.f; }
    public int getCrossings() { return this.crossings; }
    public State getParent() { return this.parent; }


    //When there are no cannibals and missionaries on the Left side the State is Final
    public boolean isFinalState() {
		return this.cannibalsLeft == 0 && this.missionariesLeft == 0;
	}

    //Check Whether the State is Valid
    private boolean isValidState(int ml, int cl, int mr, int cr) {
        return (ml >= 0 && cl >= 0 && mr >= 0 && cr >= 0) &&
               (ml == 0 || ml >= cl) && // Left Side 
               (mr == 0 || mr >= cr);  // Right Side
    }

     // Heuristic function: Estimates the minimum crossings needed
     private int Heuristic(int M) {
        int remaining = missionariesLeft + cannibalsLeft;
        return (remaining + M - 1) / M; // Rounds up for remaining trips
    }

    // Generate successors based on possible boat movements
    public List<State> generateSuccessors(int M, int K) {
        List<State> successors = new ArrayList<>();
        for (int m = 0; m <= M; m++) {
            for (int c = 0; c <= M; c++) {
                if (m + c >= 1 && m + c <= M) { // Ensures valid boat load in order to avoid unnecessary comparisons

                    if (m > 0 && c > m) {//Check whether there are more Cannibals than missionaries in the boat
                        continue; 
                    }

                    if (boatSide == 0) { // Boat moves left to right
                        int newML = missionariesLeft - m;
                        int newCL = cannibalsLeft - c;
                        int newMR = missionariesRight + m;
                        int newCR = cannibalsRight + c;

                        if (isValidState(newML, newCL, newMR, newCR) && crossings + 1 <= K) {
                            State newState = new State(newML, newCL, newMR, newCR, 1, g + 1, Heuristic(M), crossings + 1);
                            newState.setParent(this); // Link parent
                            successors.add(newState);
                        }
                    } else { // Boat moves right to left
                        int newML = missionariesLeft + m;
                        int newCL = cannibalsLeft + c;
                        int newMR = missionariesRight - m;
                        int newCR = cannibalsRight - c;

                        if (isValidState(newML, newCL, newMR, newCR) && crossings + 1 <= K) {
                            State newState = new State(newML, newCL, newMR, newCR, 0, g + 1, Heuristic(M), crossings + 1);
                            newState.setParent(this); // Link parent
                            successors.add(newState);
                        }
                    }
                }
            }
        }
        return successors;
    }

    @Override
    //Compare states based on their f value -for A*-
    public int compareTo(State other) {
        return Integer.compare(this.f, other.f);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        State state = (State) obj;
        return missionariesLeft == state.missionariesLeft &&
               cannibalsLeft == state.cannibalsLeft &&
               missionariesRight == state.missionariesRight &&
               cannibalsRight == state.cannibalsRight &&
               boatSide == state.boatSide;
               
    }

    @Override
    public int hashCode() {
        return Objects.hash(missionariesLeft, cannibalsLeft, missionariesRight, cannibalsRight, boatSide);
    }

    // State String representation
    @Override
    public String toString() {
        return "State{" +
                "Missionaries on Left bank=" + missionariesLeft +
                ", Cannibals on Left bank=" + cannibalsLeft +
                ", Missionaries on Right bank=" + missionariesRight +
                ", Cannibals on Right bank=" + cannibalsRight +
                ", Boat=" + (boatSide == 0 ? "Left" : "Right") +
                ", g=" + g +
                ", h=" + h +
                ", f=" + f +
                ", crossings=" + crossings +
                '}';
    }
}