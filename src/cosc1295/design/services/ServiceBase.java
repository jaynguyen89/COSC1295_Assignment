package cosc1295.design.services;

import cosc1295.src.controllers.FlashController;
import cosc1295.src.models.Flash;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums;
import helpers.commons.SharedEnums.DATA_TYPES;

import java.io.*;

class ServiceBase {

    private final FlashController flashController = FlashController.getInstance();

    protected final String ASSET_PATH = System.getProperty("user.dir") + "\\src\\assets\\";

    protected boolean writeToFile(String any, DATA_TYPES type) {
        String filePath = generateFilePathByDataType(type);

        File fileToWrite = new File(filePath);

        if (!fileToWrite.exists())
            try {
                fileToWrite.createNewFile();
            } catch (IOException ex) {
                flashController.flash(new Flash(
                        ex.getMessage(),
                        SharedEnums.FLASH_TYPES.ERROR
                ));

                return false;
            }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileToWrite));

            writer.write(any);
            writer.close();
        } catch (FileNotFoundException ex) {
            return false;
        } catch (IOException ex) {
            flashController.flash(new Flash(
                    ex.getMessage(),
                    SharedEnums.FLASH_TYPES.ERROR
            ));

            return false;
        }


        return true;
    }

    protected int getNextInstanceIdForNewEntry(DATA_TYPES type) {
        final int UNDETERMINED_ID = -1;
        String filePath = generateFilePathByDataType(type);

        File fileToRead = new File(filePath);

        if (!fileToRead.exists()) return 1; //No data ever saved, so ID starts at 1
        if (!fileToRead.canRead()) return UNDETERMINED_ID;

        String lastEntry = SharedConstants.EMPTY_STRING;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileToRead));

            String currentEntry;
            while ((currentEntry = reader.readLine()) != null)
                lastEntry = currentEntry;

            reader.close();
        } catch (FileNotFoundException ex) {
            return 1; //No data ever saved, so ID starts at 1
        } catch (IOException ex) {
            flashController.flash(new Flash(
                    ex.getMessage(),
                    SharedEnums.FLASH_TYPES.ERROR
            ));

            return UNDETERMINED_ID;
        }

        String[] tokens = lastEntry.split(SharedConstants.TEXT_DELIMITER);
        int currentEntryId = Integer.parseInt(tokens[0]);

        return ++currentEntryId;
    }

    private String generateFilePathByDataType(DATA_TYPES type) {
        return ASSET_PATH + (
            type == DATA_TYPES.ADDRESS ? SharedConstants.ADDRESS_FILE_NAME
                    : SharedConstants.COMPANY_FILE_NAME
        );
    }
}
