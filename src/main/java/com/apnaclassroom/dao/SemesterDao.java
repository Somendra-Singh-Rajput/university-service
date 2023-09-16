package com.apnaclassroom.dao;

import com.apnaclassroom.model.management.Semester;
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
public class SemesterDao {

    private static final Logger LOG = LoggerFactory.getLogger(SemesterDao.class);

    private static final String INSERT_SEMESTER="INSERT INTO SEMESTERS (sem_id, sem_name, create_ts, update_ts, create_by, update_by) VALUE (?,?,?,?,?,?)";

    private static final String SELECT_SEMESTER="SELECT * FROM SEMESTERS where sem_id = ?";

    private static final String UPDATE_SEMESTER="UPDATE SEMESTERS set sem_name=?, create_ts=?, update_ts=?, create_by=?, update_by=? WHERE sem_id=?";

    private static final String DELETE_SEMESTER_BY_ID = "DELETE FROM SEMESTERS WHERE sem_id = ?";

    private static final String SELECT_ALL_SEMESTERS="SELECT * FROM SEMESTERS";

    private static final String QUERY_TO_CHECK_EXIST_SEMESTERS="SELECT * FROM SEMESTERS where sem_name=?";

    private final DataSource dataSource;

    public SemesterDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int saveSemesterData(Semester semester, String createBy){
        int insertStatus = 0;
        LOG.info("Saving semester data into DB for semId: {}", semester.getSemId());
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement
                = connection.prepareStatement(INSERT_SEMESTER, Statement.RETURN_GENERATED_KEYS)) {
            insertStatement(semester, preparedStatement, createBy);

            insertStatus = preparedStatement.executeUpdate();
            if(insertStatus > 0){
                LOG.info("Saved semester data into DB successfully for semId: {}", semester.getSemId());
            }
        } catch (Exception ex) {
            LOG.error("Unable to save semester data into DB for semId: {}, Exception: {}", semester.getSemId(), ExceptionUtils.getStackTrace(ex));
        }

        return insertStatus;
    }

    private void insertStatement(Semester semester, PreparedStatement preparedStatement, String createBy) throws SQLException {
        preparedStatement.setString(1, semester.getSemId());
        preparedStatement.setString(2, semester.getSemName());
        preparedStatement.setString(3, semester.getCreateTs());
        preparedStatement.setString(4, String.valueOf(LocalDateTime.now()));
        preparedStatement.setString(5, createBy);
        preparedStatement.setString(6, createBy);
    }

    public Semester getSemesterDataById(String semId){
        Semester semester = new Semester();

        LOG.info("Fetching semester data from DB for semId: {}", semId);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_SEMESTER, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1,semId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(Objects.nonNull(resultSet)){
                while (resultSet.next()){
                    populateData(semester, resultSet);
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to fetch semester data from DB for semId: {}, Exception: {}", semId, ExceptionUtils.getStackTrace(ex));
        }

        return semester;
    }

    private void populateData(Semester semester, ResultSet resultSet) throws SQLException {
        semester.setSemId(resultSet.getString("sem_id"));
        semester.setSemName(resultSet.getString("sem_name"));
        semester.setCreateTs(resultSet.getString("create_ts"));
        semester.setUpdateTs(resultSet.getString("update_ts"));
        semester.setCreateBy(resultSet.getString("create_by"));
        semester.setUpdateBy(resultSet.getString("update_by"));
    }

    public List<Semester> getAllSemestersData() {
        List<Semester> semesterList = new ArrayList<>();

        LOG.info("Fetching all semesters data from DB");
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_SEMESTERS, Statement.RETURN_GENERATED_KEYS)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            if(Objects.nonNull(resultSet)){
                while (resultSet.next()){
                    Semester semester = new Semester();
                    populateData(semester, resultSet);
                    semesterList.add(semester);
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to fetch all semesters data from DB, Exception: {}", ExceptionUtils.getStackTrace(ex));
        }
        return semesterList;
    }

    public int deleteSemesterDataById(String semId) {
        int deleteStatus = 0;
        LOG.info("Deleting semester data from DB for semId: {}", semId);
        Semester semester = getSemesterDataById(semId);

        if(Objects.nonNull(semester)){
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement preparedStatement
                         = connection.prepareStatement(DELETE_SEMESTER_BY_ID, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1,semId);
                deleteStatus = preparedStatement.executeUpdate();
            } catch (Exception ex) {
                LOG.error("Unable to delete semester data from DB for semId: {}, Exception: {}", semId, ExceptionUtils.getStackTrace(ex));
            }
        } else {
            LOG.info("There is no semester found for given semId: {} to delete", semId);
        }
        return deleteStatus;
    }

    public int updateSemesterDataById(Semester semester, String semId, String updateBy){
        int updateStatus = 0;
        Semester existingSemData = getSemesterDataById(semId);
        LOG.info("Updating course data into DB for semId: {}", semId);

        if(Objects.nonNull(existingSemData)){
            try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement
                    = connection.prepareStatement(UPDATE_SEMESTER, Statement.RETURN_GENERATED_KEYS)) {
                updateStatement(existingSemData, semester, preparedStatement, semId, updateBy);

                updateStatus = preparedStatement.executeUpdate();
                if(updateStatus > 0){
                    LOG.info("Semester data updated into DB for semId: {}", semId);
                }
            } catch (Exception ex) {
                LOG.error("Unable to update semester data into DB for semId: {}, Exception: {}", semId, ExceptionUtils.getStackTrace(ex));
            }
        }

        return updateStatus;
    }

    private void updateStatement(Semester existingSemData, Semester semester,
                                 PreparedStatement preparedStatement, String semId, String updateBy) throws SQLException {
        preparedStatement.setString(1, StringUtils.isNotEmpty(semester.getSemName()) ? semester.getSemName() : existingSemData.getSemName());
        preparedStatement.setString(2, StringUtils.isNotEmpty(semester.getCreateTs()) ? semester.getCreateTs() : existingSemData.getCreateTs());
        preparedStatement.setString(3, String.valueOf(LocalDateTime.now()));
        preparedStatement.setString(4, StringUtils.isNotEmpty(semester.getCreateBy()) ? semester.getCreateBy() : existingSemData.getCreateBy());
        preparedStatement.setString(5, updateBy);
        preparedStatement.setString(5, semId);
    }

    public Semester checkRegisteredSem(String semName){
        Semester semester = new Semester();

        LOG.info("Checking semester data in DB if exists for semName: {}", semName);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(QUERY_TO_CHECK_EXIST_SEMESTERS, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1,semName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(Objects.nonNull(resultSet)){
                while (resultSet.next()){
                    populateData(semester, resultSet);
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to check semester data in DB for semName: {}, Exception: {}", semName, ExceptionUtils.getStackTrace(ex));
        }
        return semester;
    }
}
