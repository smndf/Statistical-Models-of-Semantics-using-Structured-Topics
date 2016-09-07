<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <meta name="description" content="Simon Dif">
    <meta name="author" content="">
    <link rel="icon" href="inc/bootstrap-3.3.6/docs/favicon.ico">

    <title>Topic</title>

    <!-- Bootstrap core CSS -->
    <link href="inc/bootstrap-3.3.6/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->
    <link href="inc/bootstrap-3.3.6/docs/assets/css/ie10-viewport-bug-workaround.css" rel="stylesheet">

    <!-- Custom styles for this template -->
    <link href="inc/bootstrap-3.3.6/docs/examples/starter-template/starter-template.css" rel="stylesheet">

    <!-- Just for debugging purposes. Don't actually copy these 2 lines! -->
    <!--[if lt IE 9]><script src="../../assets/js/ie8-responsive-file-warning.js"></script><![endif]-->
    <script src="inc/bootstrap-3.3.6/docs/assets/js/ie-emulation-modes-warning.js"></script>


    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
        <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
        <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->

</head>
<body>
<%@ include file="header.jsp" %>

    <div class="container">
						<pre>
	<u>Topic id:</u> <c:out value="${ topic.id }"/>			<a href="visualize?noTopic=${ topic.id }" target="_blank">Graphical view</a>
	
	<u>Topic size:</u> <c:out value="${ topic.size }"/> 
	
	<u>Hypernyms:</u> <c:out value="${ topic.wordNetHypernyms }"/> 
	
	<u>IsA relationships:</u> <c:out value="${ topic.isaRelations }"/>
	
	<u>Topic words by frequency:</u> <c:out value="${ topic.content }"/> 
						</pre>
</div>
</body>
</html>