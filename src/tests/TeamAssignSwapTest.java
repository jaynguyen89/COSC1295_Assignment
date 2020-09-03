package tests;

import cosc1295.providers.services.ProjectService;
import cosc1295.providers.services.StudentService;
import cosc1295.src.controllers.TeamFormationController;
import cosc1295.src.models.Project;
import cosc1295.src.models.Student;
import cosc1295.src.models.Team;
import cosc1295.src.views.TeamView;
import helpers.commons.SharedEnums.PERSONALITIES;

import helpers.utilities.LogicalAssistant;
import javafx.util.Pair;
import junit.framework.TestCase;
import org.junit.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class TeamAssignSwapTest {

    private static ProjectService projectService;
    private static TeamView teamView;

    private static List<Student> students;
    private static List<Project> projects;

    private static Team testTeam1 = new Team();
    private static Team testTeam2 = new Team();
    private static Team testTeam3 = new Team();

    private static final InputStream originalIn = System.in;
    private static final PrintStream originalOut = System.out;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    @BeforeClass
    public static void setUpBeforeClass() {
        projectService = new ProjectService();
        projects = projectService.readAllProjectsFromFile();
        teamView = new TeamView();

        StudentService studentService = new StudentService();
        students = studentService.readAllStudentsFromFile();
    }

    @Before
    public void setUp() {
        System.setOut(new PrintStream(out));

        //Team 1 has 3 Students, no Leader type among them
        testTeam1.setId(111);
        testTeam1.setProject(projects.get(2));
        testTeam1.addMember(students.get(0)); //S1
        testTeam1.addMember(students.get(6)); //S7
        testTeam1.addMember(students.get(10)); //S11

        //Team 2 has 2 Students, 1 having Leader type
        testTeam2.setId(222);
        testTeam2.setProject(projects.get(3));
        testTeam2.addMember(students.get(7)); //S8
        testTeam2.addMember(students.get(1)); //S2

        //Team 3 has no member
        testTeam3.setId(333);
        testTeam3.setProject(projects.get(0));
    }

    //Test that a Student already having Team won't be assigned to another Team
    @Test
    public void invalidMemberTest_Negative() {
        List<Team> teams = new ArrayList<Team>() {/**
			 * 
			 */
			private static final long serialVersionUID = 297111981103698088L;

		{ add(testTeam1); add(testTeam2); add(testTeam3); }};
        List<Student> unteamedStudents = LogicalAssistant.filterUnteamedStudents(students, teams);

        try {
            teamView.sendTestInput(new ByteArrayInputStream("7\n".getBytes())); //S7 from Team 1
            teamView.selectStudentsToAssign(testTeam2, unteamedStudents);
        } catch (NoSuchElementException ignored) { }

        String outputs = out.toString();
        TestCase.assertTrue(outputs.contains("Error! No Student was found with your selection, or the Student you select has been assigned to a Team."));
    }

    @Test
    public void invalidMemberTest_Positive() {
        List<Team> teams = new ArrayList<Team>() {/**
			 * 
			 */
			private static final long serialVersionUID = -5309334361410531771L;

		{ add(testTeam1); add(testTeam2); add(testTeam3); }};
        List<Student> unteamedStudents = LogicalAssistant.filterUnteamedStudents(students, teams);

        try {
            teamView.sendTestInput(new ByteArrayInputStream("10\n".getBytes())); //An unteamed Student
            teamView.selectStudentsToAssign(testTeam2, unteamedStudents);
        } catch (NoSuchElementException ignored) { }

        String outputs = out.toString();
        TestCase.assertTrue(outputs.contains("Success! Student S10 will be added to Team."));
    }

    //Test that s Student having personal conflict with a Team member won't be allowed to assign
    @Test
    public void studentConflictTest_Negative() {
        //Swap S7 to Team 2
        Pair<Team, Student> teamAndStudentToSwap1 = new Pair<>(testTeam1, testTeam1.getMembers().get(1));
        //Swap S8 to Team 1, S11 in Team 1 has conflict with S8
        Pair<Team, Student> teamAndStudentToSwap2 = new Pair<>(testTeam2, testTeam2.getMembers().get(0));

        Pair<Pair<Boolean, String>, Pair<Boolean, String>> agreementResults =
            LogicalAssistant.isTeamRequirementsMutuallySatisfied(teamAndStudentToSwap1, teamAndStudentToSwap2);

        TestCase.assertNotNull(agreementResults); //Teams disagree

        Pair<Boolean, String> team1Requirement = agreementResults.getKey();
        Pair<Boolean, String> team2Requirement = agreementResults.getValue();

        TestCase.assertNull(team2Requirement); //Team 2 is good with Student sent from Team 1
        TestCase.assertNotNull(team1Requirement); //Team 1 is unwell with Student sent from Team 2

        //Asserting into details of Team 1 Requirement
        assert team1Requirement.getKey() != null;
        TestCase.assertEquals((Boolean) false, team1Requirement.getKey()); //Team 1 no longer needs a Leader
        TestCase.assertEquals("S8", team1Requirement.getValue()); //Team 1 refuses S8
    }

    @Test
    public void studentConflictTest_Positive() {
        //Swap S11 to Team 2
        Pair<Team, Student> teamAndStudentToSwap1 = new Pair<>(testTeam1, testTeam1.getMembers().get(0));
        //Swap S8 to Team 1, S8 conflicts with S11 but S11 is offered to swap
        Pair<Team, Student> teamAndStudentToSwap2 = new Pair<>(testTeam2, testTeam2.getMembers().get(1));

        Pair<Pair<Boolean, String>, Pair<Boolean, String>> agreementResults =
            LogicalAssistant.isTeamRequirementsMutuallySatisfied(teamAndStudentToSwap1, teamAndStudentToSwap2);

        TestCase.assertNull(agreementResults);
    }

    @Test
    public void swapStudentWithPersonalityRequirementTest_Negative() {
        testTeam1.removeMemberByUniqueId("S11");
        testTeam1.addMember(students.get(2)); //S3
        testTeam1.addMember(students.get(4)); //S5
        testTeam2.addMember(students.get(5)); //S6

        //Team 1 sends S5 to Team 2
        Pair<Team, Student> teamAndStudentToSwap1 = new Pair<>(testTeam1, testTeam1.getMembers().get(3));
        //Team 2 send a Student with not a Leader type (S6)
        Pair<Team, Student> teamAndStudentToSwap2 = new Pair<>(testTeam2, testTeam2.getMembers().get(2));

        Pair<Pair<Boolean, String>, Pair<Boolean, String>> agreementResults =
            LogicalAssistant.isTeamRequirementsMutuallySatisfied(teamAndStudentToSwap1, teamAndStudentToSwap2);

        TestCase.assertNotNull(agreementResults);

        Pair<Boolean, String> team1Requirement = agreementResults.getKey();
        Pair<Boolean, String> team2Requirement = agreementResults.getValue();

        TestCase.assertNull(team2Requirement); //Team 2 is good with Student sent from Team 1
        TestCase.assertNotNull(team1Requirement); //Team 1 is unwell with Student sent from Team 2

        //Asserting into details of Team 1 Requirement
        assert team1Requirement.getKey() != null;
        TestCase.assertEquals((Boolean) true, team1Requirement.getKey()); //Team 1 enforces a Leader
        TestCase.assertNull(team1Requirement.getValue()); //Team 1 indicates no refusals
    }

    //Test that a Student won't be assigned twice to a Team
    @Test
    public void repeatedMemberTest_Negative() {
        List<Team> teams = new ArrayList<Team>() {/**
			 * 
			 */
			private static final long serialVersionUID = -784806039847708982L;

		{ add(testTeam1); add(testTeam2); add(testTeam3); }};
        List<Student> unteamedStudents = LogicalAssistant.filterUnteamedStudents(students, teams);

        try {
            teamView.sendTestInput(new ByteArrayInputStream("8\n".getBytes())); //S8 from Team 2 itself
            teamView.selectStudentsToAssign(testTeam2, unteamedStudents);
        } catch (NoSuchElementException ignored) { }

        String outputs = out.toString();
        TestCase.assertTrue(outputs.contains("Error! No Student was found with your selection, or the Student you select has been assigned to a Team."));
    }

    @Test
    public void swapStudentWithPersonalityRequirementTest_Positive() {
        testTeam1.removeMemberByUniqueId("S11");
        testTeam1.addMember(students.get(2)); //S3
        testTeam1.addMember(students.get(4)); //S5
        testTeam2.addMember(students.get(5)); //S6

        //Team 1 sends S5 to Team 2
        Pair<Team, Student> teamAndStudentToSwap1 = new Pair<>(testTeam1, testTeam1.getMembers().get(3));
        //Team 2 send a Leader type to Team 1 (S8)
        Pair<Team, Student> teamAndStudentToSwap2 = new Pair<>(testTeam2, testTeam2.getMembers().get(0));

        Pair<Pair<Boolean, String>, Pair<Boolean, String>> agreementResults =
            LogicalAssistant.isTeamRequirementsMutuallySatisfied(teamAndStudentToSwap1, teamAndStudentToSwap2);

        TestCase.assertNull(agreementResults); //Both Teams meet each other's requirements
    }

    @Test
    public void repeatedMemberTest_Positive() {
        List<Team> teams = new ArrayList<Team>() {/**
			 * 
			 */
			private static final long serialVersionUID = 8498102672531333817L;

		{ add(testTeam1); add(testTeam2); add(testTeam3); }};
        List<Student> unteamedStudents = LogicalAssistant.filterUnteamedStudents(students, teams);

        try {
            teamView.sendTestInput(new ByteArrayInputStream("4\n".getBytes())); //An unteamed Student
            teamView.selectStudentsToAssign(testTeam2, unteamedStudents);
        } catch (NoSuchElementException ignored) { }

        String outputs = out.toString();
        TestCase.assertTrue(outputs.contains("Success! Student S4 will be added to Team."));
    }

    //Test that a Team must have at lest 1 Student with Leader type
    @Test
    public void noLeaderTest_Negative() {
        List<Team> teams = new ArrayList<Team>() {/**
			 * 
			 */
			private static final long serialVersionUID = -7002655101849027773L;

		{ add(testTeam1); add(testTeam2); add(testTeam3); }};
        List<Student> unteamedStudents = LogicalAssistant.filterUnteamedStudents(students, teams);

        try {
            teamView.sendTestInput(new ByteArrayInputStream("3\n".getBytes())); //An unteamed Student
            teamView.selectStudentsToAssign(testTeam1, unteamedStudents);
        } catch (NoSuchElementException ignored) { }

        String outputs = out.toString();
        TestCase.assertTrue(outputs.contains("This Team needs a Student with Leader personality type (A)."));
        TestCase.assertTrue(outputs.contains("Error! You must select a Student with Leader personality type (A) because this Team only has 1 slot left but no Leader is assigned"));
    }

    @Test
    public void noLeaderTest_Positive() {
        List<Team> teams = new ArrayList<Team>() {/**
			 * 
			 */
			private static final long serialVersionUID = 7277754089895533172L;

		{ add(testTeam1); add(testTeam2); add(testTeam3); }};
        List<Student> unteamedStudents = LogicalAssistant.filterUnteamedStudents(students, teams);

        try {
            teamView.sendTestInput(new ByteArrayInputStream("9\n".getBytes())); //An unteamed Student
            teamView.selectStudentsToAssign(testTeam1, unteamedStudents);
        } catch (NoSuchElementException ignored) { }

        String outputs = out.toString();
        TestCase.assertTrue(outputs.contains("This Team needs a Student with Leader personality type (A)."));
        TestCase.assertTrue(outputs.contains("Success! Student S9 will be added to Team."));
    }

    //Test that the Personality Imbalance is computed correctly when assigning a Student into Team
    //This test covers both Negative and Positive cases
    @Test
    public void personalityImbalanceTest() {
        Pair<Boolean, List<PERSONALITIES>> imbalance;

        //Check 1: Team 3 has 0 member.
        imbalance = LogicalAssistant.checkImbalancePersonalityOnAssign(new Pair<>(testTeam3, students.get(7))); //S8-A
        TestCase.assertNull(imbalance); //Personality Imbalance does not occur

        //Check 2: Team 3 has 1 member with Personality A. Adding another A
        testTeam3.addMember(students.get(7)); //Add S8-A
        imbalance = LogicalAssistant.checkImbalancePersonalityOnAssign(new Pair<>(testTeam3, students.get(8))); //S9-A
        TestCase.assertNotNull(imbalance); //Personality Imbalance indicates something
        TestCase.assertEquals((Boolean) false, imbalance.getKey()); //Personality enforcement is not applied yet
        TestCase.assertEquals(3, imbalance.getValue().size()); //Team needs 3 types of Personality

        //Check 3: Team 3 has 2 members with same Personality A. Adding another A
        testTeam3.addMember(students.get(8)); //Add S9-A
        imbalance = LogicalAssistant.checkImbalancePersonalityOnAssign(new Pair<>(testTeam3, students.get(3))); //S4-A
        TestCase.assertNotNull(imbalance); //Personality Imbalance indicates something
        TestCase.assertEquals((Boolean) true, imbalance.getKey()); //Personality enforcement is applied
        TestCase.assertEquals(3, imbalance.getValue().size()); //Team needs 2 more types of Personality

        //Check 4: Team 3 has 2 members with Personality A, B. Adding another A
        testTeam3.removeMemberByUniqueId(students.get(8).getUniqueId()); //Remove S9-A
        testTeam3.addMember(students.get(5)); //Add S6-B
        imbalance = LogicalAssistant.checkImbalancePersonalityOnAssign(new Pair<>(testTeam3, students.get(8))); //S9-A
        TestCase.assertNotNull(imbalance); //Personality Imbalance indicates something
        TestCase.assertEquals((Boolean) false, imbalance.getKey()); //Personality enforcement is not applied yet
        TestCase.assertEquals(2, imbalance.getValue().size()); //Team needs 2 more types of Personality

        //Check 5: Team 3 has 3 members with Personality A, B. Adding another B
        testTeam3.addMember(students.get(8)); //Add S9-A
        imbalance = LogicalAssistant.checkImbalancePersonalityOnAssign(new Pair<>(testTeam3, students.get(9))); //S10-B
        TestCase.assertNotNull(imbalance); //Personality Imbalance indicates something
        TestCase.assertEquals((Boolean) true, imbalance.getKey()); //Personality enforcement is applied
        TestCase.assertEquals(2, imbalance.getValue().size()); //Team needs 2 more types of Personality

        //Check 6: Team 3 has 4 members with Personality A, B, D
        imbalance = LogicalAssistant.checkImbalancePersonalityOnAssign(new Pair<>(testTeam3, students.get(6))); //S7-D
        TestCase.assertNull(imbalance); //Personality Imbalance does not occur

        //Check 7: Team 3 has 4 members with Personality A, B, C, D
        testTeam3.removeMemberByUniqueId("S9"); //Remove S9-A
        imbalance = LogicalAssistant.checkImbalancePersonalityOnAssign(new Pair<>(testTeam3, students.get(2))); //S3-C
        TestCase.assertNull(imbalance); //Personality Imbalance does not occur
    }

    @After
    public void tearDown() {
        testTeam1 = new Team();
        testTeam2 = new Team();
        testTeam3 = new Team();

        System.setOut(originalOut);
        System.setIn(originalIn);
        teamView = new TeamView();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        projectService = null;
        projects = null;
        students = null;

        testTeam1 = null;
        testTeam2 = null;
        testTeam3 = null;
        teamView = null;
    }
}
