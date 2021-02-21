package edu.uclm.esi.videochat.http;

import java.util.Map;
import java.util.List;
import java.util.Vector;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import edu.uclm.esi.videochat.model.Message;
import edu.uclm.esi.videochat.springdao.MessageRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;


@SuppressWarnings("javadoc")
@RestController
@RequestMapping("messages")
public class MessageController {
	
	@Autowired 
	private MessageRepository messageRepo;
	
	/**
	 * INFO: creamos el recurso /getMensajes para hacer una peticion al MessageRepository y obtener mensajes de la BD.
	 * @param usuarios
	 * @return mensajesVector: un vector que contiene los mensajes recuperados de la query a la BD.
	 * @throws Exception
	 */
	@PostMapping(value = "/getMensajes", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Message> getMensajes(@RequestBody Map<String, Object> usuarios) throws Exception {
		Iterable<Message> mensajes;
		JSONObject jso = new JSONObject(usuarios);
		String sender= jso.getString("sender");
		String recipient = jso.getString("recipient");
		
		/* Si el receptor viene vacío, recuperamos los mensajes del chat General */
		if (recipient.equals("")){
			 mensajes = messageRepo.findByRecipient(recipient);
		}
		/* Recuperamos los mensajes enviados por SENDER a RECIPIENT */
		else { 
			mensajes = messageRepo.findBySenderAndRecipient(recipient, sender);
		}
		
		/* Creamos una lista con los mensajes obtenidos de la Query que después devolveremos */
		Vector<Message> mensajesVector = new Vector<>();
		for(Message mes : mensajes){
			mensajesVector.add(mes);
		}
				
		return mensajesVector;
	}
}


