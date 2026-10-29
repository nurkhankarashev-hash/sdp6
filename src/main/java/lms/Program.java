package lms;
public final class Program extends LmsNode.Container {
    public Program(String name) { super(name); }
    public <R> R accept(LmsVisitor<R> v) { return v.visit(this); }
}
