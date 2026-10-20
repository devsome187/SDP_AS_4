package proctoring.results;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class ResultsService {

    private final GradingEngine gradingEngine;
    private final TranscriptRecorder transcriptRecorder;
    private final ResultRepository repository;

    public ResultsService(GradingEngine gradingEngine, TranscriptRecorder transcriptRecorder,
                          ResultRepository repository) {
        this.gradingEngine = gradingEngine;
        this.transcriptRecorder = transcriptRecorder;
        this.repository = repository;
    }

    public ExamResult gradeAndRecord(String examSessionId, String studentId, String examId, List<String> answers) {
        int score;
        try {
            score = gradingEngine.grade(examId, answers);
        } catch (GradingRejectedException e) {
            repository.save(ExamResult.rejected(examSessionId, studentId, examId, e.getMessage()));
            throw e;
        }
        ExamResult result = ExamResult.recorded(examSessionId, studentId, examId, score,
                transcriptRecorder.letterGrade(score));
        repository.save(result);
        return result;
    }

    public void cancel(String resultId) {
        ExamResult result = get(resultId);
        result.cancel();
        repository.save(result);
    }

    public ExamResult get(String resultId) {
        return repository.findById(resultId)
                .orElseThrow(() -> new NoSuchElementException("Result not found: " + resultId));
    }

    public Optional<ExamResult> findRecorded(String examSessionId) {
        return repository.findByExamSessionId(examSessionId).stream()
                .filter(r -> r.getStatus() != ExamResult.Status.REJECTED)
                .findFirst();
    }

    public Optional<ExamResult> findLatest(String examSessionId) {
        List<ExamResult> results = repository.findByExamSessionId(examSessionId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(results.size() - 1));
    }
}
