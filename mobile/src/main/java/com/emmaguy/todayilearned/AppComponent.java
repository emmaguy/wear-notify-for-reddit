package com.emmaguy.todayilearned;

import com.emmaguy.todayilearned.background.RetrieveService;
import com.emmaguy.todayilearned.ui.SettingsActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by emma on 04/07/15.
 */
@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    void inject(RetrieveService retrieveService);
    void inject(SettingsActivity.SettingsFragment fragment);
}
