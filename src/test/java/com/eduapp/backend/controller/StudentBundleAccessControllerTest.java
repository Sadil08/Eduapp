package com.eduapp.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import com.eduapp.backend.model.StudentBundleAccess;
import com.eduapp.backend.service.StudentBundleAccessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StudentBundleAccessController.class)
public class StudentBundleAccessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentBundleAccessService studentBundleAccessService;

    @MockBean
    private com.eduapp.backend.security.JwtUtil jwtUtil;

    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockBean
    private com.eduapp.backend.repository.UserRepository userRepository;

    @Test
    void getAll_ReturnsOk() throws Exception {
        List<StudentBundleAccess> accesses = List.of(new StudentBundleAccess());
        when(studentBundleAccessService.findAll()).thenReturn(accesses);

        mockMvc.perform(get("/api/student-bundle-accesses")
                .with(user("user").roles("STUDENT")))
                .andExpect(status().isOk());
    }

    @Test
    void getById_ReturnsOk() throws Exception {
        StudentBundleAccess access = new StudentBundleAccess();
        when(studentBundleAccessService.findById(1L)).thenReturn(Optional.of(access));

        mockMvc.perform(get("/api/student-bundle-accesses/1")
                .with(user("user").roles("STUDENT")))
                .andExpect(status().isOk());
    }

    @Test
    void getById_ReturnsNotFound() throws Exception {
        when(studentBundleAccessService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/student-bundle-accesses/1")
                .with(user("user").roles("STUDENT")))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_ReturnsOk() throws Exception {
        StudentBundleAccess access = new StudentBundleAccess();
        StudentBundleAccess saved = new StudentBundleAccess();
        when(studentBundleAccessService.save(any(StudentBundleAccess.class))).thenReturn(saved);

        mockMvc.perform(post("/api/student-bundle-accesses")
                .contentType("application/json")
                .content("{}")
                .with(user("user").roles("STUDENT"))
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void update_ReturnsOk() throws Exception {
        StudentBundleAccess updated = new StudentBundleAccess();
        when(studentBundleAccessService.existsById(1L)).thenReturn(true);
        when(studentBundleAccessService.save(any(StudentBundleAccess.class))).thenReturn(updated);

        mockMvc.perform(put("/api/student-bundle-accesses/1")
                .contentType("application/json")
                .content("{}")
                .with(user("user").roles("STUDENT"))
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void update_ReturnsNotFound() throws Exception {
        when(studentBundleAccessService.existsById(1L)).thenReturn(false);

        mockMvc.perform(put("/api/student-bundle-accesses/1")
                .contentType("application/json")
                .content("{}")
                .with(user("user").roles("STUDENT"))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_ReturnsNoContent() throws Exception {
        when(studentBundleAccessService.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/student-bundle-accesses/1")
                .with(user("user").roles("STUDENT"))
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_ReturnsNotFound() throws Exception {
        when(studentBundleAccessService.existsById(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/student-bundle-accesses/1")
                .with(user("user").roles("STUDENT"))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }
}