package edu.uclm.esi.videochat.model;

import java.util.UUID;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Entity;
import javax.persistence.Transient;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.web.socket.WebSocketSession;


@Entity
@Table(name = "users")
public class User {
	@Id
	private String id;
	private String email;
	private String name;
	private String pwd;
	private String picture;
	private long confirmationDate;
	
	@Transient 
	private WebSocketSession sessionSignaling;
	
	@Transient 
	private WebSocketSession sessionTexto;
	
	/* Constructor */
	public User() {
		this.id = UUID.randomUUID().toString();
	}
	
	/* Getters and Setters */
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String userName) {
		this.name = userName;
	}

	@JsonIgnore
	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}
	
	public long getConfirmationDate() {
		return confirmationDate;
	}
	
	public void setConfirmationDate(long date) {
		this.confirmationDate = date;
	}

	@JsonIgnore
	public WebSocketSession getSessionTexto() {
		return sessionTexto;
	}
	
	public void setSessionTexto(WebSocketSession session) {
		this.sessionTexto = session;
	}
	
	@JsonIgnore
	public WebSocketSession getSessionSignaling() {
		return sessionSignaling;
	}
	
	public void setSessionSignaling(WebSocketSession session) {
		this.sessionSignaling = session;
	}
}
