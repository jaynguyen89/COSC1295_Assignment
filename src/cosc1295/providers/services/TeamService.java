package cosc1295.providers.services;

import com.sun.istack.internal.NotNull;
import cosc1295.providers.bases.TextFileServiceBase;
import cosc1295.providers.interfaces.ITeamService;
import cosc1295.src.models.Project;
import cosc1295.src.models.Student;
import cosc1295.src.models.Team;
import cosc1295.src.models.TeamFitness;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.SKILLS;
import helpers.commons.SharedEnums.DATA_TYPES;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
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

                TeamFitness teamFitness = retrieveTeamFitnessMetricsFromFile(teamTokens[2]);
                if (teamFitness == null) return null;
                if (teamFitness.getId() != 0) team.setFitnessMetrics(teamFitness);

                Project teamProject = new Project();
                teamProject.setId(Integer.parseInt(teamTokens[1]));

                String rawProject = getEntryFromFileById(teamTokens[1], DATA_TYPES.PROJECT);
                String[] projectTokens = rawProject.split(SharedConstants.TEXT_DELIMITER);

                teamProject.setUniqueId(projectTokens[1]);
                teamProject.setProjectTitle(projectTokens[2]);
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

    private TeamFitness retrieveTeamFitnessMetricsFromFile(String fitnessMetricsId) {
        String rawFitnessMetrics = getEntryFromFileById(fitnessMetricsId, DATA_TYPES.FITNESS_METRICS);
        TeamFitness fitnessMetrics = new TeamFitness();

        if (rawFitnessMetrics == null) return null;
        if (rawFitnessMetrics.length() == 0) {
            fitnessMetrics.setId(0);
            return fitnessMetrics;
        }

        String[] fitnessTokens = rawFitnessMetrics.split(SharedConstants.TEXT_DELIMITER);
        try {
            fitnessMetrics.setId(Integer.parseInt(fitnessTokens[0]));
            fitnessMetrics.setAverageTeamSkillCompetency(Double.parseDouble(fitnessTokens[1]));

            HashMap<SKILLS, Double> skillCompetencies = new HashMap<>();
            for (int i = 2; i < 10; i++)
                if (i % 2 != 0) {
                    SKILLS skill = fitnessTokens[i - 1].equals(SKILLS.A.name()) ? SKILLS.A : (
                            fitnessTokens[i - 1].equals(SKILLS.P.name()) ? SKILLS.P : (
                                    fitnessTokens[i - 1].equals(SKILLS.N.name()) ? SKILLS.N : SKILLS.W
                            ));

                    double score = Double.parseDouble(fitnessTokens[i]);
                    skillCompetencies.put(skill, score);
                }

            fitnessMetrics.setTeamCompetency(skillCompetencies);

            Pair<Double, Pair<Double, Double>> satisfactions = new Pair<>(
                    Double.parseDouble(fitnessTokens[10]),
                    new Pair<>(
                            Double.parseDouble(fitnessTokens[11]),
                            Double.parseDouble(fitnessTokens[12])
                    ));

            fitnessMetrics.setPreferenceSatisfaction(satisfactions);
            fitnessMetrics.setAverageSkillShortfall(Double.parseDouble(fitnessTokens[13]));

            HashMap<String, Double> skillShortfall = new HashMap<>();
            for (int i = 14; i < fitnessTokens.length; i++)
                if (i % 2 != 0) {
                    String projectUniqueId = fitnessTokens[i - 1];
                    double shortFallScore = Double.parseDouble(fitnessTokens[i]);

                    skillShortfall.put(projectUniqueId, shortFallScore);
                }

            fitnessMetrics.setSkillShortFall(skillShortfall);
        } catch (IndexOutOfBoundsException | NumberFormatException ex) {
            return null;
        }

        return fitnessMetrics;
    }

    @Override
    public boolean SaveNewTeam(@NotNull Team newTeam) {
        int newFitnessInstanceId = getNextEntryIdForNewEntry(DATA_TYPES.FITNESS_METRICS);
        if (newFitnessInstanceId == -1) return false;

        boolean fitnessSaved = false;
        if (newTeam.getFitnessMetrics() != null) {
            newTeam.getFitnessMetrics().setId(newFitnessInstanceId);
            fitnessSaved = saveEntryToFile(newTeam.getFitnessMetrics().stringify(), DATA_TYPES.FITNESS_METRICS);
        }
        else fitnessSaved = true;

        if (fitnessSaved) {
            int newTeamInstanceId = getNextEntryIdForNewEntry(DATA_TYPES.PROJECT_TEAM);
            if (newTeamInstanceId == -1) return false;

            newTeam.setId(newTeamInstanceId);
            String normalizedTeam = newTeam.stringify();

            return saveEntryToFile(normalizedTeam, DATA_TYPES.PROJECT_TEAM);
        }

        return false;
    }

    @Override
    public boolean updateTeam(@NotNull Team newTeam) {
        if (updateEntryToFileById(
            newTeam.getFitnessMetrics().stringify(),
            newTeam.getFitnessMetrics().getId(),
            DATA_TYPES.FITNESS_METRICS)
        )
            return updateEntryToFileById(newTeam.stringify(), newTeam.getId(), DATA_TYPES.PROJECT_TEAM);

        return false;
    }
}
