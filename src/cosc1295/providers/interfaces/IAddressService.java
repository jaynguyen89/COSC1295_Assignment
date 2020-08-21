package cosc1295.providers.interfaces;

import cosc1295.src.models.Address;

/**
 * Dependency Injection Design Pattern
 */
public interface IAddressService {

    Address saveNewAddress(Address address);
}
