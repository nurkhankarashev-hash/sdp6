package lms;
public final class Exam extends LmsNode {
    private final int minutes;
    public Exam(String name, int minutes) { super(name); this.minutes = minutes; }
    public int minutes() { return minutes; }
    public <R> R accept(LmsVisitor<R> v) { return v.visit(this); }
}
