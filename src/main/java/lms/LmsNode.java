package lms;

import java.util.ArrayList;
import java.util.List;

public abstract class LmsNode {
    private final String name;
    protected LmsNode(String name) { this.name = name; }
    public String name() { return name; }
    public abstract <R> R accept(LmsVisitor<R> visitor);
    public final <R> void walk(LmsVisitor<R> visitor) {
        accept(visitor);
        if (this instanceof Container container) {
            for (LmsNode child : container.children()) child.walk(visitor);
            visitor.endContainer();
        }
    }
    public static abstract class Container extends LmsNode {
        private final List<LmsNode> children = new ArrayList<>();
        protected Container(String name) { super(name); }
        public void add(LmsNode child) { children.add(child); }
        public List<LmsNode> children() { return List.copyOf(children); }
    }
}
