package tests;

import cosc1295.providers.bases.DatabaseContext;
import cosc1295.providers.bases.TextFileServiceBase;
import cosc1295.providers.services.ProjectService;
import cosc1295.providers.services.StudentService;
import cosc1295.src.controllers.ControllerBase;
import cosc1295.src.models.*;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.SKILLS;
import junit.framework.TestCase;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TeamFitnessMetricsTest {

    private static ProjectService projectService;

    private static List<Student> students;
    private static List<Project> projects;
    private static List<Preference> preferences;
    private static ControllerBase controllerBase;

    private static final List<Team> testData = new ArrayList<>();

    @BeforeClass
    public static void setUpBeforeClass() {
        SharedConstants.DATA_SOURCE = TextFileServiceBase.class.getSimpleName();

        controllerBase = new ControllerBase();
        projectService = new ProjectService();
        projects = projectService.readAllProjectsFromFile();

        StudentService studentService = new StudentService();
        students = studentService.readAllStudentsFromFile();
        preferences = studentService.readAllStudentPreferencesFromFile();
    }

    @Before
    public void setUp() {
        Random r = new Random();

        Team testTeam1 = new Team();
        setRandomTeamData(testTeam1);

        Team testTeam2 = new Team();
        setRandomTeamData(testTeam2);

        Team testTeam3 = new Team();
        setRandomTeamData(testTeam3);
        TeamFitness fitness3 = new TeamFitness();
        fitness3.setId(r.nextInt());
        testTeam3.setFitnessMetrics(fitness3);

        testData.add(testTeam1);
        testData.add(testTeam2);
        testData.add(testTeam3);

        Team testTeam4 = new Team();
        testTeam4.setId(111);
        testTeam4.setProject(projects.get(1));
        testTeam4.addMember(students.get(0));
        testTeam4.addMember(students.get(5));
        testTeam4.addMember(students.get(9));
        testTeam4.addMember(students.get(10));
        testData.add(testTeam4);

        Team testTeam5 = new Team();
        testTeam5.setId(222);
        testTeam5.setProject(projects.get(3));
        testTeam5.addMember(students.get(11));
        testTeam5.addMember(students.get(5));
        testTeam5.addMember(students.get(6));
        testTeam5.addMember(students.get(3));
        testData.add(testTeam5);

        Team testTeam6 = new Team();
        testTeam6.setId(333);
        testTeam6.setProject(projects.get(2));
        testTeam6.addMember(students.get(8));
        testTeam6.addMember(students.get(1));
        testTeam6.addMember(students.get(11));
        testTeam6.addMember(students.get(4));
        TeamFitness fitness6 = new TeamFitness();
        fitness6.setId(334);
        testTeam6.setFitnessMetrics(fitness6);
        testData.add(testTeam6);
    }

    private void setRandomTeamData(Team team) {
        Random r = new Random();

        team.setId(r.nextInt());
        team.setProject(projects.get(r.nextInt(projects.size() - 1)));

        team.addMember(students.get(r.nextInt(students.size() - 1)));
        for (int i = 0; i < 3; i++) {
            Student student = team.getMembers().get(0);
            while (team.getMembers().contains(student))
                student = students.get(r.nextInt(students.size() - 1));

            team.addMember(student);
        }
    }

    @Test
    public void skillCompetenciesShouldBeCorrect() {
        controllerBase = new ControllerBase();

        for (int i = 3; i < testData.size(); i++) {
            Team testTeam = testData.get(i);
            TeamFitness fitnessMetrics = controllerBase.calculateTeamFitnessMetricsFor(testTeam, projects, preferences);

            //Asserting average competency of team
            TestCase.assertEquals(
                i == 3 ? 6.0 : (i == 4 ? 6.0 : 6.0),
                fitnessMetrics.getAverageTeamSkillCompetency()
            );

            //Asserting average competency by skill categories
            for (Map.Entry<SKILLS, Double> entry : fitnessMetrics.getTeamCompetency().entrySet()) {
                if (entry.getKey() == SKILLS.A)
                    TestCase.assertEquals(i == 3 ? 1.0 : (i == 4 ? 1.75 : 1.25), entry.getValue());

                if (entry.getKey() == SKILLS.N)
                    TestCase.assertEquals(i == 3 ? 2.0 : (i == 4 ? 0.75 : 1.25), entry.getValue());

                if (entry.getKey() == SKILLS.P)
                    TestCase.assertEquals(i == 3 ? 2.0 : (i == 4 ? 1.0 : 1.25), entry.getValue());

                if (entry.getKey() == SKILLS.W)
                    TestCase.assertEquals(i == 3 ? 1.0 : (i == 4 ? 2.5 : 2.25), entry.getValue());
            }
        }
    }

    @Test
    public void projectPreferencePercentageShouldBeCorrect() {
        controllerBase = new ControllerBase();

        for (int i = 3; i < testData.size(); i++) {
            Team testTeam = testData.get(i);
            TeamFitness fitnessMetrics = controllerBase.calculateTeamFitnessMetricsFor(testTeam, projects, preferences);

            //Asserting average Team's satisfaction
            TestCase.assertEquals(
                i == 3 ? 50.0 : (i == 4 ? 50.0 : 50.0),
                fitnessMetrics.getPreferenceSatisfaction().getKey()
            );

            //Asserting average satisfaction by first and second preference
            TestCase.assertEquals(
                i == 3 ? 25.0 : (i == 4 ? 25.0 : 0.0),
                fitnessMetrics.getPreferenceSatisfaction().getValue().getKey()
            );

            TestCase.assertEquals(
                i == 3 ? 25.0 : (i == 4 ? 25.0 : 50.0),
                fitnessMetrics.getPreferenceSatisfaction().getValue().getValue()
            );
        }
    }

    @Test
    public void skillShortFallsShouldBeCorrect() {
        controllerBase = new ControllerBase();

        for (int i = 3; i < testData.size(); i++) {
            Team testTeam = testData.get(i);
            TeamFitness fitnessMetrics = controllerBase.calculateTeamFitnessMetricsFor(testTeam, projects, preferences);

            if (i == 5) TestCase.assertEquals(334, fitnessMetrics.getId());

            //Asserting Team's average shortfall
            TestCase.assertEquals(
                i == 3 ? 2.25 : (i == 4 ? 1.75 : 1.62),
                fitnessMetrics.getAverageSkillShortfall()
            );

            //Asserting for 4 selected projects
            for (Project project : projects) {
                double team4Expected = project.getUniqueId().equalsIgnoreCase("pro1") ? 3.0
                        : (project.getUniqueId().equalsIgnoreCase("pro2") ? 2.0
                        : (project.getUniqueId().equalsIgnoreCase("pro3") ? 2.0 : 2.0));

                double team5Expected = project.getUniqueId().equalsIgnoreCase("pro1") ? 1.5
                        : (project.getUniqueId().equalsIgnoreCase("pro2") ? 2.0
                        : (project.getUniqueId().equalsIgnoreCase("pro3") ? 1.75 : 1.75));

                double team6Expected = project.getUniqueId().equalsIgnoreCase("pro1") ? 1.75
                        : (project.getUniqueId().equalsIgnoreCase("pro2") ? 1.75
                        : (project.getUniqueId().equalsIgnoreCase("pro3") ? 1.5 : 1.5));

                TestCase.assertEquals(
                        i == 3 ? team4Expected : (i == 4 ? team5Expected : team6Expected),
                        fitnessMetrics.getSkillShortFall().get(project.getUniqueId())
                );
            }
        }
    }

    @After
    public void tearDown() {
        testData.clear();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        SharedConstants.DATA_SOURCE = DatabaseContext.class.getSimpleName();
        projectService = null;
        controllerBase = null;
        projects = null;
    }
}
