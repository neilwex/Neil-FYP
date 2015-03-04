package com.fyp;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

/**
 * Created by Neil on 21/02/2015.
 */
public class Email {

    private String from;
    private String[] to;
    private String[] cc;
    private String subject;
    private String emailContent;
    private String webmaster;

    public Email(String from, String[] to, String[] cc, String subject, String emailContent, String webmaster) {

        this.from = from;
        this.to = to;
        this.cc = cc;
        this.subject = subject;
        this.emailContent = emailContent;
        this.webmaster = webmaster;
    }

    /**
     * Sets-up and sends the email with the relevant required information
     *
     * @return      true if the email is successfully sent; otherwise, false
     */
    public boolean prepareAndSendEmail() {

        org.apache.commons.mail.Email email = new SimpleEmail();
        ///email.setHostName("HOST_NAME_HERE");
        email.setSmtpPort(25);

        try
        {
            email.setFrom(from);
            email.setSubject(subject);
            email.setMsg(emailContent);

            for (String oneTo : to)
            {
                email.addTo(oneTo);
            }
            for (String oneCc : cc)
            {
                email.addCc(oneCc);
            }
            email.send();
            return true;
        }
        catch ( EmailException e )
        {
            System.out.println("Problem while sending an email");
            e.printStackTrace();
            return false;
        }
    }

}
