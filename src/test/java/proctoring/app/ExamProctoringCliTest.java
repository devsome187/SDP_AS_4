package proctoring.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExamProctoringCliTest {

    @TempDir
    Path dataDir;

    private String run(String... lines) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String input = String.join("\n", lines) + "\n";
        new ExamProctoringCli(new ProctoringSystem(dataDir),
                new ByteArrayInputStream(input.getBytes()), new PrintStream(output)).run();
        return output.toString();
    }

    @Test
    void allMenuOptionsWork() {
        String out = run(
                "4",
                "1", "230001", "SDP-FINAL", "",
                "2", "230002", "SDP-FINAL", "",
                "3", "230003", "OOP-MIDTERM", "encapsulation, inheritance, polymorphism",
                "1", "12ab", "SDP-FINAL", "",
                "9",
                "4",
                "7");

        assertTrue(out.contains("Nothing saved yet"));
        assertTrue(out.contains("COMPLETED, score 100%"));
        assertTrue(out.contains("Rollback: [A: session"));
        assertTrue(out.contains("Rollback: [B: monitoring"));
        assertTrue(out.contains("Rejected: Invalid student ID"));
        assertTrue(out.contains("Unknown option"));
    }

    @Test
    void interruptedExamIsResumedAfterRestart() {
        run("5", "y", "230001", "SDP-FINAL", "", "7");

        String out = run("5", "5", "n");

        assertTrue(out.contains("Unfinished exams found on disk"));
        assertTrue(out.contains("COMPLETED"));
    }

    @Test
    void manualCompensationAndErrors() {
        run("5", "y", "230002", "SDP-FINAL", "", "7");
        String processId = new ProctoringSystem(dataDir).adminFacade().listUnfinished().get(0).processId();

        String out = run("6", processId, "6", "PRC-unknown");

        assertTrue(out.contains("COMPENSATED"));
        assertTrue(out.contains("Error: Exam process not found"));
    }
}
