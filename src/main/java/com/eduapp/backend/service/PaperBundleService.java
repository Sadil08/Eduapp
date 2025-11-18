package com.eduapp.backend.service;

import com.eduapp.backend.dto.PaperBundleDto;
import com.eduapp.backend.mapper.PaperBundleMapper;
import com.eduapp.backend.model.Lesson;
import com.eduapp.backend.model.PaperBundle;
import com.eduapp.backend.model.Subject;
import com.eduapp.backend.repository.LessonRepository;
import com.eduapp.backend.repository.PaperBundleRepository;
import com.eduapp.backend.repository.SubjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for managing PaperBundle entities.
 * Provides business logic for creating and retrieving paper bundles.
 * Follows Single Responsibility Principle by handling only paper bundle-related operations.
 */
@Service
@SuppressWarnings("null")
public class PaperBundleService {

    private static final Logger logger = LoggerFactory.getLogger(PaperBundleService.class);

    private final PaperBundleRepository paperBundleRepository;
    private final SubjectRepository subjectRepository;
    private final LessonRepository lessonRepository;
    private final PaperBundleMapper paperBundleMapper;

    /**
     * Constructor for dependency injection of repositories and mapper.
     * @param paperBundleRepository the repository for PaperBundle entities
     * @param subjectRepository the repository for Subject entities
     * @param lessonRepository the repository for Lesson entities
     * @param paperBundleMapper the mapper for PaperBundle DTOs
     */
    public PaperBundleService(PaperBundleRepository paperBundleRepository,
                              SubjectRepository subjectRepository,
                              LessonRepository lessonRepository,
                              PaperBundleMapper paperBundleMapper) {
        this.paperBundleRepository = paperBundleRepository;
        this.subjectRepository = subjectRepository;
        this.lessonRepository = lessonRepository;
        this.paperBundleMapper = paperBundleMapper;
    }

    /**
     * Creates a new paper bundle from the provided DTO.
     * Validates associated entities and saves the bundle.
     * @param dto the PaperBundleDto containing bundle data
     * @return the created PaperBundleDto
     */
    public PaperBundleDto createBundle(PaperBundleDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("PaperBundleDto cannot be null");
        }
        logger.info("Creating paper bundle: {}", dto.getName());
        PaperBundle entity = paperBundleMapper.toEntity(dto);

        // Manually set relationships
        if (dto.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(dto.getSubjectId())
                    .orElseThrow(() -> new IllegalArgumentException("Subject not found"));
            entity.setSubject(subject);
        }
        if (dto.getLessonId() != null) {
            Lesson lesson = lessonRepository.findById(dto.getLessonId())
                    .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
            entity.setLesson(lesson);
        }
        // Set createdBy (assume from context, placeholder)
        // entity.setCreatedBy(currentUser);

        PaperBundle saved = paperBundleRepository.save(entity);
        logger.info("Paper bundle created with ID: {}", saved.getId());
        return paperBundleMapper.toDto(saved);
    }

    /**
     * Retrieves all paper bundles.
     * @return a list of all PaperBundleDto
     */
    public List<PaperBundleDto> getAll() {
        logger.info("Fetching all paper bundles");
        List<PaperBundle> bundles = paperBundleRepository.findAll();
        List<PaperBundleDto> dtos = paperBundleMapper.toDtoList(bundles);
        logger.info("Found {} paper bundles", dtos.size());
        return dtos;
    }
}
