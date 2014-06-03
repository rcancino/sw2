package com.luxsoft.sw3.bi;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.runtime.parser.node.GetExecutor;
import org.hibernate.Hibernate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.inventarios.model.SolicitudDeTraslado;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.cfdi.model.CFDIClienteMails;
import com.luxsoft.utils.LoggerHelper;

public class ValidarCFDIClientesMail {
	
	protected Logger logger=LoggerHelper.getLogger();
	
	SimpleJdbcInsert insert;
	private boolean todo=false;
	
	protected Set<Long> sucursales=new HashSet<Long>();
	
	
	public ValidarCFDIClientesMail(){
		insert=new SimpleJdbcInsert(ServiceLocator2.getJdbcTemplate()).withTableName("sx_clientes_cfdi_mails");
		   
	}
	
	
	public void sincronizacionDeCfdiMails(){
		
		for(Long sucursalId:sucursales){
			
			System.out.println("Validando En la Sucursal: " +sucursalId);
			
			JdbcTemplate template=ConnectionServices.getInstance().getJdbcTemplate(sucursalId);
			
			String hql="from CFDIClienteMails m where m.log.creado<='2014/05/06'";
			List<CFDIClienteMails> mails=ServiceLocator2.getHibernateTemplate().find(hql);
			for(CFDIClienteMails mail:mails){
			
			String sql="SELECT * FROM sx_clientes_cfdi_mails where cliente_id=?";
			Object[] args=new Object[]{mail.getCliente().getId()};
			List<Map<String,Object>> rows=template.queryForList(sql,args);
			 for(Map<String, Object> row:rows){
				 if(!mail.getId().equals(row.get("CFD_ID"))){
					 System.out.println("Actualizando el cliente: " +mail.getCliente().getId());
					 
					 Object [] arguments=new Object[]{mail.getId(),mail.getCliente().getId()};
					 String update="UPDATE sx_clientes_cfdi_mails SET CFD_ID=? WHERE CLIENTE_ID=? ";
					int actualizado= template.update(update, arguments);
					if(actualizado==0){
						System.err.println("Actualizado");
					}
				 }
				 
				// System.err.println("Buscando Cliente "+mail.getCliente().getId()+""+ mail.getCliente().getNombre()+"--"+mail.getEmail1()+" Cliente  "+ row.get("CLIENTE_ID")+"--"+row.get("EMAIL1"));
			 }
			
		}
		
		System.out.println("Total a validar: "+mails.size());
		
	}
	
}
	public void enviarFaltantes(){
		for(Long sucursalId:sucursales){
System.out.println("Completando informacion en la Sucursal: " +sucursalId);
			
			JdbcTemplate template=ConnectionServices.getInstance().getJdbcTemplate(sucursalId);
			
			String hql="from CFDIClienteMails m where m.log.creado<='2014/05/06'";
			List<CFDIClienteMails> mails=ServiceLocator2.getHibernateTemplate().find(hql);
			for(CFDIClienteMails mail:mails){
				String sql="SELECT * FROM sx_clientes_cfdi_mails where cliente_id=?";
				Object[] args=new Object[]{mail.getCliente().getId()};
				List<Map<String,Object>> rows=template.queryForList(sql,args);
				
				if(rows.isEmpty() || rows.get(0)==null ){
					System.out.println("Enviando informacion para el cliente: "+ mail.getCliente().getNombre());
					Object[] arguments=new Object[]{mail.getId(),mail.getCliente().getId(),mail.getEmail1(),mail.getEmail2(),mail.getLog().getCreado(),mail.getLog().getCreateUser()
													,mail.getLog().getModificado(),mail.getLog().getUpdateUser()
													};
					String insert="INSERT INTO sx_clientes_cfdi_mails (CFD_ID,CLIENTE_ID,EMAIL1,EMAIL2,CREADO,CREADO_USR,MODIFICADO,MODIFICADO_USR,version)" +
								  "VALUES(?,?,?,?,?,?,?,?,1)";
					int enviado=template.update(insert, arguments);
					if(enviado!=0){
						System.err.println("Registro de mail Enviado a la sucursal");
					}
				}
				 
			}
		}
	}
	
	public void importarFaltantes(){
		for(Long sucursalId:sucursales){
			System.out.println("Buscando Faltantes para : " +sucursalId);
			JdbcTemplate template=ConnectionServices.getInstance().getJdbcTemplate(sucursalId);
			HibernateTemplate hibernate=ServiceLocator2.getHibernateTemplate();
			
			String sql="SELECT * FROM sx_clientes_cfdi_mails ";
			List<Map<String,Object>> rows=template.queryForList(sql);
			
			for(Map row:rows){
				//System.out.println("------------------"+row.get("CFD_ID"));
				Object [] args=new Object[]{row.get("CLIENTE_ID")};
				String hql="from CFDIClienteMails m  where m.cliente.id=? ";
				List<CFDIClienteMails> mail= hibernate.find(hql,args);
				
				//System.err.println("**"+mail.get(0).getCliente().getNombre());
				
				if(mail.isEmpty() || mail.get(0)==null){
					System.out.println("Falta el mail de este id: "+row.get("CFD_ID")+" Para este Cliente: "+row.get("CLIENTE_ID"));
				}
				
			}
			
			
		}
		
	}
	
	public void sincronizacionDeBasesDeDatos(){
		JdbcTemplate templateOfi=ServiceLocator2.getJdbcTemplate();
		HibernateTemplate hibernate=ServiceLocator2.getHibernateTemplate();
		
		String hql="from CFDIClienteMails m where m.log.creado<='2014/05/06'";
		List<CFDIClienteMails> mails=ServiceLocator2.getHibernateTemplate().find(hql);
		
		
		for(Long sucursalId:sucursales){
			System.out.println("sincronizando  : " +sucursalId);
			
			JdbcTemplate template=ConnectionServices.getInstance().getJdbcTemplate(sucursalId);
			
			for(CFDIClienteMails mail:mails){
				
				Object[] arguments= new Object[]{mail.getCliente().getId()};
				String sql="SELECT * FROM  sx_clientes_cfdi_mails WHERE CLIENTE_Id=?";
				Map<String, Object> fila=template.queryForMap(sql, arguments);
				
				if(!mail.getLog().getModificado().equals(fila.get("MODIFICADO"))){
					
					if(!mail.getEmail1().equals(fila.get("EMAIL1"))){
						
						if(!fila.get("EMAIL1").toString().contains("@")){
							if(mail.getEmail1().contains("@")){
								System.err.println("Sin formato de Correo");
								System.out.println("Por corregir: "+mail.getCliente().getNombre()+"---"+mail.getEmail1()+"--creado--"+mail.getLog().getModificado()+"///"+fila.get("EMAIL1")+" creado Suc"+fila.get("MODIFICADO") );
								String update="UPDATE sx_clientes_cfdi_mails SET EMAIL1=? , MODIFICADO=? WHERE CLIENTE_ID=? ";
								Object[] argumentos=new Object[] {mail.getEmail1(),mail.getLog().getModificado(),mail.getCliente().getId()};
								int actualizado= template.update(update, argumentos);
								if(actualizado!=0){
									System.err.println("Email1 Remplazado");
								}
							}
						}
						if(!mail.getEmail1().contains("@")){
							if(fila.get("EMAIL1").toString().contains("@")){
								System.err.println("Sin formato de Correo En Oficinas");
								System.out.println("Por corregir en Oficinas: "+mail.getCliente().getNombre()+"---"+mail.getEmail1()+"--creado--"+mail.getLog().getModificado()+"///"+fila.get("EMAIL1")+" creado Suc"+fila.get("MODIFICADO") );
								String update="UPDATE sx_clientes_cfdi_mails SET EMAIL1=? , MODIFICADO=? WHERE CLIENTE_ID=? ";
								Object[] argumentos=new Object[] {fila.get("EMAIL1"),fila.get("MODIFICADO"),mail.getCliente().getId()};
								int actualizado= templateOfi.update(update, argumentos);
								if(actualizado!=0){
									System.err.println("Email1 Remplazado en oficinas");
								}
							}
						}
						
						
						
					}
					
				}
			}
			
			
			
		}
		
	}
	
	public void igualarCorreos(){
		
	}
	

	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}
	
	public ValidarCFDIClientesMail addSucursal(Long...ids){
		sucursales.clear();
		for(Long id:ids){
			sucursales.add(id);
		}
		return this;
	}

	

	

	
	
	
	
	
	public static void main(String[] args) {
		new ValidarCFDIClientesMail()
		/*.addSucursal(6L)
		.sincronizacionDeCfdiMails();*/
		
		/*.addSucursal(9L)
		.enviarFaltantes();*/
		
		/*.addSucursal(2L)
		.importarFaltantes();*/
		
		.addSucursal(9L)
		.sincronizacionDeBasesDeDatos();
	}
	

}
