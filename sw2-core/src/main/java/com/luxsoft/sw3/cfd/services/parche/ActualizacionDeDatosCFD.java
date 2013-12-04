package com.luxsoft.sw3.cfd.services.parche;

import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;

public class ActualizacionDeDatosCFD {
	
	/**
	 * Actualiza un comprobante con creado nulo colocando la fecha del XML en el campo
	 * 
	 * @param cfdId
	 */
	public static void actualizarCredo(final String cfdId){		
		ComprobanteFiscal cf=(ComprobanteFiscal)ServiceLocator2.getHibernateTemplate().get(ComprobanteFiscal.class, cfdId);
		cf.loadComprobante();
		cf.getLog().setCreado(cf.getComprobante().getFecha().getTime());
		cf.getLog().setModificado(cf.getComprobante().getFecha().getTime());
		ServiceLocator2.getHibernateTemplate().update(cf);
	}
	
	/**
	 * Actualiza todos los comprobantes con creado nulo colocando la fecha del XML en el campo 
	 */
	public static void actualizarCreado(){
		List<String> pendientes=ServiceLocator2.getJdbcTemplate()
		.queryForList("select CFD_ID from SX_CFD where CREADO is null", String.class);
		System.out.println("CFD a corregir: "+pendientes.size());
		int total=pendientes.size();
		int count=1;
		for(String id:pendientes){			
			actualizarCredo(id);
			System.out.println("Actualizado: "+id+ count +" de "+total);
			count++;
		}
	}
	
	/**
	 *  Actualiza datos de el comprobante a partir del archivo XML 
	 */
	public static void actualizarDatos(){
		List<String> pendientes=ServiceLocator2.getJdbcTemplate()
			.queryForList("select CFD_ID from SX_CFD where RFC is null "
				, String.class);
		System.out.println("CFD a corregir: "+pendientes.size());
		int total=pendientes.size();
		int count=1;
		for(String id:pendientes){	
			try {
				actualizarDatos(id);
			} catch (Exception e) {
				System.out.println("Error: "+ExceptionUtils.getRootCauseMessage(e)+" CFD: "+id);
			}
			System.out.println("Actualizado: "+id+"  "+ count +" de "+total);
			count++;
		}
	}
	
	/**
	 * Actualiza datos de el comprobante a partir del archivo XML 
	 * 
	 * @param cfdId
	 */
	public static void actualizarDatos(final String cfdId){		
		ComprobanteFiscal cf=(ComprobanteFiscal)ServiceLocator2.getHibernateTemplate().get(ComprobanteFiscal.class, cfdId);
		cf.loadComprobante();
		cf.setTotal(cf.getComprobante().getTotal());
		cf.setImpuesto(cf.getComprobante().getImpuestos().getTotalImpuestosTrasladados());
		String rfc=cf.getComprobante().getReceptor().getRfc();
		
		cf.setRfc(cf.getComprobante().getReceptor().getRfc());
		cf.setTipoCfd(StringUtils.substring(
				cf.getComprobante().getTipoDeComprobante().toString(),0,1).toUpperCase());
		
		ServiceLocator2.getHibernateTemplate().update(cf);
	}
	
	public static void actualizarDatosDeVentas(){
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				String hql="from ComprobanteFiscal cf where month(creado)=? and year(creado)=?";
				return null;
			}
			
		});
	}
	
	public static void verificarFecha(){
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				ScrollableResults rs=session.createQuery(
						"from ComprobanteFiscal cf order by cf.log.creado desc").scroll();
				int buff=0;
				while(rs.next()){
					ComprobanteFiscal cfd=(ComprobanteFiscal)rs.get()[0];
					
					try {
						cfd.loadComprobante();
						Date fechaXml=cfd.getComprobante().getFecha().getTime();
						Date fechaCfd=cfd.getLog().getCreado();
						if(!DateUtils.isSameDay(fechaXml, fechaCfd)){
							String msg=MessageFormat.format("CFD con dif en fecha:{0} XML Fecha: {1,date,short} Fecha CFD: {2,date,short}", cfd,fechaXml,fechaCfd);
							System.out.println(msg);							
						}
					} catch (Exception e) {
						System.out.println("Error: "+ExceptionUtils.getRootCause(e).getClass().getName()+ " : "+ExceptionUtils.getRootCauseMessage(e));
						continue;
					}
					
					
					buff++;
					if(buff%20==0){
						session.flush();
						session.clear();
						//System.out.println("\n");
					}
				}
				return null;
			}
			
		});
		
	}
	
	public static void main(String[] args) {
		//actualizarCredo("8a8a81e7-2c179ab4-012c-179e6e9f-000d");
		
		DBUtils.whereWeAre();
		//actualizarDatos();
		//actualizarCreado();
		verificarFecha();
	}

}
