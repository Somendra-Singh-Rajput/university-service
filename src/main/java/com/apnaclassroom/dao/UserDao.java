package com.apnaclassroom.dao;

import com.apnaclassroom.enums.Role;
import com.apnaclassroom.model.user.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Repository
public class UserDao {

    private static final Logger LOG = LoggerFactory.getLogger(UserDao.class);

    private static final String INSERT_USER_CREDENTIALS="INSERT INTO users (user_id, email, password, old_password, role, " +
            "isenabled, create_ts, update_ts, create_by, update_by) VALUE (?,?,?,?,?,?,?,?,?,?)";

    private static final String SELECT_USER_CREDENTIALS="SELECT * FROM users where user_id = ?";

    private static final String UPDATE_USER_CREDENTIALS="UPDATE users set password=?, old_password=?, update_ts=?, update_by=? WHERE user_id=?";

    private static final String DELETE_USER_DATA_BY_ID = "DELETE FROM users WHERE user_id = ?";

    private final DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void saveUserCredentials(User user){
        boolean insertStatus;
        LOG.info("Saving user credentials data into DB for userId: {}", user.getUsername());
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement
                = connection.prepareStatement(INSERT_USER_CREDENTIALS, Statement.RETURN_GENERATED_KEYS)) {
            insertStatement(user, preparedStatement);

            insertStatus = preparedStatement.execute();
            if(insertStatus){
                LOG.info("User credentials saved into DB successfully for userId: {}", user.getUsername());
            }
        } catch (Exception ex) {
            LOG.error("Unable to save user credential data into DB for userId: {}, Exception: {}", user.getUsername(), ExceptionUtils.getStackTrace(ex));
        }
    }

    public void resetUserPassword(String userId, String password, String oldPassword, String updateBy){
        Optional<User> user = getUserDataById(userId);

        LOG.info("Updating user data into DB for UserId: {}", userId);
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement
                = connection.prepareStatement(UPDATE_USER_CREDENTIALS, Statement.RETURN_GENERATED_KEYS)) {
            updateUserStatement(user, preparedStatement, userId, password, oldPassword, updateBy);

            int updateStatus = preparedStatement.executeUpdate();
            if(updateStatus > 0){
                LOG.info("User credentials data updated into DB for UserId: {}", userId);
            }
        } catch (Exception ex) {
            LOG.error("Unable to update user credentials data into DB for userId: {}, Exception: {}", userId, ExceptionUtils.getStackTrace(ex));
        }
    }

    private void updateUserStatement(Optional<User> user, PreparedStatement preparedStatement, String userId, String password,
                                     String oldPassword, String updateBy) throws SQLException {
        preparedStatement.setString(1, password);
        if(StringUtils.isEmpty(oldPassword) && user.isPresent()){
            preparedStatement.setString(2, user.get().getOldPassword());
        } else{
            preparedStatement.setString(2, oldPassword);
        }
        preparedStatement.setString(3, String.valueOf(LocalDateTime.now()));
        preparedStatement.setString(4, updateBy);
        preparedStatement.setString(5, userId);
    }

    public Optional<User> getUserDataById(String userId){
        User user = new User();

        LOG.info("Fetching user credentials data from DB for UserId: {}", userId);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USER_CREDENTIALS, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1,userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(Objects.nonNull(resultSet)){
                while (resultSet.next()){
                    populateUserData(user, resultSet);
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to fetch user credentials data from DB for UserId: {}, Exception: {}", userId, ExceptionUtils.getStackTrace(ex));
        }

        return Optional.of(user);
    }

    private void populateUserData(User user, ResultSet resultSet) throws SQLException {
        user.setUsername(resultSet.getString("user_id"));
        user.setPassword(resultSet.getString("password"));
        user.setOldPassword(resultSet.getString("old_password"));
        user.setRole(Role.valueOf(resultSet.getString("role")));
        user.setEnabled(resultSet.getBoolean("isenabled"));
        user.setCreateTs(resultSet.getString("create_ts"));
        user.setUpdateTs(resultSet.getString("update_ts"));
    }

    public UserDetails saveUser(User user){
        boolean insertStatus;
        LOG.info("Saving user credentials data into DB for username: {}", user.getUsername());
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(INSERT_USER_CREDENTIALS, Statement.RETURN_GENERATED_KEYS)) {
            insertStatement(user, preparedStatement);

            insertStatus = preparedStatement.execute();
            if(insertStatus){
                LOG.info("User credentials saved into DB successfully for username: {}", user.getUsername());
            }
        } catch (Exception ex) {
            LOG.error("Unable to save user credential data into DB for username: {}, Exception: {}", user.getUsername(), ExceptionUtils.getStackTrace(ex));
        }
        return user;
    }

    private void insertStatement(User user, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, user.getUsername());
        preparedStatement.setString(2, user.getEmail());
        preparedStatement.setString(3, user.getPassword());
        preparedStatement.setString(4, user.getOldPassword());
        preparedStatement.setString(5, user.getRole().name());
        preparedStatement.setBoolean(6, user.isEnabled());
        preparedStatement.setString(7, user.getCreateTs());
        preparedStatement.setString(8, String.valueOf(LocalDateTime.now()));
        preparedStatement.setString(9, user.getCreateBy());
        preparedStatement.setString(10, user.getUpdateBy());
    }

    public void deleteUserDataById(String studentId) {
        LOG.info("Deleting user data from DB for userId: {}", studentId);
        Optional<User> user = getUserDataById(studentId);

        if(user.isPresent()){
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement preparedStatement
                         = connection.prepareStatement(DELETE_USER_DATA_BY_ID, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1,studentId);
                preparedStatement.executeUpdate();
            } catch (Exception ex) {
                LOG.error("Unable to delete user data from DB for userId: {}, Exception: {}", studentId, ExceptionUtils.getStackTrace(ex));
            }
        }
    }
}
