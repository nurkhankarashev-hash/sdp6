package lms;
public final class CreditVisitor implements LmsVisitor<Void> {
    private int total;
    public int total() { return total; }
    public Void visit(Faculty n) { return null; }
    public Void visit(Program n) { return null; }
    public Void visit(Course n) { total += n.credits(); return null; }
    public Void visit(Lecture n) { return null; }
    public Void visit(Lab n) { return null; }
    public Void visit(Exam n) { return null; }
}
