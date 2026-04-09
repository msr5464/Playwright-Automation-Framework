package automation.core;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Email helper with IMAP support and connection locking for parallel test safety.
 */
public class EmailHelper
{

    private static final ReentrantLock connectionLock = new ReentrantLock();
    private static final int MAX_WAIT_SECONDS = 60;
    private static final int POLL_INTERVAL_MS = 3000;

    /**
     * Extract OTP from email with connection locking for parallel safety
     */
    public static String getOTPFromEmail(String host, String email, String password, String subjectPattern)
    {
        connectionLock.lock();
        try
        {
            return fetchOTPFromInbox(host, email, password, subjectPattern);
        }
        finally
        {
            connectionLock.unlock();
        }
    }

    private static String fetchOTPFromInbox(String host, String email, String password, String subjectPattern)
    {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", host);
        props.put("mail.imaps.port", "993");
        props.put("mail.imaps.ssl.enable", "true");

        try
        {
            Session session = Session.getInstance(props);
            Store store = session.getStore("imaps");
            store.connect(host, email, password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < MAX_WAIT_SECONDS * 1000)
            {
                Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

                for (int i = messages.length - 1; i >= 0; i--)
                {
                    Message msg = messages[i];
                    // Only check emails from last 5 minutes
                    if (System.currentTimeMillis() - msg.getReceivedDate().getTime() > 5 * 60 * 1000)
                    {
                        continue;
                    }

                    String subject = msg.getSubject();
                    if (subject != null && subject.matches(subjectPattern))
                    {
                        String body = getTextContent(msg);
                        String otp = extractOTPFromText(body);
                        if (otp != null)
                        {
                            msg.setFlag(Flags.Flag.SEEN, true);
                            inbox.close(false);
                            store.close();
                            return otp;
                        }
                    }
                }

                Thread.sleep(POLL_INTERVAL_MS);
            }

            inbox.close(false);
            store.close();
        }
        catch (Exception e)
        {
            Log.error("Email OTP extraction failed: " + e.getMessage());
        }
        return null;
    }

    private static String extractOTPFromText(String text)
    {
        if (text == null) return null;
        // Match 4-8 digit OTP patterns
        Pattern pattern = Pattern.compile("\\b(\\d{4,8})\\b");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find())
        {
            return matcher.group(1);
        }
        return null;
    }

    private static String getTextContent(Message message) throws Exception
    {
        if (message.isMimeType("text/plain"))
        {
            return message.getContent().toString();
        }
        else if (message.isMimeType("text/html"))
        {
            return org.jsoup.Jsoup.parse(message.getContent().toString()).text();
        }
        else if (message.isMimeType("multipart/*"))
        {
            Multipart multipart = (Multipart) message.getContent();
            for (int i = 0; i < multipart.getCount(); i++)
            {
                BodyPart part = multipart.getBodyPart(i);
                if (part.isMimeType("text/plain"))
                {
                    return part.getContent().toString();
                }
            }
        }
        return null;
    }

    /**
     * Send an HTML email report via SMTP.
     * Requires these properties to be configured:
     *   smtp.host, smtp.port, smtp.username, smtp.password
     */
    public static void sendEmail(Config config, String toAddresses, String subject, String htmlBody)
    {
        try
        {
            String host     = config.getRunTimeProperty("smtp.host");
            String port     = config.getRunTimeProperty("smtp.port", "587");
            String username = config.getRunTimeProperty("smtp.username");
            String password = config.getRunTimeProperty("smtp.password");

            if (host == null || username == null || password == null)
            {
                System.out.println("[WARN] SMTP not configured (smtp.host / smtp.username / smtp.password) — email skipped.");
                return;
            }

            Properties props = new Properties();
            props.put("mail.smtp.auth",            "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host",            host);
            props.put("mail.smtp.port",            port);

            Session session = Session.getInstance(props, new Authenticator()
            {
                @Override
                protected PasswordAuthentication getPasswordAuthentication()
                {
                    return new PasswordAuthentication(username, password);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            for (String to : toAddresses.split(","))
            {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(to.trim()));
            }
            message.setSubject(subject);

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlBody, "text/html; charset=utf-8");

            MimeMultipart multipart = new MimeMultipart("mixed");
            multipart.addBodyPart(htmlPart);
            message.setContent(multipart);

            Transport.send(message);
            System.out.println("Email report sent to: " + toAddresses);
        }
        catch (Exception e)
        {
            System.err.println("[WARN] Email send failed: " + e.getMessage());
        }
    }

    /**
     * Generate static OTP based on current date (MMYY format) - fallback
     */
    public static String generateStaticOTP()
    {
        java.time.LocalDate now = java.time.LocalDate.now();
        return String.format("%02d%02d", now.getMonthValue(), now.getYear() % 100);
    }
}
