package lms;
public final class WorkloadVisitor implements LmsVisitor<Void> {
    private int minutes;
    private int activities;
    public int minutes() { return minutes; }
    public int activities() { return activities; }
    public Void visit(Faculty n) { return null; }
    public Void visit(Program n) { return null; }
    public Void visit(Course n) { return null; }
    public Void visit(Lecture n) { minutes += n.minutes(); activities++; return null; }
    public Void visit(Lab n) { minutes += n.minutes(); activities++; return null; }
    public Void visit(Exam n) { minutes += n.minutes(); activities++; return null; }
    public Void visit(Assignment n) { minutes += n.minutes(); activities++; return null; }
}
