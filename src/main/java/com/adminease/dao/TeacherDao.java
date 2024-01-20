package com.adminease.dao;

import com.adminease.enums.Role;
import com.adminease.model.Address;
import com.adminease.model.management.Department;
import com.adminease.model.teacher.Teacher;
import com.adminease.model.user.User;
import com.adminease.util.PasswordGenerator;
import com.adminease.util.RandomIdGenerator;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import static com.adminease.enums.Role.TEACHER;
import static com.adminease.model.CommonConstants.*;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Repository
public class TeacherDao {

    private static final Logger LOG = LoggerFactory.getLogger(TeacherDao.class);

    private static final String INSERT_TEACHER_TABLE = "INSERT INTO teachers(teacher_id, first_name, last_name, email, " +
            "phone, dob, gender, father_name, mother_name, street, city, state, country, expertise, dept_id, profile_photo" +
            "doj, dol, role, isEnabled, create_ts, update_ts, create_by, update_by, " +
            "password, old_password, position, reporting_to, office_location, official_email) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String INSERT_TEACHER_HISTORY_TABLE = "INSERT INTO teachers_audit(teacher_id, first_name, last_name, email, " +
            "phone, dob, gender, father_name, mother_name, street, city, state, country, expertise, dept_id, " +
            "create_ts, update_ts, profile_photo, create_by, update_by, " +
            "doj, dol, role, password, old_password, isEnabled, position, reporting_to, office_location, official_email) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String SELECT_TEACHER_DATA_BY_ID = "SELECT * FROM teachers WHERE teacher_id = ?";

    private static final String SELECT_ALL_TEACHERS_DATA = "SELECT * FROM teachers";

    private static final String DELETE_TEACHER_DATA_BY_ID = "DELETE FROM teachers WHERE teacher_id = ?";

    private static final String UPDATE_TEACHER_DATA_BY_ID = "UPDATE teachers SET first_name=?, last_name=?, " +
            "phone=?, dob=?, gender=?, father_name=?, mother_name=?, street=?, city=?, state=?, country=?, expertise=?, dept_id=?, " +
            "profile_photo=?, doj=?, dol=?, role=?, isEnabled=?, create_ts, update_ts=?," +
            "create_by, update_by=?, position=?, reporting_to=?, office_location=?, official_email=? WHERE teacher_id = ?";

    private static final String QUERY_TO_CHECK_REGISTERED_TEACHER = "SELECT * FROM teachers where first_name=? " +
            "AND phone=? AND father_name=? AND email=?";

    private final PasswordEncoder passwordEncoder;
    private final DataSource dataSource;
    final UserDao userDao;
    final CourseDao courseDao;
    final DepartmentDao departmentDao;

    public TeacherDao(PasswordEncoder passwordEncoder, final DataSource dataSource, UserDao userDao, CourseDao courseDao, DepartmentDao departmentDao){
        this.passwordEncoder = passwordEncoder;
        this.dataSource=dataSource;
        this.userDao = userDao;
        this.courseDao = courseDao;
        this.departmentDao = departmentDao;
    }

    public Map<String,String> saveTeacherData(Teacher teacher, String createBy) {
        Map<String,String> teacherInfoMap = new HashMap<>();
        String teacherId;
        LOG.info("Checking if teacher is already registered or not");

        Teacher isRegisteredTeacher = checkRegisteredTeacher(teacher);
        String password = PasswordGenerator.generateRandomPassword();
        teacherInfoMap.put(PASSWORD, password);

        if(Objects.isNull(isRegisteredTeacher) || isEmpty(isRegisteredTeacher.getTeacherId())){
            LOG.info("{} is a new teacher", teacher.getFirstName());

            //This method is to register new teacher
            newTeacherRegistration(teacher, teacherInfoMap, password, createBy);

        } else{
            LOG.info("{} is already registered teacher", teacher.getFirstName());
            teacherId = isRegisteredTeacher.getTeacherId();
            teacher.setPassword(passwordEncoder.encode(password));
            teacher.setCreateTs(String.valueOf(LocalDateTime.now()));
            teacher.setCreateBy(createBy);
            teacher.setUpdateBy(createBy);

            //Populating teacher basic info to send in email
            populateTeacherInfoMap(teacherInfoMap, "Y", teacherId, teacher);

            Optional<User> user = userDao.getUserDataById(teacherId);
            //Saving credentials in users table for login
            user.ifPresent(value -> userDao.resetUserPassword(teacher.getTeacherId(), teacher.getPassword(),
                    value.getPassword(), teacher.getUpdateBy()));
        }
        return teacherInfoMap;
    }

    private void newTeacherRegistration(Teacher teacher, Map<String, String> teacherInfoMap, String password, String createBy) {
        User user = new User();
        String teacherId;
        teacherId = RandomIdGenerator.generateStudentOrTeacherId(teacher.getDepartmentId(), TEACHER.name());
        teacher.setTeacherId(teacherId);
        teacher.setPassword(passwordEncoder.encode(password));
        teacher.setCreateTs(String.valueOf(LocalDateTime.now()));
        teacher.setCreateBy(createBy);
        teacher.setUpdateBy(createBy);

        //Populating teacher basic info to send in email
        populateTeacherInfoMap(teacherInfoMap, "N", teacherId, teacher);

        //Saving teacher data into DB
        saveTeacher(teacher, teacherId, false);

        //Populating user object from teacher object
        populateUserObject(teacher, user);

        //Saving credentials in users table for login
        userDao.saveUserCredentials(user);
    }

    private void populateUserObject(Teacher teacher, User user) {
        user.setUsername(teacher.getTeacherId());
        user.setEmail(teacher.getEmail());
        user.setPassword(teacher.getPassword());
        user.setOldPassword(teacher.getOldPassword());
        user.setRole(teacher.getRole());
        user.setEnabled(teacher.isEnabled());
        user.setCreateTs(teacher.getCreateTs());
        user.setUpdateTs(teacher.getUpdateTs());
        user.setCreateBy(teacher.getCreateBy());
        user.setUpdateBy(teacher.getUpdateBy());
    }

    private void saveTeacher(Teacher teacher, String teacherId, boolean isHistory) {
        boolean insertStatus;
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement
                = connection.prepareStatement(isHistory ? INSERT_TEACHER_HISTORY_TABLE : INSERT_TEACHER_TABLE, Statement.RETURN_GENERATED_KEYS)) {
            insertStatement(teacher, preparedStatement, teacherId);

            insertStatus = preparedStatement.execute();
            if(insertStatus){
                LOG.info("New teacher data saved into DB successfully for teacherId: {}", teacherId);
            }
        } catch (Exception ex) {
            LOG.error("Unable to save new teacher data into DB for teacherId: {}, Exception: {}", teacherId, ExceptionUtils.getStackTrace(ex));
        }
    }

    private void populateTeacherInfoMap(Map<String, String> teacherInfoMap, String isRegistered, String teacherId, Teacher teacher) {
        Department department =  departmentDao.getDepartmentDataById(teacher.getDepartmentId());

        teacherInfoMap.put(IS_REGISTERED, isRegistered);
        teacherInfoMap.put(TEACHER_ID, teacherId);
        teacherInfoMap.put(FIRST_NAME, teacher.getFirstName());
        teacherInfoMap.put(EMAIL_ID, teacher.getEmail());
        teacherInfoMap.put(DEPT_ID, department.getDeptName());
        teacherInfoMap.put(CREATE_TS, teacher.getCreateTs());
        teacherInfoMap.put(OFFICIAL_EMAIL, teacher.getOfficialEmail());
        teacherInfoMap.put(OFFICE_LOCATION, teacher.getOfficeLocation());
        teacherInfoMap.put(POSITION, teacher.getPosition());
        teacherInfoMap.put(REPORTING_TO, teacher.getReportingTo());
    }
    public Teacher checkRegisteredTeacher(Teacher teacher){
        Address address = new Address();

        LOG.info("Checking teacher data in DB for FirstName: {}, Phone: {}, " +
                "FatherName: {}, Email: {}", teacher.getFirstName(), teacher.getPhone(), teacher.getFatherName(), teacher.getEmail());
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement
                = connection.prepareStatement(QUERY_TO_CHECK_REGISTERED_TEACHER, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1,teacher.getFirstName());
            preparedStatement.setString(2,teacher.getPhone());
            preparedStatement.setString(3,teacher.getFatherName());
            preparedStatement.setString(4,teacher.getEmail());
            ResultSet resultSet = preparedStatement.executeQuery();

            if(Objects.nonNull(resultSet)){
                while (resultSet.next()){
                    populateData(teacher, address, resultSet);
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to check teacher data in DB for for FirstName: {}, Phone: {}, " +
                            "FatherName: {}, Email: {}, Exception: {}", teacher.getFirstName(), teacher.getPhone(),
                    teacher.getFatherName(), teacher.getEmail(),ExceptionUtils.getStackTrace(ex));
        }
        return teacher;
    }

    private void insertStatement(Teacher teacher, PreparedStatement preparedStatement, String teacherId) throws SQLException {
        preparedStatement.setString(1, teacherId);
        preparedStatement.setString(2, teacher.getFirstName());
        preparedStatement.setString(3, teacher.getLastName());
        preparedStatement.setString(4, teacher.getEmail());
        preparedStatement.setString(5, teacher.getPhone());
        preparedStatement.setString(6, teacher.getDob());
        preparedStatement.setString(7, teacher.getGender());
        preparedStatement.setString(8, teacher.getFatherName());
        preparedStatement.setString(9, teacher.getMotherName());
        preparedStatement.setString(10, teacher.getAddress().getStreet());
        preparedStatement.setString(11, teacher.getAddress().getCity());
        preparedStatement.setString(12, teacher.getAddress().getState());
        preparedStatement.setString(13, teacher.getAddress().getCountry());
        preparedStatement.setString(14, teacher.getExpertise());
        preparedStatement.setString(15, teacher.getDepartmentId());
        preparedStatement.setString(16, teacher.getProfilePhoto());
        preparedStatement.setString(17, teacher.getDoj());
        preparedStatement.setString(18, teacher.getDol());
        preparedStatement.setString(19, String.valueOf(Role.valueOf(String.valueOf(teacher.getRole()))));
        preparedStatement.setBoolean(20, teacher.isEnabled());
        preparedStatement.setString(21, teacher.getCreateTs());
        preparedStatement.setString(22, String.valueOf(LocalDateTime.now()));
        preparedStatement.setString(23, teacher.getCreateBy());
        preparedStatement.setString(24, teacher.getUpdateBy());
        preparedStatement.setString(25, teacher.getPassword());
        preparedStatement.setString(26, teacher.getOldPassword());
        preparedStatement.setString(27, teacher.getPosition());
        preparedStatement.setString(28, teacher.getReportingTo());
        preparedStatement.setString(29, teacher.getOfficeLocation());
        preparedStatement.setString(30, teacher.getOfficialEmail());
    }

    public Teacher getTeacherDataById(String teacherId){
        Teacher teacher = new Teacher();
        Address address = new Address();

        LOG.info("Fetching student data from DB for teacherId: {}", teacherId);
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement
                = connection.prepareStatement(SELECT_TEACHER_DATA_BY_ID, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1,teacherId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(Objects.nonNull(resultSet)){
                while (resultSet.next()){
                    populateData(teacher, address, resultSet);
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to get teacher data from DB for teacherId: {}, Exception: {}", teacherId, ExceptionUtils.getStackTrace(ex));
        }

        return teacher;
    }

    public List<Teacher> getAllTeachersData() {
        List<Teacher> teacherList = new ArrayList<>();

        LOG.info("Fetching all teachers data from DB");
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_TEACHERS_DATA, Statement.RETURN_GENERATED_KEYS)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            if(Objects.nonNull(resultSet)){
                while (resultSet.next()){
                    Teacher teacher = new Teacher();
                    Address address = new Address();
                    populateData(teacher, address, resultSet);
                    teacherList.add(teacher);
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to fetch all teachers data from DB, Exception: {}", ExceptionUtils.getStackTrace(ex));
        }
        return teacherList;
    }

    private void populateData(Teacher teacher,
                              Address address, ResultSet resultSet) throws SQLException {
        teacher.setTeacherId(resultSet.getString("teacher_id"));
        teacher.setFirstName(resultSet.getString("first_name"));
        teacher.setLastName(resultSet.getString("last_name"));
        teacher.setEmail(resultSet.getString("email"));
        teacher.setPhone(resultSet.getString("phone"));
        teacher.setDob(resultSet.getString("dob"));
        teacher.setGender(resultSet.getString("gender"));
        teacher.setFatherName(resultSet.getString("father_name"));
        teacher.setMotherName(resultSet.getString("mother_name"));
        address.setStreet(resultSet.getString("street"));
        address.setCity(resultSet.getString("city"));
        address.setState(resultSet.getString("state"));
        address.setCountry(resultSet.getString("country"));
        teacher.setAddress(address);
        teacher.setExpertise(resultSet.getString("expertise"));
        teacher.setDepartmentId(resultSet.getString("dept_id"));
        teacher.setDoj(resultSet.getString("doj"));
        teacher.setRole(Role.valueOf(resultSet.getString("role")));
        teacher.setCreateTs(resultSet.getString("create_ts"));
        teacher.setUpdateTs(resultSet.getString("update_ts"));
        teacher.setCreateBy(resultSet.getString("create_by"));
        teacher.setUpdateBy(resultSet.getString("update_by"));
        teacher.setPosition(resultSet.getString("position"));
        teacher.setReportingTo(resultSet.getString("reporting_to"));
        teacher.setOfficeLocation(resultSet.getString("office_location"));
        teacher.setOfficialEmail(resultSet.getString("official_email"));
    }

    public int deleteTeacherDataById(String teacherId, String deleteBy) {
        int deleteStatus = 0;
        LOG.info("Deleting teacher data from DB for teacherId: {}", teacherId);
        Teacher teacher = getTeacherDataById(teacherId);

        if(Objects.nonNull(teacher)){
            //Saving data into history table
            teacher.setCreateBy(deleteBy);
            teacher.setUpdateBy(deleteBy);
            saveTeacher(teacher,teacherId,true);

            //Deleting data from user table also
            userDao.deleteUserDataById(teacherId);

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement preparedStatement
                         = connection.prepareStatement(DELETE_TEACHER_DATA_BY_ID, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1,teacherId);
                deleteStatus = preparedStatement.executeUpdate();
            } catch (Exception ex) {
                LOG.error("Unable to delete teacher data from DB for teacherId: {}, Exception: {}", teacherId, ExceptionUtils.getStackTrace(ex));
            }
        }
        return deleteStatus;
    }

    public Teacher updateTeacherDataById(Teacher teacher, String teacherId) {
        LOG.info("Updating teacher data into DB for StudentId: {}", teacherId);

            try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement
                    = connection.prepareStatement(UPDATE_TEACHER_DATA_BY_ID, Statement.RETURN_GENERATED_KEYS)) {
                updateStatement(teacher, preparedStatement, teacherId);

                int updateStatus = preparedStatement.executeUpdate();
                if(updateStatus > 0){
                    LOG.info("Teacher data updated into DB for teacherId: {}", teacherId);
                }
            } catch (Exception ex) {
                LOG.error("Unable to update teacher data into DB for teacherId: {}, Exception: {}", teacherId, ExceptionUtils.getStackTrace(ex));
            }
        return teacher;
    }

    private void updateStatement(Teacher teacher, PreparedStatement preparedStatement, String teacherId) throws SQLException {
        preparedStatement.setString(1, teacher.getFirstName());
        preparedStatement.setString(2, teacher.getLastName());
        preparedStatement.setString(3, teacher.getEmail());
        preparedStatement.setString(4, teacher.getPhone());
        preparedStatement.setString(5, teacher.getDob());
        preparedStatement.setString(6, teacher.getGender());
        preparedStatement.setString(7, teacher.getFatherName());
        preparedStatement.setString(8, teacher.getMotherName());

        preparedStatement.setString(9, teacher.getAddress().getStreet());
        preparedStatement.setString(10, teacher.getAddress().getCity());
        preparedStatement.setString(11, teacher.getAddress().getState());
        preparedStatement.setString(12, teacher.getAddress().getCountry());

        preparedStatement.setString(13, teacher.getExpertise());
        preparedStatement.setString(14, teacher.getDepartmentId());
        preparedStatement.setString(15, teacher.getProfilePhoto());
        preparedStatement.setString(16, teacher.getDoj());
        preparedStatement.setString(17, teacher.getDol());
        preparedStatement.setString(18, String.valueOf(String.valueOf(teacher.getRole())));
        preparedStatement.setBoolean(19, teacher.isEnabled());
        preparedStatement.setString(20, teacher.getCreateTs());
        preparedStatement.setString(21, String.valueOf(LocalDateTime.now()));
        preparedStatement.setString(22,  teacher.getCreateBy());
        preparedStatement.setString(23, teacher.getUpdateBy());
        preparedStatement.setString(24,  teacher.getPosition());
        preparedStatement.setString(25,  teacher.getReportingTo());
        preparedStatement.setString(26,  teacher.getOfficeLocation());
        preparedStatement.setString(27,  teacher.getOfficialEmail());
        preparedStatement.setString(28, teacherId);
    }
}
