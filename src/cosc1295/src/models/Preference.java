package cosc1295.src.models;

import helpers.commons.SharedConstants;

import javafx.util.Pair;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Preference implements Serializable {

	private static final long serialVersionUID = 8820778787277011380L;

	private String studentUniqueId;
    private HashMap<String, Integer> preference;

    public Preference() {
        preference = new HashMap<>();
    }

    public void setStudentUniqueId(String uniqueId) {
        studentUniqueId = uniqueId;
    }

    public String getStudentUniqueId() {
        return studentUniqueId;
    }

    public void setPreference(HashMap<String, Integer> preference) {
        this.preference = preference;
    }

    public HashMap<String, Integer> getPreference() {
        return preference;
    }

    public void addPreference(Pair<String, Integer> prefPair) {
        preference.put(prefPair.getKey(), prefPair.getValue());
    }

    /**
     * Creates string formatted with delimiter to save into file
     * @return String
     */
    public String stringify() {
        StringBuilder stringPref = new StringBuilder(studentUniqueId + SharedConstants.TEXT_DELIMITER);

        for (Map.Entry<String, Integer> entry : preference.entrySet())
            stringPref.append(entry.getKey())
                    .append(SharedConstants.TEXT_DELIMITER)
                    .append(entry.getValue())
                    .append(SharedConstants.TEXT_DELIMITER);

        return stringPref.toString();
    }

    public List<String> composeRaw(ResultSet rs) throws SQLException {
        List<String> data = new ArrayList<>();

        String studentIdTracker = SharedConstants.EMPTY_STRING;
        StringBuilder rawData = new StringBuilder(SharedConstants.EMPTY_STRING);
        while (rs.next()) {
            if (!studentIdTracker.equals(rs.getString("student_id"))) {
                if (rawData.length() != 0) data.add(rawData.toString());

                rawData = new StringBuilder(rs.getString("student_id") + SharedConstants.TEXT_DELIMITER);
                studentIdTracker = rs.getString("student_id");
            }

            rawData.append(rs.getString("project_id"))
                    .append(SharedConstants.TEXT_DELIMITER)
                    .append(rs.getString("rating"))
                    .append(SharedConstants.TEXT_DELIMITER);

            if (rs.isLast()) data.add(rawData.toString());
        }

        return data;
    }

    public Preference clone() {
        Preference clone = new Preference();
        clone.setStudentUniqueId(studentUniqueId);
        clone.setPreference(preference == null ? null : new HashMap<>(preference));

        return clone;
    }
}
