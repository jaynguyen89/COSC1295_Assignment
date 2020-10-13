package cosc1295.src.services.analyzers;

import cosc1295.src.models.*;
import javafx.util.Pair;
import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * Used for suggesting a swap when user have selected no Teams.
 * return data of type Pair<Pair<Team, Team>, Pair<Student, Student>> with Team-Student in respective order.
 * @param <T>
 */
public class TeamsSwapAnalyzer<T> extends SuperAnalyzer implements Callable<T> {

    public TeamsSwapAnalyzer() {
        super();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T call() {
        HashMap<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>> metricsData = calculateMetrics();

        Pair<Student, Student> suggestion = produceSwapSuggestion(metricsData);
        Pair<Team, Team> teamPair = findTeamsByMembers(suggestion);

        return (T) new Pair<>(teamPair, suggestion);
    }

    /**
     * Calculates the Fitness Metrics for each possible pair combination of Students accross all Teams.
     * `Possible` means each pair is checked against all team requirements.
     * Returns HashMap of Pair<Student, Student> being the possible swap,
     * and Pair<TeamFitness, TeamFitness> being the Fitness Metrics of 2 Teams.
     * @return HashMap<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>>
     */
    private HashMap<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>> calculateMetrics() {
        //First-Second respectively
        HashMap<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>> metricsData = new HashMap<>();

        for (int i = 0; i < teams.size(); i++) {
            Team first = teams.get(i);

            for (int j = i + 1; j < teams.size(); j++) {
                Team second = teams.get(j);

                for (Student firstMember : first.getMembers())
                    for (Student secondMember : second.getMembers()) {
                        Pair<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>> metrics = produceMetricsData(
                                first, second, firstMember, secondMember
                        );
                        if (metrics == null) continue;

                        metricsData.put(metrics.getKey(), metrics.getValue());
                    }
            }

            if (i == teams.size() - 2) break;
        }

        return metricsData;
    }

    private Pair<Team, Team> findTeamsByMembers(Pair<Student, Student> members) {
        Team first = null;
        Team second = null;

        for (Team team : teams) {
            for (Student member : team.getMembers()) {
                if (member.getUniqueId().equalsIgnoreCase(members.getKey().getUniqueId())) {
                    first = team;
                    break;
                }

                if (member.getUniqueId().equalsIgnoreCase(members.getValue().getUniqueId())) {
                    second = team;
                    break;
                }
            }

            if (first != null && second != null) break;
        }

        return new Pair<>(first, second);
    }
}
