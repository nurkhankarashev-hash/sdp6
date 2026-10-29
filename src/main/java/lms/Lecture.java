package lms;
public final class Lecture extends LmsNode {
    private final int minutes;
    public Lecture(String name, int minutes) { super(name); this.minutes = minutes; }
    public int minutes() { return minutes; }
    public <R> R accept(LmsVisitor<R> v) { return v.visit(this); }
}
