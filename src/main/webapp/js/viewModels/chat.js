/**
 * @license
 * Copyright (c) 2014, 2020, Oracle and/or its affiliates.
 * Licensed under The Universal Permissive License (UPL), Version 1.0
 * as shown at https://oss.oracle.com/licenses/upl/
 * @ignore
 */
/*
 * Your incidents ViewModel code goes here
 */
define(['knockout', 'appController', 'ojs/ojmodule-element-utils', 'accUtils'],
		function(ko, app, moduleUtils, accUtils) {

	function ChatViewModel() {
		var self = this;
		
		this.user = app.user;
		
		self.recipient = ko.observable("personal");
		self.chat = ko.observable(new Chat(ko, app));
		self.videochat = ko.observable(new VideoChat(ko));
		
		self.estadoChatDeTexto = self.chat().estado;
		self.estadoSignaling = self.videochat().estado;
		self.errorChatDeTexto = self.chat().error;
		self.errorSignaling = self.videochat().error;
		
		// Header Config
		self.headerConfig = ko.observable({'view':[], 'viewModel':null});
		moduleUtils.createView({'viewPath':'views/header.html'}).then(function(view) {
			self.headerConfig({'view':view, 'viewModel': app.getHeaderModel()})
		})
		
		self.connected = function() {
			accUtils.announce('Chat page loaded.');
			document.title = "Chat";
			
			/* Solicitamos al recurso mediante una petición GET la lista de usuarios conectados al Videochat */
			//getUsuariosConectados();
			getUsuariosConectadosAsync();
		};
		
		function getUsuariosConectados() {
			var data = {	
					url : "users/getUsuariosConectados",
					type : "get",
					contentType : 'application/json',
					success : function(response) {
						for (var i=0; i<response.length; i++) {
							var userName = response[i].name;
							var picture = response[i].picture;
							self.chat().addUsuario(userName, picture);
							//self.usuarios.push(new Usuario(response[i].name, response[i].picture));
						}
					},
					error : function(response) {
						self.error(response.responseJSON.error);
					}
			};
			$.ajax(data);
		}
		
		
		function getUsuariosConectadosAsync() {
			var data = {	
					url : "users/getUsuariosConectadosAsync",
					type : "get",
					contentType : 'application/json',
					success : function(response) {
						for (var i=0; i<response.length; i++) {
							var userName = response[i];
							var user = new Usuario(userName, ko.observable(null));
							self.chat().addUsuario(user.name, user.picture);
							getPicturesAsync(user);
						}
					},
					error : function(response) {
						self.error(response.responseJSON.error);
					}
			};
			$.ajax(data);
		}
		
		
		function getPicturesAsync(user) {
			var info = {
					username: user.name
			};
			var data = {	
					data : JSON.stringify(info),
					url : "users/getPicturesAsync",
					type : "post",
					contentType : 'application/json',
					success : function(response) {
						user.picture(response[0]);
					},
					error : function(response) {
						self.error(response.responseJSON.error);
					}
			};
			$.ajax(data);
		}
		
		self.llamar = function(destinatario) {
			self.videochat().llamar(destinatario.name);
		}
		
		self.disconnected = function() {
			alert("Cerrando la conexion");
			//self.chat.close;
			self.chat().close();
		};

		self.transitionCompleted = function() {
			// Implement if needed
		};
	
	}

	return ChatViewModel;
}
);
