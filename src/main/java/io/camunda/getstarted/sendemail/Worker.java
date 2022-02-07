package io.camunda.getstarted.sendemail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;


import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@SpringBootApplication
@EnableZeebeClient
public class Worker {

    @Autowired
    private JavaMailSender javaMailSender;

    public static void main(String[] args) {
        SpringApplication.run(Worker.class, args);
    }

    @ZeebeWorker(type = "SendEmail")
    public void CheckCelebAge(final JobClient client, final ActivatedJob job) {

        Map<String, Object> variablesAsMap = job.getVariablesAsMap();

        String sender = variablesAsMap.get("sender").toString();
        String receiver = variablesAsMap.get("receiver").toString();
        String subject = variablesAsMap.get("subject").toString();
        String body = variablesAsMap.get("body").toString();

        List<String> invalidEmailAddresses = new ArrayList();
        boolean invalidEmails = false;

        if(!ValidateEmail.isValidEmail(sender)){
            invalidEmailAddresses.add(sender);
            invalidEmails = true;
        }
        if(!ValidateEmail.isValidEmail(receiver)){
            invalidEmailAddresses.add(receiver);
            invalidEmails = true;
        }
        if(invalidEmails)
        {
            client.newThrowErrorCommand(job)
                    .errorCode("INVALID_EMAIL")
                    .send();
        }else {

            try {
                sendMail(sender, receiver, subject, body);
                String resultMessage = "Mail Sent Successfully to " + receiver;

                HashMap<String, Object> variables = new HashMap<>();
                variables.put("result", resultMessage);
                client.newCompleteCommand(job.getKey())
                        .variables(variables)
                        .send()
                        .exceptionally((throwable -> {
                            throw new RuntimeException("Could not complete job", throwable);
                        }));
            } catch (MessagingException e) {
                e.printStackTrace();
                client.newFailCommand(job.getKey());
            }
        }
    }

    private void sendMail(String sender, String receiver, String subject, String body) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(sender);
        helper.setTo(receiver);
        helper.setSubject(subject);
        helper.setText(body, true);

        javaMailSender.send(message);
    }

}
