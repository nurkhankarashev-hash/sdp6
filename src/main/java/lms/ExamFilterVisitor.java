package lms;
import java.util.ArrayList;
import java.util.List;
public final class ExamFilterVisitor implements LmsVisitor<Void> {
    private final List<Exam> exams = new ArrayList<>();
    public List<Exam> exams() { return List.copyOf(exams); }
    public Void visit(Faculty n) { return null; }
    public Void visit(Program n) { return null; }
    public Void visit(Course n) { return null; }
    public Void visit(Lecture n) { return null; }
    public Void visit(Lab n) { return null; }
    public Void visit(Exam n) { exams.add(n); return null; }
    public Void visit(Assignment n) { return null; }
}
