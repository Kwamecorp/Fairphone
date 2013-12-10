/*
 * Copyright (C) 2013 Fairphone Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fairphone.launcher;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(formKey = "dDFyQmdEbmtvU0UwZUNRVDY1WEc3WWc6MQ", logcatFilterByPid = true, logcatArguments = {
        "-t", "500"
}, customReportContent = {
        ReportField.REPORT_ID, ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.PACKAGE_NAME, ReportField.FILE_PATH,
        ReportField.PHONE_MODEL, ReportField.BRAND, ReportField.PRODUCT, ReportField.ANDROID_VERSION, ReportField.BUILD, ReportField.TOTAL_MEM_SIZE,
        ReportField.AVAILABLE_MEM_SIZE, ReportField.CUSTOM_DATA, ReportField.IS_SILENT, ReportField.STACK_TRACE, ReportField.INITIAL_CONFIGURATION,
        ReportField.CRASH_CONFIGURATION, ReportField.DISPLAY, ReportField.USER_COMMENT, ReportField.USER_EMAIL, ReportField.USER_APP_START_DATE,
        ReportField.USER_CRASH_DATE, ReportField.DUMPSYS_MEMINFO, ReportField.LOGCAT, ReportField.INSTALLATION_ID, ReportField.DEVICE_FEATURES,
        ReportField.ENVIRONMENT, ReportField.SHARED_PREFERENCES, ReportField.SETTINGS_SYSTEM, ReportField.SETTINGS_SECURE, ReportField.EVENTSLOG,
        ReportField.RADIOLOG
})
public class FairphoneApplication extends LauncherApplication
{
    @Override
    public void onCreate()
    {
        ACRA.init(this);

        super.onCreate();
    }
}
