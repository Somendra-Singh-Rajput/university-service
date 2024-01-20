package com.adminease.dao;

import com.adminease.model.management.Subject;
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
public class SubjectDao {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectDao.class);

    private static final String INSERT_SUBJECT="INSERT INTO subjects (subj_id, subj_name, course_id, sem_id, create_ts, update_ts, create_by, update_by) VALUE (?,?,?,?,?,?)";

    private static final String SELECT_SUBJECT="SELECT * FROM subjects where subj_id = ?";

    private static final String UPDATE_SUBJECT="UPDATE subjects set subj_name=?, course_id=?, sem_id=?, create_ts,=?, update_ts=?, create_by=?, update_by=? WHERE subj_id=?";

    private static final String DELETE_SUBJECT_BY_ID = "DELETE FROM subjects WHERE subj_id = ?";

    private static final String SELECT_ALL_SUBJECT="SELECT * FROM subjects";

    private static final String QUERY_TO_CHECK_EXIST_SUBJECT="SELECT * FROM subjects where subj_name=?";

    private final DataSource dataSource;

    public SubjectDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int saveSubjectData(Subject subject, String createBy){
        int insertStatus = 0;
        LOG.info("Saving subject data into DB for subjId: {}", subject.getSubjId());
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement
                = connection.prepareStatement(INSERT_SUBJECT, Statement.RETURN_GENERATED_KEYS)) {
            insertStatement(subject, preparedStatement, createBy);

            insertStatus = preparedStatement.executeUpdate();
            if(insertStatus > 0){
                LOG.info("Saved subject data into DB successfully for subjId: {}", subject.getSubjId());
            }
        } catch (Exception ex) {
            LOG.error("Unable to save subject data into DB for subjId: {}, Exception: {}", subject.getSubjId(), ExceptionUtils.getStackTrace(ex));
        }

        return insertStatus;
    }

    private void insertStatement(Subject subject, PreparedStatement preparedStatement, String createBy) throws SQLException {
        preparedStatement.setString(1, subject.getSubjId());
        preparedStatement.setString(2, subject.getSubjName());
        preparedStatement.setString(3, subject.getCourseId());
        preparedStatement.setString(4, subject.getSemId());
        preparedStatement.setString(5, subject.getCreateTs());
        preparedStatement.setString(6, String.valueOf(LocalDateTime.now()));
        preparedStatement.setString(7, createBy);
        preparedStatement.setString(8, createBy);
    }

    public Subject getSubjectDataById(String subjId){
        Subject subject = new Subject();

        LOG.info("Fetching subject data from DB for subjId: {}", subjId);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_SUBJECT, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1,subjId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(Objects.nonNull(resultSet)){
                while (resultSet.next()){
                    populateData(subject, resultSet);
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to fetch subject data from DB for subjId: {}, Exception: {}", subjId, ExceptionUtils.getStackTrace(ex));
        }

        return subject;
    }

    private void populateData(Subject subject, ResultSet resultSet) throws SQLException {
        subject.setSubjId(resultSet.getString("subj_id"));
        subject.setSubjName(resultSet.getString("subj_name"));
        subject.setCourseId(resultSet.getString("course_id"));
        subject.setSemId(resultSet.getString("sem_id"));
        subject.setCreateTs(resultSet.getString("create_ts"));
        subject.setUpdateTs(resultSet.getString("update_ts"));
        subject.setCreateBy(resultSet.getString("create_by"));
        subject.setUpdateBy(resultSet.getString("update_by"));
    }

    public List<Subject> getAllSubjectsData() {
        List<Subject> subjectList = new ArrayList<>();

        LOG.info("Fetching all subjects data from DB");
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_SUBJECT, Statement.RETURN_GENERATED_KEYS)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            if(Objects.nonNull(resultSet)){
                while (resultSet.next()){
                    Subject subject = new Subject();
                    populateData(subject, resultSet);
                    subjectList.add(subject);
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to fetch all subjects data from DB, Exception: {}", ExceptionUtils.getStackTrace(ex));
        }
        return subjectList;
    }

    public int deleteSubjectDataById(String subjId) {
        int deleteStatus = 0;
        LOG.info("Deleting subject data from DB for subjId: {}", subjId);
        Subject subject = getSubjectDataById(subjId);

        if(Objects.nonNull(subject)){
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement preparedStatement
                         = connection.prepareStatement(DELETE_SUBJECT_BY_ID, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1,subjId);
                deleteStatus = preparedStatement.executeUpdate();
            } catch (Exception ex) {
                LOG.error("Unable to delete subject data from DB for subjId: {}, Exception: {}", subjId, ExceptionUtils.getStackTrace(ex));
            }
        } else {
            LOG.info("There is no subject found for given subjId: {} to delete", subjId);
        }
        return deleteStatus;
    }

    public int updateSubjectDataById(Subject subject, String subjId, String updateBy){
        int updateStatus = 0;
        Subject existingSubData = getSubjectDataById(subjId);
        LOG.info("Updating course data into DB for subjId: {}", subjId);

        if(Objects.nonNull(existingSubData)){
            try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement
                    = connection.prepareStatement(UPDATE_SUBJECT, Statement.RETURN_GENERATED_KEYS)) {
                updateStatement(existingSubData, subject, preparedStatement, subjId, updateBy);

                updateStatus = preparedStatement.executeUpdate();
                if(updateStatus > 0){
                    LOG.info("Subject data updated into DB for subjId: {}", subjId);
                }
            } catch (Exception ex) {
                LOG.error("Unable to update subject data into DB for subjId: {}, Exception: {}", subjId, ExceptionUtils.getStackTrace(ex));
            }
        }

        return updateStatus;
    }

    private void updateStatement(Subject existingSubData, Subject subject,
                                 PreparedStatement preparedStatement, String subjId, String updateBy) throws SQLException {
        preparedStatement.setString(1, StringUtils.isNotEmpty(subject.getSubjName()) ? subject.getSubjName() : existingSubData.getSubjName());
        preparedStatement.setString(2, StringUtils.isNotEmpty(subject.getCourseId()) ? subject.getCourseId() : existingSubData.getCourseId());
        preparedStatement.setString(3, StringUtils.isNotEmpty(subject.getSemId()) ? subject.getSemId() : existingSubData.getSemId());
        preparedStatement.setString(4, StringUtils.isNotEmpty(subject.getCreateTs()) ? subject.getCreateTs() : existingSubData.getCreateTs());
        preparedStatement.setString(5, String.valueOf(LocalDateTime.now()));
        preparedStatement.setString(6, StringUtils.isNotEmpty(subject.getCreateBy()) ? subject.getCreateBy() : existingSubData.getCreateBy());
        preparedStatement.setString(7, updateBy);
        preparedStatement.setString(8, subjId);
    }

    public Subject checkRegisteredSubject(String subjName){
        Subject subject = new Subject();

        LOG.info("Checkin subject data in DB if exists for subjName: {}", subjName);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(QUERY_TO_CHECK_EXIST_SUBJECT, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1,subjName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(Objects.nonNull(resultSet)){
                while (resultSet.next()){
                    populateData(subject, resultSet);
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to check subject data in DB for subjName: {}, Exception: {}", subjName, ExceptionUtils.getStackTrace(ex));
        }
        return subject;
    }
}
