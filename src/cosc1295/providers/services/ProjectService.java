package cosc1295.providers.services;

import cosc1295.providers.bases.TextFileServiceBase;
import cosc1295.providers.interfaces.IProjectService;
import cosc1295.src.models.Project;
import cosc1295.src.models.ProjectOwner;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.SKILLS;
import helpers.commons.SharedEnums.RANKINGS;
import helpers.commons.SharedEnums.DATA_TYPES;
import helpers.utilities.Helpers;

import javafx.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * For Dependency Injection
 */
public class ProjectService extends TextFileServiceBase implements IProjectService {

    @Override
    public boolean saveNewProject(Project newProject) {
        int newInstanceId = getNextInstanceIdForNewEntry(DATA_TYPES.PROJECT);
        if (newInstanceId == -1) return false;

        newProject.setId(newInstanceId);
        String normalizedProject = newProject.stringify();

        return writeToFile(normalizedProject, DATA_TYPES.PROJECT);
    }

    @Override
    public boolean isUniqueIdDuplicated(String uniqueId) {
        return isRedundantUniqueId(uniqueId, DATA_TYPES.PROJECT);
    }

    @Override
    public List<Project> readAllProjectsFromFile() {
        List<String> rawProjectData = readEntireRawDataFromFile(DATA_TYPES.PROJECT);

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
                    Pair<SKILLS, RANKINGS> skPair = Helpers.parseSkillRankingToken(projectTokens[i]);
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
}
