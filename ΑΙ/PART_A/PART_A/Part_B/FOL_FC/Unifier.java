import java.util.*;

public class Unifier {
    private final Map<String, String> substitutions = new HashMap<>();

    public boolean unify(Term x, Term y) {
        if (x.isVariable()) {
            return substitute(x.getName(), y.getName());
        } else if (y.isVariable()) {
            return substitute(y.getName(), x.getName());
        } else {
            return x.getName().equals(y.getName());
        }
    }

    public boolean unify(List<Term> terms1, List<Term> terms2) {
        if (terms1.size() != terms2.size()) return false;
        for (int i = 0; i < terms1.size(); i++) {
            if (!unify(terms1.get(i), terms2.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean substitute(String var, String value) {
        if (substitutions.containsKey(var)) {
            return substitutions.get(var).equals(value);
        }
        substitutions.put(var, value);
        return true;
    }

    public List<Term> substitute(List<Term> terms) {
        List<Term> substitutedTerms = new ArrayList<>();
        for (Term term : terms) {
            if (term.isVariable() && substitutions.containsKey(term.getName())) {
                substitutedTerms.add(new Term(substitutions.get(term.getName()), false));
            } else {
                substitutedTerms.add(term);
            }
        }
        return substitutedTerms;
    }

    @Override
    public String toString() {
        return substitutions.toString();
    }
}
