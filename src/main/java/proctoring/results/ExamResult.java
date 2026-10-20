package proctoring.results;

import java.util.UUID;

public class ExamResult {

    public enum Status { RECORDED, REJECTED, VOIDED }

    private String id;
    private String examSessionId;
    private String studentId;
    private String examId;
    private int score;
    private String grade;
    private String reason;
    private Status status;

    private ExamResult() {
    }

    private ExamResult(String examSessionId, String studentId, String examId) {
        this.id = "RES-" + UUID.randomUUID().toString().substring(0, 8);
        this.examSessionId = examSessionId;
        this.studentId = studentId;
        this.examId = examId;
    }

    public static ExamResult recorded(String examSessionId, String studentId, String examId, int score, String grade) {
        ExamResult result = new ExamResult(examSessionId, studentId, examId);
        result.score = score;
        result.grade = grade;
        result.status = Status.RECORDED;
        return result;
    }

    public static ExamResult rejected(String examSessionId, String studentId, String examId, String reason) {
        ExamResult result = new ExamResult(examSessionId, studentId, examId);
        result.reason = reason;
        result.status = Status.REJECTED;
        return result;
    }

    public void cancel() {
        if (status != Status.RECORDED) {
            throw new IllegalStateException("Cannot void result " + id + " in status " + status);
        }
        status = Status.VOIDED;
    }

    public String getId() { return id; }
    public String getExamSessionId() { return examSessionId; }
    public String getStudentId() { return studentId; }
    public String getExamId() { return examId; }
    public int getScore() { return score; }
    public String getGrade() { return grade; }
    public String getReason() { return reason; }
    public Status getStatus() { return status; }
}
