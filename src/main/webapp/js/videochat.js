class VideoChat {
	constructor(ko) {
		let self = this;
		this.ko = ko;
		
		this.videoLocalOn = false;
		
		this.mensajes = ko.observableArray([]);
		
		this.estado = ko.observable("No conectado");
		this.error = ko.observable();
		
		this.ws = new WebSocket("wss://" + window.location.host + "/wsSignaling");
		
		this.ws.onopen = function() {
			self.estado("Conectado al servidor de signaling");
			self.error("");
			self.addMensaje("Conectado al servidor de signaling", "green");
		}
		
		this.ws.onerror = function() {
			self.estado("");
			self.error("Desconectado del servidor de signaling");
			self.addMensaje("Desconectado del servidor de signaling", "red");
		}
		
		this.ws.onclose = function() {
			self.estado("");
			self.error("Desconectado del servidor de signaling");
			self.addMensaje("Desconectado del servidor de signaling", "red");
		}

		this.ws.onmessage = function(event) {
			var data = JSON.parse(event.data);
			
			if (data.type=="OFFER") {
				self.anunciarLlamada(data.remitente, data.sessionDescription);
				return;
			}
			
			if (data.type=="CANDIDATE" && data.candidate) {
				try {
					self.conexion.addIceCandidate(data.candidate);
				} catch (error) {
					//self.addMensaje("No se pudo establecer la llamada", "red");
				}
				return;
			}
			
			if (data.type=="ANSWER") {
				let sessionDescription = data.sessionDescription;
				let rtcSessionDescription = new RTCSessionDescription(sessionDescription);
				self.conexion.setRemoteDescription(rtcSessionDescription);
				self.addMensaje("La llamada fue aceptada", "green");
				return;
			}
			
			if (data.type=="CANCEL") {
				alert("La llamada ha sido rechazada por el usuario.");
				self.addMensaje("La llamada fue rechazada", "red");
				return;
			}
		}
	}
	
	/* Anunciamos la llamada. El usuario decide si aceptarla o rechazarla */
	anunciarLlamada(remitente, sessionDescription) {
		this.addMensaje("Se recibe llamada de " + remitente + ".", "black");
		
		window.focus(); //NOTA: el window.focus() no funciona correctamente en Chrome
		let aceptar = confirm("Te llama " + remitente + ". ¿Contestar?\n");
		
		if (aceptar) {
			this.aceptarLlamada(remitente, sessionDescription);
		} else {
			this.rechazarLlamada(remitente, sessionDescription);
		}
	}
	
	rechazarLlamada(remitente, sessionDescription) {
		this.addMensaje("Llamada de " + remitente + " rechazada", "red");
		let msg = {
			type : "CANCEL",
			sessionDescription : sessionDescription
		};
		/* Enviamos el mensaje de CANCELACIÓN de llamada al servidor WS de Signaling*/
		this.ws.send(JSON.stringify(msg));
	}
	
	aceptarLlamada(remitente, sessionDescription) {
		/* Si el video local no está encendido lo encendemos */
		if (!this.videoLocalOn)
			this.encenderVideoLocalLlamada(remitente, sessionDescription, this.continuarLlamada);
		else
			this.continuarLlamada(this, remitente, sessionDescription);
		
	}
	
	/** ESTABLECIMIENTO DE LLAMADA DESDE EL RECEPTOR **/
	encenderVideoLocalLlamada(remitente, sessionDescription, callback) {
		let self = this;
		
		let constraints = {
			video : true,
			audio : false
		};
		navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia;
		navigator.getUserMedia(
			constraints, 
			function(stream) {
				let widgetVideoLocal = document.getElementById("widgetVideoLocal");
				self.localStream = stream;
				widgetVideoLocal.srcObject = stream;
				self.videoLocalOn = true;
				self.addMensaje("Vídeo local conectado", "orange");
				/* Llamada a la funcion continuarLlamada */
				callback(self, remitente, sessionDescription);
			}, 
			function(error) {
				self.addMensaje("Error al cargar vídeo local: " + error, "red");
			}
		);
	}
	
	continuarLlamada(self, remitente, sessionDescription) {
		/* Creamos la conexion */
		self.crearConexion(self);
		
		/* Establecemos la sesion RTC a la conexion creada */
		let rtcSessionDescription = new RTCSessionDescription(sessionDescription);
		self.conexion.setRemoteDescription(rtcSessionDescription);
						
		self.addMensaje("Llamada aceptada", "green");
			
		let sdpConstraints = {};
		self.conexion.createAnswer(
			function(sessionDescription) {
				self.conexion.setLocalDescription(sessionDescription).then(
					function() {
						let msg = {
							type : "ANSWER",
							sessionDescription : sessionDescription
						};
						/* Enviamos el mensaje de ACEPTACIÓN de llamada al servidor WS de Signaling*/
						self.ws.send(JSON.stringify(msg));
					}
				);
			},
			function(error) {
				self.addMensaje("Error al crear oferta en el servidor Stun: " + error, "red");
			},
			sdpConstraints
		);
	}
	
	
	/* Creamos la conexion RTCpeer para poder establecer la llamada */
	crearConexion(self) {
		let servers = { 
			iceServers : [ 
				//{ "url" : "stun:stun.1.google.com:19302" }
				{ 
					urls : "turn:localhost",
					username : "webrtc",
					credential : "turnserver"
				}
			]
		};
		self.conexion = new RTCPeerConnection(servers);

		let localTracks = self.localStream.getTracks();
		localTracks.forEach(track =>
			{
				self.conexion.addTrack(track, self.localStream);
			}
		);
		
		self.conexion.onicecandidate = function(event) {
			if (event.candidate) {
				let msg = {
					type : "CANDIDATE",
					candidate : event.candidate
				};
				self.ws.send(JSON.stringify(msg));
			}
		}
		
		/*self.conexion.oniceconnectionstatechange = function(event) {
			//self.addMensaje("self.conexion.oniceconnectionstatechange: " + self.conexion.iceConnectionState, "DeepPink");
		}
			
		self.conexion.onicegatheringstatechange = function(event) {
			//self.addMensaje("self.conexion.onicegatheringstatechange: " + self.conexion.iceGatheringState, "DeepPink");
		}
		
		self.conexion.onsignalingstatechange = function(event) {
			//self.addMensaje("self.conexion.onsignalingstatechange: " + self.conexion.signalingState, "DeepPink");
		}
	
		self.conexion.onnegotiationneeded = function(event) {
			//self.addMensaje("Negociación finalizada: self.conexion.onnegotiationneeded", "black");
			//self.addMensaje("Listo para enviar oferta", "black");
		}*/
			
		self.conexion.ontrack = function(event) {
			let widgetVideoRemoto = document.getElementById("widgetVideoRemoto");
			widgetVideoRemoto.srcObject = event.streams[0];
			self.addMensaje("Vídeo remoto conectado", "orange");
		}
		
		self.conexion.onremovetrack = function(event) {
			self.addMensaje("self.conexion.onremovetrack");
		}
	}	
	
	/** ESTABLECIMIENTO DE LLAMADA DESDE EL REMITENTE **/
	llamar(destinatario) {
		let self = this;
		
		/* Si el video local no está encendido lo encendemos */
		if (!this.videoLocalOn)
			this.encenderVideoLocal(self, destinatario, this.continuarConexion);
		else
			this.continuarConexion(self, destinatario);
	}
	
	encenderVideoLocal(self, destinatario, callback) {
		let constraints = {
			video : true,
			audio : false
		};
		navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia;
		navigator.getUserMedia(
			constraints, 
			function(stream) {
				let widgetVideoLocal = document.getElementById("widgetVideoLocal");
				self.localStream = stream;
				widgetVideoLocal.srcObject = stream;
				self.videoLocalOn = true;
				self.addMensaje("Vídeo local conectado", "green");
				/* Llamada a la funcion continuarConexion */
				callback(self, destinatario);
			}, 
			function(error) {
				self.addMensaje("Error al cargar vídeo local: " + error, "red");
			}
		);
	}
	
	continuarConexion(self, destinatario) {
		let sdpConstraints = {};
		/* Creamos la conexion RTC */
		self.crearConexion(self);
		
		/* Creamos y enviamos la oferta al servidor WS de Signaling */
		self.conexion.createOffer(
			function(sessionDescription) {
				self.conexion.setLocalDescription(sessionDescription);
				self.addMensaje("Llamando a " + destinatario + " mediante el servidor de Signaling");
				let msg = {
					type : "OFFER",
					sessionDescription : sessionDescription,
					recipient : destinatario
				};
				self.ws.send(JSON.stringify(msg));
			},
			function(error) {
				self.addMensaje("Error al crear oferta en el servidor Stun", true);
			},
			sdpConstraints
		);
	}

	addMensaje(texto, color) {
		let mensaje = {
			texto : texto,
			color : color ? color : "blue"
		};
		this.mensajes.push(mensaje);
	}
}