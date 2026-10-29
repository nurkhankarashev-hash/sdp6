package lms;
public final class Faculty extends LmsNode.Container {
    public Faculty(String name) { super(name); }
    public <R> R accept(LmsVisitor<R> v) { return v.visit(this); }
}
