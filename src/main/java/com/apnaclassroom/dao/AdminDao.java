package com.apnaclassroom.dao;

import com.apnaclassroom.enums.Role;
import com.apnaclassroom.model.Address;
import com.apnaclassroom.model.Admin;
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

import static com.apnaclassroom.enums.Role.ADMIN;
import static com.apnaclassroom.model.CommonConstants.*;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Repository
public class AdminDao {

    private static final Logger LOG = LoggerFactory.getLogger(AdminDao.class);

    private static final String INSERT_ADMIN_TABLE = "INSERT INTO admins(admin_id, first_name, last_name, email, " +
            "phone, dob, gender, father_name, mother_name, street, city, state, country, profile_photo" +
            "doj, dol, role, isEnabled, create_ts, update_ts, create_by, update_by, " +
            "password, old_password, position, office_location, official_email) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String INSERT_ADMIN_HISTORY_TABLE = "INSERT INTO admins_audit(admin_id, first_name, last_name, email, " +
            "phone, dob, gender, father_name, mother_name, street, city, state, country, profile_photo" +
            "doj, dol, role, isEnabled, create_ts, update_ts, create_by, update_by, " +
            "password, old_password, position, office_location, official_email) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String SELECT_ADMIN_DATA_BY_ID = "SELECT * FROM admins WHERE admin_id = ?";

    private static final String SELECT_ALL_ADMINS_DATA = "SELECT * FROM admins";

    private static final String DELETE_ADMIN_DATA_BY_ID = "DELETE FROM admins WHERE admin_id = ?";

    private static final String UPDATE_ADMIN_DATA_BY_ID = "UPDATE admins SET first_name=?, last_name=?, " +
            "phone=?, dob=?, gender=?, father_name=?, mother_name=?, street=?, city=?, state=?, country=?, " +
            "profile_photo=?, doj=?, dol=?, role=?, isEnabled=?, create_ts, update_ts=?," +
            "create_by, update_by=?, position=?, office_location=?, official_email=? WHERE admin_id = ?";

    private static final String QUERY_TO_CHECK_REGISTERED_ADMIN = "SELECT * FROM admins where first_name=? " +
            "AND phone=? AND father_name=? AND email=?";

    private final PasswordEncoder passwordEncoder;
    private final DataSource dataSource;
    final UserDao userDao;
    final CourseDao courseDao;
    final DepartmentDao departmentDao;

    public AdminDao(PasswordEncoder passwordEncoder, final DataSource dataSource, UserDao userDao, CourseDao courseDao, DepartmentDao departmentDao){
        this.passwordEncoder = passwordEncoder;
        this.dataSource=dataSource;
        this.userDao = userDao;
        this.courseDao = courseDao;
        this.departmentDao = departmentDao;
    }

    public Map<String,String> saveAdminData(Admin admin, String createBy) {
        Map<String,String> adminInfoMap = new HashMap<>();
        String adminId;
        LOG.info("Checking if admin is already registered or not");

        Admin isRegisteredAdmin = checkRegisteredAdmin(admin);
        String password = PasswordGenerator.generateRandomPassword();
        adminInfoMap.put(PASSWORD, password);

        if(Objects.isNull(isRegisteredAdmin) || isEmpty(isRegisteredAdmin.getAdminId())){
            LOG.info("{} is a new admin", admin.getFirstName());

            //This method is to register new admin
            newAdminRegistration(admin, adminInfoMap, password, createBy);

        } else{
            LOG.info("{} is already registered admin", admin.getFirstName());
            adminId = isRegisteredAdmin.getAdminId();
            admin.setPassword(passwordEncoder.encode(password));
            admin.setCreateTs(String.valueOf(LocalDateTime.now()));
            admin.setCreateBy(createBy);
            admin.setUpdateBy(createBy);

            //Populating admin basic info to send in email
            populateAdminInfoMap(adminInfoMap, "Y", adminId, admin);

            Optional<User> user = userDao.getUserDataById(adminId);
            //Saving credentials in users table for login
            user.ifPresent(value -> userDao.resetUserPassword(admin.getAdminId(), admin.getPassword(),
                    value.getPassword(), admin.getUpdateBy()));
        }
        return adminInfoMap;
    }

    private void newAdminRegistration(Admin admin, Map<String, String> adminInfoMap, String password, String createBy) {
        User user = new User();
        String adminId;
        adminId = RandomIdGenerator.generateAdminOrManagerId(ADMIN.name());
        admin.setAdminId(adminId);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setCreateTs(String.valueOf(LocalDateTime.now()));
        admin.setCreateBy(createBy);
        admin.setUpdateBy(createBy);

        //Populating admin basic info to send in email
        populateAdminInfoMap(adminInfoMap, "N", adminId, admin);

        //Saving admin data into DB
        saveAdmin(admin, adminId, false);

        //Populating user object from admin object
        populateUserObject(admin, user);

        //Saving credentials in users table for login
        userDao.saveUserCredentials(user);
    }

    private void populateUserObject(Admin admin, User user) {
        user.setUsername(admin.getAdminId());
        user.setEmail(admin.getEmail());
        user.setPassword(admin.getPassword());
        user.setOldPassword(admin.getOldPassword());
        user.setRole(admin.getRole());
        user.setEnabled(admin.isEnabled());
        user.setCreateTs(admin.getCreateTs());
        user.setUpdateTs(admin.getUpdateTs());
        user.setCreateBy(admin.getCreateBy());
        user.setUpdateBy(admin.getUpdateBy());
    }

    private void saveAdmin(Admin admin, String adminId, boolean isHistory) {
        boolean insertStatus;
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement
                = connection.prepareStatement(isHistory ? INSERT_ADMIN_HISTORY_TABLE : INSERT_ADMIN_TABLE, Statement.RETURN_GENERATED_KEYS)) {
            insertStatement(admin, preparedStatement, adminId);

            insertStatus = preparedStatement.execute();
            if(insertStatus){
                LOG.info("New admin data saved into DB successfully for adminId: {}", adminId);
            }
        } catch (Exception ex) {
            LOG.error("Unable to save new admin data into DB for adminId: {}, Exception: {}", adminId, ExceptionUtils.getStackTrace(ex));
        }
    }

    private void populateAdminInfoMap(Map<String, String> adminInfoMap, String isRegistered, String adminId, Admin admin) {
        adminInfoMap.put(IS_REGISTERED, isRegistered);
        adminInfoMap.put(ADMIN_ID, adminId);
        adminInfoMap.put(FIRST_NAME, admin.getFirstName());
        adminInfoMap.put(EMAIL_ID, admin.getEmail());
        adminInfoMap.put(CREATE_TS, admin.getCreateTs());
        adminInfoMap.put(OFFICIAL_EMAIL, admin.getOfficialEmail());
        adminInfoMap.put(OFFICE_LOCATION, admin.getOfficeLocation());
        adminInfoMap.put(POSITION, admin.getPosition());
    }
    public Admin checkRegisteredAdmin(Admin admin){
        Address address = new Address();

        LOG.info("Checking admin data in DB for FirstName: {}, Phone: {}, " +
                "FatherName: {}, Email: {}", admin.getFirstName(), admin.getPhone(), admin.getFatherName(), admin.getEmail());
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement
                = connection.prepareStatement(QUERY_TO_CHECK_REGISTERED_ADMIN, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1,admin.getFirstName());
            preparedStatement.setString(2,admin.getPhone());
            preparedStatement.setString(3,admin.getFatherName());
            preparedStatement.setString(4,admin.getEmail());
            ResultSet resultSet = preparedStatement.executeQuery();

            if(Objects.nonNull(resultSet)){
                while (resultSet.next()){
                    populateData(admin, address, resultSet);
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to check admin data in DB for for FirstName: {}, Phone: {}, " +
                            "FatherName: {}, Email: {}, Exception: {}", admin.getFirstName(), admin.getPhone(),
                    admin.getFatherName(), admin.getEmail(),ExceptionUtils.getStackTrace(ex));
        }
        return admin;
    }

    private void insertStatement(Admin admin, PreparedStatement preparedStatement, String adminId) throws SQLException {
        preparedStatement.setString(1, adminId);
        preparedStatement.setString(2, admin.getFirstName());
        preparedStatement.setString(3, admin.getLastName());
        preparedStatement.setString(4, admin.getEmail());
        preparedStatement.setString(5, admin.getPhone());
        preparedStatement.setString(6, admin.getDob());
        preparedStatement.setString(7, admin.getGender());
        preparedStatement.setString(8, admin.getFatherName());
        preparedStatement.setString(9, admin.getMotherName());
        preparedStatement.setString(10, admin.getAddress().getStreet());
        preparedStatement.setString(11, admin.getAddress().getCity());
        preparedStatement.setString(12, admin.getAddress().getState());
        preparedStatement.setString(13, admin.getAddress().getCountry());
        preparedStatement.setString(14, admin.getProfilePhoto());
        preparedStatement.setString(15, admin.getDoj());
        preparedStatement.setString(16, admin.getDol());
        preparedStatement.setString(17, String.valueOf(Role.valueOf(String.valueOf(admin.getRole()))));
        preparedStatement.setBoolean(18, admin.isEnabled());
        preparedStatement.setString(19, admin.getCreateTs());
        preparedStatement.setString(20, String.valueOf(LocalDateTime.now()));
        preparedStatement.setString(21, admin.getCreateBy());
        preparedStatement.setString(22, admin.getUpdateBy());
        preparedStatement.setString(23, admin.getPassword());
        preparedStatement.setString(24, admin.getOldPassword());
        preparedStatement.setString(25, admin.getPosition());
        preparedStatement.setString(26, admin.getOfficeLocation());
        preparedStatement.setString(27, admin.getOfficialEmail());
    }

    public Admin getAdminDataById(String adminId){
        Admin admin = new Admin();
        Address address = new Address();

        LOG.info("Fetching admin data from DB for adminId: {}", adminId);
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement
                = connection.prepareStatement(SELECT_ADMIN_DATA_BY_ID, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1,adminId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(Objects.nonNull(resultSet)){
                while (resultSet.next()){
                    populateData(admin, address, resultSet);
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to get admin data from DB for adminId: {}, Exception: {}", adminId, ExceptionUtils.getStackTrace(ex));
        }

        return admin;
    }

    public List<Admin> getAllAdminsData() {
        List<Admin> adminList = new ArrayList<>();

        LOG.info("Fetching all admins data from DB");
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_ADMINS_DATA, Statement.RETURN_GENERATED_KEYS)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            if(Objects.nonNull(resultSet)){
                while (resultSet.next()){
                    Admin admin = new Admin();
                    Address address = new Address();
                    populateData(admin, address, resultSet);
                    adminList.add(admin);
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to fetch all admins data from DB, Exception: {}", ExceptionUtils.getStackTrace(ex));
        }
        return adminList;
    }

    private void populateData(Admin admin, Address address, ResultSet resultSet) throws SQLException {
        admin.setAdminId(resultSet.getString("admin_id"));
        admin.setFirstName(resultSet.getString("first_name"));
        admin.setLastName(resultSet.getString("last_name"));
        admin.setEmail(resultSet.getString("email"));
        admin.setPhone(resultSet.getString("phone"));
        admin.setDob(resultSet.getString("dob"));
        admin.setGender(resultSet.getString("gender"));
        admin.setFatherName(resultSet.getString("father_name"));
        admin.setMotherName(resultSet.getString("mother_name"));
        address.setStreet(resultSet.getString("street"));
        address.setCity(resultSet.getString("city"));
        address.setState(resultSet.getString("state"));
        address.setCountry(resultSet.getString("country"));
        admin.setAddress(address);
        admin.setDoj(resultSet.getString("doj"));
        admin.setRole(Role.valueOf(resultSet.getString("role")));
        admin.setEnabled(resultSet.getBoolean("isEnabled"));
        admin.setCreateTs(resultSet.getString("create_ts"));
        admin.setUpdateTs(resultSet.getString("update_ts"));
        admin.setCreateBy(resultSet.getString("create_by"));
        admin.setUpdateBy(resultSet.getString("update_by"));
        admin.setPosition(resultSet.getString("position"));
        admin.setOfficeLocation(resultSet.getString("office_location"));
        admin.setOfficialEmail(resultSet.getString("official_email"));
    }

    public int deleteAdminDataById(String adminId, String deleteBy) {
        int deleteStatus = 0;
        LOG.info("Deleting admin data from DB for adminId: {}", adminId);
        Admin admin = getAdminDataById(adminId);

        if(Objects.nonNull(admin)){
            //Saving data into history table
            admin.setCreateBy(deleteBy);
            admin.setUpdateBy(deleteBy);
            saveAdmin(admin,adminId,true);

            //Deleting data from user table also
            userDao.deleteUserDataById(adminId);

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement preparedStatement
                         = connection.prepareStatement(DELETE_ADMIN_DATA_BY_ID, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1,adminId);
                deleteStatus = preparedStatement.executeUpdate();
            } catch (Exception ex) {
                LOG.error("Unable to delete admin data from DB for adminId: {}, Exception: {}", adminId, ExceptionUtils.getStackTrace(ex));
            }
        }
        return deleteStatus;
    }

    public Admin updateAdminDataById(Admin admin, String adminId) {
        LOG.info("Updating admin data into DB for adminId: {}", adminId);

        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement
                = connection.prepareStatement(UPDATE_ADMIN_DATA_BY_ID, Statement.RETURN_GENERATED_KEYS)) {
            updateStatement(admin, preparedStatement, adminId);

            int updateStatus = preparedStatement.executeUpdate();
            if(updateStatus > 0){
                LOG.info("Admin data updated into DB for adminId: {}", adminId);
            }
        } catch (Exception ex) {
            LOG.error("Unable to update admin data into DB for adminId: {}, Exception: {}", adminId, ExceptionUtils.getStackTrace(ex));
        }
        return admin;
    }

    private void updateStatement(Admin admin, PreparedStatement preparedStatement, String adminId) throws SQLException {
        preparedStatement.setString(1, admin.getFirstName());
        preparedStatement.setString(2, admin.getLastName());
        preparedStatement.setString(3, admin.getEmail());
        preparedStatement.setString(4, admin.getPhone());
        preparedStatement.setString(5, admin.getDob());
        preparedStatement.setString(6, admin.getGender());
        preparedStatement.setString(7, admin.getFatherName());
        preparedStatement.setString(8, admin.getMotherName());

        preparedStatement.setString(9, admin.getAddress().getStreet());
        preparedStatement.setString(10, admin.getAddress().getCity());
        preparedStatement.setString(11, admin.getAddress().getState());
        preparedStatement.setString(12, admin.getAddress().getCountry());

        preparedStatement.setString(13, admin.getProfilePhoto());
        preparedStatement.setString(14, admin.getDoj());
        preparedStatement.setString(15, admin.getDol());
        preparedStatement.setString(16, String.valueOf(String.valueOf(admin.getRole())));
        preparedStatement.setBoolean(17, admin.isEnabled());
        preparedStatement.setString(18, admin.getCreateTs());
        preparedStatement.setString(19, String.valueOf(LocalDateTime.now()));
        preparedStatement.setString(20,  admin.getCreateBy());
        preparedStatement.setString(21, admin.getUpdateBy());
        preparedStatement.setString(22,  admin.getPosition());
        preparedStatement.setString(23,  admin.getReportingTo());
        preparedStatement.setString(24,  admin.getOfficeLocation());
        preparedStatement.setString(25,  admin.getOfficialEmail());
        preparedStatement.setString(26, adminId);
    }
}
