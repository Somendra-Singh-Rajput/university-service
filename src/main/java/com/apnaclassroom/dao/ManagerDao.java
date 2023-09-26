package com.apnaclassroom.dao;

import com.apnaclassroom.enums.Role;
import com.apnaclassroom.model.Address;
import com.apnaclassroom.model.management.Manager;
import com.apnaclassroom.model.user.User;
import com.apnaclassroom.util.PasswordGenerator;
import com.apnaclassroom.util.RandomIdGenerator;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import static com.apnaclassroom.enums.Role.MANAGER;
import static com.apnaclassroom.model.CommonConstants.*;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Repository
public class ManagerDao {

    private static final Logger LOG = LoggerFactory.getLogger(ManagerDao.class);

    private static final String INSERT_MANAGER_TABLE = "INSERT INTO managers(manager_id, first_name, last_name, email, " +
            "phone, dob, gender, father_name, mother_name, street, city, state, country, profile_photo" +
            "doj, dol, role, isEnabled, create_ts, update_ts, create_by, update_by, " +
            "password, old_password, position, reporting_to, office_location, official_email) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String INSERT_MANAGER_HISTORY_TABLE = "INSERT INTO managers_audit(manager_id, first_name, last_name, email, " +
            "phone, dob, gender, father_name, mother_name, street, city, state, country, profile_photo" +
            "doj, dol, role, isEnabled, create_ts, update_ts, create_by, update_by, " +
            "password, old_password, position, reporting_to, office_location, official_email) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String SELECT_MANAGER_DATA_BY_ID = "SELECT * FROM managers WHERE manager_id = ?";

    private static final String SELECT_ALL_MANAGERS_DATA = "SELECT * FROM managers";

    private static final String DELETE_MANAGER_DATA_BY_ID = "DELETE FROM managers WHERE manager_id = ?";

    private static final String UPDATE_MANAGER_DATA_BY_ID = "UPDATE managers SET first_name=?, last_name=?, " +
            "phone=?, dob=?, gender=?, father_name=?, mother_name=?, street=?, city=?, state=?, country=?, " +
            "profile_photo=?, doj=?, dol=?, role=?, isEnabled=?, create_ts, update_ts=?," +
            "create_by, update_by=?, position=?, reporting_to=?, office_location=?, official_email=? WHERE manager_id = ?";

    private static final String QUERY_TO_CHECK_REGISTERED_MANAGER = "SELECT * FROM managers where first_name=? " +
            "AND phone=? AND father_name=? AND email=?";

    private final PasswordEncoder passwordEncoder;
    private final DataSource dataSource;
    final UserDao userDao;
    final CourseDao courseDao;
    final DepartmentDao departmentDao;

    public ManagerDao(PasswordEncoder passwordEncoder, final DataSource dataSource, UserDao userDao, CourseDao courseDao, DepartmentDao departmentDao){
        this.passwordEncoder = passwordEncoder;
        this.dataSource=dataSource;
        this.userDao = userDao;
        this.courseDao = courseDao;
        this.departmentDao = departmentDao;
    }

    public Map<String,String> saveManagerData(Manager manager, String createBy) {
        Map<String,String> managerInfoMap = new HashMap<>();
        String managerId;
        LOG.info("Checking if manager is already registered or not");

        Manager isRegisteredManager = checkRegisteredManager(manager);
        String password = PasswordGenerator.generateRandomPassword();
        managerInfoMap.put(PASSWORD, password);

        if(Objects.isNull(isRegisteredManager) || isEmpty(isRegisteredManager.getManagerId())){
            LOG.info("{} is a new manager", manager.getFirstName());

            //This method is to register new manager
            newManagerRegistration(manager, managerInfoMap, password, createBy);

        } else{
            LOG.info("{} is already registered manager", manager.getFirstName());
            managerId = isRegisteredManager.getManagerId();
            manager.setPassword(passwordEncoder.encode(password));
            manager.setCreateTs(String.valueOf(LocalDateTime.now()));
            manager.setCreateBy(createBy);
            manager.setUpdateBy(createBy);

            //Populating manager basic info to send in email
            populateManagerInfoMap(managerInfoMap, "Y", managerId, manager);

            Optional<User> user = userDao.getUserDataById(managerId);
            //Saving credentials in users table for login
            user.ifPresent(value -> userDao.resetUserPassword(manager.getManagerId(), manager.getPassword(),
                    value.getPassword(), manager.getUpdateBy()));
        }
        return managerInfoMap;
    }

    private void newManagerRegistration(Manager manager, Map<String, String> managerInfoMap, String password, String createBy) {
        User user = new User();
        String managerId;
        managerId = RandomIdGenerator.generateAdminOrManagerId(MANAGER.name());
        manager.setManagerId(managerId);
        manager.setPassword(passwordEncoder.encode(password));
        manager.setCreateTs(String.valueOf(LocalDateTime.now()));
        manager.setCreateBy(createBy);
        manager.setUpdateBy(createBy);

        //Populating manager basic info to send in email
        populateManagerInfoMap(managerInfoMap, "N", managerId, manager);

        //Saving manager data into DB
        saveManager(manager, managerId, false);

        //Populating user object from manager object
        populateUserObject(manager, user);

        //Saving credentials in users table for login
        userDao.saveUserCredentials(user);
    }

    private void populateUserObject(Manager manager, User user) {
        user.setUsername(manager.getManagerId());
        user.setEmail(manager.getEmail());
        user.setPassword(manager.getPassword());
        user.setOldPassword(manager.getOldPassword());
        user.setRole(manager.getRole());
        user.setEnabled(manager.isEnabled());
        user.setCreateTs(manager.getCreateTs());
        user.setUpdateTs(manager.getUpdateTs());
        user.setCreateBy(manager.getCreateBy());
        user.setUpdateBy(manager.getUpdateBy());
    }

    private void saveManager(Manager manager, String managerId, boolean isHistory) {
        boolean insertStatus;
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement
                = connection.prepareStatement(isHistory ? INSERT_MANAGER_HISTORY_TABLE : INSERT_MANAGER_TABLE, Statement.RETURN_GENERATED_KEYS)) {
            insertStatement(manager, preparedStatement, managerId);

            insertStatus = preparedStatement.execute();
            if(insertStatus){
                LOG.info("New manager data saved into DB successfully for managerId: {}", managerId);
            }
        } catch (Exception ex) {
            LOG.error("Unable to save new manager data into DB for managerId: {}, Exception: {}", managerId, ExceptionUtils.getStackTrace(ex));
        }
    }

    private void populateManagerInfoMap(Map<String, String> managerInfoMap, String isRegistered, String managerId, Manager manager) {
        managerInfoMap.put(IS_REGISTERED, isRegistered);
        managerInfoMap.put(MANAGER_ID, managerId);
        managerInfoMap.put(FIRST_NAME, manager.getFirstName());
        managerInfoMap.put(EMAIL_ID, manager.getEmail());
        managerInfoMap.put(CREATE_TS, manager.getCreateTs());
        managerInfoMap.put(OFFICIAL_EMAIL, manager.getOfficialEmail());
        managerInfoMap.put(OFFICE_LOCATION, manager.getOfficeLocation());
        managerInfoMap.put(POSITION, manager.getPosition());
        managerInfoMap.put(REPORTING_TO, manager.getReportingTo());
    }
    public Manager checkRegisteredManager(Manager manager){
        Address address = new Address();

        LOG.info("Checking manager data in DB for FirstName: {}, Phone: {}, " +
                "FatherName: {}, Email: {}", manager.getFirstName(), manager.getPhone(), manager.getFatherName(), manager.getEmail());
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement
                = connection.prepareStatement(QUERY_TO_CHECK_REGISTERED_MANAGER, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1,manager.getFirstName());
            preparedStatement.setString(2,manager.getPhone());
            preparedStatement.setString(3,manager.getFatherName());
            preparedStatement.setString(4,manager.getEmail());
            ResultSet resultSet = preparedStatement.executeQuery();

            if(Objects.nonNull(resultSet)){
                while (resultSet.next()){
                    populateData(manager, address, resultSet);
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to check manager data in DB for for FirstName: {}, Phone: {}, " +
                            "FatherName: {}, Email: {}, Exception: {}", manager.getFirstName(), manager.getPhone(),
                    manager.getFatherName(), manager.getEmail(),ExceptionUtils.getStackTrace(ex));
        }
        return manager;
    }

    private void insertStatement(Manager manager, PreparedStatement preparedStatement, String managerId) throws SQLException {
        preparedStatement.setString(1, managerId);
        preparedStatement.setString(2, manager.getFirstName());
        preparedStatement.setString(3, manager.getLastName());
        preparedStatement.setString(4, manager.getEmail());
        preparedStatement.setString(5, manager.getPhone());
        preparedStatement.setString(6, manager.getDob());
        preparedStatement.setString(7, manager.getGender());
        preparedStatement.setString(8, manager.getFatherName());
        preparedStatement.setString(9, manager.getMotherName());
        preparedStatement.setString(10, manager.getAddress().getStreet());
        preparedStatement.setString(11, manager.getAddress().getCity());
        preparedStatement.setString(12, manager.getAddress().getState());
        preparedStatement.setString(13, manager.getAddress().getCountry());
        preparedStatement.setString(14, manager.getProfilePhoto());
        preparedStatement.setString(15, manager.getDoj());
        preparedStatement.setString(16, manager.getDol());
        preparedStatement.setString(17, String.valueOf(Role.valueOf(String.valueOf(manager.getRole()))));
        preparedStatement.setBoolean(18, manager.isEnabled());
        preparedStatement.setString(19, manager.getCreateTs());
        preparedStatement.setString(20, String.valueOf(LocalDateTime.now()));
        preparedStatement.setString(21, manager.getCreateBy());
        preparedStatement.setString(22, manager.getUpdateBy());
        preparedStatement.setString(23, manager.getPassword());
        preparedStatement.setString(24, manager.getOldPassword());
        preparedStatement.setString(25, manager.getPosition());
        preparedStatement.setString(26, manager.getReportingTo());
        preparedStatement.setString(27, manager.getOfficeLocation());
        preparedStatement.setString(28, manager.getOfficialEmail());
    }

    public Manager getManagerDataById(String managerId){
        Manager manager = new Manager();
        Address address = new Address();

        LOG.info("Fetching manager data from DB for managerId: {}", managerId);
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement
                = connection.prepareStatement(SELECT_MANAGER_DATA_BY_ID, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1,managerId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(Objects.nonNull(resultSet)){
                while (resultSet.next()){
                    populateData(manager, address, resultSet);
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to get manager data from DB for managerId: {}, Exception: {}", managerId, ExceptionUtils.getStackTrace(ex));
        }

        return manager;
    }

    public List<Manager> getAllManagersData() {
        List<Manager> managerList = new ArrayList<>();

        LOG.info("Fetching all managers data from DB");
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_MANAGERS_DATA, Statement.RETURN_GENERATED_KEYS)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            if(Objects.nonNull(resultSet)){
                while (resultSet.next()){
                    Manager manager = new Manager();
                    Address address = new Address();
                    populateData(manager, address, resultSet);
                    managerList.add(manager);
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to fetch all managers data from DB, Exception: {}", ExceptionUtils.getStackTrace(ex));
        }
        return managerList;
    }

    private void populateData(Manager manager, Address address, ResultSet resultSet) throws SQLException {
        manager.setManagerId(resultSet.getString("manager_id"));
        manager.setFirstName(resultSet.getString("first_name"));
        manager.setLastName(resultSet.getString("last_name"));
        manager.setEmail(resultSet.getString("email"));
        manager.setPhone(resultSet.getString("phone"));
        manager.setDob(resultSet.getString("dob"));
        manager.setGender(resultSet.getString("gender"));
        manager.setFatherName(resultSet.getString("father_name"));
        manager.setMotherName(resultSet.getString("mother_name"));
        address.setStreet(resultSet.getString("street"));
        address.setCity(resultSet.getString("city"));
        address.setState(resultSet.getString("state"));
        address.setCountry(resultSet.getString("country"));
        manager.setAddress(address);
        manager.setDoj(resultSet.getString("doj"));
        manager.setDol(resultSet.getString("dol"));
        manager.setRole(Role.valueOf(resultSet.getString("role")));
        manager.setEnabled(resultSet.getBoolean("isEnabled"));
        manager.setCreateTs(resultSet.getString("create_ts"));
        manager.setUpdateTs(resultSet.getString("update_ts"));
        manager.setCreateBy(resultSet.getString("create_by"));
        manager.setUpdateBy(resultSet.getString("update_by"));
        manager.setPosition(resultSet.getString("position"));
        manager.setReportingTo(resultSet.getString("reporting_to"));
        manager.setOfficeLocation(resultSet.getString("office_location"));
        manager.setOfficialEmail(resultSet.getString("official_email"));
    }

    public int deleteManagerDataById(String managerId, String deleteBy) {
        int deleteStatus = 0;
        LOG.info("Deleting manager data from DB for managerId: {}", managerId);
        Manager manager = getManagerDataById(managerId);

        if(Objects.nonNull(manager)){
            //Saving data into history table
            manager.setCreateBy(deleteBy);
            manager.setUpdateBy(deleteBy);
            saveManager(manager,managerId,true);

            //Deleting data from user table also
            userDao.deleteUserDataById(managerId);

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement preparedStatement
                         = connection.prepareStatement(DELETE_MANAGER_DATA_BY_ID, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1,managerId);
                deleteStatus = preparedStatement.executeUpdate();
            } catch (Exception ex) {
                LOG.error("Unable to delete manager data from DB for managerId: {}, Exception: {}", managerId, ExceptionUtils.getStackTrace(ex));
            }
        }
        return deleteStatus;
    }

    public Manager updateManagerDataById(Manager manager, String managerId) {
        LOG.info("Updating manager data into DB for managerId: {}", managerId);

        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement
                = connection.prepareStatement(UPDATE_MANAGER_DATA_BY_ID, Statement.RETURN_GENERATED_KEYS)) {
            updateStatement(manager, preparedStatement, managerId);

            int updateStatus = preparedStatement.executeUpdate();
            if(updateStatus > 0){
                LOG.info("Manager data updated into DB for managerId: {}", managerId);
            }
        } catch (Exception ex) {
            LOG.error("Unable to update manager data into DB for managerId: {}, Exception: {}", managerId, ExceptionUtils.getStackTrace(ex));
        }
        return manager;
    }

    private void updateStatement(Manager manager, PreparedStatement preparedStatement, String managerId) throws SQLException {
        preparedStatement.setString(1, manager.getFirstName());
        preparedStatement.setString(2, manager.getLastName());
        preparedStatement.setString(3, manager.getEmail());
        preparedStatement.setString(4, manager.getPhone());
        preparedStatement.setString(5, manager.getDob());
        preparedStatement.setString(6, manager.getGender());
        preparedStatement.setString(7, manager.getFatherName());
        preparedStatement.setString(8, manager.getMotherName());

        preparedStatement.setString(9, manager.getAddress().getStreet());
        preparedStatement.setString(10, manager.getAddress().getCity());
        preparedStatement.setString(11, manager.getAddress().getState());
        preparedStatement.setString(12, manager.getAddress().getCountry());

        preparedStatement.setString(13, manager.getProfilePhoto());
        preparedStatement.setString(14, manager.getDoj());
        preparedStatement.setString(15, manager.getDol());
        preparedStatement.setString(16, String.valueOf(String.valueOf(manager.getRole())));
        preparedStatement.setBoolean(17, manager.isEnabled());
        preparedStatement.setString(18, manager.getCreateTs());
        preparedStatement.setString(19, String.valueOf(LocalDateTime.now()));
        preparedStatement.setString(20,  manager.getCreateBy());
        preparedStatement.setString(21, manager.getUpdateBy());
        preparedStatement.setString(22,  manager.getPosition());
        preparedStatement.setString(23,  manager.getReportingTo());
        preparedStatement.setString(24,  manager.getOfficeLocation());
        preparedStatement.setString(25,  manager.getOfficialEmail());
        preparedStatement.setString(26, managerId);
    }
}
