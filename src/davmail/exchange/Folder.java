package davmail.exchange;


/**
 * Exchange folder with IMAP properties
 */
public class Folder {
	/**
	 * Logical (IMAP) folder path.
	 */
	public String folderPath;
	/**
	 * Folder unread message count.
	 */
	public int unreadCount;
	/**
	 * true if folder has subfolders (DAV:hassubs).
	 */
	public boolean hasChildren;
	/**
	 * true if folder has no subfolders (DAV:nosubs).
	 */
	public boolean noInferiors;
	/**
	 * Requested folder name
	 */
	public String folderName;
	/**
	 * Folder content tag (to detect folder content changes).
	 */
	public String contenttag;
	/**
	 * Folder message list, empty before loadMessages call.
	 */
	public MessageList messages;

	/**
	 * Get IMAP folder flags.
	 * 
	 * @return folder flags in IMAP format
	 */
	public String getFlags() {
		if (noInferiors) {
			return "\\NoInferiors";
		} else if (hasChildren) {
			return "\\HasChildren";
		} else {
			return "\\HasNoChildren";
		}
	}

	/**
	 * Folder message count.
	 * 
	 * @return message count
	 */
	public int count() {
		return messages.size();
	}

	/**
	 * Compute IMAP uidnext.
	 * 
	 * @return max(messageuids)+1
	 */
	public long getUidNext() {
		return messages.get(messages.size() - 1).getImapUid() + 1;
	}

	/**
	 * Get message uid at index.
	 * 
	 * @param index
	 *            message index
	 * @return message uid
	 */
	public long getImapUid(int index) {
		return messages.get(index).getImapUid();
	}

	/**
	 * Get message at index.
	 * 
	 * @param index
	 *            message index
	 * @return message
	 */
	public Message get(int index) {
		return messages.get(index);
	}
}