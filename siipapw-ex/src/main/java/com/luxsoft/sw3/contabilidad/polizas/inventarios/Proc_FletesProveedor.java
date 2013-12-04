package com.luxsoft.sw3.contabilidad.polizas.inventarios;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.ui.ModelMap;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;
import ca.odell.glazedlists.UniqueList;

import com.luxsoft.siipap.cxp.model.AnalisisDeFactura;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.inventarios.model.Inventario;
import com.luxsoft.siipap.maquila.model.MovimientoConFlete;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;
import com.luxsoft.sw3.maquila.model.AnalisisDeFlete;

public class Proc_FletesProveedor implements IProcesador{

	public void procesar(final Poliza poliza, ModelMap model) {
		
		//final Periodo periodo=Periodo.getPeriodoDelMesActual(poliza.getFecha());
		
		final Periodo periodo=Periodo.getPeriodoEnUnMes(poliza.getFecha());
		
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				
				String hql="select distinct det.analisis.id from AnalisisDeFacturaDet det where date(det.entrada.fecha) between ? and ?";
				Set<Long> ids=new HashSet<Long>(session.createQuery(hql)
						.setParameter(0, periodo.getFechaInicial(),Hibernate.DATE)
						.setParameter(1, periodo.getFechaFinal(),Hibernate.DATE)
						.list());
				
				EventList<AnalisisDeFactura> eventList=new BasicEventList<AnalisisDeFactura>(0);
				GroupingList<AnalisisDeFactura> gList=new GroupingList<AnalisisDeFactura>(eventList,GlazedLists.beanPropertyComparator(AnalisisDeFactura.class, "primeraSucursal"));
				
				for(Long id:ids){
					AnalisisDeFactura a=(AnalisisDeFactura)session.load(AnalisisDeFactura.class, id);					
					eventList.add(a);
					
				}	
				for(List<AnalisisDeFactura> aList:gList){
					
					AnalisisDeFactura a=aList.get(0);
					
					String asiento="Fletes Proveedor (Compras)";
				//	String ref1="";//a.getFactura().getNombre();
				//	String ref1=a.getFactura().getNombre();
					String ref2=a.getPartidas().iterator().next().getEntrada().getSucursal().getNombre();
					String ref1=a.getPartidas().iterator().next().getEntrada().getProveedor().getNombre();
					
					String desc2="Fletes";
					BigDecimal importe=BigDecimal.ZERO;
					for(AnalisisDeFactura aa:aList){
						importe=importe.add(aa.getFactura().getFleteMN().amount());
						//String desc2=MessageFormat.format("Fac: {0}  {1,date, short}",a.getFactura().getDocumento(),a.getFactura().getFecha());
					}
					
					PolizaDetFactory.generarPolizaDet(poliza, "119", "IFLT01", false, importe, desc2, ref1, ref2, asiento);
					PolizaDetFactory.generarPolizaDet(poliza, "119", "INVF01", true, importe, desc2, ref1, ref2, asiento);
				}
				
				return null;
			}
		});
		
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				
				EventList<AnalisisDeFlete> analisis=GlazedLists.eventList(session.createQuery("from AnalisisDeFlete a where a.cxpFactura.fecha between ? and ?")
				.setParameter(0, periodo.getFechaInicial(),Hibernate.DATE)
				.setParameter(1, periodo.getFechaFinal(),Hibernate.DATE)
				.list());
				
				analisis=new UniqueList<AnalisisDeFlete>(analisis,GlazedLists.beanPropertyComparator(AnalisisDeFlete.class, "id"));
				
				Comparator c = GlazedLists.beanPropertyComparator(Inventario.class, "sucursal.id");
				EventList<Inventario> source=new BasicEventList<Inventario>(0);
				GroupingList<Inventario> movimientosPorSucursal=new GroupingList<Inventario>(source,c);
				
				for (AnalisisDeFlete a : analisis) {
					source.addAll(a.getTransformaciones());
					source.addAll(a.getTraslados());
					source.addAll(a.getComs());
					source.addAll(a.getEntradas());
				}
				
				for(List<Inventario> as:movimientosPorSucursal){
					String asiento="Fletes Proveedor (Análisis)";
					String ref1 ="";// fac.getNombre();
					String ref2 = as.get(0).getSucursal().getNombre();
					String desc2="Fletes";
					
					BigDecimal importePorSucursal = BigDecimal.ZERO;
					for (Inventario inv : as) {
						MovimientoConFlete ca=(MovimientoConFlete)inv;
						importePorSucursal = importePorSucursal.add(new BigDecimal(ca.getCantidad()/ca.getFactor()*ca.getCostoFlete().doubleValue()));
					}
					importePorSucursal = PolizaUtils.redondear(importePorSucursal);
					PolizaDetFactory.generarPolizaDet(poliza, "119", "IFLT01", false, importePorSucursal, desc2, ref1, ref2, asiento);
					PolizaDetFactory.generarPolizaDet(poliza, "119", "INVF01", true, importePorSucursal, desc2, ref1, ref2, asiento);
				}
				return null;				
			}
		});
		
	}
}
