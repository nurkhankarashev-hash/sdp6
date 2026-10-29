package lms;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import lms.Events.*;
import lms.Listeners.*;

class LmsTest {
    private Faculty faculty;
    private Course java;
    private Course math;

    @BeforeEach
    void prepare() {
        faculty = new Faculty("Engineering");
        Program program = new Program("Software");
        java = new Course("Java", 5);
        java.add(new Lecture("OOP", 90));
        java.add(new Lab("Patterns", 120));
        java.add(new Exam("Final", 60));
        java.add(new Assignment("Homework", 180));
        math = new Course("Math", 4);
        math.add(new Lecture("Algebra", 60));
        program.add(java);
        program.add(math);
        faculty.add(program);
    }

    @Test void facultyContainsProgram() { assertEquals(1, faculty.children().size()); }
    @Test void programContainsTwoCourses() { assertEquals(2, ((Program) faculty.children().get(0)).children().size()); }
    @Test void childListCannotBeChanged() { assertThrows(UnsupportedOperationException.class, () -> faculty.children().clear()); }
    @Test void creditsWholeTree() { CreditVisitor v = new CreditVisitor(); faculty.walk(v); assertEquals(9, v.total()); }
    @Test void creditsOnlyJava() { CreditVisitor v = new CreditVisitor(); java.walk(v); assertEquals(5, v.total()); }
    @Test void workloadWholeTree() { WorkloadVisitor v = new WorkloadVisitor(); faculty.walk(v); assertEquals(510, v.minutes()); }
    @Test void workloadJavaOnly() { WorkloadVisitor v = new WorkloadVisitor(); java.walk(v); assertEquals(450, v.minutes()); }
    @Test void activityCount() { WorkloadVisitor v = new WorkloadVisitor(); faculty.walk(v); assertEquals(5, v.activities()); }
    @Test void filterExams() { ExamFilterVisitor v = new ExamFilterVisitor(); faculty.walk(v); assertEquals("Final", v.exams().get(0).name()); }
    @Test void filterOnlyRelatedSubtree() { ExamFilterVisitor v = new ExamFilterVisitor(); math.walk(v); assertTrue(v.exams().isEmpty()); }
    @Test void jsonContainsAllLevels() { JsonVisitor v = new JsonVisitor(); faculty.walk(v); assertTrue(v.json().contains("\"Faculty\"")); assertTrue(v.json().contains("\"Program\"")); assertTrue(v.json().contains("\"Assignment\"")); }
    @Test void jsonCorrectNesting() { JsonVisitor v = new JsonVisitor(); java.walk(v); assertTrue(v.json().startsWith("{\"type\":\"Course\"")); assertEquals(4, v.json().split("\"minutes\"", -1).length - 1); assertTrue(v.json().endsWith("]}")); }
    @Test void jsonEscapesQuotes() { JsonVisitor v = new JsonVisitor(); new Lecture("A\"B", 10).walk(v); assertTrue(v.json().contains("A\\\"B")); }
    @Test void onlySubscribedEventTypeDelivered() { EventSource<String> s = new EventSource<>(); AuditLog log = new AuditLog(); s.subscribe(Type.GRADE, log); s.publish(new Event<>(Type.DEADLINE, "Java", "today")); assertEquals(0, log.count()); }
    @Test void unsubscribeStopsNotification() { EventSource<String> s = new EventSource<>(); AuditLog log = new AuditLog(); s.subscribe(Type.GRADE, log); s.unsubscribe(Type.GRADE, log); s.publish(new Event<>(Type.GRADE, "Java", "3")); assertEquals(0, log.count()); }
    @Test void selfUnsubscribeInsideHandler() { EventSource<String> s = new EventSource<>(); AtomicInteger calls = new AtomicInteger(); Observer<String> listener = new Observer<>() { public void onEvent(Event<String> e) { calls.incrementAndGet(); s.unsubscribe(Type.GRADE, this); } }; s.subscribe(Type.GRADE, listener); s.publish(new Event<>(Type.GRADE,"Java","4")); s.publish(new Event<>(Type.GRADE,"Java","4")); assertEquals(1,calls.get()); }
    @Test void subscribeInsideHandler() { EventSource<String> s = new EventSource<>(); AuditLog second = new AuditLog(); s.subscribe(Type.GRADE, e -> s.subscribe(Type.GRADE, second)); s.publish(new Event<>(Type.GRADE,"Java","4")); s.publish(new Event<>(Type.GRADE,"Java","4")); assertEquals(1,second.count()); }
    @Test void chainPublishesGpa() { EventSource<String> report = new EventSource<>(); GpaTracker tracker = new GpaTracker(report); AuditLog log = new AuditLog(); tracker.subscribe(Type.REPORT, log); tracker.onEvent(new Event<>(Type.GRADE,"Java","3.0")); tracker.onEvent(new Event<>(Type.GRADE,"Math","4.0")); assertEquals(3.5,tracker.gpa(),0.001); assertEquals(2,log.count()); }
    @Test void integrationUsesOnlyTargetCourse() { EventSource<String> reports = new EventSource<>(); Notifier notifier = new Notifier("Test"); reports.subscribe(Type.REPORT, notifier); DeadlineInspector inspector = new DeadlineInspector(Map.of("Java",java,"Math",math), reports); inspector.onEvent(new Event<>(Type.DEADLINE,"Java","tomorrow")); assertEquals("Workload=450 minutes", notifier.received().get(0).value()); }
    @Test void integrationUnknownCourseDoesNothing() { EventSource<String> reports = new EventSource<>(); AuditLog log = new AuditLog(); reports.subscribe(Type.REPORT, log); new DeadlineInspector(Map.of("Java", java),reports).onEvent(new Event<>(Type.DEADLINE,"Unknown","today")); assertEquals(0, log.count()); }
    @Test void fourThreadsFourThousandEvents() throws Exception {
        EventSource<String> source = new EventSource<>(); AuditLog count = new AuditLog(); source.subscribe(Type.GRADE, count);
        var pool = Executors.newFixedThreadPool(4); CountDownLatch start = new CountDownLatch(1);
        for (int t = 0; t < 4; t++) pool.submit(() -> { try { start.await(); for (int i = 0; i < 1000; i++) source.publish(new Event<>(Type.GRADE,"Java","4")); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } });
        start.countDown(); pool.shutdown();
        assertTrue(pool.awaitTermination(20,TimeUnit.SECONDS));
        assertEquals(4000,count.count());
    }
}
