package com.idiotnation.raspored.dataaccess.api;

import com.idiotnation.raspored.models.dto.CourseDto;
import com.idiotnation.raspored.models.dto.CourseFilterDto;

import java.util.List;

import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface CourseService {

    @GET("courses/getAll")
    Single<List<CourseDto>> getAll();

    @POST("courses/getLatest")
    Call<List<CourseDto>> getLatestSynchronous(@Body CourseFilterDto courseFilterDto);
}
