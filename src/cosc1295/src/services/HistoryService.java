package cosc1295.src.services;

import cosc1295.src.models.Student;
import cosc1295.src.models.Team;
import helpers.commons.SharedConstants;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
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

    public Pair<Pair<Team, Team>, Pair<Student, Student>> popLastChange() {
        return history.pop();
    }

    public boolean isEmpty() {
        return history.empty();
    }

    /**
     * Because user can create a new Team while they are assigning or swapping Students,
     * So the history item saved to `history` possibly has its Team ID = 0. Therefore after the Team is
     * saved into database, it has an ID and this method set that ID for that Team in the history.
     * @param newTeamId int
     * @param featureToRun String
     */
    public void reviseLastChange(int newTeamId, String featureToRun) {
        //Since user can assign multiple Students to a Team at once, so it needs a while statement to
        //set ID for all the associated history items. Once the ID is set, the next time new Team is created
        //will not clash with the previous new Team in history.
        while (true) {
            Pair< //Set the history for undoing feature
                Pair<Team, Team>,
                Pair<Student, Student>
            > action = popLastChange();

            if (featureToRun.equalsIgnoreCase(SharedConstants.ACTION_ASSIGN) && action.getKey().getKey().getId() == 0) {
                action.getKey().getKey().setId(newTeamId);
                continue;
            }

            if (featureToRun.equalsIgnoreCase(SharedConstants.ACTION_SWAP) && action.getKey().getValue().getId() == 0) {
                action.getKey().getValue().setId(newTeamId);
                continue;
            }

            add(action);
            break;
        }
    }
}
