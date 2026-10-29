package lms;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

public final class Events {
    private Events() {}

    public enum Type { GRADE, DEADLINE, REPORT }
    public record Event(Type type, String course, String value) {}
    public interface Observer<T> { void onEvent(Event<T> event); }
    public interface Subject<T> {
        void subscribe(Type type, Observer<T> listener);
        void unsubscribe(Type type, Observer<T> listener);
        void publish(Event<T> event);
    }
    public static final class EventSource<T> implements Subject<T> {
        private final ConcurrentHashMap<Type, CopyOnWriteArrayList<Observer<T>>> listeners = new ConcurrentHashMap<>();
        public void subscribe(Type type, Observer<T> listener) {
            listeners.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).addIfAbsent(listener);
        }
        public void unsubscribe(Type type, Observer<T> listener) {
            var list = listeners.get(type);
            if (list != null) list.remove(listener);
        }
        public void publish(Event<T> event) {
            var list = listeners.get(event.type());
            if (list != null) for (var listener : list) listener.onEvent(event);
        }
    }
}
