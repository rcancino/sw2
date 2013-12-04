package com.luxsoft.siipap.pos.ui.consultas;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.util.Assert;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.Matchers;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Configuracion;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.pos.POSActions;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.pos.ui.consultas.caja.CajaController;
import com.luxsoft.siipap.pos.ui.forms.InstruccionDeEntregaForm;
import com.luxsoft.siipap.pos.ui.reports.ClientesNuevos;
import com.luxsoft.siipap.pos.ui.reports.EntregasPorChofer;
import com.luxsoft.siipap.pos.ui.reports.FacturasCanceladas;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeClientes;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeFacturasParaAsignacionDeEmbarques;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeFacturasParaCOD;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.pos.ui.venta.forms.PedidoController;
import com.luxsoft.siipap.pos.ui.venta.forms.PedidoFormView;
import com.luxsoft.siipap.pos.ui.venta.forms.PedidoForm_bak;
import com.luxsoft.siipap.security.SeleccionDeUsuario;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.dialog.SelectorDeFecha;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.ventas.model.AutorizacionParaFacturarSinExistencia;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.siipap.ventas.model2.VentaContraEntrega;
import com.luxsoft.sw3.inventarios.ExistenciasAgotadasException;
import com.luxsoft.sw3.services.PedidosManager;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ui.services.AutorizacionesFactory;
import com.luxsoft.sw3.ui.services.KernellUtils;
import com.luxsoft.sw3.ventas.AutorizacionDePedido;
import com.luxsoft.sw3.ventas.InstruccionDeEntrega;
import com.luxsoft.sw3.ventas.Pedido;

/**
 * Panel para la consulta y administracion de Pedidos
 * 
 * @author Ruben Cancino Ramos
 *
 */
//@Component
public class PedidosPanel extends FilteredBrowserPanel<Pedido> implements PropertyChangeListener{

	
	
	@Autowired
	private CajaController controller;
	

	public PedidosPanel() {
		super(Pedido.class);
	}
	
	
	
	@Override
	protected EventList getFilteredList(EventList list) {
		// TODO Auto-generated method stub
		return super.getFilteredList(list);
	}

	@Override
	public EventList getFilteredSource() {
		// TODO Auto-generated method stub
		return super.getFilteredSource();
	}



	private HeaderPanel header;
	
	
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

	protected void init(){		
		updateHeader();
		addProperty(
				"folio"
				,"origen"
				,"entrega"
				,"estado"				
				,"fecha"
				,"clave"
				,"nombre"				
				,"moneda"
				,"total"
				,"puesto"
				,"operador"
				,"log.updateUser"
				,"formaDePago"
				,"contraEntrega"
				
				,"comentario"
				,"pendienteDesc"
				,"comentarioAutorizacion"
				);
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
				,"Comentario (Aut)"
				);
		installTextComponentMatcherEditor("folio", "folio");
		installTextComponentMatcherEditor("Cliente", "clave","nombre");		
		installTextComponentMatcherEditor("Total", "total");
		//disponibleEditor.getBox().setSelected(true);
		//manejarPeriodo();
	}

	@Override
	protected void installEditors(EventList editors) {
		super.installEditors(editors);
		MatcherEditor editor=GlazedLists.fixedMatcherEditor(Matchers.beanPropertyMatcher(Pedido.class, "facturable", Boolean.FALSE));
		MatcherEditor editor2=GlazedLists.fixedMatcherEditor(Matchers.beanPropertyMatcher(Pedido.class, "especial", Boolean.FALSE));
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
			//procesos.add(addContextAction(new EsModificable(),"", "cancelarPedidos", "Cancelar Pedidos"));
			procesos.add(addContextAction(new ParaFacturar(),POSActions.GeneracionDePedidos.getId(),"exportarPedido", "Exportar"));
			
		}
		
		Action autorizarPCE=addAction(POSActions.GeneracionDePedidos.getId(), "autorizarPagoContraEntrega", "Autorizar PCE");
		Action cancelarPCE=addAction(POSActions.GeneracionDePedidos.getId(), "cancelarPagoContraEntrega", "Cancelar PCE");
		
		procesos.add(autorizarPCE);
		procesos.add(cancelarPCE);
		procesos.add(addAction(POSActions.GeneracionDePedidos.getId(),"reportePedidosPendientes", "Rep Pendientes"));
		procesos.add(addAction(null,"canclearPedidos", "Cancelación de pedidos"));
		procesos.add(addAction(null, "camcioDecliente", "Cambio de Cliente"));
		procesos.add(addAction(null, "asignarInstruccionDeEntrega", "Registrar para COD"));
		
		return procesos;
	}
	
	

	@Override
	public Action[] getActions() {
		Action imprimirAction=addContextAction(new SelectionPredicate(), POSActions.GeneracionDePedidos.getId(), "imprimirPedidos", "Imprimir Pedido");
		//imprimirAction.putValue(Action.LONG_DESCRIPTION, "Imprimir");
		imprimirAction.putValue(Action.SMALL_ICON, getIconFromResource("images/file/printview_tsk.gif"));
		
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getViewAction()
				,getInsertAction()
				,getEditAction()
				,getDeleteAction()
				,imprimirAction
				,addContextAction(new ParaFacturar(),POSActions.GeneracionDePedidos.getId(),"mandarFacturar", "Mandar a Facturar")				
				,addAction(POSActions.GeneracionDePedidos.getId(),"registrarDeposito", "Registrar depósito")
				,addAction(POSActions.GeneracionDePedidos.getId(),"consultarDepositos", "Consultar depósitos")
				,addAction("", "reporteClientesNuevos", "Clientes nuevos")
				,addAction("", "reporteFacturasCanceladas", "Facturas canceladas")
				,addAction("", "reporteEntregasPorChofer", "Entregas por chofer")
				,addAction("", "refacturar", "Refacturación autorizada")
				
				};
		return actions;
	}
	
	protected void executeLoadWorker(final SwingWorker worker){
		TaskUtils.executeSwingWorker(worker);
	}

	@Override
	protected List<Pedido> findData() {
		String hql="from Pedido p " +
				" where p.sucursal.id=? " +
				"  and p.fecha>= ?" +
				"  and p.totalFacturado=0 " +
				"  and p.facturable=false";
		Date fecha=DateUtils.addDays(new Date(), -10);
		Object[] values=new Object[]{Configuracion.getSucursalLocalId(),fecha};
		return Services.getInstance().getHibernateTemplate().find(hql,values);
		//return getManager().buscarPendientes(Services.getInstance().getConfiguracion().getSucursal());
		//return getManager().buscarPendientes(sucursal)
	}
	
	public void insert(){
		final PedidoController controller=new PedidoController();
		final PedidoForm_bak form=new PedidoForm_bak(controller);
		form.open();
		if(!form.hasBeenCanceled()){
			Pedido pedido=controller.persist();
			MessageUtils.showMessage("Su pedido: "+pedido.getFolio(), "Ventas");
			source.add(pedido);
			grid.packAll();
			if(!pedido.isDeCredito()){
				if(MessageUtils.showConfirmationMessage("Mandar facturar?", "Pedidos")){
					mandarFacturar(pedido);
				}
			}
			
		}
	}
	
	public void delete(){
		
		Pedido pedido=(Pedido)getSelectedObject();
		if(pedido==null)
			return;
		if(!pedido.isFacturado()){			
			if(MessageUtils.showConfirmationMessage("Eliminar pedidio ?"
					, "Eliminar pedidos")){
				getManager().remove(pedido.getId());
				source.remove(pedido);
			}
		}
	}
	
	public void edit(){
		Pedido source=(Pedido)getSelectedObject();
		if(source!=null && getManager().isModificable(source)){
			Pedido target=getManager().get(source.getId());
			target.getCliente().getTelefonos().size();
			final PedidoController controller=new PedidoController(target);
			final PedidoForm_bak form=new PedidoForm_bak(controller);
			form.open();
			if(!form.hasBeenCanceled()){
				target=controller.persist();
				if(target==null)
					return;
				int index=this.source.indexOf(source);
				if(index!=-1){
					this.source.set(index, target);
					grid.packAll();
					if(!target.isDeCredito()){
						if(MessageUtils.showConfirmationMessage("Mandar facturar?", "Pedidos")){
							mandarFacturar(target);
						}
					}
				}
				
				
			}
		}
		
	}
	
	@Override
	protected void doSelect(Object bean) {
		Pedido pedido=(Pedido)bean;
		PedidoFormView.showPedido(pedido.getId());
	}
	
	public void imprimirPedidos(){
		if(getSelectedObject()!=null){
			Pedido pedido=(Pedido)getSelectedObject();
			ReportUtils2.imprimirPedido(pedido);
		}
	}
	
	public void reportePedidosPendientes(){
		
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
	
	public boolean validarExistenciaOld(final Pedido pedido){
		if(1==1) //DEBUG Pendiente de activar por los traslados
			return true;
		AutorizacionParaFacturarSinExistencia aut=null;
		try {
			Services.getInstance().getFacturasManager().validarExistencias(pedido);
			return true;
		} catch (ExistenciasAgotadasException e) {
			MessageUtils.showMessage(
					"Existencias insuficientes para atender el pedido \n "+e.getDescripcion()
					+" \n para facturar se requiere una autorización especial"
					, "Validando existencias");
			if(!MessageUtils.showConfirmationMessage("Desea autorizar la factura?", "Facturación sin existencias")){
				return false;
			}			
			aut=AutorizacionesFactory.getAutorizacionParaFacturacionSinExistencias();
			if(aut!=null){
				pedido.setAutorizacionSinExistencia(aut);
				return true;
			}
			else
				return false;
			
		}
	}
	
	public void mandarFacturar(){
		if(getSelectedObject()!=null){
			Pedido pedido=(Pedido)getSelectedObject();
			mandarFacturar(pedido);
		}
		//List<Pedido> selected=new ArrayList<Pedido>(getSelected());
		/*for(Pedido p:selected){
			if(p.isFacturable())
				continue;
			if(p.isPorAutorizar() && (p.getAutorizacion()==null))
				continue;
			int index=source.indexOf(p);
			boolean existenciaValida=validarExistencia(p);
			if(!existenciaValida)
				return;
			if(index!=-1){
				p=getManager().get(p.getId());
				if(p.getInstruccionDeEntrega()!=null){
					if(autorizarPagoContraEntrega(p)){
						p.setFacturable(true);
						p=getManager().save(p);
						source.set(index, p);
					}
				}else{
					p.setFacturable(true);
					p=getManager().save(p);
					source.set(index, p);
				}
			}
		}*/
	}
	
	public void mandarFacturar(Pedido p){
		User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
		p=getManager().get(p.getId());
		p.getLog().setUpdateUser(user.getUsername());
		if(p.isFacturable())
			return;
		if(!validarParaFacturacion(p)){
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
		
		//boolean existenciaValida=validarExistencia(p);
		//Assert.isTrue(existenciaValida,"No hay existencia para facturar");
		
		int index=source.indexOf(p);
		if(index!=-1){
			//p=getManager().get(p.getId());
			if(p.isDeCredito()){
				p.setFacturable(true);
				registrarSurtidor(p);
				//p=getManager().save(p);
				p=(Pedido)Services.getInstance().getUniversalDao().save(p);
				source.set(index, p);
				return;
			}
			else if(p.getInstruccionDeEntrega()!=null){				
				boolean pce=sugerirPagoContraEntrega(p);
				if(pce){
					p.setFacturable(true);
					//p=getManager().save(p);
					registrarSurtidor(p);
					p=(Pedido)Services.getInstance().getUniversalDao().save(p);
					source.set(index, p);
				}
				
			}else{
				registrarSurtidor(p);
				p.setFacturable(true);
				p=getManager().save(p);
				source.set(index, p);
			}
		}
	}
	
	/**
	 * Valida las condiciones primarias para poder mandar facturar
	 * 
	 * @param pedido
	 * @return
	 */
	private boolean validarParaFacturacion(final Pedido pedido){
		
		// A - Cheques devueltos y / o Jurídico
		Cliente c=pedido.getCliente();
		if(c.isSuspendido()){
			if(c.isJuridico()){
				MessageUtils.showMessage("El cliente: "+c.getNombre()
						+ " se encuentra en trámite jurídico por lo que no se le puede facturar.\n Pedir autorización al departamento de crédito"
						, "Autorizaciones");
				return false;
			}
			if(c.getChequesDevueltos().doubleValue()>0){
				MessageUtils.showMessage("El cliente: "+c.getNombre()
						+"\n Tiene cheque(s) devueltos por un monto de: "+c.getChequesDevueltos()+" NO SE LE PUEDE FACTURAR." 
						+"\n Pedir autorización al departamento de crédito" 
						, "Autorizaciones");
				return false;
			}
			MessageUtils.showMessage("El cliente: "+c.getNombre()
					+"\n Esta suspendido NO SE LE PUEDE FACTURAR." 
					+"\n Pedir autorización al departamento de crédito" 
					, "Autorizaciones");
			return false;
		}else
			return true;
		
	}
	
	@Deprecated
	public void registrarSurtidor(final Pedido pedido){
		String res=JOptionPane.showInputDialog("Surtidor: ");
		if(StringUtils.isNotBlank(res)){
			//pedido.set
			pedido.setSurtidor(res);
		}
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
					MessageUtils.showMessage("Pedidos eliminados: "+res, "Eliminación de pedidos");
					load();
				} catch (Exception e) {					
					logger.error(e);
				}
			}
			
		};
		TaskUtils.executeSwingWorker(worker, "Eliminación de pedidos no atendidos"
					, "Eliminando los pedidos no atendidos antes del: "
				+new SimpleDateFormat("dd/MM/yyyy").format(fecha));
		
	}
	
	/**
	 * Cancela los pedidos con N dias de anterioridad
	 * 
	 
	public void cancelarPedidos(){
		Pedido pedido=(Pedido)getSelectedObject();
		if(!pedido.isFacturado()){
			boolean ok=getManager().isModificable(pedido);
			if(ok){
				if(MessageUtils.showConfirmationMessage("Cancelar pedido: "+pedido.getId()
						, "Pedido")){
					int index=source.indexOf(pedido);
					if(index!=-1){
						pedido=getManager().cancelarPedido(pedido);
						source.set(index, pedido);
					}
				}
			}
		}
	}
	*/
	
	public void autorizarPagoContraEntrega(){
		Pedido p=(Pedido)getSelectedObject();
		int index=source.indexOf(p);
		if(index!=-1){
			p=getManager().get(p.getId());
			if(sugerirPagoContraEntrega(p)){
				//p=getManager().save(p);
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
		Pedido pedido=(Pedido)getSelectedObject();
		int index=source.indexOf(pedido);
		if(index!=-1){
			pedido=getManager().get(pedido.getId());
			if(!pedido.isFacturado()){
				if(pedido.isContraEntrega()){
					//AutorizacionDePedido aut=pedido.getPagoContraEntrega();
					pedido.setPagoContraEntrega(null);
					pedido=getManager().save(pedido);
					MessageUtils.showMessage("Contra entrega cancelado Pedido: "+pedido.getFolio(), "Cancelación PCE");
					source.set(index, pedido);
				}
			}
		}
		
	}
	
	
	
	public void refreshSelection(){
		for(Object row:getSelected()){
			Pedido old=(Pedido)row;
			int index=source.indexOf(row);
			Pedido fresh=getManager().get(old.getId());
			if(index!=-1){
				source.set(index,fresh);
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
		source.add(pedido);
		int index=source.indexOf(pedido);
		selectionModel.clearSelection();
		//selectionModel.setSelectionInterval(index, index);
		doSelect(pedido);
	}
	
	public void camcioDecliente(){
		Pedido selected=(Pedido)getSelectedObject();
		if(selected!=null){
			if(!selected.isDeCredito() && (selected.getAutorizacion()==null) ){
				Cliente nuevoCliente=SelectorDeClientes.seleccionar();
				User user=SeleccionDeUsuario.findUser(Services.getInstance().getHibernateTemplate());
				if(nuevoCliente!=null && !nuevoCliente.equals(selected.getCliente())){
					int index=source.indexOf(selected);
					selected=getManager().get(selected.getId());
					selected.setCliente(nuevoCliente);
					selected.getLog().setModificado(new Date());
					selected.getLog().setUpdateUser(user.getUsername());
					selected=(Pedido)Services.getInstance().getHibernateTemplate().merge(selected);
					if(index!=-1){
						source.set(index, selected);
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
	
	
	
		
	

	private class TotalesPanel extends AbstractControl implements ListEventListener{
		
		private JLabel totalContado=new JLabel();
		private JLabel totalCredito=new JLabel();
		private JLabel total=new JLabel();
		

		public TotalesPanel() {
			super();
			sortedSource.addListEventListener(this);
		}

		@Override
		protected JComponent buildContent() {
			
			totalContado.setHorizontalAlignment(JLabel.RIGHT);
			totalCredito.setHorizontalAlignment(JLabel.RIGHT);
			
			final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			totalContado.setHorizontalAlignment(SwingConstants.RIGHT);
			
			builder.append("Contado",totalContado);
			builder.append("Credito",totalCredito);
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
		NumberFormat nf2=NumberFormat.getNumberInstance();
		
		public void updateTotales(){
			
			
		}
		
		protected BigDecimal sumarizar(String property){
			BigDecimal res=BigDecimal.ZERO;
			for(Object bean:sortedSource){
				res=res.add(getValor(bean, property));
			}
			return res;
		}
		
		private BigDecimal getValor(Object bean,String property){
			return BigDecimal.valueOf(getValorAsNumber(bean, property).doubleValue());
		}
		
		private Number getValorAsNumber(Object bean,String property){
			BeanWrapperImpl wrapper=new BeanWrapperImpl(bean);
			return (Number)wrapper.getPropertyValue(property);
		}
		
		private NumberFormat nf=NumberFormat.getPercentInstance();
		
		protected String part(final CantidadMonetaria total,final CantidadMonetaria part){
			
			double res=0;
			if(total.amount().doubleValue()>0){
				res=part.divide(total.amount()).amount().doubleValue();
			}
			return StringUtils.leftPad(nf.format(res),5);
		}
		
	}
	
	private class ParaFacturar implements Predicate{

		public boolean evaluate(Object bean) {
			Pedido p=(Pedido)bean;
			if(p==null) 
				return false;			
			//return true; //(p.getEstado().equalsIgnoreCase("PENDIENTE") && (!p.isPorAutorizar()));
			return !p.isFacturado();
		}
		
	}
	
	
	
	
}
