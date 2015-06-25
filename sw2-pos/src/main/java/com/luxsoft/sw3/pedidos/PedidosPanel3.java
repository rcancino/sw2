package com.luxsoft.sw3.pedidos;


import java.beans.PropertyChangeEvent;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.Matchers;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.cfdi.CFDIMandarFacturarForm;
import com.luxsoft.siipap.inventarios.model.SolicitudDeTraslado;
import com.luxsoft.siipap.inventarios.model.SolicitudDeTrasladoDet;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.pos.POSActions;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.pos.ui.consultas.RefacturadorController;
import com.luxsoft.siipap.pos.ui.consultas.caja.CajaController;
import com.luxsoft.siipap.pos.ui.forms.InstruccionDeEntregaForm;
import com.luxsoft.siipap.pos.ui.reports.ClientesNuevos;
import com.luxsoft.siipap.pos.ui.reports.EntregasPorChofer;
import com.luxsoft.siipap.pos.ui.reports.FacturasCanceladas;
import com.luxsoft.siipap.pos.ui.reports.ReporteDePedidosNoFacturados;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeClientes;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeFacturasParaCOD;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.pos.ui.venta.forms.PedidoController;
import com.luxsoft.siipap.pos.ui.venta.forms.PedidoFormView;
import com.luxsoft.siipap.pos.ui.venta.forms.PedidoForm_bak;
import com.luxsoft.siipap.security.SeleccionDeUsuario;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfdi.model.CFDIClienteMails;
import com.luxsoft.sw3.services.InventariosManager;
import com.luxsoft.sw3.services.PedidosManager;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.services.SolicitudDeTrasladosManager;
import com.luxsoft.sw3.ui.forms.PedidoDolaresController;
import com.luxsoft.sw3.ui.forms.PedidoDolaresForm;
import com.luxsoft.sw3.ui.forms.SolicitudDeTrasladoController;
import com.luxsoft.sw3.ui.forms.SolicitudDeTrasladoForm;
import com.luxsoft.sw3.ui.services.AutorizacionesFactory;
import com.luxsoft.sw3.ventas.AutorizacionDePedido;
import com.luxsoft.sw3.ventas.InstruccionDeEntrega;
import com.luxsoft.sw3.ventas.Pedido;
import com.luxsoft.sw3.ventas.Pedido.ClasificacionVale;
import com.luxsoft.sw3.ventas.PedidoDet;
import com.luxsoft.sw3.ventas.PedidoRow;

/**
 * Panel para el mantenimiento de traslados
 * 
 * @author Ruben Cancino
 *
 */
public class PedidosPanel3 extends AbstractMasterDatailFilteredBrowserPanel<PedidoRow, PedidoDet>{

	@Autowired
	private CajaController controller;
	
	private HeaderPanel header;
	
	
	
	public PedidosPanel3() {
		super(PedidoRow.class);
		
		
	}
	
	
	@Override
	protected JComponent buildHeader(){
		if(header==null){
			header=new HeaderPanel("Pedidos pendientes  ","");
			updateHeader();
		}
		return header;
	}
	
	private void updateHeader(){
		if(header!=null)
			header.setDescription("");
	}
	
	public void propertyChange(PropertyChangeEvent evt) {
		updateHeader();
	}
	
	@Override
	public void open() {
		super.open();
		PedidoUtils.initProductos();
	}

	
	protected void init(){		
		updateHeader();
		addProperty(
				"folio"
				,"tipo"
				,"entrega"
				,"estado"				
				,"fecha"
				,"clave"
				,"nombre"
				,"moneda"
				,"total"
				,"puesto"
				,"creado"
				,"modificado"
				,"formaDePago"
				,"contraEntrega"				
				,"comentario"
				,"pendiente"
				,"comentarioAutorizacion");
		addLabels(
				"Folio"
				,"Venta"
				,"Entrega"
				,"Est"				
				,"Fecha"
				,"Cliente"
				,"Nombre"
				,"Mon"
				,"Total"
				,"Puesto"
				,"Creado"
				,"Modificado"
				,"F.P."	
				,"PCE"
				,"Comentario"
				,"Comentario(Pend)"
				,"Comentario(Aut)"
				);
		installTextComponentMatcherEditor("folio", "folio");
		installTextComponentMatcherEditor("Cliente", "clave","nombre");		
		installTextComponentMatcherEditor("Total", "total");
		manejarPeriodo();
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"clave","descripcion","producto.gramos","producto.calibre","cantidad","instruccionesDecorte","precio","importeBruto","descuento","importeDescuento","cortes","importeCorte","subTotal"};
		String[] labels={"Clave","Producto","(g)","cal","Cant","Corte","Precio","Imp Bruto","Des (%)","Des ($)","Cortes (#)","Cortes ($)","Sub Total"};
		return GlazedLists.tableFormat(PedidoDet.class, props,labels);
	}

	@Override
	protected Model<PedidoRow,PedidoDet> createPartidasModel() {
		return new CollectionList.Model<PedidoRow, PedidoDet>(){
			public List<PedidoDet> getChildren(PedidoRow parent) {
				String hql="from PedidoDet det where det.pedido.id=?";
				return Services.getInstance().getHibernateTemplate().find(hql, parent.getId());
			}
		};
	}

	@Override
	protected void installEditors(EventList editors) {
		super.installEditors(editors);
		MatcherEditor editor=GlazedLists.fixedMatcherEditor(Matchers.beanPropertyMatcher(PedidoRow.class, "facturable", Boolean.FALSE));
		MatcherEditor editor2=GlazedLists.fixedMatcherEditor(Matchers.beanPropertyMatcher(PedidoRow.class, "especial", Boolean.FALSE));
		editors.add(editor);
		editors.add(editor2);
	}

	
	protected void manejarPeriodo(){
		periodo=Periodo.getPeriodo(-5);
	}
	
	public void cambiarPeriodo(){
		if(KernellSecurity.instance().hasRole(POSRoles.GERENTE_DE_VENTAS.name(),true)){
			MessageUtils.showMessage("Recuerde usar periodos cortos por el numero de retistros que puede leer es limitado", "Pedidos");
			super.cambiarPeriodo();
		}else{
			MessageUtils.showMessage("No tiene derechos para cambiar el periodo de consulta", "Pedidos");
		}
		
	}
	
	@Override
	protected List<Action> createProccessActions() {
		List<Action> procesos=super.createProccessActions();
		if(KernellSecurity.instance().hasRole(POSRoles.ADMINISTRADOR_DE_VENTAS.name())){
			
			//procesos.add(addContextAction(new ParaFacturar(),POSActions.GeneracionDePedidos.getId(),"exportarPedido", "Exportar"));
			
		}
		
		Action autorizarPCE=addAction(POSActions.GeneracionDePedidos.getId(), "autorizarPagoContraEntrega", "Autorizar PCE");
		Action cancelarPCE=addAction(POSActions.GeneracionDePedidos.getId(), "cancelarPagoContraEntrega", "Cancelar PCE");
		
		procesos.add(autorizarPCE);
		procesos.add(cancelarPCE);
		procesos.add(addAction(POSActions.GeneracionDePedidos.getId(),"reportePedidosPendientes", "Rep Pendientes"));
		procesos.add(addAction(null,"canclearPedidos", "Cancelacin de pedidos"));
		procesos.add(addAction(null, "camcioDecliente", "Cambio de Cliente"));
		procesos.add(addAction(null, "asignarInstruccionDeEntrega", "Registrar para COD"));
		
		return procesos;
	}
	
	
	@Override
	public Action[] getActions() {
		Action imprimirAction=addContextAction(new SelectionPredicate(), POSActions.GeneracionDePedidos.getId(), "imprimirPedidos", "Imprimir Pedido");
		imprimirAction.putValue(Action.SMALL_ICON, getIconFromResource("images/file/printview_tsk.gif"));
		actions=new Action[]{
				getLoadAction()
				,getViewAction()
				,getInsertAction()
				,getEditAction()
				,getDeleteAction()
				,imprimirAction
				,addAction("","pedidoEnDolares","Pedido en USD")
				,addAction(POSActions.GeneracionDePedidos.getId(),"mandarFacturar", "Mandar a Facturar")				
				,addAction(POSActions.GeneracionDePedidos.getId(),"registrarDeposito", "Registrar depsito")
				,addAction(POSActions.GeneracionDePedidos.getId(),"consultarDepositos", "Consultar depsitos")
				,addAction("", "reporteClientesNuevos", "Clientes nuevos")
				,addAction("", "reporteFacturasCanceladas", "Facturas canceladas")
				,addAction("", "reporteEntregasPorChofer", "Entregas por chofer")
				,addAction("", "refacturar", "Refacturacin autorizada")
				,addAction("", "reportePedidosNofacturados", "Pedidos No Facturados")
				
				};
		return actions;
	}
	
	
	protected void executeLoadWorker(final SwingWorker worker){
		TaskUtils.executeSwingWorker(worker);
	}
	
	@Override
	protected List<PedidoRow> findData() {
		return PedidoUtils.find(periodo);
	}
	
	public void insert(){
		final PedidoController controller=new PedidoController();
		final PedidoForm_bak form=new PedidoForm_bak(controller);
		form.open();
		if(!form.hasBeenCanceled()){
			Pedido pedido=controller.persist();
			pedido=getManager().get(pedido.getId());
			MessageUtils.showMessage("Su pedido: "+pedido.getFolio(), "Ventas");
			PedidoRow row=new PedidoRow(pedido);
			source.add(row);
			int index=source.indexOf(row);
			grid.packAll();
			if(!pedido.isDeCredito() && index!=-1){
				if(MessageUtils.showConfirmationMessage("Mandar facturar?", "Pedidos")){
					mandarFacturar(pedido,index);
				}
			}
			
		}
	}
	
	public void pedidoEnDolares(){
		User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
		if((user!=null) && user.hasRole("FACTURACION_DOLARES")){
			final PedidoDolaresController controller=new PedidoDolaresController();
			final PedidoDolaresForm form=new PedidoDolaresForm(controller);
			form.open();
			if(!form.hasBeenCanceled()){
				Pedido pedido=controller.persist();
				pedido=getManager().get(pedido.getId());
				MessageUtils.showMessage("Su pedido: "+pedido.getFolio(), "Ventas");
				PedidoRow row=new PedidoRow(pedido);
				source.add(row);
				int index=source.indexOf(row);
				grid.packAll();
				if(!pedido.isDeCredito() && index!=-1){
					if(MessageUtils.showConfirmationMessage("Mandar facturar?", "Pedidos")){
						mandarFacturar(pedido,index);
					}
				}
				
			}
		}else{
			MessageUtils.showMessage("No tiene acceso a generar pedidos en dolares", "Ventas");
		}
		
	}
	
	private Pedido getSelectedPedido(){
		PedidoRow row=(PedidoRow)getSelectedObject();
		if(row!=null){
			return getManager().get(row.getId());
		}
		return null;
	}
	
	
	public void delete(){
		Object row=getSelectedObject();
		if(row==null)
			return;
		Pedido pedido=getSelectedPedido();
		if(!pedido.isFacturado()){			
			if(MessageUtils.showConfirmationMessage("Eliminar pedidio ?"
					, "Eliminar pedidos")){
				getManager().remove(pedido.getId());
				source.remove(row);
			}
		}
	}
	
	public void edit(){
		
		Object row=getSelectedObject();
		Pedido target=getSelectedPedido();
		if(target!=null && getManager().isModificable(target) && target.getMoneda().equals(MonedasUtils.PESOS)){
			//Pedido target=getManager().get(source.getId());
			target.getCliente().getTelefonos().size();
			final PedidoController controller=new PedidoController(target);
			final PedidoForm_bak form=new PedidoForm_bak(controller);
			form.open();
			if(!form.hasBeenCanceled()){
				target=controller.persist();
				if(target==null)
					return;
				int index=this.source.indexOf(row);
				if(index!=-1){
					this.source.set(index, new PedidoRow(target));
					grid.packAll();
					if(!target.isDeCredito()){
						if(MessageUtils.showConfirmationMessage("Mandar facturar?", "Pedidos")){
							mandarFacturar(target,index);
						}
					}
				}
				
				
			}
		}
		
	}
	
	@Override
	protected void doSelect(Object bean) {
		PedidoRow pedido=(PedidoRow)bean;
		PedidoFormView.showPedido(pedido.getId());
	}
	
	public void imprimirPedidos(){
		if(getSelectedObject()!=null){
			Pedido pedido=getSelectedPedido();
			ReportUtils2.imprimirPedido(pedido);
		}
	}
	
	public void reporteClientesNuevos(){
		ClientesNuevos.run();
	}
	public void reporteFacturasCanceladas(){
		FacturasCanceladas.run();
	}
	
	public void reporteEntregasPorChofer(){
		EntregasPorChofer.run();
		
	}
	
	public void reportePedidosNofacturados(){
		ReporteDePedidosNoFacturados.run();
	}
	
	public void mandarFacturar(){
		PedidoRow row=(PedidoRow)getSelectedObject();
		if(row!=null){
			Pedido pedido=getSelectedPedido();
			int index=source.indexOf(row);
			mandarFacturar(pedido,index);
		}
	}

	public void mandarFacturar(Pedido p,final int index){
		
		//User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
		//if(user==null)
			//return;
		User user;
		if(p.getClave().equals("U050008")){
			user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
		}else{
			CFDIClienteMails mails=CFDIMandarFacturarForm.showForm(p.getClave());
			user=mails.getUsuario();
		}
		if(user==null)
			return;
		p.getLog().setUpdateUser(user.getUsername());
		
		if(!PedidoUtils.validarParaFacturacion(p)){
			return ;
		}
		if(p.isPorAutorizar() ){
			if(p.getAutorizacion()==null){
				if(p.isPorAutorizar()  ){
					AutorizacionDePedido aut=AutorizacionesFactory.getAutorizacionDePedido();
					if(aut!=null){
						//p=getManager().get(p.getId());
						p.setAutorizacion(aut);
					}else
						return;
				}
			}
		}		
		if(index!=-1){
			//p=getManager().get(p.getId());
			if(p.isDeCredito()){
				p.setFacturable(true);
				registrarSurtidor(p);				
				p=(Pedido)Services.getInstance().getUniversalDao().save(p);
				source.set(index, new PedidoRow(p));
				
				if(!p.getClasificacionVale().equals(ClasificacionVale.SIN_VALE) && !p.getClasificacionVale().equals(ClasificacionVale.EXISTENCIA_VENTA) && !p.isVale() ){
					 SolicitudDeTraslado sol=generarValePedido(p);
					 generarTraslado(sol);
					 
					p.setVale(true);
					p=(Pedido)Services.getInstance().getUniversalDao().save(p);
				}
				if(p.getClasificacionVale().equals(ClasificacionVale.EXISTENCIA_VENTA) && !p.isVale() ){
					boolean actPedido=generarValeExisVta(p);
					p.setVale(actPedido);
					p=(Pedido)Services.getInstance().getUniversalDao().save(p);
				}
				
				return;
			}
			else if(p.getInstruccionDeEntrega()!=null){				
				boolean pce=sugerirPagoContraEntrega(p);
				if(pce){
					p.setFacturable(true);
					//p=getManager().save(p);
					registrarSurtidor(p);
					p=(Pedido)Services.getInstance().getUniversalDao().save(p);
					source.set(index, new PedidoRow(p));
				}
				
			}else{
				registrarSurtidor(p);
				p.setFacturable(true);
				p=getManager().save(p);
				source.set(index, new PedidoRow(p));
			}
		}
		
		if(!p.getClasificacionVale().equals(ClasificacionVale.SIN_VALE) && !p.getClasificacionVale().equals(ClasificacionVale.EXISTENCIA_VENTA) && !p.isVale() ){
			   SolicitudDeTraslado sol=generarValePedido(p);
			   generarTraslado(sol);
				p.setVale(true);
			
				p=(Pedido)Services.getInstance().getUniversalDao().save(p);
			}
			if(p.getClasificacionVale().equals(ClasificacionVale.EXISTENCIA_VENTA) && !p.isVale() ){
				boolean actPedido=generarValeExisVta(p);
				p.setVale(actPedido);
				p=(Pedido)Services.getInstance().getUniversalDao().save(p);
				
			}
			
	}
	
public void generarTraslado(SolicitudDeTraslado sol){
		
		getInventarioManager().generarSalidaPorTraslado(sol, new Date(), null, null, null, null, null);
	
	}
	
	
	
	 
	 
	public SolicitudDeTraslado generarValePedido(Pedido pedido){
		SolicitudDeTraslado sol= new SolicitudDeTraslado();
		 EventList<SolicitudDeTrasladoDet> partidasSource = new BasicEventList<SolicitudDeTrasladoDet>();
		
		SolicitudDeTrasladosManager trdManager =Services.getInstance().getSolicitudDeTrasladosManager();
		sol.setFecha(new Date());
		sol.setOrigen(pedido.getSucursalVale());
		sol.setPorInventario(false);
		sol.setSucursal(pedido.getSucursal());
		sol.setClasificacion(pedido.getClasificacionVale().toString());
		sol.setReferencia("Ped: "+pedido.getFolio()+" - "+pedido.getTipo().toString().substring(0,2));
		sol.setPedido(pedido.getId());
		
		for(PedidoDet det : pedido.getPartidas() ){
			
			if(det.isConVale()){
				//sol.agregarPartida(det.getProducto(),det.getCantidad());
				
				SolicitudDeTrasladoDet soldet= new SolicitudDeTrasladoDet();
				soldet.setProducto(det.getProducto());
				soldet.setCortes(det.getCortes());
				soldet.setInstruccionesDecorte(det.getInstruccionesDecorte());
				soldet.setOrigen(pedido.getSucursalVale().getId());
				soldet.setSucursal(det.getSucursal().getId());
				soldet.setSolicitado(det.getCantidad());
				soldet.setRecibido(det.getCantidad());
				soldet.setRenglon(partidasSource.size()+1);
				partidasSource.add(soldet);
			}
			
		}
		ListEventListener<SolicitudDeTrasladoDet> syncEventListToList = GlazedLists.syncEventListToList(partidasSource, sol.getPartidas());
		
		Date time=Services.getInstance().obtenerFechaDelSistema();
		String user=pedido.getLog().getUpdateUser();
			
		sol.getLog().setCreado(time);
		sol.getLog().setCreateUser(pedido.getLog().getUpdateUser());
		sol.getAddresLog().setCreatedIp(sol.getAddresLog().getUpdatedIp());
		sol.getAddresLog().setCreatedMac(KernellSecurity.getMacAdress());
		sol.getLog().setModificado(time);	
		sol.getLog().setUpdateUser(pedido.getLog().getUpdateUser());
		sol.getAddresLog().setUpdatedIp(KernellSecurity.getIPAdress());
		sol.getAddresLog().setUpdatedMac(KernellSecurity.getMacAdress());
	
		
		sol=trdManager.save(sol);
		
		//
		
		MessageUtils.showMessage("Solicitud generada: "+sol.getDocumento(), "Solicitud de traslado");
		
		return sol;
	}
	
	
	public boolean generarValeExisVta(Pedido pedido){
		SolicitudDeTraslado sol= new SolicitudDeTraslado();
		 EventList<SolicitudDeTrasladoDet> partidasSource = new BasicEventList<SolicitudDeTrasladoDet>();
		
		SolicitudDeTrasladosManager trdManager =Services.getInstance().getSolicitudDeTrasladosManager();
		sol.setFecha(new Date());
		sol.setOrigen(pedido.getSucursalVale());
		sol.setPorInventario(false);
		sol.setSucursal(pedido.getSucursal());
		sol.setClasificacion(pedido.getClasificacionVale().toString());
		sol.setReferencia("Ped: "+pedido.getFolio()+" - "+pedido.getTipo());
		sol.setPedido(pedido.getId());
		
		for(PedidoDet det : pedido.getPartidas() ){
			
			if(det.isConVale()){
				//sol.agregarPartida(det.getProducto(),det.getCantidad());
				
				SolicitudDeTrasladoDet soldet= new SolicitudDeTrasladoDet();
				soldet.setProducto(det.getProducto());
				soldet.setCortes(det.getCortes());
				soldet.setInstruccionesDecorte(det.getInstruccionesDecorte());
				soldet.setOrigen(pedido.getSucursalVale().getId());
				soldet.setSucursal(det.getSucursal().getId());
				soldet.setSolicitado(det.getCantidad());
				soldet.setRenglon(partidasSource.size()+1);
				partidasSource.add(soldet);
			}
			
		}
		
	
		SolicitudDeTrasladoController controller=new SolicitudDeTrasladoController(sol,partidasSource);
		SolicitudDeTrasladoForm form=new SolicitudDeTrasladoForm(controller);
		form.open();		
		if(!form.hasBeenCanceled()){
			SolicitudDeTraslado res= controller.persist();
			 return true;
		}else{
			return false;
		}
		
	}
	
	
	
	@Deprecated
	public void registrarSurtidor(final Pedido pedido){
		/*
		String res=JOptionPane.showInputDialog("Surtidor: ");
		if(StringUtils.isNotBlank(res)){
			//pedido.set
			pedido.setSurtidor(res);
		}*/
	}
	
	
	/**
	 * Exporta un pedido a otra sucursal por WEB-Services
	 * 
	 */
	public void exportarPedido(){
		
	}
	
	public void canclearPedidos(){
		final Date fecha=DateUtils.addDays(new Date(), -10);
		final SwingWorker worker=new SwingWorker(){
			protected Object doInBackground() throws Exception {
				return getManager().eliminarPedidos(fecha);				
			}
			protected void done() {
				try {
					Integer res=(Integer)get();
					MessageUtils.showMessage("Pedidos eliminados: "+res, "Eliminacin de pedidos");
					load();
				} catch (Exception e) {					
					logger.error(e);
				}
			}
			
		};
		TaskUtils.executeSwingWorker(worker, "Eliminacin de pedidos no atendidos"
					, "Eliminando los pedidos no atendidos antes del: "
				+new SimpleDateFormat("dd/MM/yyyy").format(fecha));
		
	}
	
	
	
	public void autorizarPagoContraEntrega(){
		PedidoRow row=(PedidoRow)getSelectedObject();
		int index=source.indexOf(row);
		if(index!=-1){
			Pedido p=getSelectedPedido();
			if(sugerirPagoContraEntrega(p)){
				p=(Pedido)Services.getInstance().getUniversalDao().save(p);
				source.set(index, p);
			}
		}
	}
	
	public boolean sugerirPagoContraEntrega(Pedido p){
		if(p.getPagoContraEntrega()!=null)
			return true;
		if(!getManager().calificaPagoContraEntrega(p)){
			MessageUtils.showConfirmationMessage("El pedido no califica para pago contra entrega", "Pedido");
			return false;
		}
		
		if(MessageUtils.showConfirmationMessage("Pago contra entrega?\n "+p.getId(), "Pedido")){
			AutorizacionDePedido aut=AutorizacionesFactory.getAutorizacionParaPagoContraEntrega(p);
			if(aut!=null){
				p.setPagoContraEntrega(aut);
				return true;
			}
		}
		return true;
			
	}
	
	public void cancelarPagoContraEntrega(){
		PedidoRow row=(PedidoRow)getSelectedObject();
		int index=source.indexOf(row);
		if(index!=-1){
			Pedido pedido=getSelectedPedido();
			if(!pedido.isFacturado()){
				if(pedido.isContraEntrega()){
					//AutorizacionDePedido aut=pedido.getPagoContraEntrega();
					pedido.setPagoContraEntrega(null);
					pedido=getManager().save(pedido);
					MessageUtils.showMessage("Contra entrega cancelado Pedido: "+pedido.getFolio(), "Cancelacin PCE");
					source.set(index, pedido);
				}
			}
		}
		
	}
	
	
	public void registrarDeposito(){
		controller.registrarDeposito();
	}
	
	public void consultarDepositos(){
		controller.buscarDeposito();
	}
	
	public void refacturar(){
		Pedido pedido=RefacturadorController.refacturar();
		System.out.println("Pedido para refacturar: "+pedido.getId());
		source.add(new PedidoRow(pedido));		
		selectionModel.clearSelection();
		doSelect(pedido);
	}
	
	public void camcioDecliente(){
		Object row=getSelectedObject();
		Pedido selected=getSelectedPedido();
		if(selected!=null){
			if(!selected.isDeCredito() || (selected.getAutorizacion()==null) || (selected.getPendiente()==null) ){
				Cliente nuevoCliente=SelectorDeClientes.seleccionar();
				User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
				if(nuevoCliente!=null && !nuevoCliente.equals(selected.getCliente())){
					int index=source.indexOf(row);
					//selected=getManager().get(selected.getId());
					selected.setCliente(nuevoCliente);
					selected.getLog().setModificado(new Date());
					selected.getLog().setUpdateUser(user.getUsername());
					selected=(Pedido)Services.getInstance().getHibernateTemplate().merge(selected);
					if(index!=-1){
						source.set(index, new PedidoRow(selected));
					}
				}
			}else{
				MessageUtils.showMessage("Solo se permite con pedidos de contado no autorizados", "Cambio de cliente");
			}
		}
	}
	
	public void asignarInstruccionDeEntrega(){
		Venta venta=SelectorDeFacturasParaCOD.seleccionar();
		if(venta!=null){
			InstruccionDeEntrega ie=InstruccionDeEntregaForm.crearNueva(venta.getCliente());
			if(ie!=null){
				venta.getPedido().setInstruccionDeEntrega(ie);
				Services.getInstance().getUniversalDao().save(venta.getPedido()); //Salvamos el pedido
				venta.setInstruccionDeEntrega(StringUtils.abbreviate(ie.oneLineString(),255)); // Salvamos la venta 
				Services.getInstance().getUniversalDao().save(venta);
				
				String msg=MessageFormat.format("Venta: {0} - {1}\n COD en: {2}"
						,venta.getOrigen(), venta.getDocumento(),ie.toString());
				
				MessageUtils.showMessage(msg, "Venta COD");
			}
		}
	}
	
	
private TotalesPanel totalPanel;
	
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			totalPanel=new TotalesPanel();
		}
		return (JPanel)totalPanel.getControl();
	}
		
	private PedidosManager getManager(){
		return Services.getInstance().getPedidosManager();
	}
	
	private InventariosManager getInventarioManager(){
		return Services.getInstance().getInventariosManager();
	}

	private class TotalesPanel extends AbstractControl implements ListEventListener{
		
		private JLabel totalContado=new JLabel();
		private JLabel totalCredito=new JLabel();
		private JLabel totalCamioneta=new JLabel();
		private JLabel total=new JLabel();
		

		public TotalesPanel() {
			super();
			sortedSource.addListEventListener(this);
		}

		@Override
		protected JComponent buildContent() {
			
			totalContado.setHorizontalAlignment(JLabel.RIGHT);
			totalCredito.setHorizontalAlignment(JLabel.RIGHT);
			totalCamioneta.setHorizontalAlignment(JLabel.RIGHT);
			total.setHorizontalAlignment(JLabel.RIGHT);
			
			final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			//totalContado.setHorizontalAlignment(SwingConstants.RIGHT);
			
			builder.append("Contado",totalContado);
			builder.append("Credito",totalCredito);
			builder.append("Camioneta",totalCamioneta);
			builder.append("Total",total);
			
			
			builder.getPanel().setOpaque(false);
			getFilteredSource().addListEventListener(this);
			updateTotales();
			return builder.getPanel();
		}
		
		public void listChanged(ListEvent listChanges) {
			if(listChanges.next()){
				
			}
			updateTotales();
		}
		
		NumberFormat nf1=NumberFormat.getCurrencyInstance();
		
		
		public void updateTotales(){
			
			BigDecimal credito=BigDecimal.ZERO;
			BigDecimal mostrador=BigDecimal.ZERO;
			BigDecimal camioneta=BigDecimal.ZERO;
			BigDecimal ventaTotal=BigDecimal.ZERO;
			
			for(Object o:sortedSource){
				PedidoRow row=(PedidoRow)o;
				ventaTotal=ventaTotal.add(row.getTotal());
				if(row.getTipo().equals("CREDITO")){
					credito=credito.add(row.getTotal());
					continue;
				}else if(row.isContraEntrega()){
					camioneta=camioneta.add(row.getTotal());
					continue;
				}else if(row.getTipo().equals("CONTADO")){
					mostrador=mostrador.add(row.getTotal());
					continue;
				}
			}
			totalContado.setText(nf1.format(mostrador.doubleValue()));
			totalCredito.setText(nf1.format(credito.doubleValue()));
			totalCamioneta.setText(nf1.format(camioneta.doubleValue()));
			total.setText(nf1.format(ventaTotal.doubleValue()));
		}
		
		
	}
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	
	
	


	
	
	

	


}
