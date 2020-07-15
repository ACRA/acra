
||Current Status|
|---|---|
|Build|[ ![test](https://github.com/ACRA/acra/workflows/test/badge.svg?branch=master) ](https://github.com/ACRA/acra/actions?query=workflow%3Atest)|
|Bintray|[ ![Bintray](https://api.bintray.com/packages/acra/maven/ACRA/images/download.svg) ](https://bintray.com/acra/maven/ACRA/_latestVersion)|
|Maven Central|[![Maven Central](https://img.shields.io/maven-central/v/ch.acra/acra-core.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22ch.acra%22)|
|Android Versions|![minVersion](https://img.shields.io/badge/dynamic/json.svg?label=Minimal%20Android%20Version&colorB=FF6F00&query=version&uri=https%3A%2F%2Ffaendir.com%2Fandroid%2Facra.php%3Fversion%3DandroidMinVersion) ![targetVersion](https://img.shields.io/badge/dynamic/json.svg?label=Target%20Android%20Version&colorB=64DD17&query=version&uri=https%3A%2F%2Ffaendir.com%2Fandroid%2Facra.php%3Fversion%3DandroidVersion)|
|License|![license](https://img.shields.io/github/license/ACRA/acra.svg)|
| Statistics|[![AppBrain stats](https://www.appbrain.com/stats/libraries/shield/acra.svg)](https://www.appbrain.com/stats/libraries/details/acra/acra)|
|Donations|[![Flattr this project](http://api.flattr.com/button/flattr-badge-large.png)](https://flattr.com/submit/auto?user_id=kevingaudin&url=http://acra.ch&title=ACRA%20-%20Application%20Crash%20Reports%20for%20Android&language=&tags=opensource%2Candroid&category=software&description=ACRA%20%28Application%20Crash%20Reports%20for%20Android%29%20is%20an%20open%20source%20android%20library%20for%20developers%2C%20enabling%20their%20apps%20to%20send%20detailed%20reports%20when%20they%20crash.)|

**�뻨5������д���ǵ�[�û�����](https://goo.gl/forms/nyt9qkCk1GptRGlw2)!**

ʲô�� ACRA ?
==============

ACRC��һ��������AndroidӦ�ó����Զ����������Ϣ�������������Ŀ⡣������AndroidӦ�ÿ�����Ա�������ڱ�������Ϊ�쳣ʱ��Ӧ�ó����ȡ���ݡ�

����2020��6�£���Google Play������Ӧ�ó�����ʹ��ACRA�ı���Ϊ1.57��([����� AppBrain/stats](http://www.appbrain.com/stats/libraries/details/acra/acra))��ʹ��ACRC�� **Ӧ�ó��򳬹�13000��**�Լ� **����50�ڴ�����**��

�йطֲ���װ��ʹ��ָ�ϣ���μ�[BasicSetup](http://github.com/ACRA/acra/wiki/BasicSetup)��

��Android 2.2��FroYo����AndroidӦ�ó���ı������湦����ԭ���ṩ�ģ����ǽ���ֻ��ͨ���ٷ�Android Marketʹ�ã����������ޣ���ACRA��Android������Ա�ܴ�İ����У�

  * [������Ա���������ý����ķ�ʽ](http://github.com/ACRA/acra/wiki/AdvancedUsage#wiki-User_Interaction): ��Ĭ�ϴ�����������Toast֪ͨ��״̬��֪ͨ��Ի��򵯳�
  * ����֧�ֹٷ�������Android�汾һ��ʹ�á�
  * ����Ӧ�ó�����豸�ı�����Ϣ����Android Market�����߿���̨���󱨸�����ʾ��[�����������ϸ](http://github.com/ACRA/acra/wiki/ReportContent)
  * �����Խ� [�Զ���ı������ݻ���Ը�����Ϣ��ӵ��ϴ���Ϣ��](http://github.com/ACRA/acra/wiki/AdvancedUsage#wiki-Adding_your_own_variables_content_or_traces_in_crash_reports)
  * [����Ӧ�ó���û�б���](https://github.com/ACRA/acra/wiki/AdvancedUsage#sending-reports-for-caught-exceptions-or-for-unexpected-application-state-without-any-exception)������Ȼ���Է��ʹ�����Ϣ
  * ��ʹû��ͨ��Google PLay������Ҳ�������κ�Ӧ�ó��� => �ǳ��������޷�ʹ��Google Play���豸/���������԰����ҵ˽��Ӧ�ó���
  * ���û�����縲�ǣ���Ϣ�������ڱ��أ���Ӧ�ó�����������ʱ����
  * ���������Լ��� [������Ϣ�������ű�](https://github.com/ACRA/acra/wiki/Report-Destinations)һ��ʹ��

ACRA��֪ͨϵͳ�����ࡣ �������������Ӧ�ó��򲻻�������ϵͳ�ı���֪ͨ�򱨸湦��֮���ظ������û�֪ͨ�� Ĭ������£�����ʾ��ǿ�ƹرա��Ի������Ҫ��������Ҫ���� `alsoReportToAndroidFramework` Ϊ `true`��

�����û�֪ͨһ�δ��󣬲���������ͨ����֪ͨ/�Ի����ж����Լ����ı�������Ӧ�ó���Ľ������ء�

�벻Ҫ��ԥ���� [���������isseu](http://github.com/ACRA/acra/issues)����bug�ͼ�����

���°汾
===========================================

�й����°汾�������ı����־����μ� [����ҳ��](https://github.com/ACRA/acra/releases)��

Ҫ��4.x����Ǩ�ƣ������Wiki�е� [Ǩ��ָ��](http://github.com/ACRA/acra/wiki/Migrating)��

���
========
[Acralyzer](https://github.com/ACRA/acralyzer) �����ڱ���洢�ͷ����Ĺٷ���ˡ� ����CouchDB�����У��ṩ����ѵ��йܽ�������� ����Ŀ��������ɣ���Ŀǰ��δά���� �κ���ѡ�����Ŀ��Ϊ��ˣ����Ƕ��ǳ���ӭ��

�������������δά������Ŀ������ʹ��[Acrarium](https://github.com/F43nd1r/Acrarium)�� Acrarium���ڻ��������У���δ�ﵽ�ȶ��׶Ρ�

[����](https://github.com/ACRA/acra/wiki/Backends) �ṩ������������������������ѡ�������ǵġ�