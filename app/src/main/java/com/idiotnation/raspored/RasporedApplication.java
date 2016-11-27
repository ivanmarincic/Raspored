package com.idiotnation.raspored;

import android.app.Application;

import com.idiotnation.raspored.Presenters.MainPresenter;
import com.idiotnation.raspored.Views.ColorSetupView;
import com.idiotnation.raspored.Views.MainView;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Component;

public class RasporedApplication extends Application {

    @Singleton
    @Component(modules = AndroidModule.class)
    public interface ApplicationComponent {
        void inject(RasporedApplication application);
        void inject(MainView activity);
        void inject(ColorSetupView activity);
    }

    @Inject
    MainPresenter presenter;

    private ApplicationComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        component = DaggerRasporedApplication_ApplicationComponent.builder()
                .androidModule(new AndroidModule(this))
                .build();
        component().inject(this);
    }

    public ApplicationComponent component() {
        return component;
    }
}
