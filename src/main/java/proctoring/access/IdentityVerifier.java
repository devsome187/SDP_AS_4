package proctoring.access;

import java.util.Set;

public class IdentityVerifier {

    private final Set<String> enrolledStudents;

    public IdentityVerifier(Set<String> enrolledStudents) {
        this.enrolledStudents = enrolledStudents;
    }

    public void verify(String studentId) {
        if (studentId == null || !studentId.matches("\\d{6}")) {
            throw new IdentityVerificationException("Invalid student ID: " + studentId);
        }
        if (!enrolledStudents.contains(studentId)) {
            throw new IdentityVerificationException("Student " + studentId + " is not enrolled");
        }
    }
}
