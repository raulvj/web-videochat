class Chat {
	constructor(ko, app) {
		let self = this;
		this.ko = ko;
		this.app = app;
		this.estado = ko.observable("No conectado");
		this.error = ko.observable("");

		this.usuarios = ko.observableArray([]);
		this.conversaciones = ko.observableArray([]);
		this.mensajesRecibidos = ko.observableArray([]);
		this.mensajeQueVoyAEnviar = ko.observable("");
		
		this.recipient = ko.observable("personal");
		
		this.getMensajes();
		
		this.chat = new WebSocket("wss://" + window.location.host + "/wsTexto");
		
		this.chat.onopen = function() {
			self.estado("Conectado al chat de texto");
			self.error("");
			
			if (!self.app.user) {
			    console.info( "This page have been reloaded" );
			    app.router.go( { path : 'login2' } ); //para ir a Login
			}
		}

		this.chat.onerror = function() {
			self.estado("");
			self.error("Chat de texto cerrado");
		}

		this.chat.onclose = function() {
			self.estado("WebSocket cerrado");
			self.error("Chat de texto cerrado");
		}
		
		this.chat.onmessage = function(event) {
			var data = JSON.parse(event.data);
			
			if (data.type == "FOR ALL") {
				var mensaje = new Mensaje(data.message, data.time);
				self.mensajesRecibidos.push(data.sender + ":  " + mensaje.texto); //añadimos el mensaje al Chat general
				document.getElementById("public").value = ""; //limpiamos el cuadro de texto donde escribimos los mensajes
				
			} else if (data.type == "ARRIVAL") {
				var userName = data.user;
				var userPic = data.picture;
				/* Recorremos los usuarios. Si lo encontramos no hacemos nada, y sino lo añadimos. */
                for (var i=0; i<self.usuarios().length; i++) {
                	if (self.usuarios()[i].name == userName) {
                		break
                	}else if (i == self.usuarios().length - 1) {
                		var response = {
                			name : userName,
                			picture : userPic
                		};
                		self.usuarios.push(new Usuario(response.name, response.picture));
                		break
                	}
                }
                
			} else if (data.type == "BYE") {
				var userName = data.userName;
				/* Recorremos los usuarios, si lo encontramos, tenemos que quitarlo de la lista. */
				for (var i=0; i<self.usuarios().length; i++) {
					if (self.usuarios()[i].name == userName) {
						self.usuarios.splice(i, 1);
						break;
					}
				}
				
			} else if (data.type == "PARTICULAR") {
				var conversacionActual = self.buscarConversacion(data.remitente);
				/* Si existe la conversacion añadimos el mensaje, y sino, creamos dicha conversacion */
				if (conversacionActual!=null) {
					var mensaje = new Mensaje(data.message.texto, data.message.hora);
					conversacionActual.addMessage(mensaje, false);
				} else {
					conversacionActual = new Conversacion(self.ko, data.remitente, self.app.user().name, self.chat);
					var mensaje = new Mensaje(data.message.texto, data.message.hora);
					conversacionActual.addMessage(mensaje, false);
					self.conversaciones.push(conversacionActual); //añadimos la conversacion a la lista de conversaciones para buscarla más adelante
				}
				/* Cuando se recibe un mensaje del tipo PARTICULAR, mostramos la conversacion con el usuario que la ha generado */
				self.mostrarConversacion(data.remitente);
			} 
		}
	}
	
	close() {
		console.log("Cerrando websocket");
		this.chat.close();
	}
		
	enviarATodos(mensaje) {
		var mensaje = {
			type : "BROADCAST",
			message : this.mensajeQueVoyAEnviar()
		};
		this.chat.send(JSON.stringify(mensaje));
	}
	
	/* Al seleccionar un receptor, creamos su conversacion si es que esta no existía ya */
	setRecipient(interlocutor) {
		this.recipient(interlocutor.name);
		var conversacion = this.buscarConversacion(interlocutor.name);
		if (conversacion == null){
			conversacion = new Conversacion(this.ko, interlocutor.name, this.app.user().name, this.chat);
			this.conversaciones.push(conversacion);
		}
		this.mostrarConversacion(interlocutor.name);
	}
		
	/* NOTA: equivalente hacer: self.buscarConversacion = function(interlocutor) { } */
	buscarConversacion(interlocutor) {
		for (var i=0; i<this.conversaciones().length; i++) {
			if (this.conversaciones()[i].interlocutor == interlocutor)
				return this.conversaciones()[i];
		}
		return null;
	}
		
	mostrarConversacion(interlocutor, shown) {
		for (var i=0; i<this.conversaciones().length; i++){
			var conversacion = this.conversaciones()[i];
			conversacion.visible(conversacion.interlocutor == interlocutor); //mostramos la conversacion en funcion de un booleano.
			/* La primera vez que ejecutemos el mostrarConversacion deberemos cargar los mensajes anteriores que ésta tuviera */
			conversacion.getMensajes(conversacion);
		}
	}
	
	/* Solicitamos los mensajes generales al recurso correspondiente a través de una petición POST */
	getMensajes(){
		var self = this;
		var info = {
			recipient : "", //en el chat general, los mensajes no tienen receptores ""
			sender : "", //por continuar con el mismo formato que mensajes privados, mandaremos también el sender vacío, aunque solo comprobaremos el recipient en el server.
		};
		var data = {
			data : JSON.stringify(info),
			url : "messages/getMensajes",
			type : "post",
			contentType : 'application/json',
			textos : [],
			senders: [],
			success : function(response) {
				for (var i=0; i<response.length; i++) {
					this.textos[i]=response[i].message;
					this.senders[i]=response[i].sender;
				}
				self.setMensajes(this.textos, this.senders);
			},
			error : function(response) {
				this.error(response.responseJSON.error);
			}
				
		};
		$.ajax(data);
	}
	
	/* Recorremos los arrays de textos y emisores para añadir los mensajes */
	setMensajes(textosFinales, senders){
		for (var i=0; i<textosFinales.length; i++) {
			var msg = new Mensaje(textosFinales[i]);
			this.mensajesRecibidos.push(senders[i] + ": " + msg.texto);
		}
	}
	
	setPhoto(userName, photo){
		for (var i=0; i<this.usuarios().length; i++) {
			if (this.usuarios()[i].name == userName) {
				this.usuarios.replace(this.usuarios()[i],new Usuario(userName, photo) );
			}
		}
	}
	
	addUsuario(userName, picture) {
		this.usuarios.push(new Usuario(userName, picture));
	}
}