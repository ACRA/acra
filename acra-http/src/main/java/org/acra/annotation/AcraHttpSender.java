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

import android.support.annotation.NonNull;
import android.support.annotation.RawRes;

import org.acra.ACRAConstants;
import org.acra.config.BaseHttpConfigurationBuilder;
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
 * Http sender configuration
 *
 * @author F43nd1r
 * @since 01.06.2017
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Configuration(baseBuilderClass = BaseHttpConfigurationBuilder.class)
public @interface AcraHttpSender {
    /**
     * The Uri of your own server-side script that will receive reports.
     *
     * @return URI of a server to which to send reports.
     * @since 5.0.0
     */
    @NonNull String uri();

    /**
     * you can set here and in {@link org.acra.annotation.AcraHttpSender#basicAuthPassword()} the credentials for a BASIC HTTP authentication.
     *
     * @return Login to use.
     * @since 5.0.0
     */
    @NonNull String basicAuthLogin() default ACRAConstants.NULL_VALUE;

    /**
     * you can set here and in {@link org.acra.annotation.AcraHttpSender#basicAuthLogin()} the credentials for a BASIC HTTP authentication.
     *
     * @return Password to use.
     * @since 5.0.0
     */
    @NonNull String basicAuthPassword() default ACRAConstants.NULL_VALUE;

    /**
     * <p>
     * The {@link HttpSender.Method} to be used when posting with {@link org.acra.annotation.AcraHttpSender#uri()} .
     * </p>
     *
     * @return HTTP method used when posting reports.
     * @since 5.0.0
     */
    @NonNull HttpSender.Method httpMethod();

    /**
     * timeout for server connection
     *
     * @return Value in milliseconds for timeout attempting to connect to a network.
     * @see java.net.HttpURLConnection#setConnectTimeout(int)
     * @since 5.0.0
     */
    int connectionTimeout() default 5000;

    /**
     * timeout for socket connection
     *
     * @return Value in milliseconds for timeout receiving a response to a network request.
     * @see java.net.HttpURLConnection#setReadTimeout(int)
     * @since 5.0.0
     */
    int socketTimeout() default 20000;

    /**
     * allows to prevent resending of timed out reports, possibly relieving server stress, but also reducing received report counts
     *
     * @return if timed out reports should be dropped
     * @since 5.0.0
     */
    boolean dropReportsOnTimeout() default false;

    /**
     * A custom class supplying a {@link java.security.KeyStore}, which will be used for ssl authentication.
     * A base implementation is available: {@link org.acra.security.BaseKeyStoreFactory}
     *
     * @return Class which creates a keystore that can contain trusted certificates
     * @since 5.0.0
     */
    @NonNull Class<? extends KeyStoreFactory> keyStoreFactoryClass() default NoKeyStoreFactory.class;

    /**
     * a certificate used for ssl authentication
     *
     * @return path to a custom trusted certificate. Must start with "asset://" if the file is in the assets folder
     * @since 5.0.0
     */
    @NonNull String certificatePath() default ACRAConstants.DEFAULT_STRING_VALUE;

    /**
     * a certificate used for ssl authentication
     *
     * @return resource id of a custom trusted certificate.
     * @since 5.0.0
     */
    @RawRes int resCertificate() default ACRAConstants.DEFAULT_RES_VALUE;

    /**
     * type of the certificate used for ssl authentication
     *
     * @return specify the type of the certificate set in either {@link org.acra.annotation.AcraHttpSender#certificatePath()} or {@link org.acra.annotation.AcraHttpSender#resCertificate()}
     * @since 5.0.0
     */
    @NonNull String certificateType() default ACRAConstants.DEFAULT_CERTIFICATE_TYPE;
}
