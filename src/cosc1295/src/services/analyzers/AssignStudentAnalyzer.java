package cosc1295.src.services.analyzers;

import cosc1295.src.controllers.ControllerBase;
import cosc1295.src.models.*;
import helpers.commons.SharedConstants;
import helpers.utilities.LogicalAssistant;
import javafx.util.Pair;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Used for suggesting an assignee to a Team.
 * Returns data of type Pair<Student, Student> with Key being assignee, Value being replaced member (nullable)
 * @param <T>
 */
public class AssignStudentAnalyzer<T> extends SuperAnalyzer implements Callable<T> {

    private final Team team;

    public AssignStudentAnalyzer(Team team) {
        super();
        this.team = team;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T call() {
        //Pair<Student, Student> : Key being a student from list, Value being a team member
        HashMap<Pair<Student, Student>, TeamFitness> metricsData = calculateMetrics();
        return (T) produceAssignSuggestion(metricsData);
    }

    /**
     * Calculates Fitness Metrics for all possible assignees if they satisfy all Team's requirements.
     * @return
     */
    private HashMap<Pair<Student, Student>, TeamFitness> calculateMetrics() {
        HashMap<Pair<Student, Student>, TeamFitness> metricsData = new HashMap<>();
        ControllerBase controllerBase = new ControllerBase();

        List<Student> assignableStudents = LogicalAssistant.filterUnteamedStudents(students, teams);

        for (Student student : assignableStudents) {
            if (!hasPreferenceData(student, preferences) || team.hasMember(student.getUniqueId())) continue;

            //When Team has available slots to take more Students, so just assign, no replace
            if (team.getMembers().size() < SharedConstants.GROUP_LIMIT) {
                boolean assignable = LogicalAssistant.isStudentAssignable(student, new Pair<>(team, null));
                if (!assignable) continue;

                Team clone = team.clone();
                clone.getMembers().add(student);
                TeamFitness metrics = controllerBase.calculateTeamFitnessMetricsFor(clone, projects, preferences);

                metricsData.put(new Pair<>(student, null), metrics);
                continue;
            }

            //When Team is full, the assignee will replace 1 Team member
            for (Student member : team.getMembers()) {
                boolean assignable = LogicalAssistant.isStudentAssignable(student, new Pair<>(team, member));
                if (!assignable) continue;

                Team clone = team.clone();
                clone.replaceMemberByUniqueId(member.getUniqueId(), student);

                TeamFitness metrics = controllerBase.calculateTeamFitnessMetricsFor(clone, projects, preferences);
                metricsData.put(new Pair<>(student, member), metrics);
            }
        }

        return metricsData;
    }
}
