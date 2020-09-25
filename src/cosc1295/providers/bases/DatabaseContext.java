package cosc1295.providers.bases;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mysql.cj.jdbc.MysqlDataSource;
import com.sun.istack.internal.NotNull;
import cosc1295.src.models.*;
import helpers.commons.SharedConstants;

/**
 * Singleton: the database context that contains the connection to database.
 * Provides all necessary methods to execute on-demand queries against database.
 */
public class DatabaseContext {
    private static final Logger logger = Logger.getLogger(DatabaseContext.class.getName());

    private static final String PROPS_PATH = System.getProperty("user.dir") + "\\db.properties";

    /**
     * A HashMap that stores database table names corresponding to model names.
     * Used to get a table according to class type refection when needed to query data.
     */
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
            if (SharedConstants.DEV) logger.log(Level.SEVERE, "DatabaseContext.getInstance : " + ex.getMessage());
            return null;
        }

        return context;
    }

    /**
     * Establishes a connection to database according to DATA_ENV.
     * DATA_ENV == `server` : connects to CPanel server when assignment is submitted.
     * DATA_ENV == `local` : connects to localhost server for development.
     * The connection properties are obtained from db.properties file.
     * @throws SQLException
     * @throws IOException
     */
    private static void prepareConnection() throws SQLException, IOException {
        Properties connectionProperties = new Properties();

        //Read connection properties from db.properties into Properties object
        FileInputStream inputStream = new FileInputStream(PROPS_PATH);
        connectionProperties.load(inputStream);

        MysqlDataSource dataSource = new MysqlDataSource();

        if (SharedConstants.DATA_ENV.equals("server")) { //Connect to CPanel server
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
        else //Connect to localhost server
            dataSource.setUrl(
                connectionProperties.getProperty(SharedConstants.DATA_ENV + ".url") + ":" +
                connectionProperties.getProperty(SharedConstants.DATA_ENV + ".port") + "/" +
                connectionProperties.getProperty(SharedConstants.DATA_ENV + ".databaseName") + "?user=" +
                connectionProperties.getProperty(SharedConstants.DATA_ENV + ".username") + "&password=" +
                connectionProperties.getProperty(SharedConstants.DATA_ENV + ".password")
            );

        connection = dataSource.getConnection();
        inputStream.close();
    }

    /**
     * Retrieves all data in 1 table according to model class `type`.
     * Suitable for data that are only stored in 1 table: Address, Company, Role, Project Owner, TeamFitness.
     * Data are returned in a list of delimeterized strings like data saved in text file.
     * @param type Class<T>
     * @param <T> Class
     * @return List<String>
     */
    public <T> List<String> retrieveSimpleDataForType(@NotNull Class<T> type) {
        String table = TABLE_NAMES.get(type.getSimpleName());
        String query = "SELECT * FROM " + table;

        List<String> data = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            //Get data (delimeterized strings) from resultSet by calling makeRawEntry
            while (resultSet.next()) {
                String rawEntry = makeRawEntry(type, resultSet);
                data.add(rawEntry);
            }

            statement.close();
        } catch (
            SQLException | IllegalAccessException | InstantiationException |
            InvocationTargetException | NullPointerException ex
        ) {
            if (SharedConstants.DEV) logger.log(Level.SEVERE, "DatabaseContext.retrieveSimpleDataForType : " + ex.getMessage());
            return null;
        }

        return data;
    }

    /**
     * Retrieves all data in database according to model class `type`, by joining tables.
     * Suitable for data that are stored in multiple tables: Preference, Project, Student, Team.
     * Data are returned in a list of delimeterized strings like data saved in text file.
     * @param type Class<T>
     * @param <T> Class
     * @return List<String>
     */
    public <T> List<String> retrieveCompositeDataForType(@NotNull Class<T> type) {
        String query = composeQueryForType(type);
        List<String> data;

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            data = makeListRawEntry(type, resultSet);
        } catch (SQLException | IllegalAccessException | InstantiationException | InvocationTargetException ex) {
            if (SharedConstants.DEV) logger.log(Level.SEVERE, "DatabaseContext.retrieveCompositeDataForType : " + ex.getMessage());
            return null;
        }

        return data;
    }

    /**
     * Retrieves all data in database according to model class `type`, by executing the provided `query`.
     * Suitable for all data types. Data are returned in a list of HashMap which has:
     * Key being the column label retrieved from query.
     * Value being the data associated with the column.
     * @param query String
     * @return List<HashMap<String, String>>
     */
    public List<HashMap<String, String>> executeDataRetrievalQuery(@NotNull String query) {
        List<HashMap<String, String>> data = new ArrayList<>();

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            ResultSetMetaData metaData = resultSet.getMetaData(); //Query result's metadata that contains column labels
            while (resultSet.next()) {
                HashMap<String, String> rowData = new HashMap<>();

                for (int i = 1; i <= metaData.getColumnCount(); i++)
                    rowData.put(
                        metaData.getColumnLabel(i), //Key is the column label
                        resultSet.getString(metaData.getColumnLabel(i)) //Value is data associated with column
                    );

                data.add(rowData);
            }
        } catch (SQLException ex) {
            if (SharedConstants.DEV) logger.log(Level.SEVERE, "DatabaseContext.executeDataRetrievalQuery : " + ex.getMessage());
            return null;
        }

        return data;
    }

    /**
     * Gets 1 row record by ID or Unique ID from a table according to model class `type`.
     * If byId == true: param `id` is the row ID and data must be retrieved by primary key.
     * If byId == false: param `id` is the Unique Id and datum is matched by unique_id column.
     * Returns a raw entry which is a delimeterized string as data stored in text file.
     * @param type Class<T>
     * @param id String
     * @param byId boolean
     * @param <T> Class
     * @return String
     */
    //This method gets data from 1 single table. Suitable for Address, Company, Role, Project Owner, TeamFitness.
    public <T> String getRawEntryForType(@NotNull Class<T> type, @NotNull String id, boolean byId) {
        String table = TABLE_NAMES.get(type.getSimpleName());
        String query = "SELECT * FROM `" + table + "` WHERE " + (byId ? "`id`" : "`unique_id`") + " = ?";

        String rawData = null;
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, id);
            ResultSet entry = statement.executeQuery();

            while (entry.next()) rawData = makeRawEntry(type, entry);
        } catch (SQLException | IllegalAccessException | InstantiationException | InvocationTargetException ex) {
            if (SharedConstants.DEV) logger.log(Level.SEVERE, "DatabaseContext.getRawEntryForType : " + ex.getMessage());
            return null;
        }

        return rawData;
    }

    /**
     * Executes a query statement against database to INSERT data. Query must be an INSERT statement.
     * Returns -1 indicating an exception, 0 indicating query failed,
     * otherwise the primary key of the inserted row.
     * @param statement PreparedStatement
     * @return int
     */
    public int executeDataInsertionQuery(@NotNull PreparedStatement statement) {
        try {
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) return 0; //Query failed

            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) return resultSet.getInt(1); //primary key of the inserted row

            return 0; //Reached only if resultSet is somehow broken, almost never
        } catch (SQLException ex) {
            if (SharedConstants.DEV) logger.log(Level.SEVERE, "DatabaseContext.executeDataInsertionQuery : " + ex.getMessage());
            return -1;
        }
    }

    /**
     * Executes a query statement against database to UPDATE or DELETE data.
     * Returns NULL indicating an exception has thrown, false for unsuccess, true for success.
     * @param statement PreparedStatement
     * @return Boolean
     */
    public Boolean executeDataModifierQuery(@NotNull PreparedStatement statement) {
        try {
            int affectedRows = statement.executeUpdate();
            return affectedRows != 0;
        } catch (SQLException ex) {
            if (SharedConstants.DEV) logger.log(Level.SEVERE, "DatabaseContext.executeDataModifierQuery : " + ex.getMessage());
            return null;
        }
    }

    /**
     * Checks if a Unique ID of a being-created instance is available to create that instance.
     * Param `type` is the model class, of which the instance is created, and in what table the data will be saved.
     * Param `uniqueId` is the Unique ID to be checked against all records in a table according to model class `type`.
     * Returns NULL indicating an exception, false indicating Unique ID is not available, otherwise true.
     * @param type Class<T>
     * @param uniqueId String
     * @param <T> Type
     * @return Boolean
     */
    public <T> Boolean isRedundantUniqueId(@NotNull Class<T> type, @NotNull String uniqueId) {
        String table = TABLE_NAMES.get(type.getSimpleName());
        String query = "SELECT * FROM `" + table + "` WHERE `unique_id` = ?";

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, uniqueId.toUpperCase());
            ResultSet resultSet = statement.executeQuery();

            return resultSet.next();
        } catch (SQLException ex) {
            if (SharedConstants.DEV) logger.log(Level.SEVERE, "DatabaseContext.isRedundantUniqueId : " + ex.getMessage());
            return null;
        }
    }

    /**
     * Precompiles a `query` string which is an SQL statement when needed to execute an on-demand query.
     * Query can be any type: SELECT, INSERT, UPDATE, DELETE, DROP, CALL, JOIN, UNION, INTERSECT...
     * Useful when needed to do a custom joining or calling an SQL function/stored procedure.
     * @param query String
     * @param action String
     * @return PreparedStatement
     */
    public PreparedStatement createStatement(@NotNull String query, @NotNull String action) {
        try {
            if (action.equals(SharedConstants.DB_INSERT))
                return connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            return connection.prepareStatement(query);
        } catch (SQLException ex) {
            if (SharedConstants.DEV) logger.log(Level.SEVERE, "DatabaseContext.createStatement : " + ex.getMessage());
            return null;
        }
    }

    /**
     * Turns the auto-commit in connection ON or OFF according to param `auto`.
     * Useful when needed to make multiple changes in database, and be able to undo changes
     * if an error occurred while changes were incomplete.
     * @param auto boolean
     * @throws SQLException
     */
    public void toggleAutoCommit(boolean auto) throws SQLException {
        connection.setAutoCommit(auto);
    }

    /**
     * Applies the changes made to database that have not been committed since the last previous commit.
     * Only used together with `toggleAutocommit` method.
     * @throws SQLException
     */
    public void saveChanges() throws SQLException {
        connection.commit();
    }

    /**
     * Discards all changes made to database since the last point auto-commit was set to off.
     * Only used together with `toggleAutocommit` method.
     * @throws SQLException
     */
    public void revertChanges() throws SQLException {
        connection.rollback();
    }

    public void close() {
        try {
            connection.close();
            context = null;
        } catch (SQLException ignored) { }
    }

    /**
     * Gets the `composeRaw` method from a Class according to model type refection.
     * Then invokes the `composeRaw` method to turn data in ResultSet into a delimeterized string.
     * @param type Class<T>
     * @param rs ResultSet
     * @param <T> Type
     * @return String
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     */
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

    /**
     * Gets the `composeRaw` method from a Class according to model type refection.
     * Then invokes the `composeRaw` method to turn data in ResultSet into a delimeterized string.
     * @param type Class<T>
     * @param rs ResultSet
     * @param <T> Type
     * @return List<String>
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     */
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

    /**
     * Gets the `composeRaw` method from a class according to model type refection.
     * The `composeRaw` method is picked up from the array containing declared method of class.
     * @param type Class<T>
     * @param <T> Type
     * @return Method
     */
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
                return "SELECT P.*, SR.`skill`, SR.`ranking` FROM `projects` P, `rankings` R, `skill_rankings` SR" +
                        "  WHERE R.`subject_id` = P.`id` AND SR.`ranking_id` = R.`id` AND R.`subject_type` = 'PROJECT';";
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
