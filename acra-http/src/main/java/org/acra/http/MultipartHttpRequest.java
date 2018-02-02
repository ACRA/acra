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

package org.acra.http;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import org.acra.ACRAConstants;
import org.acra.config.CoreConfiguration;
import org.acra.sender.HttpSender;
import org.acra.util.UriUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * Produces <a href="https://www.w3.org/Protocols/rfc1341/7_2_Multipart.html">RFC 1341</a> compliant requests
 *
 * @author F43nd1r
 * @since 11.03.2017
 */

public class MultipartHttpRequest extends BaseHttpRequest<Pair<String, List<Uri>>> {

    private static final String BOUNDARY = "%&ACRA_REPORT_DIVIDER&%";
    private static final String BOUNDARY_FIX = "--";
    private static final String NEW_LINE = "\r\n";
    private static final String CONTENT_TYPE = "Content-Type: ";
    private final Context context;
    private final String contentType;

    public MultipartHttpRequest(@NonNull CoreConfiguration config, @NonNull Context context, @NonNull String contentType, @Nullable String login, @Nullable String password,
                                int connectionTimeOut, int socketTimeOut, @Nullable Map<String, String> headers) {
        super(config, context, HttpSender.Method.POST, login, password, connectionTimeOut, socketTimeOut, headers);
        this.context = context;
        this.contentType = contentType;
    }

    @NonNull
    @Override
    protected String getContentType(@NonNull Context context, @NonNull Pair<String, List<Uri>> stringListPair) {
        return "multipart/mixed; boundary=" + BOUNDARY;
    }

    @NonNull
    @Override
    protected byte[] asBytes(@NonNull Pair<String, List<Uri>> content) throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final Writer writer = new OutputStreamWriter(outputStream, ACRAConstants.UTF8);
        //noinspection TryFinallyCanBeTryWithResources we do not target api 19
        try {
            writer.append(NEW_LINE).append(BOUNDARY_FIX).append(BOUNDARY).append(NEW_LINE);
            writer.append(CONTENT_TYPE).append(contentType).append(NEW_LINE).append(NEW_LINE);
            writer.append(content.first);
            for (Uri uri : content.second) {
                writer.append(NEW_LINE).append(BOUNDARY_FIX).append(BOUNDARY).append(NEW_LINE);
                writer.append("Content-Disposition: attachment; filename=\"").append(UriUtils.getFileNameFromUri(context, uri)).append('"').append(NEW_LINE);
                writer.append(CONTENT_TYPE).append(UriUtils.getMimeType(context, uri)).append(NEW_LINE).append(NEW_LINE);
                writer.flush();
                outputStream.write(UriUtils.uriToByteArray(context, uri));
            }
            writer.append(NEW_LINE).append(BOUNDARY_FIX).append(BOUNDARY).append(BOUNDARY_FIX).append(NEW_LINE);
            writer.flush();
            return outputStream.toByteArray();
        } finally {
            writer.close();
        }
    }
}
