package edu.uclm.esi.videochat.springdao;

import edu.uclm.esi.videochat.model.Message;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;


@SuppressWarnings("javadoc")
public interface MessageRepository extends CrudRepository<Message, String> {
	public Iterable<Message> findAllByRecipientAndSender (String recipient, String sender);
	
	/**
	 * INFO: Query para recuperar los mensajes de una conversación privada
	 * @param sender el usuario que envía el mensaje.
	 * @param recipient el usuario que recibe el mensaje.
	 * @return mensajes que cumplen las condiciones de la consulta.
	 */
	@Query (value = "SELECT * FROM Message WHERE (sender =:sender and recipient =:recipient) or (sender =:recipient and recipient =:sender) ORDER BY date", nativeQuery = true)
	public Iterable<Message> findBySenderAndRecipient(@Param("sender") String sender, @Param("recipient") String recipient);
	
	/**
	 * INFO: Query para recuperar los mensajes del Chat general. El recipient es "".
	 * @param recipient el usuario que recibe el mensaje en la conversacion general es "".
	 * @return mensajes que cumples las condiciones de la consulta.
	 */
	@Query (value = "SELECT * FROM Message WHERE recipient =:recipient ORDER BY date", nativeQuery = true)
	public Iterable<Message> findByRecipient(String recipient);
	
}