package ca.sfu.cmpt276.staffrating.profile;

public class TeachingAssistantProfile extends StaffMemberProfile {

    public TeachingAssistantProfile(String name) {
        super(name);
    }

    @Override
    public String displayTitle() {
        return "TA " + name;
    }
}
