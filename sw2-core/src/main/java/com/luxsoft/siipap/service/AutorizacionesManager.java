package com.luxsoft.siipap.service;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.apache.log4j.Logger;

import com.luxsoft.siipap.model.Role;
import com.luxsoft.siipap.model.User;

/**
 * Se encarga de procesar las autorizaciones
 * 
 * 
 * @author Ruben Cancino
 * 
 */
public class AutorizacionesManager {

	private Logger logger = Logger.getLogger(getClass());
	private AuthenticationManager authenticationManager;

	static {
		SecurityContextHolder
				.setStrategyName(SecurityContextHolder.MODE_GLOBAL);
	}

	public AutorizacionesManager() {
	}
	
	
	
	/**
	 * 
	 * @param username
	 * @param password
	 * @param roleName
	 * @return
	 */
	public User autorizarOperacionPorRole(String username,final String password,String roleName){
		User user=authenticate(username, password);
		if(user==null) return null;
		Role role=user.getRole(roleName);
		return role!=null?user:null;
	}

	/**
	 * Autentifica un usuario
	 * 
	 * @param username
	 * @param password
	 */
	public User authenticate(final String username, final String password) {
		
		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
		
		Authentication authResult;
		try {
			authResult = authenticationManager.authenticate(authRequest);
		} catch (AuthenticationException failed) {			
			logger.info("Authentication request for user: " + username+ " failed: " + failed.toString());			
			throw failed;			
		}
		return (User)authResult.getPrincipal();
	}

	public AuthenticationManager getAuthenticationManager() {
		return authenticationManager;
	}

	public void setAuthenticationManager(
			AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	public static void main(String[] args) {
		User user=ServiceLocator2.getAutorizacionesManager().authenticate("admin", "admin");
		System.out.println("User: "+user);
	}

}
