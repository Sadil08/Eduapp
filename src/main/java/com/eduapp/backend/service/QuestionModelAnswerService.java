package com.eduapp.backend.service;

import com.eduapp.backend.model.Question;
import com.eduapp.backend.model.QuestionModelAnswer;
import com.eduapp.backend.repository.QuestionModelAnswerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class QuestionModelAnswerService {

    private final QuestionModelAnswerRepository repository;

    public QuestionModelAnswerService(QuestionModelAnswerRepository repository) {
        this.repository = repository;
    }

    public QuestionModelAnswer save(QuestionModelAnswer modelAnswer) {
        return repository.save(modelAnswer);
    }

    public List<QuestionModelAnswer> findByQuestionId(Long questionId) {
        return repository.findByQuestionId(questionId);
    }

    public Optional<QuestionModelAnswer> findLatestByQuestionId(Long questionId) {
        return repository.findFirstByQuestionIdOrderByCreatedAtDesc(questionId);
    }

    public void deleteByQuestionId(Long questionId) {
        repository.deleteByQuestionId(questionId);
    }
    
    /**
     * Get the text to use for AI analysis from the latest model answer
     */
    public String getModelAnswerTextForAnalysis(Question question) {
        return findLatestByQuestionId(question.getId())
            .map(QuestionModelAnswer::getTextForAnalysis)
            .orElse(question.getCorrectAnswerText()); // Fallback to old field
    }
}
