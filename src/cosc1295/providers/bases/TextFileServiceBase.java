package cosc1295.providers.bases;

import cosc1295.designs.Flasher;
import cosc1295.src.models.Flash;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums;
import helpers.commons.SharedEnums.DATA_TYPES;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TextFileServiceBase {

    private final Flasher flasher = Flasher.getInstance();

    private final String ASSET_PATH = System.getProperty("user.dir") + "\\src\\assets\\texts\\";

    public List<String> readEntireRawDataFromFile(DATA_TYPES type) {
        String filePath = generateFilePathByDataType(type);
        File fileToRead = new File(filePath);

        if (!fileToRead.exists()) return new ArrayList<>();
        if (!fileToRead.canRead()) return null;

        List<String> rawData = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileToRead));

            String currentEntry;
            while ((currentEntry = reader.readLine()) != null)
                rawData.add(currentEntry);

            reader.close();
        } catch (FileNotFoundException ex) {
            return null;
        } catch (IOException ex) {
            flasher.flash(new Flash(
                    ex.getMessage(),
                    SharedEnums.FLASH_TYPES.ERROR
            ));

            return null;
        }

        return rawData;
    }

    public String lookupRawDataFromFileById(String id, DATA_TYPES type) {
        String filePath = generateFilePathByDataType(type);
        File fileToRead = new File(filePath);

        if (!fileToRead.exists()) return SharedConstants.EMPTY_STRING;
        if (!fileToRead.canRead()) return null;

        String entryInNeed = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileToRead));

            boolean found = false;
            while (!found) {
                entryInNeed = reader.readLine();
                if (entryInNeed == null) {
                    entryInNeed = SharedConstants.NA;
                    found = true;
                }

                try {
                    String[] tokens = entryInNeed.split(SharedConstants.TEXT_DELIMITER);
                    if (tokens[0].trim().equals(id)) found = true;
                } catch (IndexOutOfBoundsException ex) {
                    flasher.flash(new Flash(
                        "An error occurred while parsing file data for to search for an entry. " +
                                "The current failure entry will be skipped.",
                        SharedEnums.FLASH_TYPES.ERROR
                    ));
                }
            }

            reader.close();
        } catch (FileNotFoundException ex) {
            return null;
        } catch (IOException ex) {
            flasher.flash(new Flash(
                    ex.getMessage(),
                    SharedEnums.FLASH_TYPES.ERROR
            ));

            return null;
        }

        return entryInNeed;
    }

    public boolean writeToFile(String any, DATA_TYPES type) {
        String filePath = generateFilePathByDataType(type);
        File fileToWrite = new File(filePath);

        if (!fileToWrite.exists())
            try {
                fileToWrite.createNewFile();
            } catch (IOException ex) {
                flasher.flash(new Flash(
                        ex.getMessage(),
                        SharedEnums.FLASH_TYPES.ERROR
                ));

                return false;
            }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileToWrite, true));
            PrintWriter printer = new PrintWriter(writer);

            printer.println(any);
            printer.close();
            writer.close();
        } catch (FileNotFoundException ex) {
            return false;
        } catch (IOException ex) {
            flasher.flash(new Flash(
                    ex.getMessage(),
                    SharedEnums.FLASH_TYPES.ERROR
            ));

            return false;
        }

        return true;
    }

    public boolean updateEntryToFileById(String thingToUpdate, int id, DATA_TYPES type) {
        String filePath = generateFilePathByDataType(type);
        File fileToUpdate = new File(filePath);

        if (!fileToUpdate.exists() || !fileToUpdate.canRead()) {
            flasher.flash(new Flash("File not found or Insufficient WRITE permission.\n", SharedEnums.FLASH_TYPES.ERROR));
            return false;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileToUpdate));
            StringBuffer lineBuffer = new StringBuffer();
            String lineToUpdate;

            while ((lineToUpdate = reader.readLine()) != null) {
                String[] tokens = lineToUpdate.split(SharedConstants.TEXT_DELIMITER);
                if (tokens[0].equals(String.valueOf(id)))
                    lineToUpdate = thingToUpdate;

                lineBuffer.append(lineToUpdate);
                lineBuffer.append("\n");
            }

            reader.close();

            FileOutputStream outputStream = new FileOutputStream(filePath);
            outputStream.write(lineBuffer.toString().getBytes());

            outputStream.close();
        } catch (IOException e) {
            flasher.flash(new Flash("An error occurred while updating new data to file.\n", SharedEnums.FLASH_TYPES.ERROR));
            return false;
        }

        return true;
    }

    public boolean isRedundantUniqueId(String uniqueId, DATA_TYPES type) {
        String filePath = generateFilePathByDataType(type);
        File fileToCheck = new File(filePath);

        if (!fileToCheck.exists()) return false;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileToCheck));

            String currentEntry;
            while ((currentEntry = reader.readLine()) != null) {
                String[] tokens = currentEntry.split(SharedConstants.TEXT_DELIMITER);
                if (tokens[1].equalsIgnoreCase(uniqueId))
                    return true;
            }

            reader.close();
        } catch (IOException | IndexOutOfBoundsException ex) {
            flasher.flash(new Flash(
                "Error occurred while reading file: " + ex.getMessage(),
                SharedEnums.FLASH_TYPES.ERROR
            ));
        }

        return false;
    }

    public int getNextInstanceIdForNewEntry(DATA_TYPES type) {
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
            flasher.flash(new Flash(
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
                    : (type == DATA_TYPES.COMPANY ? SharedConstants.COMPANY_FILE_NAME
                    : (type == DATA_TYPES.ROLE ? SharedConstants.ROLE_FILE_NAME
                    : (type == DATA_TYPES.PROJECT ? SharedConstants.PROJECT_FILE_NAME
                    : (type == DATA_TYPES.STUDENT ? SharedConstants.STUDENT_FILE_NAME
                    : SharedConstants.PROJECT_OWNER_FILE_NAME))))
        );
    }
}
