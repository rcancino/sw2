package com.luxsoft.siipap.cxp.service;

import com.luxsoft.siipap.compras.dao.EntradaPorCompraDao;
import com.luxsoft.siipap.compras.dao.ListaDePreciosDao;
import com.luxsoft.siipap.service.ServiceLocator2;

public class CXPServiceLocator {
	
	private static CXPServiceLocator INSTANCE;
	
	public static synchronized CXPServiceLocator getInstance(){
		if(INSTANCE==null){
			INSTANCE=new CXPServiceLocator();
		}
		return INSTANCE;
	}
	
	private CXPServiceLocator(){}
	
	public CXPManager getCXPManager(){
		return(CXPManager)ServiceLocator2.instance().getContext()
		.getBean("cxpManager");
	}
	
	public ContraReciboManager getRecibosManager(){
		return(ContraReciboManager)ServiceLocator2.instance().getContext()
		.getBean("contraReciboManager");
	}
	
	public EntradaPorCompraDao getEntradaPorCompraDao(){
		return (EntradaPorCompraDao)ServiceLocator2.instance().getContext()
		.getBean("entradaPorCompraDao");
	}
	
	public ListaDePreciosDao getListaDePreciosDao(){
		return ServiceLocator2.getListaDePreciosDao();
	}
	
	public FacturaManager getFacturasManager(){
		return (FacturaManager)ServiceLocator2.instance().getContext().getBean("cxpFacturaManager");
	}
	
	public CXPAbonosManager getAbonosManager(){
		return (CXPAbonosManager)ServiceLocator2.instance().getContext().getBean("cxpAbonosManager");
	}

}
