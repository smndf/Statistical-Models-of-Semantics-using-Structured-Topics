<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
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

<title>Topic Visualisation</title>

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



<script type="text/javascript" src="inc/vis.js"></script>
<link href="inc/vis.css" rel="stylesheet" type="text/css" />

<style type="text/css">
#mynetwork {
	width: 1150px;
	height: 650px;
	border: 1px solid lightgray;
}
</style>
</head>
<body>

	<%@ include file="header.jsp"%>

	<div class="container">

		<p />
		<p />

		<div id="topic_choice" style="visibility: hidden">
			<form method="post" action="choice" id='content' style='width: 100%;'>
				<SELECT name="topic" id="topic">
					<OPTION VALUE="586">Fishes</OPTION>
					<OPTION VALUE="301">Drugs</OPTION>
					<OPTION VALUE="396">Cyclists</OPTION>
					<OPTION VALUE="183">Dogs</OPTION>
					<OPTION VALUE="431">Eastern Europe & Russia</OPTION>
					<OPTION VALUE="452">Wine</OPTION>
					<OPTION VALUE="41">Navigators</OPTION>
					<OPTION VALUE="588">Theater</OPTION>
				</SELECT> <input type="submit" value="Choose" class="sansLabel" /> <br />
			</form>
		</div>

		
		<button id="storeBtn">store positions</button> <button id="resetBtn">Reset Positions</button>
		<div id="mynetwork"></div>
		<script type="text/javascript">
	
			// create an array with nodes
			var nodes = ${topicgraph.nodes};
			
			// create an array with edges
			var edges = ${topicgraph.edges};
			
			// create a network
			var container = document.getElementById('mynetwork');
			var data = {
			nodes: nodes,
			edges: edges
			};
			var options = {
				nodes: {
					color: {
						border: '#222222',
						background: '#666666'
					},
					font:{
						size: 24,
						color:'#000000'
					}
				},
				edges: {
					color: {
						color: 'lightgray',
						highlight: 'black',
						hover: 'black'
					},
					smooth: false
				},
				layout: {randomSeed:8},
				interaction: {
					hover: false,
					hoverConnectedEdges: false
				},
				physics:{
				    enabled: false,
				    forceAtlas2Based: {
				      gravitationalConstant: -150,
				      centralGravity: 0.02,
				      springConstant: 0.05,
				      springLength: 100,
				      damping: 2.0,
				      avoidOverlap: 0
				    },   
				    solver: 'forceAtlas2Based',
				    stabilization: true
				  }
			};
			var network = new vis.Network(container, data, options);
			
			document.getElementById("storeBtn").onclick = storePositions2;
			
			document.getElementById("resetBtn").onclick = resetPositions2;
			
			function storePositions2() {
			    network.storePositions();
			    var nodesToSend = nodes.get();
			    var edgesToSent = edges.get();
			    $.post("/demoApp/visualizePreloaded?action=store", window.location.href.split("noTopic=")[1]+"#"+JSON.stringify(nodes)+"#"+JSON.stringify(edges), function(response) {
			        // handle response from servlet.
			    });
			}
			
			function resetPositions2() {
			    $.post("/demoApp/visualizePreloaded?action=reset", window.location.href.split("noTopic=")[1] /*window.location.href.split("noTopic=")[1]*/, function(response) {
			        // handle response from servlet.
			    });
			    //window.location.reload();
			}
			
			//network.on("stabilized", function () {network.storePositions();});
		</script>


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