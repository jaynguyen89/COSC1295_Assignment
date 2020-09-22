package cosc1295.providers.bases;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

import com.mysql.cj.jdbc.MysqlDataSource;
import cosc1295.src.models.*;
import helpers.commons.SharedConstants;

public class DatabaseContext {

    private static final String PROPS_PATH = System.getProperty("user.dir") + "\\db.properties";

    private final HashMap<String, String> TABLE_NAMES = new HashMap<String, String>() {{
        put(Address.class.getSimpleName(), "addresses");
        put(Company.class.getSimpleName(), "companies");
        put(TeamFitness.class.getSimpleName(), "fitness_metrics");
        put(Preference.class.getSimpleName(), "preferences");
        put(Project.class.getSimpleName(), "projects");
        put(ProjectOwner.class.getSimpleName(), "project_owners");
        put(Role.class.getSimpleName(), "roles");
        put(Student.class.getSimpleName(), "students");
        put(Team.class.getSimpleName(), "teams");
    }};

    private static DatabaseContext context;
    private static Connection connection;

    private DatabaseContext() { }

    public static DatabaseContext getInstance() {
        if (context == null) {
            synchronized (DatabaseContext.class) {
                context = context == null ?
                            new DatabaseContext()
                            : context;
            }
        }

        try {
            prepareConnection();
        } catch (SQLException | IOException ex) {
            return null;
        }

        return context;
    }

    private static void prepareConnection() throws SQLException, IOException {
        Properties connectionProperties = new Properties();

        FileInputStream inputStream = new FileInputStream(PROPS_PATH);
        connectionProperties.load(inputStream);

        MysqlDataSource dataSource = new MysqlDataSource();

        if (SharedConstants.DATA_ENV.equals("server")) {
            dataSource.setURL(
                connectionProperties.getProperty(SharedConstants.DATA_ENV + ".url") +
                connectionProperties.getProperty(SharedConstants.DATA_ENV + ".databaseName")
            );
            dataSource.setUser(connectionProperties.getProperty(SharedConstants.DATA_ENV + ".username"));
            dataSource.setPassword(connectionProperties.getProperty(SharedConstants.DATA_ENV + ".password"));
            dataSource.setPort(Integer.parseInt(connectionProperties.getProperty(SharedConstants.DATA_ENV + ".port")));
            dataSource.setServerTimezone(connectionProperties.getProperty(SharedConstants.DATA_ENV + ".serverTimezone"));
            dataSource.setAutoReconnect(Boolean.parseBoolean(connectionProperties.getProperty(SharedConstants.DATA_ENV + ".autoReconnect")));
            dataSource.setUseSSL(Boolean.parseBoolean(connectionProperties.getProperty(SharedConstants.DATA_ENV + ".useSSL")));
            dataSource.setRequireSSL(Boolean.parseBoolean(connectionProperties.getProperty(SharedConstants.DATA_ENV + ".requireSSL")));
        }
        else
            dataSource.setUrl(
                connectionProperties.getProperty(SharedConstants.DATA_ENV + ".url") + ":" +
                connectionProperties.getProperty(SharedConstants.DATA_ENV + ".port") + "/" +
                connectionProperties.getProperty(SharedConstants.DATA_ENV + ".databaseName") + "?user=" +
                connectionProperties.getProperty(SharedConstants.DATA_ENV + ".username") + "&password=" +
                connectionProperties.getProperty(SharedConstants.DATA_ENV + ".password")
            );

        connection = dataSource.getConnection();
    }

    //This method gets data from 1 single table. Suitable for Address, Company, Role, Project Owner, TeamFitness.
    public <T> List<String> retrieveSimpleDataForType(Class<T> type) {
        String table = TABLE_NAMES.get(type.getSimpleName());
        String query = "SELECT * FROM " + table;

        List<String> data = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String rawEntry = makeRawEntry(type, resultSet);
                data.add(rawEntry);
            }

            statement.close();
        } catch (
            SQLException | IllegalAccessException | InstantiationException |
            InvocationTargetException | NullPointerException ex
        ) {
            return null;
        }

        return data;
    }

    //This method gets data from multiple tables by joining. Suitable for Preference, Project, Student, Team.
    public <T> List<String> retrieveCompositeDataForType(Class<T> type) {
        String query = composeQueryForType(type);
        List<String> data;

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            data = makeListRawEntry(type, resultSet);
        } catch (SQLException | IllegalAccessException | InstantiationException | InvocationTargetException ex) {
            return null;
        }

        return data;
    }

    public List<HashMap<String, String>> executeDataRetrievalQuery(String query) {
        List<HashMap<String, String>> data = new ArrayList<>();

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            ResultSetMetaData metaData = resultSet.getMetaData();
            while (resultSet.next()) {
                HashMap<String, String> rowData = new HashMap<>();

                for (int i = 1; i <= metaData.getColumnCount(); i++)
                    rowData.put(metaData.getColumnLabel(i), resultSet.getString(metaData.getColumnLabel(i)));

                data.add(rowData);
            }

        } catch (SQLException ex) {
            return null;
        }

        return data;
    }

    public <T> String getRawEntryForType(Class<T> type, String id, boolean byId) {
        String table = TABLE_NAMES.get(type.getSimpleName());
        String query = "SELECT * FROM `" + table + "` WHERE " + (byId ? "`id`" : "`unique_id`") + " = ?";

        String rawData = null;
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, id);
            ResultSet entry = statement.executeQuery();

            while (entry.next()) rawData = makeRawEntry(type, entry);
        } catch (SQLException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            return null;
        }

        return rawData;
    }

    //Used for insert, update, delete querying
    public Boolean executeDataModifierQuery(String query) {
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            int result = statement.executeUpdate(query);

            return result != 0;
        } catch (SQLException ex) {
            return null;
        }
    }

    public <T> Boolean isRedundantUniqueId(Class<T> type, String uniqueId) {
        String table = TABLE_NAMES.get(type.getSimpleName());
        String query = "SELECT * FROM `" + table + "` WHERE `unique_id` = ?";

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, uniqueId.toUpperCase());
            ResultSet resultSet = statement.executeQuery();

            return resultSet.next();
        } catch (SQLException ex) {
            return null;
        }
    }

    public void close() {
        try {
            connection.close();
            context = null;
        } catch (SQLException ignored) { }
    }

    private <T> String makeRawEntry(Class<T> type, ResultSet rs)
        throws IllegalAccessException, InstantiationException, InvocationTargetException
    {
        Method composeRaw = getComposeRawMethod(type);

        if (composeRaw != null) {
            composeRaw.setAccessible(true);
            return (String) composeRaw.invoke(type.newInstance(), rs);
        }

        throw new NullPointerException();
    }

    private <T> List<String> makeListRawEntry(Class<T> type, ResultSet rs)
        throws IllegalAccessException, InstantiationException, InvocationTargetException
    {
        Method composeRaw = getComposeRawMethod(type);

        if (composeRaw != null) {
            composeRaw.setAccessible(true);
            return (List<String>) composeRaw.invoke(type.newInstance(), rs);
        }

        throw new NullPointerException();
    }

    private <T> Method getComposeRawMethod(Class<T> type) {
        Method composeRaw = null;

        Method[] methods = type.getDeclaredMethods();
        for (Method method : methods)
            if (method.getName().equals("composeRaw")) {
                composeRaw = method;
                break;
            }

        return composeRaw;
    }

    private <T> String composeQueryForType(Class<T> type) {
        switch (type.getSimpleName()) {
            case "Preference":
                return "SELECT `student_unique_id` AS student_id, `project_unique_id` AS `project_id`, `rating`" +
                        "  FROM (" +
                        "    SELECT P.`student_preference_id` AS preference_id, S.`id` AS `student_id`, S.`unique_id` AS student_unique_id, PR.`unique_id` AS project_unique_id, P.`rating`" +
                        "      FROM `students` S, `preferences` P, `projects` PR WHERE P.`project_id` = PR.`id`" +
                        "    ) T1 JOIN (" +
                        "      SELECT SP.`id`, sp.`student_id`, MAX(SP.`inserted_on`) AS inserted_on FROM `student_preferences` SP GROUP BY SP.`student_id`" +
                        "    ) T2" +
                        "    ON T1.`preference_id` = T2.`id` AND T1.`student_id` = T2.`student_id` ORDER BY student_id;";
            case "Project":
                return "SELECT P.*, SR.`skill`, SR.`ranking` FROM `projects` P, `project_owners` PO, `rankings` R, `skill_rankings` SR" +
                        "  WHERE P.`project_owner_id` = PO.`id` AND R.`subject_id` = P.`id` AND SR.`ranking_id` = R.`id` AND R.`subject_type` = 'PROJECT';";
            case "Student":
                return "SELECT T1.*, SR.`skill`, SR.`ranking`" +
                        "  FROM (" +
                        "    SELECT S1.`id`, S1.`unique_id`, S1.`personality`, S2.`unique_id` AS first_conflicter, S3.`unique_id` AS second_conflicter" +
                        "      FROM `students` S1 LEFT JOIN `students` S2 ON S1.`conflicter1_id` = S2.`id`" +
                        "      LEFT JOIN `students` S3 ON S1.`conflicter2_id` = S3.`id`) T1, `skill_rankings` SR, `rankings` R" +
                        "  WHERE T1.`id` = R.`subject_id` AND R.`id` = SR.`ranking_id` AND R.`subject_type` = 'STUDENT';";
            default: //Team
                return "SELECT T1.*, F.`id` AS `fitness_metric_id`" +
                        "  FROM (" +
                        "    SELECT T.*, S.`unique_id` FROM `teams` T, `team_members` M, `students` S" +
                        "    WHERE T.`id` = M.`team_id` AND M.`student_id` = S.`id`" +
                        "  ) T1 LEFT JOIN `fitness_metrics` F ON F.`team_id` = T1.`id`;";
        }
    }
}
