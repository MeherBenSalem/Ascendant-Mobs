package tn.nightbeam.rpgmoblevelingsystem.platform;

import tn.nightbeam.rpgmoblevelingsystem.Constants;
import tn.nightbeam.rpgmoblevelingsystem.platform.services.IPlatformHelper;

import java.util.ServiceLoader;

public final class Services {
    private static volatile IPlatformHelper platform;

    private Services() {
    }

    public static IPlatformHelper platform() {
        IPlatformHelper loaded = platform;
        if (loaded == null) {
            synchronized (Services.class) {
                loaded = platform;
                if (loaded == null) {
                    loaded = load(IPlatformHelper.class);
                    platform = loaded;
                }
            }
        }
        return loaded;
    }

    public static <T> T load(Class<T> clazz) {
        T service = ServiceLoader.load(clazz, clazz.getClassLoader())
                .findFirst()
                .orElseGet(() -> createFallback(clazz));
        Constants.LOG.debug("Loaded {} for service {}", service, clazz.getName());
        return service;
    }

    @SuppressWarnings("unchecked")
    private static <T> T createFallback(Class<T> clazz) {
        if (clazz == IPlatformHelper.class) {
            Constants.LOG.warn("No {} service provider found; using fallback implementation", clazz.getName());
            return (T) new FallbackPlatformHelper();
        }
        throw new IllegalStateException("Failed to load service for " + clazz.getName());
    }
}
