package proctoring.access;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public class ExamTimer {

    private final Clock clock;
    private final Map<String, Duration> durations;

    public ExamTimer(Clock clock, Map<String, Duration> durations) {
        this.clock = clock;
        this.durations = durations;
    }

    public Instant now() {
        return clock.instant();
    }

    public Instant deadlineFor(String examId, Instant start) {
        return start.plus(durations.getOrDefault(examId, Duration.ofMinutes(90)));
    }
}
