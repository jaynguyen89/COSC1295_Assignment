package cosc1295.src.views;

import cosc1295.src.controllers.FlashController;
import cosc1295.src.models.Address;
import cosc1295.src.models.Flash;
import helpers.commons.SharedEnums;

import java.util.Scanner;

public class AddressView {

    private final FlashController flashController = FlashController.getInstance();
    private Scanner inputScanner;

    public AddressView() {
        inputScanner = new Scanner(System.in);
    }

    public Address getAddressDetails() {
        Address newAddress = new Address();

        int addressFieldTracker = 0;
        while (addressFieldTracker < 6)
            switch (addressFieldTracker) {
                case 0:
                    flashController.flash(new Flash("Address - Building Address: (press enter to skip)", SharedEnums.FLASH_TYPES.NONE));

                    newAddress.setBuilding(inputScanner.nextLine());
                    newAddress.prettifyBuilding();

                    addressFieldTracker++;
                    break;
                case 1:
                    flashController.flash(new Flash("Address - Street Address: ", SharedEnums.FLASH_TYPES.NONE));

                    newAddress.setStreet(inputScanner.nextLine());
                    if (!newAddress.validateAndPrettifyStreet()) {
                        flashController.flash(new Flash("Street Address cannot be empty!\n", SharedEnums.FLASH_TYPES.ERROR));
                        continue;
                    }

                    addressFieldTracker++;
                    break;
                case 2:
                    flashController.flash(new Flash("Address - Suburb: ", SharedEnums.FLASH_TYPES.NONE));

                    newAddress.setSuburb(inputScanner.nextLine());
                    if (!newAddress.validateAndPrettifySuburb()) {
                        flashController.flash(new Flash("Suburb cannot be empty!\n", SharedEnums.FLASH_TYPES.ERROR));
                        continue;
                    }

                    addressFieldTracker++;
                    break;
                case 3:
                    flashController.flash(new Flash("Address - State: ", SharedEnums.FLASH_TYPES.NONE));

                    newAddress.setState(inputScanner.nextLine());
                    if (!newAddress.validateAndPrettifyState()) {
                        flashController.flash(new Flash("State cannot be empty!\n", SharedEnums.FLASH_TYPES.ERROR));
                        continue;
                    }

                    addressFieldTracker++;
                    break;
                case 4:
                    flashController.flash(new Flash("Address - Postcode: ", SharedEnums.FLASH_TYPES.NONE));

                    newAddress.setPostCode(inputScanner.nextLine());
                    if (!newAddress.validateAndPrettifyPostCode()) {
                        flashController.flash(new Flash("Postal Code cannot be empty!\n", SharedEnums.FLASH_TYPES.ERROR));
                        continue;
                    }

                    addressFieldTracker++;
                    break;
                default:
                    flashController.flash(new Flash("Address - Country: ", SharedEnums.FLASH_TYPES.NONE));

                    newAddress.setCountry(inputScanner.nextLine());
                    if (!newAddress.validateAndPrettifyCountry()) {
                        flashController.flash(new Flash("Country cannot be empty!\n", SharedEnums.FLASH_TYPES.ERROR));
                        continue;
                    }

                    addressFieldTracker++;
                    break;
            }

        return newAddress;
    }
}
