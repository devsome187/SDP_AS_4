package proctoring.monitoring;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlagiarismChecker {

    private static final double THRESHOLD = 0.8;

    private final List<String> leakedAnswers;

    public PlagiarismChecker(List<String> leakedAnswers) {
        this.leakedAnswers = leakedAnswers;
    }

    public double similarity(List<String> answers) {
        Set<String> submitted = words(answers);
        Set<String> leaked = words(leakedAnswers);
        Set<String> all = new HashSet<>(submitted);
        all.addAll(leaked);
        if (all.isEmpty()) {
            return 0;
        }
        submitted.retainAll(leaked);
        return (double) submitted.size() / all.size();
    }

    public boolean isSuspicious(double similarity) {
        return similarity >= THRESHOLD;
    }

    private Set<String> words(List<String> answers) {
        Set<String> words = new HashSet<>();
        for (String answer : answers) {
            for (String word : answer.toLowerCase().split("\\W+")) {
                if (!word.isBlank()) {
                    words.add(word);
                }
            }
        }
        return words;
    }
}
