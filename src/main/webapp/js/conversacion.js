class Conversacion {
	
	constructor(ko, interlocutor, sender, websocket) {
		this.interlocutor = interlocutor;
		this.sender = sender;
		this.mensajes = ko.observableArray([]);
		this.websocket = websocket;
		this.textoAEnviar = ko.observable("");
		this.visible = ko.observable(true);
		this.shown = false;
		this.textosCargados = [];
	}
	
	enviar() {
		var mensaje = {
			type : "PARTICULAR",				
			recipient : this.interlocutor,
			message : this.textoAEnviar()
		};
		
		this.websocket.send(JSON.stringify(mensaje)); //enviamos el mensaje a través del WS.
		
		/* Creamos un objeto mensaje para añadirlo al HTML */
		var msg = new Mensaje(this.textoAEnviar());
		if(this.sender != this.interlocutor){
			this.addMessage(msg, mensaje.recipient==this.interlocutor);
		}
		
		/* Limpiamos el contenido del cuadro de texto donde escribimos los mensajes privados */
		document.getElementById("private").value = "";
	}
	
	/* Solicitamos los mensajes privados al recurso correspondiente a través de una petición POST */
	getMensajes(){
		/* Añadimos una condición para solo pedir los mensajes una vez (cuando se crea la Conversacion) */
		if(!this.shown){
			this.shown = true;
			var self = this;
			var info = {
					recipient : this.interlocutor,
					sender : this.sender
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
	}
	
	/* Recorremos los arrays de textos y emisores para añadir los mensajes */
	setMensajes(textosFinales, senders){
		for (var i=0; i<textosFinales.length; i++) {
			var msg = new Mensaje(textosFinales[i]);
			this.mensajes.push(senders[i] + ": " + msg.texto);
		}		
	}
	
	/* Añadimos los mensajes a la conversación con el formato: 'nombre: mensaje' */
	addMessage(mensaje, emisor) {
		/* La variable emisor es un booleano mediante el cual comprobamos si debemos coger el sender o el interlocutor */
		if (emisor) {
			this.mensajes.push(this.sender + ": " + mensaje.texto);
		} else{
			this.mensajes.push(this.interlocutor + ": " + mensaje.texto);
		}
	}
}