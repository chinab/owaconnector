# Project description #
Owaconnector is a web application that enables users to register an Outlook Web Access URL and their credentials to obtain their calendar in iCal format. This calendar can be obtained by a generated URL. It can be used in Google Calendar or in iCal to provide read only access to Outlook Web Access calendars.

# Technology #

This project is based on Spring Roo with Maven 2 for dependency support.
The goal of this project is to host an application on stax.net (based on Amazon EC2 cloud) that serves different users and their specific calendars.

Technologies used:
  * Spring Roo (Spring Framework 3, Apache Tiles, Spring Security)
  * BountyCastle for encrytion

Other projects:
  * Jackrabbit-webdav (converted to HttpClient 4.0)
  * DavMail, see http://sourceforge.net/projects/davmail/, rewritten as a library.