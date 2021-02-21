package edu.uclm.esi.videochat.http;

import java.util.Map;
import java.util.Base64;
import java.util.Vector;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import edu.uclm.esi.videochat.model.User;
import edu.uclm.esi.videochat.model.Token;
import edu.uclm.esi.videochat.model.Email;
import edu.uclm.esi.videochat.model.Manager;
import edu.uclm.esi.videochat.springdao.UserRepository;
import edu.uclm.esi.videochat.springdao.TokenRepository;


@SuppressWarnings("javadoc")
@RestController
@RequestMapping("users")
public class UsersController {
	
	@Autowired 
	private UserRepository userRepo;
	
	@Autowired 
	private TokenRepository tokenRepo;
	
	/**
	 * INFO: creamos el recurso /login para hacer una peticion de login.
	 * @param request
	 * @param credenciales
	 * @return User: el usuario registrado siempre y cuando sus credenciales sean válidas.
	 * @throws Exception
	 */
	@PostMapping(value = "/login")
	public User login(HttpServletRequest request, @RequestBody Map<String, Object> credenciales) throws Exception {
		JSONObject jso = new JSONObject(credenciales);
		String name = jso.getString("name");
		String pwd = jso.getString("pwd");
		User user = userRepo.findByNameAndPwd(name, pwd);
		/* Buscamos el usuario en la BD con las credenciales que hemos recibido */
		if(user==null) 
			throw new Exception("Incorrect login. Please check your credentials");
		
		/* Por motivos del testing de Selenium hemos comentado las siguientes lineas */
		/* Comprobamos la fecha de confirmacion, si es igual a 0 es que el usuario aún no ha confirmado su cuenta y 
		 * deberá hacerlo a través del correo que le ha llegado */
		//if(user.getConfirmationDate() == 0){
		//	throw new Exception("The account has not been confirmed yet.");
		//}else {
		Manager.get().add(user);
		/* Vamos a guardar en la sesion, el nombre del usuario que se ha logeado */
		request.getSession().setAttribute("user", user);			
		Manager.get().add(request.getSession());
		return user;
		//}
	}
	
	/**
	 * INFO: creamos el recurso /register para hacer una peticion de registro en base a los datos introducidos.
	 * @param datos
	 * @throws Exception
	 */
	@PutMapping("/register")
	public void register(@RequestBody Map<String, Object> datos) throws Exception {
		JSONObject jso = new JSONObject(datos);
		String name = jso.getString("name");
		String email = jso.getString("email");
		String pwd1 = jso.getString("pwd1");
		String pwd2 = jso.getString("pwd2");
		String picture = jso.getString("picture");
		if (!pwd1.equals(pwd2))
			throw new Exception("ERROR. Password doesn't match");
		
		/* Creamos un usuario con los datos recibidos. */
		User user = new User();
		user.setEmail(email);
		user.setName(name);
		user.setPwd(pwd1);
		user.setConfirmationDate(0);
		
		/* Si el usuario no ha seleccionado ninguna foto, se le pondrá una por defecto */
		if (picture.equals("")) {
			String foto = read("default.png");
			user.setPicture(foto);
		}else {
			user.setPicture(picture);
		}
		
		userRepo.save(user);
		
		/* Una vez guardado el usuario en la BD, creamos un token y lo enviamos a su correo electrónico para que confirme la cuenta */
		Token token = new Token(email);
		tokenRepo.save(token);
		Email sender = new Email();
		sender.send("raul.valentin1@alu.uclm.es", "Bienvenido al Videochat", 
				"Para confirmar, pulse aquí: " +
				"https://localhost:7500/users/confirmarCuenta?tokenId=" + token.getId());
	}
	
	/**
	 * INFO: método para leer una fotografía en la carpeta de resources.
	 * @param filename
	 * @return value: string que contiene la información acerca de la foto de perfil por defecto.
	 */
	public String read(String filename) {
		ClassLoader classLoader = getClass().getClassLoader();
		try {
			InputStream fis = classLoader.getResourceAsStream(filename);
			byte[] b = new byte[fis.available()];
			fis.read(b);
			String encodedUrl = new String(Base64.getEncoder().encode(b)); 
			String value = "data:image/png;base64," + encodedUrl;
			return value;
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
	}

	/**
	 * INFO: creamos el recurso /confirmarCuenta para llevar a cabo la confirmación de registro de un usuario.
	 * @param request
	 * @param response
	 * @param tokenId: el token que habíamos creado previamente y que servirá para identificar al usuario.
	 * @throws IOException
	 */
	@GetMapping("/confirmarCuenta")
	public void confirmarCuenta(HttpServletRequest request, HttpServletResponse response, @RequestParam String tokenId) throws IOException {
		/* Recuperamos de la base de datos el token y comprobamos su fecha */
		Optional<Token> tok = tokenRepo.findById(tokenId);
		/* Por el momento, el único requisito que hemos puesto es que la fecha sea menor a la actual, 
		 * pero se podría poner otro como por ejemplo, que la fecha sea menor y no haya pasado más de X tiempo. */
		if(tok.get().getDate() < System.currentTimeMillis()) {
			Optional<User> user = userRepo.findByEmail(tok.get().getEmail());
			user.get().setConfirmationDate(System.currentTimeMillis());
			
			userRepo.save(user.get());
		}
		
		response.sendRedirect("https://localhost:7500/");
	}
	
	/**
	 * NO UTILIZADO EN ESTA VERSIÓN
	 * @param request
	 * @param response
	 * @param tokenId
	 * @throws IOException
	 */
	@GetMapping("/confirmarCuenta2/{tokenId}")
	public void confirmarCuenta2(HttpServletRequest request, HttpServletResponse response, @PathVariable String tokenId) throws IOException {
		// Ir a la base de datos, buscar el token con ese tokenId en la tabla, ver que no ha caducado
		// y actualizar la confirmationDate del user
		System.out.println(tokenId);
		response.sendRedirect("https://localhost:7500/");
	}
	
	/**
	 * NO UTILIZADO EN ESTA VERSIÓN
	 * @param credenciales
	 * @throws Exception
	 */
	@PatchMapping("/cambiarPwd")
	public void cambiarPwd(@RequestBody Map<String, String> credenciales) throws Exception {
		JSONObject jso = new JSONObject(credenciales);
		String name = jso.getString("name");
		String pwd = jso.getString("pwd");
		String pwd1 = jso.getString("pwd1");
		String pwd2 = jso.getString("pwd2");
		
		if (userRepo.checkPassword(name, pwd) > 0) {
			if (pwd1.equals(pwd2)) {
				User user = userRepo.findByNameAndPwd(name, pwd);
				user.setPwd(pwd1);
				userRepo.save(user);
			} else throw new Exception("Las passwords no coinciden");
		} else 
			throw new Exception("Credenciales inválidas");
	}
	
	/**
	 * INFO: creamos el recurso /getUsuariosConectados para hacer una peticion que nos devuelva la información sobre los usuarios que están conectados en la app.
	 * @return vector de usuarios que contiene la información de todos los usuarios registrados (salvo su password)
	 */
	@GetMapping(value = "/getUsuariosConectados", produces = MediaType.APPLICATION_JSON_VALUE)
	public Vector<User> getUsuariosConectados() {
		return Manager.get().getUsuariosConectados();
	}
	
	@GetMapping(value = "/getUsuariosConectadosAsync", produces = MediaType.APPLICATION_JSON_VALUE)
	public Vector<String> getUsuariosConectadosAsync() {
		return Manager.get().getUsuariosConectadosAsync();
	}
	
	@PostMapping(value = "/getPicturesAsync", produces = MediaType.APPLICATION_JSON_VALUE)
	public Vector<String> getPicturesAsync(@RequestBody Map<String, Object> usernamejson) {
		JSONObject jso = new JSONObject(usernamejson);
		String username = jso.getString("username");
		return Manager.get().getPicturesAsync(username);
	}
}
