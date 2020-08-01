package cosc1295.src.controllers;

import cosc1295.providers.interfaces.IProjectService;
import cosc1295.providers.interfaces.IStudentService;
import cosc1295.providers.services.ProjectService;
import cosc1295.providers.services.StudentService;
import cosc1295.src.models.Flash;
import cosc1295.src.models.Preference;
import cosc1295.src.models.Project;
import cosc1295.src.models.Student;
import cosc1295.src.views.StudentView;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.FLASH_TYPES;

import java.util.List;

public class StudentController extends ControllerBase {

    private final StudentView studentView;
    private final IStudentService studentService;
    private final IProjectService projectService;

    public StudentController() {
        studentView = new StudentView();
        studentService = new StudentService();
        projectService = new ProjectService();
    }

    public Boolean executeStudentPersonalityCapturingTask() {
        List<Student> allStudents = getAllStudents();
        if (allStudents == null) return false;

        Student student = studentView.getStudentFromListToUpdate(allStudents);
        if (student == null) return null;

        boolean shouldCapturePersonality = true;
        if (student.getPersonality() != null)
            shouldCapturePersonality = studentView.promptToCapturePersonality(student);

        if (shouldCapturePersonality) {
            student = studentView.captureStudentPersonality(student, allStudents);
            if (student == null) return null;
        }

        boolean shouldCaptureConflicters = studentView.promptToCaptureConflicters(student);
        if (!shouldCaptureConflicters && shouldCapturePersonality)
            return studentService.updateStudentPersonality(student);

        if (shouldCaptureConflicters &&
            student.getConflicters().size() == 0) {
            student = studentView.captureStudentConflicters(student, allStudents);
            if (student == null) return null;

            return studentService.updateStudentPersonality(student);
        }

        if (shouldCaptureConflicters &&
                student.getConflicters().size() != 0) {
            student = studentView.updateStudentConflicters(student, allStudents);
            if (student == null) return null;
        }

        if (shouldCapturePersonality || shouldCaptureConflicters)
            return studentService.updateStudentPersonality(student);

        return null;
    }

    public void displayStudentTaskResult(boolean result) {
        studentView.printTaskResult(result);
    }

    public Boolean executeStudentPreferenceCapturingTask() {
        List<Student> allStudents = getAllStudents();
        if (allStudents == null) return false;

        List<Project> allProjects = getAllProjects();
        if (allProjects == null) return false;

        Student student = studentView.getStudentFromListToUpdate(allStudents);
        if (student == null) return null;

        int preferenceCount = 0;
        Preference preference = new Preference();
        preference.setStudentUniqueId(student.getUniqueId());

        while (preferenceCount < SharedConstants.MAX_PREFERENCE) {
            ProjectController projectController = new ProjectController();
            Project project = projectController.getAProjectFromList(allProjects);
            if (project == null) return null;

            preference = studentView.captureStudentPreferences(student, project, preference);

            preferenceCount++;
            allProjects.remove(project);
        }

        return studentService.saveStudentPreferences(preference);
    }

    private List<Student> getAllStudents() {
        List<Student> allStudents = studentService.readAllStudentsFromFile();
        if (allStudents == null) {
            flasher.flash(new Flash(
                "An error occurred while retrieving Students data from file. Please try again.",
                FLASH_TYPES.ERROR
            ));

            return null;
        }

        return allStudents;
    }

    private List<Project> getAllProjects() {
        List<Project> allProjects = projectService.readAllProjectsFromFile();
        if (allProjects == null) {
            flasher.flash(new Flash(
                "An error occurred while retrieving Students data from file. Please try again.",
                FLASH_TYPES.ERROR
            ));

            return null;
        }

        return allProjects;
    }
}
