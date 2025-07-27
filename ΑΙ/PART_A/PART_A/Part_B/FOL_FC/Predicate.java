import java.util.*;

public class Predicate {
    private String name;
    private List<Term> terms;
    private boolean isNegated;

    public Predicate(String name, List<Term> terms, boolean isNegated) {
        if (terms == null || terms.isEmpty()) {
            throw new IllegalArgumentException("Every Predicate has at least on term.");
        }
        this.name = name;
        this.terms = terms;
        this.isNegated = isNegated;
    }

    public String getName() {
        return name;
    }

    public List<Term> getTerms() {
        return terms;
    }

    public boolean isNegated() {
        return isNegated;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("(");
        for (int i = 0; i < terms.size(); i++) {
            sb.append(terms.get(i));
            if (i < terms.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }
}
