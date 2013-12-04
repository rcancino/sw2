package com.luxsoft.siipap.service.parches;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.gastos.GProveedor;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.service.ServiceLocator2;

public class CambioDeProveedorDesdeOrdenDeCompra extends HibernateDaoSupport{
	
	
	public void actualizarProveedor(final Long compraId,final Long proveedorId){
		
		getHibernateTemplate().execute(new HibernateCallback(){

			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				GCompra compra=(GCompra)session.get(GCompra.class, compraId);
				GProveedor prov=(GProveedor)session.get(GProveedor.class, proveedorId);
				//System.out.println("Asignando proveedor a la compra:"+compra);
				compra.setProveedor(prov);
				if(!compra.getFacturas().isEmpty()){
					GFacturaPorCompra factura=compra.getFacturas().iterator().next();
					//System.out.println("Re-Asignando proveedor a factura: "+factura);
					factura.setProveedor(prov.getNombreRazon());
					if(!factura.getRequisiciones().isEmpty()){
						for(RequisicionDe de:factura.getRequisiciones()){
							
							//System.out.println("Re-Asignando proveedor a requisicion: "+de.getRequisicion());
							de.getRequisicion().setAfavor(prov.getNombreRazon());
						}
					}
				}
				return null;
			}
			
		});
	}
	
	
	
	public static void main(String[] args) {
		CambioDeProveedorDesdeOrdenDeCompra parche=new CambioDeProveedorDesdeOrdenDeCompra();
		parche.setSessionFactory(ServiceLocator2.getSessionFactory());
		parche.actualizarProveedor(385221L, 163378L);
	}

}
