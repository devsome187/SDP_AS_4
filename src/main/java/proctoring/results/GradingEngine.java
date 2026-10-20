package proctoring.results;

import java.util.List;
import java.util.Map;

public class GradingEngine {

    private final Map<String, List<String>> answerKeys;
    private boolean online = true;

    public GradingEngine(Map<String, List<String>> answerKeys) {
        this.answerKeys = answerKeys;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public int grade(String examId, List<String> answers) {
        if (!online) {
            throw new GradingRejectedException("Grading engine is down for maintenance");
        }
        List<String> key = answerKeys.get(examId);
        if (key == null) {
            throw new GradingRejectedException("No answer key for exam " + examId);
        }
        if (answers.size() != key.size()) {
            throw new GradingRejectedException("Expected " + key.size() + " answers but got " + answers.size());
        }
        int correct = 0;
        for (int i = 0; i < key.size(); i++) {
            if (key.get(i).equalsIgnoreCase(answers.get(i).trim())) {
                correct++;
            }
        }
        return Math.round(100f * correct / key.size());
    }
}
