package com.emmaguy.todayilearned.storage;

import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class SharedPreferencesUniqueIdentifierStorageTest {
    private @Mock SharedPreferences mSharedPreferences;
    private @Mock SharedPreferences.Editor mEditor;

    private String mKey = "test_key";

    private UniqueIdentifierStorage mStorage;

    @Before public void before() {
        initMocks(this);

        when(mSharedPreferences.edit()).thenReturn(mEditor);
        when(mEditor.putString(anyString(), anyString())).thenReturn(mEditor);

        mStorage = new SharedPreferencesUniqueIdentifierStorage(mSharedPreferences, mKey);
    }

    @Test public void getUniqueIdentifier_withEmptyUniqueIdentifier_generatesOneAndStoresIt() {
        when(mSharedPreferences.getString(mKey, "")).thenReturn("");

        mStorage.getUniqueIdentifier();

        verify(mSharedPreferences).getString(mKey, "");
        verify(mEditor).putString(eq(mKey), anyString());
        verify(mEditor).apply();
    }

    @Test public void getUniqueIdentifier_retrievesExistingIdentifier_andDoesNotModifySharedPreferences() {
        when(mSharedPreferences.getString(mKey, "")).thenReturn("uuid");

        String id = mStorage.getUniqueIdentifier();

        verifyZeroInteractions(mEditor);
        verify(mSharedPreferences).getString(mKey, "");

        assertThat(id, equalTo("uuid"));
    }
}
