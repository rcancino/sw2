package com.luxsoft.sw3.maquila.ui.forms;

import java.util.List;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.maquila.model.Almacen;

public final class MaquilaLookup {
	
	private static MaquilaLookup INSTANCE;
	
	private List<Almacen> almacenes;
	
	private MaquilaLookup(){}
	
	
	@SuppressWarnings("unchecked")
	public List<Almacen> getAlmacenes(){
		if(almacenes==null){
			almacenes=ServiceLocator2.getHibernateTemplate().find("from Almacen a");
		}
		return almacenes;
	}
	
	public static MaquilaLookup getInstance(){
		if(INSTANCE==null){
			INSTANCE=new MaquilaLookup();
		}
		return INSTANCE;
	}

}
