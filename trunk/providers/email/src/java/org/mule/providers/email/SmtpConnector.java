/* 

 * $Header$

 * $Revision$

 * $Date$

 * ------------------------------------------------------------------------------------------------------

 * 

 * Copyright (c) Cubis Limited. All rights reserved.

 * http://www.cubis.co.uk 

 * 

 * The software in this package is published under the terms of the BSD

 * style license a copy of which has been included with this distribution in

 * the LICENSE.txt file. 

 *

 */


package org.mule.providers.email;


import org.mule.MuleException;
import org.mule.config.i18n.Messages;
import org.mule.providers.AbstractServiceEnabledConnector;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.provider.UMOMessageReceiver;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Calendar;
import java.util.Properties;


/**
 * <code>SmtpConnector</code> is used to connect to and send data to
 * an SMTP mail server
 *
 * @author <a href="mailto:ross.mason@cubis.co.uk">Ross Mason</a>
 * @version $Revision$
 */
public class SmtpConnector extends AbstractServiceEnabledConnector
{
    public static final int DEFAULT_SMTP_PORT = 25;

    /**
     * Holds value of bcc addresses.
     */
    private String bcc;

    /**
     * Holds value of  cc addresses.
     */
    private String cc;

    /**
     * determines whether a mailbox connection is active
     */
    private boolean connected = false;

    /**
     * Holds value of default subject
     */
    private String defaultSubject = "[No Subject]";

    /**
     * Holds value of the from address.
     */
    private String from;

    /**
     * Holds value of property SMTP password.
     */
    private String password;

    /**
     * Holds value of property hostname for the smtp server.
     */
    private String hostname = "localhost";

    /**
     * Holds value of property port for the smtp server.
     */
    private int port = DEFAULT_SMTP_PORT;


    /**
     * Holds value of property SMTPusername.
     */
    private String username;

    public SmtpConnector() throws Exception
    {
        super.doInitialise();
    }

    /* (non-Javadoc)
     * @see org.mule.providers.UMOConnector#createMessage(java.lang.Object)
     */
    public Object createMessage(Object message, Session session) throws Exception
    {
        if (message instanceof Message)
        {
            return message;
        }
        String body = null;
        if (message instanceof String)
        {
            body = (String) message;
        }
        Message msg = createMessage(getFromAddress(), null, cc, bcc, defaultSubject, body, session);
        return msg;
    }

    protected Message createMessage(String from, String to, String cc, String bcc, String subject, String body, Session session)
            throws MuleException
    {
        Message msg = new MimeMessage(session);
        try
        {
            //to

            InternetAddress[] toAddrs = null;
            if ((to != null) && !to.equals(""))
            {
                toAddrs = InternetAddress.parse(to, false);
                msg.setRecipients(Message.RecipientType.TO, toAddrs);
            }
            else
            {
                throw new MuleException(new org.mule.config.i18n.Message(Messages.X_IS_NULL, "toAddress"));
            }
            //sent date
            msg.setSentDate(Calendar.getInstance().getTime());
            //from
            if(from==null) {
                throw new IllegalArgumentException("From address must be set");
            }
            msg.setFrom(new InternetAddress(from));
            //cc
            InternetAddress[] ccAddrs = null;
            if ((cc != null) && !cc.equals(""))
            {
                ccAddrs = InternetAddress.parse(cc, false);
                msg.setRecipients(Message.RecipientType.CC, ccAddrs);
            }
            InternetAddress[] bccAddrs = null;
            if ((bcc != null) && !bcc.equals(""))
            {
                bccAddrs = InternetAddress.parse(bcc, false);
                msg.setRecipients(Message.RecipientType.BCC, bccAddrs);
            }
            //subject
            if ((subject != null) && !subject.equals(""))
            {
                msg.setSubject(subject);
            }
            else
            {
                msg.setSubject("(no subject)");
            }
            //todo attachments

            // create the Multipart and its parts to it
            Multipart mp = new MimeMultipart();
            MimeBodyPart mbp = new MimeBodyPart();
            mbp.setText(body);
            mp.addBodyPart(mbp);

            // add the Multipart to the message
            msg.setContent(mp);
            return msg;
        }
        catch (MuleException e)
        {
            throw e;
        }
        catch (MessagingException e)
        {
            throw new MuleException(new org.mule.config.i18n.Message(Messages.FAILED_TO_SET_PROPERTIES_ON_X, "Email message"), e);
        }
    }

    /**
     * @return
     */
    public String getFromAddress()
    {
        return from;
    }

    /* (non-Javadoc)
     * @see org.mule.providers.UMOConnector#getProtocol()
     */
    public String getProtocol()
    {
        return "smtp";
    }

    public boolean isConnected()
    {
        return this.connected;
    }

    /* (non-Javadoc)
     * @see org.mule.providers.UMOConnector#registerListener(javax.jms.MessageListener, java.lang.String)
     */
    public UMOMessageReceiver createReceiver(UMOComponent component, UMOEndpoint endpoint) throws Exception
    {
        throw new UnsupportedOperationException("Listeners cannot be registered on a SMTP endpoint");
    }

    /*
     * @see org.mule.providers.UMOConnector#start()
     */
    public void startConnector() throws UMOException
    {
        //force connection to server
        dispatcherFactory.create(this);
    }

    /* (non-Javadoc)
     * @see org.mule.providers.UMOConnector#stop()
     */
    public void stopConnector() throws UMOException
    {
        connected = false;
    }

    /* (non-Javadoc)
     * @see org.mule.providers.AbstractConnector#disposeConnector()
     */
    protected void disposeConnector() throws UMOException
    {
        stopConnector();
    }

    /**
     * @return
     */
    public String getBccAddresses()
    {
        return bcc;
    }

    /**
     * @return
     */
    public String getCcAddresses()
    {
        return cc;
    }

    /**
     * @return
     */
    public String getSubject()
    {
        return defaultSubject;
    }

    /**
     * @return
     */
    public String getHostname()
    {
        return hostname;
    }

    /**
     * @return
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @return
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * @param string
     */
    public void setBccAddresses(String string)
    {
        bcc = string;
    }

    /**
     * @param string
     */
    public void setCcAddresses(String string)
    {
        cc = string;
    }

    /**
     * @param string
     */
    public void setSubject(String string)
    {
        defaultSubject = string;
    }

    /**
     * @param string
     */
    public void setFromAddress(String string)
    {
        from = string;
    }

    /**
     * @param string
     */
    public void setHostname(String string)
    {
        hostname = string;
    }

    /**
     * @param string
     */
    public void setPassword(String string)
    {
        password = string;
    }

    /**
     * @param string
     */
    public void setUsername(String string)
    {
        username = string;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }


    protected Session createMailSession(URLName url)
    {
        if (url == null)
        {
            throw new IllegalArgumentException("Url argument cannot be null when creating a session");
        }
        Properties props = System.getProperties();
        props.put("mail.smtp.host", url.getHost());
        props.put("mail.smtp.port", String.valueOf(url.getPort()));
        Session session = Session.getDefaultInstance(props, null);
        session.setDebug(logger.isDebugEnabled());
        PasswordAuthentication pw = new PasswordAuthentication(url.getUsername(), url.getPassword());
        session.setPasswordAuthentication(url, pw);
        return session;
    }
}