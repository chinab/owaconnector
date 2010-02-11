package com.owaconnector.exchange.util;

import java.net.URISyntaxException;

import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.mortbay.util.URIUtil;

public class DavPropertyUtil {
	public static String getPropertyIfExists(DavPropertySet properties,
			String name, Namespace namespace) {
		DavProperty property = properties.get(name, namespace);
		if (property == null) {
			return null;
		} else {
			return (String) property.getValue();
		}
	}

	public static String getPropertyIfExists(DavPropertySet properties,
			DavPropertyName davPropertyName) {
		DavProperty property = properties.get(davPropertyName);
		if (property == null) {
			return null;
		} else {
			return (String) property.getValue();
		}
	}

	public static int getIntPropertyIfExists(DavPropertySet properties,
			String name, Namespace namespace) {
		DavProperty property = properties.get(name, namespace);
		if (property == null) {
			return 0;
		} else {
			return Integer.parseInt((String) property.getValue());
		}
	}

	public static long getLongPropertyIfExists(DavPropertySet properties,
			String name, Namespace namespace) {
		DavProperty property = properties.get(name, namespace);
		if (property == null) {
			return 0;
		} else {
			return Long.parseLong((String) property.getValue());
		}
	}

	public static String getURIPropertyIfExists(DavPropertySet properties,
			String name, Namespace namespace) throws URISyntaxException {
		DavProperty property = properties.get(name, namespace);
		if (property == null) {
			return null;
		} else {
			return URIUtil.decodePath((String) property.getValue());
		}
	}

}
