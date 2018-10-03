
||Current Status|
|---|---|
|Build|[![Build Status](https://travis-ci.org/ACRA/acra.svg?branch=master)](https://travis-ci.org/ACRA/acra)|
|Bintray|[ ![Bintray](https://api.bintray.com/packages/acra/maven/ACRA/images/download.svg) ](https://bintray.com/acra/maven/ACRA/_latestVersion)|
|Maven Central|[![Maven Central](https://img.shields.io/maven-central/v/ch.acra/acra-core.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22ch.acra%22)|
|Android Versions|![minVersion](https://img.shields.io/badge/dynamic/json.svg?label=Minimal%20Android%20Version&colorB=FF6F00&query=version&uri=https%3A%2F%2Ffaendir.com%2Fandroid%2Facra.php%3Fversion%3DandroidMinVersion) ![targetVersion](https://img.shields.io/badge/dynamic/json.svg?label=Target%20Android%20Version&colorB=64DD17&query=version&uri=https%3A%2F%2Ffaendir.com%2Fandroid%2Facra.php%3Fversion%3DandroidVersion)|
|License|![license](https://img.shields.io/github/license/ACRA/acra.svg)|
|Donations|[![Flattr this project](http://api.flattr.com/button/flattr-badge-large.png)](https://flattr.com/submit/auto?user_id=kevingaudin&url=http://acra.ch&title=ACRA%20-%20Application%20Crash%20Reports%20for%20Android&language=&tags=opensource%2Candroid&category=software&description=ACRA%20%28Application%20Crash%20Reports%20for%20Android%29%20is%20an%20open%20source%20android%20library%20for%20developers%2C%20enabling%20their%20apps%20to%20send%20detailed%20reports%20when%20they%20crash.)|

**Please take 5 Minutes to fill out our [User survey](https://goo.gl/forms/nyt9qkCk1GptRGlw2)!**

What is ACRA ?
==============

ACRA is a library enabling Android Application to automatically post their crash reports to a report server. It is targeted to android applications developers to help them get data from their applications when they crash or behave erroneously.

ACRA is used in 2.68% ([See AppBrain/stats](http://www.appbrain.com/stats/libraries/details/acra/acra)) of all apps on Google Play as of Feb 2016. That's over 53K **apps** using ACRA. And since the average US user has 41 apps installed on their phone that means there is a 70% chance that ACRA is running on any phone. That means ACRA is running on over a **billion devices**.

See [BasicSetup](http://github.com/ACRA/acra/wiki/BasicSetup) for a step-by-step installation and usage guide.

A crash reporting feature for android apps is native since Android 2.2 (FroYo) but only available through the official Android Market (and with limited data). ACRA is a great help for Android developers :

  * [developer configurable user interaction](http://github.com/ACRA/acra/wiki/AdvancedUsage#wiki-User_Interaction): silent reports, Toast notification, status bar notification + dialog or direct dialog
  * usable with ALL versions of Android supported by the official support libraries.
  * more [detailed crash reports](http://github.com/ACRA/acra/wiki/ReportContent) about the device running the app than what is displayed in the Android Market developer console error reports
  * you can [add your own variables content or debug traces](http://github.com/ACRA/acra/wiki/AdvancedUsage#wiki-Adding_your_own_variables_content_or_traces_in_crash_reports) to the reports
  * you can send [error reports even if the application doesn't crash](https://github.com/ACRA/acra/wiki/AdvancedUsage#sending-reports-for-caught-exceptions-or-for-unexpected-application-state-without-any-exception)
  * works for any application even if not delivered through Google PLay => great for devices/regions where the Google Play is not available, beta releases or for enterprise private apps
  * if there is no network coverage, reports are kept and sent on a later application restart
  * can be used with [your own self-hosted report receiver script](https://github.com/ACRA/acra/wiki/Report-Destinations)

ACRA's notification systems are clean. If a crash occurs, your application does not add user notifications over existing system's crash notifications or reporting features. By default, the "force close" dialog is not displayed anymore, to enable it set `alsoReportToAndroidFramework` to `true`.

The user is notified of an error only once, and you might enhance the perceived quality of your application by defining your own texts in the notifications/dialogs.

Please do not hesitate to open defects/enhancements requests in [the issue tracker](http://github.com/ACRA/acra/issues).

Latest version
===========================================

For the latest version and a complete changelog, please see the [ChangeLog page](http://github.com/ACRA/acra/wiki/ChangeLog) in the Wiki.

For migrating from previous versions, please see our [Migration guide](http://github.com/ACRA/acra/wiki/Migrating) in the Wiki.

And after that?
===============

Now that ACRA is stabilized on the device side (there shouldn't be much more data required...), the effort should be placed on crash data analysis and reports management tools for developers.

You can look at [some contributions](https://github.com/ACRA/acra/wiki/Backends) that have already been published.

[Acralyzer](http://github.com/ACRA/acralyzer) is the official backend for reports storage and analysis. It is a free and open source modern web app, based on a full open stack and using advanced
technology like CouchDB (JSON document storage with a RESTful API and Map/Reduce querying), AngularJS (one of the most advanced client-side JS frameworks), D3JS (for data visualisation)... If you are interested
in webapps development, this project can become your playground too ;-)
