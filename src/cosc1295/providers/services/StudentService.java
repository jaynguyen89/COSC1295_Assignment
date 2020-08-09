package cosc1295.providers.services;

import cosc1295.providers.bases.TextFileServiceBase;
import cosc1295.providers.interfaces.IStudentService;
import cosc1295.src.models.Preference;
import cosc1295.src.models.Student;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.SKILLS;
import helpers.commons.SharedEnums.RANKINGS;
import helpers.commons.SharedEnums.DATA_TYPES;
import helpers.commons.SharedEnums.PERSONALITIES;

import helpers.utilities.Helpers;
import javafx.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * For Dependency Injection
 */
public class StudentService extends TextFileServiceBase implements IStudentService {

    @Override
    public List<Student> readAllStudentsFromFile() {
        List<String> rawStudentData = readAllDataFromFile(DATA_TYPES.STUDENT);

        if (rawStudentData == null) return null;
        if (rawStudentData.isEmpty()) return new ArrayList<>();

        List<Student> students = new ArrayList<>();
        try {
            for (String rawStudent : rawStudentData) {
                String[] studentTokens = rawStudent.split(SharedConstants.TEXT_DELIMITER);
                Student student = new Student();

                student.setId(Integer.parseInt(studentTokens[0]));
                student.setUniqueId(studentTokens[1]);

                HashMap<SKILLS, RANKINGS> skillRanking = new HashMap<>();
                for (int i = 2; i <= 5; i++) {
                    Pair<SKILLS, RANKINGS> skPair = Helpers.parseSkillRankingToken(studentTokens[i]);
                    skillRanking.put(skPair.getKey(), skPair.getValue());
                }

                student.setSkillRanking(skillRanking);

                if (studentTokens.length > 6)
                    student.setPersonality(
                        studentTokens[6].equals(PERSONALITIES.A.name()) ? PERSONALITIES.A
                            : (studentTokens[6].equals(PERSONALITIES.B.name()) ? PERSONALITIES.B
                            : (studentTokens[6].equals(PERSONALITIES.C.name()) ? PERSONALITIES.C : PERSONALITIES.D))
                    );

                List<String> conflicters = new ArrayList<>();
                if (studentTokens.length > 7) conflicters.add(studentTokens[7].trim());
                if (studentTokens.length > 8) conflicters.add(studentTokens[8].trim());

                student.setConflicters(conflicters);
                students.add(student);
            }
        } catch (IndexOutOfBoundsException | NumberFormatException ex) {
            return null;
        }

        return students;
    }

    @Override
    public Boolean updateStudentPersonality(Student student) {
        String normalizedStudent = student.stringify();
        return updateEntryToFileById(normalizedStudent, student.getId(), DATA_TYPES.STUDENT);
    }

    @Override
    public Boolean saveStudentPreferences(Preference preference) {
        String normalizedPreference = preference.stringify();
        return saveEntryToFile(normalizedPreference, DATA_TYPES.PREFERENCE);
    }

    @Override
    public List<Preference> readAllStudentPreferencesFromFile() {
        List<String> rawPreferences = readAllDataFromFile(DATA_TYPES.PREFERENCE);

        if (rawPreferences == null) return null;
        if (rawPreferences.isEmpty()) return new ArrayList<>();

        List<Preference> preferences = new ArrayList<>();
        try {
            for (String rawPreference : rawPreferences) {
                if (rawPreference.isEmpty()) continue;

                String[] preferenceTokens = rawPreference.split(SharedConstants.TEXT_DELIMITER);
                Preference preference = new Preference();

                preference.setStudentUniqueId(preferenceTokens[0]);

                HashMap<String, Integer> projectPreferences = new HashMap<>();
                projectPreferences.put(preferenceTokens[1], Integer.parseInt(preferenceTokens[2]));
                projectPreferences.put(preferenceTokens[3], Integer.parseInt(preferenceTokens[4]));
                projectPreferences.put(preferenceTokens[5], Integer.parseInt(preferenceTokens[6]));
                projectPreferences.put(preferenceTokens[7], Integer.parseInt(preferenceTokens[8]));

                preference.setPreference(projectPreferences);

                preferences.add(preference);
            }
        } catch (IndexOutOfBoundsException | NumberFormatException ex) {
            return null;
        }

        return preferences;
    }
}
