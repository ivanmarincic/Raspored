package com.idiotnation.raspored.dataaccess.api;

import com.idiotnation.raspored.models.dto.AppointmentDto;
import com.idiotnation.raspored.models.dto.AppointmentFilterDto;

import java.util.List;

import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AppointmentService {

    @GET("appointments/getAll")
    Single<List<AppointmentDto>> getAll();

    @GET("appointments/getByCourse/{courseId}")
    Single<List<String>> getByCourse(@Path("courseId") Integer courseId);

    @POST("appointments/getLatest")
    Single<List<AppointmentDto>> getLatest(@Body AppointmentFilterDto appointmentFilterDto);

    @POST("appointments/getLatest")
    Call<List<AppointmentDto>> getLatestSynchronous(@Body AppointmentFilterDto appointmentFilterDto);

    @GET("appointments/getByCourseAndName/{courseId}/{appointmentName}")
    Single<List<AppointmentDto>> getByCourseAndName(@Path("courseId") Integer courseId, @Path("appointmentName") String appointmentName);
}
