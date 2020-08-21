package cosc1295.src.controllers;

import cosc1295.providers.interfaces.IAddressService;
import cosc1295.providers.services.AddressService;
import cosc1295.src.models.Address;
import cosc1295.src.views.AddressView;

public class AddressController extends ControllerBase {

    private final AddressView addressView;
    private final IAddressService addressService;

    public AddressController() {
        addressView = new AddressView();
        addressService = new AddressService();
    }

    public Address promptForAddressInformation() {
        return addressView.getAddressDetails();
    }

    public Address saveNewAddress(Address address) {
        return addressService.saveNewAddress(address);
    }
}
