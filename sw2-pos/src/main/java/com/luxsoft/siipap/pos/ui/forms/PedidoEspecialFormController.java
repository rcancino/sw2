package com.luxsoft.siipap.pos.ui.forms;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeAsociados;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeClientes;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeComsParaPedidosEspeciales;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeMaqsParaPedidosEspeciales;
import com.luxsoft.siipap.pos.ui.venta.forms.PedidoDetFormModel;
import com.luxsoft.siipap.pos.ui.venta.forms.PedidoDetFormWork;
import com.luxsoft.siipap.pos.ui.venta.forms.PedidoFormSupport;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.ventas.dao.DescPorVolDao;
import com.luxsoft.siipap.ventas.dao.ListaDePreciosClienteDao;
import com.luxsoft.siipap.ventas.model.Asociado;
import com.luxsoft.sw3.services.PedidosManager;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ui.catalogos.ClienteController;
import com.luxsoft.sw3.ui.catalogos.Mostrador;
import com.luxsoft.sw3.ui.services.KernellUtils;
import com.luxsoft.sw3.ventas.InstruccionDeEntrega;
import com.luxsoft.sw3.ventas.Pedido;
import com.luxsoft.sw3.ventas.PedidoDet;


/**
 * Controlado y PresentationModel que se encarga del trabajo pesado para la generacion 
 * de pedidos especiales
 *  
 * 
 * @author Ruben Cancino Ramos
 * TODO Esta clase tiene demasiadas responsabilidades es necesario refactorizar usando los
 *      mejores patrones de diseño disponibles
 *
 */
public class PedidoEspecialFormController extends DefaultFormModel {

	private EventList<PedidoDet> partidasSource;
	
	private EventList<Producto> productos;
	
	private PropertyChangeListener handler;
	
	private Logger logger=Logger.getLogger(getClass());
	
	private PedidoEspecialFormValidator validator;
	
	public PedidoEspecialFormController() {
		super(new Pedido(true));
	}
	
	public PedidoEspecialFormController(Pedido pedido){
		super(pedido);
	}

	public Pedido getPedido(){
		return (Pedido)getBaseBean();
	}
	
	protected void init(){
		Assert.isTrue(getPedido().isEspecial(),"Este controlador solo funciona para pedidos de medida especial");
		partidasSource=GlazedLists.eventList(new ArrayList<PedidoDet>());
		partidasSource=new SortedList(partidasSource,GlazedLists.beanPropertyComparator(PedidoDet.class, "log.creado"));
		
		if(getValue("id")==null){
			setValue("sucursal", Services.getInstance().getConfiguracion().getSucursal());
			
		}else{
			partidasSource.addAll(getPedido().getPartidas());			
		}		
		handler=new Handler();
		addBeanPropertyChangeListener(handler);
		validator=new PedidoEspecialFormValidator();		
	}
	
	public void dispose(){
		removeBeanPropertyChangeListener(handler);
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		validator.validate(getPedido(), support);
		super.addValidation(support);
		if(StringUtils.isBlank(getUsuario())){
			support.getResult().addError("Digite su clave de facturista para salvar el pedido");
		}
	}
	
	/**
	 * Procedmiento para la generacion de una nueva partida
	 * Por el momento este es el lugar en el que se aplican las reglas de negocio
	 * TODO Refactor usando algun Desing Pattern para mover el codigo necesario
	 *      a otro lado
	 */
	public void insertarPartida(){	
		/*if(getPedido().getId()==null){
			insertarPartidaEspecial();
			return;
		}*/
		boolean credito=getPedido().isDeCredito();
		if(getPedido().getFormaDePago().equals(FormaDePago.CHEQUE_POSTFECHADO))
			credito=false;
		final PedidoDetFormModel model=new PedidoDetFormModel(PedidoDet.getPedidoDet());
		model.setCredito(credito);
		model.setSucursal(getPedido().getSucursal());
		final PedidoDetFormWork form=new PedidoDetFormWork(model);
		form.setProductos(getProductos());		
		form.open();
		if(!form.hasBeenCanceled()){
			PedidoDet target=(PedidoDet)form.getModel().getBaseBean();
			boolean ok=getPedido().agregarPartida(target);
			if(ok){
				target.actualizarPrecioDeLista();
				partidasSource.add(target);
				actualizarImportes();
				validate();
				logger.info("Unidad de venta agregada: "+target);				
			}else{
				MessageUtils.showMessage("Ya esta registrado el producto: "+target.getProducto(), "Unidad de venta");
			}
		}
	}
	
	public void insertarPartidaEspecial(){		
		boolean credito=getPedido().isDeCredito();
		if(getPedido().getFormaDePago().equals(FormaDePago.CHEQUE_POSTFECHADO))
			credito=false;
		PedidoDet det=PedidoDet.getPedidoDet();
		det.setPedido(getPedido());
		det.setEspecial(true);
		final PedidoEspecialDetFormModel model=new PedidoEspecialDetFormModel(det);
		model.setCredito(credito);		
		final PedidoEspecialDetForm form=new PedidoEspecialDetForm(model);
		form.setProductos(Services.getInstance().getProductosManager().getMedidasEspeciales());
		form.open();
		if(!form.hasBeenCanceled()){
			PedidoDet target=(PedidoDet)form.getModel().getBaseBean();
			target.actualizarDescripcion();
			boolean ok=getPedido().agregarPartida(target);
			if(ok){
				logger.info("Partida POR INSERTAR: "+target.getClave()+"  Prec:"+target.getPrecio());
				partidasSource.add(target);
				logger.info("Partida insertada: "+target.getClave()+"  Prec:"+target.getPrecio());
				actualizarImportes();
				validate();	
			}else{
				MessageUtils.showMessage("Ya esta registrado el producto: "+target.getProducto(), "Unidad de venta");
			}
		}
	}
	
	public void editar(final PedidoDet source){		
		
		int index=partidasSource.indexOf(source);
		if(index!=-1){
			logger.info("Editando partida: "+index+ ": "+source.getClave());
			final PedidoDet target=PedidoDet.getPedidoDet();			
			//Sacamos una copia de la partida para el caso de q no se quierean los cambios
			BeanUtils.copyProperties(source, target,new String[]{"id","version","log"});
			boolean credito=getPedido().isDeCredito();
			if(getPedido().getFormaDePago().equals(FormaDePago.CHEQUE_POSTFECHADO))
				credito=false;
			
			if(!source.isEspecial()){
				final PedidoDetFormModel model=new PedidoDetFormModel(target);
				model.setCredito(credito);
				final PedidoDetFormWork form=new PedidoDetFormWork(model);
				form.setProductos(getProductos());
				form.open();
				if(!form.hasBeenCanceled()){
					
					//Regresamos los valores al bean original
					BeanUtils.copyProperties(target, source, new String[]{"id","version","log"});
					target.setPedido(null);
					source.actualizarPrecioDeLista();
					partidasSource.set(index, source);
					logger.info("Partida modificada: "+source);
					actualizarImportes();
					validate();					
				}
			}else{
				final PedidoEspecialDetFormModel model=new PedidoEspecialDetFormModel(target);
				model.setCredito(credito);
				model.setReadOnly(target.getEntrada()!=null);
				final PedidoEspecialDetForm form=new PedidoEspecialDetForm(model);
				form.setProductos(Services.getInstance().getProductosManager().getMedidasEspeciales());
				form.open();
				
				if(!form.hasBeenCanceled()){					
					//Regresamos los valores al bean original
					BeanUtils.copyProperties(target, source, new String[]{"id","version","log","pedido"});
					source.actualizarDescripcion();
					partidasSource.set(index, source);					
					actualizarImportes();
					validate();
					logger.info("Partida modificada: "+source);
				}
			}
		}
	}
	
	public void eliminarPartida(int index){
		PedidoDet det=partidasSource.get(index);
		if(MessageUtils.showConfirmationMessage("Eliminar partida: "+det.getClave(), "Pedido")){
			if(det!=null ){
				boolean ok=getPedido().eliminarPartida(det);
				if(ok){					
					partidasSource.remove(index);
					actualizarImportes();
					validate();
					return;
				}
			}else
				System.out.println("Existe un error en la seleccion de partidas");
		}		
	}
	
	/** Seleccion y asignacion de cliente ***/
	
	public void insertarClienteNuevo(){
		Cliente c=ClienteController.getInstance().registrar();
		setValue("cliente", c);
	}
	
	public void seleccionarCliente(Dialog owner){
		if(!isModificable()){
			MessageUtils.showMessage("El no es modificable", "Pedido");
		}else{
			Cliente c=SelectorDeClientes.seleccionar(owner);
			if(c!=null)
				setValue("cliente", c);
		}
	}
	
	public void asignarCliente(String clave){
		if(!StringUtils.isBlank(clave)){
			if("1".equals(clave)){				
				Cliente cliente=Services.getInstance().getClientesManager().buscarPorClave(clave);
				Mostrador mostrador=ClienteController.getInstance().getMostrador();
				if(mostrador==null)
					return;
				cliente.setPersonaFisica(mostrador.isPersonaFisica());
				cliente.setApellidoP(mostrador.getApellidoP());
				cliente.setApellidoM(mostrador.getApellidoM());
				cliente.setNombre(mostrador.getNombre());
				cliente.setNombres(mostrador.getNombres());
				getPedido().setCliente(cliente);				
			}else{
				Cliente cliente=Services.getInstance().getClientesManager().buscarPorClave(clave);
				getPedido().setCliente(cliente);
			}			
		}
	}
	
	/**
	 * Se ejecuta posterior a la asignacion de un cliente
	 * 
	 */
	public void clienteActualizado(){
		if(getPedido().getCliente()==null){
			actualizarImportes();
			return;
		}
		if(getPedido().getCliente().isDeCredito()){
			setValue("tipo", Pedido.Tipo.CREDITO);
			if(getPedido().getCliente().getCredito().isChequePostfechado()){
				setValue("formaDePago", FormaDePago.CHEQUE_POSTFECHADO);
			}if(getPedido().getCliente().getCredito().isCheckplus()){
				setValue("formaDePago", FormaDePago.CHECKPLUS);
			}if(getPedido().getCliente().getFormaDePago()==null)
				setValue("formaDePago", FormaDePago.EFECTIVO);
			else
				setValue("formaDePago",getPedido().getCliente().getFormaDePago());
		/*	else{
				setValue("formaDePago", FormaDePago.EFECTIVO);
			} */
		}else{
			setValue("tipo", Pedido.Tipo.CONTADO);			
		}
		if(getPedido().getCliente().getClave().equals("U050008")){
			Asociado socio=SelectorDeAsociados.seleccionar();
			if(socio!=null){
				getPedido().setSocio(socio);
			}
		}else{
			getPedido().setSocio(null);
		}
		actualizarImportes();
	}
	
	public void modificarSocio(){
		if(getPedido().getCliente().getClave().equals("U050008")){
			if(!getPedido().isFacturado()){
				Asociado socio=SelectorDeAsociados.seleccionar();
				getPedido().setSocio(socio);
			}
		}
	}
	
	public boolean isModificable(){
		return getManager().isModificable(getPedido());
	}
	
	/**
	 * Posteriror a la definicion de tipo de pedido
	 * 
	 */
	protected void tipoDePedidoActualizado(){		
		if(getPedido().isDeCredito()){
			if(getPedido().getCliente()!=null){
				Cliente c=getPedido().getCliente();
				if(c.isChequePostfechado())
					getPedido().setFormaDePago(FormaDePago.CHEQUE_POSTFECHADO);
				else
					getPedido().setFormaDePago(FormaDePago.EFECTIVO);
			}else
				getPedido().setFormaDePago(FormaDePago.EFECTIVO);
		}
		actualizarImportes();
	}
	
	/**
	 *  Posterior a la definicion de forma de entrega
	 */
	public void definirFormaDeEntrega(){
		if(getPedido().getEntrega().equals(Pedido.FormaDeEntrega.ENVIO)
				||
				getPedido().getEntrega().equals(Pedido.FormaDeEntrega.ENVIO_CARGO)){
			
			InstruccionDeEntrega target=getPedido().getInstruccionDeEntrega();
			
			if(target==null){
				if(getPedido().isMismaDireccion()){
					target=new InstruccionDeEntrega();
					target.resolve(getPedido().getCliente().getDireccionFiscal());
				}else{
					String dirSocio=getPedido().getSocio()!=null?getPedido().getSocio().getDireccion():null;
					target=InstruccionDeEntregaForm.crearNueva(getPedido().getCliente(),dirSocio);
				}
			}else{
				String dirSocio=getPedido().getSocio()!=null?getPedido().getSocio().getDireccion():null;
				target=InstruccionDeEntregaForm.modificar(getPedido().getCliente(), target,dirSocio);
			}
			if(target!=null){
				getPedido().setInstruccionDeEntrega(target);
			}
		}else if(getPedido().getEntrega().equals(Pedido.FormaDeEntrega.ENVIO_FORANEO)){
			InstruccionDeEntrega target=getPedido().getInstruccionDeEntrega();
			if(target==null)
				target=InstruccionDeEntregaForm.crearNuevaForaneo(getPedido().getCliente(),null);
			else
				target=InstruccionDeEntregaForm.modificarForaneo(getPedido().getCliente(), target,null);
			if(target!=null){
				getPedido().setInstruccionDeEntrega(target);
			}
		}
		//actualizarImportes();
	}
		
	/**
	 * Actualiza los importes del pedido
	 * 
	 */
	private void actualizarImportes(){
		logger.info("Recalculando importes....");
		if(isModificable() ){
			asignarDescuento();
			getPedido().actualizarImportes();
			PedidoFormSupport.actualizarCortes(getPedido(), partidasSource);
			PedidoFormSupport.actualizarManiobras(getPedido(), partidasSource);
			getPedido().actualizarImportes();
			//Refresh the event list to update UI
			for(int index=0;index<partidasSource.size();index++){
				PedidoDet det=partidasSource.get(index);
				partidasSource.set(index, det);
			}
		}
	}
	
	public boolean calificaParaDescuentoEspecial(){
		return getManager().calificaParaDescuentoEspecial(getPedido());
	}
	
	public void aplicarDescuentoEspecial(double descuento){
		if(calificaParaDescuentoEspecial()){
			getPedido().setDescuentoEspecial(descuento);
			actualizarImportes();
		}
	}
	
	public Pedido persist(){
		Pedido target=getPedido();
		if(target!=null){		
			
			
			Date creado=Services.getInstance().obtenerFechaDelSistema();
			String user=getUsuario();
			
			if(target.getId()==null){
				target.getLog().setCreado(creado);
				target.getLog().setCreateUser(user);
				target.getAddresLog().setCreatedMac(KernellSecurity.getMacAdress());				
				target.getAddresLog().setCreatedIp(KernellSecurity.getIPAdress());
			}
			target.getLog().setModificado(creado);
			target.getLog().setUpdateUser(user);			
			target.getAddresLog().setUpdatedIp(KernellSecurity.getIPAdress());
			target.getAddresLog().setUpdatedMac(KernellSecurity.getMacAdress());
			target=getManager().save(target);
			
			if(target.getPendiente()!=null){
				MessageUtils.showMessage("El pedido requiere de autorización, estará en espera de la misma para poder" +
						" facturar", "Autorización pendiente");
			}
			logger.info("Pedido  persistido: "+target);
			return target;
		}
		return null;
	}
	
	
	protected void partidasChanged() {
		actualizarImportes();
	}

	public EventList<PedidoDet> getPartidasSource() {
		return partidasSource;
	}
	
	protected EventList<Producto> getProductos(){
		if(productos==null|| productos.isEmpty()){
			productos=new BasicEventList<Producto>();
			productos.addAll(Services.getInstance().getProductosManager().getActivos());			
		}
		return productos;
	}
	
	public PedidosManager getManager(){
		return Services.getInstance().getPedidosManager();
	}
	
	/**
	 * TODO Pasar a PedidoFormModel
	 *  
	 * @return
	 */
	public List<FormaDePago> getFormasDePago(){
		List<FormaDePago> formas=new ArrayList<FormaDePago>();
		formas.addAll(Arrays.asList(FormaDePago.values()));
		return formas;
	}
	
	private class Handler implements PropertyChangeListener{

		public void propertyChange(PropertyChangeEvent evt) {
			String property=evt.getPropertyName();
			if("tipo".equals(property)){
				tipoDePedidoActualizado();
				actualizarImportes();
			}else if("cliente".equals(property)){
				clienteActualizado();
			}else if("formaDePago".equals(property)){
				actualizarImportes();
			}else if("entrega".equals(evt.getPropertyName())){
				definirFormaDeEntrega();
			}else if("flete".equals(evt.getPropertyName())){
				actualizarImportes();
			}else if("mismaDireccion".equals(property)){
				getPedido().setInstruccionDeEntrega(null);
				definirFormaDeEntrega();
			}else if("mismoComprador".equals(property)){
				Boolean res=(Boolean)evt.getNewValue();
				if(res)
					getPedido().setComprador(getPedido().getNombre());
				else
					getPedido().setComprador(null);
				
			}else{
				//logger.debug("Propiedad actualizada sin delegado: "+evt.getPropertyName() + "Valor: "+evt.getNewValue());
			}			
		}		
	}
	
	private ValueModel usuarioHolder=new ValueHolder();
	private ValueModel passworHoler;
	private ValueModel facturistaHolder=new ValueHolder();
	

	public ValueModel getUsuarioHolder() {
		return usuarioHolder;
	}
	
	public ValueModel getPasswordHolder(){
		if(passworHoler==null){
			passworHoler=new ValueHolder();
			passworHoler.addValueChangeListener(new PropertyChangeListener(){
				public void propertyChange(PropertyChangeEvent evt) {
					resolveUser(evt.getNewValue());
				}
				
			});
		}
		return passworHoler;
	}

	public void setUsuarioHolder(ValueModel usuarioHolder) {
		this.usuarioHolder = usuarioHolder;
	}
	
	public String getUsuario(){
		if(getUsuarioHolder().getValue()==null)
			return null;
		User user= (User)getUsuarioHolder().getValue();
		return user.getUsername();
	}
	
	private void resolveUser(Object newValue) {
		String s=(String)newValue;
		usuarioHolder.setValue(KernellUtils.buscarUsuarioPorPassword(s));
		if(getPedido().getId()==null)
			getPedido().getLog().setCreateUser(getUsuario());
		getPedido().getLog().setUpdateUser(getUsuario());
		if(usuarioHolder.getValue()==null){
			facturistaHolder.setValue(null);
		}else{
			User user=(User)usuarioHolder.getValue();
			facturistaHolder.setValue(user.getFullName());
		}
		validate();
	}
	
	public ValueModel getFacturistaHolder(){
		if(facturistaHolder==null){
			facturistaHolder=new ValueHolder();
		}
		return facturistaHolder;
	}
	
	private void asignarDescuento() {
		
		Pedido pedido=getPedido();
		if(pedido.isAnticipo())
			return;
		if(pedido.getCliente()==null)
			return;		
		
		//Actualizar el precio de lista
		
		for(PedidoDet det:pedido.getPartidas()){
			if(det.isEspecial())
				continue;
			double precioLista=pedido.isDeCredito()?det.getProducto().getPrecioCredito():det.getProducto().getPrecioContado();
			if(pedido.getFormaDePago().equals(FormaDePago.CHEQUE_POSTFECHADO))
				precioLista=det.getProducto().getPrecioContado();
			det.setPrecioLista(BigDecimal.valueOf(precioLista));
		}		
		
		// Caso credito
		if((pedido.getTipo().equals(Pedido.Tipo.CREDITO)) && !pedido.getFormaDePago().equals(FormaDePago.CHEQUE_POSTFECHADO)){	
			pedido.actualizarImportes();
			double descuento=pedido.getCliente().getCredito().getDescuentoEstimado();
			for(PedidoDet det:pedido.getPartidas()){
				
				
				BigDecimal precio=det.getPrecio();
				if(!det.isEspecial())
					precio=BigDecimal.valueOf(det.getProducto().getPrecioCredito());
				//Obtener descuento especial por producto del cliente (Si es que tiene )
				double descuentoPorProducto=getListaDao().buscarDescuentoPorProducto(pedido.getCliente(), det.getProducto(), pedido.getMoneda());
				
				//Ponchar el precio del catalogo (Credito)
				if(descuentoPorProducto>0){
					precio=MonedasUtils.aplicarDescuentosEnCascada(precio, descuentoPorProducto);
				}				
				det.setPrecio(precio);
				
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
			//pedido.actualizarImportes();
			
		}else if(pedido.getTipo().equals(Pedido.Tipo.CONTADO) || pedido.getFormaDePago().equals(FormaDePago.CHEQUE_POSTFECHADO)){
			
			for(PedidoDet det:pedido.getPartidas()){
				if(det.isEspecial()){
					det.setPrecioOriginal(det.getPrecio());
				}else{
					det.setPrecio(BigDecimal.valueOf(det.getProducto().getPrecioContado()));
					det.setPrecioOriginal(det.getPrecio());					
				}
				if(det.getPrecioEspecial().doubleValue()>0){
					det.setPrecio(det.getPrecioEspecial());
				}
			}
			//Actualizamos el total del pedido para calcular el % dscto 
			pedido.actualizarImportes();
			BigDecimal importeBruto=pedido.getImporteBrutoParaDescuento();
			
			double descuento=getDescDao().buscarDescuentoContado(importeBruto.doubleValue());
			
			if(pedido.getFormaDePago().equals(FormaDePago.TARJETA_CREDITO)){
				descuento-=2.00;
			}if(pedido.getFormaDePago().equals(FormaDePago.TARJETA_DEBITO)){
				descuento-=1.00;
			}if(pedido.getFormaDePago().equals(FormaDePago.CHEQUE_POSTFECHADO)){
				descuento-=getCargoChequePostFechado();
			}
			if(descuento<0)
				descuento=0;
			pedido.setDescuentoOrigen(descuento);
			if(pedido.getDescuentoEspecial()>0){				
				descuento=pedido.getDescuentoEspecial();
			}
			for(PedidoDet det:pedido.getPartidas()){				
				if(det.getProducto().getModoDeVenta().endsWith("B"))
					det.setDescuento(descuento);
				else
					det.setDescuento(0);
			}
			pedido.setDescuento(descuento);
			//pedido.actualizarImportes();
		}
		
	}
	
	public void asignarPrecioEspecial(PedidoDet selected,BigDecimal precioNuevo) {
		int index=partidasSource.indexOf(selected);
		if(index!=-1){
			//selected.setPrecioOriginal(selected.getPrecio());
			//selected.setPrecio(precioNuevo);
			selected.setPrecioEspecial(precioNuevo);
			actualizarImportes();
			partidasSource.set(index, selected);
		}else{
			System.out.println("No encuentra registro: "+selected);
		}
		
	}
	
	public void asignarEntradaPorCompra(PedidoDet selected){
		EntradaPorCompra com=SelectorDeComsParaPedidosEspeciales.find(selected.getClave());
		
		if(com!=null){
			selected.setCantidad(com.getCantidad());
			selected.setTipoEntrada("COM");
			selected.setEntrada(com.getId());
			actualizarImportes();
		}
	}
	
	public void asignarEntradaPorMaquila(PedidoDet selected){
		EntradaDeMaquila maq=SelectorDeMaqsParaPedidosEspeciales.find(selected.getClave());
		if(maq!=null){
			System.out.println("Actualizando  entrada de maquila para medida especial....");
			selected.setCantidad(maq.getCantidad());
			selected.setTipoEntrada("MAQ");
			selected.setEntrada(maq.getId());
			actualizarImportes();
		}
	}
	
	private double getCargoChequePostFechado(){
		return 4d;
	}
	
	private DescPorVolDao getDescDao(){
		return (DescPorVolDao)(Services.getInstance().getContext().getBean("descPorVolDao"));
	}
	private ListaDePreciosClienteDao getListaDao(){
		return (ListaDePreciosClienteDao)Services.getInstance().getContext().getBean("listaDePreciosClienteDao");
	}
	
}
