package edu.uclm.esi.videochat.model;

import java.util.Vector;
import java.util.Optional;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import edu.uclm.esi.videochat.model.User;
import edu.uclm.esi.videochat.springdao.UserRepository;
import edu.uclm.esi.videochat.springdao.MessageRepository;


@SuppressWarnings("javadoc")
@Component
public class Manager {
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private MessageRepository messageRepo;
	
	private ConcurrentHashMap<String, User> usersMap;
	private ConcurrentHashMap<String, HttpSession> sessions;
	
	/* Constructor */
	private Manager() {
		this.usersMap = new ConcurrentHashMap<>();
		this.sessions = new ConcurrentHashMap<>();
	}
	
	private static class ManagerHolder {
		static Manager singleton=new Manager();
	}
	
	@Bean
	public static Manager get() {
		return ManagerHolder.singleton;
	}
	
	public void add(User user) {
		usersMap.put(user.getName(), user);
	}
	
	public void remove(User user) {
		this.usersMap.remove(user.getName());
	}
	
	public HttpSession getSession(String sessionId) {
		return this.sessions.get(sessionId);
	}

	public void add(HttpSession session) {
		this.sessions.put(session.getId(), session);
	}

	public User findUser(String userName) {
		return this.usersMap.get(userName);
	}
	
	public MessageRepository getMessageRepo() {
		return messageRepo;
	}
	
	/**
	 * INFO: accedemos al 'mapa' de usuarios del Manager para recuperar los usuarios.
	 * @return listado de usuarios conectados al Videochat.
	 */
	public Vector<User> getUsuariosConectados() {
		Vector<User> users = new Vector<>();
		Enumeration<User> eUsers = this.usersMap.elements();
		while (eUsers.hasMoreElements()) {
			User user = eUsers.nextElement();
			/* Ponemos el campos password a NULL para que no se muestre la contraseña */
			user.setPwd(null);
			users.add(user);
		}
		return users;
	}
	
	public Vector<String> getUsuariosConectadosAsync() {
		Vector<String> users = new Vector<>();
		Enumeration<User> eUsers = this.usersMap.elements();
		while (eUsers.hasMoreElements()) {
			User user = eUsers.nextElement();
			users.add(user.getName());
		}
		return users;
	}
	
	public Vector<String> getPicturesAsync(String name) {
		Optional<User> user = userRepo.findByName(name);
		Vector<String> picture = new Vector<>();
		picture.add(user.get().getPicture());
		return picture;
	}
}
