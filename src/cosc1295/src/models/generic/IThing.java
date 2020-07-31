package cosc1295.src.models.generic;

import helpers.commons.SharedConstants;

public interface IThing {

    int id = 0;

    public void setId(int id);

    public int getId();

    String uniqueId = SharedConstants.EMPTY_STRING;

    public void setUniqueId(String uniqueId);

    public String getUniqueId();

    public Boolean isUniqueIdAvailable();
}
