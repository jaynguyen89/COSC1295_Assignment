package cosc1295.providers.services;

import cosc1295.providers.bases.DatabaseContext;
import cosc1295.providers.bases.TextFileServiceBase;
import cosc1295.providers.interfaces.IAddressService;
import cosc1295.src.models.Address;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.DATA_TYPES;
import helpers.utilities.Helpers;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * For Dependency Injection
 */
public class AddressService extends TextFileServiceBase implements IAddressService {
    private static final Logger logger = Logger.getLogger(DatabaseContext.class.getName());

    private final DatabaseContext context;

    public AddressService() {
        context = DatabaseContext.getInstance();
    }

    /**
     * Saves a new Address into file or database according to DATA_SOURCE.
     * Returns null on exception, or the newly created Address.
     * @param address Address
     * @return Address
     */
    @Override
    public Address saveNewAddress(Address address) {
        if (SharedConstants.DATA_SOURCE.equals(TextFileServiceBase.class.getSimpleName()))
            return saveEntryToTextFile(address);

        return saveEntryToDatabase(address);
    }

    private Address saveEntryToTextFile(Address address) {
        int newInstanceId = getNextEntryIdForNewEntry(DATA_TYPES.ADDRESS);
        if (newInstanceId == -1) return null;

        address.setId(newInstanceId);
        String normalizedAddress = address.stringify();

        if (saveEntryToFile(normalizedAddress, DATA_TYPES.ADDRESS))
            return address;

        return null;
    }

    private Address saveEntryToDatabase(Address address) {
        String building = Helpers.isNullOrBlankOrEmpty(address.getBuilding()) ? null : address.getBuilding();
        String query = "INSERT INTO `addresses` (`building`, `street`, `suburb`, `state`, `post_code`, `country`) VALUES (?, ?, ?, ?, ?, ?);";

        PreparedStatement statement = context.createStatement(query, SharedConstants.DB_INSERT);
        if (statement == null) return null;

        try {
            statement.setString(1, building);
            statement.setString(2, address.getStreet());
            statement.setString(3, address.getSuburb());
            statement.setString(4, address.getState());
            statement.setString(5, address.getPostCode());
            statement.setString(6, address.getCountry());

            int result = context.executeDataInsertionQuery(statement);
            if (result <= 0) return null;

            address.setId(result);
            return address;
        } catch (SQLException ex) {
            if (SharedConstants.DEV) logger.log(Level.SEVERE, "AddressService.saveEntryToDatabase : " + ex.getMessage());
            return null;
        }
    }
}
