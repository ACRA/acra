
||Current Status|
|---|---|
|Build|[ ![test](https://github.com/ACRA/acra/workflows/test/badge.svg?branch=master) ](https://github.com/ACRA/acra/actions?query=workflow%3Atest)|
|Maven Central|[![Maven Central](https://img.shields.io/maven-central/v/ch.acra/acra-core.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22ch.acra%22)|
|Android Versions|![minVersion](https://img.shields.io/badge/dynamic/json?label=Android%20Min%20Version&query=version&url=https%3A%2F%2Ffaendir.com%2Fandroid%2Facra.php%3Fversion%3Dandroid-min) ![targetVersion](https://img.shields.io/badge/dynamic/json?label=Android%20Target%20Version&query=version&url=https%3A%2F%2Ffaendir.com%2Fandroid%2Facra.php%3Fversion%3Dandroid-target)|
|License|![license](https://img.shields.io/github/license/ACRA/acra.svg)|
| Statistics|[![AppBrain stats](https://www.appbrain.com/stats/libraries/shield/acra.svg)](https://www.appbrain.com/stats/libraries/details/acra/acra)|

What is ACRA ?
==============

ACRA is an open-source library for Android developers to easily integrate crash reporting into their applications. It provides a comprehensive set of features, including customizable reporting, support for multiple different senders, and flexible data collection options, enabling developers to quickly identify and diagnose issues in their apps.

ACRA is used in 1.57% ([See AppBrain/stats](https://www.appbrain.com/stats/libraries/details/acra/acra)) of all apps on Google Play as of June 2020. That's over **13 thousand apps** and over **5 billion downloads** including ACRA.

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

How to use
=====
Our [Website](https://www.acra.ch/docs/Setup) covers a step-by-step guide for initial setup as well as advanced usage information.

Latest version
===========================================

For the latest version and a complete changelog, please see the [Release page](https://github.com/ACRA/acra/releases).

For migrating from 4.x, please see our [Migration guide](https://github.com/ACRA/acra/wiki/Migrating) in the Wiki.

Backends
========

[Acrarium](https://github.com/F43nd1r/Acrarium) is the official backend for report storage and analysis. Acrarium is still in active development.

[Acralyzer](https://github.com/ACRA/acralyzer) was the official backend before that. It runs on CouchDB, for which free hosting solutions exist. It is feature complete, but currently unmaintained. Anybody picking this project up is very welcome.

[A lot of other solutions](https://www.acra.ch/docs/Backends) have been provided by the community, just check which one you like most.
