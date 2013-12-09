package com.luxsoft.sw3.cfdi;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfdi.model.CFDI;

@Service("cfdiManager")
public class CFDIManager {
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	@Autowired
	private IFactura iFactura;
	
	public CFDI getCFDI(String id){
		return (CFDI)hibernateTemplate.get(CFDI.class, id);
	}
	
	public CFDI buscarCFDI(Venta venta){
		List<CFDI> res=hibernateTemplate.find("from CFDI c where c.origen=?",venta.getId());
		return res.isEmpty()?res.get(0):null;
	}
	
	public CFDI generarFactura(Venta venta){
		return getiFactura().generar(venta);
		
	}
	
	public CFDI timbrar(CFDI cfdi){
		throw new NotImplementedException("Aun no esta listo el servicio de timbrado");
	}

	public IFactura getiFactura() {
		return iFactura;
	}

	public void setiFactura(IFactura iFactura) {
		this.iFactura = iFactura;
	}

	
	
	
	

}
