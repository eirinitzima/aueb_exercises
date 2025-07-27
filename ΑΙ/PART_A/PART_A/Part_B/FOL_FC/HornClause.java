import java.util.*;

public class HornClause {
    private Predicate conclusion;
    private List<Predicate> premises;

    public HornClause(Predicate conclusion, List<Predicate> premises) {
        if (conclusion == null) {
            throw new IllegalArgumentException("Every Horn Clause has a Conclusion.");
        }
        this.conclusion = conclusion;
        this.premises = premises != null ? premises : new ArrayList<>();
    }

    public Predicate getConclusion() {
        return conclusion;
    }

    public List<Predicate> getPremises() {
        return premises;
    }

    public boolean isFact() {
        return premises.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!premises.isEmpty()) {
            for (int i = 0; i < premises.size(); i++) {
                sb.append(premises.get(i).toString());
                if (i < premises.size() - 1) {
                    sb.append(" ^ ");
                }
            }
            sb.append(" => ");
        }
        // Add the conclusion
        sb.append(conclusion.toString());

        return sb.toString();
    }
}
