package com.idiotnation.raspored.services;

import com.idiotnation.raspored.dataaccess.api.ServiceGenerator;
import com.idiotnation.raspored.dataaccess.database.DatabaseManager;
import com.idiotnation.raspored.helpers.Utils;
import com.idiotnation.raspored.models.dto.CourseDto;
import com.idiotnation.raspored.models.dto.CourseFilterDto;
import com.idiotnation.raspored.models.jpa.Course;
import com.idiotnation.raspored.models.jpa.CourseType;
import com.j256.ormlite.dao.Dao;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
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

    public Single<List<CourseDto>> syncLatest(final CourseFilterDto courseFilterDto, final Integer filteredOutCourse) {
        return Single.fromCallable(new Callable<List<CourseDto>>() {
            @Override
            public List<CourseDto> call() throws Exception {
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
                        Collections.sort(synced, new Comparator<CourseDto>() {
                            @Override
                            public int compare(CourseDto o1, CourseDto o2) {
                                return o1.getName().compareTo(o2.getName());
                            }
                        });
                        if (filteredOutCourse != null) {
                            Iterator iterator = synced.iterator();
                            while (iterator.hasNext()) {
                                CourseDto courseDto = (CourseDto) iterator.next();
                                if (courseDto.getId().equals(filteredOutCourse)) {
                                    iterator.remove();
                                    break;
                                }
                            }
                        }
                        return synced;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    List<CourseDto> courses = Utils.convertToDto(
                            courseDao
                                    .queryBuilder()
                                    .orderBy("name", true)
                                    .where()
                                    .ne("id", filteredOutCourse)
                                    .query()
                            , CourseDto.class);
                    if (courses.size() == 0) {
                        throw e;
                    } else {
                        return courses;
                    }
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
