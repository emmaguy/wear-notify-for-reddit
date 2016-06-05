package com.emmaguy.todayilearned.storage;

import android.content.SharedPreferences;
import android.content.res.Resources;

import com.emmaguy.todayilearned.R;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by emma on 19/07/15.
 */
public class SharedPreferencesUserStorageTest {
    private static final String PREFS_KEY_NUMBER_TO_RETRIEVE = "prefs_key_number_to_retrieve";
    private static final String PREFS_KEY_CREATED_UTC = "prefs_key_created_utc";

    @Mock SharedPreferences.Editor mEditor;
    @Mock SharedPreferences mSharedPrefs;
    @Mock Resources mResources;

    private SharedPreferencesUserStorage mStorage;

    @Before public void before() {
        initMocks(this);

        when(mResources.getString(R.string.prefs_key_created_utc)).thenReturn(PREFS_KEY_CREATED_UTC);
        when(mResources.getString(R.string.prefs_key_number_to_retrieve)).thenReturn(
                PREFS_KEY_NUMBER_TO_RETRIEVE);

        when(mSharedPrefs.getString(PREFS_KEY_NUMBER_TO_RETRIEVE, "5")).thenReturn("1");
        when(mSharedPrefs.getLong(PREFS_KEY_CREATED_UTC, 0)).thenReturn(100l);

        when(mSharedPrefs.edit()).thenReturn(mEditor);
        when(mEditor.remove(PREFS_KEY_CREATED_UTC)).thenReturn(mEditor);

        mStorage = new SharedPreferencesUserStorage(mSharedPrefs, mResources);
    }

    @Test public void numberToRetrieve_returnsCorrectValue() {
        assertThat(mStorage.getNumberToRequest(), equalTo(1));
    }

    @Test public void getTimestamp_retrievesCorrectValue() {
        assertThat(mStorage.getTimestamp(), equalTo(100l));
    }

    @Test public void clearTimestamp_shouldRemoveTimestamp() {
        mStorage.clearTimestamp();

        verify(mEditor).remove(PREFS_KEY_CREATED_UTC);
    }
}
