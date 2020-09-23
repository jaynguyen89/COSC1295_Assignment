package helpers.commons;

import cosc1295.providers.bases.DatabaseContext;
import cosc1295.providers.bases.TextFileServiceBase;
import helpers.commons.SharedEnums.GUI_ACTION_CONTEXT;

import java.util.HashMap;

/**
 * All the constants values that will be used in the whole project.
 */
public final class SharedConstants {

    public static final String DATA_ENV = "local"; //"server" or "local"

    public static final String DATA_SOURCE = DatabaseContext.class.getSimpleName();//TextFileServiceBase.class.getSimpleName();

    public static final String DB_INSERT = "INSERT";

    public static final String DB_UPDATE = "UPDATE";

    public static final String DB_DELETE = "DELETE";

    public static final String EMPTY_STRING = "";

    public static final String MULTIPLE_SPACE = " +";

    public static final String SPACE = " ";

    public static final String TEXT_DELIMITER = ",,";

    public static final String NA = "N/A";

    public static final int DECIMAL_PRECISION = 2;

    public static final int GROUP_LIMIT = 4; //Number of students in a group

    public static final int MAX_CONFLICTERS = 2;

    public static final int MAX_PREFERENCE = 4;

    public static final int SHORTLISTED_NUM = 5;

    public static final String ACTION_SWAP = "SWAP";

    public static final String ACTION_ASSIGN = "ASSIGN";

    public static final String ADDRESS_FILE_NAME = "addresses.txt";

    public static final String COMPANY_FILE_NAME = "companies.txt";

    public static final String ROLE_FILE_NAME = "roles.txt";

    public static final String PROJECT_OWNER_FILE_NAME = "project_owners.txt";

    public static final String PROJECT_FILE_NAME = "projects.txt";

    public static final String STUDENT_FILE_NAME = "student_info.txt";

    public static final String PREFERENCE_FILE_NAME = "preferences.txt";

    public static final String PROJECT_TEAM_FILE_NAME = "project_teams.txt";

    public static final String TEAM_FITNESS_METRICS_FILE_NAME = "fitness_metrics.txt";

    public static final double GUI_ASPECT_RATIO = 16/9.0;

    public static final HashMap<String, Integer> DIMENSIONS =
            new HashMap<String, Integer>() {{
                put("WIDTH", 896);
                put("HEIGHT", 504);
                put("MAX_WIDTH", 1024);
                put("MAX_HEIGHT", 576);
            }};

    public static final HashMap<GUI_ACTION_CONTEXT, String> RESOURCES =
            new HashMap<GUI_ACTION_CONTEXT, String>() {{
                put(GUI_ACTION_CONTEXT.LAUNCH, "onlaunch-styles.css");
                put(GUI_ACTION_CONTEXT.ASSIGN, "onassign-styles.css");
                put(GUI_ACTION_CONTEXT.SWAP, "onswap-styles.css");
                put(GUI_ACTION_CONTEXT.REMOVE, "onremove-styles.css");
                put(GUI_ACTION_CONTEXT.PROJECT, "onproject-styles.css");
                put(GUI_ACTION_CONTEXT.STATS, "onstats-styles.css");
            }};
}
