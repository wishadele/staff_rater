package ca.sfu.cmpt276.staffrating.profile;

public class InstructorProfile extends StaffMemberProfile {

    public InstructorProfile(String name) {
        super(name);
    }

    @Override
    public String displayTitle() {
        return name + " (Instructor)";
    }
}
