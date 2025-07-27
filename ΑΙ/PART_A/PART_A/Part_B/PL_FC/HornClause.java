import java.util.ArrayList;
import java.util.List;

public class HornClause {
    private List<Literal> premises; // Premises
    private Literal conclusion;    // Conclusion (positive literal)

    // Constructor for a Horn Clause
    public HornClause(Literal conclusion, List<Literal> premises) {
        this.conclusion = conclusion; // Positive literal
        this.premises = premises != null ? premises : new ArrayList<>();//Null only for facts (True->A)
    }

    // Getters
    public List<Literal> getPremises() {
        return premises;
    }

    public Literal getConclusion() {
        return conclusion;
    }

    // Check if the clause is a fact (no premises, only a conclusion)
    public boolean isFact() {
        return premises.isEmpty();
    }

    // Print the Horn clause
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
    
        //Check if there are premises
        if (!premises.isEmpty()) {
            //Creating the conjuction of the premises
            for (int i = 0; i < premises.size(); i++) {
                sb.append(premises.get(i).getName());
                if (i < premises.size() - 1) sb.append(" ^ ");
            }
            sb.append(" -> ").append(conclusion != null ? conclusion.getName() : "true");
        } else {//No premises means its a fact
            //  A facts representation is "True -> literal's name"
            sb.append("True -> ").append(conclusion != null ? conclusion.getName() : "true");
        }
        
        return sb.toString();
    }
    
}
