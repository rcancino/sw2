package com.luxsoft.sw3.contabilidad.polizas.maquila;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.ui.ModelMap;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion.Concepto;
import com.luxsoft.siipap.cxp.model.CXPFactura;

import com.luxsoft.siipap.inventarios.model.Inventario;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.service.ServiceLocator2;

import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;
import com.luxsoft.sw3.maquila.model.AnalisisDeMaterial;
import com.luxsoft.sw3.maquila.model.EntradaDeMaterialDet;


public class Proc_EntradasAlMaquilador implements IProcesador {

	

	public void procesar(Poliza poliza, ModelMap model) {

		EventList<AnalisisDeMaterial> analisis = (EventList<AnalisisDeMaterial>) model.get("analisis");
		String asiento = "ENTRADAS MAQUILADOR";
		
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				
				return null;
			}
		});

		for (AnalisisDeMaterial a : analisis) {
			
			
			String pattern="Fac: {0} ({1,date,short})";
			String ref1=a.getNombre(); //"IMPAP, S.A DE C.V.";
			String ref2="";
			String desc2=MessageFormat.format(pattern, a.getFactura(),a.getFechaFactura());
			
			String almacen="";
			String concepto=null;
			
			for(EntradaDeMaterialDet det:a.getEntradas()){
				almacen=det.getAlmacen().getNombre();
				concepto=det.getAlmacen().getId().toString();
			}
			
			PolizaDetFactory.generarPolizaDet(poliza, "119",concepto, true, a.getImporte(), desc2+" "+almacen, ref1,ref2, asiento);
			PolizaDetFactory.generarPolizaDet(poliza, "117","IVAC02", true, a.getImpuesto(), desc2+" "+almacen, ref1,ref2, asiento);
			PolizaDetFactory.generarPolizaDet(poliza, "200",a.getClave(), false, a.getTotal(), desc2+" "+almacen, ref1,ref2, asiento);
			
			
		
		}
	}

}
