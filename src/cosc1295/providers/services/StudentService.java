package cosc1295.providers.services;

import cosc1295.providers.bases.DatabaseContext;
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
import helpers.utilities.LogicalAssistant;

import javafx.util.Pair;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * For Dependency Injection
 */
public class StudentService extends TextFileServiceBase implements IStudentService {

    private final DatabaseContext context;

    public StudentService() {
        context = DatabaseContext.getInstance();
    }

    @Override
    public List<Student> readAllStudentsFromFile() {
        List<String> rawStudentData;
        if (SharedConstants.DATA_SOURCE.equals(TextFileServiceBase.class.getSimpleName()))
            rawStudentData = readAllDataFromFile(DATA_TYPES.STUDENT);
        else
            rawStudentData = context.retrieveCompositeDataForType(Student.class);

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
                    Pair<SKILLS, RANKINGS> skPair = LogicalAssistant.parseSkillRankingToken(studentTokens[i]);
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
    public Boolean updateStudent(Student student) {
        return SharedConstants.DATA_SOURCE.equals(TextFileServiceBase.class.getSimpleName())
                ? updateStudentToFile(student) : updateStudentToDatabase(student);
    }

    @Override
    public Boolean saveStudentPreferences(Preference preference) {
        return SharedConstants.DATA_SOURCE.equals(TextFileServiceBase.class.getSimpleName())
                ? savePreferenceToFile(preference) : savePreferenceToDatabase(preference);
    }

    @Override
    public List<Preference> readAllStudentPreferencesFromFile() {
        List<String> rawPreferences;
        if (SharedConstants.DATA_SOURCE.equals(TextFileServiceBase.class.getSimpleName()))
            rawPreferences = readAllDataFromFile(DATA_TYPES.PREFERENCE);
        else
            rawPreferences = context.retrieveCompositeDataForType(Preference.class);

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

    //Reversed traversal is needed because only the last preference entry of the student is effective
    @Override
    public Preference retrievePreferenceForStudent(String uniqueId) {
        List<Preference> allPreferences = readAllStudentPreferencesFromFile();

        for (int i = allPreferences.size() - 1; i >= 0; i--)
            if (allPreferences.get(i).getStudentUniqueId().equals(uniqueId))
                return allPreferences.get(i);

        return null; //no preference entry found
    }

    private Boolean updateStudentToFile(Student student) {
        String normalizedStudent = student.stringify();
        return updateEntryToFileById(normalizedStudent, student.getId(), DATA_TYPES.STUDENT);
    }

    private Boolean updateStudentToDatabase(Student student) {
        String query = "UPDATE `students` SET `personality`= ?, `conflicter1_id`= ?, `conflicter2_id`= ? WHERE `id`= ?";

        PreparedStatement statement = context.createStatement(query, SharedConstants.DB_UPDATE);
        if (statement == null) return false;

        try {
            statement.setString(1, student.getPersonality().name());
            statement.setInt(4, student.getId());

            List<String> conflicters = student.getConflicters();
            if (conflicters.size() != 0) {
                statement.setString(2, conflicters.get(0));
                statement.setString(3, conflicters.size() < 2 ? null : conflicters.get(1));
            }

            return context.executeDataModifierQuery(statement);
        } catch (SQLException ex) {
            return null;
        }
    }

    private Boolean savePreferenceToFile(Preference preference) {
        String normalizedPreference = preference.stringify();
        return saveEntryToFile(normalizedPreference, DATA_TYPES.PREFERENCE);
    }

    private Boolean savePreferenceToDatabase(Preference preference) {
        String query = "SELECT `id` FROM `students` WHERE `unique_id` = '" + preference.getStudentUniqueId() + "';";

        List<HashMap<String, String>> result = context.executeDataRetrievalQuery(query);
        if (result == null) return null;

        try {
            int studentId = Integer.parseInt(result.get(0).get("id"));
            String randDate = Helpers.randomDateTimeInRange("2020-09-10 00:00:00", "2020-09-20 23:59:59");

            context.toggleAutoCommit(false);
            query = "INSERT INTO `student_preferences` (`student_id`, `inserted_on`) VALUES (?, ?)";

            PreparedStatement statement = context.createStatement(query, SharedConstants.DB_INSERT);
            if (statement == null) return null;

            statement.setInt(1, studentId);
            statement.setString(2, randDate);

            int spId = context.executeDataInsertionQuery(statement);
            boolean error = spId <= 0;

            if (!error) for (Map.Entry<String, Integer> entry : preference.getPreference().entrySet()) {
                query = "SELECT `id` FROM `projects` WHERE `unique_id` = '" + entry.getKey() + "';";

                result = context.executeDataRetrievalQuery(query);
                error = result == null;

                if (error) break;

                int projectId = Integer.parseInt(result.get(0).get("id"));
                query = "INSERT INTO `preferences` (`student_preference_id`, `project_id`, `rating`) VALUES (?, ?, ?)";

                statement = context.createStatement(query, SharedConstants.DB_INSERT);
                error = statement == null;

                if (error) break;

                statement.setInt(1, spId);
                statement.setInt(2, projectId);
                statement.setInt(3, entry.getValue());

                error = context.executeDataInsertionQuery(statement) <= 0;
                if (error) break;
            }

            if (error) {
                context.revertChanges();
                context.toggleAutoCommit(true);
                return null;
            }

            context.saveChanges();
            context.toggleAutoCommit(true);
            return true;
        } catch (NumberFormatException | SQLException ex) {
            try { context.revertChanges(); } catch (SQLException e) { return null; }
            return null;
        }
    }
}
