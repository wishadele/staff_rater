package ca.sfu.cmpt276.staffrating.profile;

public class ProfessorProfile extends StaffMemberProfile {

    public ProfessorProfile(String name) {
        super(name);
    }

    @Override
    public String displayTitle() {
        return "Prof. " + name;
    }
}
