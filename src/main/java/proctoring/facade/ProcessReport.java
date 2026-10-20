package proctoring.facade;

import java.util.List;

public record ProcessReport(String processId, String studentId, String examId, ExamProcess.Stage stage,
                            String sessionStatus, String monitoringStatus, String resultStatus,
                            List<String> rollbackLog) {

    @Override
    public String toString() {
        return processId + " " + stage + " | student " + studentId + ", exam " + examId
                + " | A=" + sessionStatus + " B=" + monitoringStatus + " C=" + resultStatus
                + (rollbackLog.isEmpty() ? "" : " | rollback " + rollbackLog);
    }
}
