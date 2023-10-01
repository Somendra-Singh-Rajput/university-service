package com.apnaclassroom.service;

import com.apnaclassroom.util.CommonUtility;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender javaMailSender;

    @Autowired
    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public boolean sendEmail(String toEmail, String subject, String text) {
        if (CommonUtility.isValidEmail(toEmail)) {
            LOG.info("Sending email to EmailId: {}", toEmail);
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(toEmail);
                message.setSubject(subject);
                message.setText(text);

                javaMailSender.send(message);

                return true; //Email sent successfully
            } catch (MailException ex) {
                LOG.error("An error occurred while sending email, Exception: {}", ExceptionUtils.getStackTrace(ex));
                return false; //Email sending failed
            }
        } else {
            LOG.info("Provided email address is not a valid email address!");
            return false;
        }
    }
}
