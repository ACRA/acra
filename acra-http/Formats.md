# Http Sender Format Definition

This sender supports multiple formats based on configuration.

## Content

### JSON

Reports are sent as `application/json`.

Keys are [ReportField](http://www.acra.ch/javadoc/latest/acra/org.acra/-report-field/index.html) entries or custom keys set by your custom senders.

Value types vary depending on the report field, but are usually strings or objects.

Example:

```json
{
  "REPORT_ID": "00019d4d-570e-4d98-a854-23fbbf01ca2d",
  "ANDROID_VERSION": "11",
  "BUILD_CONFIG": {
    "BUILD_TYPE": "debug",
    "DEBUG": true
  },
  "CUSTOM_DATA": {},
  "IS_SILENT": false
}
```

### KEY_VALUE_LIST

Reports are sent as `application/x-www-form-urlencoded`.

Form keys are [ReportField](http://www.acra.ch/javadoc/latest/acra/org.acra/-report-field/index.html) entries or custom keys set by your custom senders.

Values are url encoded strings. In those strings first level nested object keys are joined with `\n`. Deeper nested objects are flattened with `.`.

Example (note that this is only written on multiple lines for readability):

```text
REPORT_ID=00019d4d-570e-4d98-a854-23fbbf01ca2d&
ANDROID_VERSION=11&
BUILD_CONFIG=BUILD_TYPE%3Ddebug%0ADEBUG%3Dtrue&
CUSTOM_DATA=&
IS_SILENT=false
```

## Method

### POST

Reports are sent as a single `POST` request to `<report uri>`.

If attachments are present, the content type changes to `multipart/form-data`. 
The report is sent as a part with the name `ACRA_REPORT`, no file name and the content type as seen under [Content](#Content) above. 
Attachments are sent as a part each with the name `ACRA_ATTACHMENT`, the respective file name and the content type as a best guess based on the file name.

### PUT

Reports are sent as a one or more `PUT` requests.

The report is always sent to `<report uri>/<report id>`.
Attachments are sent as a separate request each to `<report uri>/<report id>-<attachment file name>`.