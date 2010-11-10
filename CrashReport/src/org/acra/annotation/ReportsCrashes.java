package org.acra.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.acra.ReportingInteractionMode;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ReportsCrashes {
    /**
     * The id of the Google Doc form.
     * @return
     */
    String formId();
    /**
     * The Uri of your own server side script that will receive reports.
     * @return
     */
    String formUri() default "";
    /**
     * The interaction mode you want to implement.
     * @return
     */
    ReportingInteractionMode mode() default ReportingInteractionMode.SILENT;
    
    /**
     * Resource id for the user comment input label in the crash dialog. If not
     * provided, disables the input field.
     */
    int resDialogCommentPrompt() default 0;
    /**
     * Resource id for the icon in the crash dialog.
     */ 
    int resDialogIcon() default android.R.drawable.ic_dialog_alert;
    /**
     * Resource id for the Toast text triggered when the user accepts to send a
     * report in the crash dialog.
     */
    int resDialogOkToast() default 0;
    /**
     * Resource id for the text in the crash dialog.
     */
    int resDialogText() default 0;
    /**
     * Resource id for the title in the crash dialog.
     */
    int resDialogTitle() default 0;
    /**
     * Resource id for the icon in the status bar notification.
     */
    int resNotifIcon() default 0;
    /**
     * Resource id for the text in the status bar notification.
     */ 
    int resNotifText() default 0;
    /**
     * Resource id for the ticker text in the status bar notification.
     */
    int resNotifTickerText() default 0;
    /**
     * Resource id for the title in the status bar notification.
     */
    int resNotifTitle() default 0;
    /**
     * Resource id for the Toast text triggered when the application crashes if
     * the notification+dialog mode is not used.
     */
    int resToastText() default 0;
}
