package com.luxsoft.sw3.services;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.dao.core.FolioDao;
import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.Folio;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.ventas.dao.DescPorVolDao;
import com.luxsoft.siipap.ventas.dao.ListaDePreciosClienteDao;
import com.luxsoft.sw3.embarque.ZonaDeEnvio;
import com.luxsoft.sw3.model.EvaluadorGenerico;
import com.luxsoft.sw3.ventas.AutorizacionDePedido;
import com.luxsoft.sw3.ventas.InstruccionDeEntrega;
import com.luxsoft.sw3.ventas.Pedido;
import com.luxsoft.sw3.ventas.PedidoDet;
import com.luxsoft.sw3.ventas.PedidoPendiente;
import com.luxsoft.sw3.ventas.Pedido.FormaDeEntrega;
import com.luxsoft.sw3.ventas.Pedido.Tipo;
import com.luxsoft.sw3.ventas.dao.PedidoDao;
import com.luxsoft.sw3.ventas.rules.PedidoProcessor;

@Service("pedidosManager")
@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class PedidosManagerImpl implements PedidosManager{
	
	private Logger logger=Logger.getLogger(getClass());
	
	@Autowired
	private FolioDao folioDao;
	
	@Autowired
	private PedidoDao pedidoDao;
	
	@Autowired
	private DescPorVolDao descPorVolDao;
	
	@Autowired
	private ListaDePreciosClienteDao listaDePreciosClienteDao;
	
	@Autowired
	@Qualifier("evaluadorParaAutorizacionDePedidos")
	private EvaluadorGenerico evaluadorDeAutorizaciones;
	
	@Autowired 
	private InventariosManager inventariosManager;	
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	/*@Autowired 
	@Qualifier("procesadorDeComisionPorTarjeta")
	private PedidoProcessor comisionPorTarjeta;
	
	
	@Autowired	@Qualifier("procesadorDeComisionPorCheque")
	private PedidoProcessor comisionPorCheque;
	*/
	
	private double cargoChequePostFechado=4.0;
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	

	public boolean exists(String id) {
		return getPedidoDao().exists(id);
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public Pedido get(String id) {
		Pedido p= getPedidoDao().get(id);
		Hibernate.initialize(p.getCliente().getTelefonos());
		Hibernate.initialize(p.getCliente().getContactos());
		Hibernate.initialize(p.getCliente().getComentarios());
		//System.out.println("Telefonos inicializados: "+p.getCliente().getTelefonos().size());
		return p;
		
	}

	/**
	 * Regresa un numer limitado de pedidos
	 * 
	 */
	public List<Pedido> getAll() {
		return getPedidoDao().getAll();
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public void remove(String id) {
		getPedidoDao().remove(id);
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public Pedido save(Pedido pedido) {
		Assert.isTrue(!pedido.isFacturado(),"El pedido ya esta facturado por lo que no es posible actualizar ni modificar");
		if(pedido.getId()==null){
			Folio folio=folioDao.buscarNextFolio(pedido.getSucursal(), "PEDIDO");
			pedido.setFolio(folio.getFolio());
			folioDao.save(folio);
		}
		//if(pedido.getAutorizacion()!=null)
			//Assert.isNull(pedido.getAutorizacion().getId(),"El pedido ya esta registrado como autorizado por lo que no es modificable");
		/*if(pedido.getId()==null){
			String solicitudDesc=evaluadorDeAutorizaciones.requiereParaSalvar(pedido);
			if(solicitudDesc!=null){
				PedidoPendiente pendiente=new PedidoPendiente();
				pendiente.setComentario(solicitudDesc);				
				pedido.setPendiente(pendiente);
			}
		}*/
		String solicitudDesc=evaluadorDeAutorizaciones.requiereParaSalvar(pedido);		
		if(solicitudDesc!=null){			
			PedidoPendiente pendiente=pedido.getPendiente();
			if(pendiente==null){
				pendiente=new PedidoPendiente();
				pendiente.setComentario(solicitudDesc);				
				pedido.setPendiente(pendiente);
			}
		}
		
		//Evaluar si tiene precio especial
		for(PedidoDet det:pedido.getPartidas()){
			if(det.getPrecioEspecial().doubleValue()>0){
				PedidoPendiente pendiente=pedido.getPendiente();
				if(pendiente==null){
					pendiente=new PedidoPendiente();
					pendiente.setComentario("Precio especial en una o mas partidas");				
					pedido.setPendiente(pendiente);
				}else{
					pendiente.setComentario2("Precio especial en una o mas partidas");
				}
				break;
			}
		}
		
		if(pedido.getEntrega().equals(Pedido.FormaDeEntrega.LOCAL)){
			InstruccionDeEntrega ie=pedido.getInstruccionDeEntrega();
			if(ie!=null){
				hibernateTemplate.delete(ie);
				pedido.setInstruccionDeEntrega(null);
			}
			pedido.setPagoContraEntrega(null);
		}
		
		if(pedido.isDeCredito()){
			pedido.setPagoContraEntrega(null);
		}
		
		//Cliente cliente=pedido.getCliente();
		//pedido.setClave(cliente.getClave());
		//pedido.setNombre(cliente.getNombre());
		Pedido res= getPedidoDao().save(pedido);
		
		/*
		if(res.getClave().equals("1")){
			com.luxsoft.siipap.model.core.Cliente c=res.getCliente();
			c.setNombre("MOSTRADOR");
			c.setApellidoM("1");
			c.setApellidoP("1");
			c.setNombres("1");
			c.setDireccionFiscal(new Direccion());
			//
			hibernateTemplate.save(c);
		}
		*/
		return res;
	}
	
	
	
	@Transactional(propagation=Propagation.SUPPORTS)
	public void asignarDescuento(Pedido pedido) {
		
		if(pedido.isAnticipo())
			return;
		for(PedidoDet det:pedido.getPartidas()){
			double precioLista=pedido.isDeCredito()?det.getProducto().getPrecioCredito():det.getProducto().getPrecioContado();
			if(pedido.getFormaDePago().equals(FormaDePago.CHEQUE_POSTFECHADO) || pedido.getFormaDePago().equals(FormaDePago.CHECKPLUS))
				precioLista=det.getProducto().getPrecioContado();
			
			det.setPrecioLista(BigDecimal.valueOf(precioLista));
			//det.setPrecio(det.getPrecioLista());
		}
		//pedido.actualizarImportes();
		
		if((pedido.getCliente()!=null) //Cliente no nulo
				&& (pedido.getTipo().equals(Pedido.Tipo.CREDITO)) //Credito 
				&& pedido.getCliente().isDeCredito() 
				&& !(pedido.getCliente().isSuspendido()) 
				&& (!pedido.getCliente().getCredito().isSuspenderDescuento()
				&& !pedido.getFormaDePago().equals(FormaDePago.CHEQUE_POSTFECHADO)
				&& !pedido.getFormaDePago().equals(FormaDePago.CHECKPLUS)
				//&& !pedido.getFormaDePago().equals(FormaDePago.CHECKPLUS)
				)) 
		{	
			pedido.actualizarImportes();
			double descuento=pedido.getCliente().getCredito().getDescuentoEstimado();
			
			for(PedidoDet det:pedido.getPartidas()){
				
				
				BigDecimal precio=BigDecimal.valueOf(det.getProducto().getPrecioCredito());
				
				//Obtener descuento especial por producto del cliente (Si es que tiene )
				double descuentoPorProducto=listaDePreciosClienteDao.buscarDescuentoPorProducto(pedido.getCliente(), det.getProducto(), pedido.getMoneda());
				
				//Ponchar el precio del catalogo (Credito)
				if(descuentoPorProducto!=0){
					
					precio=MonedasUtils.aplicarDescuentosEnCascada(precio, descuentoPorProducto);
				}
				
				det.setPrecio(precio);
				//BigDecimal precio=listaDePreciosClienteDao.buscarPrecio(pedido.getCliente(), det.getProducto(), pedido.getMoneda());	
				
				if(precio!=null && (precio.doubleValue()>0)){
					det.setPrecioOriginal(precio);
					det.setPrecio(precio);					
				}else
					det.setPrecioOriginal(det.getPrecio());
				
				//Si se asigno un precio especial
				if(det.getPrecioEspecial().doubleValue()>0){
					det.setPrecio(det.getPrecioEspecial());
				}
				det.setDescuento(descuento);
			}
			pedido.actualizarImportes();
			
		}else{ /// Para pedidos de Contado o Cheque post fechado
			
			for(PedidoDet det:pedido.getPartidas()){					
				det.setPrecio(BigDecimal.valueOf(det.getProducto().getPrecioContado()));
				det.setPrecioOriginal(det.getPrecio());	
				if(det.getPrecioEspecial().doubleValue()>0){
					det.setPrecio(det.getPrecioEspecial());
				}
			}
			pedido.actualizarImportes();
			BigDecimal importeBruto=pedido.getImporteBrutoParaDescuento();
			System.out.println("El importe Bruot para descuento  es___----------------------------_________-----------------"+ pedido.getImporteBrutoParaDescuento());
			double descuentoVolumen=descPorVolDao.buscarDescuentoContado(importeBruto.doubleValue());
			double descuento=descuentoVolumen;
			if(pedido.getFormaDePago().equals(FormaDePago.CHEQUE_POSTFECHADO)){
				descuento=descuento-getCargoChequePostFechado();
			}
			if(pedido.getFormaDePago().equals(FormaDePago.TARJETA_CREDITO)){
				descuento=descuentoVolumen-2.00;
			}
			if(pedido.getFormaDePago().equals(FormaDePago.TARJETA_DEBITO)){
				descuento=descuentoVolumen-1.00;
			}
			if(pedido.getFormaDePago().equals(FormaDePago.CHECKPLUS)){
				descuento=descuentoVolumen-pedido.getCheckplusOpcion().getCargo().doubleValue();
			}
			if(descuento<0)
				descuento=0;
			pedido.setDescuentoOrigen(descuento);
			if(pedido.getDescuentoEspecial()>0){				
				descuento=pedido.getDescuentoEspecial();
			}
			
			for(PedidoDet det:pedido.getPartidas()){
				//det.setPrecio(BigDecimal.valueOf(det.getProducto().getPrecioContado()));
				if(det.getProducto().getModoDeVenta().endsWith("B"))
					det.setDescuento(descuento);
				else
					det.setDescuento(0);
			}
			
			pedido.setDescuento(descuento);
			pedido.actualizarImportes();
		}
		
	}
	
	 
	
	/**
	 * Regresa la lista de los pedidos pendientes por facturar
	 * 
	 * @param sucursal
	 * @return
	 */
	public List<Pedido> buscarPendientes(final Sucursal sucursal){
		return pedidoDao.buscarPendientes(sucursal);
	}
	
	public List<Pedido> buscarFacturables(final Sucursal sucursal){
		return pedidoDao.buscarFacturables(sucursal);
	}
	
	
	
	public Pedido buscarPorFolio(Long folio) {
		return pedidoDao.buscarPorFolio(folio);
	}

	public List<Pedido> buscarFacturables(Sucursal sucursal, Tipo tipo) {		
		return pedidoDao.buscarFacturables(sucursal, tipo);
	}

	public boolean isModificable(Pedido pedido) {
		if((pedido.isFacturado() )|| (pedido.getAutorizacion()!=null))
			return false;
		if(StringUtils.containsIgnoreCase(pedido.getComentario2(), "CANCELADO"))
			return false;		
		else
			return true;
	}
	
	public boolean isFacturable(final Pedido pedido){
		if(pedido.isFacturado())
			return false;
		if(pedido.isPorAutorizar())
			return pedido.getAutorizacion()!=null;
		if(pedido.getTotal().doubleValue()<=0)
			return false;
		if(StringUtils.containsIgnoreCase(pedido.getComentario2(), "CANCELADO"))
			return false;		
		return true;
	}
	
	/**
	 * Genera las comisiones aplicables  al pedido sin persistirlo
	 * 
	 * @param pedido
	 
	public void aplicarComisiones(final Pedido pedido){
		logger.debug("Generando comisiones aplicables para el pedido ");
		comisionPorTarjeta.process(pedido);
		comisionPorCheque.process(pedido);
	}
	*/
	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.sw3.services.PedidosManager#calificaParaDescuentoEspecial(com.luxsoft.sw3.ventas.Pedido)
	 */
	public boolean calificaParaDescuentoEspecial(final Pedido pedido) {
		if ( (isModificable(pedido)) && ( !pedido.isDeCredito()) ){
			for(PedidoDet det:pedido.getPartidas()){
				if(!det.getProducto().getModoDeVenta().equals("B"))
					return false;
				return true;
			}
		}
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.sw3.services.PedidosManager#aplicarFlete(com.luxsoft.sw3.ventas.Pedido)
	 */
	public void aplicarFlete(final Pedido pedido){
		if(FormaDeEntrega.ENVIO_CARGO.equals(pedido.getEntrega())){
			InstruccionDeEntrega inst=pedido.getInstruccionDeEntrega();
			if(inst!=null){
				String hql="from ZonaDeEnvio z";
				List<ZonaDeEnvio> zonas=hibernateTemplate.find(hql);
				for(ZonaDeEnvio zona:zonas){
					if(zona.getEstado().equalsIgnoreCase(inst.getEstado()))
						if(zona.getCiudad().equalsIgnoreCase(inst.getMunicipio())){
							BigDecimal flete=zona.getTarifa();
							double toneladas=pedido.getKilos()/1000;							
							double cargos=Math.ceil(toneladas/zona.getMultiplo());
							flete=flete.multiply(BigDecimal.valueOf(cargos));
							pedido.setFlete(flete);
						}
				}
			}
		}		
		//Localizar la ciudad y  estado 
	}

	

	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.sw3.services.PedidosManager#actualizarFechaDeImpresion(com.luxsoft.sw3.ventas.Pedido, java.util.Date)
	 */
	public Pedido actualizarFechaDeImpresion(Pedido pedido, Date fecha) {
		Pedido target=get(pedido.getId());
		if(target.getImpreso()==null){
			target.setImpreso(fecha);
			return save(target);
		}else
			return pedido;
	}
	
	public boolean calificaPagoContraEntrega(final Pedido pedido){
		
		if(pedido.isFacturado())
			return false;
		
		if(pedido.getPagoContraEntrega()!=null)
			return false;
		
		if(pedido.isDeCredito())
			return false;
		if(pedido.getInstruccionDeEntrega()==null)
			return false;
		
		return true;
		/*
		return (
				isModificable(pedido)
				&& (!pedido.isDeCredito()) 
				&& (pedido.getInstruccionDeEntrega()!=null) 
				&&(!pedido.isContraEntrega())
				);
				*/
	}
	
	/**
	 * Actualiza los importes del pedido en moficiaciones de forma de pago, pero no persiste el pedido
	 * 
	 * @param pedido
	 */
	
	public void actualizarFormaDePago(final Pedido pedido){
		if(!pedido.isFacturado()){
			if(pedido.getFormaDePago().equals(FormaDePago.TARJETA_CREDITO)){
				pedido.setComisionTarjeta(2);
				//pedido.setDescripcionFormaDePago(FormaDePago.TARJETA.name());
			}else if(pedido.getFormaDePago().equals(FormaDePago.TARJETA_DEBITO)){
				pedido.setComisionTarjeta(1);
			}else{
				pedido.setComisionTarjeta(0);
				//pedido.setDescripcionFormaDePago(null);
			}
			asignarDescuento(pedido);
			//pedido.actualizarImportes();
			//aplicarComisiones(pedido); //DEPCIATED
			//pedido.actualizarImportes();
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.sw3.services.PedidosManager#eliminarPedidos(java.util.Date)
	 */
	@Transactional(propagation=Propagation.NEVER,readOnly=false)
	public int eliminarPedidos(final Date antesDe) {
		String sql="SELECT P.PEDIDO_ID FROM sx_pedidos p where P.FECHA<? AND p.PEDIDO_ID NOT in(SELECT V.PEDIDO_ID FROM sx_ventas v where V.PEDIDO_ID=P.PEDIDO_ID)";
		Object[] params=new Object[]{new SqlParameterValue(Types.DATE, antesDe)};
		List<String> pedidosIds=this.jdbcTemplate.queryForList(sql,params,String.class);
		/*String hql="select p.id from Pedido p where p.fecha<? and " +
				"p.id not in(select v.pedido.id from Venta v) order by p.folio ";
		List<String> pedidosIds=this.hibernateTemplate.find(hql, antesDe);
		System.out.println("Pedidos a eliminar: "+pedidosIds.size());*/
		Set<String> ids=new HashSet<String>(pedidosIds);
		int deleted=0;
		for(String id:ids){
			try {
				//this.hibernateTemplate.delete(p);
				Pedido p=(Pedido)this.hibernateTemplate.get(Pedido.class, id);
				this.hibernateTemplate.delete(p);
				//getPedidoDao().remove(id);
				this.hibernateTemplate.flush();
				this.hibernateTemplate.clear();
				deleted++;
			} catch (Exception e) {
				e.printStackTrace();
				this.hibernateTemplate.clear();
				System.out.println("Imposible eliminar pedido: "+id);
			}			
		}
		return deleted;
	}
	
	
	
	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public Pedido generarCopia(final String pedidoId,final Date creado,final String user){
		return (Pedido)this.hibernateTemplate.execute(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				Pedido source=(Pedido)session.load(Pedido.class, pedidoId);
				Pedido target=new Pedido();
				BeanUtils.copyProperties(source, target
						,new String[]{"id","version"
						,"folio","fecha","autorizacion"
						,"pagoContraEntrega","pendiente","log","addresLog","partidas"
						,"tarjeta","esquema","totalFacturado"
						}
				);				
				target.setComentario("Copia del pedido: "+source.getFolio());
				target.setComentario2(source.getId());
				target.setFacturable(false);
				Assert.isNull(target.getId());
				Assert.isTrue(target.getVersion()==0);
				int buff=1000;
				
				for(PedidoDet sourceDet:source.getPartidas()){
					PedidoDet targetDet=new PedidoDet();
					Date creado=new Date(System.currentTimeMillis()+buff);					
					targetDet.getLog().setCreado(creado);
					buff+=1000;
					BeanUtils.copyProperties(sourceDet, targetDet,new String[]{"id","version","log","pedido"});
					target.agregarPartida(targetDet);
				}
				PedidoPendiente pendiente=new PedidoPendiente();
				pendiente.setComentario("Refacturación pedido orig: "+source.getFolio()+ " "+source.getOrigen());
								
				target.setPendiente(pendiente);
				
				target.getLog().setCreado(creado);
				target.getLog().setCreateUser(user);
				target.getAddresLog().setCreatedMac(KernellSecurity.getMacAdress());				
				target.getAddresLog().setCreatedIp(KernellSecurity.getIPAdress());
				target.getLog().setModificado(creado);
				target.getLog().setUpdateUser(user);			
				target.getAddresLog().setUpdatedIp(KernellSecurity.getIPAdress());
				target.getAddresLog().setUpdatedMac(KernellSecurity.getMacAdress());
				return save(target);
			}
		});
	}

	/*** IoC Colaboradores *****/	

	public PedidoDao getPedidoDao() {
		return pedidoDao;
	}

	
	public void setPedidoDao(PedidoDao pedidoDao) {
		this.pedidoDao = pedidoDao;
	}

	public ListaDePreciosClienteDao getListaDePreciosClienteDao() {
		return listaDePreciosClienteDao;
	}

	public void setListaDePreciosClienteDao(
			ListaDePreciosClienteDao listaDePreciosClienteDao) {
		this.listaDePreciosClienteDao = listaDePreciosClienteDao;
	}

	public void setEvaluadorDeAutorizaciones(
			EvaluadorGenerico evaluadorDeAutorizaciones) {
		this.evaluadorDeAutorizaciones = evaluadorDeAutorizaciones;
	}

	public InventariosManager getInventariosManager() {
		return inventariosManager;
	}

	public void setInventariosManager(InventariosManager inventariosManager) {
		this.inventariosManager = inventariosManager;
	}

	public double getCargoChequePostFechado() {
		return cargoChequePostFechado;
	}

	public void setCargoChequePostFechado(double cargoChequePostFechado) {
		this.cargoChequePostFechado = cargoChequePostFechado;
	}
	
	public static void main(String[] args) {
		POSDBUtils.whereWeAre();
		Services.getInstance().getPedidosManager().eliminarPedidos(DateUtil.toDate("02/11/2011"));
	}

	
}
