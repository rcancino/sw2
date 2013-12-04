package com.luxsoft.siipap.swing.login;

import org.jdesktop.swingx.auth.LoginService;

import com.luxsoft.siipap.service.ServiceLocator2;

public class SWXLoginService extends LoginService{
	
	public SWXLoginService(){
		setSynchronous(false);
	}

	@Override
	public boolean authenticate(String name, char[] password, String server) throws Exception {
		ServiceLocator2.getLoginManager().authenticate(name, new String(password));
		/*try {
			
		} catch (Exception e) {
			System.out.println("Error: "+e.getMessage());
			return false;
		}
		*/
		return true;
	}

}
