package com.luxsoft.sw3.tasks.parches;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.AplicacionDePago;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.Services;

public class GenerarAplicacionFaltante {
	
	public static void execute(){
		
		Services.getInstance().getHibernateTemplate().execute(new HibernateCallback(){

			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				String pagoId="8a8a81ee-25953588-0125-956278c2-0006";
				String ventaId="8a8a8189-25959b90-0125-95a3ec3a-071e";
				Pago pago=(Pago)session.get(Pago.class, pagoId);
				System.out.println("Disponible: "+pago.getDisponible());
				
				Venta v=(Venta)session.get(Venta.class, ventaId);
				System.out.println("Saldo Venta: "+v.getSaldoCalculado());
				
				Aplicacion a=new AplicacionDePago();
				a.setAbono(pago);
				a.setCargo(v);
				a.setFecha(DateUtil.toDate("15/12/2009"));
				a.setImporte(pago.getDisponible());
				a.actualizarDetalle();
				pago.agregarAplicacion(a);
				session.merge(pago);
				return null;
			}
			
		});
	}
	
	public static void main(String[] args) {
		execute();
	}

}
