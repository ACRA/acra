/*
 *  Copyright 2016
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.acra.security

import android.content.Context
import org.acra.ACRAConstants
import org.acra.config.ConfigUtils.getPluginConfiguration
import org.acra.config.CoreConfiguration
import org.acra.config.HttpSenderConfiguration
import org.acra.util.InstanceCreator
import java.security.KeyStore

/**
 * Helper to get a KeyStore from a configuration
 *
 * @author F43nd1r
 * @since 4.9.0
 */
object KeyStoreHelper {
    private const val ASSET_PREFIX = "asset://"

    /**
     * try to get the keystore
     * @param context a context
     * @param config the configuration
     * @return the keystore, or null if none provided / failure
     */
    fun getKeyStore(context: Context, config: CoreConfiguration): KeyStore? {
        val senderConfiguration = getPluginConfiguration(config, HttpSenderConfiguration::class.java)
        var keyStore = InstanceCreator.create(senderConfiguration.keyStoreFactoryClass) { NoKeyStoreFactory() }.create(context)
        if (keyStore == null) {
            //either users factory did not create a keystore, or the configuration is default {@link NoKeyStoreFactory}
            val certificateRes: Int = senderConfiguration.resCertificate
            val certificatePath: String = senderConfiguration.certificatePath
            val certificateType: String = senderConfiguration.certificateType
            if (certificateRes != ACRAConstants.DEFAULT_RES_VALUE) {
                keyStore = ResourceKeyStoreFactory(certificateType, certificateRes).create(context)
            } else if (certificatePath != ACRAConstants.DEFAULT_STRING_VALUE) {
                keyStore = if (certificatePath.startsWith(ASSET_PREFIX)) {
                    AssetKeyStoreFactory(certificateType, certificatePath.substring(ASSET_PREFIX.length)).create(context)
                } else {
                    FileKeyStoreFactory(certificateType, certificatePath).create(context)
                }
            }
        }
        return keyStore
    }
}