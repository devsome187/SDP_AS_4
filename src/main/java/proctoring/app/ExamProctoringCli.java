package proctoring.app;

import proctoring.facade.ExamOutcome;
import proctoring.facade.ExamRequestRejectedException;
import proctoring.facade.ProcessReport;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ExamProctoringCli {

    private final ProctoringSystem system;
    private final Scanner in;
    private final PrintStream out;

    public ExamProctoringCli(ProctoringSystem system, InputStream in, PrintStream out) {
        this.system = system;
        this.in = new Scanner(in);
        this.out = out;
    }

    public static void main(String[] args) {
        ProctoringSystem system = new ProctoringSystem(Path.of("data"));
        new ExamProctoringCli(system, System.in, System.out).run();
    }

    public void run() {
        out.println("Exam Proctoring");
        out.println("Students: " + ProctoringSystem.STUDENTS + ", exams: " + ProctoringSystem.ANSWER_KEYS.keySet());
        List<ProcessReport> unfinished = system.adminFacade().listUnfinished();
        if (!unfinished.isEmpty()) {
            out.println("Unfinished exams found on disk:");
            unfinished.forEach(r -> out.println("  " + r));
        }

        while (true) {
            out.println();
            out.println("1. Start normal exam");
            out.println("2. Simulate Screen Monitor failure");
            out.println("3. Simulate Grading Engine failure");
            out.println("4. View persisted state");
            out.println("5. Resume interrupted exam");
            out.println("6. Manual compensation");
            out.println("7. Exit");
            String choice = ask("> ");
            if (choice == null || choice.equals("7")) {
                return;
            }
            try {
                switch (choice) {
                    case "1" -> startExam();
                    case "2" -> screenMonitorFailure();
                    case "3" -> gradingEngineFailure();
                    case "4" -> showState();
                    case "5" -> resume();
                    case "6" -> compensate();
                    default -> out.println("Unknown option");
                }
            } catch (ExamRequestRejectedException e) {
                out.println("Rejected: " + e.getMessage());
            } catch (RuntimeException e) {
                out.println("Error: " + e.getMessage());
            }
        }
    }

    private void startExam() {
        String studentId = ask("Student ID: ");
        String examId = ask("Exam ID: ");
        List<String> answers = askAnswers(examId);
        print(system.examFacade().conductExam(studentId, examId, answers));
    }

    private void screenMonitorFailure() {
        system.setScreenMonitorOnline(false);
        try {
            startExam();
        } finally {
            system.setScreenMonitorOnline(true);
        }
    }

    private void gradingEngineFailure() {
        system.setGradingEngineOnline(false);
        try {
            startExam();
        } finally {
            system.setGradingEngineOnline(true);
        }
    }

    private void showState() {
        List<ProcessReport> reports = system.adminFacade().listProcesses();
        if (reports.isEmpty()) {
            out.println("Nothing saved yet");
        }
        reports.forEach(r -> out.println(r));
    }

    private void resume() {
        if (system.adminFacade().listUnfinished().isEmpty()) {
            String answer = ask("No unfinished exams. Start one and stop after step A? (y/n): ");
            if (answer != null && answer.equalsIgnoreCase("y")) {
                String studentId = ask("Student ID: ");
                String examId = ask("Exam ID: ");
                String processId = system.examFacade().beginExam(studentId, examId, askAnswers(examId));
                out.println("Saved " + processId + " as PENDING_B. Restart the app and choose 5.");
            }
            return;
        }
        system.examFacade().resumeUnfinished().forEach(this::print);
    }

    private void compensate() {
        showState();
        String processId = ask("Process ID: ");
        out.println(system.adminFacade().compensate(processId, "Manual compensation"));
    }

    private List<String> askAnswers(String examId) {
        String line = ask("Answers separated by commas (empty = correct answers): ");
        if (line == null || line.isBlank()) {
            return ProctoringSystem.ANSWER_KEYS.getOrDefault(examId, List.of());
        }
        List<String> answers = new ArrayList<>();
        for (String part : line.split(",")) {
            answers.add(part.trim());
        }
        return answers;
    }

    private void print(ExamOutcome outcome) {
        out.println(outcome);
        if (!outcome.isCompleted()) {
            out.println("Rollback: " + system.adminFacade().inspect(outcome.processId()).rollbackLog());
        }
    }

    private String ask(String prompt) {
        out.print(prompt);
        return in.hasNextLine() ? in.nextLine().trim() : null;
    }
}
