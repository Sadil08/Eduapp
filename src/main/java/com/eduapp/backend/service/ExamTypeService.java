package com.eduapp.backend.service;

import com.eduapp.backend.model.ExamType;
import com.eduapp.backend.repository.ExamTypeRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ExamTypeService {

    private final ExamTypeRepository examTypeRepository;

    public ExamTypeService(ExamTypeRepository examTypeRepository) {
        this.examTypeRepository = examTypeRepository;
    }

    public List<ExamType> getAllExamTypes() {
        return examTypeRepository.findAll();
    }

    public ExamType getExamTypeById(Long id) {
        return examTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exam Type not found with ID: " + id));
    }

    public ExamType createExamType(ExamType examType) {
        if (examTypeRepository.existsByName(examType.getName())) {
            throw new IllegalArgumentException("Exam Type with name " + examType.getName() + " already exists");
        }
        return examTypeRepository.save(examType);
    }

    public ExamType updateExamType(Long id, ExamType examTypeDetails) {
        ExamType examType = getExamTypeById(id);

        if (!examType.getName().equals(examTypeDetails.getName()) &&
                examTypeRepository.existsByName(examTypeDetails.getName())) {
            throw new IllegalArgumentException("Exam Type with name " + examTypeDetails.getName() + " already exists");
        }

        examType.setName(examTypeDetails.getName());
        examType.setDescription(examTypeDetails.getDescription());
        return examTypeRepository.save(examType);
    }

    public void deleteExamType(Long id) {
        if (!examTypeRepository.existsById(id)) {
            throw new IllegalArgumentException("Exam Type not found with ID: " + id);
        }
        examTypeRepository.deleteById(id);
    }
}
