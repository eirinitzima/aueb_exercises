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
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))){
            String line;
            while ((line = reader.readLine()) != null) {//For every line in the file
                if (!processClause(line, knowledgeBase)) //Methods that checks for the validity of a clause 
                    System.out.println("Invalid input in file. Skipping line: " + line);//If a line(aka clause) is invalid it is skipped and not included in the KB
            }
        } catch (IOException e) {//Could not find the file
                System.out.println("Error reading the file: " + e.getMessage());
                filePath = scanner.nextLine();
            }
       
        //Printing every clause in the Knowledge Base
        System.out.println("\nKnowledge Base:");
        for (HornClause clause : knowledgeBase) {
            System.out.println(clause);
        }

        //Creation of a Forward Chaining instance 
        ForwardChaining forwardChaining = new ForwardChaining(knowledgeBase);

        //The clause to be checked 
        System.out.println("\nEnter the query (literal) you want to check:");
        String query = scanner.nextLine();

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

    private static boolean processClause(String line, List<HornClause> knowledgeBase) {
        // Split the line into premises and conclusion
        String[] parts = line.split("->");
    
        if (parts.length == 1) {
            // Single literal (fact)
            String fact = parts[0].trim();
            if (fact.matches("[a-zA-Z]+")) { // Validate fact
                // Add fact to knowledge base
                knowledgeBase.add(new HornClause(new Literal(fact, false), new ArrayList<>()));
                return true;
            }
        } else if (parts.length == 2) {
            // Horn clause with premises and conclusion
            String conclusionPart = parts[1].trim();
            String[] premisesParts = parts[0].trim().split("\\^");
    
            // Validate premises
            List<Literal> premises = new ArrayList<>();
            for (String premise : premisesParts) {
                if (premise.trim().matches("[a-zA-Z]+")) {
                    premises.add(new Literal(premise.trim(), false));
                } else {
                    return false; // Invalid premise
                }
            }
            // Validate conclusion
            if (conclusionPart.matches("[a-zA-Z]+")) {
                Literal conclusion = new Literal(conclusionPart, false);
                // Add Horn clause to knowledge base
                knowledgeBase.add(new HornClause(conclusion, premises));
                return true;
            }
        }
        // Invalid clause
        return false;
    }
}    