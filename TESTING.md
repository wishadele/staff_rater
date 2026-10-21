# Testing

## How to run

```
mvn test
```

No database setup is needed. The tests use an in-memory H2 database (test scope only), so Postgres is not required.

## Approach: slice tests

I used **slice tests** instead of full `@SpringBootTest` integration tests:

- `@WebMvcTest` loads only the web layer (controller, Thymeleaf views, validation) with the repository mocked.
- `@DataJpaTest` loads only JPA and the repository against H2.
- Plain JUnit for the validation rules and the polymorphic profile classes.

Reason: slice tests start faster, and a failure points at one layer (a validation rule, a controller route, a query) instead of "something in the app". All test classes follow this style.

## What is tested

### Validation - `StaffRatingValidationTest`
- Valid rating has no violations
- Invalid emails rejected (`not-an-email`, `a@b`, `@sfu.ca`, `ada@`, email with a space)
- Scores below 1 and above 10 rejected; 1 and 10 accepted
- Missing required fields rejected (name, email, role, all three scores)
- Blank name rejected
- Comment over 500 characters rejected (my extra validation rule)
- Overall score is the average rounded to one decimal

### Controller / web - `StaffRatingControllerTest` (MockMvc)
- `GET /` returns 200, view `index`, `ratings` model attribute, and shows the role-specific title; shows an empty-state message with no data
- `GET /ratings/{id}` returns 200 with details and Edit/Delete links; unknown id returns 404
- `GET /ratings/new` returns the form
- `POST /ratings` valid: redirects to `/ratings/{id}` and saves
- `POST /ratings` invalid: returns the form (200) with field errors and does not save
- `POST /ratings` with no fields: an error on every required field
- `POST /ratings` with text in a score field: field error, no crash
- `POST /ratings` with a duplicate email: field error, no save
- `GET /ratings/{id}/edit` is prefilled
- `POST /ratings/{id}` valid: saves and redirects; invalid: form redisplayed with the typed values and errors; unknown id returns 404
- `GET /ratings/{id}/delete` shows the confirmation and does not delete; `POST /ratings/{id}/delete` deletes and redirects to `/`

### Persistence - `StaffRatingRepositoryTest` (H2)
- Saving then retrieving returns the same values
- `createdAt` / `updatedAt` are set on save
- Updating stores the new values
- Delete removes the entry
- `findAll` returns every saved entry
- `existsByEmailIgnoreCase` ignores case; `...AndIdNot` excludes the entry being edited
- The database unique constraint rejects a duplicate email

### Design / polymorphism - `StaffMemberProfileTest`
- Each role subclass formats `displayTitle()` differently
- `forRole(...)` returns the correct subclass for each `RoleType`
- Callers can use just the base type
