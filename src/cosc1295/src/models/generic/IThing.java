package cosc1295.src.models.generic;

import helpers.commons.SharedConstants;

public interface IThing {

    int id = 0;

    void setId(int id);

    int getId();

    String uniqueId = SharedConstants.EMPTY_STRING;

    void setUniqueId(String uniqueId);

    String getUniqueId();

    Boolean isUniqueIdAvailable();

    String stringify();
}
