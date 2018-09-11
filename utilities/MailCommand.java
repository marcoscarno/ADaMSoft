/**
* Copyright (c) 2015 MS
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package ADaMSoft.utilities;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import ADaMSoft.utilities.activation.CommandMap;
import ADaMSoft.utilities.activation.MailcapCommandMap;

/**
* This class is used to send an email<p>
* Usage<br>
* MailCommand mailCommand = new MailCommand();<br>
* mailCommand.set_smtp("your smtp server");<br>
* mailCommand.set_smtp_port("465");<br>
* mailCommand.set_smtp_auth(true);<br>
* mailCommand.set_smtp_starttls(true);<br>
* mailCommand.set_smtp_user("your user");<br>
* mailCommand.set_smtp_password("your password");<br>
* mailCommand.set_smtp_ssl_enable(true);<br>
* mailCommand.set_from("field for from");<br>
* mailCommand.set_transport("smtp");<br>
* mailCommand.initialize();<br>
* mailCommand.addTo("address to which the mail will be sent, can be repeated");<br>
* mailCommand.setSubject("subject of the mail");<br>
* mailCommand.setBody("Text inside the mail");<br>
* mailCommand.prepareMessage();<br>
* mailCommand.addAttachment("file to enclose");<br>
* mailCommand.sendmessage();<br>
* mailCommand.get_sending_error<br>
* mailCommand.get_message_MailCommand<br><br>
* @author marco.scarno@gmail.com
* @date 02/09/15
*/
public class MailCommand
{
	private String smtp;
	private String smtp_user;
	private String smtp_password;
	private String from;
	private String body;
	private Session session;
	private MimeMessage message;
	Properties mailServerProperties;
	String message_MailCommand;
	private MimeMultipart multipart;
	boolean sending_error;
	private String transport_method;
	public MailCommand()
	{
		sending_error=false;
		mailServerProperties = System.getProperties();
		message_MailCommand="";
		transport_method="smtp";
	}
	public void set_transport(String transport_method)
	{
		this.transport_method=transport_method;
	}
	public void set_smtp(String smtp)
	{
		this.smtp=smtp;
	}
	public void set_smtp_port(String smtp_port)
	{
		mailServerProperties.setProperty("mail.smtp.port", smtp_port);
	}
	public void set_smtp_auth(boolean smtp_auth)
	{
		if (smtp_auth)
			mailServerProperties.setProperty("mail.smtp.auth", "true");
	}
	public void set_smtp_starttls(boolean smtp_starttls)
	{
		if (smtp_starttls)
			mailServerProperties.setProperty("mail.smtp.starttls.enable", "true");
	}
	public void set_smtp_user(String smtp_user)
	{
		mailServerProperties.setProperty("mail.user", smtp_user);
		mailServerProperties.setProperty("mail.smtp.user", smtp_user);
		this.smtp_user=smtp_user;
	}
	public void set_smtp_password(String smtp_password)
	{
		mailServerProperties.setProperty("mail.smtp.password", smtp_password);
		mailServerProperties.setProperty("mail.password", smtp_password);
		this.smtp_password=smtp_password;
	}
	public void set_smtp_ssl_enable(boolean smtp_ssl_enable)
	{
		if (smtp_ssl_enable)
			mailServerProperties.setProperty("mail.smtp.ssl.enable", "true");
	}
	public void set_from(String from)
	{
		this.from=from;
	}
	public void addAttachment(String filename)
	{
		try
		{
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.attachFile(filename);
			multipart.addBodyPart(messageBodyPart);
		}
		catch (Exception eattachment)
		{
			sending_error=true;
			if (!message_MailCommand.equals("")) message_MailCommand="\n"+eattachment.toString();
			else message_MailCommand=eattachment.toString();
		}
	}
	public void initialize()
	{
		MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
		CommandMap.setDefaultCommandMap(mc);

		session = Session.getDefaultInstance(mailServerProperties, null);
		multipart = new MimeMultipart();
		this.message = new MimeMessage(this.session);
		try
		{
			message.setFrom(from);
		}
		catch (Exception ex)
		{
			sending_error=true;
			if (!message_MailCommand.equals("")) message_MailCommand="\n"+ex.toString();
			else message_MailCommand=ex.toString();
		}
	}
	public void addTo(String to)
	{
		try
		{
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		}
		catch (Exception ex)
		{
			sending_error=true;
			if (!message_MailCommand.equals("")) message_MailCommand="\n"+ex.toString();
			else message_MailCommand=ex.toString();
		}
	}
	public void setSubject(String subject)
	{
		try
		{
			message.setSubject(subject);
		}
		catch (Exception ex)
		{
			sending_error=true;
			if (!message_MailCommand.equals("")) message_MailCommand="\n"+ex.toString();
			else message_MailCommand=ex.toString();
		}
	}
	public void setBody(String body)
	{
		this.body=body;
	}
	public void prepareMessage()
	{
		try
		{
			if (body!=null)
			{
				MimeBodyPart messageBodyPart = new MimeBodyPart();
				messageBodyPart.setText(body);
				multipart.addBodyPart(messageBodyPart);
			}
		}
		catch (Exception ex)
		{
			sending_error=true;
			if (!message_MailCommand.equals("")) message_MailCommand="\n"+ex.toString();
			else message_MailCommand=ex.toString();
		}
	}
	public void sendmessage()
	{
		try
		{
			message.setContent(multipart);
			Transport transport = session.getTransport(transport_method);
			transport.connect(smtp, smtp_user, smtp_password);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
		}
		catch (MessagingException ex)
		{
			sending_error=true;
			if (!message_MailCommand.equals("")) message_MailCommand="\n"+ex.toString();
			else message_MailCommand=ex.toString();
		}
	}
	public String get_message_MailCommand()
	{
		return message_MailCommand;
	}
	public boolean get_sending_error()
	{
		return sending_error;
	}
}