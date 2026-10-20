package proctoring.facade;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExamProcess {

    public enum Stage { PENDING_B, PENDING_C, COMPLETED, COMPENSATED }

    private String id;
    private String studentId;
    private String examId;
    private List<String> answers;
    private Stage stage;
    private String examSessionId;
    private String monitoringId;
    private String resultId;
    private String failureReason;
    private List<String> rollbackLog = new ArrayList<>();

    private ExamProcess() {
    }

    public ExamProcess(String studentId, String examId, List<String> answers, String examSessionId) {
        this.id = "PRC-" + UUID.randomUUID().toString().substring(0, 8);
        this.studentId = studentId;
        this.examId = examId;
        this.answers = answers;
        this.examSessionId = examSessionId;
        this.stage = Stage.PENDING_B;
    }

    public void monitoringDone(String monitoringId) {
        this.monitoringId = monitoringId;
        this.stage = Stage.PENDING_C;
    }

    public void gradingDone(String resultId) {
        this.resultId = resultId;
        this.stage = Stage.COMPLETED;
    }

    public void compensated(String reason, List<String> rollbackLog) {
        this.failureReason = reason;
        this.rollbackLog = rollbackLog;
        this.stage = Stage.COMPENSATED;
    }

    public boolean isFinished() {
        return stage == Stage.COMPLETED || stage == Stage.COMPENSATED;
    }

    public String getId() { return id; }
    public String getStudentId() { return studentId; }
    public String getExamId() { return examId; }
    public List<String> getAnswers() { return answers; }
    public Stage getStage() { return stage; }
    public String getExamSessionId() { return examSessionId; }
    public String getMonitoringId() { return monitoringId; }
    public String getResultId() { return resultId; }
    public String getFailureReason() { return failureReason; }
    public List<String> getRollbackLog() { return rollbackLog; }
}
