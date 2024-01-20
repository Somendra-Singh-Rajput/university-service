package com.adminease.dao;

import com.adminease.model.management.Course;
import com.adminease.model.Address;
import com.adminease.model.student.Student;
import com.adminease.model.user.User;
import com.adminease.util.PasswordGenerator;
import com.adminease.util.RandomIdGenerator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import static com.adminease.enums.Role.USER;
import static com.adminease.model.CommonConstants.*;

@Repository
public class StudentDao {

    private static final Logger LOG = LoggerFactory.getLogger(StudentDao.class);

    private static final String INSERT_STUDENT_TABLE = "INSERT INTO students(student_id, first_name, last_name, email, " +
            "phone, dob, gender, father_name, mother_name, " +
            "street, city, state, country, course_id, dept_id, create_ts, update_ts, profile_photo, create_by, update_by) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String INSERT_STUDENT_HISTORY_TABLE = "INSERT INTO students_audit(student_id, first_name, last_name, email, " +
            "phone, dob, gender, father_name, mother_name, " +
            "street, city, state, country, course_id, dept_id, create_ts, update_ts, profile_photo, create_by, update_by) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String SELECT_STUDENT_DATA_BY_ID = "SELECT * FROM students WHERE student_id = ?";

    private static final String SELECT_ALL_STUDENTS_DATA = "SELECT * FROM students";

    private static final String DELETE_STUDENT_DATA_BY_ID = "DELETE FROM students WHERE student_id = ?";
    private static final String UPDATE_STUDENT_DATA_BY_ID = "UPDATE students SET first_name=?, last_name=?, " +
            "phone=?, dob=?, gender=?, father_name=?, mother_name=?, street=?, city=?, state=?, country=?," +
            "course_id=?, dept_id=?, create_ts, update_ts, profile_photo=?, create_by=?, update_by=? WHERE student_id = ?";

    private static final String QUERY_TO_CHECK_REGISTERED_STUDENT = "SELECT * FROM students where first_name=? " +
            "AND phone=? AND father_name=? AND email=?";

    private final DataSource dataSource;
    private final PasswordEncoder passwordEncoder;
    final UserDao userDao;
    final CourseDao courseDao;

    public StudentDao(PasswordEncoder passwordEncoder, final DataSource dataSource, UserDao userDao, CourseDao courseDao){
        this.passwordEncoder = passwordEncoder;
        this.dataSource=dataSource;
        this.userDao = userDao;
        this.courseDao = courseDao;
    }

    public Map<String,String> saveStudentData(Student student, String createBy) {
        Map<String,String> studentInfoMap = new HashMap<>();
        String studentId;
        LOG.info("Checking if student is already registered or not");

        Student isRegisteredStudent = checkRegisteredStudent(student);
        String password = PasswordGenerator.generateRandomPassword();
        studentInfoMap.put(PASSWORD, password);

        if(Objects.isNull(isRegisteredStudent) || StringUtils.isEmpty(isRegisteredStudent.getStudentId())){
            LOG.info("{} is a new student", student.getFirstName());

            //This method is to register new student
            newStudentRegistration(student, studentInfoMap, password, createBy);

        } else{
            LOG.info("{} is already registered student", student.getFirstName());
            studentId = isRegisteredStudent.getStudentId();
            student.setPassword(passwordEncoder.encode(password));
            student.setCreateTs(String.valueOf(LocalDateTime.now()));
            student.setCreateBy(createBy);
            student.setUpdateBy(createBy);

            //Populating student basic info to send in email
            populateStudentInfoMap(studentInfoMap, "Y", studentId, student);

            Optional<User> user = userDao.getUserDataById(studentId);
            //Saving credentials in users table for login
            user.ifPresent(value -> userDao.resetUserPassword(student.getStudentId(), student.getPassword(),
                    value.getPassword(), student.getUpdateBy()));
        }
        return studentInfoMap;
    }

    private void newStudentRegistration(Student student, Map<String, String> studentInfoMap, String password, String createBy) {
        String studentId;
        User user = new User();
        studentId = RandomIdGenerator.generateStudentOrTeacherId(student.getCourseId(), USER.name());
        student.setStudentId(studentId);
        student.setPassword(passwordEncoder.encode(password));
        student.setCreateTs(String.valueOf(LocalDateTime.now()));
        student.setCreateBy(createBy);
        student.setUpdateBy(createBy);

        //Populating student basic info to send in email
        populateStudentInfoMap(studentInfoMap, "N", studentId, student);

        //Saving student data into DB
        saveStudent(student, studentId, false);

        //Populating user object from student object
        populateUserObject(student, user);

        //Saving credentials in users table for login
        userDao.saveUserCredentials(user);
    }

    private void populateUserObject(Student student, User user) {
        user.setUsername(student.getStudentId());
        user.setEmail(student.getEmail());
        user.setPassword(student.getPassword());
        user.setOldPassword(student.getOldPassword());
        user.setRole(student.getRole());
        user.setEnabled(student.isEnabled());
        user.setCreateTs(student.getCreateTs());
        user.setUpdateTs(student.getUpdateTs());
        user.setCreateBy(student.getCreateBy());
        user.setUpdateBy(student.getUpdateBy());
    }

    private void saveStudent(Student student, String studentId, boolean isHistory) {
        boolean insertStatus;
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement
                = connection.prepareStatement(isHistory ? INSERT_STUDENT_HISTORY_TABLE : INSERT_STUDENT_TABLE, Statement.RETURN_GENERATED_KEYS)) {
            insertStatement(student, preparedStatement, studentId);

            insertStatus = preparedStatement.execute();
            if(insertStatus){
                LOG.info("New student data saved into DB successfully for studentId: {}", studentId);
            }
        } catch (Exception ex) {
            LOG.error("Unable to save new student data into DB for studentId: {}, Exception: {}", studentId, ExceptionUtils.getStackTrace(ex));
        }
    }

    private void populateStudentInfoMap(Map<String, String> studentInfoMap, String isRegistered, String studentId, Student student) {
        Course course =  courseDao.getCourseDataById(student.getCourseId());

        studentInfoMap.put(IS_REGISTERED, isRegistered);
        studentInfoMap.put(STUDENT_ID, studentId);
        studentInfoMap.put(FIRST_NAME, student.getFirstName());
        studentInfoMap.put(EMAIL_ID, student.getEmail());
        studentInfoMap.put(COURSE_ID, course.getCourseName());
        studentInfoMap.put(COURSE_DURATION, course.getCourseDuration());
        studentInfoMap.put(CREATE_TS, student.getCreateTs());
    }
    public Student checkRegisteredStudent(Student student){
        Address address = new Address();

        LOG.info("Checking student data in DB for FirstName: {}, Phone: {}, " +
                "FatherName: {}, Email: {}", student.getFirstName(), student.getPhone(), student.getFatherName(), student.getEmail());
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(QUERY_TO_CHECK_REGISTERED_STUDENT, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1,student.getFirstName());
            preparedStatement.setString(2,student.getPhone());
            preparedStatement.setString(3,student.getFatherName());
            preparedStatement.setString(4,student.getEmail());
            ResultSet resultSet = preparedStatement.executeQuery();

            if(Objects.nonNull(resultSet)){
                while (resultSet.next()){
                    populateStudentData(student, address, resultSet);
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to check student data in DB for for FirstName: {}, Phone: {}, " +
                    "FatherName: {}, Email: {}, Exception: {}", student.getFirstName(), student.getPhone(),
                    student.getFatherName(), student.getEmail(),ExceptionUtils.getStackTrace(ex));
        }

        return student;
    }

    private void insertStatement(Student student, PreparedStatement preparedStatement, String studentId) throws SQLException {
        preparedStatement.setString(1, studentId);
        preparedStatement.setString(2, student.getFirstName());
        preparedStatement.setString(3, student.getLastName());
        preparedStatement.setString(4, student.getEmail());
        preparedStatement.setString(5, student.getPhone());
        preparedStatement.setString(6, student.getDob());
        preparedStatement.setString(7, student.getGender());
        preparedStatement.setString(8, student.getFatherName());
        preparedStatement.setString(9, student.getMotherName());
        preparedStatement.setString(10, student.getAddress().getStreet());
        preparedStatement.setString(11, student.getAddress().getCity());
        preparedStatement.setString(12, student.getAddress().getState());
        preparedStatement.setString(13, student.getAddress().getCountry());
        preparedStatement.setString(14, student.getCourseId());
        preparedStatement.setString(15, student.getDepartmentId());
        preparedStatement.setString(16, student.getCreateTs());
        preparedStatement.setString(17, String.valueOf(LocalDateTime.now()));
        preparedStatement.setString(18, student.getProfilePhoto());
        preparedStatement.setString(19, student.getCreateBy());
        preparedStatement.setString(20, student.getUpdateBy());
    }

    public Student getStudentDataById(String studentId){
        Student student = new Student();
        Address address = new Address();

        LOG.info("Fetching student data from DB for studentId: {}", studentId);
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SELECT_STUDENT_DATA_BY_ID, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1,studentId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(Objects.nonNull(resultSet)){
                while (resultSet.next()){
                    populateStudentData(student, address, resultSet);
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to get student data from DB for studentId: {}, Exception: {}", studentId, ExceptionUtils.getStackTrace(ex));
        }

        return student;
    }

    public List<Student> getAllStudentsData() {
        List<Student> studentList = new ArrayList<>();

        LOG.info("Fetching all students data from DB");
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_STUDENTS_DATA, Statement.RETURN_GENERATED_KEYS)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            if(Objects.nonNull(resultSet)){
                while (resultSet.next()){
                    Student student = new Student();
                    Address address = new Address();
                    populateStudentData(student, address, resultSet);
                    studentList.add(student);
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to fetch all students data from DB, Exception: {}", ExceptionUtils.getStackTrace(ex));
        }
        return studentList;
    }

    private void populateStudentData(Student student,
                                     Address address, ResultSet resultSet) throws SQLException {
        student.setStudentId(resultSet.getString("student_id"));
        student.setFirstName(resultSet.getString("first_name"));
        student.setLastName(resultSet.getString("last_name"));
        student.setEmail(resultSet.getString("email"));
        student.setPhone(resultSet.getString("phone"));
        student.setDob(resultSet.getString("dob"));
        student.setGender(resultSet.getString("gender"));
        student.setFatherName(resultSet.getString("father_name"));
        student.setMotherName(resultSet.getString("mother_name"));
        address.setStreet(resultSet.getString("street"));
        address.setCity(resultSet.getString("city"));
        address.setState(resultSet.getString("state"));
        address.setCountry(resultSet.getString("country"));
        student.setAddress(address);
        student.setCourseId(resultSet.getString("course_id"));
        student.setDepartmentId(resultSet.getString("dept_id"));
        student.setCreateTs(resultSet.getString("create_ts"));
        student.setUpdateTs(resultSet.getString("update_ts"));
    }

    public int deleteStudentDataById(String studentId, String deleteBy) {
        int deleteStatus = 0;
        LOG.info("Deleting student data from DB for studentId: {}", studentId);
        Student student = getStudentDataById(studentId);

        if(Objects.nonNull(student)){
            //Saving data into history table
            student.setCreateBy(deleteBy);
            student.setUpdateBy(deleteBy);
            saveStudent(student,studentId,true);

            //Deleting data from user table also
            userDao.deleteUserDataById(studentId);

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement preparedStatement
                         = connection.prepareStatement(DELETE_STUDENT_DATA_BY_ID, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1,studentId);
                deleteStatus = preparedStatement.executeUpdate();
            } catch (Exception ex) {
                LOG.error("Unable to delete student data from DB for studentId: {}, Exception: {}", studentId, ExceptionUtils.getStackTrace(ex));
            }
        }

        return deleteStatus;
    }

    public Student updateStudentDataById(Student student, String studentId) {
        LOG.info("Updating student data into DB for StudentId: {}", studentId);

            try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement
                    = connection.prepareStatement(UPDATE_STUDENT_DATA_BY_ID, Statement.RETURN_GENERATED_KEYS)) {
                updateStatement(student, preparedStatement, studentId);

                int updateStatus = preparedStatement.executeUpdate();
                if(updateStatus > 0){
                    LOG.info("Student data updated into DB for studentId: {}", studentId);
                }
            } catch (Exception ex) {
                LOG.error("Unable to update student data into DB for studentId: {}, Exception: {}", studentId, ExceptionUtils.getStackTrace(ex));
            }
        return student;
    }

    private void updateStatement(Student student, PreparedStatement preparedStatement, String studentId) throws SQLException {
        preparedStatement.setString(1, student.getFirstName());
        preparedStatement.setString(2, student.getLastName());
        preparedStatement.setString(3, student.getPhone());
        preparedStatement.setString(4, student.getDob());
        preparedStatement.setString(5, student.getGender());
        preparedStatement.setString(6, student.getFatherName());
        preparedStatement.setString(7, student.getMotherName());

        preparedStatement.setString(8, student.getAddress().getStreet());
        preparedStatement.setString(9, student.getAddress().getCity());
        preparedStatement.setString(10, student.getAddress().getState());
        preparedStatement.setString(11, student.getAddress().getCountry());

        preparedStatement.setString(12,student.getCourseId());
        preparedStatement.setString(13, student.getDepartmentId());
        preparedStatement.setString(14, student.getCreateTs());
        preparedStatement.setString(15, String.valueOf(LocalDateTime.now()));

        preparedStatement.setString(16, student.getProfilePhoto());
        preparedStatement.setString(17, student.getCreateBy());
        preparedStatement.setString(18, student.getUpdateBy());
        preparedStatement.setString(19, studentId);
    }
}
