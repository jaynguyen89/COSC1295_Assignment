package cosc1295.src.services.analyzers;

import cosc1295.src.controllers.ControllerBase;
import cosc1295.src.models.Student;
import cosc1295.src.models.Team;
import cosc1295.src.models.TeamFitness;
import helpers.commons.SharedConstants;
import helpers.utilities.LogicalAssistant;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.concurrent.Callable;

public class TeamToAssignAnalyzer<T> extends SuperAnalyzer implements Callable<T> {

    private final Student student;

    public TeamToAssignAnalyzer(Student student) {
        super();
        this.student = student;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T call() {
        //Pair<Team, Student> - Team to get the assignee, Student (team member - nullable) to be replaced
        HashMap<Pair<Team, Student>, TeamFitness> metricsData = calculateMetrics();
        return (T) produceAssignSuggestion(metricsData);
    }

    private HashMap<Pair<Team, Student>, TeamFitness> calculateMetrics() {
        HashMap<Pair<Team, Student>, TeamFitness> metricsData = new HashMap<>();
        ControllerBase controllerBase = new ControllerBase();

        for (Team team : teams) {
            if (team.hasMember(student.getUniqueId())) continue;

            //When team has available slot to take more Students, consider adding the Student to Team, and then...
            if (team.getMembers().size() < SharedConstants.GROUP_LIMIT) {
                boolean assignable = LogicalAssistant.isStudentAssignable(student, new Pair<>(team, null));
                if (assignable) {
                    Team clone = team.clone();
                    clone.addMember(student);
                    TeamFitness metrics = controllerBase.calculateTeamFitnessMetricsFor(clone, projects, preferences);

                    metricsData.put(new Pair<>(team, null), metrics);
                }

                if (team.getMembers().size() == 1) continue;
            }

            //...also consider replacing 1 member in Team by the assignee
            for (Student member : team.getMembers()) {
                boolean assignable = LogicalAssistant.isStudentAssignable(student, new Pair<>(team, member));
                if (!assignable) continue;

                Team clone = team.clone();
                clone.replaceMemberByUniqueId(member.getUniqueId(), student);

                TeamFitness metrics = controllerBase.calculateTeamFitnessMetricsFor(clone, projects, preferences);
                metricsData.put(new Pair<>(team, member), metrics);
            }
        }

        return metricsData;
    }
}
