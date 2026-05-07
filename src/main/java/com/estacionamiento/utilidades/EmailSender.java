package com.estacionamiento.utilidades;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class EmailSender {
    private final String host;
    private final String port;
    private final String username;
    private final String password;
    private final boolean useTLS;

    public EmailSender(String host, String port, String username, String password, boolean useTLS) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.useTLS = useTLS;
    }

    public void enviarCorreo(String destinatario, String asunto, String mensaje) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", String.valueOf(useTLS));
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(username));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
        msg.setSubject(asunto);
        msg.setText(mensaje);

        Transport.send(msg);
    }
}
