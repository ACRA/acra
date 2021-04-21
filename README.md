
||Current Status|
|---|---|
|Build|[ ![test](https://github.com/ACRA/acra/workflows/test/badge.svg?branch=master) ](https://github.com/ACRA/acra/actions?query=workflow%3Atest)|
|Maven Central|[![Maven Central](https://img.shields.io/maven-central/v/ch.acra/acra-core.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22ch.acra%22)|
|Android Versions|![minVersion](https://img.shields.io/badge/dynamic/json.svg?label=Minimal%20Android%20Version&colorB=FF6F00&query=version&uri=https%3A%2F%2Ffaendir.com%2Fandroid%2Facra.php%3Fversion%3DandroidMinVersion) ![targetVersion](https://img.shields.io/badge/dynamic/json.svg?label=Target%20Android%20Version&colorB=64DD17&query=version&uri=https%3A%2F%2Ffaendir.com%2Fandroid%2Facra.php%3Fversion%3DandroidVersion)|
|License|![license](https://img.shields.io/github/license/ACRA/acra.svg)|
| Statistics|[![AppBrain stats](https://www.appbrain.com/stats/libraries/shield/acra.svg)](https://www.appbrain.com/stats/libraries/details/acra/acra)|
|Donations|[![Flattr this project](http://api.flattr.com/button/flattr-badge-large.png)](https://flattr.com/submit/auto?user_id=kevingaudin&url=http://acra.ch&title=ACRA%20-%20Application%20Crash%20Reports%20for%20Android&language=&tags=opensource%2Candroid&category=software&description=ACRA%20%28Application%20Crash%20Reports%20for%20Android%29%20is%20an%20open%20source%20android%20library%20for%20developers%2C%20enabling%20their%20apps%20to%20send%20detailed%20reports%20when%20they%20crash.)|

What is ACRA ?
==============

ACRA is a library enabling Android applications to automatically post their crash reports to a report server. It is targeted to android applications developers to help them get data from their applications when they crash or behave erroneously.

ACRA is used in 1.57% ([See AppBrain/stats](https://www.appbrain.com/stats/libraries/details/acra/acra)) of all apps on Google Play as of June 2020. That's over **13 thousand apps** and over **5 billion downloads** including ACRA.

See [Setup](https://www.acra.ch/docs/Setup) for a step-by-step installation and usage guide.

A crash reporting feature for android apps is native since Android 2.2 (FroYo) but only available through the official Android Market (and with limited data). ACRA is a great help for Android developers:

  * [developer configurable user interaction](https://www.acra.ch/docs/Interactions): silent reports, Toast notification, status bar notification or dialog
  * usable with ALL versions of Android supported by the official support libraries.
  * more [detailed crash reports](https://www.acra.ch/javadoc/latest/org/acra/ReportField.html) about the device running the app than what is displayed in the Android Market developer console error reports
  * you can [add your own variables content or debug traces](https://www.acra.ch/docs/AdvancedUsage#adding-your-own-custom-variables-or-traces-in-crash-reports-breadcrumbs) to the reports
  * you can send [error reports even if the application doesn't crash](https://www.acra.ch/docs/AdvancedUsage#sending-reports-for-caught-exceptions-or-for-unexpected-application-state-without-any-exception)
  * works for any application even if not delivered through Google Play => great for devices/regions where the Google Play is not available, beta releases or for enterprise private apps
  * if there is no network coverage, reports are kept and sent on a later application restart
  * can be used with [your own self-hosted report receiver script](https://www.acra.ch/docs/Senders)

ACRA's notification systems are clean. If a crash occurs, your application does not add user notifications over existing system's crash notifications or reporting features. By default, the "force close" dialog is not displayed anymore, to enable it set `alsoReportToAndroidFramework` to `true`.

The user is notified of an error only once, and you might enhance the perceived quality of your application by defining your own texts in the notifications/dialogs.

Please do not hesitate to open defects/enhancements requests in [the issue tracker](https://github.com/ACRA/acra/issues).

Latest version
===========================================

For the latest version and a complete changelog, please see the [Release page](https://github.com/ACRA/acra/releases).

For migrating from 4.x, please see our [Migration guide](https://github.com/ACRA/acra/wiki/Migrating) in the Wiki.

Backends
========

[Acrarium](https://github.com/F43nd1r/Acrarium) is the official backend for report storage and analysis. Acrarium is in active development and has recently reached stable release.

[Acralyzer](https://github.com/ACRA/acralyzer) was the official backend before that. It runs on CouchDB, for which free hosting solutions exist. It is feature complete, but currently unmaintained. Anybody picking this project up is very welcome.

[A lot of other solutions](https://github.com/ACRA/acra/wiki/Backends) have been provided by the community, just check which one you like most.
