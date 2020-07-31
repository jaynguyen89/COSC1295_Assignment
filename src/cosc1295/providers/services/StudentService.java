package cosc1295.providers.services;

import cosc1295.providers.bases.TextFileServiceBase;
import cosc1295.providers.interfaces.IStudentService;
import cosc1295.src.models.Student;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.SKILLS;
import helpers.commons.SharedEnums.RANKINGS;
import helpers.commons.SharedEnums.DATA_TYPES;
import helpers.commons.SharedEnums.PERSONALITIES;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StudentService extends TextFileServiceBase implements IStudentService {

    @Override
    public List<Student> readAllStudentsFromFile() {
        List<String> rawStudentData = readEntireRawDataFromFile(DATA_TYPES.STUDENT);

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
                    String skill = studentTokens[i].substring(0, 1);
                    int ranking = Integer.parseInt(
                        studentTokens[i].substring(studentTokens[i].length() - 1)
                    ) - 1;
                    String name = SKILLS.P.name();
                    SKILLS eSkill = skill.equals(SKILLS.A.name()) ? SKILLS.A :
                        (skill.equals(SKILLS.N.name()) ? SKILLS.N :
                            (skill.equals(SKILLS.P.name()) ? SKILLS.P : SKILLS.W)
                        );

                    RANKINGS eRanking = RANKINGS.values()[ranking];

                    skillRanking.put(eSkill, eRanking);
                }

                student.setSkillRanking(skillRanking);

                if (studentTokens.length == 7)
                    student.setPersonality(
                        studentTokens[6].equals(PERSONALITIES.A.name()) ? PERSONALITIES.A
                            : (studentTokens[6].equals(PERSONALITIES.B.name()) ? PERSONALITIES.B
                            : (studentTokens[6].equals(PERSONALITIES.C.name()) ? PERSONALITIES.C : PERSONALITIES.D))
                    );

                List<String> conflicters = new ArrayList<>();
                if (studentTokens.length == 8) conflicters.add(studentTokens[7].trim());
                if (studentTokens.length == 9) conflicters.add(studentTokens[8].trim());

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
}
