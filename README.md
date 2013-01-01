<a href='http://www.pledgie.com/campaigns/18789'><img alt='Click here to lend your support to: ACRA - Application Crash Reports for Android and make a donation at www.pledgie.com !' src='http://www.pledgie.com/campaigns/18789.png?skin_name=chrome' border='0' /></a>

Please tell us how you use ACRA
===============================

In order to focus future developments on what is really important to you, please [take this survey](https://spreadsheets.google.com/viewform?hl=en&formkey=dDV5ek03OS1SOWNlZlBxNkFXbV9kSmc6MQ#gid=0)!

You can view the survey results [here](https://spreadsheets.google.com/spreadsheet/viewanalytics?hl=en&formkey=dDV5ek03OS1SOWNlZlBxNkFXbV9kSmc6MQ).

<a href="https://plus.google.com/118444843928759726538" rel="publisher">Follow ACRA on Google+ for latest news and tips.</a>

[![](https://ssl.gstatic.com/images/icons/gplus-32.png)](https://plus.google.com/118444843928759726538)

What is ACRA ?
==============

ACRA is a library enabling Android Application to automatically post their crash reports to a GoogleDoc form. It is targetted to android applications developers to help them get data from their applications when they crash or behave erroneously.

See [BasicSetup](acra/wiki/BasicSetup) for a step-by-step installation and usage guide.

A crash reporting feature for android apps is native since Android 2.2 (FroYo) but only available through the official Android Market (and with limited data). ACRA is a great help for Android developers :

  * [developer configurable user interaction](acra/wiki/AdvancedUsage#wiki-User_Interaction): silent reports, Toast notification, status bar notification + dialog or direct dialog
  * usable with ALL versions of android (compiled with 1.5, not tested on 1.0/1.1 but might work... but who does really care ?) and capable of retrieving data from latest versions through reflection.
  * more [detailed crash reports](acra/wiki/ReportContent) about the device running the app than what is displayed in the Android Market developer console error reports
  * you can [add your own variables content or debug traces](acra/wiki/AdvancedUsage#wiki-Adding_your_own_variables_content_or_traces_in_crash_reports) to the reports
  * you can send [error reports even if the application doesn't crash](acra/wiki/AdvancedUsage#wiki-Sending_reports_for_caught_exceptions)
  * works for any application even if not delivered through Google's Android Market => great for devices/regions where the Android Market is not available, beta releases or for enterprise private apps
  * if there is no network coverage, reports are kept and sent on a later application restart
  * can be used with [your own self-hosted report receiver script](acra/wiki/AdvancedUsage#wiki-Reports_destination)
  * google doc reports can be shared with a whole development team. Other benefits from the Google Docs platform are still to be investigated (stats, macros...)

ACRA's notification systems are clean. If a crash occurs, your application does not add user notifications over existing system's crash notifications or reporting features. If you use the Toast, Status bar notification or direct dialog modes, the "force close" dialog is not displayed anymore and devices where the system native reporting feature is enabled do not offer the user to send an additional report.

The user is notified of an error only once, and you might enhance the percieved quality of your application by defining your own texts in the notifications/dialogs.

Please do not hesitate to open defects/enhancements requests in [the issue tracker](acra/issues).

ACRA v4.4 - enforcing security
==============================

**ACRA 4.4.0 is now the official stable version.**

ACRA has been named in [this report](http://www.cs.utexas.edu/~shmat/shmat_ccs12.pdf) as a potential cause of SSL vulnerability for all android apps using it.

The truth is that, in order to let devs use alternative backends over an SSL connection with self-signed certificates, I chose to disable certificate validation in earlier versions of the lib. But this was done only on the scope of ACRA reports senders. Using ACRA did not imply that your app became unsafe for all its SSL communications.

Prior to ACRA v4.4.0, reports content were indeed vulnerable to a man in the middle attack. There "can" be some private data in there, but there are really few by default.

ACRA v4.4.0 has been modified to use SSL certificate validation by default. If you send your reports to your own server via SSL with a self-signed certificate, you have to set the option `disableSSLCertValidation` to `true` (annotation or dynamic config).

ACRA v4.3 is now STABLE
=======================

After 15 months of great service and more than 11700 downloads, it's time for v4.2.3 to bow out and live a new life among the deprecated releases.

Here's what's new in ACRA 4.3.0:

* cleaned, more stable code base, reducing reports duplicates (thanks to William Ferguson)
* new experimental and long awaited **direct dialog** interaction mode, _without notifications_ (thanks to Julia Segal)
* full **runtime configuration API**, required for projects using Android Library Projects since ADT14, and very handy for developers in need of dynamic ACRA configuration.
* addition of a collector for a custom log file
* addition of a collector for the details of the broken thread (id, name, groupname)
* addition of a collector for the new MediaCodecList provided in the Jelly Bean API

A more detailed description of the changes has been introduced in [this Google+ post](https://plus.google.com/b/118444843928759726538/118444843928759726538/posts/cnABXX7bbxV), based on the [ChangeLog](acra/wiki/ChangeLog).

If you upgrade from 4.2.3, be aware that the default list of ReportFields has changed. You would better create a new spreadsheet & form with the help of the doc/CrashReports-Template.csv or use `@ReportsCrashes(customReportContent={...})` to redefine your own list of fields.

Thanks a lot to everyone for testing during these 3 weeks of Beta (with special thanks to Nikolay Elenkov for his feedback on the dynamic configuration API), the 3 successive beta releases have reached 397 downloads on googlecode, not including Maven downloads. There has been very few reports during the Beta, a proof that you can rely on this new version even more than you could rely on the previous.

About Maven. ACRA is now available on Maven Central, with 4.2.3 and 4.3.0 stable releases available on the central repository. Just note these IDs: groupId `ch.acra` artifactId `acra`.

If you think there are missing parts in the documentation, please open an issue. 

_Kevin_

----

ACRA v4.X main new features
===========================

You can read in the [ChangeLog](http://code.google.com/p/acra/acra/wiki/ChangeLog) that many things have been added since ACRA 3.1. Here is a summary:

  * In addition to standard logcat data, reports can contain eventslog and radioevents data
  * Reports will contain the result of the "`adb shell dumpsys meminfo <pid>`" command which gives details about your application memory usage right after the crash.
  * Introduction of an abstraction layer for report senders. This allows to:
    * use the `formUri` parameter to send reports to your custom server script with POST parameters names not related to Google Forms naming. POST parameters will have easy to understand names.
    * introduce a new report sending mode: email (see below)
    * create your own custom report senders. There is now a simple public interface allowing you to code your own class in charge of handling report data. Your sender(s) can be added to default senders or replace them.
  * Reports can now be sent via email (through an `ACTION_SEND` intent so the user has to choose the email client he wants to use and then send the email containing report fields in the body). The list of report fields included is configurable. This allows to get rid of the `INTERNET` permission in apps where it does not make any sense.
  * Custom report receiver server scripts can be secured with basic http authentication (login/password can be configured in ACRA)
  * If the `READ_PHONE_STATE` permission is granted, reports include the Unique Device Identifier (IMEI). This can be really useful for enterprise applications deployment.

-----

And after that?
===============

Now that ACRA is stabilized on the device side (there shouldn't be much more data required...), the effort should be placed on crash data analysis and reports management tools for developers.

You can look at [some contributions](acra/wiki/Contribs) that have already been published. Most of them are work in progress, so if you feel like joining the effort, please do!
