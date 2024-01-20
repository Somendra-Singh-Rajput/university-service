package com.adminease.model;

public class CommonConstants {

    private CommonConstants() {
        throw new IllegalStateException("CommonConstants class");
    }

    public static final String STUDENT_ID="student_id";
    public static final String PASSWORD="password";
    public static final String EMAIL_ID="email_id";
    public static final String FIRST_NAME="first_name";
    public static final String COURSE_ID="course_id";
    public static final String COURSE_DURATION="course_duration";
    public static final String CREATE_TS="create_ts";
    public static final String UPDATE_TS="update_ts";
    public static final String SEMESTER="semester";
    public static final String DEPARTMENT="department";
    public static final String COURSE="course";
    public static final String SUBJECT="subject";
    public static final String IS_REGISTERED="is_registered";
    public static final String TO_EMAIL="to_email";
    public static final String BODY="body";

    //Accessible paths after authentication
    public static final String ADMIN_ACCESS_PATH="/api/v1/admin/**";
    public static final String MANAGER_ACCESS_PATH="/api/v1/manager/**";
    public static final String TEACHER_ACCESS_PATH="/api/v1/teacher/**";
    public static final String STUDENT_ACCESS_PATH="/api/v1/student/**";
    public static final String COURSE_ACCESS_PATH="/api/v1/course/**";
    public static final String DEPARTMENT_ACCESS_PATH="/api/v1/department/**";
    public static final String SEMESTER_ACCESS_PATH="/api/v1/semester/**";
    public static final String SUBJECT_ACCESS_PATH="/api/v1/subject/**";

    public static final String STUDENTID="StudentId";
    public static final String DEPARTMENTID="DepartmentId";
    public static final String SEMESTERID="SemesterId";
    public static final String COURSEID="CourseId";
    public static final String SUBJECTID="SubjectId";
    public static final String TEACHERID="TeacherId";
    public static final String TEACHER_ID="teacher_id";
    public static final String DEPT_ID="dept_id";
    public static final String OFFICIAL_EMAIL="official_email";
    public static final String POSITION="position";
    public static final String REPORTING_TO="reporting_to";
    public static final String OFFICE_LOCATION="office_location";

    public static final String ADMINID="AdminId";
    public static final String ADMIN_ID="admin_id";
    public static final String MANAGERID="ManagerId";
    public static final String MANAGER_ID="manager_id";
}
