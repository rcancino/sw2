package com.luxsoft.siipap.cxp.ui.form;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventComboBoxModel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.cxp.service.CXPServiceLocator;
import com.luxsoft.siipap.cxp.ui.selectores.SelectorDeContraRecibos;
import com.luxsoft.siipap.cxp.ui.selectores.SelectorDeFacturasPorRequisitar;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.model.gastos.GProductoServicio;
import com.luxsoft.siipap.model.tesoreria.Concepto;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.binding.CantidadMonetariaControl;
import com.luxsoft.siipap.swing.form2.AbstractMasterDetailForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;




/**
 * Forma para el mantenimiento de instancias de {@link GProductoServicio}
 * 
 * @author Ruben Cancino
 *
 */
public class RequisicionForm extends AbstractMasterDetailForm{
	
	protected Logger logger=Logger.getLogger(getClass());

	public RequisicionForm(RequisicionFormModel model) {
		super(model);
		setTitle("Requisición");
		
		model.getModel("concepto").addValueChangeListener(new ConceptoHandler());
		getMainModel().totalModel.addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {			
				//System.out.println("Nuvvv total: "+evt.getNewValue());
			}
		});
	}
	
	public RequisicionFormModel getMainModel(){
		return (RequisicionFormModel)getModel();
	}

	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Formato de requisición","Cuentas por Pagar (Compras)");
	}
	
	/***** Binding support ******/
	

	protected JComponent buildMasterForm(){		
		final FormLayout layout=new FormLayout(
				"max(p;40dlu),2dlu,max(p;110dlu), 2dlu," +
				"max(p;40dlu),2dlu,max(p;110dlu), 2dlu," +
				"max(p;40dlu),2dlu,max(p;110dlu), 2dlu," +
				"max(p;40dlu),2dlu,max(p;110dlu)"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		//final DefaultFormBuilder builder=getDefaultMasterFormBuilder();
		if(model.getValue("id")!=null){
			JComponent ct=getControl("id");
			ct.setEnabled(false);
			builder.append("Id",ct,true);
			getControl("moneda").setEnabled(false);
		}		
		builder.append("Proveedor",getControl("proveedor"),9);
		builder.append("Concepto",getControl("concepto"),true);		
		
		builder.append("Fecha",getControl("fecha"));
		builder.append("Fecha P.",getControl("fechaDePago"));
		builder.append("Moneda",getControl("moneda"));		
		builder.append("TC",getControl("tipoDeCambio"),true);
		
		builder.append("F. Pago",getControl("formaDePago"));		
		builder.append("Manejar DF",getControl("descuentoFinanciero"));
		builder.append("Des. F",getControl("df"),true);
		
		//builder.nextLine();
		builder.append("Comentario",getControl("comentario"),9);
		
		//getControl("total").setEnabled(false);
		
		return builder.getPanel();
	}
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("proveedor".equals(property)){
			return buildProveedorControl(model.getModel(property));
		}else if("concepto".equals(property)){			
			SelectionInList sl=new SelectionInList(getMainModel().getConceptosValidos(),model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setEnabled(!model.isReadOnly());
			
			return box;
		}else  if("formaDePago".equals(property)){
			JComboBox box=Bindings.createFormasDePagoBinding(model.getComponentModel(property));
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("comentario".equals(property)){
			JComponent control=Binder.createMayusculasTextField(model.getModel(property), false);
			control.setEnabled(!model.isReadOnly());
			return control;			
		}else if("descuentoFinanciero".equals(property)){
			JCheckBox box=BasicComponentFactory.createCheckBox(getMainModel().getConDescuentoModel(), "");
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("df".equals(property)){
			JComponent  c=Bindings.createDescuentoEstandarBinding(getMainModel().getDFModel());
			return c;
			
		}
		return null;
	}
	
	private Action insertarPorContrarecibo;
	
	protected Action[] getDetallesActions(){
		if(actions==null){
			insertarPorContrarecibo=new DispatchingAction(this,"insertarPorRequisicion");
			insertarPorContrarecibo.putValue(Action.NAME, "X Req");
			insertarPorContrarecibo.putValue(Action.SHORT_DESCRIPTION, "Buscar facturas por requisición");
			insertarPorContrarecibo.putValue(Action.SMALL_ICON,ResourcesUtils.getIconFromResource("images2/application_view_list.png"));
			actions=new Action[]{
				getInsertAction()
				,getDeleteAction()
				,getEditAction()
				,insertarPorContrarecibo
				//,getViewAction()
				};
		}
		return actions;
	}
	
	private JComponent buildProveedorControl(final ValueModel vm) {
		if (model.getValue("proveedor") == null) {
			final JComboBox box = new JComboBox();
			final EventList source = GlazedLists.eventList(ServiceLocator2
					.getProveedorManager().getAll());
			final TextFilterator filterator = GlazedLists
					.textFilterator(new String[] { "clave", "nombre"});
			AutoCompleteSupport support = AutoCompleteSupport.install(box,
					source, filterator);
			support.setFilterMode(TextMatcherEditor.CONTAINS);
			support.setStrict(false);
			final EventComboBoxModel model = (EventComboBoxModel) box
					.getModel();
			model.addListDataListener(new Bindings.WeakListDataListener(vm));
			box.setSelectedItem(vm.getValue());
			return box;
		} else {
			String prov = ((Proveedor) vm.getValue()).getNombreRazon();			
			JLabel label = new JLabel(prov);
			return label;
		}
	}
	
	/****  Configuracion de el detalle ****/
	
	
	protected TableFormat getTableFormat(){
		final String[] props={
				"facturaDeCompras.id"
				,"documento"
				,"fechaDocumento"
				,"facturaDeCompras.vencimiento"
				,"facturaDeCompras.total"
				,"facturaDeCompras.totalAnalisis"
				,"facturaDeCompras.totalFlete"
				,"facturaDeCompras.totalCargos"
				,"facturaDeCompras.bonificadoCM"				
				,"facturaDeCompras.importeDescuentoFinanciero"
				,"facturaDeCompras.vencimientoDF"
				,"total"};
		final String[] names={
				"Analisis"
				,"Factura"
				,"Fecha F"
				,"Vto F"
				,"Total (Fac)"
				,"Análisis"
				,"Flete "
				,"Cargos "
				,"Bonificaciones "
				,"Desc F"
				,"Vto DF"
				,"Requisitado"};
		return GlazedLists.tableFormat(RequisicionDe.class, props, names);
	}
		
	public void insertPartida(){
		if(model.isReadOnly() || (model.getValue("proveedor")==null)) 
			return; //Make sure nothing happends when the form is read-only
		System.out.println("Localizando facturas para: "+getMainModel().getMasterBean().getProveedor()+  " en : "+getMainModel().getMasterBean().getMoneda());
		List<CXPFactura> facturas=SelectorDeFacturasPorRequisitar.buscarFacturasParaRequisitar(
				getMainModel().getMasterBean().getProveedor(), 
				getMainModel().getMasterBean().getMoneda());
		if(!facturas.isEmpty()){
			getMainModel().procesarFacturas(facturas);
		}
	}
	
	public void insertarPorRequisicion(){
		Proveedor p=getMainModel().getMasterBean().getProveedor();
		if(model.isReadOnly() || (p==null)) 
			return; //Make sure nothing happends when the form is read-only		
		List<String> numeros=SelectorDeContraRecibos.buscarFacturasRecibidasPendientes(p);
		List<CXPFactura> facturas=CXPServiceLocator.getInstance().getFacturasManager().buscarFacturas(numeros, p);
		if(!facturas.isEmpty()){
			getMainModel().procesarFacturas(facturas);
		}
	}
	
	
	
	protected void doView(Object obj){
		RequisicionDe det=(RequisicionDe)obj;
		DefaultFormModel model=new DefaultFormModel(det,true);
		RequisicionDetForm form=new RequisicionDetForm(model);
		form.open();
	}
	
	@Override
	protected void doEdit(Object obj) {
		RequisicionDe det=(RequisicionDe)obj;
		if(det!=null){
			DefaultFormModel model=new DefaultFormModel(det,false);
			RequisicionDetForm form=new RequisicionDetForm(model);
			form.open();
			if(!form.hasBeenCanceled()){
				getMainModel().afterEdit(model.getBaseBean());
				
			}
		}
	}

	
	
	/**
	 * Consstruye el panel de Importe, impuesto y total. Asume que el modelo puede
	 * proporcionar {@link ValueModel} para estas propiedades
	 * Tambien coloca aqui el panel de validación si la fomra no es de solo lectura
	 * 
	 * @return
	 */
	protected JComponent buildTotalesPanel(){
		
		final FormLayout layout=new FormLayout(
				"p:g,5dlu,p,2dlu,max(p;50dlu)"
				,"p,2dlu,p,2dlu,p:g");		
		//final FormDebugPanel debugPanel=new FormDebugPanel(layout);		
		final PanelBuilder builder=new PanelBuilder(layout);		
		final CellConstraints cc=new CellConstraints();
		
		if(!model.isReadOnly())			
			builder.add(buildValidationPanel(),cc.xywh(1, 1,1,5));
		
		builder.addLabel("Total",cc.xy(3, 5));		
		builder.add(addReadOnly("total"),cc.xy(5, 5));
		
		
		
		CantidadMonetariaControl cont=new CantidadMonetariaControl(getMainModel().totalModel);
		cont.setEnabled(false);
		builder.addLabel("Nvo Total",cc.xy(3, 1));
		builder.add(cont,cc.xy(5, 1));
		
		return builder.getPanel();
	}
	
	
	
	public static Requisicion showForm(){
		return showForm(new Requisicion());
	}
	
	public static Requisicion showForm(Requisicion bean){
		return showForm(bean,false);
	}
	
	public static Requisicion showForm(Requisicion bean,boolean readOnly){
		RequisicionFormModel model=new RequisicionFormModel(bean);
		model.setReadOnly(readOnly);
		final RequisicionForm form=new RequisicionForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.getMasterBean();
		}
		return null;
	}
	
	
	private class ConceptoHandler implements PropertyChangeListener{

		public void propertyChange(PropertyChangeEvent evt) {
			Concepto c=(Concepto)evt.getNewValue();
			if(!model.isReadOnly() && (c!=null)){
				getControl("total").setEnabled(c.getClave().equals("ANTICIPO"));
				
			}
			
		}
		
	}
	
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable(){
			public void run() {
				SWExtUIManager.setup();
				//Requisicion target=ServiceLocator2.getRequisiciionesManager().buscarRequisicionDeCompras(71L);
				//Requisicion  bean=showForm(target);
				Requisicion  bean=showForm();
				if(bean!=null){
					//bean=ServiceLocator2.getRequisiciionesManager().save(bean);
					RequisicionForm.showObject(bean);
					//ServiceLocator2.getRequisiciionesManager().save(bean);
				}
				System.exit(0);
				
			}
		});
		
	}

}
