package org.acra.log;

/**
 * Responsible for providing ACRA classes with a platform neutral way of logging.
 * <p>
 *     One reason for using this mechanism is to allow ACRA classes to use a logging system,
 *     but be able to execute in a test environment outside of an Android JVM.
 * </p>
 * @author William Ferguson
 * @since 4.3.0
 */
public interface ACRALog {
    public int v(java.lang.String tag, java.lang.String msg);
    public int v(java.lang.String tag, java.lang.String msg, java.lang.Throwable tr);
    public int d(java.lang.String tag, java.lang.String msg);
    public int d(java.lang.String tag, java.lang.String msg, java.lang.Throwable tr);
    public int i(java.lang.String tag, java.lang.String msg);
    public int i(java.lang.String tag, java.lang.String msg, java.lang.Throwable tr);
    public int w(java.lang.String tag, java.lang.String msg);
    public int w(java.lang.String tag, java.lang.String msg, java.lang.Throwable tr);
    //public native  boolean isLoggable(java.lang.String tag, int level);
    public int w(java.lang.String tag, java.lang.Throwable tr);
    public int e(java.lang.String tag, java.lang.String msg);
    public int e(java.lang.String tag, java.lang.String msg, java.lang.Throwable tr);
    public java.lang.String getStackTraceString(java.lang.Throwable tr);
    //public native  int println(int priority, java.lang.String tag, java.lang.String msg);
}
