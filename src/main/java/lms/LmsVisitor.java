package lms;
public interface LmsVisitor<R> {
    R visit(Faculty node);
    R visit(Program node);
    R visit(Course node);
    R visit(Lecture node);
    R visit(Lab node);
    R visit(Exam node);
    default void endContainer() {}
}
