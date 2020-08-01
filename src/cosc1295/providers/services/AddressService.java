package cosc1295.providers.services;

import cosc1295.providers.bases.TextFileServiceBase;
import cosc1295.providers.interfaces.IAddressService;
import cosc1295.src.models.Address;
import helpers.commons.SharedEnums.DATA_TYPES;

/**
 * For Dependency Injection
 */
public class AddressService extends TextFileServiceBase implements IAddressService {

    @Override
    public Address writeAddressToFile(Address address) {
        int newInstanceId = getNextInstanceIdForNewEntry(DATA_TYPES.ADDRESS);
        if (newInstanceId == -1) return null;

        address.setId(newInstanceId);
        String normalizedAddress = address.stringify();

        if (writeToFile(normalizedAddress, DATA_TYPES.ADDRESS))
            return address;

        return null;
    }
}
