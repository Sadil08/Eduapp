package com.eduapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.eduapp.backend.model.PaperBundle;
import com.eduapp.backend.model.PaperType;

import java.math.BigDecimal;
import java.util.List;

public interface PaperBundleRepository extends JpaRepository<PaperBundle, Long> {

        // Individual filter methods
        List<PaperBundle> findByType(PaperType type);

        List<PaperBundle> findByExamTypeId(Long examTypeId);

        List<PaperBundle> findBySubjectId(Long subjectId);

        List<PaperBundle> findByLessonId(Long lessonId);

        List<PaperBundle> findByIsPastPaper(Boolean isPastPaper);

        List<PaperBundle> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

        List<PaperBundle> findByNameContainingIgnoreCase(String name);

        // Count methods for integrity checks
        long countBySubjectId(Long subjectId);

        long countByLessonId(Long lessonId);
        /**
         * Search bundles by name or description with pagination
         */
        @Query("SELECT pb FROM PaperBundle pb WHERE " +
               "LOWER(pb.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
               "LOWER(pb.description) LIKE LOWER(CONCAT('%', :search, '%'))")
        org.springframework.data.domain.Page<PaperBundle> searchBundles(
                @Param("search") String search, 
                org.springframework.data.domain.Pageable pageable);

        // Combined filter method with all criteria AND pagination
        @Query(value = "SELECT pb FROM PaperBundle pb WHERE " +
                        "(:type IS NULL OR pb.type = :type) AND " +
                        "(:examTypeId IS NULL OR pb.examType.id = :examTypeId) AND " +
                        "(:subjectId IS NULL OR pb.subject.id = :subjectId) AND " +
                        "(:lessonId IS NULL OR pb.lesson.id = :lessonId) AND " +
                        "(:isPastPaper IS NULL OR pb.isPastPaper = :isPastPaper) AND " +
                        "(:minPrice IS NULL OR pb.price >= :minPrice) AND " +
                        "(:maxPrice IS NULL OR pb.price <= :maxPrice) AND " +
                        "(:name IS NULL OR LOWER(pb.name) LIKE :name)",
               countQuery = "SELECT count(pb) FROM PaperBundle pb WHERE " +
                        "(:type IS NULL OR pb.type = :type) AND " +
                        "(:examTypeId IS NULL OR pb.examType.id = :examTypeId) AND " +
                        "(:subjectId IS NULL OR pb.subject.id = :subjectId) AND " +
                        "(:lessonId IS NULL OR pb.lesson.id = :lessonId) AND " +
                        "(:isPastPaper IS NULL OR pb.isPastPaper = :isPastPaper) AND " +
                        "(:minPrice IS NULL OR pb.price >= :minPrice) AND " +
                        "(:maxPrice IS NULL OR pb.price <= :maxPrice) AND " +
                        "(:name IS NULL OR LOWER(pb.name) LIKE :name)")
        org.springframework.data.domain.Page<PaperBundle> findByFiltersPaginated(
                        @Param("type") PaperType type,
                        @Param("examTypeId") Long examTypeId,
                        @Param("subjectId") Long subjectId,
                        @Param("lessonId") Long lessonId,
                        @Param("isPastPaper") Boolean isPastPaper,
                        @Param("minPrice") BigDecimal minPrice,
                        @Param("maxPrice") BigDecimal maxPrice,
                        @Param("name") String name,
                        org.springframework.data.domain.Pageable pageable);
}