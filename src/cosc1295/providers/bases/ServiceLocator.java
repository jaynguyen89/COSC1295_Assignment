package cosc1295.providers.bases;

public final class ServiceLocator {

    private static ServiceLocator locator;

    private ServiceLocator() { }

    public static ServiceLocator getInstance() {
        if (locator == null) {
            synchronized (ServiceLocator.class) {
                locator = locator == null ?
                        new ServiceLocator() :
                        locator;
            }
        }

        return locator;
    }

    public <T> T getService(Class<T> type)
        throws
            IllegalAccessException,
            InstantiationException
    {
        return type.newInstance();
    }
}
