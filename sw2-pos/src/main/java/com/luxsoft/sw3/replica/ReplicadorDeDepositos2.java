package com.luxsoft.sw3.replica;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ReplicationMode;
import org.hibernate.Session;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.services.Services;

/**
 * Replicador especializado en la replicacion de Pagos con deposito
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReplicadorDeDepositos2 extends ReplicadorTemplate{
	
	
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
		System.out.println("Importando pago: "+pago);
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
			if(StringUtils.isBlank(pago.getLog().getCreateUser())){
				pago.setComentario("FALTA USUARIO QUE SOLICITA");
				source.merge(pago);
				return;
			}
			if(StringUtils.isBlank(pago.getBanco()) 
					|| (pago.getCuenta()==null)
					||(pago.getFechaDeposito()==null)){
				pago.setComentario("FALTA BANCO y/o CUENTA DEST y/o FECHA DEP");
				source.update(pago);
				return;
			}
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
		for(PagoConDeposito original:depositos){
			try {
				//logger.info("Deposito por refrescar: "+pago);
				if(original.isCancelado())
					continue;
				PagoConDeposito importado=(PagoConDeposito)target.get(PagoConDeposito.class, original.getId());
				if(importado!=null){
					if(importado.getAutorizacion()!=null){
						importado.setComentario("AUTORIZADO");
						source.replicate(importado, ReplicationMode.OVERWRITE);
						target.update(importado);						
						logger.info("Deposito autorizado y refrescado: "+importado);
					}else{
						boolean actualizarOriginal=false;
						if(!StringUtils.equalsIgnoreCase(importado.getComentario(),original.getComentario())){
							original.setComentario(importado.getComentario());
							actualizarOriginal=true;
							
						}if(importado.getSalvoBuenCobro().equals(original.getSalvoBuenCobro())){
							original.setSalvoBuenCobro(importado.getSalvoBuenCobro());
							actualizarOriginal=true;
						}
						if(importado.isCancelado()){
							source.replicate(importado, ReplicationMode.OVERWRITE);
							continue;
						}
						if(actualizarOriginal){
							source.update(original);
							logger.info("Deposito original actualizado: "+original);
							continue;
						}
						original.setComentario("");
						target.replicate(original, ReplicationMode.OVERWRITE);
						logger.info("Deposito original replicado: "+original);
						
					}
					
				}else{
					importar(original, source, target);
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
		//final Date fecha=Services.getInstance().obtenerFechaDelSistema();
		Date fecha=DateUtil.toDate("01/02/2010");
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
	public synchronized void refrescar(final Date fecha,final Long sourceSucursal,HibernateTemplate source,HibernateTemplate target){
		logger.info("Refresh para sucursal: "+sourceSucursal);
		String hql="from PagoConDeposito p where p.sucursal.id=? and p.fecha>=? " +
				" and p.autorizacion=null" +
				"";
		Object[] params={sourceSucursal,fecha};
		List<PagoConDeposito> depositos=source.find(hql, params);
		logger.info("Depositos a refrescar: "+depositos.size());
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
						target.replicate(pago, ReplicationMode.LATEST_VERSION);
						source.update(pago);
						logger.info("Deposito actualizado: "+found);
					}
					
				}else{
					importar(pago, source, target);
				}
			} catch (Exception e) {
				logger.error("Error replicando deposito",e);
				
			}
		}
	}
	
	private boolean validarCliente(Cliente c,HibernateTemplate target){
		//ClienteDaoImpl dao=new ClienteDaoImpl();
		//dao.setHibernateTemplate(target);
		Cliente found=(Cliente)target.get(Cliente.class, c.getId());//dao.get(c.getId());
		if(found==null){
			logger.info("Cliente no localizado por clave "+c.getClave()+ "Buscando por RFC ");
			//found=dao.save(c);
			target.replicate(c, ReplicationMode.OVERWRITE);
			return true;
		}
		else
			return found.getNombre().equals(c.getNombre());
	}

	private void validarDuplicidad(final PagoConDeposito deposito,HibernateTemplate target){
		logger.info("Validando duplicidad...");
		final String banco=deposito.getBanco();
		final Date fecha=deposito.getFechaDeposito();
		final Long ctaId=deposito.getCuenta().getId();
		final BigDecimal total=deposito.getTotal();
		final String claveCliente=deposito.getCliente().getClave();
		final String hql="from PagoConDeposito d " +
				" where d.fechaDeposito=?" +
				//"  and d.nombre=?" +
				"  and d.banco=?" +
				"  and d.cuenta.id=?" +
				"  and d.total=?" +
				//"  and d.autorizacion is null" +
				"";
		target.execute(new HibernateCallback(){

			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				List<PagoConDeposito> pagos=session.createQuery(hql)
				.setParameter(0, fecha,Hibernate.DATE)
				//.setString(1,claveCliente)
				.setString(1, banco)
				.setLong(2,ctaId)
				.setBigDecimal(3, total)
				.list();
				if(!pagos.isEmpty()){
					PagoConDeposito p=pagos.get(0);
					String pattern="Folio:{0} Suc:{1} Fecha:{2,date,short}";
					deposito.setComentario("Duplicado con: "
							+MessageFormat.format(pattern, p.getFolio(),p.getSucursal().getNombre(),p.getFecha())
									);
				}
				return null;
			}
			
		});
	}
	
	
	public void importar(Long sucursalId,String... ids){
		HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		for(String id:ids){
			PagoConDeposito pago=(PagoConDeposito)source.get(PagoConDeposito.class, id);
			if(pago.getAutorizacion()==null){
				System.out.println("Importando deposito");
				target.replicate(pago, ReplicationMode.LATEST_VERSION);
			}
		}		
	}
	
	/**
	 * Envia de la base de datos de
	 * @param sucursalId
	 * @param id
	 */
	public void replicarAutorizacion(Long sucursalId,String... ids){
		HibernateTemplate source=Services.getInstance().getHibernateTemplate();
		HibernateTemplate target=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		for(String id:ids){
			PagoConDeposito pago=(PagoConDeposito)source.get(PagoConDeposito.class, id);
			if(pago.getAutorizacion()!=null){
				System.out.println("Replicando pago autorizado");
				target.replicate(pago, ReplicationMode.OVERWRITE);
			}
		}		
	}
	
	public void importarPendientes(Long sucursalId,Date fecha){
		String sql="select ABONO_ID from sx_cxc_abonos where fecha=? and autorizacion_id is null and tipo_id=\'PAGO_DEP\' and sucursal_id=?";
		SqlParameterValue p1=new SqlParameterValue(Types.DATE,fecha);		
		SqlParameterValue p2=new SqlParameterValue(Types.INTEGER,sucursalId);
		List<String> ids=ReplicaServices.getInstance().getJdbcTemplate(sucursalId).queryForList(sql,new Object[]{p1,p2},String.class);
		for(String id:ids){
			PagoConDeposito target=(PagoConDeposito)Services.getInstance().getHibernateTemplate().get(PagoConDeposito.class, id);
			if(target==null){
				target=(PagoConDeposito)ReplicaServices.getInstance().getHibernateTemplate(sucursalId).get(PagoConDeposito.class, id);
				target.setImportado(new Date());
				Services.getInstance().getHibernateTemplate().replicate(target, ReplicationMode.EXCEPTION);
				logger.info("Deposito importado: "+target);
				ReplicaServices.getInstance().getHibernateTemplate(sucursalId).update(target);
			}
		}
	}
	
	public static void main(String[] args) {
		ReplicadorDeDepositos2 replicador=new ReplicadorDeDepositos2();
		/*
		Long sucursalId=5L;
		HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		replicador.refrescar(sucursalId, source, target);
		*/
		//replicador.importar(2L, source, target);
		
		//8a8a8588-2844b579-0128-457c1289-0042		
		//8a8a81e7-284f074f-0128-4f4ab3e3-0082

		/*
		replicador.importar(
				"8a8a81e7-284f074f-0128-4f4ab3e3-0082"
				,ReplicaServices.getInstance().getHibernateTemplate(3L)
				,Services.getInstance().getHibernateTemplate()
				);
		*/
		
		//replicador.replicarAutorizacion(5L, "8a8a8487-2869b721-0128-69fda201-0010");
				
		//replicador.refrescar(sucursalId, source, target);
		//replicador.replicarAutorizacion(sucursalId, id);
		
		/*
		Long[] sucursales={6L};
		
		HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		for(Long id:sucursales){
			HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(id);
			//replicador.refrescar(id, source, target);
			replicador.refrescar(DateUtil.toDate("09/02/2010"),id, source, target);
		}
		*/
		/*
		replicador.importar(
				"8a8a81e7-284f074f-0128-4f4ab3e3-0082"
				,ReplicaServices.getInstance().getHibernateTemplate(3L)
				,Services.getInstance().getHibernateTemplate()
				);
		*/
		//replicador.importar(5L,"8a8a8487-293d851b-0129-3dbad59d-0008"	);
		//replicador.refreshPendientes(sourceSucursal, source, target)
		//replicador.importarPendientes(5L,DateUtil.toDate("15/06/2010"));
		replicador.replicarAutorizacion(2L, "8a8a8587-29410dca-0129-4206cc65-001a"	);
	}
	

}
