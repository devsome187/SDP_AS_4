package proctoring.access;

import java.time.Instant;
import java.util.NoSuchElementException;

public class ExamAccessService {

    private final IdentityVerifier verifier;
    private final ExamTimer timer;
    private final ExamSessionRepository repository;

    public ExamAccessService(IdentityVerifier verifier, ExamTimer timer, ExamSessionRepository repository) {
        this.verifier = verifier;
        this.timer = timer;
        this.repository = repository;
    }

    public ExamSession startExam(String studentId, String examId) {
        verifier.verify(studentId);
        Instant now = timer.now();
        ExamSession session = new ExamSession(studentId, examId, now, timer.deadlineFor(examId, now));
        repository.save(session);
        return session;
    }

    public void submit(String sessionId) {
        ExamSession session = get(sessionId);
        session.submit();
        repository.save(session);
    }

    public void cancel(String sessionId) {
        ExamSession session = get(sessionId);
        session.cancel();
        repository.save(session);
    }

    public ExamSession get(String sessionId) {
        return repository.findById(sessionId)
                .orElseThrow(() -> new NoSuchElementException("Exam session not found: " + sessionId));
    }
}
