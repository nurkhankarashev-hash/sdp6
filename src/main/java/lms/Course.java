package lms;
public final class Course extends LmsNode.Container {
    private final int credits;
    public Course(String name, int credits) { super(name); this.credits = credits; }
    public int credits() { return credits; }
    public <R> R accept(LmsVisitor<R> v) { return v.visit(this); }
}
