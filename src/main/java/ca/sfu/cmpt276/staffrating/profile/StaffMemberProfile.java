package ca.sfu.cmpt276.staffrating.profile;

import ca.sfu.cmpt276.staffrating.model.RoleType;

/**
 * Base class for role-based behaviour. Each role decides how its title is displayed.
 * The UI just calls displayTitle() and does not care which role it is.
 */
public abstract class StaffMemberProfile {

    protected final String name;

    protected StaffMemberProfile(String name) {
        this.name = name;
    }

    public abstract String displayTitle();

    /** Picks the right subclass for a role. */
    public static StaffMemberProfile forRole(RoleType roleType, String name) {
        if (roleType == null) {
            return new StaffProfile(name);
        }
        return switch (roleType) {
            case TA -> new TeachingAssistantProfile(name);
            case PROF -> new ProfessorProfile(name);
            case INSTRUCTOR -> new InstructorProfile(name);
            case STAFF -> new StaffProfile(name);
        };
    }
}
