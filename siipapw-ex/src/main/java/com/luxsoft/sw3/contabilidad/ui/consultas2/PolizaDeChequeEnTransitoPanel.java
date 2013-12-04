package com.luxsoft.sw3.contabilidad.ui.consultas2;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.gastos.operaciones.OCompraForm;
import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.polizas.ControladorDinamico;


public class PolizaDeChequeEnTransitoPanel extends PolizaDinamicaPanel{

	public PolizaDeChequeEnTransitoPanel(ControladorDinamico controller) {
		super(controller);
		
	}
	
	@Override
	public void drill(PolizaDet det) {
		doSelect(getSelectedObject());
	}
	
	@Override
	protected void doSelect(Object bean) {
		Poliza poliza=(Poliza)getSelectedObject();
		if(poliza!=null){
			final Long pagoId=new Long(poliza.getReferencia());
			System.out.println("Buscando comprra de gastos para cargoabono: "+pagoId);
			GCompra compra=findCompra(pagoId);
			if(compra!=null){
				OCompraForm.showForm(compra, true);
			}
		}
	}
	
	private GCompra findCompra(final Long pagoId){
		return (GCompra)getHibernateTemplate().execute(new HibernateCallback() {
			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				CargoAbono c=(CargoAbono)session.get(CargoAbono.class, pagoId);
				Requisicion requisicion=c.getRequisicion();
				GFacturaPorCompra factura=requisicion
								.getPartidas().iterator().next()
								.getFacturaDeGasto();
				GCompra compra=factura.getCompra();
				compra.getPartidas().iterator().next();				
				return compra;
			}
		});
	}

	
}
