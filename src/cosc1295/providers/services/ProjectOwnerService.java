package cosc1295.providers.services;

import cosc1295.providers.interfaces.IProjectOwnerService;
import cosc1295.src.models.ProjectOwner;
import helpers.commons.SharedEnums;

public class ProjectOwnerService extends FileServiceBase implements IProjectOwnerService {

    @Override
    public boolean saveNewProjectOwner(ProjectOwner projectOwner) {
        int newInstanceId = getNextInstanceIdForNewEntry(SharedEnums.DATA_TYPES.PROJECT_OWNER);
        if (newInstanceId == -1) return false;

        projectOwner.setId(newInstanceId);
        String normalizedProjectOwner = projectOwner.stringify();

        return writeToFile(normalizedProjectOwner, SharedEnums.DATA_TYPES.PROJECT_OWNER);
    }
}
