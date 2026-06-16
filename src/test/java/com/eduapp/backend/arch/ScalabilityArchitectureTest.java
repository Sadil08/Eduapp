package com.eduapp.backend.arch;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * N-SC2 — scalability guard: a service must not call the inherited no-arg {@code findAll()}
 * on a repository. Such calls materialise an entire table and don't scale; callers must use a
 * paginated ({@code findAll(Pageable)}) or scoped ({@code findByX}) finder instead. A new
 * violation fails the build.
 *
 * <p>The allow-list now covers only the naturally-tiny reference tables ({@code Subject},
 * {@code Lesson}, {@code ExamType}). N-SC3 cleared {@code AnalyticsService} and B3 cleared
 * {@code PaperBundleService} (the public catalog is served by a paged query).
 */
class ScalabilityArchitectureTest {

    private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.eduapp.backend");

    // Services consciously allowed to call no-arg findAll() (see class javadoc).
    private static final Set<String> ALLOW_LISTED_SERVICES = Set.of(
            "com.eduapp.backend.service.SubjectService",
            "com.eduapp.backend.service.LessonService",
            "com.eduapp.backend.service.ExamTypeService");

    @Test
    void servicesDoNotCallUnboundedFindAll() {
        ArchRule rule = noClasses()
                .should().callMethodWhere(isUnboundedFindAllFromNonAllowedService())
                .as("services must not call no-arg findAll() (use findAll(Pageable) or a scoped finder)")
                .because("loading an entire table doesn't scale and risks OOM/timeouts as data grows");

        rule.check(PRODUCTION_CLASSES);
    }

    private static DescribedPredicate<JavaMethodCall> isUnboundedFindAllFromNonAllowedService() {
        return new DescribedPredicate<>("no-arg findAll() called from a non-allow-listed service") {
            @Override
            public boolean test(JavaMethodCall call) {
                boolean isNoArgFindAll = call.getTarget().getName().equals("findAll")
                        && call.getTarget().getRawParameterTypes().isEmpty();
                if (!isNoArgFindAll) {
                    return false;
                }
                boolean targetsRepository = call.getTarget().getOwner().getSimpleName().endsWith("Repository");
                String origin = call.getOriginOwner().getFullName();
                boolean fromService = origin.startsWith("com.eduapp.backend.service.");
                return targetsRepository && fromService && !ALLOW_LISTED_SERVICES.contains(origin);
            }
        };
    }
}
