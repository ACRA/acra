package org.acra.security;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;
import java.security.KeyStore;

/**
 * The interface can be used to provide a KeyStore with certificates.
 * Note that implementations need to be serializable.
 * (e.g. can't be anonymous inner classes of non-serializable classes)
 */
public interface KeyStoreFactory extends Serializable{
    @Nullable
    KeyStore create(@NonNull Context context);
}
