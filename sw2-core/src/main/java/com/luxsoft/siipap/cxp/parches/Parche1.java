package com.luxsoft.siipap.cxp.parches;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Currency;
import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.util.Assert;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.cxp.model.CXPAplicacion;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.cxp.model.CXPPago;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;

/**
 * Parche para pagar todas las facturas anteriores al 2009 en CxP
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class Parche1 {
	
	
	public void execute(){
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				String hql="from CXPFactura f where f.fecha<? order by f.id";
				ScrollableResults rs=session.createQuery(hql)
				.setParameter(0, DateUtil.toDate("01/01/2009"),Hibernate.DATE)
				.scroll();
				final EventList<CXPFactura> facturas=new BasicEventList<CXPFactura>();
				int buff=0;
				while(rs.next()){
					CXPFactura fac=(CXPFactura)rs.get()[0];
					fac.setSaldo(BigDecimal.ZERO);
					facturas.add(fac);
					System.out.println("Fac: "+fac.getId());
					if(buff++%20==0){
						session.flush();
						session.clear();
					}
				}
				
				//final Comparator<CXPFactura> c=GlazedLists.beanPropertyComparator(CXPFactura.class,"clave");
				GroupingList<CXPFactura> facturasGrup=new GroupingList<CXPFactura>(facturas,new MyComparator());
				
				for(List<CXPFactura> facProv:facturasGrup){
					//CXPFactura pivot=facProv.get(0);
					//System.out.println("Pago para proveedor: "+pivot.getProveedor().getNombreRazon()+ "Facturas: "+facProv.size());
					CXPPago pago=generarPago(facProv);
					System.out.println("Pago: "+pago);
					session.save(pago);
					session.flush();
					session.clear();
				}
				return null;
			}
			
		});
	}
	
	private CXPPago generarPago(List<CXPFactura> facturas){
		Proveedor p=facturas.get(0).getProveedor();
		Currency moneda=facturas.get(0).getMoneda();
		for(CXPFactura fac:facturas){
			Assert.isTrue(fac.getProveedor().equals(p),"No son facturas del mismo proveedor");
		}
		CXPPago pago=new CXPPago();
		pago.setProveedor(facturas.get(0).getProveedor());
		pago.setComentario("Pago automatico para carga inicial");
		pago.setDocumento("PA_"+moneda.toString()+"_"+p.getId());
		pago.setMoneda(moneda);
		Date fecha=DateUtil.toDate("31/12/2008");
		pago.setFecha(fecha);
		for(CXPFactura fac:facturas){
			CXPAplicacion aplic=new CXPAplicacion();
			aplic.setCargo(fac);
			aplic.setComentario("Aplicación automática por carga inicial");
			aplic.setImporte(fac.getSaldoCalculado());
			aplic.setFecha(fecha);
			pago.agregarAplicacion(aplic);
		}
		pago.setTotal(pago.getAplicadoCalculado());
		pago.setImporte(MonedasUtils.calcularImporteDelTotal(pago.getTotal()));
		pago.setImpuesto(MonedasUtils.calcularImpuesto(pago.getImporte()));
		return pago;
	}
	
	private class MyComparator implements Comparator<CXPFactura>{

		public int compare(CXPFactura o1, CXPFactura o2) {
			int res=o1.getProveedor().getClave().compareTo(o2.getProveedor().getClave());
			if(res==0)
				return o1.getMoneda().getCurrencyCode().compareTo(o2.getMoneda().getCurrencyCode());
			else 
				return
					res;
		}
		
	}
	
	public static void main(String[] args) {
		new Parche1().execute();
	}

}
