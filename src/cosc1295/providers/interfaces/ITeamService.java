package cosc1295.providers.interfaces;

import cosc1295.src.models.Team;

import java.util.List;

public interface ITeamService {

    List<Team> readAllTeamsFromFile();

    boolean SaveNewTeam(Team newTeam);

    boolean updateTeam(Team newTeam);
}
