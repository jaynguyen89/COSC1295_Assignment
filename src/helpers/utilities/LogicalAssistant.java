package helpers.utilities;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import cosc1295.src.models.Student;
import cosc1295.src.models.Team;
import helpers.commons.SharedEnums;

import javafx.util.Pair;
import java.util.ArrayList;
import java.util.List;

public final class LogicalAssistant {

    /**
     * To be used by any data processing Service if required. This method parses a
     * Skill-Ranking token saved in file to produce corresponding Enum values.
     * Eg. token "W1" parses into SKILLS.W and RANKING.LOW
     * Returns a Skill-Ranking Pair.
     * @param token String
     * @return Pair<SharedEnums.SKILLS, SharedEnums.RANKINGS>
     */
    public static
    Pair<SharedEnums.SKILLS,
            SharedEnums.RANKINGS>
    parseSkillRankingToken(@NotNull String token) {
        String skill = token.substring(0, 1);
        int ranking = Integer.parseInt(
                token.substring(token.length() - 1)
        ) - 1;

        SharedEnums.SKILLS eSkill =
            skill.equals(SharedEnums.SKILLS.A.name()) ? SharedEnums.SKILLS.A :
                (skill.equals(SharedEnums.SKILLS.N.name()) ? SharedEnums.SKILLS.N :
                    (skill.equals(SharedEnums.SKILLS.P.name()) ? SharedEnums.SKILLS.P
                                                               : SharedEnums.SKILLS.W)
                );

        SharedEnums.RANKINGS eRanking = SharedEnums.RANKINGS.values()[ranking];

        return new Pair<>(eSkill, eRanking);
    }

    /**
     * Produce the requirements on new member for a Team when user assign a Student into Team.
     * Requirements include Leader type and Personality.
     * The selectedStudents is list of Students that are pending to be added (these students are validated against the Team requirements priorly).
     * Returns a Pair, with Key indicating if Leader type is required, and Value indicating the Students that are refused to be added.
     * @param members List<Student>
     * @param selectedStudents List<Student>
     * @return Pair<Boolean, List<String>>
     */
    //null for any student, Key=true for leader type required, Value contains refused Student Unique Id
    public static Pair<Boolean, List<String>> produceTeamRequirementsOnNewMember(List<Student> members, @Nullable List<Student> selectedStudents) {
        boolean leaderTypeRequired = true; //Key of the Pair to be returned
        List<String> refusedStudents = new ArrayList<>(); //Value of the Pair to be returned

        //Temporarily add the pending Students to Team to check the resulting Team
        List<Student> teamMembers = new ArrayList<>();
        teamMembers.addAll(members);
        teamMembers.addAll(selectedStudents);

        //Check the resulting Team
        for (Student member : teamMembers) {
            //leaderTypeRequired is combined in this condition to omit this checking if Leader type is already assigned in Team
            if (leaderTypeRequired && member.getPersonality().name().equals(SharedEnums.PERSONALITIES.A.name()))
                leaderTypeRequired = false;

            //Get the Students that cannot be added to Team due to conflicts
            for (String studentUniqueId : member.getConflicters())
                if (!refusedStudents.contains(studentUniqueId))
                    refusedStudents.add(studentUniqueId);
        }

        //No requirements, any Students can be added, returns null, otherwise, returns the requirements
        if (!leaderTypeRequired && refusedStudents.size() == 0) return null;
        return new Pair<>(
            leaderTypeRequired,
            refusedStudents.size() == 0 ? null : refusedStudents
        );
    }

    /**
     * Checks the requirements of both Teams in swapping Students. Each Team passes into this method its
     * Team details and the Student it offers for swap. The user will get a result as follow:
     * Pair<Key, Value> with Key is the requirements of First Team, Value is the requirements of Second Team.
     * Each Key, Value is Pair<Boolean, String>, with Boolean indicating if the Team needs Leader type, and
     * Value indicating the Students it refuses.
     * @param first Pair<Team, Student>
     * @param second Pair<Team, Student>
     * @return Pair<Pair<Boolean, String>, Pair<Boolean, String>>
     */
    public static Pair<Pair<Boolean, String>, Pair<Boolean, String>>
    isTeamRequirementsMutuallySatisfied(Pair<Team, Student> first, Pair<Team, Student> second) {
        List<Student> firstTeamMembers = new ArrayList<>(first.getKey().getMembers());
        firstTeamMembers.remove(first.getValue()); //Fake removing the Student offered for swap in first Team

        List<Student> secondTeamMembers = new ArrayList<>(second.getKey().getMembers());
        if (second.getValue() != null) secondTeamMembers.remove(second.getValue()); //Fake removing the Student offered for swap in second Team

        //Call the above method to produce requirements of each Team
        Pair<Boolean, List<String>> firstTeamRequirements = produceTeamRequirementsOnNewMember(firstTeamMembers, new ArrayList<>());
        Pair<Boolean, List<String>> secondTeamRequirements = produceTeamRequirementsOnNewMember(secondTeamMembers, new ArrayList<>());

        //Pick up requirements of first Team
        Boolean firstTeamLeaderRequired = firstTeamRequirements != null && firstTeamRequirements.getKey();
        List<String> firstTeamRefusals = firstTeamRequirements == null ? null : firstTeamRequirements.getValue();

        //Pick up requirements of second Team
        boolean secondTeamLeaderRequired = secondTeamRequirements!= null && secondTeamRequirements.getKey();
        List<String> secondTeamRefusals = secondTeamRequirements == null ? null : secondTeamRequirements.getValue();

        //Team 2 does not send Student to Team 1, so no need to check Team 1,
        //Only check if Student sent from Team 1 meets Team 2 requirements
        if (second.getValue() == null) {
            if (secondTeamLeaderRequired && secondTeamMembers.size() < 3)
                secondTeamLeaderRequired = false;

            if (secondTeamRefusals != null && secondTeamRefusals.contains(first.getValue().getUniqueId()))
                return new Pair<>(null, new Pair<>(secondTeamLeaderRequired, first.getValue().getUniqueId()));

            if (secondTeamLeaderRequired)
                return new Pair<>(null, new Pair<>(true, null));
        }
        //Both Teams send Students to each other (swap), check both Team's requirements
        else {
            Pair<Boolean, String> firstTeamUnsatisfiedRequirements = checkForUnsatisfiedRequirements(
                second, firstTeamMembers, firstTeamLeaderRequired, firstTeamRefusals
            );
            Pair<Boolean, String> secondTeamUnsatisfiedRequirements = checkForUnsatisfiedRequirements(
                first, secondTeamMembers, secondTeamLeaderRequired, secondTeamRefusals
            );

            if (firstTeamUnsatisfiedRequirements != null || secondTeamUnsatisfiedRequirements != null)
                return new Pair<>(firstTeamUnsatisfiedRequirements, secondTeamUnsatisfiedRequirements);
        }

        return null;
    }

    /**
     * When swapping Students between Teams, if both Teams refuse each other, pick up what is being refused.
     * Eg. Pick up what Team 1 needs on Team 2 and vice versa.
     * Return a Pair with Key indicating if one Team requires Leader from the other,
     * and Value indicating the Students that one Team refuses.
     * @param teamSelections Pair<Team, Student>
     * @param members List<Student>
     * @param isTeamLeaderRequired Boolean
     * @param refusals List<String>
     * @return Pair<Boolean, String>
     */
    private static Pair<Boolean, String> checkForUnsatisfiedRequirements(
        Pair<Team, Student> teamSelections,
        List<Student> members,
        Boolean isTeamLeaderRequired,
        List<String> refusals
    ) {
        Pair<Boolean, String> unsatisfiedRequirements = null;

        //Team only has 0-2 Student, not yet enforce Leader type as there are slot available later
        if (isTeamLeaderRequired && members.size() < 3)
            isTeamLeaderRequired = false;

        //Team has 3 Students and Leader already assigned, no need to enforce Leader type
        if (members.size() == 3 && teamSelections.getValue().getPersonality() == SharedEnums.PERSONALITIES.A)
            isTeamLeaderRequired = false;

        //Team refuses some Students, check if the offered Student is among the refused ones
        //If yes, return isTeamLeaderRequired along with refusal to simplify the next return
        if (refusals != null && refusals.contains(teamSelections.getValue().getUniqueId()))
            unsatisfiedRequirements = new Pair<>(isTeamLeaderRequired, teamSelections.getValue().getUniqueId());

        //Team does not refuse the offered Student
        if (isTeamLeaderRequired)
            unsatisfiedRequirements = new Pair<>(true, null);

        //Team is cool with the offered student, return null here
        return unsatisfiedRequirements;
    }

    /**
     * The selection param holds Team being the Team to get the assigned Student.
     * Checks for Personality Imbalance if the Team actually gets the offered Student.
     * Returns a Pair with Key indicating if Team's Personality is imbalance,
     * and Value indicating the Personalities that the Team needs, so user know what
     * Student they should select.
     * @param selection Pair<Team, Student>
     * @return Pair<Boolean, List<SharedEnums.PERSONALITIES>>
     */
    //Key indicates if Personality is imbalance, when Key==true,
    // Value indicates the Personalities it requires for the next added member (not the current one)
    public static Pair<Boolean, List<SharedEnums.PERSONALITIES>> checkImbalancePersonalityOnAssign(Pair<Team, Student> selection) {
        List<SharedEnums.PERSONALITIES> allPersonalities = new ArrayList<SharedEnums.PERSONALITIES>() {/**
			 * 
			 */
			private static final long serialVersionUID = 1429894234507507258L;

		{
            add(SharedEnums.PERSONALITIES.A);
            add(SharedEnums.PERSONALITIES.B);
            add(SharedEnums.PERSONALITIES.C);
            add(SharedEnums.PERSONALITIES.D);
        }};
        List<Student> teamMembers = new ArrayList<>(selection.getKey().getMembers());

        //Team has no member, no PERSONALITIES enforcement is required yet.
        if (teamMembers.size() < 1) return null;
        teamMembers.add(selection.getValue()); //Add the student to Team, so we have a prospective Team to inspect

        List<SharedEnums.PERSONALITIES> teamPersonalities = new ArrayList<>();
        for (Student member : teamMembers)
            if (!teamPersonalities.contains(member.getPersonality()))
                teamPersonalities.add(member.getPersonality());

        //The prospective team has 3 or more Personality types, no requirement is needed
        if (teamPersonalities.size() > 2) return null;

        //The prospective team has only 1 or 2 Personality types, but team has rooms for 2 more Personality types
        //So personality enforcement is not yet required, but indicates Team's requirement on Personality
        List<SharedEnums.PERSONALITIES> required = new ArrayList<>(allPersonalities);
        required.removeAll(teamPersonalities);

        if (teamMembers.size() == 1 || (
            teamMembers.size() == 2 && teamPersonalities.size() >= 1) || (
            teamMembers.size() == 3 && teamPersonalities.size() >= 2)
        ) return new Pair<>(false, required);

        return new Pair<>(true, required);
    }

    /**
     * Checks for Personality Imbalance of 2 Teams in swapping Students.
     * Params are the Teams and the Student it offered for swap.
     * Returns a Pair for each Team, with Boolean indicating if Personality imbalance occurs,
     * and List of Personalities that it needs on the offered Student.
     * @param first Pair<Team, Student>
     * @param second Pair<Team, Student>
     * @return Pair<
            * Pair<Boolean, List<SharedEnums.PERSONALITIES>>,
     *        Pair<Boolean, List<SharedEnums.PERSONALITIES>>
     *      >
     */
    public static Pair<
        Pair<Boolean, List<SharedEnums.PERSONALITIES>>,
        Pair<Boolean, List<SharedEnums.PERSONALITIES>>
    > isImbalancePersonalityOnSwap(Pair<Team, Student> first, Pair<Team, Student> second) {
        Team firstTeam = first.getKey();
        Team secondTeam = second.getKey();

        //Fake removing the Student that each Team offers for swap,
        // and add the offered Student from the other Team to have the prospective Teams
        firstTeam.getMembers().remove(first.getValue());
        secondTeam.getMembers().remove(second.getValue());

        //Call the above method to produce requirements for each Team
        Pair<Boolean, List<SharedEnums.PERSONALITIES>> firstTeamImbalanceCheck =
                checkImbalancePersonalityOnAssign(new Pair<>(firstTeam, second.getValue()));
        Pair<Boolean, List<SharedEnums.PERSONALITIES>> secondTeamImbalanceCheck =
                checkImbalancePersonalityOnAssign(new Pair<>(secondTeam, first.getValue()));

        //If 1 or both Teams disagree on the offered Student, return at this
        if (firstTeamImbalanceCheck != null || secondTeamImbalanceCheck != null)
            return new Pair<>(firstTeamImbalanceCheck, secondTeamImbalanceCheck);

        return null;
    }
}
