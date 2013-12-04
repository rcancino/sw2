package com.luxsoft.sw3.contabilidad.polizas.af;

import java.sql.SQLException;
import java.text.MessageFormat;

import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.gastos.ActivoFijo;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;

public class Proc_ActivoFijo implements IProcesador{

	public void procesar(final Poliza poliza, final ModelMap model) {
		final Periodo periodo=(Periodo)model.get("periodo");
		//System.out.println("Procesando poliza de af para: "+periodo);
		final HibernateTemplate template=(HibernateTemplate)model.get("hibernateTemplate");
		template.execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				int year=Periodo.obtenerYear(periodo.getFechaFinal());
				int mes=Periodo.obtenerMes(periodo.getFechaFinal())+1;
				String hql="from ActivoFijo a where  " +
						" year(a.fechaActualizacion)=?" +
						" and month(a.fechaActualizacion)=?" +
						" and a.venta is null" +
						" and a.depreciacionMensual>0";
				ScrollableResults rs=session.createQuery(hql)
						.setParameter(0,  year)
						.setParameter(1, mes)						
						.scroll();
				int buff=0;
				while(rs.next()){
					ActivoFijo af=(ActivoFijo)rs.get()[0];
					registrar(af, poliza);
					buff++;
					if(buff%20==0){
						session.flush();
						session.clear();
					}
				}
				return null;
			}
		});
		
	}
	
	private void registrar(ActivoFijo af,Poliza poliza){
		if(af.getDepreciacionDelEjercicio().doubleValue()>0){
			
			//Cargo a Gastos con abono al Activo
			String desc2=MessageFormat.format("Depreciación Activo: {0} Fac: {1} F.Fac{2,date,short}", af.getId(),af.getDocumento(),af.getFechaDeAdquisicion());
			
			String ref1=af.getProveedor()!=null?af.getProveedor().getNombreRazon():"SIN PROVEEDOR";
			String ref2=af.getSucursal().getNombre();
			PolizaDetFactory.generarPolizaDet(poliza, "600", af.getRubro().getId().toString(), true, af.getDepreciacionDelEjercicio(), desc2, ref1, ref2, "AF");
			PolizaDetFactory.generarPolizaDet(poliza, "121", af.getRubro().getId().toString(), false, af.getDepreciacionDelEjercicio(), desc2, ref1, ref2, "AF");
		}
		
	}

}
