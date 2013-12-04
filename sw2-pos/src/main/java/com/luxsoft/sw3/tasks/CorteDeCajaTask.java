package com.luxsoft.sw3.tasks;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.UniqueList;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoConCheque;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.cxc.model.PagoConEfectivo;
import com.luxsoft.siipap.cxc.model.PagoConTarjeta;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.caja.Caja;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;

public class CorteDeCajaTask {
	
	
	public Map<Caja.Tipo, BigDecimal> ejecutar(final Date fecha,final Sucursal sucursal){
		final Map<Caja.Tipo, BigDecimal> map=new HashMap<Caja.Tipo, BigDecimal>();
		
		Services.getInstance().getHibernateTemplate().execute(new HibernateCallback(){

			public Object doInHibernate(Session session)	throws HibernateException, SQLException {
				List<Aplicacion> aplicaciones=session.createQuery(
						"from Aplicacion a where a.cargo.sucursal.id=? " +
						" and a.fecha=?" +
						" and a.cargo.origen!=\'CRE\'" +
						" and a.abono.anticipo=false" 
						)
				.setParameter(0, sucursal.getId())
				.setParameter(1, fecha,Hibernate.DATE)
				.list();
				
				Comparator<Pago> c=GlazedLists.beanPropertyComparator(Pago.class, "id");
				UniqueList<Pago> pagos=new UniqueList<Pago>(new BasicEventList<Pago>(),c);
				for(Aplicacion a:aplicaciones){
					Abono abono=a.getAbono();
					if(abono instanceof Pago){
						Pago p=(Pago)abono;
						if(p.isAnticipo())
							continue;
						Date fechaAplica=a.getFecha();
						Date fechaAbono=p.getFecha();
						if(DateUtils.isSameDay(fechaAplica, fechaAbono)){
							pagos.add(p);
							continue;
						}						
						if(p.getAplicaciones().size()==1){
							pagos.add(p);
							continue;
						}
					}
				}
				List<Pago> anticipos=session.createQuery(
						"from Pago p where p.sucursal.id=?" +
						" and p.fecha=? " +
						" and p.origen!=\'CRE\'" +
						" and p.anticipo=true")
						.setParameter(0, sucursal.getId())
						.setParameter(1, fecha,Hibernate.DATE)
						.list();
				pagos.addAll(anticipos);
				
				CantidadMonetaria totalDeposito=CantidadMonetaria.pesos(0.0);
				CantidadMonetaria totalEfectivo=CantidadMonetaria.pesos(0.0);
				CantidadMonetaria totalCheque=CantidadMonetaria.pesos(0.0);
				CantidadMonetaria totalTarjeta=CantidadMonetaria.pesos(0.0);
				
				for(Pago p:pagos){
					if(p instanceof PagoConDeposito){
						totalDeposito=totalDeposito.add(p.getTotalCM());						
					}
					if(p instanceof PagoConCheque){
						totalCheque=totalCheque.add(p.getTotalCM());
					}
					if(p instanceof PagoConTarjeta){
						totalTarjeta=totalTarjeta.add(p.getTotalCM());
					}
					if(p instanceof PagoConEfectivo){
						totalEfectivo=totalEfectivo.add(p.getTotalCM());
					}
				}
				System.out.println("Total efectivo: "+totalEfectivo);
				map.put(Caja.Tipo.EFECTIVO, totalEfectivo.amount());
				System.out.println("Total cheque: "+totalCheque);
				map.put(Caja.Tipo.CHEQUE, totalCheque.amount());
				System.out.println("Total depositos: "+totalDeposito);
				map.put(Caja.Tipo.DEPOSITO,totalDeposito.amount());
				System.out.println("Total tarjeta: "+totalTarjeta);
				map.put(Caja.Tipo.TARJETA, totalTarjeta.amount());
				
				return null;
			}
			
		});
		return map;
	}
	
	public static void main(String[] args) {
		POSDBUtils.whereWeAre();
		CorteDeCajaTask task=new CorteDeCajaTask();
		final Date fecha=DateUtil.toDate("07/01/2010");
		final Sucursal sucursal=Services.getInstance().getConfiguracion().getSucursal();
		task.ejecutar(fecha,sucursal);
	}

}
