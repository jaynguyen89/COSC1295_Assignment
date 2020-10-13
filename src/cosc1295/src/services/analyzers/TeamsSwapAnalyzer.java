package cosc1295.src.services.analyzers;

import cosc1295.src.models.*;
import javafx.util.Pair;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

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
