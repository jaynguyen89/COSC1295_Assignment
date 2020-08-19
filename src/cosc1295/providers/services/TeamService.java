package cosc1295.providers.services;

import cosc1295.providers.bases.TextFileServiceBase;
import cosc1295.providers.interfaces.ITeamService;
import cosc1295.src.models.Project;
import cosc1295.src.models.Student;
import cosc1295.src.models.Team;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.DATA_TYPES;

import java.util.ArrayList;
import java.util.List;

public class TeamService extends TextFileServiceBase implements ITeamService {

    @Override
    public List<Team> readAllTeamsFromFile() {
        List<String> rawTeamData = readAllDataFromFile(DATA_TYPES.PROJECT_TEAM);

        if (rawTeamData == null) return null;
        if (rawTeamData.isEmpty()) return new ArrayList<>();

        List<Team> teams = new ArrayList<>();
        try {
            for (String rawTeam : rawTeamData) {
                String[] teamTokens = rawTeam.split(SharedConstants.TEXT_DELIMITER);
                Team team = new Team();

                team.setId(Integer.parseInt(teamTokens[0]));
                team.setFitnessMetrics(Double.parseDouble(teamTokens[2]));

                Project teamProject = new Project();
                teamProject.setId(Integer.parseInt(teamTokens[1]));
                team.setProject(teamProject);

                List<Student> members = new ArrayList<>();
                for (int i = 3; i < 7; i++)
                    try {
                        Student member = new Student();
                        member.setUniqueId(teamTokens[i]);

                        members.add(member);
                    } catch (IndexOutOfBoundsException ex) {
                        break;
                    }

                team.setMembers(members);
                teams.add(team);
            }
        } catch (IndexOutOfBoundsException | NumberFormatException ex) {
            return null;
        }

        return teams;
    }
}
