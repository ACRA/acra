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
import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.config.CoreConfiguration;
import org.acra.sender.HttpSender;
import org.acra.util.UriUtils;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Produces <a href="https://tools.ietf.org/html/rfc7578">RFC 7578</a> compliant requests
 *
 * @author F43nd1r
 * @since 11.03.2017
 */

public class MultipartHttpRequest extends BaseHttpRequest<Pair<String, List<Uri>>> {

    private static final String BOUNDARY = "%&ACRA_REPORT_DIVIDER&%";
    private static final String BOUNDARY_FIX = "--";
    private static final String NEW_LINE = "\r\n";
    private static final String SECTION_START = NEW_LINE + BOUNDARY_FIX + BOUNDARY + NEW_LINE;
    private static final String MESSAGE_END = NEW_LINE + BOUNDARY_FIX + BOUNDARY + BOUNDARY_FIX + NEW_LINE;
    private static final String CONTENT_DISPOSITION = "Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"" + NEW_LINE;
    private static final String CONTENT_TYPE = "Content-Type: %s" + NEW_LINE;
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
        return "multipart/form-data; boundary=" + BOUNDARY;
    }

    @Override
    protected void write(OutputStream outputStream, @NonNull Pair<String, List<Uri>> content) throws IOException {
        final PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, ACRAConstants.UTF8));
        writer.append(SECTION_START)
                .format(CONTENT_DISPOSITION, "ACRA_REPORT", "")
                .format(CONTENT_TYPE, contentType)
                .append(NEW_LINE)
                .append(content.first);
        for (Uri uri : content.second) {
            try {
                String name = UriUtils.getFileNameFromUri(context, uri);
                writer.append(SECTION_START)
                        .format(CONTENT_DISPOSITION, "ACRA_ATTACHMENT", name)
                        .format(CONTENT_TYPE, UriUtils.getMimeType(context, uri))
                        .append(NEW_LINE)
                        .flush();
                UriUtils.copyFromUri(context, outputStream, uri);
            } catch (FileNotFoundException e) {
                ACRA.log.w("Not sending attachment", e);
            }
        }
        writer.append(MESSAGE_END).flush();
    }
}
