package tests;

import cosc1295.providers.services.ProjectService;
import cosc1295.providers.services.StudentService;
import cosc1295.src.controllers.ControllerBase;
import cosc1295.src.models.*;
import junit.framework.TestCase;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;

public class StandardDeviationTest {

    private static ProjectService projectService;

    private static List<Student> students;
    private static List<Project> projects;
    private static List<Preference> preferences;
    private static ControllerBase controllerBase;

    private static final List<Team> testData1 = new ArrayList<>();
    private static final List<Team> testData2 = new ArrayList<>();

    @BeforeClass
    public static void setUpBeforeClass() {
        projectService = new ProjectService();
        projects = projectService.readAllProjectsFromFile();

        StudentService studentService = new StudentService();
        students = studentService.readAllStudentsFromFile();
        preferences = studentService.readAllStudentPreferencesFromFile();
    }

    @Before
    public void setUp() {
        //Team set 1: contains 3 Teams
        Team testTeam1 = new Team();
        testTeam1.setId(111);
        testTeam1.setProject(projects.get(1));
        testTeam1.addMember(students.get(0));
        testTeam1.addMember(students.get(5));
        testTeam1.addMember(students.get(9));
        testTeam1.addMember(students.get(10));
        testData1.add(testTeam1);

        Team testTeam2 = new Team();
        testTeam2.setId(222);
        testTeam2.setProject(projects.get(3));
        testTeam2.addMember(students.get(11));
        testTeam2.addMember(students.get(5));
        testTeam2.addMember(students.get(6));
        testTeam2.addMember(students.get(3));
        testData1.add(testTeam2);

        Team testTeam3 = new Team();
        testTeam3.setId(333);
        testTeam3.setProject(projects.get(2));
        testTeam3.addMember(students.get(8));
        testTeam3.addMember(students.get(1));
        testTeam3.addMember(students.get(11));
        testTeam3.addMember(students.get(4));
        TeamFitness fitness3 = new TeamFitness();
        fitness3.setId(334);
        testTeam3.setFitnessMetrics(fitness3);
        testData1.add(testTeam3);

        //Team set 2: contains 3 Teams
        Team testTeam4 = new Team();
        testTeam4.setId(444);
        testTeam4.setProject(projects.get(0));
        testTeam4.addMember(students.get(7));
        testTeam4.addMember(students.get(3));
        testTeam4.addMember(students.get(11));
        testTeam4.addMember(students.get(2));
        testData2.add(testTeam4);

        Team testTeam5 = new Team();
        testTeam5.setId(555);
        testTeam5.setProject(projects.get(2));
        testTeam5.addMember(students.get(6));
        testTeam5.addMember(students.get(8));
        testTeam5.addMember(students.get(0));
        testTeam5.addMember(students.get(1));
        testData2.add(testTeam5);

        Team testTeam6 = new Team();
        testTeam6.setId(666);
        testTeam6.setProject(projects.get(1));
        testTeam6.addMember(students.get(5));
        testTeam6.addMember(students.get(9));
        testTeam6.addMember(students.get(3));
        testTeam6.addMember(students.get(4));
        TeamFitness fitness6 = new TeamFitness();
        fitness6.setId(667);
        testTeam6.setFitnessMetrics(fitness6);
        testData2.add(testTeam6);
    }

    @Test
    public void skillCompetencyStandardDeviationShouldBeCorrect() {
        controllerBase = new ControllerBase();
        //Calculate skill competency SD for each Team set
        double set1CompetencySD = controllerBase.executeStandardDeviationCalculationForTest(testData1, projects, preferences).get(0);
        double set2CompetencySD = controllerBase.executeStandardDeviationCalculationForTest(testData2, projects, preferences).get(0);

        //Assert correct skill competency SD for each Teams set
        TestCase.assertEquals(0.057, set1CompetencySD);
        TestCase.assertEquals(0.057, set2CompetencySD);
    }

    @Test
    public void preferenceSatisfactionStandardDeviationShouldBeCorrect() {
        controllerBase = new ControllerBase();
        //Calculate preference satisfaction SD for each Team set
        double set1SatisfactionSD = controllerBase.executeStandardDeviationCalculationForTest(testData1, projects, preferences).get(1);
        double set2SatisfactionSD = controllerBase.executeStandardDeviationCalculationForTest(testData2, projects, preferences).get(1);

        //Assert correct preference satisfaction SD for each Teams set
        TestCase.assertEquals(31.18, set1SatisfactionSD);
        TestCase.assertEquals(23.57, set2SatisfactionSD);
    }

    @Test
    public void skillShortfallsStandardDeviationShouldBeCorrect() {
        controllerBase = new ControllerBase();
        //Calculate skill shortfall SD for each Team set
        double set1ShortfallSD = controllerBase.executeStandardDeviationCalculationForTest(testData1, projects, preferences).get(2);
        double set2ShortfallSD = controllerBase.executeStandardDeviationCalculationForTest(testData2, projects, preferences).get(2);

        //Assert correct skill shortfall SD for each Teams set
        TestCase.assertEquals(0.329, set1ShortfallSD);
        TestCase.assertEquals(0.118, set2ShortfallSD);
    }

    @After
    public void tearDown() {
        testData1.clear();
        testData2.clear();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        projectService = null;
        controllerBase = null;
        projects = null;
    }
}
