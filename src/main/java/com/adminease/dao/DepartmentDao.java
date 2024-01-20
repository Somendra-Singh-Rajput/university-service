package com.adminease.dao;

import com.adminease.model.management.Department;
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
public class DepartmentDao {
    private static final Logger LOG = LoggerFactory.getLogger(DepartmentDao.class);

    private static final String INSERT_DEPTS="INSERT INTO departments (dept_id, dept_name, create_ts, update_ts, create_by, update_by) VALUE (?,?,?,?,?,?)";

    private static final String SELECT_DEPTS="SELECT * FROM departments where dept_id = ?";

    private static final String UPDATE_DEPTS="UPDATE departments set dept_name=?, create_ts=?, update_ts=?, create_by=?, update_by=? WHERE dept_id=?";

    private static final String DELETE_DEPTS_BY_ID = "DELETE FROM departments WHERE dept_id = ?";

    private static final String SELECT_ALL_DEPTS="SELECT * FROM departments";

    private static final String QUERY_TO_CHECK_EXIST_DEPTS="SELECT * FROM departments where dept_name=?";

    private final DataSource dataSource;

    public DepartmentDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int saveDepartmentData(Department department, String createBy){
        int insertStatus = 0;
        LOG.info("Saving departments data into DB for DeptId: {}", department.getDeptId());
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement
                = connection.prepareStatement(INSERT_DEPTS, Statement.RETURN_GENERATED_KEYS)) {
            insertStatement(department, preparedStatement, createBy);

            insertStatus = preparedStatement.executeUpdate();
            if(insertStatus > 0){
                LOG.info("Department data saved into DB successfully for deptId: {}", department.getDeptId());
            }
        } catch (Exception ex) {
            LOG.error("Unable to save department data into DB for deptId: {}, Exception: {}", department.getDeptId(), ExceptionUtils.getStackTrace(ex));
        }
        
        return insertStatus;
    }

    private void insertStatement(Department department, PreparedStatement preparedStatement,
                                 String createBy) throws SQLException {
        preparedStatement.setString(1, department.getDeptId());
        preparedStatement.setString(2, department.getDeptName());
        preparedStatement.setString(3, department.getCreateTs());
        preparedStatement.setString(4, String.valueOf(LocalDateTime.now()));
        preparedStatement.setString(5, createBy);
        preparedStatement.setString(6, createBy);
    }

    public Department getDepartmentDataById(String deptId){
        Department department = new Department();

        LOG.info("Fetching department data from DB for deptId: {}", deptId);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_DEPTS, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1,deptId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(Objects.nonNull(resultSet)){
                while (resultSet.next()){
                    populateData(department, resultSet);
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to fetch department data from DB for deptId: {}, Exception: {}", deptId, ExceptionUtils.getStackTrace(ex));
        }
        return department;
    }

    private void populateData(Department department, ResultSet resultSet) throws SQLException {
        department.setDeptId(resultSet.getString("dept_id"));
        department.setDeptName(resultSet.getString("dept_name"));
        department.setCreateTs(resultSet.getString("create_ts"));
        department.setUpdateTs(resultSet.getString("update_ts"));
        department.setCreateBy(resultSet.getString("create_by"));
        department.setUpdateBy(resultSet.getString("update_by"));
    }

    public List<Department> getAllDepartmentsData() {
        List<Department> departmentList = new ArrayList<>();

        LOG.info("Fetching all departments data from DB");
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_DEPTS, Statement.RETURN_GENERATED_KEYS)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            if(Objects.nonNull(resultSet)){
                while (resultSet.next()){
                    Department department = new Department();
                    populateData(department, resultSet);
                    departmentList.add(department);
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to fetch all departments data from DB, Exception: {}", ExceptionUtils.getStackTrace(ex));
        }
        return departmentList;
    }

    public int deleteDepartmentDataById(String deptId) {
        int deleteStatus = 0;
        LOG.info("Deleting department data from DB for deptId: {}", deptId);
        Department department = getDepartmentDataById(deptId);

        if(Objects.nonNull(department)){
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement preparedStatement
                         = connection.prepareStatement(DELETE_DEPTS_BY_ID, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1,deptId);
                deleteStatus = preparedStatement.executeUpdate();
            } catch (Exception ex) {
                LOG.error("Unable to delete department data from DB for deptId: {}, Exception: {}", deptId, ExceptionUtils.getStackTrace(ex));
            }
        } else {
            LOG.info("There is no department found for given deptId: {} to delete", deptId);
        }
        return deleteStatus;
    }

    public int updateDepartmentById(Department department, String deptId, String updateBy){
        int updateStatus = 0;
        Department existingDeptData = getDepartmentDataById(deptId);
        LOG.info("Updating department data into DB for deptId: {}", deptId);

        if(Objects.nonNull(existingDeptData)){
            try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement
                    = connection.prepareStatement(UPDATE_DEPTS, Statement.RETURN_GENERATED_KEYS)) {
                updateStatement(existingDeptData, department, preparedStatement, deptId, updateBy);

                updateStatus = preparedStatement.executeUpdate();
                if(updateStatus > 0){
                    LOG.info("Department data updated into DB for deptId: {}", deptId);
                }
            } catch (Exception ex) {
                LOG.error("Unable to update department data into DB for deptId: {}, Exception: {}", deptId, ExceptionUtils.getStackTrace(ex));
            }
        }

        return updateStatus;
    }

    private void updateStatement(Department existingDeptData, Department department,
                                 PreparedStatement preparedStatement, String deptId, String updateBy) throws SQLException {
        preparedStatement.setString(1, StringUtils.isNotEmpty(department.getDeptName()) ? department.getDeptName() : existingDeptData.getDeptName());
        preparedStatement.setString(2, StringUtils.isNotEmpty(department.getCreateTs()) ? department.getCreateTs() : existingDeptData.getCreateTs());
        preparedStatement.setString(3, String.valueOf(LocalDateTime.now()));
        preparedStatement.setString(4, StringUtils.isNotEmpty(department.getCreateBy()) ? department.getCreateBy() : existingDeptData.getCreateBy());
        preparedStatement.setString(5, updateBy);
        preparedStatement.setString(6, deptId);
    }

    public Department checkRegisteredDept(String deptName){
        Department department = new Department();

        LOG.info("Checking department data in DB if exists for deptName: {}", deptName);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(QUERY_TO_CHECK_EXIST_DEPTS, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1,deptName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(Objects.nonNull(resultSet)){
                while (resultSet.next()){
                    populateData(department, resultSet);
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to check department data in DB for deptName: {}, Exception: {}", deptName, ExceptionUtils.getStackTrace(ex));
        }
        return department;
    }
}
