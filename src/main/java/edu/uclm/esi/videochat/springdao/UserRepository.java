package edu.uclm.esi.videochat.springdao;

import java.util.Optional;
import edu.uclm.esi.videochat.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;


@SuppressWarnings("javadoc")
public interface UserRepository extends CrudRepository<User, String> {
	public User findByNameAndPwd (String name, String pwd);
	public Optional<User> findByName (String name);
	public Optional<User> findByEmail(String email);
	
	@Query (value = "SELECT count(*) FROM users WHERE name = :name and pwd = :pwd", nativeQuery = true)
	public int checkPassword(@Param("name") String name, @Param("pwd") String pwd);
	
}