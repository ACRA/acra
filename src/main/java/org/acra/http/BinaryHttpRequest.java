package org.acra.http;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.acra.config.ACRAConfiguration;
import org.acra.sender.HttpSender;
import org.acra.util.IOUtils;

import java.io.IOException;
import java.util.Map;

/**
 * @author F43nd1r
 * @since 10.03.2017
 */

public class BinaryHttpRequest extends BaseHttpRequest<Uri> {
    @NonNull
    private final Context context;

    public BinaryHttpRequest(@NonNull ACRAConfiguration config, @NonNull Context context, @NonNull HttpSender.Method method,
                             @Nullable String login, @Nullable String password, int connectionTimeOut, int socketTimeOut, @Nullable Map<String, String> headers) {
        super(config, context, method, login, password, connectionTimeOut, socketTimeOut, headers);
        this.context = context;
    }

    @Override
    protected String getContentType() {
        return "application/octet-stream";
    }

    @Override
    protected byte[] asBytes(Uri content) throws IOException {
        return IOUtils.uriToByteArray(context, content);
    }
}
