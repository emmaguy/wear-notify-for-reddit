package com.emmaguy.todayilearned.storage;

import android.content.SharedPreferences;
import android.content.res.Resources;

import com.emmaguy.todayilearned.R;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by emma on 19/07/15.
 */
public class SharedPreferencesUserStorageTest {
    @Mock SharedPreferences mSharedPrefs;
    @Mock Resources mResources;

    @Before public void before() {
        initMocks(this);
    }

    @Test public void numberToRetrieve_returnsCorrectValue() {
        when(mResources.getString(R.string.prefs_key_number_to_retrieve)).thenReturn("prefs_key_number_to_retrieve");
        when(mSharedPrefs.getString("prefs_key_number_to_retrieve", "5")).thenReturn("1");

        SharedPreferencesUserStorage storage = new SharedPreferencesUserStorage(mSharedPrefs, mResources);

        assertThat(storage.getNumberToRequest(), equalTo(1));
    }

    @Test public void olderTimestampThanStored_isNotNewer() {
        when(mResources.getString(R.string.prefs_key_created_utc)).thenReturn("prefs_key_created_utc");
        when(mSharedPrefs.getLong("prefs_key_created_utc", 0)).thenReturn(100l);

        SharedPreferencesUserStorage storage = new SharedPreferencesUserStorage(mSharedPrefs, mResources);

        assertThat(storage.isTimestampNewerThanStored(90l), equalTo(false));
    }

    @Test public void sameTimestampThanStored_isNotNewer() {
        when(mResources.getString(R.string.prefs_key_created_utc)).thenReturn("prefs_key_created_utc");
        when(mSharedPrefs.getLong("prefs_key_created_utc", 0)).thenReturn(100l);

        SharedPreferencesUserStorage storage = new SharedPreferencesUserStorage(mSharedPrefs, mResources);

        assertThat(storage.isTimestampNewerThanStored(100l), equalTo(false));
    }

    @Test public void newerTimestampThanStored_isNewer() {
        when(mResources.getString(R.string.prefs_key_created_utc)).thenReturn("prefs_key_created_utc");
        when(mSharedPrefs.getLong("prefs_key_created_utc", 0)).thenReturn(100l);

        SharedPreferencesUserStorage storage = new SharedPreferencesUserStorage(mSharedPrefs, mResources);

        assertThat(storage.isTimestampNewerThanStored(110l), equalTo(true));
    }
}
