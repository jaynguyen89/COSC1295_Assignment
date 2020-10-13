package cosc1295.src.services;

import cosc1295.src.models.Student;
import cosc1295.src.models.Team;
import javafx.util.Pair;

import java.util.Stack;

/**
 * Singleton class
 */
public final class HistoryService {

    /**
     * The history object that stores the changes from `assign` and `swap` activities.
     * Pair<Team, Team> - Key should always hold a Team, if Value == null means `assign`, otherwise `swap`.
     * Pair<Student, Student> - For swap, both Key and Value must hold a Student. For assign,
     * Key must hold a Student being assigned, if Value == null means no Student in Team was replaced,
     * if Value != null means a Student in Team was replaced.
     */
    private static Stack<
        Pair<
                Pair<Team, Team>,
                Pair<Student, Student>
            >
    > history;

    private static HistoryService historyService;

    private HistoryService() {
        history = new Stack<>();
    }

    public static HistoryService getInstance() {
        if (historyService == null) {
            synchronized (HistoryService.class) {
                historyService = historyService == null
                    ? new HistoryService()
                    : historyService;
            }
        }

        return historyService;
    }

    public void add(Pair<Pair<Team, Team>, Pair<Student, Student>> entry) {
        history.push(entry);
    }

    public Pair<Pair<Team, Team>, Pair<Student, Student>> getLastChangeAndRemove() {
        return history.pop();
    }

    public boolean isEmpty() {
        return history.empty();
    }
}
