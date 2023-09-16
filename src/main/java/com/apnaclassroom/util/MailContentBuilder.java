package com.apnaclassroom.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.apnaclassroom.model.CommonConstants.*;

@Component
public class MailContentBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(MailContentBuilder.class);

    private static final String WELCOME_EMAIL_CNTNT_FOR_USER_ID = "Generating welcome mail content for UserId: {}";
    private static final String WELCOME_BACK_TO = "Welcome back to ";
    private static final String WELCOME_TO = "Welcome to ";
    private static final String DEAR = "Dear ";
    private static final String TEMP_PWD = "Temporary Password";
    private static final String BEST_REGARDS = "Best Regards,\n";

    @Value("${registered.university.name}")
    private String universityName;

    @Value("${registered.university.contacts}")
    private String universityContacts;

    @Value("${registered.university.email}")
    private String universityEmail;

    @Value("${registered.university.website}")
    private String universityWebsite;

    public Map<String, String> generateUserWelcomeEmail(Map<String, String> studentIdByPwd) {
        Map<String, String> mailContentMap = new HashMap<>();
        String studentId = studentIdByPwd.get(STUDENT_ID);
        String toEmail = studentIdByPwd.get(EMAIL_ID);
        String firstName = studentIdByPwd.get(FIRST_NAME);
        String password = studentIdByPwd.get(PASSWORD);
        String courseId = studentIdByPwd.get(COURSE_ID);
        String courseDuration = studentIdByPwd.get(COURSE_DURATION);
        String createTs = studentIdByPwd.get(CREATE_TS);
        String isRegistered = studentIdByPwd.get(IS_REGISTERED);
        String subject;
        LOG.info(WELCOME_EMAIL_CNTNT_FOR_USER_ID, studentId);

        if("Y".equalsIgnoreCase(isRegistered)){
            subject = WELCOME_BACK_TO+universityName+", "+firstName+"!";
        } else {
            subject = WELCOME_TO+universityName+", "+firstName+"!";
        }

        String body = prepareUserMailBody(studentId,firstName, password, courseId, courseDuration, createTs);

        mailContentMap.put(TO_EMAIL, toEmail);
        mailContentMap.put(SUBJECT, subject);
        mailContentMap.put(BODY, body);

        return mailContentMap;
    }


    public Map<String, String> generateTeacherWelcomeEmail(Map<String, String> teacherIdByPwd) {
        Map<String, String> mailContentMap = new HashMap<>();
        String teacherId = teacherIdByPwd.get(TEACHER_ID);
        String toEmail = teacherIdByPwd.get(EMAIL_ID);
        String firstName = teacherIdByPwd.get(FIRST_NAME);
        String password = teacherIdByPwd.get(PASSWORD);
        String deptId = teacherIdByPwd.get(DEPT_ID);
        String createTs = teacherIdByPwd.get(CREATE_TS);
        String isRegistered = teacherIdByPwd.get(IS_REGISTERED);
        String position = teacherIdByPwd.get(POSITION);
        String reportingTo = teacherIdByPwd.get(REPORTING_TO);
        String officialEmail = teacherIdByPwd.get(OFFICIAL_EMAIL);
        String officeLocation = teacherIdByPwd.get(OFFICE_LOCATION);

        String subject;
        LOG.info(WELCOME_EMAIL_CNTNT_FOR_USER_ID, teacherId);

        if("Y".equalsIgnoreCase(isRegistered)){
            subject = WELCOME_BACK_TO+universityName+", "+firstName+"!";
        } else {
            subject = WELCOME_TO+universityName+", "+firstName+"!";
        }

        String body = prepareTeacherMailBody(teacherId, firstName, password, deptId, createTs, position, reportingTo, officialEmail, officeLocation);

        mailContentMap.put(TO_EMAIL, toEmail);
        mailContentMap.put(SUBJECT, subject);
        mailContentMap.put(BODY, body);

        return mailContentMap;
    }

    public Map<String, String> generateAdminWelcomeEmail(Map<String, String> adminIdByPwd) {
        Map<String, String> mailContentMap = new HashMap<>();
        String adminId = adminIdByPwd.get(TEACHER_ID);
        String toEmail = adminIdByPwd.get(EMAIL_ID);
        String firstName = adminIdByPwd.get(FIRST_NAME);
        String password = adminIdByPwd.get(PASSWORD);
        String createTs = adminIdByPwd.get(CREATE_TS);
        String isRegistered = adminIdByPwd.get(IS_REGISTERED);
        String position = adminIdByPwd.get(POSITION);
        String officialEmail = adminIdByPwd.get(OFFICIAL_EMAIL);
        String officeLocation = adminIdByPwd.get(OFFICE_LOCATION);

        String subject;
        LOG.info(WELCOME_EMAIL_CNTNT_FOR_USER_ID, adminId);

        if("Y".equalsIgnoreCase(isRegistered)){
            subject = WELCOME_BACK_TO+universityName+", "+firstName+"!";
        } else {
            subject = WELCOME_TO+universityName+", "+firstName+"!";
        }

        String body = prepareAdminMailBody(adminId, firstName, password, createTs, position, officialEmail, officeLocation);

        mailContentMap.put(TO_EMAIL, toEmail);
        mailContentMap.put(SUBJECT, subject);
        mailContentMap.put(BODY, body);

        return mailContentMap;
    }

    public Map<String, String> generateManagerWelcomeEmail(Map<String, String> managerIdByPwd) {
        Map<String, String> mailContentMap = new HashMap<>();
        String managerId = managerIdByPwd.get(TEACHER_ID);
        String toEmail = managerIdByPwd.get(EMAIL_ID);
        String firstName = managerIdByPwd.get(FIRST_NAME);
        String password = managerIdByPwd.get(PASSWORD);
        String createTs = managerIdByPwd.get(CREATE_TS);
        String isRegistered = managerIdByPwd.get(IS_REGISTERED);
        String position = managerIdByPwd.get(POSITION);
        String officialEmail = managerIdByPwd.get(OFFICIAL_EMAIL);
        String officeLocation = managerIdByPwd.get(OFFICE_LOCATION);

        String subject;
        LOG.info(WELCOME_EMAIL_CNTNT_FOR_USER_ID, managerId);

        if("Y".equalsIgnoreCase(isRegistered)){
            subject = WELCOME_BACK_TO+universityName+", "+firstName+"!";
        } else {
            subject = WELCOME_TO+universityName+", "+firstName+"!";
        }

        String body = prepareManagerMailBody(managerId, firstName, password, createTs, position, officialEmail, officeLocation);

        mailContentMap.put(TO_EMAIL, toEmail);
        mailContentMap.put(SUBJECT, subject);
        mailContentMap.put(BODY, body);

        return mailContentMap;
    }

    private String prepareUserMailBody(String studentId, String firstName, String password, String courseId, String courseDuration, String createTs) {
        return DEAR+firstName+ "," +
                "\n" +
                "Congratulations and a warm welcome to "+universityName+"! We are thrilled to have you as a part of our vibrant community of learners.\n" +
                "\n" +
                "This email confirms your successful registration for the upcoming [Semester/Year]. As you embark on this exciting journey, we want to assure you that we are committed to providing you with an enriching and supportive academic experience.\n" +
                "\n" +
                "Here are a few important details to get you started:\n" +
                "\n" +
                "- Student Id: "+studentId+"\n" +
                "- "+TEMP_PWD+": "+password+"\n" +
                "- Program/Course: "+courseId+"\n" +
                "- Course Start Date: "+createTs+"\n" +
                "- Course Duration: "+courseDuration+"\n" +
                "\n" +
                "Please take a moment to explore our "+universityWebsite+" and familiarize yourself with the various resources available to you, including course information, campus facilities, and student services.\n" +
                "\n" +
                "Should you have any questions or need assistance, don't hesitate to reach out to our Student Support team at "+universityEmail+"/"+universityContacts+".\n" +
                "\n" +
                "Once again, welcome to "+universityName+"! We look forward to seeing you on campus and supporting you throughout your academic journey.\n" +
                "\n" +
                BEST_REGARDS +universityName+"\n" +universityEmail+"/"+universityContacts+"\n";
    }

    private String prepareTeacherMailBody(String teacherId, String firstName, String password, String depItd, String createTs,
                                          String position, String reportingTo, String officialEmailId, String officeLocation) {
        return DEAR+firstName+ "," +
                "\n" +
                "We are delighted to welcome you to "+universityName+"! as a valued member of our esteemed faculty. \n" +
                "Your expertise and dedication will undoubtedly contribute to the academic excellence and growth of our institution.\n" +
                "\n" +
                "- Teacher Id: "+teacherId+"\n" +
                "- "+TEMP_PWD+": "+password+"\n" +
                "- Position: "+position+"\n" +
                "- Department: "+depItd+"\n" +
                "- Date Of Joining: "+createTs+"\n" +
                "- Reporting To: "+reportingTo+"\n" +
                "\n" +
                "Office Location: Your office is located at "+officeLocation+". If you have any questions about your office or workspace, please feel free to reach out to "+reportingTo+".\n" +
                "\n" +
                "University Email: Your official university email address is "+officialEmailId+". This email will be used for all official communications, so please check it regularly.\n" +
                "\n" +
                "Teaching Assignments: Your teaching schedule for the upcoming semester will be shared by "+depItd+" in due course. If you have any specific preferences or requirements, please communicate them at the earliest convenience.\n" +
                "\n" +
                "Course Materials: You will receive access to the university's course management system where you can upload course materials, assignments, and interact with students.\n" +
                "\n" +
                "Faculty Meetings: Regular faculty meetings are held on Monday 8:00 AM. These meetings are an opportunity to discuss departmental matters, share ideas, and collaborate with colleagues."+
                "\n" +
                BEST_REGARDS +universityName+"\n" +universityEmail+"/"+universityContacts+"\n";
    }

    private String prepareAdminMailBody(String adminId, String firstName, String password, String createTs,
                                          String position, String officialEmailId, String officeLocation) {
        return DEAR+firstName+ "," +
                "\n" +

                "We are excited to welcome you as the newest member of the administrative team at "+universityName+"!\n." +
                "Congratulations on your appointment as an admin, and thank you for joining our prestigious institution.\n" +

                "Your role as an admin is vital to the efficient operation of our university, and we are confident that your expertise and dedication will contribute significantly to our continued success.\n" +
                "\n" +
                "Here are some essential details to help you get started:\n" +
                "\n" +
                "Admin Role: You have been assigned the role of ADMIN, which includes all the responsibilities to manage our university. Your role plays a crucial part in maintaining the university's administrative functions and supporting our students and faculty.\n"+
                "\n" +

                "University Credentials:\n" +
                "\n" +
                "- AdminId: "+adminId+"\n" +
                "- "+TEMP_PWD+": "+password+"\n" +
                "- Position: "+position+"\n" +
                "- Date Of Joining: "+createTs+"\n" +
                "\n" +
                "Please use the provided username and temporary password to log in to our administrative portal at "+universityWebsite+". Upon your first login, you will be prompted to change your password for security purposes."+
                "Orientation Program:\n" +
                "To help you become acquainted with our administrative procedures and campus policies, we have scheduled an orientation program on [Orientation Date]. During this program, you will learn about various aspects of your role and have the opportunity to meet fellow administrators.\n" +
                "\n"+
                "Office Location:\n" +
                "Your office is located in "+officeLocation+", [Building Name], Room [Room Number]. If you have any questions or need assistance finding your office, please don't hesitate to contact our administration department at "+universityContacts+"\n"+
                "\n" +
                "University Email: Your official university email address is "+officialEmailId+". This email will be used for all official communications, so please check it regularly.\n" +
                "\n" +
                "University Policies:\n" +
                "Familiarize yourself with our university's policies and guidelines, which can be found in the administration handbook provided to you. These policies are essential to ensuring a smooth and efficient administrative process."+
                "\n" +
                "Contact Information:\n" +
                "If you have any immediate questions or require assistance, please feel free to reach out to our administration department at "+universityEmail+" or "+universityContacts+"." +
                "\n" +
                BEST_REGARDS +universityName+"\n" +universityEmail+"/"+universityContacts+"\n";
    }

    private String prepareManagerMailBody(String adminId, String firstName, String password, String createTs,
                                        String position, String officialEmailId, String officeLocation) {
        return DEAR+firstName+ "," +
                "\n" +

                "We are pleased to inform you that your registration with the "+universityName+" has been successfully completed\n." +
                "Welcome to our university's management cell, where you will have access to a range of powerful tools and resources to streamline administrative tasks and support the efficient operation of our institution." +

                "Dashboard:\n" +
                "Upon logging in, you will be directed to the management dashboard. This dashboard provides an overview of key functions and allows you to manage various aspects of university administration, including student records, course scheduling, faculty management, and more.\n" +

                "University Credentials:\n" +
                "\n" +
                "- ManagerId: "+adminId+"\n" +
                "- "+TEMP_PWD+": "+password+"\n" +
                "- Position: "+position+"\n" +
                "- Date Of Joining: "+createTs+"\n" +
                "\n" +
                "Please use the provided username and temporary password to log in to our administrative portal at "+universityWebsite+". Upon your first login, you will be prompted to change your password for security purposes.\n"+
                "User Guide:\n" +
                "To assist you in navigating the system effectively, we have prepared a detailed User Guide. You can access it from the dashboard's Help section. This guide provides step-by-step instructions on using the system's features.\n"+
                "Office Location:\n" +
                "Your office is located in "+officeLocation+", [Building Name], Room [Room Number]. If you have any questions or need assistance finding your office, please don't hesitate to contact our administration department at "+universityContacts+"\n"+
                "\n" +
                "University Email: Your official university email address is "+officialEmailId+". This email will be used for all official communications, so please check it regularly.\n" +
                "\n" +
                "Support:\n" +
                "If you encounter any issues or have questions about the University Management System, our dedicated support team is here to assist you\n." +
                "You can reach out to them via email at "+universityEmail+" or by calling "+universityContacts+".\n" +
                "We are confident that our system will enhance your ability to manage university operations efficiently and effectively. If you have any suggestions or feedback for improving the system, please feel free to share them with us.\n" +
                "\n"+
                "Thank you for choosing our University Management System. We look forward to working together to make our university's administrative processes more streamlined and productive.\n"+
                "\n"+
                BEST_REGARDS +universityName+"\n" +universityEmail+"/"+universityContacts+"\n";
    }
}
