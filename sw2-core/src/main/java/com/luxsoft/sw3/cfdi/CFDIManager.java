package com.luxsoft.sw3.cfdi;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

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
	
	@Autowired
	private CFDITimbrador cfdiTimbrador;
	
	public CFDI getCFDI(String id){
		return (CFDI)hibernateTemplate.get(CFDI.class, id);
	}
	
	public CFDI buscarCFDI(Venta venta){
		List<CFDI> res=hibernateTemplate.find("from CFDI c where c.origen=?",venta.getId());
		Assert.notEmpty(res,"No localizo el CFDI origen: "+venta.getId());
		return res.get(0);
	}
	
	public CFDI generarFactura(Venta venta){
		return getiFactura().generar(venta);
		
	}
	
	public CFDI timbrar(CFDI cfdi) throws Exception{
		return cfdiTimbrador.timbrar(cfdi);
	}
	
	@Transactional(propagation=Propagation.SUPPORTS)
	public CFDI cancelar(CFDI cfdi) throws Exception{
		throw new RuntimeException("PENDIENTE DE IMPLEMENTAR");
	}

	public IFactura getiFactura() {
		return iFactura;
	}

	public void setiFactura(IFactura iFactura) {
		this.iFactura = iFactura;
	}

	public void setCfdiTimbrador(CFDITimbrador cfdiTimbrador) {
		this.cfdiTimbrador = cfdiTimbrador;
	}
	
	
	

}
