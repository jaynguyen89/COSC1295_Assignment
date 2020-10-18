package helpers.utilities;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import cosc1295.src.controllers.ControllerBase;
import cosc1295.src.models.*;
import cosc1295.src.services.HistoryService;
import helpers.commons.SharedConstants;
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

    public static List<Team> filterAssignableTeams(List<Team> teams) {
        List<Team> assignableTeams = new ArrayList<>();
        teams.forEach(team -> {
            if (team.getMembers().size() < SharedConstants.GROUP_LIMIT)
                assignableTeams.add(team);
        });

        return assignableTeams;
    }

    //Set full Student data into Team members
    public static void setStudentDataInTeams(List<Team> teams, List<Student> students) {
        for (Team team : teams) {
            List<Student> members = team.getMembers();
            List<Student> memberData = new ArrayList<>();

            for (Student member : members)
                for (Student student : students)
                    if (student.getUniqueId().equals(member.getUniqueId())) {
                        memberData.add(student);
                        break;
                    }

            team.setMembers(memberData);
        }
    }

    //Pick up only the Students who have not already in any Teams when user want to assign a Student to a Team
    public static List<Student> filterUnteamedStudents(List<Student> students, List<Team> teams) {
        List<Student> unteamedStudents = new ArrayList<>(students);

        for (Team team : teams)
            for (Student member : team.getMembers())
                unteamedStudents.removeIf(
                        m -> m.getUniqueId().equals(member.getUniqueId())
                );

        return unteamedStudents;
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

        //Temporarily faking add the pending Students to Team to check the resulting Team
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
     * The selection param holds the Team to get the assigned Student.
     * Checks for Personality Imbalance if the Team actually gets the offered Student.
     * Returns a Pair with Key indicating if Team's Personality is imbalance,
     * and Value indicating the Personalities that the Team needs, so user know what
     * Student they should select.
     * @param selection Pair<Team, Student>
     * @return Pair<Boolean, List<SharedEnums.PERSONALITIES>>
     */
    // Key indicates if Personality is imbalance when Key==true,
    // Value indicates the Personalities it enforces or needs
    private static Pair<Boolean, List<SharedEnums.PERSONALITIES>> checkImbalancePersonalityOnAssign(Pair<Team, Student> selection) {
        List<SharedEnums.PERSONALITIES> allPersonalities = new ArrayList<SharedEnums.PERSONALITIES>() {{
            add(SharedEnums.PERSONALITIES.A);
            add(SharedEnums.PERSONALITIES.B);
            add(SharedEnums.PERSONALITIES.C);
            add(SharedEnums.PERSONALITIES.D);
        }};
        List<Student> teamMembers = new ArrayList<>(selection.getKey().getMembers());

        //Team has no member, no PERSONALITIES enforcement is required yet.
        if (teamMembers.size() < 1) return null;
        teamMembers.add(selection.getValue()); //Faking add the student to Team, so we have a prospective Team to inspect

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
        Team firstTeam = first.getKey().clone();
        Team secondTeam = second.getKey().clone();

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

    /**
     * Assigns a Student to a Team and possibly replaces a Team's member.
     * The action will be saved to history and if the Team has 4 members after the assignment,
     * the FitnessMetrics will be calculated for it.
     * @param teamAndMember Pair<Team, Student>
     * @param assignee Student
     * @param projects List<Project>
     * @param preferences List<Preference>
     * @return boolean
     */
    @SuppressWarnings("UnusedReturnValue")
    public static boolean assignStudentToTeam(
        Pair<Team, Student> teamAndMember, Student assignee, List<Project> projects, List<Preference> preferences
    ) {
        HistoryService history = HistoryService.getInstance();

        //The teamAndMember has its Value not null, meaning a member should be replaced, so remove the member first
        if (teamAndMember.getValue() != null &&
            !teamAndMember.getKey().removeMemberByUniqueId(teamAndMember.getValue().getUniqueId())
        ) return false;

        //Then add the assignee to the Team then calculate FitnessMetrics if Team has 4 members
        teamAndMember.getKey().addMember(assignee);
        if (teamAndMember.getKey().getMembers().size() == SharedConstants.GROUP_LIMIT)
            teamAndMember.getKey().setFitnessMetrics(
                (new ControllerBase()).calculateTeamFitnessMetricsFor(teamAndMember.getKey(), projects, preferences)
            );

        //Finally add a history item to support undoing
        history.add(new Pair<>(
            new Pair<>(teamAndMember.getKey(), null), new Pair<>(assignee, teamAndMember.getValue())
        ));

        return true;
    }

    /**
     * Swaps Students between Teams and recalculates FitnessMetrics if any Team has 4 members
     * @param first Pair<Team, Student>
     * @param second Pair<Team, Student>
     * @param projects List<Project>
     * @param preferences List<Preference>
     * @return Pair<Team, Team>
     */
    public static Pair<Team, Team> swapStudentsBetweenTeams(
        Pair<Team, Student> first, Pair<Team, Student> second, List<Project> projects, List<Preference> preferences
    ) {
        HistoryService history = HistoryService.getInstance();
        //First, remove the member from Team 1
        boolean swapResult = first.getKey().removeMemberByUniqueId(first.getValue().getUniqueId());;

        //If Team 2 specifies a member, it means that member will be swapped to Team 1
        //If Team 2 specifies no member, it means Team 2 will take Team 1 member, and Team 1 receive no member
        if (swapResult && second.getValue() == null)
            second.getKey().addMember(first.getValue());
        else if (swapResult) {
            first.getKey().addMember(second.getValue());
            swapResult = second.getKey().replaceMemberByUniqueId(second.getValue().getUniqueId(), first.getValue());
        }

        if (swapResult) { //If Students are swapped successfully, then recalculate FitnessMetrics if necessary
            if (first.getKey().getMembers().size() == SharedConstants.GROUP_LIMIT)
                first.getKey().setFitnessMetrics(
                    (new ControllerBase()).calculateTeamFitnessMetricsFor(first.getKey(), projects, preferences)
                );

            history.add(new Pair<>( //Set history for undoing feature
                new Pair<>(first.getKey(), second.getKey()),
                new Pair<>(second.getValue(), first.getValue()))
            );

            return new Pair<>(first.getKey(), second.getKey());
        }

        return null; //Swap failed
    }

    /**
     * Used for the analyzers to check if a Student can be assigned to a Team without any
     * worry about the details of requirements.
     * Data passed in with Pair<Team, Student> being Team to get the assignee, and Student being a replaced member.
     * @param student Student
     * @param teamAndMember Pair<Team, Student>
     * @return boolean
     */
    public static boolean isStudentAssignable(Student student, Pair<Team, Student> teamAndMember) {
        List<Student> members = new ArrayList<>(teamAndMember.getKey().getMembers());
        Team faker = teamAndMember.getKey().clone();
        if (teamAndMember.getValue() != null) {
            members.removeIf(m -> m.getUniqueId().equalsIgnoreCase(teamAndMember.getValue().getUniqueId()));
            faker.removeMemberByUniqueId(teamAndMember.getValue().getUniqueId());
        }

        Pair<Boolean, List<String>> requirements = produceTeamRequirementsOnNewMember(members, new ArrayList<Student>() {{ add(student); }});
        Pair<Boolean, List<SharedEnums.PERSONALITIES>> balance = checkImbalancePersonalityOnAssign(new Pair<>(faker, student));
        if (requirements == null && balance == null) return true;

        boolean assignable = true;
        if (requirements != null) {
            assignable = !(requirements.getKey() && teamAndMember.getKey().getMembers().size() == SharedConstants.GROUP_LIMIT - 1);
            assignable = assignable && (requirements.getValue() == null || !requirements.getValue().contains(student.getUniqueId()));
        }

        if (balance != null) assignable = assignable && !balance.getKey();
        return assignable;
    }

    /**
     * Used for the analyzers to check if 2 Students can be swapped between 2 Teams without any
     * worry about the details of requirements.
     * Data must be passed in respective order of Team-Student belonging to each other.
     * @param teams Pair<Team, Team>
     * @param students Pair<Student, Student>
     * @return boolean
     */
    public static boolean areStudentsSwappable(Pair<Team, Team> teams, Pair<Student, Student> students) {
        Pair<Team, Student> first = new Pair<>(teams.getKey(), students.getKey());
        Pair<Team, Student> second = new Pair<>(teams.getValue(), students.getValue());

        Pair<Pair<Boolean, String>, Pair<Boolean, String>> requirements = isTeamRequirementsMutuallySatisfied(first, second);
        Pair<
            Pair<Boolean, List<SharedEnums.PERSONALITIES>>,
            Pair<Boolean, List<SharedEnums.PERSONALITIES>>
        > balance = isImbalancePersonalityOnSwap(first, second);

        if (requirements == null && balance == null) return true;

        boolean isSwappable = true;
        if (requirements != null) {
            Pair<Boolean, String> firstTeamRequires = requirements.getKey();
            Pair<Boolean, String> secondTeamRequires = requirements.getValue();

            if (firstTeamRequires != null) {
                isSwappable = firstTeamRequires.getKey();
                isSwappable = isSwappable && firstTeamRequires.getValue() == null;
            }

            if (secondTeamRequires != null) {
                isSwappable = isSwappable && secondTeamRequires.getKey();
                isSwappable = isSwappable && secondTeamRequires.getValue() == null;
            }
        }

        if (isSwappable && balance != null) {
            Pair<Boolean, List<SharedEnums.PERSONALITIES>> firstTeamBalance = balance.getKey();
            Pair<Boolean, List<SharedEnums.PERSONALITIES>> secondTeamBalance = balance.getValue();

            isSwappable = isSwappable &&
                (firstTeamBalance == null || firstTeamBalance.getKey() == null || !firstTeamBalance.getKey()) &&
                (secondTeamBalance == null || secondTeamBalance.getKey() == null || !secondTeamBalance.getKey());
        }

        return isSwappable;
    }

    //For unittest only
    public static Pair<Boolean, List<SharedEnums.PERSONALITIES>>
    runCheckImbalancePersonalityOnAssignForTest(Pair<Team, Student> assignment) {
        return checkImbalancePersonalityOnAssign(assignment);
    }
}
