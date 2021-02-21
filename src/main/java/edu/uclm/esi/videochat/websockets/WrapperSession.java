package edu.uclm.esi.videochat.websockets;

import org.springframework.web.socket.WebSocketSession;

import edu.uclm.esi.videochat.model.User;


/**
 * INFO: esta clase se usa para agrupar las sesiones de los usuarios y corregir un fallo de duplicidad que existía.
 */
public class WrapperSession {
	
	private WebSocketSession session;
	private User user;
	
	/* Constructor */
	public WrapperSession(WebSocketSession session, User user) {
		this.session = session;
		this.user = user;
	}
	
	/* Getters */
	public WebSocketSession getSession() {
		return this.session;
	}
	
	public User getUser() {
		return this.user;
	}
}
