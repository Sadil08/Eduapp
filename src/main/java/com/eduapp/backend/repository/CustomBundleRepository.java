package com.eduapp.backend.repository;

import com.eduapp.backend.model.CustomBundle;
import com.eduapp.backend.model.CustomBundleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomBundleRepository extends JpaRepository<CustomBundle, Long> {

    /**
     * Find the current draft/created bundle for a user.
     * A user can only have one CREATED bundle at a time.
     */
    Optional<CustomBundle> findByCreatorIdAndStatus(Long creatorId, CustomBundleStatus status);

    /**
     * Find all bundles created by a specific user.
     */
    List<CustomBundle> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);

    /**
     * Find all purchased bundles awaiting approval.
     */
    List<CustomBundle> findByStatusOrderByPurchasedAtAsc(CustomBundleStatus status);

    /**
     * Count how many bundles a user has in CREATED status.
     */
    long countByCreatorIdAndStatus(Long creatorId, CustomBundleStatus status);

    /**
     * Find all approved bundles for public display.
     */
    @Query("SELECT cb FROM CustomBundle cb WHERE cb.status = 'APPROVED' ORDER BY cb.approvedAt DESC")
    List<CustomBundle> findApprovedBundles();
}
