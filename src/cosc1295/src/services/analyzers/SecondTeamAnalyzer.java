package cosc1295.src.services.analyzers;

import cosc1295.src.models.Student;
import cosc1295.src.models.Team;
import cosc1295.src.models.TeamFitness;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

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
