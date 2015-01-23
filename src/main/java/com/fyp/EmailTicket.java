package com.fyp;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

/**
 * Created by o_connor on 18-Aug-14.
 */
public class EmailTicket {

    private String from;
    private String[] to;
    private String[] cc;
    private String subject;
    private String emailContent;
    private boolean copyToWebmaster;
    private String webmaster;

    public EmailTicket(String from, String[] to, String[] cc, String subject, String emailContent,
                       boolean copyToWebmaster, String webmaster) {

        this.from = from;
        this.to = to;
        this.cc = cc;
        this.subject = subject;
        this.emailContent = emailContent;
        this.copyToWebmaster = copyToWebmaster;
        this.webmaster = webmaster;
    }

    /**
     * Sets-up and sends the email with the relevant required information
     *
     * @return      true if the email is successfully sent; otherwise, false
     */
    public boolean prepareAndSendEmail() {

        org.apache.commons.mail.Email email = new SimpleEmail();
        email.setHostName("smtp.embl.de");
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
            if (copyToWebmaster)
            {
                email.addBcc(webmaster);
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
