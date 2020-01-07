package utils.mail

import javax.activation.{DataHandler, DataSource}
import javax.mail.util.ByteArrayDataSource

/**
  * Represents an Attachment that can be put inside an email.
  *
  * An attachment has to be able to produce a [[DataHandler]] to put into the mail.
  */
sealed trait Attachment {
  def dataHandler: DataHandler
}

/**
  * Creates an attachment for a PDF, based on the pdf bytes.
  * @param pdfBytes array of the bytes of the pdf
  */
final case class PdfAttachment(pdfBytes: Array[Byte]) extends Attachment {
  lazy val dataSource: DataSource = new ByteArrayDataSource(pdfBytes, "application/pdf")
  def dataHandler: DataHandler = new DataHandler(dataSource)
}
