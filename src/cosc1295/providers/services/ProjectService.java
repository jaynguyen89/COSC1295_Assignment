package cosc1295.providers.services;

import cosc1295.providers.interfaces.IProjectService;
import cosc1295.src.models.Project;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums;

public class ProjectService extends FileServiceBase implements IProjectService {

    @Override
    public boolean saveNewProject(Project newProject) {
        String normalizedProject = newProject.stringify();
        return writeToFile(normalizedProject, SharedEnums.DATA_TYPES.PROJECT);
    }
}
