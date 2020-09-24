package cosc1295.providers.services;

import cosc1295.providers.bases.DatabaseContext;
import cosc1295.providers.bases.TextFileServiceBase;
import cosc1295.providers.interfaces.IProjectService;
import cosc1295.src.models.Project;
import cosc1295.src.models.ProjectOwner;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.SKILLS;
import helpers.commons.SharedEnums.RANKINGS;
import helpers.commons.SharedEnums.DATA_TYPES;
import helpers.utilities.LogicalAssistant;

import javafx.util.Pair;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * For Dependency Injection
 */
public class ProjectService extends TextFileServiceBase implements IProjectService {

    private final DatabaseContext context;

    public ProjectService() {
        context = DatabaseContext.getInstance();
    }

    @Override
    public boolean saveNewProject(Project newProject) {
        if (SharedConstants.DATA_SOURCE.equals(TextFileServiceBase.class.getSimpleName()))
            return saveEntryToTextFile(newProject);

        return saveEntryToDatabase(newProject);
    }

    @Override
    public boolean isUniqueIdDuplicated(String uniqueId) {
        return SharedConstants.DATA_SOURCE.equals(TextFileServiceBase.class.getSimpleName())
                ? isRedundantUniqueId(uniqueId, DATA_TYPES.PROJECT)
                : context.isRedundantUniqueId(Project.class, uniqueId);
    }

    @Override
    public List<Project> readAllProjectsFromFile() {
        List<String> rawProjectData;
        if (SharedConstants.DATA_SOURCE.equals(TextFileServiceBase.class.getSimpleName()))
            rawProjectData = readAllDataFromFile(DATA_TYPES.PROJECT);
        else
            rawProjectData = context.retrieveCompositeDataForType(Project.class);

        if (rawProjectData == null) return null;
        if (rawProjectData.isEmpty()) return new ArrayList<>();

        List<Project> projects = new ArrayList<>();
        try {
            for (String rawProject : rawProjectData) {
                if (rawProject.isEmpty()) continue;

                Project project = new Project();
                String[] projectTokens = rawProject.split(SharedConstants.TEXT_DELIMITER);

                project.setId(Integer.parseInt(projectTokens[0].trim()));
                project.setUniqueId(projectTokens[1]);
                project.setProjectTitle(projectTokens[2]);
                project.setBriefDescription(projectTokens[3]);

                ProjectOwner projectOwner = new ProjectOwner();
                projectOwner.setId(Integer.parseInt(projectTokens[4].trim()));
                project.setProjectOwner(projectOwner);

                HashMap<SKILLS, RANKINGS> skillRanking = new HashMap<>();
                for (int i = 5; i <= 8; i++) {
                    Pair<SKILLS, RANKINGS> skPair = LogicalAssistant.parseSkillRankingToken(projectTokens[i]);
                    skillRanking.put(skPair.getKey(), skPair.getValue());
                }

                project.setSkillRanking(skillRanking);
                projects.add(project);
            }
        } catch (IndexOutOfBoundsException | NumberFormatException ex) {
            return null;
        }

        return projects;
    }

    private boolean saveEntryToTextFile(Project project) {
        int newInstanceId = getNextEntryIdForNewEntry(DATA_TYPES.PROJECT);
        if (newInstanceId == -1) return false;

        project.setId(newInstanceId);
        String normalizedProject = project.stringify();

        return saveEntryToFile(normalizedProject, DATA_TYPES.PROJECT);
    }

    private boolean saveEntryToDatabase(Project project) {
        String query = "INSERT INTO `projects` (`project_owner_id`, `unique_id`, `project_title`, `brief_description`) VALUES (?, ?, ?, ?);";

        PreparedStatement statement = context.createStatement(query, SharedConstants.DB_INSERT);
        if (statement == null) return false;

        try {
            statement.setInt(1, project.getProjectOwner().getId());
            statement.setString(2, project.getUniqueId());
            statement.setString(3, project.getProjectTitle());
            statement.setString(4, project.getBriefDescription());

            context.toggleAutoCommit(false);
            int projectId = context.executeDataInsertionQuery(statement);

            boolean error = projectId <= 0;

            if (!error) {
                String sql = "INSERT INTO `rankings` (`subject_id`, `subject_type`) VALUES (?, 'PROJECT');";

                statement = context.createStatement(sql, SharedConstants.DB_INSERT);
                statement.setInt(1, projectId);

                int rankingId = context.executeDataInsertionQuery(statement);
                error = rankingId <= 0;

                if (!error) for (Map.Entry<SKILLS, RANKINGS> entry : project.getSkillRanking().entrySet()) {
                    sql = "INSERT INTO `skill_rankings` (`ranking_id`, `skill`, `ranking`) VALUES (?, ?, ?);";

                    statement = context.createStatement(sql, SharedConstants.DB_INSERT);
                    statement.setInt(1, rankingId);
                    statement.setString(2, entry.getKey().name());
                    statement.setInt(3, entry.getValue().getValue() + 1);

                    int result = context.executeDataInsertionQuery(statement);
                    error = result <= 0;

                    if (error) break;
                }

                if (error) {
                    context.revertChanges();
                    context.toggleAutoCommit(true);
                    return false;
                }

                context.saveChanges();
                context.toggleAutoCommit(true);
                return true;
            }

            context.toggleAutoCommit(true);
            context.revertChanges();
            return false;
        } catch (SQLException ex) {
            return false;
        }
    }
}
