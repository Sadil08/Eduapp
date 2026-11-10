package com.eduapp.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * SEC-06/SEC-08: @PreAuthorize is not active in the @WebMvcTest slices, so pin the annotations
 * directly. Any state-changing endpoint on these controllers must be ADMIN-only.
 */
class WriteEndpointsAdminOnlyTest {

    @Test
    void subjectAndLessonWrites_areAdminOnly() {
        for (Class<?> c : List.of(SubjectController.class, LessonController.class)) {
            assertWritesAreAdminOnly(c);
        }
    }

    @Test
    void studentAttemptDelete_isAdminOnly() throws Exception {
        Method m = Arrays.stream(StudentPaperAttemptController.class.getDeclaredMethods())
                .filter(x -> x.isAnnotationPresent(DeleteMapping.class)).findFirst().orElseThrow();
        assertAdmin(m);
    }

    private void assertWritesAreAdminOnly(Class<?> c) {
        long writes = Arrays.stream(c.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(PostMapping.class) || m.isAnnotationPresent(PutMapping.class)
                        || m.isAnnotationPresent(DeleteMapping.class))
                .peek(this::assertAdmin).count();
        assertThat(writes).as("write endpoints found on " + c.getSimpleName()).isGreaterThan(0);
    }

    private void assertAdmin(Method m) {
        PreAuthorize pa = m.getAnnotation(PreAuthorize.class);
        assertThat(pa).as(m + " needs @PreAuthorize").isNotNull();
        assertThat(pa.value()).contains("ADMIN");
    }
}
