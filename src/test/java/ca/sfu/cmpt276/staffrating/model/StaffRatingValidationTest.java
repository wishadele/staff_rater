package ca.sfu.cmpt276.staffrating.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaffRatingValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private StaffRating validRating() {
        StaffRating r = new StaffRating();
        r.setName("Ada Lovelace");
        r.setEmail("ada@sfu.ca");
        r.setRoleType(RoleType.PROF);
        r.setClarity(8);
        r.setNiceness(9);
        r.setKnowledgeableScore(10);
        r.setComment("Great lectures");
        return r;
    }

    private Set<String> invalidFields(StaffRating r) {
        return validator.validate(r).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void validRatingHasNoViolations() {
        assertTrue(validator.validate(validRating()).isEmpty());
    }

    @Test
    void invalidEmailIsRejected() {
        for (String bad : new String[]{"not-an-email", "a@b", "@sfu.ca", "ada@", "ada sfu@sfu.ca"}) {
            StaffRating r = validRating();
            r.setEmail(bad);
            assertTrue(invalidFields(r).contains("email"), "should reject: " + bad);
        }
    }

    @Test
    void scoreBelowOneIsRejected() {
        StaffRating r = validRating();
        r.setClarity(0);
        assertTrue(invalidFields(r).contains("clarity"));
    }

    @Test
    void scoreAboveTenIsRejected() {
        StaffRating r = validRating();
        r.setNiceness(11);
        r.setKnowledgeableScore(100);
        Set<String> bad = invalidFields(r);
        assertTrue(bad.contains("niceness"));
        assertTrue(bad.contains("knowledgeableScore"));
    }

    @Test
    void scoresOnTheBoundariesAreAccepted() {
        StaffRating r = validRating();
        r.setClarity(1);
        r.setNiceness(10);
        assertTrue(validator.validate(r).isEmpty());
    }

    @Test
    void missingRequiredFieldsAreRejected() {
        Set<String> bad = invalidFields(new StaffRating());
        assertTrue(bad.containsAll(Set.of("name", "email", "roleType", "clarity", "niceness", "knowledgeableScore")));
    }

    @Test
    void blankNameIsRejected() {
        StaffRating r = validRating();
        r.setName("   ");
        assertTrue(invalidFields(r).contains("name"));
    }

    @Test
    void commentOver500CharactersIsRejected() {
        StaffRating r = validRating();
        r.setComment("x".repeat(501));
        Set<ConstraintViolation<StaffRating>> violations = validator.validate(r);
        assertEquals(1, violations.size());
        assertEquals("comment", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void overallScoreIsTheAverageRoundedToOneDecimal() {
        StaffRating r = validRating(); // 8, 9, 10
        assertEquals(9.0, r.getOverallScore());
        r.setClarity(7); // 7, 9, 10 -> 8.666...
        assertEquals(8.7, r.getOverallScore());
    }
}
