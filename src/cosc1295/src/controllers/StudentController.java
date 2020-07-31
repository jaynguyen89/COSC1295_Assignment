package cosc1295.src.controllers;

import cosc1295.providers.interfaces.IStudentService;
import cosc1295.providers.services.StudentService;
import cosc1295.src.models.Flash;
import cosc1295.src.models.Student;
import cosc1295.src.views.StudentView;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.FLASH_TYPES;

import java.util.List;

public class StudentController extends ControllerBase {

    private final StudentView studentView;
    private final IStudentService studentService;

    public StudentController() {
        studentView = new StudentView();
        studentService = new StudentService();
    }

    public Boolean executeStudentPersonalityCapturingTask() {
        List<Student> allStudents = studentService.readAllStudentsFromFile();
        if (allStudents == null) {
            flasher.flash(new Flash(
                "An error occurred while retrieving Students data from file. Please try again.",
                FLASH_TYPES.ERROR
            ));

            return false;
        }

        Student student = studentView.getStudentFromListToUpdate(allStudents);
        if (student == null) return null;

        student = studentView.captureStudentPersonality(student, allStudents);
        if (student == null) return null;

        if (student.getConflicters().size() == SharedConstants.MAX_CONFLICTERS)
            studentView.displayConflicterSkippingInformationFor(student.getUniqueId());
        else {
            student = studentView.captureStudentConflicters(student, allStudents);
            if (student == null) return null;
        }

        return studentService.updateStudentPersonality(student);
    }

    public void displayPersonalityCapturingTaskResult(boolean result) {
        studentView.printTaskResult(result);
    }
}
