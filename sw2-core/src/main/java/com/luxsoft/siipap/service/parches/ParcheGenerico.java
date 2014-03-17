package com.luxsoft.siipap.service.parches;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.gastos.GProductoServicio;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.tesoreria.model.CorteDeTarjeta;
import com.luxsoft.sw3.tesoreria.model.CorteDeTarjetaDet;

public class ParcheGenerico extends HibernateDaoSupport{
	
	public static void find(){
		DBUtils.whereWeAre();
		ServiceLocator2.getJdbcTemplate().setMaxRows(1000);
		
		List res=ServiceLocator2.getJdbcTemplate().queryForList(
				//"");
				
		"select * from sw_trequisicion where date(fecha)>=\'2014-03-1\' order by fecha desc ");
		//CARGOABONO_ID=970640
		int row=0;
		for(Object o:res){
			System.out.println("ROW:"+(row++)+" "+o);
		}
		System.out.println("Rows_ "+res.size());
	}
	
	/*
	public void corregir(){
		
		final Long newId=163332l;
		
		getHibernateTemplate().execute(new HibernateCallback(){

			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				String hql="from GCompra c where c.proveedor.id=?";
				ScrollableResults rs=session.createQuery(hql).setLong(0, newId)
				.scroll();
				
				while(rs.next()){
					GCompra com=(GCompra)rs.get()[0];
					//System.out.println(com);
					for(GFacturaPorCompra fac:com.getFacturas()){
						//fac.setProveedor(com.getProveedor().getNombreRazon());
							if(fac.getRequisiciondet()!=null){
								System.out.println(fac.getRequisiciondet().getRequisicion().getAfavor());
								Requisicion r=fac.getRequisiciondet().getRequisicion();
								//r.setAfavor(com.getProveedor().getNombreRazon());
								if(fac.getRequisiciondet().getRequisicion().getPago()!=null){
									System.out.println("Pago:" +fac.getRequisiciondet().getRequisicion().getPago().getAFavor());
									//CargoAbono ca=fac.getRequisiciondet().getRequisicion().getPago();
									//ca.setAFavor(com.getProveedor().getNombreRazon());
								}
							}
					}
				}
				return null;
			}
			
		});
	}
	*/
	
	/**
	 * Actualiza la fecha de la requisicion a partir de la fecha de la compra para operaciones anteriores
	 * al 01/07/2008
	 
	public void corregir(){
		
		getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				String hql="from GCompra c where c.fecha<?";
				ScrollableResults rs=session.createQuery(hql).setParameter(0, DateUtil.toDate("01/07/2008"),Hibernate.DATE)
				.scroll();
				while(rs.next()){
					GCompra com=(GCompra)rs.get()[0];
					
					for(GFacturaPorCompra fac:com.getFacturas()){
						for(RequisicionDe re:fac.getRequisiciones()){
							Date reqDate=re.getRequisicion().getFecha();
							Date fecCompra=com.getFecha();
							Periodo perCompra=Periodo.getPeriodoEnUnMes(fecCompra);
							if(!perCompra.isBetween(reqDate)){
								System.out.println("Compra: "+com.getId()+" Fecha: "+com.getFecha()+" Req: "+re.getRequisicion().getId()+" Fecha: "+reqDate);
								//re.getRequisicion().setFecha(com.getFecha());
							}
						
						}
					}
						
						
						
				}
				return null;
			}
			
		});
	}*/
	
	public void corregir(){
		getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				String hql="from GProductoServicio p";
				ScrollableResults rs=session.createQuery(hql)
				.scroll();
				while(rs.next()){
					GProductoServicio prod=(GProductoServicio)rs.get()[0];	
						System.out.println(prod);
				}
				return null;
			}
			
		});
	}
	
	public static void probarRedondeo(){
		CantidadMonetaria base=CantidadMonetaria.pesos(4474.81);
		double factor=1.46d;
		CantidadMonetaria res=base.multiply(factor).divide(100);
		System.out.println("Resultado: "+res);
		CorteDeTarjeta corte=new CorteDeTarjeta();
		corte.setTipoDeTarjeta(CorteDeTarjeta.TIPOS_DE_TARJETAS[0]);
		
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				CantidadMonetaria comisionDebito=CantidadMonetaria.pesos(0);
				CantidadMonetaria comisionCredito=CantidadMonetaria.pesos(0);
				CorteDeTarjeta corte=(CorteDeTarjeta)session.get(CorteDeTarjeta.class, 999L);
				double debito=1.46d;
				double credito=2.36d;
				if(corte.getTipoDeTarjeta().equals(CorteDeTarjeta.TIPOS_DE_TARJETAS[0])){
					
					for(CorteDeTarjetaDet det:corte.getPartidas()){
						//double comision=det.getPago().getComisionBancaria();
						//CantidadMonetaria imp=det.getPago().getTotalCM().multiply(comision/-100);
						CantidadMonetaria imp=det.getPago().getTotalCM();
						if(det.getPago().getTarjeta().isDebito()){
							comisionDebito=comisionDebito.add(imp);
						}else{
							comisionCredito=comisionCredito.add(imp);
						}
							
					}
				}
				comisionDebito=comisionDebito.multiply(debito/-100);
				comisionCredito=comisionCredito.multiply(credito/-100);
				System.out.println("DEBITO: "+comisionDebito);
				System.out.println("CREDITO: "+comisionCredito);
				return null;
			}
			
		});
		
		
	}
	
	public static void main(String[] args) {
		//ParcheGenerico parche=new ParcheGenerico();
		//parche.setSessionFactory(ServiceLocator2.getSessionFactory());
		//parche.corregir();
		find();
		//probarRedondeo();
	}

}
