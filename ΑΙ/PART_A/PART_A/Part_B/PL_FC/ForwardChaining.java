import java.util.*;

public class ForwardChaining {
    private List<HornClause> knowledgeBase; // Knowledge base (list of Horn clauses)
    private Set<Literal> inferred;         // Set of inferred literals
    private Queue<Literal> agenda;         // Agenda (queue of literals to process)

    // Constructor
    public ForwardChaining(List<HornClause> knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
        this.inferred = new HashSet<>();
        this.agenda = new LinkedList<>();
        
        // Add all facts to the agenda initially 
        for (HornClause clause : knowledgeBase) {
            if (clause.isFact()) {
                // Adding facts to the agenda
                System.out.println("Adding fact to agenda: " + clause.getConclusion().getName());
                agenda.add(clause.getConclusion());
            }
        }
    }

    // Check if the query can be entailed
    public boolean isEntailed(String query) {
        System.out.println("Starting Forward Chaining...");
        
        while (!agenda.isEmpty()) {
            // Retrieve and remove the head of the agenda
            Literal literal = agenda.poll();
            System.out.println("Processing literal: " + literal.getName());

            // Check if the current literal matches the query
            if (literal.getName().equals(query)) 
                return true;

            // If the literal is not already inferred
            if (!inferred.contains(literal)) {
                inferred.add(literal); // Mark it as inferred
                System.out.println("Inferred: " + literal.getName());
                System.out.println("Inferred set: " + inferred);

                // Process the knowledge base
                for (HornClause clause : knowledgeBase) {
                    if (clause.getPremises().contains(literal)) {
                        System.out.println("Literal " + literal.getName() + " satisfies a premise in clause: " + clause);
                        
                        // Remove the satisfied premise from the clause
                        clause.getPremises().remove(literal);

                        // If all premises are satisfied, add the conclusion to the agenda
                        if (clause.getPremises().isEmpty() && clause.getConclusion() != null) {
                            Literal conclusion = clause.getConclusion();
                            // Check if conclusion is already inferred or in the agenda
                            if (!inferred.contains(conclusion) && !agenda.contains(conclusion)) {
                                System.out.println("All premises satisfied. Adding conclusion to agenda: " + conclusion.getName());
                                agenda.add(conclusion);
                            }
                        }
                    }
                }
            } else {
                System.out.println("Literal " + literal.getName() + " already inferred. Skipping.");
            }
        }
        return false;
    }
}
