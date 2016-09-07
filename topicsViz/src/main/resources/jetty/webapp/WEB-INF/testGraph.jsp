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
		#zoomButtons {
			position:absolute;
			z-index: 10;
		}
	
        #mynetwork {
            width: 100%;
            height: 600px;
            border: 1px solid lightgray;
        }
        #loadingBar {
            position:absolute;
            top:0px;
            z-index: 20;
            left:0px;
            width: 100%;
            height: 802px;
            background-color:rgba(200,200,200,0.8);
            -webkit-transition: all 0.5s ease;
            -moz-transition: all 0.5s ease;
            -ms-transition: all 0.5s ease;
            -o-transition: all 0.5s ease;
            transition: all 0.5s ease;
            opacity:1;
        }
        #wrapper {
            position:relative;
            width:1100px;
            height:600px;
        }

        #text {
            position:absolute;
            top:1px;
            left:530px;
            width:30px;
            height:50px;
            margin:auto auto auto auto;
            font-size:22px;
            color: #000000;
        }


        div.outerBorder {
            position:relative;
            top:270px;
            width:600px;
            height:44px;
            margin:auto auto auto auto;
            border:8px solid rgba(0,0,0,0.1);
            background: rgb(252,252,252); /* Old browsers */
            background: -moz-linear-gradient(top,  rgba(252,252,252,1) 0%, rgba(237,237,237,1) 100%); /* FF3.6+ */
            background: -webkit-gradient(linear, left top, left bottom, color-stop(0%,rgba(252,252,252,1)), color-stop(100%,rgba(237,237,237,1))); /* Chrome,Safari4+ */
            background: -webkit-linear-gradient(top,  rgba(252,252,252,1) 0%,rgba(237,237,237,1) 100%); /* Chrome10+,Safari5.1+ */
            background: -o-linear-gradient(top,  rgba(252,252,252,1) 0%,rgba(237,237,237,1) 100%); /* Opera 11.10+ */
            background: -ms-linear-gradient(top,  rgba(252,252,252,1) 0%,rgba(237,237,237,1) 100%); /* IE10+ */
            background: linear-gradient(to bottom,  rgba(252,252,252,1) 0%,rgba(237,237,237,1) 100%); /* W3C */
            filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#fcfcfc', endColorstr='#ededed',GradientType=0 ); /* IE6-9 */
            border-radius:72px;
            box-shadow: 0px 0px 10px rgba(0,0,0,0.2);
        }

        #border {
            position:absolute;
            top:5px;
            left:10px;
            width:500px;
            height:23px;
            margin:auto auto auto auto;
            box-shadow: 0px 0px 4px rgba(0,0,0,0.2);
            border-radius:10px;
        }

        #bar {
            position:absolute;
            top:0px;
            left:0px;
            width:20px;
            height:20px;
            margin:auto auto auto auto;
            border-radius:11px;
            border:2px solid rgba(30,30,30,0.05);
            background: rgb(0, 173, 246); /* Old browsers */
            box-shadow: 2px 0px 4px rgba(0,0,0,0.4);
        }
</style>

<script type="text/javascript">
		function draw() {
			var nodesDataSet = new vis.DataSet([]);
			var nodesJson = ${topicgraph.nodes};
			var positionsStored = ${positionsStored};

			for (var i = 0; i < nodesJson.length; i++) {
				var node = nodesJson[i];
				//alert("add node "+JSON.stringify(node));
				var nodeId = node["id"];
				var nodeLabel = node["label"];
				var nodeValue = node["value"];
				if (nodesJson.length > 3) {
					var nodeImage = node["image"];
				} else {
					var nodeImage = "http://serelex.org/image/"
							+ nodeLabel.toLowercase();
				}

				if (positionsStored) {
					var nodeX = node["x"];
					var nodeY = node["y"];
					nodesDataSet.add({
						'id' : nodeId,
						'label' : nodeLabel,
						'value' : nodeValue,
						'image' : nodeImage,
						'shape' : "image",
						'physics' : false,
						'x' : nodeX,
						'y' : nodeY
					/*,
						'fixed' : {
							'x' : true,
							'y' : true
						} */
					});
				} else {
					nodesDataSet.add({
						'id' : nodeId,
						'label' : nodeLabel,
						'value' : nodeValue,
						'image' : nodeImage,
						'shape' : "image",
						'physics' : true
					});
				}
				//alert("add node "+node[0]+" "+node[1]+" "+nodeValue);

			}

			var edgesDataSet = new vis.DataSet([]);
			var edgesJson = ${topicgraph.edges};
			for (var i = 0; i < edgesJson.length; i++) {
				var edge = edgesJson[i];
				var edgeFrom = edge["from"];
				var edgeTo = edge["to"];
				edgesDataSet.add({
					from : edgeFrom,
					to : edgeTo
				});
			}

			// create a network
			var container = document.getElementById('mynetwork');
			var data = {
				nodes : nodesDataSet,
				edges : edgesDataSet
			};
			// disable physics if positions loaded (previously stored)
			var options;
			if (positionsStored) {
				options = {
					nodes : {
						color : {
							border : '#222222',
							background : '#666666'
						},
						font : {
							size : 24,
							color : '#000000'
						}
					},
					edges : {
						color : {
							color : 'lightgray',
							highlight : 'black',
							hover : 'black'
						},
						smooth : false
					},
					layout : {
						randomSeed : 8
					},
					interaction : {
						hover : false,
						hoverConnectedEdges : false
					},
					physics: {
	                    forceAtlas2Based: {
	                        gravitationalConstant: -26,
	                        centralGravity: 0.005,
	                        springLength: 230,
	                        springConstant: 0.18
	                    },
	                    maxVelocity: 146,
	                    solver: 'forceAtlas2Based',
	                    timestep: 0.35,
	                    stabilization: {
	                        enabled:true,
	                        iterations:2000,
	                        updateInterval:25
	                    }
	                }
				};
			} else {
				options = {
					nodes : {
						color : {
							border : '#222222',
							background : '#666666'
						},
						font : {
							size : 24,
							color : '#000000'
						}
					},
					edges : {
						color : {
							color : 'lightgray',
							highlight : 'black',
							hover : 'black'
						},
						smooth : false
					},
					layout : {
						randomSeed : 8
					},
					interaction : {
						hover : false,
						hoverConnectedEdges : false
					},
					physics: {
	                    forceAtlas2Based: {
	                        gravitationalConstant: -100,
	                        centralGravity: 0.003,
	                        springLength: 230,
	                        springConstant: 0.18,
	                        damping : 0.2
	                    },
	                    //maxVelocity: 146,
	                    solver: 'forceAtlas2Based',
	                    //timestep: 0.35,
	                    stabilization: {
	                        enabled:true,
	                        iterations:2000,
	                        updateInterval:25
	                    }
	                }
					/*
					physics : {
						enabled : true,
						forceAtlas2Based : {
							gravitationalConstant : -150,
							centralGravity : 0.02,
							springConstant : 0.05,
							springLength : 100,
							damping : 2.0,
							avoidOverlap : 0
						},
						solver : 'forceAtlas2Based',
						stabilization: {
	                        enabled:true,
	                        iterations:2000,
	                        updateInterval:25
	                    }
					}*/
					
				};
			}
			var network = new vis.Network(container, data, options);
			/*if (positionsStored){
				network.stopSimulation();
			}*/
			document.getElementById("storeBtn").onclick = storePositions1;

			document.getElementById("resetBtn").onclick = resetPositions1;
						
			document.getElementById("zoomIn").onclick = zoomInFunction;
			
			document.getElementById("zoomOut").onclick = zoomOutFunction;

			document.getElementById("layoutSettings").onclick = layoutSettingsClick;

			if (positionsStored) {
				physicsEnabled = false;
				// remove loading bar
				document.getElementById('text').innerHTML = '100%';
	            document.getElementById('bar').style.width = '496px';
	            document.getElementById('loadingBar').style.opacity = 0;
	            network.fit();
	            // really clean the dom element
	            setTimeout(function () {document.getElementById('loadingBar').style.display = 'none';}, 500);
			}
			network.on("stabilizationProgress", function(params) {
                var maxWidth = 496;
                var minWidth = 20;
                var widthFactor = params.iterations/params.total;
                var width = Math.max(minWidth,maxWidth * widthFactor);

                document.getElementById('bar').style.width = width + 'px';
                document.getElementById('text').innerHTML = Math.round(widthFactor*100) + '%';
            });
            network.once("stabilizationIterationsDone", function() {
                document.getElementById('text').innerHTML = '100%';
                document.getElementById('bar').style.width = '496px';
                document.getElementById('loadingBar').style.opacity = 0;
                network.stopSimulation();
                // really clean the dom element
                setTimeout(function () {document.getElementById('loadingBar').style.display = 'none';}, 500);
            });
			
			function storePositions1() {
				network.setOptions({
					physics: {barnesHut: {gravitationalConstant: 0,
						centralGravity: 0, springConstant: 0}}
				});
				network.storePositions();
				//{fields: ['id', 'date', 'group']}
				var nodesToSend = JSON.stringify(nodesDataSet.get());
				$.post("/demoApp/visualize?action=store", window.location.href
						.split("noTopic=")[1]
						+ "#" + nodesToSend, function(
						response) {
					// handle response from servlet.
				});
			}

			function resetPositions1() {
				$.post("/demoApp/visualize?action=reset", window.location.href
						.split("noTopic=")[1], function(response) {
					// handle response from servlet.
				});
				window.location.reload();
			}
			
			function zoomInFunction() {
				var newZoomLevel = network.getScale() * 1.1;
				var zoomOptions = {
						scale: newZoomLevel
				};
				network.moveTo(zoomOptions);
			}
			
			function zoomOutFunction() {
				var newZoomLevel = network.getScale() / 1.1;
				var zoomOptions = {
						scale: newZoomLevel
				};
				network.moveTo(zoomOptions);
			}
			
			function layoutSettingsClick() {
				document.getElementById('storeButtons').style.display = '';
				document.getElementById('layoutSettings').style.display = 'none';
			}
		}
		</script>

</head>
<body onload="draw()">

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

		<div id="zoomButtons">
		<button id="zoomOut">Zoom -</button>
		<button id="zoomIn">Zoom +</button>
		</div>
		<div id="mynetwork"></div>
		<div id="loadingBar">
        <div class="outerBorder">
            <div id="text">0%</div>
	            <div id="border">
	                <div id="bar"></div>
	            </div>
	        </div>
    	</div>
    	<button id="layoutSettings">Layout settings</button>
		<div id="storeButtons" style="display: none">
		<button id="storeBtn">Store Positions</button>
		<button id="resetBtn">Reset Positions</button>
		</div>


	</div>
	<!-- /.container -->


	<!-- Bootstrap core JavaScript
    ================================================== -->
	<!-- Placed at the end of the document so the pages load faster -->
	<script
		src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
	<script>
		window.jQuery
				|| document
						.write('<script src="../../assets/js/vendor/jquery.min.js"><\/script>')
	</script>
	<script src="inc/bootstrap-3.3.6/dist/js/bootstrap.min.js"></script>
	<!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->
	<script
		src="inc/bootstrap-3.3.6/docs/assets/js/ie10-viewport-bug-workaround.js"></script>

</body>
</html>