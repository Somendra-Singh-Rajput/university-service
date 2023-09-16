package com.apnaclassroom.service;

import com.apnaclassroom.dao.CourseDao;
import com.apnaclassroom.model.management.Course;
import com.apnaclassroom.model.user.RegisterResponse;
import com.apnaclassroom.util.RandomIdGenerator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.apnaclassroom.model.CommonConstants.COURSE;
import static com.apnaclassroom.model.CommonConstants.COURSEID;

@Service
public class CourseService {

    private static final Logger LOG = LoggerFactory.getLogger(CourseService.class);

    private final CourseDao courseDao;

    public CourseService(CourseDao courseDao) {
        this.courseDao = courseDao;
    }

    public RegisterResponse addCourseData(Course course, String createBy) {
        RegisterResponse response;
        Course exitingCourseData = courseDao.checkRegisteredCourse(course.getCourseName());

        if(StringUtils.isEmpty(exitingCourseData.getCourseId()) && StringUtils.isEmpty(exitingCourseData.getCourseName())){
            course.setCourseId(RandomIdGenerator.generateId(COURSE));
            course.setCreateTs(String.valueOf(LocalDateTime.now()));
            int insertStatus = courseDao.saveCourseData(course, createBy);
            if(insertStatus > 0){
                response = RegisterResponse.builder()
                        .status(HttpStatus.OK.name())
                        .id(COURSEID+": "+course.getCourseId())
                        .message("Course data saved successfully!").build();
            } else {
                response = RegisterResponse.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                        .id(COURSEID+": "+course.getCourseId())
                        .message("Failed to save course data!").build();
            }
        } else{
            response = RegisterResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                    .id(COURSEID+": "+course.getCourseId())
                    .message("With this courseId data already exits in DB.").build();
        }
        return response;
    }

    public Course getCourseDataById(String courseId) {
        Course course = courseDao.getCourseDataById(courseId);
        if(Objects.nonNull(course)){
            return course;
        } else{
            LOG.info("There is no course data found for courseId: {}", courseId);
            return null;
        }
    }

    public List<Course> getAllCoursesData() {
        List<Course> courseList = courseDao.getAllCoursesData();

        if(!CollectionUtils.isEmpty(courseList)){
            return courseList;
        } else {
            LOG.info("There is no course data found in DB");
            return Collections.emptyList();
        }
    }

    public RegisterResponse deleteCourseDataById(String courseId) {
        int deleteStatus = courseDao.deleteCourseDataById(courseId);
        if(deleteStatus > 0){
            return RegisterResponse.builder()
                    .status(HttpStatus.OK.name())
                    .id(COURSEID+": " + courseId)
                    .message("Course data deleted successfully!").build();
        } else {
            return RegisterResponse.builder()
                    .status(HttpStatus.OK.name())
                    .id(COURSEID+": " + courseId)
                    .message("Failed to delete course data!").build();
        }
    }

    public RegisterResponse updateCourseDataById(Course course, String courseId, String updateBy) {
        int updateStatus = courseDao.updateCourseDataById(course, courseId, updateBy);

        if(updateStatus > 0){
            return RegisterResponse.builder()
                    .status(HttpStatus.OK.name())
                    .id(COURSEID+": " + courseId)
                    .message("Course data updated successfully!").build();
        } else {
            return RegisterResponse.builder()
                    .status(HttpStatus.OK.name())
                    .id(COURSEID+": " + courseId)
                    .message("Failed to update course data!").build();
        }
    }
}
