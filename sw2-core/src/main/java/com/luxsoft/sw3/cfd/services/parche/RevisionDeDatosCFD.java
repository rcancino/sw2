package com.luxsoft.sw3.cfd.services.parche;

import java.sql.SQLException;

import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;

public class RevisionDeDatosCFD {
	
	/**
	 * Verifica los XML del periodo 
	 *  
	 */
	public static void verificar(final Periodo periodo){
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				ScrollableResults rs=session.createQuery("from ComprobanteFiscal cf where date(cf.log.creado) between ? and ? order by folio")
					.setDate(0, periodo.getFechaInicial())
					.setDate(1, periodo.getFechaFinal())
					.scroll();
				while(rs.next()){
					ComprobanteFiscal cf=(ComprobanteFiscal)rs.get()[0];
					cf.loadComprobante();
					Comprobante cfd=cf.getComprobante();
					Venta v=(Venta)session.load(Venta.class, cf.getOrigen());
					//System.out.println("Verificando folio: "+cfd.getFolio()+" Tipo:"+v.getOrigen()+  " Sucursal: "+v.getSucursal());
					//Validar Total e Impuesto
					if(!v.getTotal().equals(cfd.getTotal())){
						if(v.getTotal().doubleValue()!=0)
							if(!v.getCliente().getClave().equals("1"))
							System.out.println("No cuadra el Total en el CFD: "+cf.getId()+ " Total CFD: "+cfd.getTotal()+ " Total Venta: "+v.getTotal());
					}
					if(!v.getImpuesto().equals(cfd.getImpuestos().getTotalImpuestosTrasladados())){
						if(v.getImpuesto().doubleValue()!=0)
							if(!v.getCliente().getClave().equals("1"))
								System.out.println("No cuadra el Impuesto en el CFD: "+cf.getId()+ "Iva CFD:"+cfd.getImpuestos().getTotalImpuestosTrasladados()+" Iva Venta: "+v.getImpuesto());
					}
					System.out.print(".");
				}
				return null;
			}			
		});
	}
	
	public static void main(String[] args) {
		//actualizarCredo("8a8a81e7-2c179ab4-012c-179e6e9f-000d");
		//actualizarCreado();
		DBUtils.whereWeAre();
		verificar(new Periodo("01/11/2010","30/11/2010"));
	}

}
