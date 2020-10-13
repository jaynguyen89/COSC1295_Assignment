package cosc1295.src.services.analyzers;

import cosc1295.src.models.Student;
import cosc1295.src.models.Team;
import cosc1295.src.models.TeamFitness;

import javafx.util.Pair;
import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * Used for suggesting a swap when user have selected 1 Team.
 * Returns data of type Pair<Team, Pair<Student, Student>>
 *     with Team being the second Team in swap,
 *     and Pair<Student, Student> being the Students in 2 Teams for swap
 * @param <T>
 */
public class SecondTeamAnalyzer<T> extends SuperAnalyzer implements Callable<T> {

    private final Team selectedTeam;

    public SecondTeamAnalyzer(Team team) {
        super();
        selectedTeam = team;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T call() {
        HashMap<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>> metricsData = calculateMetrics();
        Pair<Student, Student> suggestion = produceSwapSuggestion(metricsData);
        Team secondTeam = findTeamByMember(suggestion.getValue());

        return (T) new Pair<>(secondTeam, suggestion);
    }

    /**
     * Calculates the Fitness Metrics for each possible pair combination of Students across all Teams.
     * `Possible` means each pair is checked against all team requirements.
     * Returns HashMap of Pair<Student, Student> being the possible swap,
     * and Pair<TeamFitness, TeamFitness> being the Fitness Metrics of 2 Teams.
     * @return HashMap<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>>
     */
    private HashMap<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>> calculateMetrics() {
        HashMap<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>> metricsData = new HashMap<>();

        for (Team team : teams) {
            if (team.getId() == selectedTeam.getId()) continue;

            for (Student member : selectedTeam.getMembers())
                for (Student elector : team.getMembers()) {
                    Pair<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>> metrics = produceMetricsData(
                        selectedTeam, team, member, elector
                    );
                    if (metrics == null) continue;

                    metricsData.put(metrics.getKey(), metrics.getValue());
                }
        }

        return metricsData;
    }

    private Team findTeamByMember(Student member) {
        for (Team team : teams)
            for (Student mem : team.getMembers())
                if (mem.getUniqueId().equalsIgnoreCase(member.getUniqueId()))
                    return team;

        return null;
    }
}
