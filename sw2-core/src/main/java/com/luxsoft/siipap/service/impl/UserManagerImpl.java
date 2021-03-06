package com.luxsoft.siipap.service.impl;

import java.util.List;

import javax.jws.WebService;
import javax.persistence.EntityExistsException;

import org.acegisecurity.providers.dao.DaoAuthenticationProvider;
import org.acegisecurity.providers.encoding.PasswordEncoder;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.dao.UserDao;
import com.luxsoft.siipap.model.Address;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.service.UserExistsException;
import com.luxsoft.siipap.service.UserManager;
import com.luxsoft.siipap.service.UserService;


/**
 * Implementation of UserManager interface.
 *
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 */
@WebService(serviceName = "UserService", endpointInterface = "com.luxsoft.siipap.service.UserService")
public class UserManagerImpl extends UniversalManagerImpl implements UserManager, UserService {
    
	private UserDao dao;
    private DaoAuthenticationProvider authenticationProvider;

    /**
     * Set the Dao for communication with the data layer.
     * @param dao the UserDao that communicates with the database
     */
    @Required
    public void setUserDao(UserDao dao) {
        this.dao = dao;
    }
    public UserDao getUserDao(){
    	return this.dao;
    }

    /**
     * Set the DaoAuthenticationProvider object that will provide both the
     * PasswordEncoder and the SaltSource which will be used for password
     * encryption when necessary.
     * @param authenticationProvider the DaoAuthenticationProvider object
     */
    @Required
    public void setAuthenticationProvider(DaoAuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    /**
     * {@inheritDoc}
     */
    public User getUser(String userId) {
        return dao.get(new Long(userId));
    }

    /**
     * {@inheritDoc}
     */
    public List<User> getUsers(User user) {
        return dao.getUsers();
    }
    
    
    /**
     * {@inheritDoc}
     */    
    public User saveUser(User user) throws UserExistsException {

        if (user.getVersion() == null) {
            // if new user, lowercase userId
            user.setUsername(user.getUsername().toLowerCase());
        }
        
        // Get and prepare password management-related artifacts
        boolean passwordChanged = false;
        if (authenticationProvider != null) {
            PasswordEncoder passwordEncoder = authenticationProvider.getPasswordEncoder();

            if (passwordEncoder != null) {
                // Check whether we have to encrypt (or re-encrypt) the password
                if (user.getVersion() == null) {
                    // New user, always encrypt                	
                    passwordChanged = true;
                } else {
                    // Existing user, check password in DB
                    String currentPassword = dao.getUserPassword(user.getUsername());
                    if (currentPassword == null) {
                        passwordChanged = true;
                    } else {
                        if (!currentPassword.equals(user.getPassword())) {
                            passwordChanged = true;
                        }
                    }
                }
                // If password was changed (or new user), encrypt it
                if (passwordChanged) {
                    user.setPassword(passwordEncoder.encodePassword(user.getPassword(), null));                    
                }
            } else {
                log.warn("PasswordEncoder not set on AuthenticationProvider, skipping password encryption...");
            }
        } else {
            log.warn("AuthenticationProvider not set, skipping password encryption...");

        }
        
        try {        	
            return dao.saveUser(user);
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            log.warn(e.getMessage());
            throw new UserExistsException("User '" + user.getUsername() + "' already exists!");
        } catch (EntityExistsException e) { // needed for JPA
            e.printStackTrace();
            log.warn(e.getMessage());
            throw new UserExistsException("User '" + user.getUsername() + "' already exists!");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeUser(String userId) {
        log.debug("removing user: " + userId);
        dao.remove(new Long(userId));
    }

    /**
     * {@inheritDoc}
     * @param username the login name of the human
     * @return User the populated user object
     * @throws UsernameNotFoundException thrown when username not found
     */
    public User getUserByUsername(String username) throws UsernameNotFoundException {
        return (User) dao.loadUserByUsername(username);
    }
    
    /**
     * {@inheritDoc}
     */
    @Transactional(propagation=Propagation.REQUIRED)
	public void resetAdminUser() throws UserExistsException {
		User admin=null;
		try {
			admin=getUserByUsername("admin");			
			admin.setPassword("admin");
		} catch (UsernameNotFoundException e) {
			User user=new User();
			user.setUsername("admin");
			user.setPassword("admin");
			user.setConfirmPassword("admin");
	        user.setFirstName("Administrador");
	        user.setLastName("Administrador");
	        Address address = new Address();
	        address.setAddress("");
	        address.setCity("DF");
	        address.setProvince("DF");
	        address.setCountry("MEXICO");
	        address.setPostalCode("54040");
	        user.setAddress(address);
	        user.setEmail("support@luxsoftnet.com");
	        user.setWebsite("http://luxsoftnet.com");
	        admin=user;
		}
		saveUser(admin);
		
	}
    
    
    
	public List getUsers() {
		return dao.getAll();
	}
	
	
	
	
	public static void main(String[] args) throws UserExistsException {
		/*
		User user=new User();
		user.setUsername("admin");
		user.setPassword("admin");
		user.setEnabled(true);
		user.setAccountLocked(false);
		user.setConfirmPassword("admin");
        user.setFirstName("Administrador");
        user.setLastName("Administrador");
        Address address = new Address();
        address.setAddress("");
        address.setCity("DF");
        address.setProvince("DF");
        address.setCountry("MEXICO");
        address.setPostalCode("54040");
        user.setAddress(address);
        user.setEmail("support@luxsoftnet.com");
        user.setWebsite("http://luxsoftnet.com");
		ServiceLocator2.getUserManager().saveUser(user);
		*/
		User u=ServiceLocator2.getUserManager().getUserByUsername("admin");
		u.setPassword("admin");
		ServiceLocator2.getUserManager().saveUser(u);
	}
    
}
