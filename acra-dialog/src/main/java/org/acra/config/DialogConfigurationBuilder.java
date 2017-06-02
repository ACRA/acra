/*
 * Copyright (c) 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acra.config;

import android.app.Application;
import android.support.annotation.NonNull;

import org.acra.annotation.ConfigurationBuilder;
import org.acra.annotation.NoPropagation;
import org.acra.dialog.CrashReportDialog;

import static org.acra.ACRAConstants.DEFAULT_RES_VALUE;

/**
 * @author F43nd1r
 * @since 01.06.2017
 */
@ConfigurationBuilder(configurationName = "DialogConfiguration", markerInterfaces = PluginConfiguration.class)
public class DialogConfigurationBuilder extends BaseDialogConfigurationBuilder<DialogConfigurationBuilder> implements PluginConfigurationBuilder {

    public DialogConfigurationBuilder(@NonNull Application app) {
        super(app);
    }

    @NoPropagation
    @NonNull
    @Override
    public PluginConfiguration build() throws ACRAConfigurationException {
        if (CrashReportDialog.class.equals(reportDialogClass()) && resDialogText() == DEFAULT_RES_VALUE) {
        throw new ACRAConfigurationException("DIALOG mode: using the (default) CrashReportDialog requires you to define the resDialogText parameter in your application @ReportsCrashes() annotation.");
    }
    ConfigUtils.checkValidity(reportDialogClass());
        return new DialogConfiguration(this);
    }
}
