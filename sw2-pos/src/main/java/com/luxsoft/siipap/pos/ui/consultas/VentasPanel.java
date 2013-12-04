package com.luxsoft.siipap.pos.ui.consultas;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanWrapperImpl;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.pos.POSActions;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.sw3.services.PedidosManager;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.Pedido;

/**
 * Panel para la consulta y administracion de Pedidos
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class VentasPanel extends FilteredBrowserPanel<Pedido> implements PropertyChangeListener{

	public VentasPanel() {
		super(Pedido.class);
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
			header.setDescription("Periodo: "+periodo.toString());
	}
	
	public void propertyChange(PropertyChangeEvent evt) {
		updateHeader();
	}



	protected void init(){		
		updateHeader();
		addProperty(
				"id"
				,"estado"
				,"operador"
				,"fecha"
				,"clave"
				,"nombre"
				,"origen"
				,"moneda"
				,"total"
				,"formaDePago"				
				,"utilidad"				
				,"porAutorizar"
				,"comentario"
				);
		addLabels(
				"id"
				,"Est"
				,"Facturista"
				,"Fecha"
				,"Cliente"
				,"Nombre"
				,"Tipo"
				,"Mon"
				,"Total"
				,"F.P."				
				,"Util (%)"
				,"Por autorizar"
				,"Comentario"
				);
		
		installTextComponentMatcherEditor("Cliente", "clave","nombre");		
		installTextComponentMatcherEditor("Total", "total");
		manejarPeriodo();
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
		if(KernellSecurity.instance().hasRole(POSRoles.ADMINISTRADOR_DE_VENTAS.name()))
			procesos.add(addAction("", "cancelarPedidos", "Cancelar Pedidos"));
		return procesos;
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getViewAction()
				,getInsertAction()
				,getEditAction()
				,getDeleteAction()
				,addAction(POSActions.GeneracionDePedidos.getId(),"reportePedidosPendientes", "Rep Pendientes")
				,addContextAction(new Exportable(),POSActions.GeneracionDePedidos.getId(),"exportarPedido", "Exportar")
				,addContextAction(new Exportable(),POSActions.GeneracionDePedidos.getId(),"solicitarFacturacion", "Mandar a Facturar")
				};
		return actions;
	}	
	
	
	protected void executeLoadWorker(final SwingWorker worker){
		TaskUtils.executeSwingWorker(worker);
	}

	@Override
	protected List<Pedido> findData() {
		String hql="from Pedido p where p.fecha between ? and ?";
		return Services.getInstance().getHibernateTemplate().find(hql,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
	}
	
	public void reportePedidosPendientes(){
		
	}
	
	public void solicitarFacturacion(){
		List<Pedido> selected=new ArrayList<Pedido>(getSelected());
		for(Pedido p:selected){
			if(p.isFacturable())
				continue;
			if(p.isPorAutorizar())
				continue;
			int index=source.indexOf(p);
			if(index!=-1){
				p.setFacturable(true);
				p=getManager().save(p);
				source.set(index, p);
			}
		}
	}
	
	/**
	 * Exporta un pedido a otra sucursal por WEB-Services
	 * 
	 */
	public void exportarPedido(){
		
	}
	
	/**
	 * Cancela los pedidos con N dias de anterioridad
	 * 
	 */
	public void cancelarPedidos(){
		
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
	
	private class Exportable implements Predicate{

		public boolean evaluate(Object bean) {
			Pedido p=(Pedido)bean;
			return (p.getEstado().equalsIgnoreCase("PENDIENTE") && (!p.isPorAutorizar()));
		}
		
	}

}
