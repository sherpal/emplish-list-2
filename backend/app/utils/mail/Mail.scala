package utils.mail

import java.util.Properties

import javax.activation.{CommandMap, MailcapCommandMap}
import javax.mail._
import javax.mail.internet._
import utils.config.ConfigRequester.|>
import utils.mail.Mail.MailSetting

import scala.util.Try

/**
  * The [[Mail]] class can be created to send emails.
  * It uses connection information specified in the `application.conf` file.
  *
  * If you need to put attachment in the mail, you need to make an instance of [[Attachment]], specifying how the
  * [[javax.activation.DataHandler]] needs to be instantiated.
  *
  * @example
  *   import mail.Mail.mail
  *   mail.sendEmail(new InternetAddress("example@provider.com"), "A subject", html(h1("Some content")))
  *   mail.sendEmail(new InternetAddress("example@provider.com"), "Some content"))
  */
final class Mail private (password: String) {

  def emplishListAddress: String = Mail.mailAddress.getAddress

  lazy val authenticator: Authenticator = new Authenticator {
    override def getPasswordAuthentication = new PasswordAuthentication(emplishListAddress, password)
  }

  lazy val mailSetting: MailSetting = MailSetting("smtp.gmail.com", 587, "TLS")

  lazy val props: Properties = {
    val p = System.getProperties
    p.put("mail.smtp.auth", true.toString)
    p.put("mail.smtp.host", mailSetting.host)
    p.put("mail.smtp.port", mailSetting.port.toString)
    p.put("mail.smtp.starttls.enable", true.toString)
    p.put("mail.smtp.ssl.protocols", "TLSv1.2")
    p
  }

  lazy val session: Session = Session.getInstance(props, authenticator)

  /**
    * If both the `htmlBody` and the `plainTextBody` are set, only the `htmlBody` is used.
    *
    * @param recipient recipient of the message
    * @param subject subject of the mail
    * @param htmlBody html body as String, if set
    * @param plainTextBody plain text body as String, if set
    */
  private def sendEmail(
      recipient: InternetAddress,
      subject: String,
      htmlBody: Option[String] = None,
      plainTextBody: Option[String] = None,
      attachments: Seq[Attachment] = Seq()
  ): Try[Unit] = Try {
    val mc: MailcapCommandMap = CommandMap.getDefaultCommandMap.asInstanceOf[MailcapCommandMap]
    mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html")
    mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml")
    mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain")
    mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed")
    mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822")
    val msg = new MimeMessage(session)
    msg.setFrom(new InternetAddress(emplishListAddress))
    msg.addRecipient(Message.RecipientType.TO, recipient)
    msg.setSubject(subject)

    def addAttachment(mimeMultipart: MimeMultipart, attachment: Attachment): Unit = {
      val attachmentPart = new MimeBodyPart
      attachmentPart.setDataHandler(attachment.dataHandler)
      mimeMultipart.addBodyPart(attachmentPart)
    }

    val multipart = new MimeMultipart

    // adding the main body part
    val bodyPart = new MimeBodyPart
    (htmlBody, plainTextBody) match {
      case (Some(bodyContent), _) =>
        bodyPart.setContent(bodyContent, "text/html; charset=\"UTF-8\"")

      case (None, Some(bodyContent)) =>
        bodyPart.setContent(bodyContent, "text/plain; charset=\"UTF-8\"")

      case (None, None) =>
        bodyPart.setContent("", "text/plain")
    }
    multipart.addBodyPart(bodyPart)

    // adding all attachments
    attachments.foreach(addAttachment(multipart, _))

    // putting all the contents into the mail
    msg.setContent(multipart)

    Transport.send(msg)
  }

//  /**
//    * Send an email to the specified recipient, subject and html content
//    * @param recipient of the mail
//    * @param subject of the mail
//    * @param html body of the mail
//    */
//  def sendEmail(
//      recipient: InternetAddress,
//      subject: String,
//      html: Text.TypedTag[String]
//  ): Unit = sendEmail(recipient, subject, htmlBody = Some(html.render))

  /**
    * Send an email to the specified recipient, with specified subject and text content
    * @param recipient of the mail
    * @param subject of the mail
    * @param content body of the mail
    */
  def sendEmail(
      recipient: InternetAddress,
      subject: String,
      content: String
  ): Try[Unit] = sendEmail(recipient, subject, plainTextBody = Some(content))

  def sendEmail(
      recipient: InternetAddress,
      subject: String,
      content: String,
      attachments: Seq[Attachment]
  ): Try[Unit] = sendEmail(recipient, subject, plainTextBody = Some(content), attachments = attachments)

  /**
    * Send an email to the specified recipient, with specified subject and html content from the `twirlGenerator`, with
    * the specified argument `t`.
    *
    * The `twirlGenerator` is thought as a function from type `T` to string content, generated using twirl, so that the
    * html can be made in html.
    *
    * @example
    *          Imagine you have a twirl template with signature (String, Int) => String, called `template`. You can do
    *          sendTwirlEmail(recipient, subject, html.template.f.tupled)(("aString", anInt))
    *          If the template only take one argument, then the `.tupled` is not required (and actually doesn't compile)
    *
    * @param recipient of the mail
    * @param subject of the mail
    * @param twirlGenerator twirl generator function, taking argument of type T
    * @param t argument to give to the twirl generator
    * @tparam T type taken by the twirl generator function
    */
  def sendTwirlEmail[T](
      recipient: InternetAddress,
      subject: String,
      twirlGenerator: T => play.twirl.api.HtmlFormat.Appendable,
      t: T,
      attachments: Seq[Attachment] = Seq()
  ): Try[Unit] = sendEmail(
    recipient,
    subject,
    htmlBody = Some("<!DOCTYPE html>" + twirlGenerator(t)),
    attachments = attachments
  )

  def sendTwirlEmail[T](
      recipient: InternetAddress,
      subject: String,
      twirlGenerator: T => play.twirl.api.HtmlFormat.Appendable
  )(implicit ev: Unit =:= T): Try[Unit] =
    sendTwirlEmail(recipient, subject, twirlGenerator, ev(()))

  def sendTwirlEmail[T](
      recipient: InternetAddress,
      subject: String,
      twirlGenerator: T => play.twirl.api.HtmlFormat.Appendable,
      attachments: Seq[Attachment]
  )(implicit ev: Unit =:= T): Try[Unit] =
    sendTwirlEmail(recipient, subject, twirlGenerator, ev(()), attachments)

}

object Mail {

  private[mail] final case class MailSetting(host: String, port: Int, method: String)

  def apply(password: String): Mail = new Mail(password)

  def mail: Option[Mail] = password.map(apply)

  lazy val password: Option[String] = (|> >> "mail" >> "password").maybeInto[String]

  final case class MailAccount(userName: String, address: String, password: String)

  def mailAddress: InternetAddress = new InternetAddress("emplish.list@gmail.com")

  //def reportMailAccount: MailAccount = ???

}
