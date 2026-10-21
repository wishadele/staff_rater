package ca.sfu.cmpt276.staffrating.controller;

import ca.sfu.cmpt276.staffrating.model.RoleType;
import ca.sfu.cmpt276.staffrating.model.StaffRating;
import ca.sfu.cmpt276.staffrating.repository.StaffRatingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

// Slice test: only the web layer is loaded, the repository is mocked
@WebMvcTest(StaffRatingController.class)
class StaffRatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StaffRatingRepository repository;

    private StaffRating sample() {
        StaffRating r = new StaffRating();
        r.setId(1L);
        r.setName("Ada Lovelace");
        r.setEmail("ada@sfu.ca");
        r.setRoleType(RoleType.PROF);
        r.setClarity(8);
        r.setNiceness(9);
        r.setKnowledgeableScore(10);
        r.setComment("Great lectures");
        return r;
    }


    private MockHttpServletRequestBuilder validPost(String url, String... overrides) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("name", "Ada Lovelace");
        fields.put("email", "ada@sfu.ca");
        fields.put("roleType", "PROF");
        fields.put("clarity", "8");
        fields.put("niceness", "9");
        fields.put("knowledgeableScore", "10");
        fields.put("comment", "Great lectures");
        for (int i = 0; i < overrides.length; i += 2) {
            fields.put(overrides[i], overrides[i + 1]);
        }

        MockHttpServletRequestBuilder request = post(url);
        fields.forEach((key, value) -> request.param(key, value));
        return request;
    }

    @Test
    void indexReturns200AndTheRatingsModel() throws Exception {
        when(repository.findAll(any(Sort.class))).thenReturn(List.of(sample()));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("ratings"))
                .andExpect(content().string(containsString("Prof. Ada Lovelace")));
    }

    @Test
    void indexShowsAMessageWhenThereAreNoRatings() throws Exception {
        when(repository.findAll(any(Sort.class))).thenReturn(List.of());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No ratings yet")));
    }

    @Test
    void detailShowsTheRating() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.of(sample()));

        mockMvc.perform(get("/ratings/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("detail"))
                .andExpect(model().attributeExists("rating"))
                .andExpect(content().string(containsString("ada@sfu.ca")))
                .andExpect(content().string(containsString("Edit")))
                .andExpect(content().string(containsString("Delete")));
    }

    @Test
    void detailOfMissingIdReturns404() throws Exception {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/ratings/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void newFormReturns200() throws Exception {
        mockMvc.perform(get("/ratings/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("form"))
                .andExpect(model().attributeExists("rating", "roleTypes"));
    }

    @Test
    void createWithValidInputRedirectsToTheDetailPage() throws Exception {
        when(repository.save(any(StaffRating.class))).thenAnswer(inv -> {
            StaffRating r = inv.getArgument(0);
            r.setId(5L);
            return r;
        });

        mockMvc.perform(validPost("/ratings"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ratings/5"));

        verify(repository).save(any(StaffRating.class));
    }

    @Test
    void createWithInvalidInputRedisplaysTheFormWithErrors() throws Exception {
        mockMvc.perform(validPost("/ratings", "email", "not-an-email", "clarity", "11"))
                .andExpect(status().isOk())
                .andExpect(view().name("form"))
                .andExpect(model().attributeHasFieldErrors("rating", "email", "clarity"))
                .andExpect(content().string(containsString("valid email")))
                .andExpect(content().string(containsString("between 1 and 10")));

        verify(repository, never()).save(any());
    }

    @Test
    void createWithMissingFieldsShowsAnErrorForEach() throws Exception {
        mockMvc.perform(post("/ratings"))
                .andExpect(status().isOk())
                .andExpect(view().name("form"))
                .andExpect(model().attributeHasFieldErrors("rating",
                        "name", "email", "roleType", "clarity", "niceness", "knowledgeableScore"));

        verify(repository, never()).save(any());
    }

    @Test
    void createWithTextInAScoreFieldDoesNotCrash() throws Exception {
        mockMvc.perform(validPost("/ratings", "clarity", "abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("form"))
                .andExpect(model().attributeHasFieldErrors("rating", "clarity"));
    }

    @Test
    void createWithADuplicateEmailIsRejected() throws Exception {
        when(repository.existsByEmailIgnoreCase("ada@sfu.ca")).thenReturn(true);

        mockMvc.perform(validPost("/ratings"))
                .andExpect(status().isOk())
                .andExpect(view().name("form"))
                .andExpect(model().attributeHasFieldErrors("rating", "email"))
                .andExpect(content().string(containsString("already exists")));

        verify(repository, never()).save(any());
    }

    // ---------- Update ----------

    @Test
    void editFormIsPrefilled() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.of(sample()));

        mockMvc.perform(get("/ratings/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("form"))
                .andExpect(content().string(containsString("Ada Lovelace")));
    }

    @Test
    void updateWithValidInputRedirectsToTheDetailPage() throws Exception {
        StaffRating existing = sample();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        mockMvc.perform(validPost("/ratings/1", "name", "Ada King", "clarity", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ratings/1"));

        verify(repository).save(existing);
    }

    @Test
    void updateWithInvalidInputKeepsTheTypedValuesAndShowsErrors() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.of(sample()));

        mockMvc.perform(validPost("/ratings/1", "name", "Typed Name", "niceness", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("form"))
                .andExpect(model().attributeHasFieldErrors("rating", "niceness"))
                .andExpect(content().string(containsString("Typed Name")));

        verify(repository, never()).save(any());
    }

    @Test
    void updateOfMissingIdReturns404() throws Exception {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(validPost("/ratings/99"))
                .andExpect(status().isNotFound());
    }
    @Test
    void deleteConfirmationPageIsShown() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.of(sample()));

        mockMvc.perform(get("/ratings/1/delete"))
                .andExpect(status().isOk())
                .andExpect(view().name("delete"))
                .andExpect(content().string(containsString("Delete this rating?")));

        verify(repository, never()).delete(any());
    }

    @Test
    void confirmedDeleteRemovesTheEntryAndRedirectsToTheList() throws Exception {
        StaffRating existing = sample();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        mockMvc.perform(post("/ratings/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(repository).delete(existing);
    }
}
