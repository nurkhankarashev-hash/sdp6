package lms;

import java.util.ArrayDeque;
import java.util.Deque;

public final class JsonVisitor implements LmsVisitor<Void> {
    private final StringBuilder json = new StringBuilder();
    private final Deque<Boolean> first = new ArrayDeque<>();

    public String json() { return json.toString(); }
    private String escape(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
    private void separate() {
        if (!first.isEmpty()) {
            if (!first.pop()) json.append(',');
            first.push(false);
        }
    }
    private void open(String type, String name) {
        separate();
        json.append("{\"type\":\"").append(type).append("\",\"name\":\"")
                .append(escape(name)).append("\",\"children\":[");
        first.push(true);
    }
    private void leaf(String type, String name, int minutes) {
        separate();
        json.append("{\"type\":\"").append(type).append("\",\"name\":\"")
                .append(escape(name)).append("\",\"minutes\":").append(minutes).append('}');
    }
    public Void visit(Faculty n) { open("Faculty", n.name()); return null; }
    public Void visit(Program n) { open("Program", n.name()); return null; }
    public Void visit(Course n) {
        open("Course", n.name());
        return null;
    }
    public Void visit(Lecture n) { leaf("Lecture", n.name(), n.minutes()); return null; }
    public Void visit(Lab n) { leaf("Lab", n.name(), n.minutes()); return null; }
    public Void visit(Exam n) { leaf("Exam", n.name(), n.minutes()); return null; }
    public Void visit(Assignment n) { leaf("Assignment", n.name(), n.minutes()); return null; }
    public void endContainer() { json.append("]}"); first.pop(); }
}
