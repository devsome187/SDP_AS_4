package proctoring.results;

public class GradingRejectedException extends RuntimeException {
    public GradingRejectedException(String message) {
        super(message);
    }
}
