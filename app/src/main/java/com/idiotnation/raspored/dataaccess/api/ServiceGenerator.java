package com.idiotnation.raspored.dataaccess.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.idiotnation.raspored.helpers.Utils;

import java.util.TimeZone;

import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class ServiceGenerator {
    private static Retrofit retrofit;

    public static <S> S createService(Class<S> serviceClass) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(Utils.WS_BASE_URL)
                    .addConverterFactory(JacksonConverterFactory.create(new ObjectMapper().registerModule(new JodaModule()).setTimeZone(TimeZone.getDefault())))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                    .build();
        }
        return retrofit.create(serviceClass);
    }
}
