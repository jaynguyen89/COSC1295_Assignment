package cosc1295.src.views.gui.viewmodels;

import cosc1295.src.models.Student;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.SKILLS;
import javafx.beans.property.SimpleStringProperty;

import java.util.concurrent.atomic.AtomicReference;

public class StudentVM {

    private SimpleStringProperty uniqueId;
    public String getUniqueId() {
        return uniqueId.get();
    }

    private SimpleStringProperty aVal;
    public String getAVal() {
        return aVal.get();
    }

    private SimpleStringProperty nVal;
    public String getNVal() {
        return nVal.get();
    }

    private SimpleStringProperty pVal;
    public String getPVal() {
        return pVal.get();
    }

    private SimpleStringProperty wVal;
    public String getWVal() {
        return wVal.get();
    }

    private SimpleStringProperty personality;
    public String getPersonality() {
        return personality.get();
    }

    private SimpleStringProperty conflicters;
    public String getConflicters() {
        return conflicters.get();
    }

    public static StudentVM cast(Student any) {
        StudentVM s = new StudentVM();
        s.uniqueId = new SimpleStringProperty(any.getUniqueId());
        s.aVal = new SimpleStringProperty((any.getSkillRanking().get(SKILLS.A).getValue() + 1) + SharedConstants.EMPTY_STRING);
        s.nVal = new SimpleStringProperty((any.getSkillRanking().get(SKILLS.N).getValue() + 1) + SharedConstants.EMPTY_STRING);
        s.pVal = new SimpleStringProperty((any.getSkillRanking().get(SKILLS.P).getValue() + 1) + SharedConstants.EMPTY_STRING);
        s.wVal = new SimpleStringProperty((any.getSkillRanking().get(SKILLS.W).getValue() + 1) + SharedConstants.EMPTY_STRING);
        s.personality = new SimpleStringProperty(any.getPersonality().name());
        s.conflicters = new SimpleStringProperty(any.getConflicters().size() == 0 ? "-" : (
            any.getConflicters().get(0) + (
                any.getConflicters().size() == 2 ? ", " + any.getConflicters().get(1) : SharedConstants.EMPTY_STRING
            )
        ));

        return s;
    }

    public AtomicReference<Student> backCast() {
        return new AtomicReference<>(new Student());
    }
}
