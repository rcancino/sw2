package com.luxsoft.cfdi;

import com.luxsoft.sw3.cfdi.CFDITasks;
import com.luxsoft.sw3.services.Services;

public class InicializarEntidades {
	
	public static void inicializar() throws Exception{
		CFDITasks tasks=new CFDITasks(Services.getInstance().getHibernateTemplate());
		//tasks.subirCertificadoPfx();
	}
	
	
	public static void main(String[] args) throws Exception{
		inicializar();
	}

}
