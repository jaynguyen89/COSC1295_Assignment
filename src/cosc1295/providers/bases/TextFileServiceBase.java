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

    /**
     * Reads all lines in a file into a list of strings then returns.
     * @param type DATA_TYPES
     * @return List<String>
     */
    public List<String> readAllDataFromFile(DATA_TYPES type) {
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

    /**
     * Search for a line in file with the `id` according to DATA_TYPES.
     * Return that line to the Service accordingly.
     * @param id String
     * @param type DATA_TYPES
     * @return String
     */
    public String getEntryFromFileById(String id, DATA_TYPES type) {
        String filePath = generateFilePathByDataType(type);
        File fileToRead = new File(filePath);

        if (!fileToRead.exists()) return SharedConstants.EMPTY_STRING;
        if (!fileToRead.canRead()) return null;

        String entryInNeed = SharedConstants.EMPTY_STRING;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileToRead));

            boolean found = false;
            while (!found) {
                entryInNeed = reader.readLine();
                if (entryInNeed == null) {
                    entryInNeed = SharedConstants.EMPTY_STRING;
                    found = true;
                    continue;
                }

                try {
                    String[] tokens = entryInNeed.split(SharedConstants.TEXT_DELIMITER);
                    if (tokens[0].trim().equals(id)) found = true;
                } catch (IndexOutOfBoundsException ex) {
                    flasher.flash(new Flash(
                        "An error occurred while parsing file data to search for an entry. " +
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

    /**
     * Writes any strings that is given to this method into a file according to DATA_TYPES.
     * Returns false on exception or error, otherwise, returns true on success.
     * @param any String
     * @param type DATA_TYPES
     * @return boolean
     */
    public boolean saveEntryToFile(String any, DATA_TYPES type) {
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

    /**
     * Edits (replaces) a line in a file according to DATA_TYPES. Searches for the line by `id`,
     * replaces it when found. Returns false on exception or error, true on success.
     * @param thingToUpdate String
     * @param id int
     * @param type DATA_TYPES
     * @return boolean
     */
    public boolean updateEntryToFileById(String thingToUpdate, int id, DATA_TYPES type) {
        String filePath = generateFilePathByDataType(type);
        File fileToUpdate = new File(filePath);

        if (!fileToUpdate.exists())
            try {
                fileToUpdate.createNewFile();
            } catch (IOException ex) {
                flasher.flash(new Flash(
                        ex.getMessage(),
                        SharedEnums.FLASH_TYPES.ERROR
                ));

                return false;
            }

        if (!fileToUpdate.canRead()) {
            flasher.flash(new Flash("Insufficient WRITE permission.\n", SharedEnums.FLASH_TYPES.ERROR));
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

    /**
     * Taking the uniqueId, check a file according to DATA_TYPES for a line that contains the uniqueId.
     * If found, returns true to indicate a redundancy, otherwise false.
     * @param uniqueId String
     * @param type DATA_TYPES
     * @return boolean
     */
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

    /**
     * Reads a file according to DATA_TYPES, looks for the max `id` of all entries,
     * then returns the `id` for the next entry.
     * @param type DATA_TYPES
     * @return int
     */
    public int getNextEntryIdForNewEntry(DATA_TYPES type) {
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

    public Boolean removeEntryFromFileById(String id, DATA_TYPES type) {
        String filePath = generateFilePathByDataType(type);
        File fileToRead = new File(filePath);

        if (!fileToRead.exists()) return false;
        if (!fileToRead.canRead()) return null;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileToRead));

            File tempFile = new File(ASSET_PATH + "temp.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
            PrintWriter printer = new PrintWriter(writer);

            String currentEntry;
            while ((currentEntry = reader.readLine()) != null) {
                String[] tokens = currentEntry.split(SharedConstants.TEXT_DELIMITER);
                if (tokens[0].trim().equalsIgnoreCase(id)) continue;

                printer.println(currentEntry);
            }

            printer.close();
            writer.close();
            reader.close();

            if (fileToRead.delete()) return tempFile.renameTo(fileToRead);
        } catch (IOException ex) {
            return null;
        }

        return false;
    }

    /**
     * Creates the file path for reading a file according to DATA_TYPES.
     * @param type DATA_TYPES
     * @return String
     */
    private String generateFilePathByDataType(DATA_TYPES type) {
        return ASSET_PATH + (
            type == DATA_TYPES.ADDRESS ? SharedConstants.ADDRESS_FILE_NAME
                    : (type == DATA_TYPES.COMPANY ? SharedConstants.COMPANY_FILE_NAME
                    : (type == DATA_TYPES.ROLE ? SharedConstants.ROLE_FILE_NAME
                    : (type == DATA_TYPES.PROJECT ? SharedConstants.PROJECT_FILE_NAME
                    : (type == DATA_TYPES.STUDENT ? SharedConstants.STUDENT_FILE_NAME
                    : (type == DATA_TYPES.PREFERENCE ? SharedConstants.PREFERENCE_FILE_NAME
                    : (type == DATA_TYPES.PROJECT_TEAM ? SharedConstants.PROJECT_TEAM_FILE_NAME
                    : (type == DATA_TYPES.FITNESS_METRICS ? SharedConstants.TEAM_FITNESS_METRICS_FILE_NAME
                    : SharedConstants.PROJECT_OWNER_FILE_NAME)))))))
        );
    }
}
