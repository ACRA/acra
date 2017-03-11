package org.acra.http;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.acra.ACRAConstants;
import org.acra.config.ACRAConfiguration;
import org.acra.sender.HttpSender;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
        InputStream inputStream = context.getContentResolver().openInputStream(content);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[ACRAConstants.DEFAULT_BUFFER_SIZE_IN_BYTES];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        return outputStream.toByteArray();
    }
}
