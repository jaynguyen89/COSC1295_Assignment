package cosc1295.providers.bases;

/**
 * Singleton class that provides other classes the access to services.
 */
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

    /**
     * Gets an instance of a service according to the class type.
     * @param type <T>
     * @param <T> Type
     * @return <T>
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public <T> T getService(Class<T> type)
        throws
            IllegalAccessException,
            InstantiationException
    {
        return type.newInstance();
    }
}
