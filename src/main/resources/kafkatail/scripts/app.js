angular.module("kafkatail")
	.controller("test", ["$scope", "socket",
						 function($scope, socket) {
							 $scope.topics = {};
							 socket.create("ws://localhost:5679/",
										  function(e) {
											  $scope.$apply(function() {
												  var msg = e.data.split("|");
												  var topic = msg[0];
												  var m = msg[1];
												  if($scope.topics[topic]) {
													  $scope.topics[topic].msg.push(m);
												  }
												  });
										  });

							 $scope.addTopic = function(topic) {
								 console.log("add",topic);
								 $scope.topics[topic] = {name: topic, msg: []};
								 socket.send("add|"+topic);
							 };
						 }]);
