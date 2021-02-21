class Mensaje {
	/* NOTA: constructor de mensajes para el HTML */
	constructor(texto, hora) {
		this.texto = texto;
		var date = new Date();
		this.hora = hora ? hora : (date.getUTCDate() + "-" + date.getMonth()+1 + "-" + date.getUTCFullYear()
					+ ", " + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds());
	}
}