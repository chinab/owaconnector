package davmail.exchange;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.print.URIException;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.mortbay.util.URIUtil;

import davmail.exception.DavMailException;
import davmail.http.DavGatewayHttpClientFacade;

/**
 * Exchange message.
 */
public class Message implements Comparable<Message> {
	/**
	 * 
	 */
	private final ExchangeSession exchangeSession;

	/**
	 * @param exchangeSession
	 */
	Message(ExchangeSession exchangeSession) {
		this.exchangeSession = exchangeSession;
	}

	protected String messageUrl;

	protected String permanentUrl;
	/**
	 * Message uid.
	 */
	protected String uid;
	/**
	 * Message IMAP uid, unique in folder (x0e230003).
	 */
	protected long imapUid;
	/**
	 * MAPI message size.
	 */
	public int size;
	/**
	 * Mail header message-id.
	 */
	protected String messageId;
	/**
	 * Message date (urn:schemas:mailheader:date).
	 */
	public String date;

	/**
	 * Message flag: read.
	 */
	public boolean read;
	/**
	 * Message flag: deleted.
	 */
	public boolean deleted;
	/**
	 * Message flag: junk.
	 */
	public boolean junk;
	/**
	 * Message flag: flagged.
	 */
	public boolean flagged;
	/**
	 * Message flag: draft.
	 */
	public boolean draft;
	/**
	 * Message flag: answered.
	 */
	public boolean answered;
	/**
	 * Message flag: fowarded.
	 */
	public boolean forwarded;

	/**
	 * Message content parsed in a MIME message.
	 */
	protected MimeMessage mimeMessage;

	/**
	 * IMAP uid , unique in folder (x0e230003)
	 * 
	 * @return IMAP uid
	 */
	public long getImapUid() {
		return imapUid;
	}

	/**
	 * Exchange uid.
	 * 
	 * @return uid
	 */
	public String getUid() {
		return uid;
	}

	/**
	 * Return permanent message url.
	 * 
	 * @return permanent message url
	 * @throws URIException
	 *             on error
	 */
	public String getPermanentUrl() throws URISyntaxException {
		return permanentUrl;
	}

	/**
	 * Return encoded message name.
	 * 
	 * @return encoded message name
	 * @throws IOException
	 *             on error
	 */
	public String getEncodedMessageName() throws IOException, HttpException {
		int index = messageUrl.lastIndexOf('/');
		if (index < 0) {
			throw new DavMailException("EXCEPTION_INVALID_MESSAGE_URL",
					messageUrl);
		}

		return URIUtil.encodePath(messageUrl.substring(index + 1));
	}

	/**
	 * Return message flags in IMAP format.
	 * 
	 * @return IMAP flags
	 */
	public String getImapFlags() {
		StringBuilder buffer = new StringBuilder();
		if (read) {
			buffer.append("\\Seen ");
		}
		if (deleted) {
			buffer.append("\\Deleted ");
		}
		if (flagged) {
			buffer.append("\\Flagged ");
		}
		if (junk) {
			buffer.append("Junk ");
		}
		if (draft) {
			buffer.append("\\Draft ");
		}
		if (answered) {
			buffer.append("\\Answered ");
		}
		if (forwarded) {
			buffer.append("$Forwarded ");
		}
		return buffer.toString().trim();
	}

	/**
	 * Write MIME message to os
	 * 
	 * @param os
	 *            output stream
	 * @throws IOException
	 *             on error
	 * @throws DavMailException
	 */
	public void write(OutputStream os) throws IOException, DavMailException {
		HttpGet method = new HttpGet(permanentUrl);
		method.setHeader("Content-Type", "text/xml; charset=utf-8");
		method.setHeader("Translate", "f");
		BufferedReader reader = null;
		try {
			HttpResponse response = DavGatewayHttpClientFacade
					.executeGetMethod(this.exchangeSession.httpClient, method, false);
			reader = new BufferedReader(new InputStreamReader(response
					.getEntity().getContent()));
			OutputStreamWriter isoWriter = new OutputStreamWriter(os);
			String line;
			while ((line = reader.readLine()) != null) {
				if (".".equals(line)) {
					line = "..";
					// patch text/calendar to include utf-8 encoding
				} else if ("Content-Type: text/calendar;".equals(line)) {
					StringBuilder headerBuffer = new StringBuilder();
					headerBuffer.append(line);
					while ((line = reader.readLine()) != null
							&& line.startsWith("\t")) {
						headerBuffer.append((char) 13);
						headerBuffer.append((char) 10);
						headerBuffer.append(line);
					}
					if (headerBuffer.indexOf("charset") < 0) {
						headerBuffer.append(";charset=utf-8");
					}
					headerBuffer.append((char) 13);
					headerBuffer.append((char) 10);
					headerBuffer.append(line);
					line = headerBuffer.toString();
				}
				isoWriter.write(line);
				isoWriter.write((char) 13);
				isoWriter.write((char) 10);
			}
			isoWriter.flush();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					ExchangeSession.LOGGER.warn("Error closing message input stream", e);
				}
			}
			method.abort();
		}
	}

	/**
	 * Load message content in a Mime message
	 * 
	 * @return mime message
	 * @throws IOException
	 *             on error
	 * @throws MessagingException
	 *             on error
	 * @throws DavMailException
	 */
	public MimeMessage getMimeMessage() throws IOException,
			MessagingException, DavMailException {
		if (mimeMessage == null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			write(baos);
			mimeMessage = new MimeMessage(null, new ByteArrayInputStream(
					baos.toByteArray()));
		}
		return mimeMessage;
	}

	/**
	 * Drop mime message to avoid keeping message content in memory.
	 */
	public void dropMimeMessage() {
		mimeMessage = null;
	}

	/**
	 * Delete message.
	 * 
	 * @throws IOException
	 *             on error
	 */
	public void delete() throws IOException {
		DavGatewayHttpClientFacade.executeDeleteMethod(this.exchangeSession.httpClient,
				permanentUrl);
	}

	/**
	 * Comparator to sort messages by IMAP uid
	 * 
	 * @param message
	 *            other message
	 * @return imapUid comparison result
	 */
	public int compareTo(Message message) {
		long compareValue = (imapUid - message.imapUid);
		if (compareValue > 0) {
			return 1;
		} else if (compareValue < 0) {
			return -1;
		} else {
			return 0;
		}
	}

	/**
	 * Override equals, compare IMAP uids
	 * 
	 * @param message
	 *            other message
	 * @return true if IMAP uids are equal
	 */
	@Override
	public boolean equals(Object message) {
		return message instanceof Message
				&& imapUid == ((Message) message).imapUid;
	}

	/**
	 * Override hashCode, return imapUid hashcode.
	 * 
	 * @return imapUid hashcode
	 */
	@Override
	public int hashCode() {
		return (int) (imapUid ^ (imapUid >>> 32));
	}
}