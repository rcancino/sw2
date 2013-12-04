package com.luxsoft.siipap.service;

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;

import com.luxsoft.siipap.model.User;

public class LoginManagerTest extends BaseManagerTestCase{
	
	private LoginManager loginManager;	
	
	
	public void testAuthentication() throws UserExistsException{
		
		loginManager.authenticate("admin", "admin");
		Authentication found=SecurityContextHolder.getContext().getAuthentication();
		assertNotNull(found);
		User user=LoginManager.getCurrentUser();
		assertNotNull(user);
		assertEquals("admin", user.getUsername());
		
	}	

	
	public void setLoginManager(LoginManager loginManager) {
		this.loginManager = loginManager;
	}

	
	
	
	

}
