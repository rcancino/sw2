package com.luxsoft.siipap.pos.ui.consultas;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
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
import org.springframework.beans.BeanWrapperImpl;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.Matchers;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.pos.POSActions;
import com.luxsoft.siipap.pos.ui.forms.PedidoEspecialForm;
import com.luxsoft.siipap.pos.ui.forms.PedidoEspecialFormController;
import com.luxsoft.siipap.pos.ui.forms.PedidoEspecialFormView;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
//import com.luxsoft.siipap.pos.ui.venta.forms.PedidoFormView;
import com.luxsoft.siipap.security.SeleccionDeUsuario;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.dialog.SelectorDeFecha;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.sw3.services.PedidosManager;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ui.services.AutorizacionesFactory;
import com.luxsoft.sw3.ventas.AutorizacionDePedido;
import com.luxsoft.sw3.ventas.Pedido;

/**
 * Panel para la consulta y administracion de Pedidos Especiales
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PedidosEspecialesPanel extends FilteredBrowserPanel<Pedido> implements PropertyChangeListener{

	
	public PedidosEspecialesPanel() {
		super(Pedido.class);
	}


	private HeaderPanel header;
	
	
	@Override
	protected JComponent buildHeader(){
		if(header==null){
			header=new HeaderPanel("Pedidos especiales  ","");
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
		
	}

	@Override
	protected void installEditors(EventList editors) {
		super.installEditors(editors);
		MatcherEditor editor=GlazedLists.fixedMatcherEditor(Matchers.beanPropertyMatcher(Pedido.class, "especial", Boolean.TRUE));
		editors.add(editor);
	}

	@Override
	protected List<Action> createProccessActions() {
		List<Action> procesos=super.createProccessActions();
				
		procesos.add(addAction(POSActions.GeneracionDePedidos.getId(), "autorizarPagoContraEntrega", "Autorizar PCE"));
		procesos.add(addAction(POSActions.GeneracionDePedidos.getId(), "cancelarPagoContraEntrega", "Cancelar PCE"));
		procesos.add(addAction(POSActions.GeneracionDePedidos.getId(),"reportePedidosPendientes", "Rep Pendientes"));
		return procesos;
	}
	
	

	@Override
	public Action[] getActions() {
		Action imprimirAction=addContextAction(new SelectionPredicate(), POSActions.GeneracionDePedidos.getId(), "imprimirPedidos", "Imprimir Pedido");
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
				
				
				};
		return actions;
	}
	
	protected void executeLoadWorker(final SwingWorker worker){
		TaskUtils.executeSwingWorker(worker);
	}

	@Override
	protected List<Pedido> findData() {		
		return getManager().buscarPendientes(Services.getInstance().getConfiguracion().getSucursal());
		//return getManager().buscarPendientes(sucursal)
	}
	
	public void insert(){
		final PedidoEspecialFormController controller=new PedidoEspecialFormController();
		final PedidoEspecialForm form=new PedidoEspecialForm(controller);
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
			final PedidoEspecialFormController controller=new PedidoEspecialFormController(target);
			final PedidoEspecialForm form=new PedidoEspecialForm(controller);
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
		PedidoEspecialFormView.showPedido(pedido.getId());
	}
	
	public void imprimirPedidos(){
		if(getSelectedObject()!=null){
			Pedido pedido=(Pedido)getSelectedObject();
			ReportUtils2.imprimirPedido(pedido);
		}
	}
	
	public void mandarFacturar(){
		if(getSelectedObject()!=null){
			Pedido pedido=(Pedido)getSelectedObject();
			mandarFacturar(pedido);
		}
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
	
	public void registrarSurtidor(final Pedido pedido){
		String res=JOptionPane.showInputDialog("Surtidor: ");
		if(StringUtils.isNotBlank(res)){
			//pedido.set
			pedido.setSurtidor(res);
		}
	}
	
	public void canclearPedidos(){
		final Date fecha=SelectorDeFecha.seleccionar();
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
	
	
	public void reportePedidosPendientes(){
		
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
