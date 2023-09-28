package com.apnaclassroom.controller;

import com.apnaclassroom.model.management.Course;
import com.apnaclassroom.model.user.RegisterResponse;
import com.apnaclassroom.model.user.User;
import com.apnaclassroom.service.CourseService;
import com.mysql.cj.util.StringUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/course")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@SecurityRequirement(name = "bearerAuth")
public class CourseController {
    private static final Logger LOG = LoggerFactory.getLogger(CourseController.class);

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public RegisterResponse addCourse(@RequestBody Course course,
                                          @AuthenticationPrincipal User user) {
        RegisterResponse response = null;
        LOG.info("Inside adding course data method");
        if(Objects.nonNull(course) && !StringUtils.isNullOrEmpty(user.getUsername())){
            response = courseService.addCourseData(course, user.getUsername());
        }
        return response;
    }

    @GetMapping(path = "/get/{courseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Course getCourseById(@PathVariable String courseId){
        Course course = null;
        LOG.info("Inside fetching course data method for deptId: {}", courseId);

        if(!StringUtils.isNullOrEmpty(courseId)){
            course =  courseService.getCourseDataById(courseId);
        }
        return course;
    }

    @GetMapping(path = "/getAllCourses/", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Course> getAllCourses(){
        LOG.info("Inside fetching all courses data method");
        return courseService.getAllCoursesData();
    }

    @DeleteMapping(value = "/delete/{courseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RegisterResponse deleteCourseById(@PathVariable String courseId){
        RegisterResponse response = null;
        LOG.info("Inside deleting course data method for deptId: {}", courseId);
        if(!StringUtils.isNullOrEmpty(courseId)){
            response = courseService.deleteCourseDataById(courseId);
        }
        return response;
    }

    @PutMapping(value = "/update/{courseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RegisterResponse updateCourseById(@RequestBody Course course, @PathVariable String courseId,
                                                 @AuthenticationPrincipal User user){
        RegisterResponse response = null;
        LOG.info("Inside updating course data method for deptId: {}", courseId);
        if(!StringUtils.isNullOrEmpty(courseId) && Objects.nonNull(course)
                && !StringUtils.isNullOrEmpty(user.getUsername())){
            response = courseService.updateCourseDataById(course, courseId, user.getUsername());
        }
        return response;
    }
}
