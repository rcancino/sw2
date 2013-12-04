package com.luxsoft.siipap.service;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import javax.swing.Action;

import org.acegisecurity.providers.encoding.PasswordEncoder;
import org.acegisecurity.providers.encoding.ShaPasswordEncoder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.model.Autorizacion2;
import com.luxsoft.siipap.model.Modulos;
import com.luxsoft.siipap.model.Permiso;
import com.luxsoft.siipap.model.Role;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.sw3.model.AdressLog;



/**
 * Acceso global a información de seguridad
 * 
 * @author Ruben Cancino
 *
 */
public final class KernellSecurity {
	
	private boolean securityEnabled=false;
	private Logger logger=Logger.getLogger(getClass());
	public static final String KERNELL_ENABLED_PROPERTY="luxor.kernell.enabled";
	
	private static KernellSecurity INSTANCE;
	
	private KernellSecurity(){
		String val=System.getProperty(KERNELL_ENABLED_PROPERTY,"FALSE");
		logger.info("Kerenell enabled: "+val);
		securityEnabled=val.equalsIgnoreCase("TRUE");
	}
	
	public static synchronized KernellSecurity instance(){
		
		if(INSTANCE==null){
			INSTANCE=new KernellSecurity();
		}
		return INSTANCE;
	}

	public boolean isSecurityEnabled() {
		return securityEnabled;
	}
	
	public User getCurrentUser(){
		return LoginManager.getCurrentUser();
	}
	public String getCurrentUserName(){
		if(getCurrentUser()!=null){
			return getCurrentUser().getFullName();
		}else {
			if(!isSecurityEnabled())
				return "ADMIN";
		}
		return "ND";
	}
	
	public boolean hasAdminRole(){
		Role r=LoginManager.getCurrentUser().getRole(Role.ROLE_ADMIN);
		return r!=null;
	}
	
	public boolean hasRole(String roleName){
		if(isSecurityEnabled()){
			Role r=LoginManager.getCurrentUser().getRole(roleName);
			return r!=null;
		}
		return true;
		
	}
	
	public boolean hasRole(String roleName,boolean chequSecurityEnabled){
		if(chequSecurityEnabled){
			if(isSecurityEnabled()){
				Role r=LoginManager.getCurrentUser().getRole(roleName);
				return r!=null;
			}
			return true;
		}else{
			User u=LoginManager.getCurrentUser();
			Role r=u!=null?u.getRole(roleName):null;
			return r!=null;
			
		}
	}
	
	/**
	 * Determina si se tiene acceso a un recurso 
	 * 
	 * @param id El identificador del recurso
	 * @param modulo El modulo al que pertenece
	 * @return
	 */
	public boolean isResourceGranted(String id,Modulos modulo){
		if(!isSecurityEnabled())	return true;
		if(hasAdminRole()) return true;
		Permiso p=findPermiso(id, modulo);
		if(logger.isDebugEnabled()){
			String pattern="Permiso id: {0}   : {1}";
			logger.debug(MessageFormat.format(pattern,id,p!=null?"GRANTED":"NOT GRANTED"));
		}
		return p!=null;
	}
	
	/**
	 * Determina si una accion es autorizada ya sea mediante un permiso asignado 
	 * o mediante un role 
	 * 
	 * @param a
	 * @return
	 */
	public boolean isActionGranted(final Action a){
		if(!isSecurityEnabled())	return true;
		if(hasAdminRole()) return true;
		String id=(String)a.getValue("ID");
		
		Permiso p=findPermiso(id, null);
		if(logger.isDebugEnabled()){
			String pattern="Permiso id: {0}   : {1}";
			logger.debug(MessageFormat.format(pattern,id,p!=null?"GRANTED":"NOT GRANTED"));
		}
		//No hay permiso definido, buscando role
		
		if(p==null){
			User user=getCurrentUser();
			if(user!=null){
				return user.hasRole(id);
			}
		}
		return p!=null;
	}
	
	/**
	 * Determina si una consulta es permitida
	 * 
	 * @param a
	 * @return
	 */
	public boolean isConsultaGranted(final String cunsutlaId){
		if(StringUtils.isBlank(cunsutlaId))
			return true;
		if(!isSecurityEnabled())	return true;
		if(hasAdminRole()) return true;
		Permiso p=findPermiso(cunsutlaId, null);
		if(logger.isDebugEnabled()){
			String pattern="Permiso id: {0}   : {1}";
			logger.debug(MessageFormat.format(pattern,cunsutlaId,p!=null?"GRANTED":"NOT GRANTED"));
		}
		return p!=null;
	}
	
	/**
	 * Encuentra el permiso indicado en los permisos efectivos del usuario 
	 * 
	 * @param id
	 * @param modulo
	 * @return
	 */
	private Permiso findPermiso(final String id,final Modulos modulo){
		try {
			if(getCurrentUser()==null)
				return null;
			Permiso p=(Permiso)CollectionUtils.find(getCurrentUser().getPermisos(), new Predicate(){
				public boolean evaluate(Object object) {
					Permiso per=(Permiso)object;
					/*if(per.getModulo().equals(modulo)){
						return per.getNombre().equalsIgnoreCase(id);
					}*/
					return per.getNombre().equalsIgnoreCase(id);
				}
				
			});
			return p;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			return null;
		}
	}
	
	public Autorizacion2 getAutorizacion(){
		Autorizacion2 aut=new Autorizacion2();
		if(isSecurityEnabled()){
			aut.setAutorizo(getCurrentUserName());
		}else{
			aut.setAutorizo("ADMIN PRUEBAS");
		}
		aut.setFechaAutorizacion(new Date());
		aut.setIpAdress(getIPAdress());
		aut.setMacAdress(getMacAdress());
		return aut;
	}
	
	public static String getIPAdress(){
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			System.out.println("IMPOSIBLE RESOLVER IP ADRESS ERR: "+ExceptionUtils.getRootCauseMessage(e));
			return "ND";
		}
	}
	
	public static String getMacAdress(){
		try {		
            InetAddress address = InetAddress.getLocalHost();
			
            /*
             * Get NetworkInterface for the current host and then read the 
             * hardware address.
             */
            NetworkInterface ni = NetworkInterface.getByInetAddress(address);
            byte[] mac = ni.getHardwareAddress();
			StringBuffer buf=new StringBuffer();
			
			
            /*
             * Extract each array of mac address and convert it to hexa with the 
             * following format 08-00-27-DC-4A-9E.
             */
            for (int i = 0; i < mac.length; i++) {
            	Formatter formatter=new Formatter(Locale.getDefault());
                buf.append(formatter.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : "").toString());
                
            }
            return buf.toString();
        } catch (UnknownHostException e) {
            //e.printStackTrace();
            System.out.println("IMPOSIBLE RESOLVER MAC ADRESS ERR: "+ExceptionUtils.getRootCauseMessage(e));
            return "ND";
        } catch (SocketException e) {
        	System.out.println("IMPOSIBLE RESOLVER MAC ADRESS ERR: "+ExceptionUtils.getRootCauseMessage(e));
        	return "ND";
        }
	}
	
	
	public synchronized User findUser(String password,HibernateTemplate source){
		
		PasswordEncoder encoder=new ShaPasswordEncoder();
		/*PasswordEncoder encoder=(PasswordEncoder)ServiceLocator2.instance()
			.getContext().getBean("passwordEncoder");
		*/
		//Buscamos el password en la base de datos
		final String epass=encoder.encodePassword(password, null);
		//System.out.println("Encoded password: "+epass);
		User res=(User)CollectionUtils.find(getUsuarios(source), new Predicate() {
			public boolean evaluate(Object object) {
				User u=(User)object;
				return u.getPassword().equals(epass);
			}
		});
		//if(res!=null)
			//logger.info("Usuario localizado: "+res);
		return res;
	}
	
	private List<User> usuarios;
	
	public List<User> getUsuarios(HibernateTemplate source){
		if(usuarios==null){
			usuarios=source.find("from User u");
		}
		return usuarios;
	}
	
	
	public   void registrarUserLog(Object bean,String property){
		Date time=new Date();
		String user=getCurrentUserName();
		BeanWrapperImpl wrapper=new BeanWrapperImpl(bean);
		UserLog  log=(UserLog)wrapper.getPropertyValue(property);
		if(log==null){
			log=new UserLog();
			log.setCreado(time);
			log.setCreateUser(user);
		}
		log.setModificado(time);
		log.setUpdateUser(user);
		wrapper.setPropertyValue(property, log);
		
	}
	
	
	public   void registrarUserLog(User u,Object bean,String property){
		Date time=new Date();
		String user=u.getFullName();
		BeanWrapperImpl wrapper=new BeanWrapperImpl(bean);
		UserLog  log=(UserLog)wrapper.getPropertyValue(property);
		if(log==null){
			log=new UserLog();
			log.setCreado(time);
			log.setCreateUser(user);
		}
		log.setModificado(time);
		log.setUpdateUser(user);
		wrapper.setPropertyValue(property, log);
		
	}
	
	public   void registrarAddressLog(Object bean,String property){
		String ip=getIPAdress();
		String mac=getMacAdress();
		BeanWrapperImpl wrapper=new BeanWrapperImpl(bean);
		AdressLog  log=(AdressLog)wrapper.getPropertyValue(property);
		if(log==null){
			log=new AdressLog();
			log.setCreatedIp(ip);
			log.setCreatedMac(mac);
		}
		log.setUpdatedIp(ip);
		log.setUpdatedMac(mac);
		wrapper.setPropertyValue(property, log);
	}
	
	
	public static void main(String[] args) throws UnknownHostException, SocketException {
		//System.out.println("Mac Adress:"+getMacAdress());
		User u=KernellSecurity.instance().findUser("mevale",ServiceLocator2.getHibernateTemplate());
		System.out.println("Usuario localizado: "+u);
		
	}

}
