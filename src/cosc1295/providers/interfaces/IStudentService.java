package cosc1295.providers.interfaces;

import cosc1295.src.models.Preference;
import cosc1295.src.models.Student;

import java.util.List;

public interface IStudentService {
    
    List<Student> readAllStudentsFromFile();

    Boolean updateStudentPersonality(Student student);

    Boolean saveStudentPreferences(Preference preference);

    List<Preference> readAllStudentPreferencesFromFile();
}
