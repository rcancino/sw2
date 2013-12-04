package com.luxsoft.sw3.replica;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.ReplicationMode;
import org.springframework.orm.hibernate3.HibernateObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.utils.LoggerHelper;

/**
 * Replicador para comprobantes fiscales digitales
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReplicadorDeCFD {
	
	private Logger logger=LoggerHelper.getLogger();
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	
	/**
	 * Importa todos los CFD pendientes desde todas las sucursales 
	 * 
	 */
	public void importar(){
		for(Long sucursalId:getSucursales()){
			importar(sucursalId);
		}
	}
	
	
	/**
	 * Importa todos los  CFD  <b>PENDIENTES</b> de una sucursal a la base de datos central
	 * 
	 * @param sucursalId
	 */
	public void importar(Long sucursalId){
		HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		List<ComprobanteFiscal> pendientes=source
					.find("from ComprobanteFiscal c where c.importado is null ");
		if(pendientes.size()==0)
			return;
		logger.info("Registros CFD pendientes: "+pendientes.size());
		for(ComprobanteFiscal cfd:pendientes){
			try {
				cfd.setImportado(new Date());
				Services.getInstance().getHibernateTemplate().replicate(cfd, ReplicationMode.OVERWRITE);
				source.update(cfd);
				logger.info("Registro CFD importado: "+cfd);
			} catch (Exception e) {				
				logger.error("Error importando CFD: "+cfd
						+"  "+ExceptionUtils.getRootCauseMessage(e),e);				
			}		
		}
	}
	
	
	public void importarFaltantes(Periodo periodo){
		for(Long sucursalId:getSucursales()){
			importarFaltantes(sucursalId,periodo);
		}
	}
	
	public void importarFaltantes(Long sucursalId,Periodo periodo){
		importarFaltantes(sucursalId, periodo.getListaDeDias().toArray(new Date[0]));
	}
	
public void importarFaltantes(final Long sucursalId,Date...dias){
		
		HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		for(Date dia:dias){
			//String msg=MessageFormat.format("Importando CFD'S pendientes del" + dia + " para la sucursal: "+ sucursalId);
			System.out.println("Importando CFD'S pendientes del   " + dia + "   para la sucursal: "+ sucursalId);
			String hql="select v.id from ComprobanteFiscal v where date(v.log.creado)=? ";
			HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
			List<String> faltantes=new ArrayList<String>();
			List<String> ids=source.find(hql, new Object[]{dia});
			for(String id:ids){
				//System.out.println("Verificando ID: "+id);
				ComprobanteFiscal v=(ComprobanteFiscal)target.get(ComprobanteFiscal.class, id);
				if(v==null){
					System.out.println("Faltante ID: "+id);
					faltantes.add(id);
				}
			}
			System.out.println("Faltantes detectados: "+faltantes.size());
			importar(sucursalId, source, target, faltantes.toArray(new String[0]));
		}
	}

public synchronized void importar(final Long sucursalId
		,final HibernateTemplate source
		,final HibernateTemplate target,String...ids){
	for(String id:ids){
		try {
			importar(id,sucursalId,source,target);
		} catch (Exception e) {
			logger.error(ExceptionUtils.getRootCause(e),e);
		}
		
	}
}

	

public synchronized void importar(final String id
		,final Long sucursalId
		,final HibernateTemplate source
		,final HibernateTemplate target){
		String hql="from ComprobanteFiscal v  where v.id=?";
	List<ComprobanteFiscal> cfds=(List<ComprobanteFiscal>) source.find(hql, id);
		 
	      ComprobanteFiscal cfd=cfds.get(0);
		
		try {
			cfd.setImportado(new Date());
			
				target.replicate(cfd, ReplicationMode.OVERWRITE);	
				
			logger.info("CFD replicado: "+cfd.getId());
			
		} catch (Exception e) {
			//e.printStackTrace();				
			if(e instanceof HibernateObjectRetrievalFailureException){
				logger.info("Hibernate Optimistic lock exception.. Permanecera pendiente");
			}
			logger.error("Error replicando cfd: "+cfd.getId()+"  "+" Sucursal: "+sucursalId+"  "+ ExceptionUtils.getRootCauseMessage(e));
		}
	
}
	
	
	
	
	
	
	public ReplicadorDeCFD addSucursal(Long... sucursales){
		for (Long sucursalId:sucursales){
			getSucursales().add(sucursalId);
		}
		return this;
	}
	
	public Set<Long> getSucursales() {
		return sucursales;
	}

	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}
	
	public static void main(String[] args) {
		POSDBUtils.whereWeAre();
		ReplicadorDeCFD replicador=new ReplicadorDeCFD();
		replicador.addSucursal(2L,3L,5L,6L,9L);		
		//replicador.importar();
		//replicador.addSucursal(6L);
		replicador.importarFaltantes(new Periodo("01/06/2012","02/06/2012"));
		
		
	}

}
