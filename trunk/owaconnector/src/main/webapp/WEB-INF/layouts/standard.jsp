<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Spring Travel: Spring MVC and Web Flow Reference Application</title>
	<link type="text/css" rel="stylesheet" href="<c:url value="/resources/dijit/themes/tundra/tundra.css" />" />
	<style type="text/css" media="screen">
        @import url("<c:url value="/resources/css-framework/css/tools.css" />");
        @import url("<c:url value="/resources/css-framework/css/typo.css" />");
        @import url("<c:url value="/resources/css-framework/css/forms.css" />");
        @import url("<c:url value="/resources/css-framework/css/layout-navtop-localleft.css" />");
        @import url("<c:url value="/resources/css-framework/css/layout.css" />");
        @import url("<c:url value="/resources/styles/booking.css" />");
    </style>
    <script type="text/javascript" src="<c:url value="/resources/dojo/dojo.js" />"></script>
    <script type="text/javascript" src="<c:url value="/resources/spring/Spring.js" />"></script>
    <script type="text/javascript" src="<c:url value="/resources/spring/Spring-Dojo.js" />"></script> 
</head>
<body class="tundra spring">
<div id="page">
	<div id="header" class="clearfix spring">
		<div id="welcome">
			<div class="left">Spring Travel: Spring MVC and Web Flow Reference Application</div>
		
			</div>
		</div>
		<div id="branding" class="spring">
			<a href="<c:url value="/" />"><img src="<c:url value="/resources/images/header.jpg"/>" alt="Spring Travel" /></a>
		</div>
	</div>
	<div id="content" class="clearfix spring">
		<div id="local" class="spring">
			<a href="http://www.thespringexperience.com">
				<img src="<c:url value="/resources/images/diplomat.jpg"/>" alt="generic hotel" />
			</a>
			<a href="http://www.thespringexperience.com">
				<img src="<c:url value="/resources/images/tse.gif"/>" alt="The Spring Experience" />
			</a>
			<p>
			</p>
		</div>
		<div id="main">
			<tiles:insertAttribute name="body" />
		</div>
	</div>
	<div id="footer" class="clearfix spring">
		<a href="http://www.springframework.org">
			<img src="<c:url value="/resources/images/powered-by-spring.png"/>" alt="Powered by Spring" />
		</a>
	</div>
</div>
</body>
</html>