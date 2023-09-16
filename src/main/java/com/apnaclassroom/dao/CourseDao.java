package com.apnaclassroom.dao;

import com.apnaclassroom.model.management.Course;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class CourseDao {

    private static final Logger LOG = LoggerFactory.getLogger(CourseDao.class);

    private static final String INSERT_COURSE="INSERT INTO COURSES (course_id, course_name, dept_id, duration, create_ts, update_ts, create_by, update_by) VALUE (?,?,?,?,?,?,?,?)";

    private static final String SELECT_COURSE="SELECT * FROM COURSES where course_id = ?";

    private static final String UPDATE_COURSE="UPDATE COURSES set course_name=?, dept_id=?, duration=?, create_ts=?, update_ts=?, create_by=?, update_by=? WHERE course_id=?";

    private static final String DELETE_COURSE_BY_ID = "DELETE FROM COURSES WHERE course_id = ?";

    private static final String SELECT_ALL_COURSES="SELECT * FROM COURSES";

    private static final String QUERY_TO_CHECK_EXIST_COURSES="SELECT * FROM COURSES where course_name=?";

    private final DataSource dataSource;

    public CourseDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int saveCourseData(Course course, String createBy){
        int insertStatus = 0;
        LOG.info("Saving course data into DB for courseId: {}", course.getCourseId());
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement
                = connection.prepareStatement(INSERT_COURSE, Statement.RETURN_GENERATED_KEYS)) {
            insertStatement(course, preparedStatement, createBy);

            insertStatus = preparedStatement.executeUpdate();
            if(insertStatus > 0){
                LOG.info("Saved course data into DB successfully for courseId: {}", course.getCourseId());
            }
        } catch (Exception ex) {
            LOG.error("Unable to save course data into DB for courseId: {}, Exception: {}", course.getCourseId(), ExceptionUtils.getStackTrace(ex));
        }
        
        return insertStatus;
    }

    private void insertStatement(Course course, PreparedStatement preparedStatement, String createBy) throws SQLException {
        preparedStatement.setString(1, course.getCourseId());
        preparedStatement.setString(2, course.getCourseName());
        preparedStatement.setString(3, course.getDeptId());
        preparedStatement.setString(4, course.getCourseDuration());
        preparedStatement.setString(5, course.getCreateTs());
        preparedStatement.setString(6, String.valueOf(LocalDateTime.now()));
        preparedStatement.setString(7, createBy);
        preparedStatement.setString(8, createBy);
    }

    public Course getCourseDataById(String courseId){
        Course course = new Course();

        LOG.info("Fetching course data from DB for courseId: {}", courseId);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_COURSE, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1,courseId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(Objects.nonNull(resultSet)){
                while (resultSet.next()){
                    populateData(course, resultSet);
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to fetch course data from DB for courseId: {}, Exception: {}", courseId, ExceptionUtils.getStackTrace(ex));
        }

        return course;
    }

    private void populateData(Course course, ResultSet resultSet) throws SQLException {
        course.setCourseId(resultSet.getString("course_id"));
        course.setCourseName(resultSet.getString("course_name"));
        course.setDeptId(resultSet.getString("dept_id"));
        course.setCourseDuration(resultSet.getString("duration"));
        course.setCreateTs(resultSet.getString("create_ts"));
        course.setUpdateTs(resultSet.getString("update_ts"));
        course.setCreateBy(resultSet.getString("create_by"));
        course.setUpdateBy(resultSet.getString("update_by"));
    }

    public List<Course> getAllCoursesData() {
        List<Course> courseList = new ArrayList<>();

        LOG.info("Fetching all courses data from DB");
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_COURSES, Statement.RETURN_GENERATED_KEYS)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            if(Objects.nonNull(resultSet)){
                while (resultSet.next()){
                    Course course = new Course();
                    populateData(course, resultSet);
                    courseList.add(course);
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to fetch all courses data from DB, Exception: {}", ExceptionUtils.getStackTrace(ex));
        }
        return courseList;
    }

    public int deleteCourseDataById(String courseId) {
        int deleteStatus = 0;
        LOG.info("Deleting course data from DB for courseId: {}", courseId);
        Course course = getCourseDataById(courseId);

        if(Objects.nonNull(course)){
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement preparedStatement
                         = connection.prepareStatement(DELETE_COURSE_BY_ID, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1,courseId);
                deleteStatus = preparedStatement.executeUpdate();
            } catch (Exception ex) {
                LOG.error("Unable to delete course data from DB for courseId: {}, Exception: {}", courseId, ExceptionUtils.getStackTrace(ex));
            }
        } else {
            LOG.info("There is no course found for given courseId: {} to delete", courseId);
        }
        return deleteStatus;
    }

    public int updateCourseDataById(Course course, String courseId, String updateBy){
        int updateStatus = 0;
        Course existingCourseData = getCourseDataById(courseId);
        LOG.info("Updating course data into DB for courseId: {}", courseId);

        if(Objects.nonNull(existingCourseData)){
            try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement
                    = connection.prepareStatement(UPDATE_COURSE, Statement.RETURN_GENERATED_KEYS)) {
                updateStatement(existingCourseData, course, preparedStatement, courseId, updateBy);

                updateStatus = preparedStatement.executeUpdate();
                if(updateStatus > 0){
                    LOG.info("Course data updated into DB for courseId: {}", courseId);
                }
            } catch (Exception ex) {
                LOG.error("Unable to update course data into DB for courseId: {}, Exception: {}", courseId, ExceptionUtils.getStackTrace(ex));
            }
        }

        return updateStatus;
    }

    private void updateStatement(Course existingCourseData, Course course,
                                 PreparedStatement preparedStatement, String courseId, String updateBy) throws SQLException {
        preparedStatement.setString(1, StringUtils.isNotEmpty(course.getCourseName()) ? course.getCourseName() : existingCourseData.getCourseName());
        preparedStatement.setString(2, StringUtils.isNotEmpty(course.getDeptId()) ? course.getDeptId() : existingCourseData.getDeptId());
        preparedStatement.setString(3, StringUtils.isNotEmpty(course.getCourseDuration()) ? course.getCourseDuration() : existingCourseData.getCourseDuration());
        preparedStatement.setString(4, StringUtils.isNotEmpty(course.getCreateTs()) ? course.getCreateTs() : existingCourseData.getCreateTs());
        preparedStatement.setString(5, String.valueOf(LocalDateTime.now()));
        preparedStatement.setString(6, StringUtils.isNotEmpty(course.getCreateBy()) ? course.getCreateBy() : existingCourseData.getCreateBy());
        preparedStatement.setString(7, updateBy);
        preparedStatement.setString(8, courseId);
    }

    public Course checkRegisteredCourse(String courseName){
        Course course = new Course();

        LOG.info("Checking course data in DB if exists for courseName: {}", courseName);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(QUERY_TO_CHECK_EXIST_COURSES, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1,courseName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(Objects.nonNull(resultSet)){
                while (resultSet.next()){
                    populateData(course, resultSet);
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to check course data in DB for courseName: {}, Exception: {}", courseName, ExceptionUtils.getStackTrace(ex));
        }

        return course;
    }
}
