package com.luxsoft.siipap.cxc.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.list.SetUniqueList;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.core.task.TaskExecutor;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.UniqueList;

import com.luxsoft.siipap.cxc.dao.CargoDao;
import com.luxsoft.siipap.cxc.dao.NotaDeCargoDao;
import com.luxsoft.siipap.cxc.dao.NotaDeCreditoDao;
import com.luxsoft.siipap.cxc.dao.PagoDao;
import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.AplicacionDePago;
import com.luxsoft.siipap.cxc.model.AutorizacionParaCargo;
import com.luxsoft.siipap.cxc.model.CancelacionDeAbono;
import com.luxsoft.siipap.cxc.model.CancelacionDeCargo;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.CargoPorDiferencia;
import com.luxsoft.siipap.cxc.model.ChequeDevuelto;
import com.luxsoft.siipap.cxc.model.Juridico;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargoDet;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDescuento;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDevolucion;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoConCheque;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.cxc.model.PagoConEfectivo;
import com.luxsoft.siipap.cxc.model.PagoConTarjeta;
import com.luxsoft.siipap.cxc.model.PagoDeDiferencias;
import com.luxsoft.siipap.cxc.model.PagoEnEspecie;
import com.luxsoft.siipap.cxc.model.Tarjeta;
import com.luxsoft.siipap.cxc.rules.RevisionDeCargosRules;
import com.luxsoft.siipap.model.Autorizacion2;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Concepto;
import com.luxsoft.siipap.model.tesoreria.Origen;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.LoginManager;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.service.core.ClienteManager;
import com.luxsoft.siipap.support.hibernate.StringEnumUserType;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.ventas.dao.ListaDePreciosClienteDao;
import com.luxsoft.siipap.ventas.model.Cobrador;
import com.luxsoft.siipap.ventas.model.ListaDePreciosCliente;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;
import com.luxsoft.sw3.cfd.services.ComprobantesDigitalesManager;
import com.luxsoft.sw3.cfdi.INotaDeCredito;

/**
 * Implementacion de {@link CXCManager}
 * 
 * @author Ruben Cancino
 *
 */
public class CXCManagerImpl extends HibernateDaoSupport implements CXCManager{

	private PagoDao pagoDao;
	private CargoDao cargoDao;
	private ListaDePreciosClienteDao listaDePreciosClienteDao;
	private ClienteManager clienteManager;
	private NotaDeCreditoDao notaDao;
	private ClienteServices clienteServices;
	private NotaDeCargoDao notaDeCargoDao;
	
	//private ComprobantesDigitalesManager comprobanteDigitalManager;
	private INotaDeCredito cfdiNotaDeCredito;
	
	@Transactional(propagation=Propagation.REQUIRED)
	public Cargo getCargo(String id) {
		Cargo c=(Cargo)getHibernateTemplate().get(Cargo.class, id);
		Hibernate.initialize(c.getCobrador());
		return c; 
	}
	
	/**
	 * Persiste cualquier tipo de abono en la base de datos
	 * 
	 * @param a El abono a persistir
	 * @return El abono perfectamente inicializado en todas sus propiedades
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public Abono salvarAbono(final Abono a) {
		
		Abono res= (Abono)getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				session.saveOrUpdate(a);				
				a.acutalizarDisponible();
				//Actualizamos el saldo de los cargos para todas las aplicaciones
				for(Aplicacion ap:a.getAplicaciones()){
					ap.actualizarDetalle();
					BigDecimal saldo=ap.getCargo().getSaldo();
					saldo=saldo.subtract(ap.getImporte());
					if(saldo.doubleValue()<0)
						saldo=BigDecimal.ZERO;
					ap.getCargo().setSaldo(saldo);
					
				}				
				a.setReplicado(null);
				return session.merge(a);
			}			
		});	
		if(res!=null)
			actualizarSaldoDeCliente(res.getCliente());
		return res;		
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public Cargo save(Cargo bean) {
		if(bean instanceof NotaDeCargo){
			//NotaDeCargo notaDeCargo=(NotaDeCargo)getHibernateTemplate().merge(bean);
			//getComprobanteDigitalManager().generarComprobante(notaDeCargo);
			//cfdiNota.generar(notaDeCargo);
			//return notaDeCargo;
			return null;
		}
		return (Cargo)getHibernateTemplate().merge(bean);
	}
	
	/**
	 * Forma generica de buscar un abono
	 * 
	 * @param id
	 * @return
	 */
	@Transactional(propagation=Propagation.SUPPORTS)
	public Abono getAbono(final String id){		
		//List<Abono> abonos=getHibernateTemplate().find("from Abono a left join fetch a.aplicaciones ap where a.id=?", id);
		//return abonos.isEmpty()?null:abonos.get(0);
		
		Abono a=(Abono)getHibernateTemplate().get(Abono.class, id);
		if(a!=null)
			Hibernate.initialize(a.getAplicaciones());
		return a;
	}	

	
	
	@Transactional(propagation=Propagation.REQUIRED)
	public List<Cargo> salvar(List<Cargo> cuentas) {
		List<Cargo> res=new ArrayList<Cargo>(cuentas.size());
		for(Cargo c:cuentas){
			res.add(save(c));
		}
		return res;
	}

	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.cxc.service.CXCManager#buscarCuentasPorCobrar(com.luxsoft.siipap.cxc.model.OrigenDeOperacion)
	 */
	public List<Cargo> buscarCuentasPorCobrar(OrigenDeOperacion origen) {
		String hql="from Cargo c left join fetch c.cobrador cc " +
				"  where c.origen=? " +
				"    and (c.total-c.aplicado)!=0 " +
				"  and c.fecha>?" +
				" order by c.vencimiento desc";
		Object[] params=new Object[]{origen,DateUtil.toDate("31/12/2008")};
		return ServiceLocator2.getHibernateTemplate().find(hql,params);
	}
	
	

	public List<Cargo> buscarCuentasPorCobrar() {
		String hql="from Cargo c left join fetch c.cobrador cc " +
		"  where  (c.total-c.aplicado)!=0 " +
		"  and c.fecha>?" +
		" and c.origen not in(\'CAM\',\'MOS\',\'JUR\',\'CHE\')" +
		" order by c.vencimiento desc";
		Object[] params=new Object[]{DateUtil.toDate("31/12/2008")};
		return ServiceLocator2.getHibernateTemplate().find(hql,params);
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public PagoConEfectivo salvarPago(PagoConEfectivo pago) {		
		PagoConEfectivo res=(PagoConEfectivo)getPagoDao().save(pago);
		if(res!=null)
			actualizarSaldoDeCliente(res.getCliente());
		return res;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public PagoConTarjeta salvarPago(PagoConTarjeta pago) {		
		PagoConTarjeta res=(PagoConTarjeta)getPagoDao().save(pago);
		if(res!=null)
			actualizarSaldoDeCliente(res.getCliente());
		return res;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public PagoConCheque salvarPago(PagoConCheque pago) {		
		PagoConCheque res= (PagoConCheque)getPagoDao().save(pago);
		if(res!=null)
			actualizarSaldoDeCliente(res.getCliente());
		return res;
	}	

	@Transactional(propagation=Propagation.REQUIRED)
	public PagoConDeposito salvarPago(PagoConDeposito pago) {
		PagoConDeposito res=(PagoConDeposito)getPagoDao().save(pago);
		if(res!=null)
			actualizarSaldoDeCliente(res.getCliente());
		return res;
	}
	
	public List<Tarjeta> buscarTarjetas() {
		return getHibernateTemplate().find("from Tarjeta");
	}	

	public List<Cargo> buscarCuentasPorCobrar(final Cliente cliente,final OrigenDeOperacion origen) {
		List data;
		Date limite=DateUtil.toDate("31/12/2008");
		if((origen!=null) && (origen.equals(OrigenDeOperacion.JUR))){
			data=getHibernateTemplate().find("select j.cargo from Juridico j where j.cargo.clave=?", cliente.getClave());
			SetUniqueList res=SetUniqueList.decorate(data);
			return res;
		}	
		if(origen!=null){
			Object[] params=new Object[]{cliente.getClave(),origen,limite};
			data= getHibernateTemplate().find("from Cargo c left join fetch c.partidas where (c.total-c.aplicado)!=0 and c.clave=? and c.origen=? and c.fecha>?",params);
		}else{
			Object[] params=new Object[]{cliente.getClave(),limite};
			data =getHibernateTemplate().find("from Cargo c left join fetch c.partidas where (c.total-c.aplicado)!=0 and c.clave=? and c.fecha>?",params);
		}
		SetUniqueList res=SetUniqueList.decorate(data);
		return res;		
	}
	
	/**
	 * Actualiza la fecha de revision y cobro para toda la cuenta por pagar
	 * y la regresa para facilitar la
	 * 
	 */
	public List<Cargo> actualizarRevisionYCobro(){
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				String hql="from Cargo c left join fetch c.cobrador cc " +
				"  where c.origen=\'CRE\' " +
				"    and (c.total-c.aplicado)!=0 " +
				"  and c.fecha>?" +
				" order by c.vencimiento desc";
				
				ScrollableResults rs=session.createQuery(hql)
				.setDate(0, DateUtil.toDate("31/12/2008"))
				.scroll();
				List<Cargo> cargos=new ArrayList<Cargo>();
				RevisionDeCargosRules rules=RevisionDeCargosRules.instance();
				while(rs.next()){
					Cargo cargo=(Cargo)rs.get()[0];
					cargos.add(cargo);
					try {
						rules.actualizar(cargo, new Date());						
					} catch (Exception e) {
						e.printStackTrace();
						logger.error(e);
					}					
				}				
				return cargos;
			}			
			
		});
	}
	
	/*** Manejo de listas de precios De Cargo**/
	
	public ListaDePreciosCliente buscarListaDePrecios(Long id) {
		return getListaDePreciosClienteDao().get(id);
	}

	public List<ListaDePreciosCliente> buscarListasDePrecios(){
		return getListaDePreciosClienteDao().getAll();
	}
	/*
	public List<ListaDePreciosCliente> buscarListasDePrecios(Periodo p) {
		return getListaDePreciosClienteDao().buscarListas(p);
	}*/

	@Transactional (propagation=Propagation.REQUIRED)
	public ListaDePreciosCliente salvarLista(ListaDePreciosCliente lp) {		
		return getListaDePreciosClienteDao().save(lp);
	}
	 
	@Transactional (propagation=Propagation.REQUIRED)
	public void eliminarLista(Long id) {
		getListaDePreciosClienteDao().remove(id);
	}

	public List<ListaDePreciosCliente> buscarListasDePreciosVigentes() {
		return getListaDePreciosClienteDao().buscarListasVigentes();
	}
	
	public ListaDePreciosCliente buscarListaVigente(Cliente c) {
		return getListaDePreciosClienteDao().buscarListaVigente(c);
	}

	/*
	 * 
	 * 
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public ListaDePreciosCliente copiar(Long id) {
		return getListaDePreciosClienteDao().copiar(id);
	}	

	public ListaDePreciosClienteDao getListaDePreciosClienteDao() {
		return listaDePreciosClienteDao;
	}

	public void setListaDePreciosClienteDao(
			ListaDePreciosClienteDao listaDePreciosClienteDao) {
		this.listaDePreciosClienteDao = listaDePreciosClienteDao;
	}

	public List<Cobrador> getCobradores() {
		return getHibernateTemplate().find("from Cobrador c where c.activo=?",new Object[]{Boolean.TRUE});
	}
	
	
	public List<Cobrador> getVendedores() {
		return getHibernateTemplate().find("from Vendedor c where c.activo=?",new Object[]{Boolean.TRUE});
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public NotaDeCredito salvarNota(NotaDeCredito nota) {
		validarAplicaciones(nota);
		nota.setAplicable(nota.getAplicaciones().isEmpty());
		//return  (NotaDeCredito)salvarAbono(nota);
		if(nota instanceof NotaDeCreditoBonificacion){
			Assert.notNull(nota.getAutorizacion(),"La nota requiere autorización");			
		}		
		
		for(Aplicacion ap:nota.getAplicaciones()){
			ap.actualizarDetalle();				
		}
		nota.setReplicado(null);
		nota.setImportado(null);
		registrarBitacora(nota);
		NotaDeCredito res= (NotaDeCredito)getHibernateTemplate().merge(nota);
		//getComprobanteDigitalManager().generarComprobante(res);
		cfdiNotaDeCredito.generar(res);
		return res;
	}
	
	/**
	 * Elimina las aplicaciones q puedan hacer negativa el saldo de una factura
	 * 
	 * @param nota
	 */
	private void validarAplicaciones(final NotaDeCredito nota){
		//Eliminamos las posibles aplicaciones q puedan hacer negativo el saldo de una factura
		List<Aplicacion> quitar=new ArrayList<Aplicacion>();
		for(int index=0;index<nota.getAplicaciones().size();index++){
			Aplicacion a=nota.getAplicaciones().get(index);	
			double saldo=a.getCargo().getSaldoCalculado().doubleValue();
			double importe=a.getImporte().doubleValue();
			double diferencia=saldo-importe;
			if(diferencia>=-01 && diferencia<-.001){
				a.setImporte(BigDecimal.valueOf(saldo));
				importe=a.getImporte().doubleValue();
			}
			if(saldo<=0){
				quitar.add(a);
			}
			if(importe>saldo){
				//a.setImporte(a.getCargo().getSaldoCalculado());
				quitar.add(a);
			}
		}
		for(Aplicacion togo:quitar){
			
			nota.getAplicaciones().remove(togo);
		}
	}
	
	/**
	 * Cancela un pago si es posible
	 * 
	 * @param id
	 * @param aut
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public void cancelarPago(final String id,final Autorizacion2 aut){
		getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				Pago pago=(Pago)session.load(Pago.class, id);				
				Assert.isTrue(pago.getAplicaciones().isEmpty(),"El pago tiene aplicaciones no se puede cancelar");
				Assert.isNull( pago.getDeposito(),"El pago ya ha sido depositado en el banco: "+pago.getDeposito());
								
				CancelacionDeAbono c=new CancelacionDeAbono(pago);
				if(aut==null){
					c.setAutorizacion(CXCAutorizaciones.paraCancelarAbono(pago));
				}else
					c.setAutorizacion(aut);
				c.setAbono(pago);
				c.setFecha(ServiceLocator2.obtenerFechaDelSistema());
				c.getLog().setCreado(c.getFecha());
				c.getLog().setCreateUser(KernellSecurity.instance().getCurrentUserName());
				session.saveOrUpdate(c);				
				session.flush();
				session.clear();
				actualizarSaldoDeCliente(pago.getCliente());
				return null;
			}			
		});
	}
	
	/**
	 * Cancela una nota de credito
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public void cancelarNota(final String notaId,final Autorizacion2 aut){		
		getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				NotaDeCredito nota=(NotaDeCredito)session.load(NotaDeCredito.class, notaId);
				nota.getAplicaciones().clear();
				nota.setTotal(BigDecimal.ZERO);
				nota.setImporte(BigDecimal.ZERO);
				nota.setComentario("CANCELADO");
				nota.acutalizarDisponible();
				nota.cancelar();
				nota.getConceptos().clear();
				CancelacionDeAbono c=new CancelacionDeAbono(nota);
				if(aut==null){
					c.setAutorizacion(CXCAutorizaciones.paraCancelarAbono(nota));
				}else
					c.setAutorizacion(aut);
				c.getAutorizacion().setComentario("AUTORIZACION PARA CANCELACION DE NOTA: "+nota.getFolio());
				c.setFecha(ServiceLocator2.obtenerFechaDelSistema());
				c.getLog().setCreado(c.getFecha());
				c.getLog().setCreateUser(KernellSecurity.instance().getCurrentUserName());
				
				session.saveOrUpdate(c);				
				session.flush();
				session.clear();
				actualizarSaldoDeCliente(nota.getCliente());
				return null;
			}			
		});
	}
	
	
	

	/**
	 * Cancela una aplicacion
	 * 
	 * @param id
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public void cancelarAplicacion(final String id){
		Aplicacion a=(Aplicacion)getHibernateTemplate().get(Aplicacion.class, id);
		getHibernateTemplate().delete(a);
		actualizarSaldoDeCliente(a.getAbono().getCliente());
	}
	
	/**
	 * Cancela una nota de cargo 
	 * 
	 * @param id
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public void cancelarNotaDeCargo(final String id){
		
		NotaDeCargo cargo=getNotaDeCargoDao().get(id);
		Assert.isTrue(cargo.getAplicaciones().isEmpty(),"La nota de cargo tiene aplicaciones no se puede eliminar");
		//Paso 1 Generar una autorizacion
		AutorizacionParaCargo aut=new AutorizacionParaCargo();
		aut.setAutorizo(KernellSecurity.instance().getCurrentUserName());
		aut.setComentario("CANCELACION DE NOTA DE CARGO");
		aut.setFechaAutorizacion(new Date());
		aut.setIpAdress(KernellSecurity.getIPAdress());
		aut.setMacAdress(KernellSecurity.getMacAdress());
		
		//Paso 2 Generamos la cancelacion
		final CancelacionDeCargo cancelacion=new CancelacionDeCargo();
		cancelacion.setCargo(cargo);
		cargo.setCancelacion(cancelacion);
		cancelacion.setComentario("CANCELACION");
		cancelacion.setDocumento(cargo.getDocumento());
		cancelacion.setImporte(cargo.getTotal());
		cancelacion.setMoneda(cargo.getMoneda());
		cancelacion.setAutorizacion(aut);
		cancelacion.setFecha(ServiceLocator2.obtenerFechaDelSistema());
		cancelacion.getLog().setCreado(cancelacion.getFecha());
		cancelacion.getLog().setCreateUser(KernellSecurity.instance().getCurrentUserName());
		//Paso 3 Actualizamos la NotaDeCargo
		cargo.setImporte(BigDecimal.ZERO);
		cargo.setImpuesto(BigDecimal.ZERO);
		cargo.setTotal(BigDecimal.ZERO);
		cargo.getConceptos().clear();		
		cargo.setComentario2("CARGO CANCELADO");
		
		//Paso 4 Persistir
		
		getNotaDeCargoDao().save(cargo);
		getHibernateTemplate().save(cancelacion);
	}
	
	
	
	
	public void cancelarChequeDevuelto(String id) {
		ChequeDevuelto cargo=(ChequeDevuelto)getCargo(id);
		Assert.isTrue(cargo.getAplicaciones().isEmpty(),"El  cargo tiene aplicaciones no se puede eliminar");
		//Paso 1 Generar una autorizacion
		AutorizacionParaCargo aut=new AutorizacionParaCargo();
		aut.setAutorizo(KernellSecurity.instance().getCurrentUserName());
		aut.setComentario("CANCELACION DE CARGO POR CHEQUE DEVUELTO");
		aut.setFechaAutorizacion(new Date());
		aut.setIpAdress(KernellSecurity.getIPAdress());
		aut.setMacAdress(KernellSecurity.getMacAdress());
		
		//Paso 2 Generamos la cancelacion
		final CancelacionDeCargo cancelacion=new CancelacionDeCargo();
		cancelacion.setCargo(cargo);
		cargo.setCancelacion(cancelacion);
		cancelacion.setComentario("CANCELACION");
		cancelacion.setDocumento(cargo.getDocumento());
		cancelacion.setImporte(cargo.getTotal());
		cancelacion.setMoneda(cargo.getMoneda());
		cancelacion.setAutorizacion(aut);
		cancelacion.setFecha(ServiceLocator2.obtenerFechaDelSistema());
		cancelacion.getLog().setCreado(cancelacion.getFecha());
		cancelacion.getLog().setCreateUser(KernellSecurity.instance().getCurrentUserName());
		//Paso 3 Actualizamos la NotaDeCargo
		cargo.setImporte(BigDecimal.ZERO);
		cargo.setImpuesto(BigDecimal.ZERO);
		cargo.setTotal(BigDecimal.ZERO);
		//cargo.getConceptos().clear();		
		cargo.setComentario2("CARGO CANCELADO");
		
		//Paso 4 Persistir
		this.save(cargo);
		getHibernateTemplate().save(cancelacion);
		
	}

	/**
	 * Busca los abonos del periodo para el tipo de opracion indicado
	 * 
	 * @param p El periodo del abono
	 * @param tipo El tipo de operacion al que pertenecen los abonos
	 * @return La lista de abonos 
	 */
	public List<Abono> buscarAbonos(final Periodo p,final OrigenDeOperacion tipo){
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Properties properties=new Properties();
				properties.put("enumClassName", "com.luxsoft.siipap.cxc.model.OrigenDeOperacion");
				return session.createQuery("from Abono a " +
						"left join fetch a.sucursal s " +
						"left join fetch a.cliente c " +
						"where a.fecha between ? and ? " 
						+"and a.origen=?"
						)
						.setParameter(0, p.getFechaInicial(),Hibernate.DATE)
						.setParameter(1, p.getFechaFinal(),Hibernate.DATE)
						.setParameter(2, tipo,Hibernate.custom(StringEnumUserType.class,properties))
						.list()
						;
			}
			
		});
	}
	
	/**
	 * Busca los cargos relacionados con  el abono
	 * 
	 * @param abono
	 * @return
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public List<Cargo> buscarCargos(final Abono abono){
		String hql="select cargo.id from Aplicacion ap where ap.abono.id=?";
		List<String> ids=getHibernateTemplate().find(hql, abono.getId());
		List<Cargo> res=new ArrayList<Cargo>();
		for(String id:ids){
			res.add(getCargo(id));
		}
		return res;
	}
	
	/**
	 * Busca las aplicaciones relacionadas con el abono
	 * 
	 * @param abono
	 * @return
	 */
	public List<Aplicacion> buscarAplicaciones(final Abono abono){
		String hql="from Aplicacion a " +
				"left join fetch a.cargo x" +
				"left join fetch a.abono y" +
				" where y.id=?";
		return getHibernateTemplate().find(hql, abono.getId());
	}
	
	
	
	/**
	 * Busca los abonos disponibles para aplicar por el cliente
	 * 
	 * @param cliente
	 * @return
	 */
	public List<Abono> buscarDisponibles(Cliente cliente) {
		String hql="from Abono p left join fetch p.aplicaciones ap where p.clave=? " +
				" and p.total-p.diferencia-p.aplicado>0";
		List data=getHibernateTemplate().find(hql, cliente.getClave());
		CollectionUtils.filter(data, new Predicate(){
			public boolean evaluate(Object object) {
				if(object instanceof PagoConCheque){
					PagoConCheque p=(PagoConCheque)object;
					return !p.isDevuelto();
				}
				return true;
			}
		});
		final EventList<Abono> source=GlazedLists.eventList(data);
		UniqueList<Abono> pagos=new UniqueList<Abono>(source,GlazedLists.beanPropertyComparator(Abono.class, "id"));
		return pagos;
	}

	@Transactional(propagation=Propagation.SUPPORTS)
	public BigDecimal sumarPagos(Cargo c) {
		String hql="select sum(x.importe) from AplicacionDePago x where x.cargo.id=?";
		List<BigDecimal> pagos=getHibernateTemplate().find(hql, c.getId());
		if(pagos.isEmpty())
			return BigDecimal.ZERO;
		else
			return pagos.get(0)!=null?pagos.get(0):BigDecimal.ZERO;
	}
	
	public int buscarProximaNota() {
		String hql="select max(folio) from NotaDeCredito n";
		List res=getHibernateTemplate().find(hql);
		if(res.isEmpty())
			return 1;
		Number val=(Number)res.get(0);
		return val.intValue()+1;
		
	}
	
	public int buscarProximaNotaDeCargo() {
		String hql="select max(documento) from NotaDeCargo n";
		List res=getHibernateTemplate().find(hql);
		if(res.isEmpty())
			return 1;
		Number val=(Number)res.get(0);
		return val.intValue()+1;
	}
	
	/**
	 * Genera el pago por diferencia a una factura/cargo
	 * 
	 * @param cargo
	 * @return
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public PagoDeDiferencias generarPagoPorDiferencia(final Cargo cargo,final boolean cambiaria){
		if(!cargo.getTipoSiipap().equals("X"))
			Assert.isTrue(cargo.getSaldoCalculado().doubleValue()<100,"No se permite pago de diferencias mayores a 100");
		Assert.isTrue(cargo.getSaldoCalculado().doubleValue()>0,"No se permite pago de facturas con saldos negativos");
		PagoDeDiferencias pago=new PagoDeDiferencias();
		pago.setCambiaria(cambiaria);
		pago.setCliente(cargo.getCliente());
		pago.setComentario("OTROS PRODUCTOS");
		pago.setOrigen(cargo.getOrigen());
		pago.setSucursal(ServiceLocator2.getConfiguracion().getSucursal());
		AplicacionDePago aplicacion=new AplicacionDePago();
		aplicacion.setCargo(cargo);
		aplicacion.setImporte(cargo.getSaldoCalculado());
		aplicacion.setComentario("OTROS PRODUCTOS");
		aplicacion.actualizarDetalle();
		aplicacion.getDetalle().setFormaDePago(pago.getInfo());
		pago.agregarAplicacion(aplicacion);
		
		pago.setImporte(MonedasUtils.calcularImporteDelTotal(aplicacion.getImporte()));
		pago.actualizarImpuesto();
		pago.setTotal(aplicacion.getImporte());
		
		
		
		pago=(PagoDeDiferencias)getPagoDao().save(pago);
		
		actualizarSaldoDeCliente(pago.getCliente());
		return pago;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.cxc.service.CXCManager#generarPagoEnEspecie(com.luxsoft.siipap.cxc.model.Cargo, java.lang.String)
	 */
	public PagoEnEspecie generarPagoEnEspecie(Cargo cargo, String comentario) {
		PagoEnEspecie pago=new PagoEnEspecie();
		pago.setCliente(cargo.getCliente());
		pago.setComentario(comentario);
		pago.setSucursal(ServiceLocator2.getConfiguracion().getSucursal());
		AplicacionDePago aplicacion=new AplicacionDePago();
		aplicacion.setCargo(cargo);
		aplicacion.setImporte(cargo.getSaldoCalculado());
		aplicacion.setComentario(comentario);
		aplicacion.actualizarDetalle();
		aplicacion.getDetalle().setFormaDePago(pago.getInfo());
		pago.agregarAplicacion(aplicacion);
		
		pago.setImporte(MonedasUtils.calcularImporteDelTotal(aplicacion.getImporte()));
		pago.actualizarImpuesto();
		pago.setTotal(aplicacion.getImporte());		
		pago=(PagoEnEspecie)getPagoDao().save(pago);
		return pago;
	}

	/**
	 * Genera el pago por incobrabilidad
	 * 
	 * @param cargo
	 * @return
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public PagoDeDiferencias generarPagoPorIncobrabilidad(final Cargo cargo,final String comentario){
		PagoDeDiferencias pago=new PagoDeDiferencias();
		pago.setCambiaria(false);
		pago.setCliente(cargo.getCliente());
		pago.setComentario("INCOBRABILIDAD");
		pago.setSucursal(ServiceLocator2.getConfiguracion().getSucursal());
		AplicacionDePago aplicacion=new AplicacionDePago();
		aplicacion.setCargo(cargo);
		aplicacion.setImporte(cargo.getSaldoCalculado());
		aplicacion.setComentario("AJUSTE POR INCOBRABILIDAD");
		aplicacion.actualizarDetalle();
		aplicacion.getDetalle().setFormaDePago(pago.getInfo());
		pago.agregarAplicacion(aplicacion);
		
		pago.setImporte(MonedasUtils.calcularImporteDelTotal(aplicacion.getImporte()));
		pago.actualizarImpuesto();
		pago.setTotal(aplicacion.getImporte());
		
		pago=(PagoDeDiferencias)getPagoDao().save(pago);
		
		actualizarSaldoDeCliente(pago.getCliente());
		return pago;
	}
	
	/**
	 * Salda un abono por diferencia 
	 * 
	 * @param abono
	 * @param cambiaria
	 * @return
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public Abono generarAplicacionPorDiferencia(final Abono abono,final Date fecha,final CargoPorDiferencia.TipoDiferencia tipo){
		if(abono.getDisponible().doubleValue()<=0)
			return abono;
		
		CargoPorDiferencia cargo=new CargoPorDiferencia();
		cargo.setCliente(abono.getCliente());
		cargo.setFecha(fecha);
		cargo.setTipoDiferencia(tipo);
		cargo.setTotal(abono.getDisponible());
		cargo.setImporte(MonedasUtils.calcularImporteDelTotal(cargo.getTotal()));
		cargo.setImpuesto(MonedasUtils.calcularImpuesto(cargo.getImporte()));
		cargo.setOrigen(abono.getOrigen());
		cargo.setSucursal(abono.getSucursal());
		Long max=(Long)getHibernateTemplate().find("select max(c.documento) from CargoPorDiferencia c ").get(0);
		if(max==null) max=0l;
		cargo.setDocumento(max+1);
		
		AplicacionDePago aplicacion=new AplicacionDePago();
		aplicacion.setCargo(cargo);
		aplicacion.setImporte(cargo.getSaldoCalculado());
		aplicacion.setComentario(cargo.getTipoDiferencia().name());
		aplicacion.actualizarDetalle();
		
		
		abono.agregarAplicacion(aplicacion);
		
		abono.setImporte(MonedasUtils.calcularImporteDelTotal(aplicacion.getImporte()));
		actualizarSaldoDeCliente(abono.getCliente());
		return salvarAbono(abono);
		
	}
	
	/**
	 * Genera el cargo correspondiente por cheque devuelto 
	 * 
	 * @param pago
	 * @return
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public ChequeDevuelto generarChequeDevuelto(final PagoConCheque pag,final Date fecha){
		List<Number> data=getHibernateTemplate().find("select max(c.documento) from ChequeDevuelto c ");
		Long folio=0l;
		if(data.isEmpty())
			folio=1l;
		else{
			Number next=data.get(0);
			if(next==null)
				next=0L;
			folio=next.longValue()+1l;
		}
		PagoConCheque pago=(PagoConCheque)getAbono(pag.getId());
		
		ChequeDevuelto ch=new ChequeDevuelto();
		ch.setNumeroFiscal(folio.intValue());
		ch.setDocumento(folio);
		ch.setCheque(pago);
		ch.setFecha(fecha);
		ch.setFechaRecepcionCXC(fecha);
		pago.setComentario("DEVUELTO");
		ch=(ChequeDevuelto)getHibernateTemplate().merge(ch);
		ch.setVencimiento(fecha);
		ch.setPlazo(0);
		//RevisionDeCargosRules.instance().actualizar(ch);
		actualizarSaldoDeCliente(ch.getCliente());
		generarNotaDeCargoPorChequeDevuelto(ch);
		generarCargoEnBancos(pago, fecha);
		return ch;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public NotaDeCargo generarNotaDeCargoPorChequeDevuelto(final ChequeDevuelto cargo){
		NotaDeCargo nota=new NotaDeCargo();
		nota.setCliente(cargo.getCliente());
		nota.setCargo(20d);
		nota.setCargos(BigDecimal.valueOf(20d));
		nota.setComentario("Cargo del  20% por cheque devuelto: "+cargo.getCheque().getNumero());
		nota.setFecha(new Date());
		int folio=buscarProximaNotaDeCargo();
		nota.setDocumento(new Long(folio));
		nota.setNumeroFiscal(folio);
		NotaDeCargoDet det=new NotaDeCargoDet();
		det.setCargo(20d);
		det.setVenta(cargo);
		String pattern="Suc:{0} Docto:{1,number,#######} ({2,date,short})";
		det.setComentario(MessageFormat.format(pattern
				,cargo.getSucursal().getNombre()
				,cargo.getDocumento()
				,cargo.getFecha()
				));
		
		nota.agregarConcepto(det);
		det.actualizarImporte();
		
		nota.setOrigen(cargo.getOrigen());
		nota.setCobrador(cargo.getCobrador());
		nota.setVencimiento(cargo.getFecha());
		nota.setPlazo(0);
		nota.setSucursal(ServiceLocator2.getConfiguracion().getSucursal());
		nota.setFechaRecepcionCXC(new Date());
		nota.setDiaRevision(7);
		nota.setDiaDelPago(7);
		nota.setMoneda(cargo.getMoneda());
		nota.setTc(cargo.getTc());
		nota.setImporte(det.getImporte());
		nota.setImpuesto(MonedasUtils.calcularImpuesto(nota.getImporte()));
		nota.setTotal(MonedasUtils.calcularTotal(nota.getImporte()));
		//RevisionDeCargosRules.instance().actualizar(nota, nota.getFecha());
		nota.setVencimiento(nota.getFecha());
		nota.setPlazo(0);
		nota.setCheque(cargo.getCheque());
		nota=(NotaDeCargo)save(nota);
		//getComprobanteDigitalManager().generarComprobante(nota);
		return nota;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	private void generarCargoEnBancos(final PagoConCheque pago,Date fecha){
		CargoAbono cargo=new CargoAbono();
		cargo.setAFavor(pago.getCuenta().getBanco().getEmpresa().getNombre());
		cargo.setImporte(pago.getTotal().multiply(BigDecimal.valueOf(-1.0)));
		cargo.setCuenta(pago.getCuenta());
		cargo.setFecha(fecha);
		
		cargo.setMoneda(pago.getCuenta().getMoneda());
		cargo.setSucursal(pago.getSucursal());
		cargo.setEncriptado(false);
		cargo.setConciliado(false);
		/*if(!DateUtil.isSameMonth(fecha, pago.getFecha()))
			cargo.setConciliado(true);*/
		String pattern="Cargo  por cheque dev. {0,date,short} suc: {1}";
		cargo.setComentario(MessageFormat.format(pattern, new Date(),pago.getSucursal()));		
		cargo.setReferencia(pago.getInfo());
		cargo.setOrigen(Origen.CHE);
		cargo.setPago(pago);
		
		Date time=new Date();
		
		String user=KernellSecurity.instance().getCurrentUserName();	
		//String ip=KernellSecurity.getIPAdress();
		//String mac=KernellSecurity.getMacAdress();
		
		cargo.getUserLog().setModificado(time);
		cargo.getUserLog().setUpdateUser(user);
		//bean.getAddresLog().setUpdatedIp(ip);
		//bean.getAddresLog().setUpdatedMac(mac);
		
		
		if(cargo.getId()==null){
			cargo.getUserLog().setCreado(time);
			cargo.getUserLog().setCreateUser(user);
			//bean.getAddresLog().setCreatedIp(ip);
			///bean.getAddresLog().setCreatedMac(mac);
		}
		try {
			Concepto c=(Concepto)getHibernateTemplate().get(Concepto.class, 737292L);
			cargo.setConcepto(c);
		} catch (Exception e) {
			e.printStackTrace();
		}
		getHibernateTemplate().merge(cargo);
	}

	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.cxc.service.CXCManager#generarJuridico(com.luxsoft.siipap.cxc.model.Cargo)
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public Juridico generarJuridico(Juridico jur){
		return (Juridico)getHibernateTemplate().merge(jur);
	}
	
	/**
	 * 
	 * @param jur
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public void cancelarJuridico(Juridico jur){
		getHibernateTemplate().delete(jur);
	}
	
	/**
	 * @deprecated No se requiere mas, el saldo se actualiza mediante un trigger en 
	 * la base de datos
	 * @param c
	 */
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	private void actualizarSaldoDeCliente(final Cliente c){
		/*if(c.getCredito()!=null){
			Runnable runner=new Runnable(){
				public void run() {
					try {
						getClienteServices().registrarSaldoyAtraso(c);
						//BigDecimal saldo=getClienteServices().getSaldo(c);
						//int atrasoMaximo=getClienteServices().get
						//System.out.println("Saldo del cliente: "+saldo);
						//c.getCredito().setSaldo(saldo);
						//c.getCredito().setAtrasoMaximo(atrasoMaximo);
					} catch (Exception e) {
						logger.error(e);
					}
				}
			};
			taskExecutor.execute(runner);
		}*/
	}
	
	private TaskExecutor taskExecutor;

	private EstadoDeCuentaManager estadoDeCuentaManager;
	
	
	public EstadoDeCuentaManager getEstadoDeCuentaManager(){
		return estadoDeCuentaManager;
	}
	
	public void setEstadoDeCuentaManager(EstadoDeCuentaManager estadoDeCuentaManager) {
		this.estadoDeCuentaManager = estadoDeCuentaManager;
	}
	
	

	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	/*** Colaboradores IoC ***/

	public ClienteManager getClienteManager() {
		return clienteManager;
	}

	public void setClienteManager(ClienteManager clienteManager) {
		this.clienteManager = clienteManager;
	}

	public PagoDao getPagoDao() {
		return pagoDao;
	}

	public void setPagoDao(PagoDao pagoDao) {
		this.pagoDao = pagoDao;
	}

	public NotaDeCreditoDao getNotaDao() {
		return notaDao;
	}

	public void setNotaDao(NotaDeCreditoDao notaDao) {
		this.notaDao = notaDao;
	}	
	
	public CargoDao getCargoDao() {
		return cargoDao;
	}

	public void setCargoDao(CargoDao cargoDao) {
		this.cargoDao = cargoDao;
	}
	
	public ClienteServices getClienteServices() {
		return clienteServices;
	}

	public void setClienteServices(ClienteServices clienteServices) {
		this.clienteServices = clienteServices;
	}
	
	

	public NotaDeCargoDao getNotaDeCargoDao() {
		return notaDeCargoDao;
	}

	public void setNotaDeCargoDao(NotaDeCargoDao notaDeCargoDao) {
		this.notaDeCargoDao = notaDeCargoDao;
	}
	public void setCfdiNotaDeCredito(INotaDeCredito cfdiNotaDeCredito) {
		this.cfdiNotaDeCredito = cfdiNotaDeCredito;
	}
	
	
/*
	public ComprobantesDigitalesManager getComprobanteDigitalManager() {
		return comprobanteDigitalManager;
	}

	public void setComprobanteDigitalManager(
			ComprobantesDigitalesManager comprobanteDigitalManager) {
		this.comprobanteDigitalManager = comprobanteDigitalManager;
	}
*/
	private void registrarBitacora(NotaDeCredito bean){
		Date time=new Date();		
		String user=KernellSecurity.instance().getCurrentUserName();	
		//String ip=KernellSecurity.getIPAdress();
		//String mac=KernellSecurity.getMacAdress();
		
		bean.getLog().setModificado(time);
		bean.getLog().setUpdateUser(user);
		//bean.getAddresLog().setUpdatedIp(ip);
		//bean.getAddresLog().setUpdatedMac(mac);
		
		
		if(bean.getId()==null){
			bean.getLog().setCreado(time);
			bean.getLog().setCreateUser(user);
			//bean.getAddresLog().setCreatedIp(ip);
			//bean.getAddresLog().setUpdatedMac(mac);
		}
		
	}
	
	
}
