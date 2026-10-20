package proctoring.facade;

public class ExamRequestRejectedException extends RuntimeException {
    public ExamRequestRejectedException(String message) {
        super(message);
    }
}
