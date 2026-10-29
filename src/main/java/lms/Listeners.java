package lms;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;
import lms.Events.*;

public final class Listeners {
    private Listeners() {}

    public static final class Notifier implements Observer<String> {
        private final String recipient;
        private final List<Event<String>> received = new CopyOnWriteArrayList<>();
        public Notifier(String recipient) { this.recipient = recipient; }
        public void onEvent(Event<String> event) {
            received.add(event);
            System.out.println(recipient + ": " + event.type() + " " + event.course() + " " + event.value());
        }
        public List<Event<String>> received() { return List.copyOf(received); }
    }
    public static final class AuditLog implements Observer<String> {
        private final LongAdder count = new LongAdder();
        public void onEvent(Event<String> event) { count.increment(); }
        public long count() { return count.sum(); }
    }
    public static final class GpaTracker implements Observer<String>, Subject<String> {
        private final EventSource<String> reports;
        private final DoubleAdder gradePoints = new DoubleAdder();
        private final LongAdder grades = new LongAdder();
        public GpaTracker(EventSource<String> reports) { this.reports = reports; }
        public void onEvent(Event<String> event) {
            gradePoints.add(Double.parseDouble(event.value()));
            grades.increment();
            reports.publish(new Event<>(Type.REPORT, event.course(), "GPA=" + gpa()));
        }
        public double gpa() { return grades.sum() == 0 ? 0 : gradePoints.sum() / grades.sum(); }
        public void subscribe(Type type, Observer<String> listener) { reports.subscribe(type, listener); }
        public void unsubscribe(Type type, Observer<String> listener) { reports.unsubscribe(type, listener); }
        public void publish(Event<String> event) { reports.publish(event); }
    }
    public static final class DeadlineInspector implements Observer<String> {
        private final java.util.Map<String, Course> courses;
        private final Subject<String> reports;
        public DeadlineInspector(java.util.Map<String, Course> courses, Subject<String> reports) {
            this.courses = courses;
            this.reports = reports;
        }
        public void onEvent(Event<String> event) {
            Course course = courses.get(event.course());
            if (course == null) return;
            WorkloadVisitor visitor = new WorkloadVisitor();
            course.walk(visitor);
            reports.publish(new Event<>(Type.REPORT, course.name(), "Workload=" + visitor.minutes() + " minutes"));
        }
    }
}
