package com.luxsoft.sw3.contabilidad.ui.consultas2;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.gastos.operaciones.OCompraForm;
import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.ControladorDinamico;


public class PolizaDeProvisionesContablesPanel extends PolizaDinamicaPanel{

	public PolizaDeProvisionesContablesPanel(ControladorDinamico controller) {
		super(controller);
		
	}
	
	@Override
	protected void afterGridCreated() {
		
		super.afterGridCreated();
		grid.getColumnExt("Id").setVisible(false);
		grid.getColumnExt("Clase").setVisible(false);
	}

	@Override
	protected void doSelect(Object bean) {		
		Poliza poliza=(Poliza)getSelectedObject();		
		if(poliza!=null){			
			final Long id=new Long(poliza.getReferencia());
			GCompra compra=findCompra(id);
			if(compra!=null){
				OCompraForm.showForm(compra, true);
			}
			
		}
	}
	
	
	
	private GCompra findCompra(final Long pagoId){
		return (GCompra)getHibernateTemplate().execute(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				GCompra compra=(GCompra)session.get(GCompra.class, pagoId);				
				compra.getPartidas().iterator().next();				
				return compra;
			}
		});
	}
	
	
	
	
}
