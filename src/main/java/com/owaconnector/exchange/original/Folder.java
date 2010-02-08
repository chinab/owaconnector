package com.owaconnector.exchange.original;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.MoveMethod;
import org.apache.jackrabbit.webdav.client.methods.PropPatchMethod;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.apache.log4j.Logger;
import org.mortbay.util.URIUtil;

import com.owaconnector.exception.DavMailException;
import com.owaconnector.exchange.DavPropertyUtil;
import com.owaconnector.exchange.ExchangeNamespace;

/**
 * Exchange folder with IMAP properties
 */
public class Folder {

	public enum FolderTypes {
		IPF_APPOINTMENT("IPF.Appointment"), IPF_CONTACT("IPF.Contact"), IPF_NOTE("IPF.Note"), IPF_JOURNAL(
				"IPF.Journal"), IPF_TASK("IPF.Task");

		private String type;

		FolderTypes(String type) {
			this.type = type;
		}

		public String getType() {
			return this.type;

		}
	}

	protected static final Logger LOGGER = Logger.getLogger(ExchangeSession.class);
	/**
	 * Logical (IMAP) folder path.
	 */
	private String folderPath;
	/**
	 * Folder unread message count.
	 */
	private int unreadCount;
	/**
	 * true if folder has subfolders (DAV:hassubs).
	 */
	private boolean hasChildren;
	/**
	 * true if folder has no subfolders (DAV:nosubs).
	 */
	private boolean noInferiors;
	/**
	 * Requested folder name
	 */
	private String folderName;
	/**
	 * Folder content tag (to detect folder content changes).
	 */
	private String contenttag;
	/**
	 * Folder message list, empty before loadMessages call.
	 */
	private ExchangeSession exchangeSession;
	private DavGatewayHttpClientFacade facade;

	public Folder(String folderName, ExchangeSession exchangeSession) {
		setFolderName(folderName);
		this.exchangeSession = exchangeSession;
		this.facade = exchangeSession.getFacade();
	}

	public Folder(MultiStatusResponse entity, ExchangeSession exchangeSession)
			throws DavMailException {

		this.exchangeSession = exchangeSession;
		this.facade = exchangeSession.getFacade();
		String href = URIUtil.decodePath(entity.getHref());
		DavPropertySet properties = entity.getProperties(HttpStatus.SC_OK);
		setHasChildren("1".equals(DavPropertyUtil.getPropertyIfExists(properties, "hassubs",
				Namespace.getNamespace("DAV:"))));
		setNoInferiors("1".equals(DavPropertyUtil.getPropertyIfExists(properties, "nosubs",
				Namespace.getNamespace("DAV:"))));
		setUnreadCount(DavPropertyUtil.getIntPropertyIfExists(properties, "unreadcount",
				ExchangeNamespace.URN_SCHEMAS_HTTPMAIL));
		setContenttag(DavPropertyUtil.getPropertyIfExists(properties, "contenttag", Namespace
				.getNamespace("http://schemas.microsoft.com/repl/")));

		// replace well known folder names
		if (href.startsWith(exchangeSession.getInboxUrl())) {
			setFolderPath(href.replaceFirst(exchangeSession.getInboxUrl(), "INBOX"));
		} else if (href.startsWith(exchangeSession.getSentitemsUrl())) {
			setFolderPath(href.replaceFirst(exchangeSession.getSentitemsUrl(), "Sent"));
		} else if (href.startsWith(exchangeSession.getDraftsUrl())) {
			setFolderPath(href.replaceFirst(exchangeSession.getDraftsUrl(), "Drafts"));
		} else if (href.startsWith(exchangeSession.getDeleteditemsUrl())) {
			setFolderPath(href.replaceFirst(exchangeSession.getDeleteditemsUrl(), "Trash"));
		} else {
			int index = href.indexOf(exchangeSession.getMailPath().substring(0,
					exchangeSession.getMailPath().length() - 1));
			if (index >= 0) {
				if (index + exchangeSession.getMailPath().length() > href.length()) {
					setFolderPath("");
				} else {
					setFolderPath(href.substring(index + exchangeSession.getMailPath().length()));
				}
			} else {
				try {
					URI folderURI = new URI(href);
					setFolderPath(folderURI.getPath());
				} catch (URISyntaxException e) {
					throw new DavMailException("EXCEPTION_INVALID_FOLDER_URL", href);
				}
			}
		}
		if (getFolderPath().endsWith("/")) {
			setFolderPath(getFolderPath().substring(0, getFolderPath().length() - 1));
		}
	}

	/**
	 * Get IMAP folder flags.
	 * 
	 * @return folder flags in IMAP format
	 */
	public String getFlags() {
		if (isNoInferiors()) {
			return "\\NoInferiors";
		} else if (isHasChildren()) {
			return "\\HasChildren";
		} else {
			return "\\HasNoChildren";
		}
	}

	private void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
	}

	public boolean isHasChildren() {
		return hasChildren;
	}

	private void setNoInferiors(boolean noInferiors) {
		this.noInferiors = noInferiors;
	}

	public boolean isNoInferiors() {
		return noInferiors;
	}

	private void setUnreadCount(int unreadCount) {
		this.unreadCount = unreadCount;
	}

	public int getUnreadCount() {
		return unreadCount;
	}

	private void setContenttag(String contenttag) {
		this.contenttag = contenttag;
	}

	public String getContenttag() {
		return contenttag;
	}

	private void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}

	public String getFolderPath() {
		return folderPath;
	}

	private void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public String getFolderName() {
		return folderName;
	}

	private static final DavPropertyNameSet CONTENT_TAG = new DavPropertyNameSet();

	static {
		CONTENT_TAG.add(DavPropertyName.create("contenttag", Namespace
				.getNamespace("http://schemas.microsoft.com/repl/")));
	}

	/**
	 * Get folder ctag (change tag). This flag changes whenever folder or folder
	 * content changes
	 * 
	 * @param folderPath
	 *            Exchange folder path
	 * @return folder ctag
	 * @throws IOException
	 *             on error
	 * @throws HttpException
	 */
	public String getFolderCtag(String folderPath) throws IOException, HttpException {
		return getFolderProperty(folderPath, CONTENT_TAG);
	}

	private String getFolderProperty(String folderPath, DavPropertyNameSet davPropertyNameSet)
			throws IOException, HttpException {
		String result;
		MultiStatusResponse[] responses = facade.executePropFindMethod(URIUtil
				.encodePath(folderPath), 0, davPropertyNameSet);
		if (responses.length == 0) {
			throw new DavMailException("EXCEPTION_UNABLE_TO_GET_FOLDER", folderPath);
		}
		DavPropertySet properties = responses[0].getProperties(HttpStatus.SC_OK);
		DavPropertyName davPropertyName = davPropertyNameSet.iterator().nextPropertyName();
		result = DavPropertyUtil.getPropertyIfExists(properties, davPropertyName);
		if (result == null) {
			throw new DavMailException("EXCEPTION_UNABLE_TO_GET_PROPERTY", davPropertyName);
		}
		return result;
	}

	/**
	 * Delete Exchange folder.
	 * 
	 * @param folderName
	 *            logical folder name
	 * @param httpClient
	 * @throws IOException
	 *             on error
	 */
	public void delete() throws IOException {
		facade.executeDeleteMethod(URIUtil.encodePath(getPath()));
	}

	/**
	 * Convert logical or relative folder path to absolute folder path.
	 * 
	 * @param folderName
	 *            folder name
	 * @param exchangeSession
	 * @return folder path
	 */
	public String getPath() {
		String folderPath;
		if (folderName.startsWith("INBOX")) {
			folderPath = folderName.replaceFirst("INBOX", exchangeSession.getInboxUrl());
		} else if (folderName.startsWith("Trash")) {
			folderPath = folderName.replaceFirst("Trash", exchangeSession.getDeleteditemsUrl());
		} else if (folderName.startsWith("Drafts")) {
			folderPath = folderName.replaceFirst("Drafts", exchangeSession.getDraftsUrl());
		} else if (folderName.startsWith("Sent")) {
			folderPath = folderName.replaceFirst("Sent", exchangeSession.getSentitemsUrl());
		} else if (folderName.startsWith("calendar")) {
			folderPath = folderName.replaceFirst("calendar", exchangeSession.getCalendarUrl());
		} else if (folderName.startsWith("public")) {
			folderPath = '/' + folderName;
			// absolute folder path
		} else if (folderName.startsWith("/")) {
			folderPath = folderName;
		} else {
			folderPath = exchangeSession.getMailPath() + folderName;
		}
		return folderPath;
	}

	public static String getPath(String folderName, ExchangeSession exchangeSession) {
		String folderPath;
		if (folderName.startsWith("INBOX")) {
			folderPath = folderName.replaceFirst("INBOX", exchangeSession.getInboxUrl());
		} else if (folderName.startsWith("Trash")) {
			folderPath = folderName.replaceFirst("Trash", exchangeSession.getDeleteditemsUrl());
		} else if (folderName.startsWith("Drafts")) {
			folderPath = folderName.replaceFirst("Drafts", exchangeSession.getDraftsUrl());
		} else if (folderName.startsWith("Sent")) {
			folderPath = folderName.replaceFirst("Sent", exchangeSession.getSentitemsUrl());
		} else if (folderName.startsWith("calendar")) {
			folderPath = folderName.replaceFirst("calendar", exchangeSession.getCalendarUrl());
		} else if (folderName.startsWith("public")) {
			folderPath = '/' + folderName;
			// absolute folder path
		} else if (folderName.startsWith("/")) {
			folderPath = folderName;
		} else {
			folderPath = exchangeSession.getMailPath() + folderName;
		}
		return folderPath;
	}

	/**
	 * Search folders under given folder matching filter.
	 * 
	 * @param folderName
	 *            Exchange folder name
	 * @param filter
	 *            search filter
	 * @param recursive
	 *            deep search if true
	 * @return list of folders
	 * @throws IOException
	 *             on error
	 * @throws HttpException
	 */

	public List<Folder> getSubFolders(Folder parentFolder, String filter, boolean recursive)
			throws IOException, HttpException {
		String mode = recursive ? "DEEP" : "SHALLOW";
		List<Folder> folders = new ArrayList<Folder>();
		StringBuilder searchRequest = new StringBuilder();
		searchRequest
				.append(
						"Select \"DAV:nosubs\", \"DAV:hassubs\", \"DAV:hassubs\","
								+ "\"urn:schemas:httpmail:unreadcount\" FROM Scope('")
				.append(mode)
				.append(" TRAVERSAL OF \"")
				.append(getPath(parentFolder.getPath(), exchangeSession))
				.append("\"')\n" + " WHERE \"DAV:ishidden\" = False AND \"DAV:isfolder\" = True \n");
		if (filter != null && filter.length() > 0) {
			searchRequest.append("                      AND ").append(filter);
		}
		MultiStatusResponse[] responses = this.exchangeSession.getFacade().executeSearchMethod(
				URIUtil.encodePath(getPath(folderName, exchangeSession)), searchRequest.toString());

		for (MultiStatusResponse response : responses) {
			// TODO replace with new Folder()
			folders.add(new Folder(response, exchangeSession));
		}
		return folders;
	}

	/**
	 * Create Exchange calendar folder.
	 * 
	 * @param folderName
	 *            logical folder name
	 * @param httpClient
	 * @throws IOException
	 *             on error
	 * @throws HttpException
	 */
	public void createCalendarFolder() throws IOException, HttpException {
		create(FolderTypes.IPF_APPOINTMENT);
	}

	/**
	 * Create Exchange folder with given folder class.
	 * 
	 * @param folderName
	 *            logical folder name
	 * @param folderClass
	 *            folder class
	 * @param httpClient
	 * @throws IOException
	 *             on error
	 * @throws HttpException
	 */
	public void create(FolderTypes folderClass) throws IOException, HttpException {
		String folderPath = getPath();
		ArrayList<DavProperty> list = new ArrayList<DavProperty>();
		list.add(new DefaultDavProperty(DavPropertyName.create("outlookfolderclass", Namespace
				.getNamespace("http://schemas.microsoft.com/exchange/")), folderClass.getType()));
		// standard MkColMethod does not take properties, override
		// PropPatchMethod instead
		PropPatchMethod method = new PropPatchMethod(URIUtil.encodePath(folderPath), list) {
			@Override
			public String getMethod() {
				return "MKCOL";
			}
		};
		HttpResponse response = this.exchangeSession.getFacade().executeHttpMethod(method);
		int status = response.getStatusLine().getStatusCode();
		// ok or alredy exists
		if (status != HttpStatus.SC_MULTI_STATUS && status != HttpStatus.SC_METHOD_NOT_ALLOWED) {
			throw this.exchangeSession.getFacade().buildHttpException(method, response);
		}
	}

	/**
	 * Move folder to target name.
	 * 
	 * @param folderName
	 *            current folder name/path
	 * @param targetName
	 *            target folder name/path
	 * @param httpClient
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws IOException
	 *             on error
	 */
	public void move(String targetName) throws HttpException, ClientProtocolException, IOException {
		String folderPath = getPath();
		String targetPath = getPath(targetName, exchangeSession);
		MoveMethod method = new MoveMethod(URIUtil.encodePath(folderPath), URIUtil
				.encodePath(targetPath), false);
		facade.executeMoveMethod(method);

	}

}