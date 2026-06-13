package com.eduapp.backend.arch;

import com.eduapp.backend.repository.TenantScoped;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * WP-4.4 — architecture guards that make tenant leaks un-mergeable.
 *
 * The headline rule: nothing may call the inherited no-arg {@code findAll()} on a
 * {@code @TenantScoped} repository (it would return every tenant's rows). Callers must
 * use the {@code ...BySchoolId} finders instead. A violation fails the build.
 */
class TenantArchitectureTest {

    private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.eduapp.backend");

    @Test
    void noCodeCallsRawFindAllOnTenantScopedRepository() {
        ArchRule rule = noClasses()
                .should().callMethodWhere(isNoArgFindAllOnTenantScopedRepo())
                .as("never call no-arg findAll() on a @TenantScoped repository (use a *BySchoolId finder)")
                .because("findAll() ignores the tenant boundary and would leak every school's rows");

        rule.check(PRODUCTION_CLASSES);
    }

    private static com.tngtech.archunit.base.DescribedPredicate<JavaMethodCall> isNoArgFindAllOnTenantScopedRepo() {
        return new com.tngtech.archunit.base.DescribedPredicate<>("no-arg findAll() on a @TenantScoped repository") {
            @Override
            public boolean test(JavaMethodCall call) {
                return call.getTarget().getName().equals("findAll")
                        && call.getTarget().getRawParameterTypes().isEmpty()
                        && call.getTarget().getOwner().isAnnotatedWith(TenantScoped.class);
            }
        };
    }
}
