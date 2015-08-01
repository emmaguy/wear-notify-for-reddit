package com.emmaguy.todayilearned;

import com.emmaguy.todayilearned.refresh.BackgroundAlarmListener;
import com.emmaguy.todayilearned.refresh.RetrieveService;
import com.emmaguy.todayilearned.refresh.WearListenerService;
import com.emmaguy.todayilearned.settings.DragReorderActionsPreference;
import com.emmaguy.todayilearned.settings.SettingsActivity;
import com.emmaguy.todayilearned.settings.SettingsModule;
import com.emmaguy.todayilearned.storage.StorageModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by emma on 04/07/15.
 */
@Singleton
@Component(modules = {AppModule.class, StorageModule.class, SettingsModule.class})
public interface AppComponent {
    void inject(DragReorderActionsPreference dragReorderActionsPreference);
    void inject(BackgroundAlarmListener backgroundAlarmListener);
    void inject(SettingsActivity.SettingsFragment fragment);
    void inject(WearListenerService wearListenerService);
    void inject(RetrieveService retrieveService);
    void inject(App app);
}
