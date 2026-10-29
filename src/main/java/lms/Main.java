package lms;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lms.Events.*;
import lms.Listeners.*;

public final class Main {
    public static void main(String[] args) throws InterruptedException {
        int threads = args.length == 0 ? 3 : Integer.parseInt(args[0]);
        if (threads < 3) throw new IllegalArgumentException("Use at least 3 threads");

        Faculty faculty = new Faculty("Engineering");
        Program program = new Program("Software Engineering");
        Course java = new Course("Java", 5);
        java.add(new Lecture("OOP", 90));
        java.add(new Lab("Patterns", 120));
        java.add(new Exam("Final", 60));
        java.add(new Assignment("Observer and Visitor", 180));
        Course maths = new Course("Math", 4);
        maths.add(new Lecture("Algebra", 60));
        program.add(java);
        program.add(maths);
        faculty.add(program);

        EventSource<String> grades = new EventSource<>();
        EventSource<String> deadlines = new EventSource<>();
        EventSource<String> reports = new EventSource<>();
        GpaTracker gpa = new GpaTracker(reports);
        Notifier student = new Notifier("Student");
        Notifier parent = new Notifier("Parent");
        Notifier advisor = new Notifier("Advisor");
        AuditLog audit = new AuditLog();
        DeadlineInspector inspection = new DeadlineInspector(Map.of("Java", java, "Math", maths), reports);
        grades.subscribe(Type.GRADE, student);
        grades.subscribe(Type.GRADE, parent);
        grades.subscribe(Type.GRADE, advisor);
        grades.subscribe(Type.GRADE, gpa);
        grades.subscribe(Type.GRADE, audit);
        deadlines.subscribe(Type.DEADLINE, student);
        deadlines.subscribe(Type.DEADLINE, parent);
        deadlines.subscribe(Type.DEADLINE, advisor);
        deadlines.subscribe(Type.DEADLINE, audit);
        deadlines.subscribe(Type.DEADLINE, inspection);
        gpa.subscribe(Type.REPORT, student);
        gpa.subscribe(Type.REPORT, advisor);
        gpa.subscribe(Type.REPORT, audit);

        var pool = Executors.newFixedThreadPool(threads);
        pool.submit(() -> grades.publish(new Event<>(Type.GRADE, "Java", "3.7")));
        pool.submit(() -> grades.publish(new Event<>(Type.GRADE, "Math", "4.0")));
        pool.submit(() -> deadlines.publish(new Event<>(Type.DEADLINE, "Java", "Tomorrow")));
        pool.shutdown();
        if (!pool.awaitTermination(30, TimeUnit.SECONDS)) throw new IllegalStateException("Tasks did not finish");

        CreditVisitor credits = new CreditVisitor(); faculty.walk(credits);
        WorkloadVisitor workload = new WorkloadVisitor(); faculty.walk(workload);
        ExamFilterVisitor exams = new ExamFilterVisitor(); faculty.walk(exams);
        JsonVisitor json = new JsonVisitor(); faculty.walk(json);
        System.out.println("Credits: " + credits.total());
        System.out.println("Workload: " + workload.minutes() + " minutes");
        System.out.println("Exams: " + exams.exams().size());
        System.out.println("GPA: " + gpa.gpa());
        System.out.println("Audit events: " + audit.count());
        System.out.println("Transcript: " + json.json());
    }
}
