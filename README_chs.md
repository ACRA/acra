
||Current Status|
|---|---|
|Build|[ ![test](https://github.com/ACRA/acra/workflows/test/badge.svg?branch=master) ](https://github.com/ACRA/acra/actions?query=workflow%3Atest)|
|Bintray|[ ![Bintray](https://api.bintray.com/packages/acra/maven/ACRA/images/download.svg) ](https://bintray.com/acra/maven/ACRA/_latestVersion)|
|Maven Central|[![Maven Central](https://img.shields.io/maven-central/v/ch.acra/acra-core.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22ch.acra%22)|
|Android Versions|![minVersion](https://img.shields.io/badge/dynamic/json.svg?label=Minimal%20Android%20Version&colorB=FF6F00&query=version&uri=https%3A%2F%2Ffaendir.com%2Fandroid%2Facra.php%3Fversion%3DandroidMinVersion) ![targetVersion](https://img.shields.io/badge/dynamic/json.svg?label=Target%20Android%20Version&colorB=64DD17&query=version&uri=https%3A%2F%2Ffaendir.com%2Fandroid%2Facra.php%3Fversion%3DandroidVersion)|
|License|![license](https://img.shields.io/github/license/ACRA/acra.svg)|
| Statistics|[![AppBrain stats](https://www.appbrain.com/stats/libraries/shield/acra.svg)](https://www.appbrain.com/stats/libraries/details/acra/acra)|
|Donations|[![Flattr this project](http://api.flattr.com/button/flattr-badge-large.png)](https://flattr.com/submit/auto?user_id=kevingaudin&url=http://acra.ch&title=ACRA%20-%20Application%20Crash%20Reports%20for%20Android&language=&tags=opensource%2Candroid&category=software&description=ACRA%20%28Application%20Crash%20Reports%20for%20Android%29%20is%20an%20open%20source%20android%20library%20for%20developers%2C%20enabling%20their%20apps%20to%20send%20detailed%20reports%20when%20they%20crash.)|

**请花5分钟填写我们的[用户调查](https://goo.gl/forms/nyt9qkCk1GptRGlw2)!**

什么是 ACRA ?
==============

ACRC是一个可以让Android应用程序自动将其崩溃信息发布到服务器的库。它帮助Android应用开发人员更方便在崩溃或行为异常时从应用程序获取数据。

截至2020年6月，在Google Play上所有应用程序中使用ACRA的比例为1.57％([请参阅 AppBrain/stats](http://www.appbrain.com/stats/libraries/details/acra/acra))。使用ACRC的 **应用程序超过13000个**以及 **超过50亿次下载**。

有关分步安装和使用指南，请参见[BasicSetup](http://github.com/ACRA/acra/wiki/BasicSetup)。

自Android 2.2（FroYo）起，Android应用程序的崩溃报告功能是原生提供的，但是仅仅只能通过官方Android Market使用（且数据有限）。ACRA对Android开发人员很大的帮助有：

  * [开发人员可自由配置交互的方式](http://github.com/ACRA/acra/wiki/AdvancedUsage#wiki-User_Interaction): 静默上传给服务器，Toast通知，状态栏通知或对话框弹出
  * 可以支持官方的所有Android版本一起使用。
  * 运行应用程序的设备的崩溃信息，比Android Market开发者控制台错误报告中显示的[崩溃报告更详细](http://github.com/ACRA/acra/wiki/ReportContent)
  * 您可以将 [自定义的变量内容或调试跟踪信息添加到上传信息中](http://github.com/ACRA/acra/wiki/AdvancedUsage#wiki-Adding_your_own_variables_content_or_traces_in_crash_reports)
  * [就算应用程序没有崩溃](https://github.com/ACRA/acra/wiki/AdvancedUsage#sending-reports-for-caught-exceptions-or-for-unexpected-application-state-without-any-exception)，您依然可以发送错误信息
  * 即使没有通过Google PLay交付，也适用于任何应用程序 => 非常适用于无法使用Google Play的设备/地区，测试版或企业私有应用程序
  * 如果没有网络覆盖，信息将保留在本地，待应用程序重新启动时发送
  * 可以与您自己的 [错误信息接收器脚本](https://github.com/ACRA/acra/wiki/Report-Destinations)一起使用

ACRA的通知系统很整洁。 如果发生崩溃，应用程序不会在现有系统的崩溃通知或报告功能之上重复增加用户通知。 默认情况下，不显示“强制关闭”对话框，如果要开启它需要设置 `alsoReportToAndroidFramework` 为 `true`。

仅向用户通知一次错误，并且您可以通过在通知/对话框中定义自己的文本来调整应用程序的交互轻重。

请不要犹豫，在 [问题跟踪器isseu](http://github.com/ACRA/acra/issues)中提bug和加需求。

最新版本
===========================================

有关最新版本和完整的变更日志，请参见 [发布页面](https://github.com/ACRA/acra/releases)。

要从4.x进行迁移，请参阅Wiki中的 [迁移指南](http://github.com/ACRA/acra/wiki/Migrating)。

后端
========
[Acralyzer](https://github.com/ACRA/acralyzer) 是用于报表存储和分析的官方后端。 它在CouchDB上运行，提供了免费的托管解决方案。 此项目功能已完成，但目前尚未维护。 任何人选择此项目作为后端，我们都非常欢迎。

如果您不想依赖未维护的项目，则建议使用[Acrarium](https://github.com/F43nd1r/Acrarium)。 Acrarium正在积极开发中，尚未达到稳定阶段。

[社区](https://github.com/ACRA/acra/wiki/Backends) 提供了许多其他解决方案，您可以选择最心仪的。