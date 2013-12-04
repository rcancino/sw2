package com.luxsoft.siipap.kernell;

import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.UniqueList;

import com.luxsoft.siipap.cxc.CXCActions;
import com.luxsoft.siipap.gastos.GasActions;
import com.luxsoft.siipap.model.Modulos;
import com.luxsoft.siipap.model.Permiso;
import com.luxsoft.siipap.model.Role;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.service.UniversalManager;
import com.luxsoft.siipap.service.UserExistsException;
import com.luxsoft.siipap.service.UserManager;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swx.GlobalActions;

/**
 * Presentation layer para el Kernell 
 * 
 * @author Ruben Cancino
 *
 */
public class KernellModel {
	
	private EventList<User> usuarios;
	private EventList<Role> roles;
	private EventList<Permiso> permisos;
	
	
	@SuppressWarnings("unchecked")
	public void loadUsuarios(){
		usuarios.clear();
		usuarios.addAll(getUserManager().getUsers());
	}
	
	@SuppressWarnings("unchecked")
	public void loadRoles(){
		getRoles().clear();
		roles.addAll(getUniversalManager().getAll(Role.class));
	}

	@SuppressWarnings("unchecked")
	public void loadPermisos(){
		permisos.clear();
		permisos.addAll(getUniversalManager().getAll(Permiso.class));
	}
	
	public EventList<User> getUsuarios() {
		if(usuarios==null){
			EventList<User> source=new BasicEventList<User>();
			usuarios=GlazedLists.threadSafeList(source);
		}
		return usuarios;
	}
	
	public EventList<Role> getRoles(){
		if(roles==null){
			EventList<Role> source=new UniqueList<Role>(new BasicEventList<Role>(),GlazedLists.beanPropertyComparator(Role.class, "name"));
			roles=GlazedLists.threadSafeList(source);
			//roles=new UniqueList<Role>(roles);
		}
		return roles;
	}
	
	
	public EventList<Permiso> getPermisos(){
		if(permisos==null){
			EventList<Permiso> source=new BasicEventList<Permiso>();
			permisos=GlazedLists.threadSafeList(source);
		}
		return permisos;
	}
	
	public void actualizarUsuario(User user) throws Exception{		
		
		int index=usuarios.indexOf(user);
		if(index!=-1){
			User res=getUserManager().saveUser(user);
			usuarios.set(index, res);
			
		}
			
	}
	
	public void actualizarRole(Role r)throws Exception{		
		Role res=(Role)getUniversalManager().save(r);
		if(r.getId()!=null){
			int index=roles.indexOf(r);
			roles.set(index, res);	
		}else{
			roles.add(res);
		}
		
	}
	public void eliminarUsuario(User user){
		getUserManager().remove(User.class, user.getId());
	}
	
	public void eliminarRole(final Role r){		
		getUniversalManager().remove(Role.class, r.getId());
		roles.remove(r);
	}
	
	public void salvaNuevoUsuario(final User user) throws UserExistsException{
		User u=getUserManager().saveUser(user);
		usuarios.add(u);
	}
	
	protected UserManager getUserManager(){
		return ServiceLocator2.getUserManager();
	}
	
	protected UniversalManager getUniversalManager(){
		return ServiceLocator2.getUniversalManager();
	}
	
	private void actualizarPermisos(final Modulos m,final List<Permiso> permisos){
		
		//Elminar todos los permisos del modulo
		for(Permiso p:permisos){
			try {
				getUniversalManager().save(p);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			loadPermisos();
		}
	}
	
	
	
	public void regenerarPermisos(Modulos modulo){
		switch (modulo) {
		case KERNELL:
			actualizarPermisos(modulo,KernellActions.toPermisos());
			break;
		case GASTOS:
			actualizarPermisos(modulo,GasActions.toPermisos());
			break;
		case INVENTARIOS:
			//actualizarPermisos(InventariosActions.toPermisos());
			break;
		case CORE:
			actualizarPermisos(modulo,GlobalActions.toPermisos());
			break;
		case CXC:
			actualizarPermisos(modulo,CXCActions.toPermisos());
			break;
		default:
			break;
		}
	}
	

}
