<%@ page pageEncoding="UTF-8"%>
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

<title>Input Text</title>

<!-- Bootstrap core CSS -->
<link href="inc/bootstrap-3.3.6/dist/css/bootstrap.min.css"
	rel="stylesheet">

<!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->
<link
	href="inc/bootstrap-3.3.6/docs/assets/css/ie10-viewport-bug-workaround.css"
	rel="stylesheet">

<!-- Custom styles for this template -->
<link
	href="inc/bootstrap-3.3.6/docs/examples/starter-template/starter-template.css"
	rel="stylesheet">

<!-- Just for debugging purposes. Don't actually copy these 2 lines! -->
<!--[if lt IE 9]><script src="../../assets/js/ie8-responsive-file-warning.js"></script><![endif]-->
<script
	src="inc/bootstrap-3.3.6/docs/assets/js/ie-emulation-modes-warning.js"></script>


<!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
<!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
<!--[if lt IE 9]>
        <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
        <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->

	<style type="text/css">
		
		textarea{
		  border:1px solid #999999;
		  width:100%;
		  margin:5px 0;
		  padding:3px;
		}
	
	</style>
	<script type="text/javascript">
	//document.getElementById("submitSearch").onclick = submitText;
	</script>
</head>
<body>

	<%@ include file="header.jsp" %>
	
	<div class="container">
		<legend style="text-align: center;">Enter a text and look for its topics!</legend>
		<form method="get" action="input" id="formContent" style="width: 100%;">
			<fieldset>
				<textarea id="unstructuredtext" name="unstructuredtext" rows="10"></textarea>
			</fieldset>
			<p></p>
			<SELECT name="elasticSearchIndex" id="elasticSearchIndex">
				<!--<OPTION VALUE="7clusters">Manually Selected Topics (7)</OPTION>-->
				<!--<OPTION VALUE="swe-topics">news200-f2000-CW (285 topics)</OPTION>-->
				<!--<OPTION VALUE="mwe-topics">Multi word Expressions Topics (5000?)</OPTION>-->
				<OPTION VALUE="news200-2000-cw">news-n200-f2000-CW</OPTION>
				<OPTION VALUE="wiki-200-2000-cw">wiki-n200-f2000-CW</OPTION>
				<OPTION VALUE="news-50-2000-cw">news-n50-f2000-CW</OPTION>
				<OPTION VALUE="wiki-30-0-lm">wiki-n30-f0-LM</OPTION>
			</SELECT>
			<p></p>
			<input type="submit" name="submitTextAndIndex" id="submitTextAndIndex" value="Submit" onclick=""/> <br />
			<p></p><p></p>
			<div id="example_search">
				Or for example, the abstract from the article for 'Jaguar' on Wikipedia with the index: 
				<ul>
				  <!-- <li><a href="<c:url value="example"><c:param name="index" value="7clusters"/></c:url>">7 topics</a></li>-->
				  <!-- <li><a href="<c:url value="example"><c:param name="index" value="swe-topics"/></c:url>">Single Word Expressions Topics</a></li>-->
				  <!-- <li><a href="<c:url value="example"><c:param name="index" value="mwe-topics"/></c:url>">Multi Word Expressions Topics</a></li>-->
				  <li><a href="<c:url value="example"><c:param name="index" value="news200-2000-cw"/></c:url>">news-n200-f2000-CW</a></li>
				  <li><a href="<c:url value="example"><c:param name="index" value="wiki-200-2000-cw"/></c:url>">wiki-n200-f2000-CW</a></li>
				  <li><a href="<c:url value="example"><c:param name="index" value="news-50-2000-cw"/></c:url>">news-n50-f2000-CW</a></li>
				  <li><a href="<c:url value="example"><c:param name="index" value="wiki-30-0-lm"/></c:url>">wiki-n30-f0-LM</a></li>
				</ul>
			</div>

		</form>
		
	</div>
	<!-- /.container -->


	<!-- Bootstrap core JavaScript
    ================================================== -->
	<!-- Placed at the end of the document so the pages load faster -->
	<script
		src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
	<script>window.jQuery || document.write('<script src="../../assets/js/vendor/jquery.min.js"><\/script>')</script>
	<script src="inc/bootstrap-3.3.6/dist/js/bootstrap.min.js"></script>
	<!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->
	<script
		src="inc/bootstrap-3.3.6/docs/assets/js/ie10-viewport-bug-workaround.js"></script>


</body>
</html>