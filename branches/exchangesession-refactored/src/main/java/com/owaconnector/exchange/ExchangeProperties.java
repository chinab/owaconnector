package com.owaconnector.exchange;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.util.StringUtils;

@Configuration
public class ExchangeProperties {

	@Bean
	@Scope(value = BeanDefinition.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
	public ExchangeProperties exchangeProperties() {
		return new ExchangeProperties();
	}

	private String mailPath;
	private String email;
	private String alias;

	private String inboxUrl;
	private String deleteditemsUrl;
	private String sentitemsUrl;
	private String sendmsgUrl;
	private String draftsUrl;
	private String calendarUrl;
	private String contactsUrl;

	public String getMailPath() {
		return mailPath;
	}

	public void setMailPath(String mailPath) {
		this.mailPath = mailPath;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getInboxUrl() {
		return inboxUrl;
	}

	public void setInboxUrl(String inboxUrl) {
		this.inboxUrl = inboxUrl;
	}

	public String getDeleteditemsUrl() {
		return deleteditemsUrl;
	}

	public void setDeleteditemsUrl(String deleteditemsUrl) {
		this.deleteditemsUrl = deleteditemsUrl;
	}

	public String getSentitemsUrl() {
		return sentitemsUrl;
	}

	public void setSentitemsUrl(String sentitemsUrl) {
		this.sentitemsUrl = sentitemsUrl;
	}

	public String getSendmsgUrl() {
		return sendmsgUrl;
	}

	public void setSendmsgUrl(String sendmsgUrl) {
		this.sendmsgUrl = sendmsgUrl;
	}

	public String getDraftsUrl() {
		return draftsUrl;
	}

	public void setDraftsUrl(String draftsUrl) {
		this.draftsUrl = draftsUrl;
	}

	public String getCalendarUrl() {
		return calendarUrl;
	}

	public void setCalendarUrl(String calendarUrl) {
		this.calendarUrl = calendarUrl;
	}

	public String getContactsUrl() {
		return contactsUrl;
	}

	public void setContactsUrl(String contactsUrl) {
		this.contactsUrl = contactsUrl;
	}

	public String toString() {

		return "ExchangeProperties [Inbox URL : " + this.getInboxUrl() + " Trash URL : "
				+ this.getDeleteditemsUrl() + " Sent URL : " + this.getSentitemsUrl()
				+ " Drafts URL : " + this.getDraftsUrl() + " Calendar URL : "
				+ this.getCalendarUrl() + " Contacts URL : " + this.getContactsUrl() + " ]";
	}

	public boolean validate() {
		return StringUtils.hasText(inboxUrl) && StringUtils.hasText(deleteditemsUrl)
				&& StringUtils.hasText(deleteditemsUrl) && StringUtils.hasText(sentitemsUrl)
				&& StringUtils.hasText(sendmsgUrl) && StringUtils.hasText(draftsUrl)
				&& StringUtils.hasText(calendarUrl) && StringUtils.hasText(contactsUrl);
	}
}
