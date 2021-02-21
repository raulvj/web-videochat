package edu.uclm.esi.videochat.springdao;

import edu.uclm.esi.videochat.model.Token;
import org.springframework.data.repository.CrudRepository;


@SuppressWarnings("javadoc")
public interface TokenRepository extends CrudRepository <Token, String> {

}
