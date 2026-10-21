package ca.sfu.cmpt276.staffrating.controller;

import ca.sfu.cmpt276.staffrating.model.RoleType;
import ca.sfu.cmpt276.staffrating.model.StaffRating;
import ca.sfu.cmpt276.staffrating.repository.StaffRatingRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class StaffRatingController {

    private final StaffRatingRepository repository;

    public StaffRatingController(StaffRatingRepository repository) {
        this.repository = repository;
    }

    @ModelAttribute("roleTypes")
    public RoleType[] roleTypes() {
        return RoleType.values();
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("ratings", repository.findAll(Sort.by("name")));
        return "index";
    }

    @GetMapping("/ratings")
    public String ratingsRedirect() {
        return "redirect:/";
    }

    @GetMapping("/ratings/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("rating", findOr404(id));
        return "detail";
    }

    @GetMapping("/ratings/new")
    public String newForm(Model model) {
        model.addAttribute("rating", new StaffRating());
        addFormInfo(model, "Add a rating", "/ratings");
        return "form";
    }

    @PostMapping("/ratings")
    public String create(@Valid @ModelAttribute("rating") StaffRating rating,
                         BindingResult result, Model model) {
        rating.setId(null); 
        if (!result.hasFieldErrors("email") && repository.existsByEmailIgnoreCase(rating.getEmail())) {
            result.rejectValue("email", "duplicate", "A rating for this email already exists.");
        }

        if (result.hasErrors()) {
            addFormInfo(model, "Add a rating", "/ratings");
            return "form";
        }

        StaffRating saved = repository.save(rating);
        return "redirect:/ratings/" + saved.getId();
    }
    @GetMapping("/ratings/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("rating", findOr404(id));
        addFormInfo(model, "Edit rating", "/ratings/" + id);
        return "form";
    }

    @PostMapping("/ratings/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("rating") StaffRating form,
                         BindingResult result, Model model) {
        StaffRating existing = findOr404(id);

        if (!result.hasFieldErrors("email") && repository.existsByEmailIgnoreCaseAndIdNot(form.getEmail(), id)) {
            result.rejectValue("email", "duplicate", "A rating for this email already exists.");
        }

        if (result.hasErrors()) {
            // The typed values stay in "rating", so the form is redisplayed with them
            addFormInfo(model, "Edit rating", "/ratings/" + id);
            return "form";
        }

        existing.setName(form.getName());
        existing.setEmail(form.getEmail());
        existing.setRoleType(form.getRoleType());
        existing.setClarity(form.getClarity());
        existing.setNiceness(form.getNiceness());
        existing.setKnowledgeableScore(form.getKnowledgeableScore());
        existing.setComment(form.getComment());
        repository.save(existing);

        return "redirect:/ratings/" + id;
    }

//deleting
    @GetMapping("/ratings/{id}/delete")
    public String confirmDelete(@PathVariable Long id, Model model) {
        model.addAttribute("rating", findOr404(id));
        return "delete";
    }

    @PostMapping("/ratings/{id}/delete")
    public String delete(@PathVariable Long id) {
        repository.delete(findOr404(id));
        return "redirect:/";
    }

//helpers
    private StaffRating findOr404(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rating not found"));
    }

    private void addFormInfo(Model model, String pageTitle, String formAction) {
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("formAction", formAction);
    }
}
