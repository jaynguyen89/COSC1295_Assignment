package cosc1295.providers.services;

import cosc1295.providers.bases.TextFileServiceBase;
import cosc1295.providers.interfaces.IProjectService;
import cosc1295.src.models.Project;
import helpers.commons.SharedEnums.DATA_TYPES;

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
}
