package edu.uclm.esi.videochat.model;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Authenticator;
import javax.mail.internet.MimeMessage;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.InternetAddress;


public class Email {
	
	private final Properties properties = new Properties();

	/**
	 * INFO: establecemos el servidor de correo con las credenciales para enviar el mensaje
	 * @param destinatario
	 * @param subject
	 * @param body
	 */
	public void send(String destinatario, String subject, String body) {
		String smtpHost= "smtp.office365.com";
		String startTTLS="true";
		String port="587";
		String sender="raul.valentin1@alu.uclm.es";		// REMITENTE
		String serverUser="raul.valentin1@alu.uclm.es";	// USUARIO
		String userAutentication= "true";
		String pwd="*****";
		String fallback="true";	
		
		properties.put("mail.smtp.host", smtpHost);  
        properties.put("mail.smtp.starttls.enable", startTTLS);  
        properties.put("mail.smtp.port", port);  
        properties.put("mail.smtp.mail.sender", sender);  
        properties.put("mail.smtp.user", serverUser);  
        properties.put("mail.smtp.auth", userAutentication);
        properties.put("mail.smtp.socketFactory.port", port);
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.socketFactory.fallback", fallback);
        
        Runnable r = new Runnable() {
			@Override
			public void run() {
		        Authenticator auth = new AutentificadorSMTP(sender, pwd);
		        Session session = Session.getInstance(properties, auth);

		        MimeMessage msg = new MimeMessage(session);
		        try {
			        msg.setSubject(subject);
			        msg.setText(body);
			        msg.setFrom(new InternetAddress(sender));
			        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(destinatario));
			        Transport.send(msg);
		        } catch (Exception e) {
					System.err.println(e);
				}
			}
		};
		new Thread(r).start();
	}
	
	private class AutentificadorSMTP extends javax.mail.Authenticator {
        private String sender;
		private String pwd;

		public AutentificadorSMTP(String sender, String pwd) {
			this.sender = sender;
			this.pwd = pwd;
		}

		public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(sender, pwd);
        }
    }
}
