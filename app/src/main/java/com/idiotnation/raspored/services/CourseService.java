package com.idiotnation.raspored.services;

import com.idiotnation.raspored.dataaccess.api.ServiceGenerator;
import com.idiotnation.raspored.dataaccess.database.DatabaseManager;
import com.idiotnation.raspored.helpers.Utils;
import com.idiotnation.raspored.models.db.Course;
import com.idiotnation.raspored.models.db.CourseType;
import com.idiotnation.raspored.models.dto.CourseDto;
import com.idiotnation.raspored.models.dto.CourseFilterDto;
import com.idiotnation.raspored.models.dto.CourseTypeDto;
import com.j256.ormlite.dao.Dao;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.functions.Function;
import retrofit2.Response;

public class CourseService {

    private Dao<Course, Integer> courseDao;
    private Dao<CourseType, Integer> courseTypeDao;
    private com.idiotnation.raspored.dataaccess.api.CourseService courseService;

    public CourseService() {
        courseDao = DatabaseManager.courseDao;
        courseTypeDao = DatabaseManager.courseTypeDao;
        courseService = ServiceGenerator.createService(com.idiotnation.raspored.dataaccess.api.CourseService.class);
    }

    public Single<List<CourseDto>> getAll() {
        return Single
                .fromCallable(new Callable<List<Course>>() {
                    @Override
                    public List<Course> call() throws Exception {
                        return courseDao
                                .queryBuilder()
                                .orderBy("course_type_id", true)
                                .orderBy("name", false)
                                .query();
                    }
                })
                .map(new Function<List<Course>, List<CourseDto>>() {
                    @Override
                    public List<CourseDto> apply(List<Course> courses) {
                        return Utils.convertToDto(courses, CourseDto.class);
                    }
                });
    }

    public Single<List<CourseTypeDto>> syncLatest(final CourseFilterDto courseFilterDto) {
        return Single.fromCallable(new Callable<List<CourseTypeDto>>() {
            @Override
            public List<CourseTypeDto> call() {
                try {
                    Response<List<CourseDto>> response = courseService
                            .getLatestSynchronous(courseFilterDto)
                            .execute();
                    if (response.code() == 200) {
                        final List<CourseDto> synced = response.body();
                        courseDao.callBatchTasks(new Callable<Void>() {

                            @Override
                            public Void call() throws Exception {
                                for (CourseDto courseDto : synced) {
                                    courseTypeDao.createIfNotExists(courseDto.getType().toPojo());
                                    courseDao.createIfNotExists(courseDto.toPojo());
                                }
                                return null;
                            }
                        });
                    }
                    List<CourseTypeDto> courses = Utils.convertToDto(
                            courseTypeDao
                                    .queryBuilder()
                                    .distinct()
                                    .query(),
                            CourseTypeDto.class);
                    if (courses.size() == 0) {
                        throw new NullPointerException();
                    } else {
                        return courses;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }

    public Single<CourseDto> getById(final Integer id) {
        return Single.fromCallable(new Callable<CourseDto>() {
            @Override
            public CourseDto call() throws Exception {
                Course pojo = courseDao.queryForId(id);
                if (pojo != null) {
                    return new CourseDto();
                }
                return null;
            }
        });
    }
}
