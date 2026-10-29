# Assignment 6 — University 
**Karashev Nurkhan SE-2540**

Java 17, Observer + Visitor, JUnit 5. No external runtime libraries.

## What the program does

- Grade source sends grade events.
- Deadline source sends deadline events.
- Student, parent and advisor receive messages. AuditLog counts events. GpaTracker receives grades and publishes GPA reports. DeadlineInspector receives deadlines and publishes workload reports. These give more than five listeners in total.
- Faculty → Program → Course → Lecture / Lab / Exam / Assignment is the LMS tree.
- CreditVisitor adds credits. WorkloadVisitor adds minutes and counts activities. ExamFilterVisitor selects exams. JsonVisitor exports the tree.
- On a Java deadline, DeadlineInspector visits **only the Java course**, not the whole faculty, and publishes the result as a REPORT event.

## Run

Open `pom.xml` in IntelliJ IDEA. Use Java 17. Run `lms.Main` (or run `mvn test` and `mvn exec:java -Dexec.mainClass=lms.Main` if the exec plugin is available).

Without Maven:

```text
javac -d out src/main/java/lms/*.java
java -cp out lms.Main
java -cp out lms.Main 4
```

The number after `Main` selects producer thread count (minimum 3). Use `mvn test` for JUnit tests. Output order can differ because producers work concurrently.

## Design decisions

**Observer:** Push model. Each immutable `Event<T>` contains type, course and value. Each source owns a thread-safe listener map (`ConcurrentHashMap` and `CopyOnWriteArrayList`). Subscribers register per type and can register/unregister during callbacks. Producers call `publish` synchronously; a `shutdown` and `awaitTermination` finish submitted tasks. `GpaTracker` is also a subject and republishes calculated reports.

**Visitor:** The tree owns traversal through `walk`. Each element calls `visitor.visit(this)` by double dispatch. The visitors contain the operations; element classes contain data and the small `accept` method only. Visitors are created per task, so their accumulated state is not shared between threads.

**Clean Code:** `Main` creates and connects objects (composition root). Dependencies are passed through constructors. No lazy initialized business objects. Tree is constructed before producers start.

**Limit:** GPA values in the demo are already on the 0–4 scale, so GPA is their simple average. The example prints events to the console; no database is required.

## Trade-off commits

1. `Add JSON visitor without modifying any element class`: only `src/main/java/lms/JsonVisitor.java` was added. New operations are cheap with Visitor.
2. `Add Assignment element and update every visitor`: `Assignment.java`, `LmsVisitor.java`, `CreditVisitor.java`, `WorkloadVisitor.java`, `ExamFilterVisitor.java`, `JsonVisitor.java` changed. New element types are expensive because all visitors must handle them.

## Tests

`src/test/java/lms/LmsTest.java` has **21 tests**, including:
- hand-calculated credits: 5 + 4 = 9;
- hand-calculated workload: 90 + 120 + 60 + 180 + 60 = 510 minutes;
- a 4-thread × 1,000 event stress test, expecting exactly 4,000 notifications;
- unsubscribe/subscribe inside a handler;
- only the related course being inspected.