package com.luxsoft.sw3.ventas.dao;

//import org.springframework.stereotype.Service;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.sw3.ventas.Factura;

//@Service("facturaDao")
public class FacturaDaoImpl extends GenericDaoHibernate<Factura, String> implements FacturaDao{

	public FacturaDaoImpl() {
		super(Factura.class);		
	}

}
