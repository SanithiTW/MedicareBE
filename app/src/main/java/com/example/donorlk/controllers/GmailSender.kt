import java.util.*
import javax.mail.*
import javax.mail.internet.*

class GmailSender(private val email: String, private val password: String) {

    fun sendEmail(recipient: String, subject: String, body: String) {
        val props = Properties()
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.host"] = "smtp.gmail.com"
        props["mail.smtp.port"] = "587"

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(email, password)
            }
        })

        try {
            val message = MimeMessage(session)
            message.setFrom(InternetAddress(email))
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient))
            message.subject = subject
            message.setText(body)

            Thread {
                Transport.send(message)
            }.start()
        } catch (e: MessagingException) {
            e.printStackTrace()
        }
    }
}
