package cosc1295.src.models;

import helpers.commons.SharedConstants;

import javafx.util.Pair;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Preference implements Serializable {

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
}
