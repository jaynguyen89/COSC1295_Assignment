package cosc1295.src.models;

import cosc1295.src.models.generic.IThing;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.SKILLS;
import helpers.commons.SharedEnums.RANKINGS;
import helpers.commons.SharedEnums.PERSONALITIES;
import helpers.utilities.Helpers;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Student implements IThing, Serializable {

	private static final long serialVersionUID = 8281202876066671060L;

	private int id;
    private String uniqueId;
    private HashMap<SKILLS, RANKINGS> skillRanking;
    private PERSONALITIES personality;
    private List<String> conflicters;

    public Student() {
        conflicters = new ArrayList<>();
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    @Override
    public Boolean isUniqueIdAvailable() {
        return Helpers.checkUniqueIdAvailableFor(this.getClass(), uniqueId);
    }

    public void setSkillRanking(HashMap<SKILLS, RANKINGS> skillRanking) {
        this.skillRanking = skillRanking;
    }

    public HashMap<SKILLS, RANKINGS> getSkillRanking() {
        return skillRanking;
    }

    public void setPersonality(PERSONALITIES personality) {
        this.personality = personality;
    }

    public void setConflicters(List<String> conflicters) {
        this.conflicters = conflicters;
    }

    public void addConflicter(String uniqueId) {
        conflicters.add(uniqueId);
    }

    /**
     * Creates a string information of Student to print out on console
     * @return String
     */
    public String display() {
        StringBuilder displayString = new StringBuilder(id + ". " + uniqueId + "\t\t");

        for (Map.Entry<SKILLS, RANKINGS> entry : skillRanking.entrySet())
            displayString.append(entry.getKey().name())
                    .append(entry.getValue().getValue() + 1)
                    .append(SharedConstants.SPACE);

        if (personality != null)
            displayString.append(personality.name())
                         .append(" ");

        if (!conflicters.isEmpty())
            for (String conflicter : conflicters)
                displayString.append(conflicter)
                             .append(SharedConstants.SPACE);

        return displayString.toString();
    }

    public PERSONALITIES getPersonality() {
        return personality;
    }

    public List<String> getConflicters() {
        return conflicters;
    }

    /**
     * Creates string formatted with delimiter to save into file
     * @return String
     */
    public String stringify() {
        StringBuilder stringStudent = new StringBuilder(
            id + SharedConstants.TEXT_DELIMITER +
            uniqueId + SharedConstants.TEXT_DELIMITER
        );

        for (Map.Entry<SKILLS, RANKINGS> entry : skillRanking.entrySet())
            stringStudent.append(entry.getKey().name())
            .append(entry.getValue().getValue() + 1)
            .append(SharedConstants.TEXT_DELIMITER);

        if (personality != null)
            stringStudent.append(personality.name())
            .append(SharedConstants.TEXT_DELIMITER);

        if (!conflicters.isEmpty())
            for (String conflicter : conflicters)
                stringStudent.append(conflicter)
                .append(SharedConstants.TEXT_DELIMITER);

        return stringStudent.toString();
    }

    /**
     * Creates delimeterized string from data retrieved from database as data saved in text file.
     * @param rs ResultSet
     * @return String
     * @throws SQLException
     */
    public List<String> composeRaw(ResultSet rs) throws SQLException {
        List<String> data = new ArrayList<>();

        int studentIdTracker = -1;
        StringBuilder rawData = new StringBuilder(SharedConstants.EMPTY_STRING);
        String moreData = SharedConstants.EMPTY_STRING;
        while (rs.next()) {
            if (studentIdTracker != rs.getInt("id")) {
                if (rawData.length() != 0) {
                    rawData.append(moreData);
                    data.add(rawData.toString());
                }

                rawData = new StringBuilder(
                    rs.getInt("id") + SharedConstants.TEXT_DELIMITER +
                    rs.getString("unique_id") + SharedConstants.TEXT_DELIMITER
                );

                String conflicter1 = rs.getString("first_conflicter");
                String conflicter2 = rs.getString("second_conflicter");

                moreData = rs.getString("personality") + SharedConstants.TEXT_DELIMITER +
                        (conflicter1 == null ? SharedConstants.EMPTY_STRING : conflicter1 + SharedConstants.TEXT_DELIMITER) +
                        (conflicter2 == null ? SharedConstants.EMPTY_STRING : conflicter2 + SharedConstants.TEXT_DELIMITER);

                studentIdTracker = rs.getInt("id");
            }

            rawData.append(rs.getString("skill"))
                    .append(rs.getInt("ranking"))
                    .append(SharedConstants.TEXT_DELIMITER);

            if (rs.isLast()) {
                rawData.append(moreData);
                data.add(rawData.toString());
            }
        }

        return data;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public Student clone() {
        Student clone = new Student();
        clone.setId(id);
        clone.setConflicters(conflicters == null ? null : new ArrayList<>(conflicters));
        clone.setPersonality(personality);
        clone.setSkillRanking(skillRanking == null ? null : new HashMap<>(skillRanking));
        clone.setUniqueId(uniqueId);

        return clone;
    }
}
