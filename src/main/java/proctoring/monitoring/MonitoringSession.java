package proctoring.monitoring;

import java.util.UUID;

public class MonitoringSession {

    public enum Status { CLEARED, FLAGGED, VOIDED }

    private String id;
    private String examSessionId;
    private String studentId;
    private double plagiarismScore;
    private Status status;

    private MonitoringSession() {
    }

    public MonitoringSession(String examSessionId, String studentId, double plagiarismScore, boolean suspicious) {
        this.id = "MON-" + UUID.randomUUID().toString().substring(0, 8);
        this.examSessionId = examSessionId;
        this.studentId = studentId;
        this.plagiarismScore = plagiarismScore;
        this.status = suspicious ? Status.FLAGGED : Status.CLEARED;
    }

    public void cancel() {
        if (status == Status.VOIDED) {
            throw new IllegalStateException("Monitoring session " + id + " is already voided");
        }
        status = Status.VOIDED;
    }

    public String getId() { return id; }
    public String getExamSessionId() { return examSessionId; }
    public String getStudentId() { return studentId; }
    public double getPlagiarismScore() { return plagiarismScore; }
    public Status getStatus() { return status; }
}
