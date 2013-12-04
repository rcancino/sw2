package com.luxsoft.sw3.impap.ui.form;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.impap.ui.KernellUtils;
import com.luxsoft.sw3.impap.ui.selectores.SelectorDeClientes;
import com.luxsoft.sw3.ventas.Pedido;
import com.luxsoft.sw3.ventas.PedidoDet;




/**
 * Controlado y PresentationModel que se encarga del trabajo pesado para la generacion 
 * de pedidos
 *  
 * 
 * @author Ruben Cancino Ramos
 * TODO Esta clase tiene demasiadas responsabilidades es necesario refactorizar usando los
 *      mejores patrones de diseño disponibles
 *
 */
public class PedidoController extends DefaultFormModel implements ListEventListener{

	private EventList<PedidoDet> partidasSource;
	
	private EventList<Producto> productos;
	
	private PropertyChangeListener handler;
	
	private Logger logger=Logger.getLogger(getClass());
	
	private PedidoFormValidator validator;
	
	
	
	public PedidoController() {
		super(new Pedido());
		getPedido().setTipo(Pedido.Tipo.CREDITO);
		getPedido().setFormaDePago(FormaDePago.TRANSFERENCIA);
	}
	
	public PedidoController(Pedido pedido){
		super(pedido);
	}

	public Pedido getPedido(){
		return (Pedido)getBaseBean();
	}
	
	protected void init(){
		partidasSource=GlazedLists.eventList(new ArrayList<PedidoDet>());
		partidasSource=new SortedList(partidasSource,GlazedLists.beanPropertyComparator(PedidoDet.class, "log.creado"));
		
		if(getValue("id")==null){
			setValue("sucursal", ServiceLocator2.getConfiguracion().getSucursal());
			
		}else{
			partidasSource.addAll(getPedido().getPartidas());			
		}		
		partidasSource.addListEventListener(this);		
		handler=new Handler();
		addBeanPropertyChangeListener(handler);
		validator=new PedidoFormValidator();
		
	}
	
	
	public void dispose(){
		partidasSource.removeListEventListener(this);
		removeBeanPropertyChangeListener(handler);
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		validator.validate(getPedido(), support);
		if(getPedido().getFormaDePago().equals(FormaDePago.CHEQUE_POSTFECHADO)){
			if(!getPedido().isDeCredito()){
				
			}
		}
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
		boolean credito=getPedido().isDeCredito();
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
				validate();
				logger.info("Unidad de venta agregada: "+target);				
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
			BeanUtils.copyProperties(source, target,new String[]{"id","version","log","pedido"});
			boolean credito=getPedido().isDeCredito();
			if(getPedido().getFormaDePago().equals(FormaDePago.CHEQUE_POSTFECHADO))
				credito=false;
			final PedidoDetFormModel model=new PedidoDetFormModel(target);
			model.setCredito(credito);
			final PedidoDetFormWork form=new PedidoDetFormWork(model);
			form.setProductos(getProductos());
			form.open();
			
			if(!form.hasBeenCanceled()){
				
				//Regresamos los valores al bean original
				BeanUtils.copyProperties(target, source, new String[]{"id","version","log","pedido"});
				source.actualizarPrecioDeLista();
				partidasSource.set(index, source);
				logger.info("Partida modificada: "+source);
				validate();
				actualizarImportes();
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
					validate();
					return;
				}
			}else
				System.out.println("Existe un error en la seleccion de partidas");
		}		
	}
	
	/** Seleccion y asignacion de cliente ***/
	
	/*public void insertarClienteNuevo(){
		Cliente c=ClienteController.getInstance().registrar();
		setValue("cliente", c);
	}*/
	
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
				/*
				Cliente cliente=ServiceLocator2.getClienteManager().buscarPorClave(clave);
				Mostrador mostrador=ClienteController.getInstance().getMostrador();
				if(mostrador==null)
					return;
				cliente.setPersonaFisica(mostrador.isPersonaFisica());
				cliente.setApellidoP(mostrador.getApellidoP());
				cliente.setApellidoM(mostrador.getApellidoM());
				cliente.setNombre(mostrador.getNombre());
				cliente.setNombres(mostrador.getNombres());
				getPedido().setCliente(cliente);
				//String nombre=mostrador.getNombre();
				//getPedido().setNombre(nombre);
				//return;
				 */
			}else{
				Cliente cliente=ServiceLocator2.getClienteManager().buscarPorClave(clave);
				/*List<Cliente> clientes=Services.getInstance()
					.getClientesManager()
					.buscarClientePorClave(clave);
				if(clientes.size()==1){
					getPedido().setCliente(clientes.get(0));
				}*/
				getPedido().setCliente(cliente);
			}
		}
	}
	
	/**
	 * Se ejecuta posterior a la asignacion de un cliente
	 * 
	 */
	public void clienteActualizado(){
		if(hayPartidas()){
			actualizarImportes();
		}else{
			if(getPedido().getCliente().isDeCredito())
				setValue("tipo", Pedido.Tipo.CREDITO);
			else
				setValue("tipo", Pedido.Tipo.CONTADO);
			if(getPedido().getCliente().isDeCredito() && getPedido().isDeCredito()){
				if(getPedido().getCliente().getCredito().isChequePostfechado()){
					setValue("formaDePago", FormaDePago.CHEQUE_POSTFECHADO);
				}
			}
		}
		
	}
	
	public boolean isModificable(){
		if((getPedido().isFacturado() )|| (getPedido().getAutorizacion()!=null))
			return false;
		if(StringUtils.containsIgnoreCase(getPedido().getComentario2(), "CANCELADO"))
			return false;		
		else
			return true;
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
	
	
	
	
	private boolean hayPartidas(){
		return !getPedido().getPartidas().isEmpty();
	}
	
	/**
	 * Actualiza los importes del pedido
	 * 
	 */
	public void actualizarImportes(){
		if(isModificable() ){			
			//getManager().asignarDescuento(getPedido());			
			getPedido().actualizarImportes();			
			for(int index=0;index<partidasSource.size();index++){
				PedidoDet det=partidasSource.get(index);
				partidasSource.set(index, det);
			}
		}
	}	
	
	public Pedido persist(){
		Pedido target=getPedido();
		if(target!=null){		
			
			
			Date creado=ServiceLocator2.obtenerFechaDelSistema();
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
			ServiceLocator2.getPedidosManager().salvar(target);
			logger.info("Pedido  persistido: "+target);
			return target;
		}
		return null;
	}
	
	private Pedido salvarPedido(final Pedido pedido){
		for(PedidoDet det:pedido.getPartidas()){
			Assert.notNull(det.getPedido());
			System.out.println(ToStringBuilder.reflectionToString(det, ToStringStyle.MULTI_LINE_STYLE));
		}
		Pedido res=(Pedido)ServiceLocator2.getHibernateTemplate().save(pedido);
		String hql="from Pedido p left join fetch p.partidas where p.id=?";
		return (Pedido)ServiceLocator2.getHibernateTemplate().find(hql,res.getId());
	}
	
	
	protected void partidasChanged() {
		actualizarImportes();
	}
	
	
	/**
	 * Implementacion de {@link ListEventListener} para responder a cambios en las partidas
	 * 
	 */
	public void listChanged(ListEvent listChanges) {
		if(listChanges.next()){
			switch (listChanges.getType()) {
			case ListEvent.INSERT:
			case ListEvent.DELETE:
				partidasChanged();
				break;			
			default:
				break;
			}				
		}
	}	
	
	

	public EventList<PedidoDet> getPartidasSource() {
		return partidasSource;
	}
	
	
	protected EventList<Producto> getProductos(){
		if(productos==null|| productos.isEmpty()){
			productos=new BasicEventList<Producto>();
			productos.addAll(ServiceLocator2.getProductoManager().buscarProductosActivos());			
		}
		return productos;
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
			}else if("cliente".equals(property)){
				clienteActualizado();
			}else if("formaDePago".equals(property)){
				//formaDePagoActualizada();
			}else if("entrega".equals(evt.getPropertyName())){
				//definirFormaDeEntrega();
			}else if("flete".equals(evt.getPropertyName())){
				actualizarImportes();
			}else if("mismaDireccion".equals(property)){
				getPedido().setInstruccionDeEntrega(null);
				//definirFormaDeEntrega();
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
	
}
