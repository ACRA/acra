package org.acra.util;

/**
 * Helps to construct objects via reflection.
 */
public final class ReflectionHelper {

    public Object create(String className) throws ReflectionException {
        try {
            final Class clazz = Class.forName(className);
            return clazz.newInstance();
        } catch (ClassNotFoundException e) {
            throw new ReflectionException("Could not find class : " + className, e);
        } catch (InstantiationException e) {
            throw new ReflectionException("Could not instantiate class : " + className, e);
        } catch (IllegalAccessException e) {
            throw new ReflectionException("Could not access class : " + className, e);
        }
    }
}
