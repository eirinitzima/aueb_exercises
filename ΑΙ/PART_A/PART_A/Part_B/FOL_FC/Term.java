public class Term {
    private String name;
    private boolean isVariable;

    public Term(String name, boolean isVariable) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Term must have a name.");
        }
        this.name = name;
        this.isVariable = isVariable;
    }

    public String getName() {
        return name;
    }

    public boolean isVariable() {
        return isVariable;
    }

    @Override
    public String toString() {
        return name;
    }
}
