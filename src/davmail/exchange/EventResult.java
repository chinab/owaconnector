package davmail.exchange;

/**
 * Event result object to hold HTTP status and event etag from an event
 * creation/update.
 */
public class EventResult {
	/**
	 * HTTP status
	 */
	public int status;
	/**
	 * Event etag from response HTTP header
	 */
	public String etag;
}