package com.luxsoft.siipap.pos.ui.venta.forms;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.jasperreports.engine.JasperManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;

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
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.ProductoRow;
import com.luxsoft.siipap.pos.ui.forms.InstruccionDeEntregaForm;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeAsociados;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeCheckplus;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeClientes;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDePedidosPendientes;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.browser.JRBrowserReportForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Asociado;
import com.luxsoft.sw3.pedidos.PedidoUtils;
import com.luxsoft.sw3.pedidos.forms.PedidoDetForm2;
import com.luxsoft.sw3.pedidos.forms.PedidoDetFormModel2;
import com.luxsoft.sw3.services.PedidosManager;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ui.catalogos.ClienteController;
import com.luxsoft.sw3.ui.catalogos.Mostrador;
import com.luxsoft.sw3.ui.services.KernellUtils;
import com.luxsoft.sw3.ventas.CheckPlusOpcion;
import com.luxsoft.sw3.ventas.InstruccionDeEntrega;
import com.luxsoft.sw3.ventas.Pedido;
import com.luxsoft.sw3.ventas.PedidoDet;
import java.util.*;

import javax.swing.JOptionPane;





/**
 * Controlado y PresentationModel que se encarga del trabajo pesado para la generacion 
 * de pedidos
 *  
 * 
 * @author Ruben Cancino Ramos
 * TODO Esta clase tiene demasiadas responsabilidades es necesario refactorizar usando los
 *      mejores patrones de diseo disponibles
 *
 */
public class PedidoController extends DefaultFormModel implements ListEventListener{

	private EventList<PedidoDet> partidasSource;
	
	private EventList<ProductoRow> productos;
	
	private PropertyChangeListener handler;
	
	private Logger logger=Logger.getLogger(getClass());
	
	private PedidoFormValidator validator;
	
	
	
	public PedidoController() {
		super(new Pedido());
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
			setValue("sucursal", Services.getInstance().getConfiguracion().getSucursal());
			
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
		if(getPedido().getFormaDePago().equals(FormaDePago.CHEQUE_POSTFECHADO))
			credito=false;
		final PedidoDetFormModel2 model=new PedidoDetFormModel2(PedidoDet.getPedidoDet());
		model.setCredito(credito);
		model.setSucursal(getPedido().getSucursal());
		final PedidoDetForm2 form=new PedidoDetForm2(model);
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
	
	public void editar(final PedidoDet source){		
		
		int index=partidasSource.indexOf(source);
		if(index!=-1){
			if(source.getProducto().getClave().equals("CORTE") || source.getProducto().getClave().equals("MANIOBRA"))
				return;
			logger.info("Editando partida: "+index+ ": "+source.getClave());
			final PedidoDet target=PedidoDet.getPedidoDet();			
			//Sacamos una copia de la partida para el caso de q no se quierean los cambios
			BeanUtils.copyProperties(source, target,new String[]{"id","version","log","pedido"});
			boolean credito=getPedido().isDeCredito();
			if(getPedido().getFormaDePago().equals(FormaDePago.CHEQUE_POSTFECHADO))
				credito=false;
			final PedidoDetFormModel2 model=new PedidoDetFormModel2(target);
			model.setCredito(credito);
			final PedidoDetForm2 form=new PedidoDetForm2(model);
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
		if(det.getProducto().getClave().equals("CORTE") || det.getProducto().getClave().equals("MANIOBRA"))
			return;
		if(MessageUtils.showConfirmationMessage("Eliminar partida: "+det.getClave(), "Pedido")){
			if(det!=null ){
				boolean ok=getPedido().eliminarPartida(det);
				if(ok){
					partidasSource.remove(index);
					//actualizarCortes(getPedido());
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
				//String nombre=mostrador.getNombre();
				//getPedido().setNombre(nombre);
				//return;
			}else{
				Cliente cliente=Services.getInstance().getClientesManager().buscarPorClave(clave);
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
		if(getPedido().getCliente()==null) return;
		warning(getPedido().getCliente());
		if(getPedido().getCliente().isDeCredito()){
			setValue("tipo", Pedido.Tipo.CREDITO);
			
			if(getPedido().getCliente().getFormaDePago().equals(FormaDePago.NA)){
				setValue("formaDePago", FormaDePago.EFECTIVO);
			}	
			else{
				setValue("formaDePago",getPedido().getCliente().getFormaDePago());
			}
			if(getPedido().getCliente().getCredito().isChequePostfechado()){
				setValue("formaDePago", FormaDePago.CHEQUE_POSTFECHADO);
			}
			if(getPedido().getCliente().getCredito().isCheckplus()){
				setValue("formaDePago", FormaDePago.CHECKPLUS);
			}
			/*if(getPedido().getCliente().getFormaDePago().equals(FormaDePago.DEPOSITO_CHEQUE)|| getPedido().getCliente().getFormaDePago().equals(FormaDePago.DEPOSITO_EFECTIVO)){
				setValue("formaDePago", FormaDePago.DEPOSITO);
			}*/
			if(getPedido().getCliente().getCredito().isCheckplus()){
				setValue("formaDePago", FormaDePago.CHECKPLUS);
			}
			
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
		if(!getPedido().getCliente().getClave().equals("1") && !getPedido().getCliente().getClave().equals("U050008") ){
			buscarPedidosPendientesCte();
		}
		
	}
	
	public void buscarPedidosPendientesCte(){
		
		if (getPedido().getCliente().getId()!=null){
			Long clienteId= getPedido().getCliente().getId();
			String sql ="SELECT P.FOLIO,P.FECHA FROM SX_PEDIDOS P LEFT JOIN SX_VENTAS V ON (P.PEDIDO_ID=V.PEDIDO_ID) "+
		     " WHERE P.FECHA BETWEEN  DATE_ADD(NOW(),INTERVAL -15 DAY) AND NOW() AND V.CARGO_ID IS NULL AND P.CLIENTE_ID=?";
			Object[] args={clienteId}; 
			List<Map> rows=Services.getInstance().getJdbcTemplate().queryForList(sql, args);	
			if (!rows.isEmpty()){
				Pedido ped=SelectorDePedidosPendientes.seleccionar(getPedido().getCliente());
				if(ped!=null){
					PedidoFormView.showPedido(ped.getId());
				}

			}
			
		}
	
	}
	
	

	public void modificarSocio(){
		if(getPedido().getCliente().getClave().equals("U050008")){
			if(!getPedido().isFacturado()){
				Asociado socio=SelectorDeAsociados.seleccionar();
				getPedido().setSocio(socio);
			}
		}
	}
	
	public void modificarCheckplus(){
		if(getPedido().getFormaDePago().equals(FormaDePago.CHECKPLUS)){
			if(!getPedido().isFacturado()){
				CheckPlusOpcion opcion=SelectorDeCheckplus.seleccionar();
				getPedido().setCheckplusOpcion(opcion);
				if(opcion!=null){
					actualizarImportes();
				}else{
					getPedido().setFormaDePago(FormaDePago.EFECTIVO);
				}
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
				if(c.isChequeplus())
					getPedido().setFormaDePago(FormaDePago.CHECKPLUS);
			}else
				getPedido().setFormaDePago(FormaDePago.EFECTIVO);
		}
		actualizarImportes();
	}
	
	/**
	 * Posteriro a la definicion de la fomra de pago
	 * 
	 */
	public void formaDePagoActualizada(){
		//System.out.println("Forma de pago actualizada");
		if(getPedido().getFormaDePago().equals(FormaDePago.CHECKPLUS)){
			CheckPlusOpcion opcion=SelectorDeCheckplus.seleccionar();
			if(opcion!=null){
				getPedido().setCheckplusOpcion(opcion);
			}
		}
		getManager().actualizarFormaDePago(getPedido());
		/*if(getPedido().getFormaDePago().name().startsWith("TARJETA")){
			getPedido().setComisionTarjeta(2);
			getPedido().setDescripcionFormaDePago(getPedido().getFormaDePago().name());			
		}else{
			getPedido().setComisionTarjeta(0);
			getPedido().setDescripcionFormaDePago(null);
		}*/		
		//actualizarImportes();	
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
					if(getPedido().getCliente()!=null)
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
	public void actualizarImportes(){
		if(isModificable() ){
			//System.out.println("Actualizando importes...");
			
			getManager().asignarDescuento(getPedido());
			
			getPedido().actualizarImportes();
			
			actualizarCortes(getPedido());
			
			actualizarManiobras(getPedido());			
			getPedido().actualizarImportes();
			ordenar(getPedido());
			//Refresh the event list to update UI
			for(int index=0;index<partidasSource.size();index++){
				PedidoDet det=partidasSource.get(index);
				partidasSource.set(index, det);
			}
		}
	}
	
	public void autorizarSinExistencia(String userName){
		getPedido().setComentario2(userName);
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
			
			
			Date creado=new Date();
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
				MessageUtils.showMessage("El pedido requiere de autorizacin, estar en espera de la misma para poder" +
						" facturar", "Autorizacin pendiente");
			}
			logger.info("Pedido  persistido: "+target);
			return target;
		}
		return null;
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
				//actualizarCortes(getPedido());
				break;
			case ListEvent.DELETE:
				//partidasChanged();
				break;			
			default:
				break;
			}				
		}
	}	
	
	

	public EventList<PedidoDet> getPartidasSource() {
		return partidasSource;
	}
	
	
	protected EventList<ProductoRow> getProductos(){
		if(productos==null|| productos.isEmpty()){
			productos=new BasicEventList<ProductoRow>();
			productos.addAll(Services.getInstance().getProductosManager().getActivosAsRows());		
			//productos=PedidoUtils.getProductos();
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
		formas.addAll(Arrays.asList(FormaDePago.getFormasValidas()));
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
				formaDePagoActualizada();
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
	
	private void actualizarCortes(Pedido pedido){
		
		//Verificar si se requiere partida de cortes
		BigDecimal impCortes=BigDecimal.valueOf(0);		
		for(PedidoDet det:pedido.getPartidas()){
			impCortes=impCortes.add(det.getImporteCorte());			
		}
		//Registramos si existe el corte existente 
		PedidoDet cortes=(PedidoDet)CollectionUtils.find(pedido.getPartidas(), new Predicate(){
			public boolean evaluate(Object object) {
				PedidoDet row=(PedidoDet)object;
				return row.getProducto().getClave().equals("CORTE");
			}
		});
		
		if(impCortes.abs().doubleValue()>0){			
			System.out.println("Corte requerido.....");
			if(cortes==null){
				
				cortes=PedidoDet.getPedidoDet(22);
				cortes.setProducto(Services.getInstance().getProductosManager().buscarPorClave("CORTE"));
				cortes.setCantidad(1.0d);
				cortes.setPrecio(impCortes);
				cortes.setDescuento(0);
				cortes.actualizar();
				cortes.actualizarImporteBruto();
				boolean res=pedido.agregarPartida(cortes);
				if(res)
					partidasSource.add(cortes);
			}else{
				cortes.setPrecio(impCortes);
				cortes.setDescuento(0);
				cortes.actualizar();
				cortes.actualizarImporteBruto();
			}
		}else{			
			System.out.println("Corte NO requerido.....");
			if(cortes!=null){
				boolean ok=pedido.getPartidas().remove(cortes);
				if(ok){
					System.out.println("Corte NO requerido existente Por eliminar ");
					partidasSource.remove(cortes);
				}else{
					System.out.println("PELIGRO NO SE ELIMINO EL CORTE....");
				}
			}
		}
		
	}
	
	private void actualizarManiobras(Pedido pedido){
		
		
		BigDecimal importeManiobras=BigDecimal.ZERO;
		BigDecimal importeCortes=BigDecimal.ZERO;
		for(PedidoDet det:pedido.getPartidas()){
			if(det.getClave().equals("MANIOBRA") )
				continue;
			
			if(det.getProducto().getModoDeVenta().equals("N") && det.getProducto().getLinea().getId()!=129L ){
				importeManiobras=importeManiobras.add(det.getImporteBruto());
			}
		}
		
		double comision=0;
		FormaDePago fp=pedido.getFormaDePago();
		switch (fp) {
		case CHEQUE_POSTFECHADO:
			comision=.04;
			break;
		case TARJETA_CREDITO:
			comision=.02;
			break;
		case TARJETA_DEBITO:
			comision=.01;
			break;
		case CHECKPLUS:
			comision=getPedido().getCheckplusOpcion().getCargo().doubleValue()/100;
			break;
		default:
			break;
		}
		
		importeManiobras=importeManiobras.multiply(BigDecimal.valueOf(comision));
		importeManiobras=importeManiobras.add(pedido.getFlete());	
		//Registramos si existe el corte existente 
		PedidoDet maniobra=(PedidoDet)CollectionUtils.find(pedido.getPartidas(), new Predicate(){
			public boolean evaluate(Object object) {
				PedidoDet row=(PedidoDet)object;
				return row.getProducto().getClave().equals("MANIOBRA");
			}
		});
		
		if(importeManiobras.abs().doubleValue()>0){			
			System.out.println("Maniobra requerida.....");
			if(maniobra==null){
				//SystemUtils.sleep(1000);
				maniobra=PedidoDet.getPedidoDet(23);
				
				
				maniobra.setProducto(Services.getInstance().getProductosManager().buscarPorClave("MANIOBRA"));
				maniobra.setCantidad(1.0d);
				maniobra.setPrecio(importeManiobras);
				maniobra.setDescuento(0);
				maniobra.actualizar();
				maniobra.actualizarImporteBruto();
				boolean res=pedido.agregarPartida(maniobra);
				if(res)
					partidasSource.add(maniobra);
			}else{
				maniobra.setPrecio(importeManiobras);
				maniobra.setDescuento(0);
				maniobra.actualizar();
				maniobra.actualizarImporteBruto();
			}
			
		}else{
			//System.out.println("Maniobra  NO requerida.....");
			if(maniobra!=null){
				boolean ok=pedido.getPartidas().remove(maniobra);
				if(ok){
					partidasSource.remove(maniobra);
				}else
					System.out.println("PELIGRO MANIOBRA NO REQUERIDA NO SE ELIMINO DEL BEAN...");
			}
		}
		
		pedido.setComisionTarjetaImporte(importeManiobras.subtract(pedido.getFlete()));
		pedido.setComisionTarjeta(comision);
	}
	
	private void ordenar(Pedido pedido){
		/*
		System.out.println("Ordenando pedido.....");
		PedidoDet cortes=null;
		for(int index=0;index<partidasSource.size();index++){
			PedidoDet det=partidasSource.get(index);
			if(det.getClave().equals("CORTE")){
				cortes=det;
			}
		}
		if(cortes!=null && partidasSource.size()>0){
			PedidoDet ultimo=partidasSource.get(partidasSource.size()-1);
			if(ultimo!=cortes){
				Date time=new Date(ultimo.getLog().getCreado().getTime()+10);
				cortes.getLog().setCreado(time);
			}
		}*/
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
	
	private void warning(Cliente c){
		Iterator it=c.getComentarios().entrySet().iterator();
		while(it.hasNext()){
			if(c.getComentarios().get("AVISO")!=null)
				MessageUtils.showMessage("Atencin: "+c.getComentarios().get("AVISO"), "Atencion");
			//JOptionPane.showMessageDialog(null,"Atencin: "+c.getComentarios().get("SUSP_AUT") ); 
			return;
		}
	}
	

}
