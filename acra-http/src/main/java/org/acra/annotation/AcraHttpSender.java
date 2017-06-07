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

package org.acra.annotation;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;

import org.acra.ACRAConstants;
import org.acra.collections.ImmutableList;
import org.acra.collections.ImmutableMap;
import org.acra.collections.ImmutableSet;
import org.acra.config.ACRAConfigurationException;
import org.acra.config.BaseHttpConfigurationBuilder;
import org.acra.config.ConfigUtils;
import org.acra.config.ConfigurationBuilder;
import org.acra.config.ConfigurationBuilderFactory;
import org.acra.security.KeyStoreFactory;
import org.acra.security.NoKeyStoreFactory;
import org.acra.sender.HttpSender;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author F43nd1r
 * @since 01.06.2017
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Configuration(configName = "HttpSenderConfiguration",
        builderSuperClass = BaseHttpConfigurationBuilder.class,
        packageName = "org.acra.config",
        applicationClass = Application.class,
        nonNull = NonNull.class,
        configuration = org.acra.config.Configuration.class,
        configurationBuilder = ConfigurationBuilder.class,
        configurationBuilderFactory = ConfigurationBuilderFactory.class,
        configurationException = ACRAConfigurationException.class,
        configUtils = ConfigUtils.class,
        mapWrapper = ImmutableMap.class,
        listWrapper = ImmutableList.class,
        setWrapper = ImmutableSet.class)
public @interface AcraHttpSender {
    /**
     * The Uri of your own server-side script that will receive reports.
     *
     * @return URI of a server to which to send reports.
     */
    String uri();

    /**
     * you can set here and in {@link #basicAuthPassword()} the credentials for a BASIC HTTP authentication.
     *
     * @return Login to use.
     */
    @NonNull String basicAuthLogin() default ACRAConstants.NULL_VALUE;

    /**
     * you can set here and in {@link #basicAuthLogin()} the credentials for a BASIC HTTP authentication.
     *
     * @return Password to use.
     */
    @NonNull String basicAuthPassword() default ACRAConstants.NULL_VALUE;

    /**
     * <p>
     * The {@link HttpSender.Method} to be used when posting with {@link #uri()} .
     * </p>
     *
     * @return HTTP method used when posting reports.
     */
    @NonNull HttpSender.Method httpMethod();

    /**
     * <p>
     * The {@link HttpSender.Type} to be used when posting with {@link #uri()}.
     * </p>
     *
     * @return the report type used when posting reports
     */
    @NonNull HttpSender.Type reportType();

    /**
     * @return Value in milliseconds for timeout attempting to connect to a network (default 5000ms).
     */
    int connectionTimeout() default ACRAConstants.DEFAULT_CONNECTION_TIMEOUT;

    /**
     * If the request is retried due to timeout, the socketTimeout will double
     * before retrying the request.
     *
     * @return Value in milliseconds for timeout receiving a response to a network request (default 8000ms).
     */
    int socketTimeout() default ACRAConstants.DEFAULT_SOCKET_TIMEOUT;

    /**
     * @return Class which creates a keystore that can contain trusted certificates
     */
    @NonNull Class<? extends KeyStoreFactory> keyStoreFactoryClass() default NoKeyStoreFactory.class;

    /**
     * @return path to a custom trusted certificate. Must start with "asset://" if the file is in the assets folder
     */
    @NonNull String certificatePath() default ACRAConstants.DEFAULT_STRING_VALUE;

    /**
     * @return resource id of a custom trusted certificate.
     */
    @RawRes int resCertificate() default ACRAConstants.DEFAULT_RES_VALUE;

    /**
     * @return specify the type of the certificate set in either {@link #certificatePath()} or {@link #resCertificate()}
     */
    @NonNull String certificateType() default ACRAConstants.DEFAULT_CERTIFICATE_TYPE;
}
