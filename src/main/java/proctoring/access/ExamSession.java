package proctoring.access;

import java.time.Instant;
import java.util.UUID;

public class ExamSession {

    public enum Status { STARTED, SUBMITTED, CANCELLED }

    private String id;
    private String studentId;
    private String examId;
    private Instant startedAt;
    private Instant deadline;
    private Status status;

    private ExamSession() {
    }

    public ExamSession(String studentId, String examId, Instant startedAt, Instant deadline) {
        this.id = "SES-" + UUID.randomUUID().toString().substring(0, 8);
        this.studentId = studentId;
        this.examId = examId;
        this.startedAt = startedAt;
        this.deadline = deadline;
        this.status = Status.STARTED;
    }

    public void submit() {
        if (status != Status.STARTED) {
            throw new IllegalStateException("Cannot submit session " + id + " in status " + status);
        }
        status = Status.SUBMITTED;
    }

    public void cancel() {
        if (status == Status.CANCELLED) {
            throw new IllegalStateException("Session " + id + " is already cancelled");
        }
        status = Status.CANCELLED;
    }

    public String getId() { return id; }
    public String getStudentId() { return studentId; }
    public String getExamId() { return examId; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getDeadline() { return deadline; }
    public Status getStatus() { return status; }
}
