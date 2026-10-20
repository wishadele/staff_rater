package ca.sfu.cmpt276.staffrating.repository;

import ca.sfu.cmpt276.staffrating.model.StaffRating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRatingRepository extends JpaRepository<StaffRating, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
