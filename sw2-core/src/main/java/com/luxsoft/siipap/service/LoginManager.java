package com.luxsoft.siipap.service;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.ui.basicauth.BasicProcessingFilter;
import org.apache.log4j.Logger;

import com.luxsoft.siipap.model.User;

/**
 * Se encarga de firmar al usuario Similar a{@link BasicProcessingFilter} que se
 * usa en aplicaciones WEB
 * 
 * @see BasicProcessingFilter
 * @author Ruben Cancino
 * 
 */
public class LoginManager {

	private Logger logger = Logger.getLogger(getClass());
	private AuthenticationManager authenticationManager;

	static {
		SecurityContextHolder
				.setStrategyName(SecurityContextHolder.MODE_GLOBAL);
	}

	public LoginManager() {
	}

	/**
	 * Regresa el usuario actual firmado al sistema
	 * 
	 * @return El usuario registrado o nulo si no se ha registrado un usario
	 */
	public static User getCurrentUser() {
		Authentication existingAuth = SecurityContextHolder.getContext()
				.getAuthentication();
		if (existingAuth != null)
			return (User) existingAuth.getPrincipal();
		return null;

	}

	public static Authentication getCurrentAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

	/**
	 * Autentifica un usuario
	 * 
	 * @param username
	 * @param password
	 */
	public void authenticate(final String username, final String password) {
		if (authenticationIsRequired(username)) {
			UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
					username, password);
			// TODO: Fill more details
			// authRequest.setDetails();

			Authentication authResult;

			try {
				authResult = authenticationManager.authenticate(authRequest);
			} catch (AuthenticationException failed) {
				// Authentication failed
				if (logger.isDebugEnabled()) {
					logger.debug("Authentication request for user: " + username
							+ " failed: " + failed.toString());
				}

				SecurityContextHolder.getContext().setAuthentication(null);
				throw failed;

				// return;
			}

			// Authentication success
			if (logger.isDebugEnabled()) {
				logger
						.debug("Authentication success: "
								+ authResult.toString());
			}
			SecurityContextHolder.getContext().setAuthentication(authResult);
		}
	}

	private boolean authenticationIsRequired(String username) {
		// Only reauthenticate if username doesn't match SecurityContextHolder
		// and user isn't authenticated
		// (see SEC-53)
		Authentication existingAuth = SecurityContextHolder.getContext()
				.getAuthentication();

		if (existingAuth == null || !existingAuth.isAuthenticated()) {
			return true;
		}

		// Limit username comparison to providers which use usernames (ie
		// UsernamePasswordAuthenticationToken)
		// (see SEC-348)

		if (existingAuth instanceof UsernamePasswordAuthenticationToken
				&& !existingAuth.getName().equals(username)) {
			return true;
		}

		return false;
	}

	public User autentificar(String username,final String password) {
		//String username="admin";
		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
				username, password);
		Authentication authResult;

		try {
			authResult = authenticationManager.authenticate(authRequest);
		} catch (AuthenticationException failed) {
			// Authentication failed
			if (logger.isDebugEnabled()) {
				logger.debug("Authentication request for user: " + username
						+ " failed: " + failed.toString());
			}			
			return null;
		}
		// Authentication success
		if (logger.isDebugEnabled()) {
			logger.debug("Authentication success: " + authResult.toString());
		}
		User user=(User)authResult.getPrincipal();
		System.out.println("Usuario autentificado: "+user);
		return user;
	}

	public AuthenticationManager getAuthenticationManager() {
		return authenticationManager;
	}

	public void setAuthenticationManager(
			AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	public static void main(String[] args) {
		User u = ServiceLocator2.getUserManager().getUserByUsername("vroman");
		System.out.println("Roles: " + u.getRoles());
		System.out.println("Permisos: " + u.getPermisos());
	}

}
