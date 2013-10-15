angular.module("kafkatail")
	.controller("test", ["$scope", "socket",
						 function($scope, socket) {
							 $scope.topics = {};
							 socket.create("ws://localhost:5679/",
										  function(e) {
											  $scope.$apply(function() {
												  var msg = e.data.split("|");
												  var topic = msg[0];
												  var m = {timestamp: parseInt(msg[1]),
														   data: msg[2]};
												  if($scope.topics[topic]) {
													  var rest =  _.last($scope.topics[topic].msg, 50);
													  rest.push(m);
													  $scope.topics[topic].msg = rest;
												  }
											  });
										  });

							 $scope.removeTopic = function(topic) {
								 console.log("remove", topic);
								 delete $scope.topics[topic];
								 socket.send("remove|"+topic);
							 };

							 $scope.addTopic = function(topic) {
								 console.log("add",topic);
								 $scope.topics[topic] = {name: topic, msg: []};
								 socket.send("add|"+topic);
							 };
						 }]);
