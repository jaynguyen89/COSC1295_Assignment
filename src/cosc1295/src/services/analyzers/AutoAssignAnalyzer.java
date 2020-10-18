package cosc1295.src.services.analyzers;

import cosc1295.src.controllers.ControllerBase;
import cosc1295.src.models.Student;
import cosc1295.src.models.Team;
import cosc1295.src.models.TeamFitness;
import helpers.commons.SharedConstants;
import helpers.utilities.LogicalAssistant;

import javafx.util.Pair;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

public class AutoAssignAnalyzer<T> extends SuperAnalyzer implements Callable<T> {

    public AutoAssignAnalyzer() {
        super();
    }

    //Suggestion responds in format Pair<Student, Pair<Team, Student>>
    @SuppressWarnings("unchecked")
    @Override
    public T call() {
        HashMap<Pair<Student, Pair<Team, Student>>, TeamFitness> metricsData = calculateMetrics();
        return (T) produceAssignSuggestion(metricsData);
    }

    private HashMap<Pair<Student, Pair<Team, Student>>, TeamFitness> calculateMetrics() {
        HashMap<Pair<Student, Pair<Team, Student>>, TeamFitness> metricsData = new HashMap<>();
        ControllerBase controllerBase = new ControllerBase();

        List<Student> assignableStudents = LogicalAssistant.filterUnteamedStudents(students, teams);

        for (Student student : assignableStudents) {
            if (!hasPreferenceData(student, preferences)) continue;

            for (Team team : teams) {
                //When Team has available slots to take more Students, so just assign, no replace
                if (team.getMembers().size() < SharedConstants.GROUP_LIMIT) {
                    boolean assignable = LogicalAssistant.isStudentAssignable(student, new Pair<>(team, null));
                    if (!assignable) continue;

                    Team clone = team.clone();
                    clone.getMembers().add(student);
                    TeamFitness metrics = controllerBase.calculateTeamFitnessMetricsFor(clone, projects, preferences);

                    metricsData.put(new Pair<>(student, new Pair<>(team, null)), metrics);
                    continue;
                }

                //When Team is full, the assignee will replace 1 Team member
                for (Student member : team.getMembers()) {
                    boolean assignable = LogicalAssistant.isStudentAssignable(student, new Pair<>(team, member));
                    if (!assignable) continue;

                    Team clone = team.clone();
                    clone.replaceMemberByUniqueId(member.getUniqueId(), student);

                    TeamFitness metrics = controllerBase.calculateTeamFitnessMetricsFor(clone, projects, preferences);
                    metricsData.put(new Pair<>(student, new Pair<>(team, member)), metrics);
                }
            }
        }

        return metricsData;
    }
}
