package edu.uclm.esi.videochat.model;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;

@SuppressWarnings("javadoc")
@Entity
public class Token {
	@Id
	private String id;
	private String email;
	private long date;
	
	/* Empty constructor */
	public Token() {
	}

	/* Constructor */
	public Token(String email) {
		this.id = UUID.randomUUID().toString();
		this.email = email;
		this.date = System.currentTimeMillis();		
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

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}
}
