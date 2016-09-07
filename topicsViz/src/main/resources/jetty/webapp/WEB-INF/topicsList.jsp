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

    <title>Topics</title>

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

       
       <script type="text/javascript">			
			
			function onSubmitForm(){
				var topicListDiv = document.getElementById('topicListDivId');
				var esIndex = document.getElementById("elasticSearchIndex").value;
				//alert(esIndex);
				window.location.href = window.location.href.split("?")[0] +"?elasticSearchIndex="+esIndex;
				/*
				$.post(window.location.href, "elasticSearchIndex="+esIndex, function(response) {
		    		response = response.split("topicListDivId")[response.split("topicListDivId").length-1];
		    		response = response.substring(response.split(">")[0].length +1);
		    		response = response.split("</div>")[0];
		    		topicListDiv.innerHTML=response; 
		    		alert(response);
		    	});
				*/
				topicListDiv.style.display = "" ;
				//window.location.reload();
				//alert("onSubmitForm() finished");
			}
	
		</script>
        
    </head>
    <body>

	<%@ include file="header.jsp" %>

    <div class="container">

			<legend style="text-align: center;">Browse all topics </legend>
			<div style="width: 40%; margin: 0 auto;">
			<form id="my-form" method="get">
				<div style="float:left;">
					Select a model:&nbsp
           	<SELECT name="elasticSearchIndex" id="elasticSearchIndex">
				<OPTION VALUE="swe-topics">Single word Expressions Topics (385?)</OPTION>
				<OPTION VALUE="mwe-topics">Multi word Expressions Topics (5000?)</OPTION>
			</SELECT><br>
				</div>
			<br>
			<div style="float:left;">
				Sorted by:&nbsp
			</div>
			<div>
			<SELECT name="criteria" id="criteria">
				<OPTION VALUE="score">Score</OPTION>
				<OPTION VALUE="size">Size</OPTION>
				<OPTION VALUE="id">Id</OPTION>
			</SELECT>
			<SELECT name="direction" id="direction">
				<OPTION VALUE="plus">+ to -</OPTION>
				<OPTION VALUE="minus">- to +</OPTION>
			</SELECT>
			&nbsp&nbsp
			<input type="submit" id="submitForm" value="OK" class="submitText" onclick="return onSubmitForm();" style=""/> 
			</div>
			</form>
			</div>
			<br>
            
			<div id="topicListDivId" style="">
				<p></p>
	            <c:forEach items="${topics}" var="topic"> 
						<pre>
	<u>Topic id:</u> <c:out value="${ topic.id }"/>			<a href="topic?noTopic=${ topic.id }" target="_blank">Full topic</a>  <a href="visualize?noTopic=${ topic.id }" target="_blank">Graphical view</a>
	
	<u>Topic size:</u> <c:out value="${ topic.size }"/> 
	
	<u>50 topic words by frequency:</u> <c:out value="${ topic.fiftyFirstWords }"/> 
	
	<u>Hypernyms:</u> <c:out value="${ topic.wordNetHypernyms }"/> 
	
	<u>IsA relationships:</u> <c:out value="${ topic.isaRelations }"/>
						</pre>
				</c:forEach>
			</div>
		</div>


	</div><!-- /.container -->


    <!-- Bootstrap core JavaScript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
    <script>window.jQuery || document.write('<script src="../../assets/js/vendor/jquery.min.js"><\/script>')</script>
    <script src="inc/bootstrap-3.3.6/dist/js/bootstrap.min.js"></script>
    <!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->
    <script src="inc/bootstrap-3.3.6/docs/assets/js/ie10-viewport-bug-workaround.js"></script>

    
      </body>
</html>
