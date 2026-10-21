package ca.sfu.cmpt276.staffrating.profile;

import ca.sfu.cmpt276.staffrating.model.RoleType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class StaffMemberProfileTest {

    @Test
    void eachRoleFormatsItsTitleDifferently() {
        assertEquals("TA Sam", new TeachingAssistantProfile("Sam").displayTitle());
        assertEquals("Prof. Sam", new ProfessorProfile("Sam").displayTitle());
        assertEquals("Sam (Instructor)", new InstructorProfile("Sam").displayTitle());
        assertEquals("Sam (Staff)", new StaffProfile("Sam").displayTitle());
    }

    @Test
    void forRolePicksTheRightSubclass() {
        assertInstanceOf(TeachingAssistantProfile.class, StaffMemberProfile.forRole(RoleType.TA, "Sam"));
        assertInstanceOf(ProfessorProfile.class, StaffMemberProfile.forRole(RoleType.PROF, "Sam"));
        assertInstanceOf(InstructorProfile.class, StaffMemberProfile.forRole(RoleType.INSTRUCTOR, "Sam"));
        assertInstanceOf(StaffProfile.class, StaffMemberProfile.forRole(RoleType.STAFF, "Sam"));
    }

    @Test
    void callerOnlyNeedsTheBaseType() {
        StaffMemberProfile profile = StaffMemberProfile.forRole(RoleType.PROF, "Sam");
        assertEquals("Prof. Sam", profile.displayTitle());
    }
}
