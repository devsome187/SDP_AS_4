package proctoring.facade;

public record ExamOutcome(String processId, ExamProcess.Stage stage, Integer score, String grade, String message) {

    public boolean isCompleted() {
        return stage == ExamProcess.Stage.COMPLETED;
    }

    @Override
    public String toString() {
        if (isCompleted()) {
            return processId + " COMPLETED, score " + score + "%, grade " + grade;
        }
        return processId + " " + stage + (message != null ? " (" + message + ")" : "");
    }
}
