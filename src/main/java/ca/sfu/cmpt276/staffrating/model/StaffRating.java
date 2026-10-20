package ca.sfu.cmpt276.staffrating.model;

import ca.sfu.cmpt276.staffrating.profile.StaffMemberProfile;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "staff_ratings", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class StaffRating {

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required.")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters.")
    private String name;

    // Rule: one rating entry per staff member, so email must be unique (checked in the controller).
    @NotBlank(message = "Email is required.")
    @Email(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "Please enter a valid email address (like name@sfu.ca).")
    @Size(max = 150, message = "Email must be at most 150 characters.")
    private String email;

    @NotNull(message = "Please choose a role.")
    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    @NotNull(message = "Clarity is required.")
    @Min(value = 1, message = "Clarity must be between 1 and 10.")
    @Max(value = 10, message = "Clarity must be between 1 and 10.")
    private Integer clarity;

    @NotNull(message = "Niceness is required.")
    @Min(value = 1, message = "Niceness must be between 1 and 10.")
    @Max(value = 10, message = "Niceness must be between 1 and 10.")
    private Integer niceness;

    @NotNull(message = "Knowledgeable score is required.")
    @Min(value = 1, message = "Knowledgeable score must be between 1 and 10.")
    @Max(value = 10, message = "Knowledgeable score must be between 1 and 10.")
    private Integer knowledgeableScore;

    @Size(max = 500, message = "Comment must be at most 500 characters.")
    @jakarta.persistence.Column(length = 500)
    private String comment;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Transient
    public double getOverallScore() {
        if (clarity == null || niceness == null || knowledgeableScore == null) {
            return 0;
        }
        return Math.round((clarity + niceness + knowledgeableScore) / 3.0 * 10) / 10.0;
    }

    @Transient
    public StaffMemberProfile getProfile() {
        return StaffMemberProfile.forRole(roleType, name);
    }

    @Transient
    public String getCreatedAtDisplay() {
        return createdAt == null ? "" : createdAt.format(DISPLAY_FORMAT);
    }

    @Transient
    public String getUpdatedAtDisplay() {
        return updatedAt == null ? "" : updatedAt.format(DISPLAY_FORMAT);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public RoleType getRoleType() { return roleType; }
    public void setRoleType(RoleType roleType) { this.roleType = roleType; }

    public Integer getClarity() { return clarity; }
    public void setClarity(Integer clarity) { this.clarity = clarity; }

    public Integer getNiceness() { return niceness; }
    public void setNiceness(Integer niceness) { this.niceness = niceness; }

    public Integer getKnowledgeableScore() { return knowledgeableScore; }
    public void setKnowledgeableScore(Integer knowledgeableScore) { this.knowledgeableScore = knowledgeableScore; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
