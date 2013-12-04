package com.luxsoft.sw3.replica;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ReplicationMode;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.dao.core.ClienteDaoImpl;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.services.Services;

/**
 * Replicador especializado en la replicacion de Pagos con deposito
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReplicadorDeDepositos extends ReplicadorTemplate{
	
	
	/**
	 * Importa depositos de la fecha indicada utilizando replicacion de Hibernate
	 * a partir de la fecha del sistema
	 * 
	 * 
	 * @param sourceSucursal
	 * @param source
	 * @param target
	 */
	public void importar(Long sourceSucursal,HibernateTemplate source,HibernateTemplate target){
		final Date fecha=Services.getInstance().obtenerFechaDelSistema();
		importar(fecha,sourceSucursal,source,target);
	}
	
	/**
	 * Importa depositos de la fecha indicada utilizando replicacion de Hibernate
	 * 
	 * @param fecha
	 * @param sourceSucursal
	 * @param source
	 * @param target
	 */
	public void importar(final Date fecha,final Long sourceSucursal,HibernateTemplate source,HibernateTemplate target){
		String hql="from PagoConDeposito p where p.sucursal.id=? and p.fecha=? " +
				" and p.aplicado=0" +
				"";
		Object[] params={sourceSucursal,fecha};
		List<PagoConDeposito> depositos=source.find(hql, params);
		importar(depositos, source, target);
	}
	
	/**
	 * Importa una lista de depositos utilizando replicacion de Hibernate   
	 * 
	 * @param depositos
	 * @param source
	 * @param target
	 */
	public void importar(List<PagoConDeposito> depositos,HibernateTemplate source,HibernateTemplate target){
		for(PagoConDeposito pago:depositos){
			importar(pago, source, target);	
		}
	}
	
	/**
	 * 
	 * Importa un deposito utilizando {@link ReplicationMode.LATEST_VERSION}
	 * 
	 * @param pagoId El Id del deposito a replicar
	 * @param source
	 * @param target
	 */
	public void importar(String pagoId,HibernateTemplate source,HibernateTemplate target){
		PagoConDeposito pago=(PagoConDeposito)source.get(PagoConDeposito.class, pagoId);
		importar(pago,source,target);
	}
	
	/**
	 * Importa un deposito utilizando {@link ReplicationMode.LATEST_VERSION} 
	 * 
	 * @param pago
	 * @param source
	 * @param target
	 */
	public void importar(PagoConDeposito pago,HibernateTemplate source,HibernateTemplate target){
		try {
			//Verificar el cliente
			boolean res=validarCliente(pago.getCliente(), target);
			if(!res){
				logger.info("Imposible importar Deposito: "+pago);
			}
			pago.getAutorizacion();
			validarDuplicidad(pago,target);
			target.replicate(pago, ReplicationMode.LATEST_VERSION);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
		
	}
	
	public void refreshPendientes(final Long sourceSucursal,HibernateTemplate source,HibernateTemplate target){
		logger.info("Refresh para sucursal: "+sourceSucursal);
		String hql="from PagoConDeposito p where p.sucursal.id=? and p.fecha=? " +
				" and p.autorizacion=null" +
				"";
		Object[] params={sourceSucursal};
		List<PagoConDeposito> depositos=source.find(hql, params);
		for(PagoConDeposito pago:depositos){
			try {
				//logger.info("Deposito por refrescar: "+pago);
				PagoConDeposito found=(PagoConDeposito)target.get(PagoConDeposito.class, pago.getId());
				if(found!=null){
					if(found.getAutorizacion()!=null){
						source.replicate(found, ReplicationMode.OVERWRITE);
						logger.info("Deposito autorizado y refrescado: "+found);
					}else{
						pago.setComentario(found.getComentario());
						pago.setSalvoBuenCobro(found.getSalvoBuenCobro());
						source.update(pago);
						target.replicate(pago, ReplicationMode.LATEST_VERSION);
						logger.info("Deposito actualizado: "+found);
					}
					
				}else{
					importar(pago, source, target);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * A diferencia de la importacion este metodo busca registros ya importados y los actualiza (replica)
	 * 
	 * @param sourceSucursal
	 * @param source
	 * @param target
	 */
	public void refrescar(Long sourceSucursal,HibernateTemplate source,HibernateTemplate target){
		final Date fecha=Services.getInstance().obtenerFechaDelSistema();
		refrescar(fecha,sourceSucursal, source, target);
	}
	
	/**
	 * A diferencia de la importacion este metodo busca registros ya importados y los actualiza (replica)
	 * 
	 * @param fecha
	 * @param sourceSucursal
	 * @param source
	 * @param target
	 */
	public void refrescar(final Date fecha,final Long sourceSucursal,HibernateTemplate source,HibernateTemplate target){
		logger.info("Refresh para sucursal: "+sourceSucursal);
		String hql="from PagoConDeposito p where p.sucursal.id=? and p.fecha=? " +
				" and p.autorizacion=null" +
				"";
		Object[] params={sourceSucursal,fecha};
		List<PagoConDeposito> depositos=source.find(hql, params);
		for(PagoConDeposito pago:depositos){
			try {
				//logger.info("Deposito por refrescar: "+pago);
				PagoConDeposito found=(PagoConDeposito)target.get(PagoConDeposito.class, pago.getId());
				if(found!=null){
					if(found.getAutorizacion()!=null){
						source.replicate(found, ReplicationMode.OVERWRITE);
						logger.info("Deposito autorizado y refrescado: "+found);
					}else{
						pago.setComentario(found.getComentario());
						pago.setSalvoBuenCobro(found.getSalvoBuenCobro());
						source.update(pago);
						target.replicate(pago, ReplicationMode.LATEST_VERSION);
						logger.info("Deposito actualizado: "+found);
					}
					
				}else{
					importar(pago, source, target);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private boolean validarCliente(Cliente c,HibernateTemplate target){
		ClienteDaoImpl dao=new ClienteDaoImpl();
		dao.setHibernateTemplate(target);
		Cliente found=dao.get(c.getId());
		if(found==null){
			logger.info("Cliente no localizado por clave "+c.getClave()+ "Buscando por RFC ");
			found=dao.save(c);
			return true;
		}
		else
			return found.getNombre().equals(c.getNombre());
	}

	private void validarDuplicidad(PagoConDeposito deposito,HibernateTemplate target){
		logger.info("Validando duplicidad...");
		final Date fecha=deposito.getFechaDeposito();
		final String banco=deposito.getBanco();
		final Long ctaId=deposito.getCuenta().getId();
		final BigDecimal total=deposito.getTotal();
		final String claveCliente=deposito.getCliente().getClave();
		final String hql="from PagoConDeposito d " +
				" where d.fechaDeposito=?" +
				"  and d.nombre=?" +
				"  and d.banco=?" +
				"  and d.cuenta.id=?" +
				"  and d.total=?" +
				"  and d.autorizacion is null" +
				"";
		Boolean res=(Boolean)target.execute(new HibernateCallback(){

			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				List<PagoConDeposito> pagos=session.createQuery(hql)
				.setParameter(0, fecha,Hibernate.DATE)
				.setString(1,claveCliente)
				.setString(2, banco)
				.setLong(3,ctaId)
				.setBigDecimal(4, total)
				.list();
				return pagos.isEmpty()?Boolean.FALSE:Boolean.TRUE;
			}
			
		});
		if(res){
			String msg=MessageFormat.format(
					"Deposito duplicado:" +
					"\nFecha Deposito:\t{0,date,short}," +
					"\nNombre:\t{1} " +
					"\nBanco:\t{2}  " +
					"\nCuenta:\t{3}" +
					"\nITotal:\t{4}"
					, deposito.getFechaDeposito()
					,deposito.getNombre()
					,deposito.getBanco()					
					,deposito.getCuenta().getId()
					,deposito.getTotal()
					);
			throw new RuntimeException(msg);
		}	
	}
	
	
	
	public static void main(String[] args) {
		ReplicadorDeDepositos replicador=new ReplicadorDeDepositos();
		Long sucursalId=5L;
		//HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		//HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		
		//replicador.importar(2L, source, target);
		//replicador.importar("8a8a8488-267a9f0f-0126-7c36f712-001f", source, target);
		//replicador.regresarAutorizacion("8a8a8589-267012cf-0126-711b50b7-001f", target,source );
		//replicador.refrescar(sucursalId, source, target);
		
		
		Long[] sucursales={6L};
		
		HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		for(Long id:sucursales){
			HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(id);
			//replicador.refrescar(id, source, target);
			replicador.refrescar(DateUtil.toDate("06/02/2010"),id, source, target);
		}
		
	}
	

}
