package cosc1295.src.views.gui.viewmodels;

import cosc1295.src.models.Student;
import helpers.commons.SharedConstants;
import javafx.beans.property.SimpleStringProperty;

/**
 * This ViewModel is used to display members of a Team in a TableView widget.
 */
public class TeamMemberVM {

    public SimpleStringProperty firstMem;
    public String getFirstMem() {
        return firstMem == null ? SharedConstants.EMPTY_STRING : firstMem.get();
    }

    public SimpleStringProperty secondMem;
    public String getSecondMem() {
        return secondMem == null ? SharedConstants.EMPTY_STRING : secondMem.get();
    }

    public SimpleStringProperty thirdMem;
    public String getThirdMem() {
        return thirdMem == null ? SharedConstants.EMPTY_STRING : thirdMem.get();
    }

    public SimpleStringProperty fourthMem;
    public String getFourthMem() {
        return fourthMem == null ? SharedConstants.EMPTY_STRING : fourthMem.get();
    }

    public void set(Student any, int index) {
        if (index == 0) firstMem = new SimpleStringProperty(any.getUniqueId());
        if (index == 1) secondMem = new SimpleStringProperty(any.getUniqueId());
        if (index == 2) thirdMem = new SimpleStringProperty(any.getUniqueId());
        if (index == 3) fourthMem = new SimpleStringProperty(any.getUniqueId());
    }
}
