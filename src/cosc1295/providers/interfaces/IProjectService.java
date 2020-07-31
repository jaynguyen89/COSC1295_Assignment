package cosc1295.providers.interfaces;

import cosc1295.src.models.Project;

import java.util.List;

public interface IProjectService {

    boolean saveNewProject(Project newProject);

    boolean isUniqueIdDuplicated(String uniqueId);

    List<Project> readAllProjectsFromFile();
}
