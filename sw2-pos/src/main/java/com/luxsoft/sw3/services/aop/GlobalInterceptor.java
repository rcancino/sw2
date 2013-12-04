package com.luxsoft.sw3.services.aop;

import java.io.Serializable;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import com.luxsoft.siipap.inventarios.model.Inventario;


public class GlobalInterceptor extends EmptyInterceptor{

	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state,String[] propertyNames, Type[] types) {
		if(entity instanceof Inventario){
			System.out.println("Insertando inventario desde interceptor");
			((Inventario) entity).setCreateUser("ADMIN");
			return true;
		}
		
		return false;
	}
	
	

}
