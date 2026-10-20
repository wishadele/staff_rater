package ca.sfu.cmpt276.staffrating.profile;

public class StaffProfile extends StaffMemberProfile {

    public StaffProfile(String name) {
        super(name);
    }

    @Override
    public String displayTitle() {
        return name + " (Staff)";
    }
}
