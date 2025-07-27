import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        //Scanner object to read user's input
        Scanner scanner = new Scanner(System.in);

        System.out.println("Please enter the path to the knowledge base file:");
        String filePath = scanner.nextLine();

        //Knowledge Base (KB) creation
        List<HornClause> knowledgeBase = new ArrayList<>();

        //Trying to open and read from the file
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {//For every line in the file
                if(!processClause(line, knowledgeBase))
                    System.out.println("Invalid input in file. Skipping line: " + line);//If a line(aka clause) is invalid it is skipped and not included in the KB
            }
        } catch (IOException e) {//Could not find the file
            System.out.println("Error reading the file: " + e.getMessage());
            filePath = scanner.nextLine();
        }

        //Printing every clause in the Knowledge Base
        System.out.println("Knowledge Base:");
        for (HornClause clause : knowledgeBase) {
            System.out.println(clause);
        }

        //Creation of a Forward Chaining instance
        ForwardChaining forwardChaining = new ForwardChaining(knowledgeBase);

        //The clause to be checked 
        System.out.println("Enter the query you want to check:");
        String queryString = scanner.nextLine();

        //Parse the User's input to a Predicate
        Predicate query = processPredicate(queryString.trim());

        //Time before the execution of Forward Chaining 
        long start = System.currentTimeMillis();
    
        //Forward chaining execution
        boolean result = forwardChaining.isEntailed(query);

        //Time after the execution of Forward Chaining 
        long end = System.currentTimeMillis();

        //Result Prinitng 
        if (result) {
            System.out.println("Answer: Yes, '" + query + "' is entailed.");
        } else {
            System.out.println("Answer: No, '" + query + "' is not entailed.");
        }
        System.out.println("Search time:" + (double)(end - start) / 1000 + " sec.");  // total time of searching in seconds
        
        scanner.close();
    }

    private static boolean processClause(String input, List<HornClause> knowledgeBase) {
        // Trim the clause and handle parentheses correctly
        input = input.replaceAll("\\s+", ""); // Remove all spaces
    
        if (input.contains("=>")) {
            String[] parts = input.split("=>");
            String premisesPart = parts[0];
            if (premisesPart.startsWith("(") && premisesPart.endsWith(")")) {
                premisesPart = premisesPart.substring(1, premisesPart.length() - 1); // Remove surrounding parentheses
            }
            String conclusionPart = parts[1].trim();
    
            List<Predicate> premises = new ArrayList<>();
            String[] premiseParts = premisesPart.split("\\^");
            for (String premise : premiseParts) {
                premises.add(processPredicate(premise.trim()));
            }
    
            Predicate conclusion = processPredicate(conclusionPart);
            knowledgeBase.add(new HornClause(conclusion, premises));
            return true; // Clause successfully created and added
        } 
        
        if (!input.isEmpty()) { // Validate non-empty input for facts
            Predicate fact = processPredicate(input.trim());
            knowledgeBase.add(new HornClause(fact, new ArrayList<>()));
            return true; // Fact successfully created and added
        }
    
        return false; // Fallback for invalid or empty input
    }    
           
    private static Predicate processPredicate(String input) {
        // Extract predicate name and arguments
        String name = input.split("\\(")[0].trim(); // Ensure the name is properly extracted
        String[] args = input.contains("(") ? input.split("\\(")[1].replace(")", "").split(",") : new String[0];

        List<Term> terms = new ArrayList<>();
        for (String arg : args) {
            // Identify variables (lowercase) and constants (uppercase)
            boolean isVariable = Character.isLowerCase(arg.trim().charAt(0));
            terms.add(new Term(arg.trim(), isVariable));
        }

        return new Predicate(name, terms, false);
    }
}
