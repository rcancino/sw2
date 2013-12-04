package com.luxsoft.siipap.ventas.service;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.dao.core.ClienteDao;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.ventas.dao.DescPorVolDao;
import com.luxsoft.siipap.ventas.dao.ListaDePreciosClienteDao;
import com.luxsoft.siipap.ventas.model.DescuentoEspecial;
import com.luxsoft.siipap.ventas.model.ListaDePreciosCliente;
import com.luxsoft.siipap.ventas.model.ListaDePreciosClienteDet;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.siipap.ventas.model.VentaDet;

/**
 * Implementacion estandar de {@link DescuentosManager} 
 * 
 * @author Ruben Cancino Ramos
 *@deprecated NO SE USA PENDIENTE DE ELIMINACION
 */
public class DescuentosManagerImpl extends HibernateDaoSupport implements DescuentosManager{
	
	private ListaDePreciosClienteDao listaDePreciosClienteDao;
	private ClienteDao clienteDao;
	private VentasManager ventasManager;
	private Logger logger=Logger.getLogger(getClass());

	
	/**
	 * Busca el descuento especial para la venta indicada
	 * 
	 * @param v La venta de la que se requiere el descuento
	 * @return El descuento especial o nulo si no existe
	 */
	public DescuentoEspecial buscarDescuentoEspecial(final Venta v){
		List<DescuentoEspecial> data=getHibernateTemplate().find(
				"from DescuentoEspecial e left join fetch e.cargo c where c.id=?",v.getId());
		return data.isEmpty()?null:data.get(0);
	}
	
	/**
	 * Aplica un descuento especial
	 * 
	 * @param d El descuento especial solicitado
	 * 
	 * @return El descuento si fue exitosa su persistencia
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public DescuentoEspecial asignarDescuentoEspecial(final DescuentoEspecial de){
		
		Venta v=getVentasManager().get(de.getCargo().getId());
		
		DescuentoEspecial existente=buscarDescuentoEspecial(v);
		if(existente!=null)
			throw new DescuentoEspecialAsignadoException(existente.getCargo());
		if(logger.isDebugEnabled()){
			logger.debug("Aplicando descuento especial: "+de);
		}
		for(VentaDet det:v.getPartidas()){
			det.setDescuentoNota(de.getDescuento().getDescuento());
			det.setDescuento(0);
		}
		v.setDescuentoGeneral(de.getDescuento().getDescuento());
		return (DescuentoEspecial)getHibernateTemplate().merge(de);
	}
	
	/**
	 * Facade para controlar la administracion de los descuentos
	 * debe delegar a otros metodos la actualizacion
	 * 
	 * @param v
	 */
	public void actualizarDescuento(final Venta v){
		actualizarDescuentoCredPrecioBruto(v);
	}
	
	/**
	 * 
	 * @param v
	 */
	public void actualizarDescuentoCredPrecioBruto(final Venta v){

		if(!v.getOrigen().equals(OrigenDeOperacion.CRE))
			return; //No es de credito
		if(!v.isPrecioBruto()) return ;// No es precio bruto
		
		// Caso 1 Descuento especial
		DescuentoEspecial especial=buscarDescuentoEspecial(v);
		if(especial!=null) 
			return; //Ya existe un descuento especial
		
		// Caso 2 Credito cheque post-fechado
		Cliente cliente = clienteDao.get(v.getCliente().getId());
		if( (cliente.getCredito()!=null) && cliente.getCredito().isChequePostfechado()){
			double descuentoPorVolumen = descPorVolDao.buscarDescuentoContado(v.getImporte().doubleValue());
			int plazo = v.getPlazo();
			if (plazo <= 15)
				descuentoPorVolumen -= 1;
			else if (plazo > 15 && plazo <= 30)
				descuentoPorVolumen -= 2;
			else if (plazo > 30 && plazo <= 45)
				descuentoPorVolumen -= 3;
			v.setDescuentoGeneral(descuentoPorVolumen);
			for (VentaDet det : v.getPartidas()) {
				if(det.getDescuentoOriginal()==0)
					det.setDescuentoOriginal(descuentoPorVolumen);
				det.setDescuentoNota(descuentoPorVolumen);
				det.setDescuento(0);
			}
			return;
		}
		
		//Caso 3 Precio de lista para el cliente
		ListaDePreciosCliente lista = listaDePreciosClienteDao.buscarListaVigente(v.getCliente());		
		if(lista!=null){
			for (VentaDet det : v.getPartidas()) {
				ListaDePreciosClienteDet precio = lista.getPrecio(det.getProducto(), v.getMoneda());
				if(precio!=null){
					if(det.getDescuentoOriginal()==0)
						det.setDescuentoOriginal(precio.getDescuento());
					det.setDescuentoNota(precio.getDescuento());
					det.setDescuento(0);
				}else{
					if(det.getDescuentoOriginal()==0)
						det.setDescuentoOriginal(lista.getDescuento());
					det.setDescuentoNota(lista.getDescuento());
					det.setDescuento(0);
				}						
			}
			return;
		}
		
		//Caso 4 Escala (para todos los demas)
		else{
			final Periodo periodo=Periodo.getPeriodoEnUnMes(v.getFecha());
			double volumen = getVentasManager().getVolumenDeVentas(periodo, v.getCliente());
			volumen += v.getImporte().doubleValue();
			double descuentoPorVolumen = descPorVolDao.buscarDescuentoCredito(volumen);
			for (VentaDet det : v.getPartidas()) {
				det.setDescuentoOriginal(descuentoPorVolumen);
				det.setDescuentoNota(descuentoPorVolumen);
				det.setDescuento(0);
			}
		}
		
	}
	
	/**
	 * Actualiza de manera adecuada el descuento para  ventas de credito
	 * 
	 * por escala Unicamente por escala
	 *  
	 * @param v
	 */
	public void actualizarDescuentoCreditoEscala(final Venta v){
		if(!v.getOrigen().equals(OrigenDeOperacion.CRE))
			return; //No es de credito
		if(!v.isPrecioBruto()) return ;// No es precio bruto
		
		// Caso 1 Descuento especial
		//boolean especial=actualizarConDescuentoEspecial(v);
		DescuentoEspecial especial=buscarDescuentoEspecial(v);
		if(especial!=null) 
			return;
		
		// Caso 2 Credito cheque post-fechado
		Cliente cliente = clienteDao.get(v.getCliente().getId());
		if( (cliente.getCredito()!=null) && cliente.getCredito().isChequePostfechado()){
			return;
		}		
		
		//Caso 3 Precio de lista para el cliente
		ListaDePreciosCliente lista = listaDePreciosClienteDao.buscarListaVigente(v.getCliente());		
		if(lista!=null){			
			return;
		}
		
		//Caso 4 Escala (para todos los demas)
		else{
			final Periodo periodo=Periodo.getPeriodoEnUnMes(v.getFecha());
			double volumen = getVentasManager().getVolumenDeVentas(periodo, v.getCliente());
			volumen += v.getImporte().doubleValue();
			double descuentoPorVolumen = descPorVolDao.buscarDescuentoCredito(volumen);
			for (VentaDet det : v.getPartidas()) {
				det.setDescuentoOriginal(descuentoPorVolumen);
				det.setDescuentoNota(descuentoPorVolumen);
				det.setDescuento(0);
			}
		}
	}
	
	/** Metodos validados ***/
	
	/**
	 * Aplica un descuento especial a una venta
	 * 
	 * @param de
	 * @param ventaId
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public void aplicarDescuentoEspecial(final DescuentoEspecial de, final String ventaId) {
		Venta ventas=getVentasManager().get(ventaId);
		Assert.isTrue(ventas.getOrigen().equals(OrigenDeOperacion.CRE),"Solo aplica para ventas de credito");
		Assert.isTrue(ventas.isPrecioBruto(),"Solo aplica para ventas precio bruto");
		Assert.isTrue(ventas.getSaldo().doubleValue()>0,"No se puede aplicar un descuento a una venta sin saldo ");
		de.setCargo(ventas);
		final double descuento=de.getDescuento().getDescuento();
		for(VentaDet d:ventas.getPartidas()){
			d.setDescuentoNota(descuento);
		}
		ventas.setDescuentoGeneral(descuento);
		getVentasManager().salvar(ventas);
		getHibernateTemplate().save(de);
	}
	
	/**
	 * Cancela el descuento especial aplicado a una venta
	 * 
	 * @param descuentoId
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public void cancelarDescuentoEspecial(final Long descuentoId){
		
		DescuentoEspecial de=(DescuentoEspecial)getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				return session.get(DescuentoEspecial.class,descuentoId);
			}
		});
		if(de==null)
			return;
		Venta v=getVentasManager().get(de.getCargo().getId());
		for(VentaDet d:v.getPartidas()){
			d.setDescuentoNota(0);
		}
		v.setDescuentoGeneral(0);
		getHibernateTemplate().delete(de);
		getVentasManager().salvar(v);
	}
	
	

	public void setListaDePreciosClienteDao(
			ListaDePreciosClienteDao listaDePreciosClienteDao) {
		this.listaDePreciosClienteDao = listaDePreciosClienteDao;
	}	


	public void setClienteDao(ClienteDao clienteDao) {
		this.clienteDao = clienteDao;
	}

	private DescPorVolDao descPorVolDao;


	public void setDescPorVolDao(DescPorVolDao descPorVolDao) {
		this.descPorVolDao = descPorVolDao;
	}


	public VentasManager getVentasManager() {
		return ventasManager;
	}


	public void setVentasManager(VentasManager ventasManager) {
		this.ventasManager = ventasManager;
	}
	
	

}
