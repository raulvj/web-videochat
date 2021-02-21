package edu.uclm.esi.videochat.websockets;

import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import edu.uclm.esi.videochat.model.User;
import edu.uclm.esi.videochat.model.Manager;
import edu.uclm.esi.videochat.model.Message;


/**
 * INFO: esta clase contiene los métodos para gestionar WS de tipo Texto.
 */
@Component
public class WebSocketTexto extends WebSocketVideoChat {

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		session.setBinaryMessageSizeLimit(1000 * 1024 * 1024);
		session.setTextMessageSizeLimit(64 * 1024);

		User user = getUser(session);
		user.setSessionTexto(session);

		JSONObject mensaje = new JSONObject();
		mensaje.put("type", "ARRIVAL");
		mensaje.put("user", user.getName());
		mensaje.put("picture", user.getPicture());
		this.broadcast(mensaje);

		WrapperSession wrapper = new WrapperSession(session, user);
		this.sessionsByUserName.put(user.getName(), wrapper);
		this.sessionsById.put(session.getId(), wrapper);
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		JSONObject jso = new JSONObject(message.getPayload());
		String type = jso.getString("type");
		String enviador = getUser(session).getName();
		String destinatario = jso.optString("recipient"); // Con optString devolvemos una cadena vacia si no existe la clave.

		if (type.equals("BROADCAST")) {
			JSONObject jsoMessage = new JSONObject();
			jsoMessage.put("type", "FOR ALL");
			jsoMessage.put("message", jso.getString("message"));
			//jsoMessage.put("time", formatDate(System.currentTimeMillis()));
			jsoMessage.put("sender", enviador);

			broadcast(jsoMessage);
			saveMessage(enviador, "", jso.getString("message"));

		} else if (type.equals("PARTICULAR")) {
			// JSONObject recipient = jso.optJSONObject("recipient");
			User user = Manager.get().findUser(destinatario);
			WebSocketSession navegadorDestinatario = user.getSessionTexto();
			JSONObject jsoMessageToSend = new JSONObject(); 
			jsoMessageToSend.put("texto", jso.get("message")); 
			jsoMessageToSend.put("hora", System.currentTimeMillis());
			this.send(navegadorDestinatario, "type", "PARTICULAR", "remitente", enviador, "message",
					jsoMessageToSend);
			saveMessage(enviador, destinatario, jso.getString("message"));

		}
	}

	/* Guardamos mensajes en la Base de Datos */
	private void saveMessage(String sender, String recipient, String message) throws NullPointerException{
		try {
			if(!sender.equals(null) && !recipient.equals(null)) {
				Message receivedMessage = new Message();
				receivedMessage.setSender(sender);
				receivedMessage.setRecipient(recipient);
				receivedMessage.setMessage(message);
				receivedMessage.setDate(System.currentTimeMillis());
				Manager.get().getMessageRepo().save(receivedMessage);
			}
		} catch (NullPointerException e) {
			// TODO: handle exception
			System.out.println(e.getMessage());
		}
	}

	@Override
	protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
		session.setBinaryMessageSizeLimit(1000 * 1024 * 1024);

		byte[] payload = message.getPayload().array();
		System.out.println("La sesión " + session.getId() + " manda un binario de " + payload.length + " bytes");
	}
}
