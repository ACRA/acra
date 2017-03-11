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
import org.acra.config.ACRAConfiguration;
import org.acra.sender.HttpSender;
import org.acra.util.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * @author F43nd1r
 * @since 11.03.2017
 */

public class MultipartHttpRequest extends BaseHttpRequest<Pair<String, List<Uri>>> {

    private static final String BOUNDARY = "---ACRA_REPORT_DIVIDER---";
    private static final char NEW_LINE = '\n';
    private static final String CONTENT_TYPE = "Content-Type: ";
    @NonNull
    private final Context context;
    @NonNull
    private final HttpSender.Type type;

    public MultipartHttpRequest(@NonNull ACRAConfiguration config, @NonNull Context context, @NonNull HttpSender.Method method, @NonNull HttpSender.Type type, @Nullable String login, @Nullable String password,
                                int connectionTimeOut, int socketTimeOut, @Nullable Map<String, String> headers) {
        super(config, context, method, login, password, connectionTimeOut, socketTimeOut, headers);
        this.context = context;
        this.type = type;
    }

    @Override
    protected String getContentType() {
        return "multipart/mixed; boundary=" + BOUNDARY;
    }

    @Override
    protected byte[] asBytes(Pair<String, List<Uri>> content) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(outputStream, ACRAConstants.UTF8);
        writer.append(NEW_LINE).append(BOUNDARY).append(NEW_LINE);
        writer.append(CONTENT_TYPE).append(type.getContentType()).append(NEW_LINE);
        writer.append(content.first);
        for (Uri uri : content.second){
            writer.append(NEW_LINE).append(BOUNDARY).append(NEW_LINE);
            writer.append("Content-Disposition: attachment; filename=\"").append(HttpSender.getFileName(context, uri)).append('"');
            writer.append(CONTENT_TYPE).append("application/octet-stream").append(NEW_LINE);
            outputStream.write(IOUtils.uriToByteArray(context, uri));
        }
        writer.append(NEW_LINE).append(BOUNDARY).append(NEW_LINE);
        return outputStream.toByteArray();
    }
}
