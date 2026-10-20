package ca.sfu.cmpt276.staffrating.model;

public enum RoleType {
    TA("Teaching Assistant"),
    PROF("Professor"),
    INSTRUCTOR("Instructor"),
    STAFF("Staff");

    private final String label;

    RoleType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
