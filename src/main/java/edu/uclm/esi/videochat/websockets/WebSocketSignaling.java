package edu.uclm.esi.videochat.websockets;

import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import edu.uclm.esi.videochat.model.User;

/**
 * INFO: esta clase contiene los métodos para gestionar WS de tipo Videollamada.
 */
@Component
public class WebSocketSignaling extends WebSocketVideoChat {
	private ConcurrentHashMap<String, VideoRoom> videoRooms = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, WrapperSession> sessionsByUserName = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, WrapperSession> sessionsById = new ConcurrentHashMap<>();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		session.setBinaryMessageSizeLimit(1000*1024*1024);
		session.setTextMessageSizeLimit(64*1024);

		User user = getUser(session);
		user.setSessionSignaling(session);

		WrapperSession wrapper = new WrapperSession(session, user);
		this.sessionsByUserName.put(user.getName(), wrapper);
		this.sessionsById.put(session.getId(), wrapper);
	}

	@Override
	protected void handleTextMessage(WebSocketSession navegadorDelRemitente, TextMessage message) throws Exception {
		JSONObject jso = new JSONObject(message.getPayload());
		String type = jso.getString("type");
		
		User remitente = this.sessionsById.get(navegadorDelRemitente.getId()).getUser();
		String nombreRemitente = remitente.getName();
		
		String recipient = jso.optString("recipient");
		WebSocketSession navegadorDelDestinatario = null;
		
		if (recipient.length()>0)
			navegadorDelDestinatario = this.sessionsByUserName.get(recipient).getSession();

		if (type.equals("OFFER")) {
			VideoRoom videoRoom = new VideoRoom(navegadorDelRemitente, navegadorDelDestinatario);
			this.videoRooms.put("1", videoRoom);
			this.send(navegadorDelDestinatario, "type", "OFFER", "remitente", nombreRemitente, "sessionDescription", jso.get("sessionDescription"));
			return;
		}
		
		if (type.equals("ANSWER")) {
			VideoRoom videoRoom = this.videoRooms.get("1");
			this.send(videoRoom.getA(), "type", "ANSWER", "sessionDescription", jso.get("sessionDescription"));
			return;
		}
		
		if (type.equals("CANDIDATE")) {
			VideoRoom videoRoom = this.videoRooms.get("1");
			videoRoom.broadcast("type", "CANDIDATE", "candidate", jso.get("candidate"));
		}
		
		if (type.equals("CANCEL")) {
			VideoRoom videoRoom = this.videoRooms.get("1");
			this.send(videoRoom.getA(), "type", "CANCEL", "sessionDescription", jso.get("sessionDescription"));
			return;
		}
	}
}
