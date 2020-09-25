package cosc1295.providers.services;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import cosc1295.providers.bases.DatabaseContext;
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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TeamService extends TextFileServiceBase implements ITeamService {
    private static final Logger logger = Logger.getLogger(DatabaseContext.class.getName());

    private final DatabaseContext context;

    public TeamService() {
        context = DatabaseContext.getInstance();
    }

    /**
     * Reads all Teams from file or database according to DATA_SOURCE.
     * @return List<Team>
     */
    @Override
    public List<Team> readAllTeamsFromFile() {
        List<String> rawTeamData;
        if (SharedConstants.DATA_SOURCE.equals(TextFileServiceBase.class.getSimpleName()))
            rawTeamData = readAllDataFromFile(DATA_TYPES.PROJECT_TEAM);
        else
            rawTeamData = context.retrieveCompositeDataForType(Team.class);

        if (rawTeamData == null) return null;
        if (rawTeamData.isEmpty()) return new ArrayList<>();

        List<Team> teams = new ArrayList<>();
        try {
            for (String rawTeam : rawTeamData) {
                String[] teamTokens = rawTeam.split(SharedConstants.TEXT_DELIMITER);
                Team team = new Team();
                team.setId(Integer.parseInt(teamTokens[0]));

                if (Integer.parseInt(teamTokens[2].trim()) != 0) {
                    TeamFitness teamFitness = retrieveTeamFitnessMetricsFromFile(teamTokens[2]);
                    if (teamFitness == null) return null;
                    team.setFitnessMetrics(teamFitness);
                }

                Project teamProject = new Project();
                teamProject.setId(Integer.parseInt(teamTokens[1]));

                String rawProject = SharedConstants.DATA_SOURCE.equals(TextFileServiceBase.class.getSimpleName())
                                    ? getEntryFromFileById(teamTokens[1], DATA_TYPES.PROJECT)
                                    : retrieveRawProjectFromDatabase(teamTokens[1]);

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
            if (SharedConstants.DEV) logger.log(Level.SEVERE, "TeamService.readAllTeamsFromFile : " + ex.getMessage());
            return null;
        }

        return teams;
    }

    /**
     * Reads a Fitness Metrics for a Team by Team ID from file or database according to DATA_SOURCE.
     * @param fitnessMetricsId String
     * @return TeamFitness
     */
    private TeamFitness retrieveTeamFitnessMetricsFromFile(String fitnessMetricsId) {
        String rawFitnessMetrics;
        if (SharedConstants.DATA_SOURCE.equals(TextFileServiceBase.class.getSimpleName()))
            rawFitnessMetrics = getEntryFromFileById(fitnessMetricsId, DATA_TYPES.FITNESS_METRICS);
        else
            rawFitnessMetrics = context.getRawEntryForType(TeamFitness.class, fitnessMetricsId, true);

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
            if (SharedConstants.DEV) logger.log(Level.SEVERE, "TeamService.retrieveTeamFitnessMetricsFromFile : " + ex.getMessage());
            return null;
        }

        return fitnessMetrics;
    }

    /**
     * Gets a delimeterized string as a Project for a Team from database.
     * @param projectId String
     * @return String
     */
    private String retrieveRawProjectFromDatabase(String projectId) {
        String query = "SELECT `unique_id`, `project_title` FROM `projects` WHERE id = " + projectId;

        List<HashMap<String, String>> result = context.executeDataRetrievalQuery(query);
        if (result == null) return SharedConstants.EMPTY_STRING;

        HashMap<String, String> projectData = result.get(0);
        return projectId + SharedConstants.TEXT_DELIMITER +
               projectData.get("unique_id") + SharedConstants.TEXT_DELIMITER +
               projectData.get("project_title");
    }

    /**
     * Save a Team into file or database according to DATA_SOURCE.
     * Returns -1 on exception, or the ID of newly created Team.
     * @param newTeam Team
     * @return int
     */
    @Override
    public int SaveNewTeam(@NotNull Team newTeam) {
        return SharedConstants.DATA_SOURCE.equals(TextFileServiceBase.class.getSimpleName())
                ? saveTeamToTextFile(newTeam) : saveTeamToDatabase(newTeam);
    }

    private int saveTeamToTextFile(Team team) {
        int newFitnessInstanceId = getNextEntryIdForNewEntry(DATA_TYPES.FITNESS_METRICS);
        if (newFitnessInstanceId == -1) return -1;

        boolean fitnessSaved;
        if (team.getFitnessMetrics() != null) {
            team.getFitnessMetrics().setId(newFitnessInstanceId);
            fitnessSaved = saveEntryToFile(team.getFitnessMetrics().stringify(), DATA_TYPES.FITNESS_METRICS);
        }
        else fitnessSaved = true;

        if (fitnessSaved) {
            int newTeamInstanceId = getNextEntryIdForNewEntry(DATA_TYPES.PROJECT_TEAM);
            if (newTeamInstanceId == -1) return -1;

            team.setId(newTeamInstanceId);
            String normalizedTeam = team.stringify();

            if (saveEntryToFile(normalizedTeam, DATA_TYPES.PROJECT_TEAM))
                return newTeamInstanceId;
        }

        return -1;
    }

    private int saveTeamToDatabase(Team team) {
        boolean error;
        try {
            context.toggleAutoCommit(false);
            String query = "INSERT INTO `teams` (`project_id`) VALUES (" + team.getProject().getId() + ");";

            PreparedStatement statement = context.createStatement(query, SharedConstants.DB_INSERT);
            if (statement == null) return -1;

            int teamId = context.executeDataInsertionQuery(statement);
            error = teamId <= 0;

            if (!error) {
                error = !saveTeamMembersToDatabase(teamId, team.getMembers());
                if (!error) error = !saveFitnessMetricsToDatabase(teamId, team.getFitnessMetrics());
            }

            if (error) {
                context.revertChanges();
                context.toggleAutoCommit(true);
                return -1;
            }

            context.saveChanges();
            context.toggleAutoCommit(true);
            return teamId;
        } catch (SQLException ex) {
            if (SharedConstants.DEV) logger.log(Level.SEVERE, "TeamService.saveTeamToDatabase : " + ex.getMessage());
            return -1;
        }
    }

    //true for success, false for exception
    private boolean saveTeamMembersToDatabase(int teamId, List<Student> members) {
        try {
            for (Student member : members) {
                String query = "SELECT `id` FROM `students` WHERE `unique_id` = '" + member.getUniqueId() + "';";

                List<HashMap<String, String>> result = context.executeDataRetrievalQuery(query);
                if (result == null) throw new SQLException();

                int memberId = Integer.parseInt(result.get(0).get("id"));
                query = "INSERT INTO `team_members` (`team_id`, `student_id`) VALUES (?, ?);";

                PreparedStatement statement = context.createStatement(query, SharedConstants.DB_INSERT);
                if (statement == null) throw new SQLException();

                statement.setInt(1, teamId);
                statement.setInt(2, memberId);

                int inserted = context.executeDataInsertionQuery(statement);
                if (inserted <= 0) throw new SQLException();
            }
        } catch (SQLException | NumberFormatException ex) {
            if (SharedConstants.DEV) logger.log(Level.SEVERE, "TeamService.saveTeamMembersToDatabase : " + ex.getMessage());
            return false;
        }

        return true;
    }

    //true for success, false for exception
    private boolean saveFitnessMetricsToDatabase(int teamId, @Nullable TeamFitness metrics) {
        if (metrics == null) return true;

        String query = "INSERT INTO `fitness_metrics` (`team_id`, `avg_skill_competency`, `competency_by_skill`," +
                "`avg_preference_satisfaction`, `avg_skill_shortfall`,`shortfall_by_project`) VALUES (?, ?, ?, ?, ?, ?);";

        PreparedStatement statement = context.createStatement(query, SharedConstants.DB_INSERT);
        if (statement == null) return false;

        try {
            statement.setInt(1, teamId);
            statement.setDouble(2, metrics.getAverageTeamSkillCompetency());
            statement.setDouble(5, metrics.getAverageSkillShortfall());
            statement.setString(4,
                metrics.getPreferenceSatisfaction().getKey() + SharedConstants.TEXT_DELIMITER +
                    metrics.getPreferenceSatisfaction().getValue().getKey() + SharedConstants.TEXT_DELIMITER +
                    metrics.getPreferenceSatisfaction().getValue().getValue() + SharedConstants.TEXT_DELIMITER
            );

            StringBuilder competencyBySkill = new StringBuilder(SharedConstants.EMPTY_STRING);
            for (Map.Entry<SKILLS, Double> entry : metrics.getTeamCompetency().entrySet())
                competencyBySkill.append(entry.getKey()).append(SharedConstants.TEXT_DELIMITER).append(entry.getValue()).append(SharedConstants.TEXT_DELIMITER);

            StringBuilder shortfallByProject = new StringBuilder(SharedConstants.EMPTY_STRING);
            for (Map.Entry<String, Double> entry : metrics.getSkillShortFall().entrySet())
                shortfallByProject.append(entry.getKey()).append(SharedConstants.TEXT_DELIMITER).append(entry.getValue()).append(SharedConstants.TEXT_DELIMITER);

            statement.setString(3, competencyBySkill.toString());
            statement.setString(6, shortfallByProject.toString());

            return context.executeDataInsertionQuery(statement) > 0;
        } catch (SQLException ex) {
            if (SharedConstants.DEV) logger.log(Level.SEVERE, "TeamService.saveFitnessMetricsToDatabase : " + ex.getMessage());
            return false;
        }
    }

    /**
     * Updates a Team to its corresponding instance in file or database according to DATA_SOURCE.
     * Returns true on update success, otherwise false.
     * @param newTeam Team
     * @return boolean
     */
    @Override
    public boolean updateTeam(@NotNull Team newTeam) {
        return SharedConstants.DATA_SOURCE.equals(TextFileServiceBase.class.getSimpleName())
                ? updateTeamToTextFile(newTeam) : updateTeamToDatabase(newTeam);
    }

    private boolean updateTeamToTextFile(Team team) {
        boolean fitnessMetricsSaved = true;
        if (team.getFitnessMetrics() != null) {
            if (team.getFitnessMetrics().getId() == 0) {
                int newMetricsId = getNextEntryIdForNewEntry(DATA_TYPES.FITNESS_METRICS);
                team.getFitnessMetrics().setId(newMetricsId);

                fitnessMetricsSaved = saveEntryToFile(team.getFitnessMetrics().stringify(), DATA_TYPES.FITNESS_METRICS);
            } else {
                fitnessMetricsSaved = updateEntryToFileById(
                    team.getFitnessMetrics().stringify(), team.getFitnessMetrics().getId(), DATA_TYPES.FITNESS_METRICS
                );
            }
        }

        if (fitnessMetricsSaved)
            return updateEntryToFileById(team.stringify(), team.getId(), DATA_TYPES.PROJECT_TEAM);

        return false;
    }

    private boolean updateTeamToDatabase(Team team) {
        try {
            context.toggleAutoCommit(false);

            Boolean updateResult = updateTeamProjectInDatabase(team);
            if (!updateResult) throw new SQLException();

            updateResult = removeTeamMembersInDatabase(team.getId());
            if (!updateResult) throw new SQLException();

            updateResult = saveTeamMembersToDatabase(team.getId(), team.getMembers());
            if (!updateResult) throw new SQLException();

            if (team.getFitnessMetrics() == null || team.getMembers().size() != SharedConstants.GROUP_LIMIT) {
                updateResult = removeTeamFitness(team.getId());
                if (updateResult == null) throw new SQLException();
            }
            else {
                updateResult = removeTeamFitness(team.getId());
                if (updateResult == null) throw new SQLException();

                updateResult = saveFitnessMetricsToDatabase(team.getId(), team.getFitnessMetrics());
                if (!updateResult) throw new SQLException();
            }

            context.saveChanges();
            context.toggleAutoCommit(true);
            return true;
        } catch (SQLException ex) {
            if (SharedConstants.DEV) logger.log(Level.SEVERE, "TeamService.updateTeamToDatabase : " + ex.getMessage());
            try {
                context.revertChanges();
                context.toggleAutoCommit(true);
            } catch (SQLException e) {
                if (SharedConstants.DEV) logger.log(Level.SEVERE, "TeamService.updateTeamToDatabase.catch : " + ex.getMessage());
                return false;
            }
            return false;
        }
    }

    private boolean updateTeamProjectInDatabase(Team team) {
        String query = "UPDATE `teams` SET `project_id` = ? WHERE id = ?;";

        PreparedStatement statement = context.createStatement(query, SharedConstants.DB_UPDATE);
        if (statement == null) return false;

        try {
            statement.setInt(1, team.getProject().getId());
            statement.setInt(2, team.getId());

            Boolean result = context.executeDataModifierQuery(statement);
            if (result == null) return false;
        } catch (SQLException ex) {
            if (SharedConstants.DEV) logger.log(Level.SEVERE, "TeamService.updateTeamProjectInDatabase : " + ex.getMessage());
            return false;
        }

        return true;
    }

    private boolean removeTeamMembersInDatabase(int teamId) {
        String query = "DELETE FROM `team_members` WHERE `team_id` = " + teamId;

        PreparedStatement statement = context.createStatement(query, SharedConstants.DB_DELETE);
        if (statement == null) return false;

        Boolean result = context.executeDataModifierQuery(statement);
        if (result == null) return false;

        return result;
    }

    /**
     * Delete a TeamFitness for a Team from file or database according to DATA_SOURCE.
     * Returns null on exception, false for update failed, otherwise false for update success.
     * @param id int
     * @return Boolean
     */
    @Override
    public Boolean removeTeamFitness(int id) {
        if (SharedConstants.DATA_SOURCE.equals(TextFileServiceBase.class.getSimpleName()))
            return removeEntryFromFileById(id + "", DATA_TYPES.FITNESS_METRICS);

        String query = "DELETE FROM `fitness_metrics` WHERE `team_id` = " + id;

        PreparedStatement statement = context.createStatement(query, SharedConstants.DB_DELETE);
        if (statement == null) return null;

        return context.executeDataModifierQuery(statement);
    }

    /**
     * Delete a Team from file or database according to DATA_SOURCE.
     * Returns null on exception, false for update failed, otherwise false for update success.
     * @param team Team
     * @return Boolean
     */
    @Override
    public Boolean deleteTeam(Team team) {
        if (SharedConstants.DATA_SOURCE.equals(TextFileServiceBase.class.getSimpleName()))
            return removeEntryFromFileById(team.getId() + "", DATA_TYPES.PROJECT_TEAM);

        String query = "DELETE FROM `teams` WHERE id = " + team.getId();

        PreparedStatement statement = context.createStatement(query, SharedConstants.DB_DELETE);
        if (statement == null) return null;

        return context.executeDataModifierQuery(statement);
    }
}
