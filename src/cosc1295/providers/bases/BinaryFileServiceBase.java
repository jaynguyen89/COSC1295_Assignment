package cosc1295.providers.bases;

import cosc1295.designs.Flasher;
import cosc1295.src.models.Flash;
import helpers.commons.SharedConstants;
import helpers.commons.SharedEnums.DATA_TYPES;
import helpers.commons.SharedEnums.FLASH_TYPES;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class BinaryFileServiceBase<T> {

    private final Flasher flasher = Flasher.getInstance();

    private final String ASSET_PATH = System.getProperty("user.dir") + "\\src\\assets\\binaries";

    public List<T> readAllDataFromFile(Class<T> type) {
        String filePath = generateFilePathByDataType(type);
        List<T> data;

        try {
            FileInputStream fileStream = new FileInputStream(filePath);
            ObjectInputStream inputStream = new ObjectInputStream(fileStream);

            data = (ArrayList<T>) inputStream.readObject();
            inputStream.close();
        } catch (FileNotFoundException | ClassNotFoundException ex) {
            return null;
        } catch (IOException ex) {
            flasher.flash(new Flash(
                    ex.getMessage(),
                    FLASH_TYPES.ERROR
            ));

            return null;
        }

        return data;
    }

    public T getEntryFromFileById(Class<T> type, int id) {
        List<T> data = readAllDataFromFile(type);
        if (data == null) return null;

        try {
            for (T item : data) {
                Field idField = type.getDeclaredField("id");
                idField.setAccessible(true);
                int idVal = (int) idField.get(item);

                if (idVal == id) return item;
            }
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException ex) {
            flasher.flash(new Flash(
                    ex.getMessage(),
                    FLASH_TYPES.ERROR
            ));

            return null;
        }

        return null;
    }

    public boolean saveEntryToFile(Class<T> type, T entry) {
        String filePath = generateFilePathByDataType(type);

        try {
            FileOutputStream fileStream = new FileOutputStream(filePath, true);
            ObjectOutputStream outputStream = new ObjectOutputStream(fileStream) {
                protected void writeStreamHeader() throws IOException { reset(); }
            };

            outputStream.writeObject(entry);
            outputStream.close();
        } catch (IOException ex) {
            flasher.flash(new Flash(
                    ex.getMessage(),
                    FLASH_TYPES.ERROR
            ));

            return false;
        }

        return true;
    }

    public Boolean updateEntryToFile(Class<T> type, T entry) {
        String filePath = generateFilePathByDataType(type);

        List<T> data = readAllDataFromFile(type);
        if (data == null) return null;

        try {
            Field idEntry = type.getDeclaredField("id");
            idEntry.setAccessible(true);
            int idEntryVal = (int) idEntry.get(entry);

            T itemToReplace = null;
            for (T item : data) {
                Field idItem = type.getDeclaredField("id");
                idItem.setAccessible(true);
                int idItemVal = (int) idItem.get(item);

                if (idItemVal == idEntryVal) {
                    itemToReplace = item;
                    break;
                }
            }

            if (itemToReplace == null) return false;
            data.set(data.indexOf(itemToReplace), entry);
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException ex) {
            flasher.flash(new Flash(
                    ex.getMessage(),
                    FLASH_TYPES.ERROR
            ));

            return null;
        }

        try {
            FileOutputStream fileStream = new FileOutputStream(filePath);
            ObjectOutputStream outputStream = new ObjectOutputStream(fileStream);

            outputStream.writeObject(data);
            outputStream.close();
        } catch (IOException ex) {
            flasher.flash(new Flash(
                    ex.getMessage(),
                    FLASH_TYPES.ERROR
            ));

            return null;
        }

        return true;
    }

    public Boolean isRedundantUniqueId(Class<T> type, String uniqueId) {
        List<T> data = readAllDataFromFile(type);
        if (data == null) return null;

        try {
            for (T item : data) {
                Field uidItem = type.getDeclaredField("uniqueId");
                uidItem.setAccessible(true);
                String uidItemVal = (String) uidItem.get(item);

                if (uidItemVal.equals(uniqueId)) return true;
            }
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException ex) {
            flasher.flash(new Flash(
                    ex.getMessage(),
                    FLASH_TYPES.ERROR
            ));

            return null;
        }

        return false;
    }

    public int getNextEntryIdForNewEntry(Class<T> type) {
        final int UNDETERMINED_ID = -1;

        List<T> data = readAllDataFromFile(type);
        if (data == null) return UNDETERMINED_ID;

        T lastItem = data.get(data.size() - 1);
        try {
            Field idItem = type.getDeclaredField("id");
            idItem.setAccessible(true);
            int idItemVal = (int) idItem.get(lastItem);

            return ++idItemVal;
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException ex) {
            flasher.flash(new Flash(
                    ex.getMessage(),
                    FLASH_TYPES.ERROR
            ));

            return UNDETERMINED_ID;
        }
    }

    private String generateFilePathByDataType(Class<T> type) {
        String sType = type.getSimpleName().toUpperCase();

        return ASSET_PATH + (
            sType == DATA_TYPES.ADDRESS.name().replaceAll("_", SharedConstants.EMPTY_STRING) ? SharedConstants.ADDRESS_FILE_NAME
                : (sType == DATA_TYPES.COMPANY.name().replaceAll("_", SharedConstants.EMPTY_STRING) ? SharedConstants.COMPANY_FILE_NAME
                : (sType == DATA_TYPES.ROLE.name().replaceAll("_", SharedConstants.EMPTY_STRING) ? SharedConstants.ROLE_FILE_NAME
                : (sType == DATA_TYPES.PROJECT.name().replaceAll("_", SharedConstants.EMPTY_STRING) ? SharedConstants.PROJECT_FILE_NAME
                : (sType == DATA_TYPES.STUDENT.name().replaceAll("_", SharedConstants.EMPTY_STRING) ? SharedConstants.STUDENT_FILE_NAME
                : (sType == DATA_TYPES.PREFERENCE.name().replaceAll("_", SharedConstants.EMPTY_STRING) ? SharedConstants.PREFERENCE_FILE_NAME
                : SharedConstants.PROJECT_OWNER_FILE_NAME)))))
        );
    }
}
