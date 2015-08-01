package com.emmaguy.todayilearned.storage;

import android.content.SharedPreferences;
import android.content.res.Resources;

import com.emmaguy.todayilearned.R;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Contains all the injectable storage classes
 */
@Module
public class StorageModule {
    @Provides
    @Singleton
    @Named("state")
    public UniqueIdentifierStorage provideUniqueIdentifierStorage(SharedPreferences preferences, Resources resources) {
        return new SharedPreferencesUniqueIdentifierStorage(preferences, resources.getString(R.string.prefs_key_state));
    }

    @Provides
    @Singleton
    public UserStorage provideUserStorage(SharedPreferences preferences, Resources resources) {
        return new SharedPreferencesUserStorage(preferences, resources);
    }

    @Provides
    @Singleton
    public TokenStorage provideTokenStorage(SharedPreferences preferences, Resources resources) {
        return new SharedPreferencesTokenStorage(preferences, resources);
    }
}
