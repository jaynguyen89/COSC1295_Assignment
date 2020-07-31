package cosc1295.providers.interfaces;

import cosc1295.src.models.ProjectOwner;

import java.util.List;

public interface IProjectOwnerService {

    boolean saveNewProjectOwner(ProjectOwner projectOwner);

    List<ProjectOwner> readAllProjectOwnersFromFile();

    boolean isUniqueIdDuplicated(String uniqueId);
}
