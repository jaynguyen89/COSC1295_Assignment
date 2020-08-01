package cosc1295.src.views;

import cosc1295.designs.Flasher;
import cosc1295.src.models.Address;
import cosc1295.src.models.Flash;
import helpers.commons.SharedEnums;

import java.util.Scanner;

public class AddressView {

    private final Flasher flasher = Flasher.getInstance();
    private final Scanner inputScanner;

    public AddressView() {
        inputScanner = new Scanner(System.in);
    }

    /**
     * Gets user inputs to create a new Address.
     * @return Address
     */
    public Address getAddressDetails() {
        Address newAddress = new Address();

        int addressFieldTracker = 0;
        while (addressFieldTracker < 6)
            switch (addressFieldTracker) {
                case 0: //Getting building name/number
                    flasher.flash(new Flash("Address - Building Address: (press enter to skip)", SharedEnums.FLASH_TYPES.NONE));

                    newAddress.setBuilding(inputScanner.nextLine());
                    newAddress.prettifyBuilding(); //No need to validate this input

                    addressFieldTracker++;
                    break;
                case 1: //Getting streetAddress
                    flasher.flash(new Flash("Address - Street Address: ", SharedEnums.FLASH_TYPES.NONE));

                    newAddress.setStreet(inputScanner.nextLine());
                    if (!newAddress.validateAndPrettifyStreet()) {
                        flasher.flash(new Flash("Street Address cannot be empty!\n", SharedEnums.FLASH_TYPES.ERROR));
                        continue;
                    }

                    addressFieldTracker++;
                    break;
                case 2: //Getting suburb
                    flasher.flash(new Flash("Address - Suburb: ", SharedEnums.FLASH_TYPES.NONE));

                    newAddress.setSuburb(inputScanner.nextLine());
                    if (!newAddress.validateAndPrettifySuburb()) {
                        flasher.flash(new Flash("Suburb cannot be empty!\n", SharedEnums.FLASH_TYPES.ERROR));
                        continue;
                    }

                    addressFieldTracker++;
                    break;
                case 3: //Getting state
                    flasher.flash(new Flash("Address - State: ", SharedEnums.FLASH_TYPES.NONE));

                    newAddress.setState(inputScanner.nextLine());
                    if (!newAddress.validateAndPrettifyState()) {
                        flasher.flash(new Flash("State cannot be empty!\n", SharedEnums.FLASH_TYPES.ERROR));
                        continue;
                    }

                    addressFieldTracker++;
                    break;
                case 4: //Getting postCode
                    flasher.flash(new Flash("Address - Postcode: ", SharedEnums.FLASH_TYPES.NONE));

                    newAddress.setPostCode(inputScanner.nextLine());
                    if (!newAddress.validateAndPrettifyPostCode()) {
                        flasher.flash(new Flash("Postal Code cannot be empty!\n", SharedEnums.FLASH_TYPES.ERROR));
                        continue;
                    }

                    addressFieldTracker++;
                    break;
                default: //Getting country
                    flasher.flash(new Flash("Address - Country: ", SharedEnums.FLASH_TYPES.NONE));

                    newAddress.setCountry(inputScanner.nextLine());
                    if (!newAddress.validateAndPrettifyCountry()) {
                        flasher.flash(new Flash("Country cannot be empty!\n", SharedEnums.FLASH_TYPES.ERROR));
                        continue;
                    }

                    addressFieldTracker++;
                    break;
            }

        return newAddress;
    }
}
