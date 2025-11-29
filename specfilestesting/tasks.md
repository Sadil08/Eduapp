# EduApp Testing Implementation Tasks

## Phase 1: Core Setup
1. **Test UserService.register**: Mock UserRepository.findByEmail (return empty), PasswordEncoder.encode. Assert saved user has encoded password, role STUDENT. Edge: Existing email throws exception.
2. **Test UserService.login**: Mock findByEmail (return user), PasswordEncoder.matches (true). Assert JWT token generated. Edge: Wrong password throws exception.
3. **Test SubjectService.findAll**: Mock SubjectRepository.findAll (return list). Assert list returned, logger called. Edge: Empty list ok.
4. **Test SubjectService.save**: Mock repo.save. Assert entity saved, ID set. Edge: Null throws IllegalArgumentException.
5. **Test LessonService.save**: Mock SubjectRepository.findById (return subject), LessonRepository.save. Assert lesson saved with subject. Edge: Subject not found throws exception.
6. **Test AuthController.register**: Use MockMvc, mock UserService.register. Assert 200, JWT in response. Edge: Invalid data 400.
7. **Test SubjectController.getAll**: MockMvc, mock SubjectService.findAll. Assert 200, JSON list. Edge: No auth 401.

## Phase 2: Content Management
8. **Test PaperBundleService.createBundle**: Mock repos/mapper, set subject/lesson. Assert DTO returned, relations set. Edge: Subject not found throws exception.
9. **Test PaperService.save**: Mock PaperBundleRepository.findById (return bundle). Assert paper saved. Edge: Bundle not found throws exception.
10. **Test QuestionService (assume exists)**: Mock PaperRepository, save question. Assert options saved. Edge: Invalid type throws exception.
11. **Test PaperBundleController.create**: MockMvc, mock service. Assert 201, DTO in response. Edge: Validation error 400.
12. **Test PaperController.getAll**: MockMvc, mock service. Assert 200, papers list. Edge: No papers empty array.

## Phase 3: Assessment & AI
13. **Test StudentPaperAttemptService.startAttempt**: Mock repos, create attempt. Assert status IN_PROGRESS, time started. Edge: Max attempts exceeded throws exception.
14. **Test StudentAnswerService.saveAnswer**: Mock attempt repo, save answer. Assert answer linked. Edge: Invalid question throws exception.
15. **Test AIAnalysisService.analyze**: Mock AI call (WebClient), save analysis. Assert feedback/marks stored. Edge: AI error defaults feedback.
16. **Test OverallPaperAnalysisService.aggregate**: Mock analysis repo, calculate total. Assert overall saved. Edge: No analyses throws exception.
17. **Test StudentPaperAttemptController.submit**: MockMvc, mock services. Assert 200, analysis triggered. Edge: Incomplete answers 400.
18. **Test AIAnalysisController.getFeedback**: MockMvc, mock service. Assert 200, feedback JSON. Edge: No analysis 404.

## Phase 4: Enhancements
19. **Test ProgressService.updateProgress**: Mock repo, calculate percentage. Assert progress saved. Edge: Invalid bundle throws exception.
20. **Test LeaderboardService.getRankings**: Mock repo, filter anonymous. Assert list sorted. Edge: No scores empty list.
21. **Test CartService.addToCart**: Mock repo, add bundle. Assert cart updated. Edge: Duplicate bundle ignored.
22. **Test NotificationService.send**: Mock email service, save notification. Assert sent. Edge: Send fail logs error.
23. **Test ProgressController.update**: MockMvc, mock service. Assert 200. Edge: No auth 403.
24. **Test LeaderboardController.get**: MockMvc, mock service. Assert 200, rankings. Edge: Opt-out excludes user.

## Additional Tasks for Remaining Controllers and Services
25. **Test AIAnalysisService.findAll**: Mock repo, return list. Assert list returned.
26. **Test AIService.analyzeAnswer**: Mock answer, assert response map. Edge: Null throws exception.
27. **Test CartService.findAll**: Mock repo, return list. Assert list returned.
28. **Test LeaderboardEntryService.findAll**: Mock repo, return list. Assert list returned.
29. **Test NotificationService.findAll**: Mock repo, return list. Assert list returned.
30. **Test OverallPaperAnalysisService.findAll**: Mock repo, return list. Assert list returned.
31. **Test ProgressService.findAll**: Mock repo, return list. Assert list returned.
32. **Test QuestionOptionService.findAll**: Mock repo, return list. Assert list returned.
33. **Test StudentAnswerService.findAll**: Mock repo, return list. Assert list returned.
34. **Test StudentBundleAccessService.findAll**: Mock repo, return list. Assert list returned.
35. **Test StudentPaperAttemptService.findAll**: Mock repo, return list. Assert list returned.
36. **Test AdminController.getAllUsers**: MockMvc, mock service. Assert 200, list. Edge: No admin auth 403.
37. **Test AIAnalysisController.getAll**: MockMvc, mock service. Assert 200, list. Edge: No auth 401.
38. **Test CartController.getAll**: MockMvc, mock service. Assert 200, list. Edge: No auth 401.
39. **Test LeaderboardEntryController.getAll**: MockMvc, mock service. Assert 200, list. Edge: No auth 401.
40. **Test LessonController.getAll**: MockMvc, mock service. Assert 200, list. Edge: No auth 401.
41. **Test NotificationController.getAll**: MockMvc, mock service. Assert 200, list. Edge: No auth 401.
42. **Test OverallPaperAnalysisController.getAll**: MockMvc, mock service. Assert 200, list. Edge: No auth 401.
43. **Test ProgressController.getAll**: MockMvc, mock service. Assert 200, list. Edge: No auth 401.
44. **Test QuestionController.getAll**: MockMvc, mock service. Assert 200, list. Edge: No auth 401.
45. **Test QuestionOptionController.getAll**: MockMvc, mock service. Assert 200, list. Edge: No auth 401.
46. **Test StudentAnswerController.getAll**: MockMvc, mock service. Assert 200, list. Edge: No auth 401.
47. **Test StudentBundleAccessController.getAll**: MockMvc, mock service. Assert 200, list. Edge: No auth 401.
48. **Test StudentPaperAttemptController.getAll**: MockMvc, mock service. Assert 200, list. Edge: No auth 401.

## General Testing Tasks
- **Mocks**: Use @Mock for repos/services, @InjectMocks for target. Verify interactions with verify().
- **Assertions**: Use AssertJ: assertThat(result).isEqualTo(expected), hasSize(), etc.
- **Edge Cases**: Null inputs, empty collections, max values, invalid enums.
- **Logging**: Test logger calls with ArgumentCaptor.
- **Coverage**: Aim 80%, exclude getters/setters.
- **Integration**: Use @SpringBootTest, TestRestTemplate for full flows.