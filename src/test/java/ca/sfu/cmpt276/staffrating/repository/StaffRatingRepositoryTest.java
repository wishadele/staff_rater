package ca.sfu.cmpt276.staffrating.repository;

import ca.sfu.cmpt276.staffrating.model.RoleType;
import ca.sfu.cmpt276.staffrating.model.StaffRating;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Uses an in-memory H2 database, so no Postgres is needed to run the tests
@DataJpaTest
class StaffRatingRepositoryTest {

    @Autowired
    private StaffRatingRepository repository;

    private StaffRating rating(String name, String email) {
        StaffRating r = new StaffRating();
        r.setName(name);
        r.setEmail(email);
        r.setRoleType(RoleType.TA);
        r.setClarity(7);
        r.setNiceness(8);
        r.setKnowledgeableScore(9);
        r.setComment("Helpful");
        return r;
    }

    @Test
    void savedRatingCanBeRetrieved() {
        StaffRating saved = repository.save(rating("Sam Lee", "sam@sfu.ca"));

        assertNotNull(saved.getId());
        StaffRating found = repository.findById(saved.getId()).orElseThrow();
        assertEquals("Sam Lee", found.getName());
        assertEquals("sam@sfu.ca", found.getEmail());
        assertEquals(RoleType.TA, found.getRoleType());
        assertEquals(7, found.getClarity());
        assertEquals("Helpful", found.getComment());
    }

    @Test
    void timestampsAreSetOnSave() {
        StaffRating saved = repository.save(rating("Sam Lee", "sam@sfu.ca"));
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void updatingChangesTheStoredValues() {
        StaffRating saved = repository.saveAndFlush(rating("Sam Lee", "sam@sfu.ca"));
        saved.setClarity(3);
        repository.saveAndFlush(saved);

        assertEquals(3, repository.findById(saved.getId()).orElseThrow().getClarity());
    }

    @Test
    void deleteRemovesTheEntry() {
        StaffRating saved = repository.save(rating("Sam Lee", "sam@sfu.ca"));

        repository.delete(saved);

        assertFalse(repository.findById(saved.getId()).isPresent());
        assertEquals(0, repository.count());
    }

    @Test
    void findAllReturnsEverySavedRating() {
        repository.save(rating("Sam Lee", "sam@sfu.ca"));
        repository.save(rating("Kim Park", "kim@sfu.ca"));

        assertEquals(2, repository.findAll().size());
    }

    @Test
    void emailExistsCheckIgnoresCase() {
        repository.save(rating("Sam Lee", "sam@sfu.ca"));

        assertTrue(repository.existsByEmailIgnoreCase("SAM@SFU.CA"));
        assertFalse(repository.existsByEmailIgnoreCase("other@sfu.ca"));
    }

    @Test
    void emailExistsCheckCanExcludeTheEntryBeingEdited() {
        StaffRating saved = repository.save(rating("Sam Lee", "sam@sfu.ca"));

        assertFalse(repository.existsByEmailIgnoreCaseAndIdNot("sam@sfu.ca", saved.getId()));
        assertTrue(repository.existsByEmailIgnoreCaseAndIdNot("sam@sfu.ca", saved.getId() + 1));
    }

    @Test
    void databaseRejectsDuplicateEmails() {
        repository.saveAndFlush(rating("Sam Lee", "sam@sfu.ca"));

        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(rating("Someone Else", "sam@sfu.ca")));
    }
}
