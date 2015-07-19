package com.emmaguy.todayilearned;

import com.emmaguy.todayilearned.refresh.ConverterModule;
import com.emmaguy.todayilearned.refresh.RetrieveService;
import com.emmaguy.todayilearned.refresh.WearListenerService;
import com.emmaguy.todayilearned.settings.DragReorderActionsPreference;
import com.emmaguy.todayilearned.settings.SettingsActivity;
import com.emmaguy.todayilearned.settings.SettingsModule;
import com.emmaguy.todayilearned.storage.StorageModule;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Component;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;

/**
 * Created by emma on 04/07/15.
 */
@Singleton
@Component(modules = {AppModule.class, StorageModule.class, SettingsModule.class, ConverterModule.class})
public interface AppComponent {
    @Named("token") Converter token();
    @Named("markread") Converter markAsRead();
    @Named("posts") GsonConverter posts();
    @Named("redditResponse") GsonConverter redditResponse();
    @Named("comments") GsonConverter comments();

    void inject(DragReorderActionsPreference dragReorderActionsPreference);
    void inject(SettingsActivity.SettingsFragment fragment);
    void inject(WearListenerService wearListenerService);
    void inject(RetrieveService retrieveService);
}
