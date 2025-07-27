import java.util.*;

public class ForwardChaining {
    private List<HornClause> knowledgeBase; // Knowledge base (list of Horn clauses)
    private Set<Predicate> inferred;       // Set of inferred predicates
    private Queue<Predicate> agenda;       // Agenda (queue of predicates to process)
    private Unifier unifier;               // Unifier for handling variable substitutions
    private static final int MAX_STEPS = 5000; // Maximum number of steps

    public ForwardChaining(List<HornClause> knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
        this.inferred = new HashSet<>();
        this.agenda = new LinkedList<>();
        this.unifier = new Unifier();

        // Add all facts to the agenda initially
        for (HornClause clause : knowledgeBase) {
            if (clause.isFact()) {
                agenda.add(clause.getConclusion());
            }
        }
    }

    public boolean isEntailed(Predicate query) {
        System.out.println("Starting Forward Chaining...");
        int steps = 0;

        while (!agenda.isEmpty()) {
            if (steps >= MAX_STEPS) {
                System.out.println("Maximum steps reached (" + MAX_STEPS + "). Query likely not entailed.");
                return false;
            }

            Predicate current = agenda.poll();
            System.out.println("Processing Predicate: " + current);
            steps++;

            // Attempt to unify the current predicate with the query
            if (unifier.unify(current.getTerms(), query.getTerms())) {
                System.out.println("Unification successful with query: " + query);
                return true;
            }

            // If not already inferred, process it
            if (!inferred.contains(current)) {
                inferred.add(current);
                System.out.println("Inferred: " + current);
                System.out.println("Agenda: " + agenda);
                System.out.println("Inferred Set: " + inferred);

                for (HornClause clause : knowledgeBase) {
                    List<Predicate> premises = new ArrayList<>(clause.getPremises());
                    Predicate conclusion = clause.getConclusion();

                    Iterator<Predicate> iterator = premises.iterator();
                    while (iterator.hasNext()) {
                        Predicate premise = iterator.next();

                        // Attempt to unify terms within predicates
                        if (unifier.unify(premise.getTerms(), current.getTerms())) {
                            System.out.println("Unification successful for premise: " + premise);
                            iterator.remove(); // Remove the satisfied premise

                            // If all premises are satisfied, add the conclusion to the agenda
                            if (premises.isEmpty()) {
                                Predicate substitutedConclusion = new Predicate(conclusion.getName(), unifier.substitute(conclusion.getTerms()), false);
                                System.out.println("Adding to agenda: " + substitutedConclusion);
                                agenda.add(substitutedConclusion);
                            }
                        } else {
                            System.out.println("Unification failed for premise: " + premise);
                        }
                    }
                }
            }
        }

        System.out.println("Forward Chaining complete. Query not entailed.");
        return false;
    }
}
