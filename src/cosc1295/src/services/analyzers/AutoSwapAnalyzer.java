package cosc1295.src.services.analyzers;

import cosc1295.src.controllers.ControllerBase;
import cosc1295.src.models.Student;
import cosc1295.src.models.Team;
import cosc1295.src.models.TeamFitness;

import javafx.util.Pair;
import java.util.HashMap;
import java.util.concurrent.Callable;

public class AutoSwapAnalyzer<T> extends SuperAnalyzer implements Callable<T> {

    public AutoSwapAnalyzer() {
        super();
    }

    //Suggestion responds in format Pair<Pair<Team, Student>, Pair<Team, Student>>
    @SuppressWarnings("unchecked")
    @Override
    public T call() {
        HashMap<Pair<Student, Student>, Pair<TeamFitness, TeamFitness>> metricsData = calculateSwapMetrics();

        Pair<Student, Student> suggestion = produceSwapSuggestion(metricsData);
        if (suggestion == null) return null;

        Pair<Team, Team> teamPair = findTeamsByMembers(suggestion);
        return (T) new Pair<>(
            new Pair<>(teamPair.getKey(), suggestion.getKey()),
            new Pair<>(teamPair.getValue(), suggestion.getValue())
        );
    }
}
