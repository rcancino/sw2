package com.luxsoft.siipap.cxp.ui.form;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

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
import com.luxsoft.siipap.cxp.model.CXPAplicacion;
import com.luxsoft.siipap.cxp.model.CXPCargo;
import com.luxsoft.siipap.cxp.model.CXPNota;


import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractMasterDetailForm;
import com.luxsoft.siipap.swing.form2.MasterDetailFormModel;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.cxp.selectores.SelectorDeCuentasPorPagar3;

/**
 * Forma para la administracion de notas de credito
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class NotaDeCreditoForm extends AbstractMasterDetailForm{
	
	

	public NotaDeCreditoForm(MasterDetailFormModel model) {
		super(model);
		setTitle("Nota de Crédito");
		model.getModel("proveedor").addValueChangeListener(new ProveedorHandler());
		model.getModel("moneda").addValueChangeListener(new MonedaHandler());
		model.getModel("concepto").addValueChangeListener(new ConceptoHandler());
		
	}
	
	protected NotaDeCreditoFormModel getNotasModel(){
		return (NotaDeCreditoFormModel)getModel();
	}

	@Override
	protected JComponent buildMasterForm() {
		final FormLayout layout=new FormLayout(
				"max(p;40dlu),2dlu,max(p;110dlu), 2dlu," +
				"max(p;40dlu),2dlu,max(p;110dlu), 2dlu," +
				"max(p;40dlu),2dlu,max(p;110dlu), 2dlu," +
				"max(p;40dlu),2dlu,max(p;110dlu)"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		if(model.getValue("id")!=null){
			builder.append("Id", addReadOnly("id"),true);
			//builder.append("Disponible", addReadOnly("disponible"));
			//builder.append("Autorización", addReadOnly("autorizacion"));
			builder.append("Proveedor", addReadOnly("proveedor"), 9);
			//getControl("concepto").setEnabled(false);
			getControl("moneda").setEnabled(false);
		}else{
			builder.append("Proveedor", getControl("proveedor"), 9);
		}
		builder.append("Folio", getControl("documento"));
		builder.append("Concepto",getControl("concepto"));
		builder.append("Fecha", getControl("fecha"));
		
		
		
		builder.append("Moneda", getControl("moneda"));
		builder.append("T.C.", getControl("tc"),true);
		builder.append("Comentario", getControl("comentario"), 5);
		builder.append("Disponible",addReadOnly("disponible"));
		updateMoneda();
		
		return builder.getPanel();
	}

	@Override
	protected TableFormat getTableFormat() {
		String[] props=new String[]{"fecha","cargo.documento","cargo.fecha","cargo.total","cargo.saldo","factura.totalAnalizadoConFlete","cargo.cargosCM","importe"};
		String[] names=new String[]{"Fecha","Documento","Fecha(F)","Total","Saldo (F)","Analizado","Cargos","Por Aplicar"};
		boolean[] edits=new boolean[]{false,false,false,false,false,false,false,true};
		return GlazedLists.tableFormat(CXPAplicacion.class,props, names,edits);
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		
		if("comentario".equals(property)){
			JComponent control=Binder.createMayusculasTextField(model.getModel(property), false);
			control.setEnabled(!model.isReadOnly());
			return control;			
		}else if("proveedor".equals(property)){
			return buildProveedorControl(model.getModel(property));
		}else if(property.startsWith("descuento")){
			JComponent c=Bindings.createDescuentoEstandarBinding(model.getModel(property));
			c.setEnabled(!model.isReadOnly());
			return c;
		}else if("concepto".equals(property)){
			SelectionInList sl=new SelectionInList(CXPNota.Concepto.values(),model.getModel("concepto"));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("disponible".equals(property)){
			JFormattedTextField tf=BasicComponentFactory.createFormattedTextField(getNotasModel().getDisponibleModel(), NumberFormat.getCurrencyInstance());
			tf.setEnabled(false);
			return tf;
		}
		else return super.createCustomComponent(property);
	}
	
	

	private JComponent buildProveedorControl(final ValueModel vm) {
		if (model.getValue("proveedor") == null) {
			final JComboBox box = new JComboBox();
			final EventList source = GlazedLists.eventList(ServiceLocator2
					.getProveedorManager().getAll());
			final TextFilterator filterator = GlazedLists
					.textFilterator(new String[] { "clave", "nombre", "rfc" });
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
				,"p,2dlu,p,2dlu,p");
		
		//final FormDebugPanel debugPanel=new FormDebugPanel(layout);
		
		final PanelBuilder builder=new PanelBuilder(layout);		
		final CellConstraints cc=new CellConstraints();
		
		if(!model.isReadOnly())			
			builder.add(buildValidationPanel(),cc.xywh(1, 1,1,5));
		
		builder.addLabel("Importe",cc.xy(3, 1));
		builder.add(addReadOnly("importe"),cc.xy(5, 1));
		
		builder.addLabel("Impuesto",cc.xy(3, 3));
		builder.add(addReadOnly("impuesto"),cc.xy(5, 3));
		
		builder.addLabel("Total",cc.xy(3, 5));
		if(!getMainModel().isReadOnly()){
			getControl("total").setEnabled(getNotasModel().isTotalMutable());
		}else
			getControl("total").setEnabled(false);		
		builder.add(getControl("total"),cc.xy(5, 5));	
				
		return builder.getPanel();
	}
	
	
	
	protected Action[] getDetallesActions(){
		return new Action[]{
				//getInsertAction()
				buscarFacturasAction()
				,getDeleteAction()
				,getEditAction()
				};
	}
	
	private Action buscarFacturasAction;
	
	protected Action buscarFacturasAction(){
		if(buscarFacturasAction==null){
			buscarFacturasAction=new DispatchingAction(this,"seleccionarFacturas");
			CommandUtils.configAction(buscarFacturasAction, "seleccionarCuentasPorPagar", null);
		}
		return buscarFacturasAction;
	}
	
	private HeaderPanel header;
	
	protected JComponent buildHeader() {
		header = new HeaderPanel(HEADER_TITLE, "");
		return header;
	}

	public void insertPartida(){
		seleccionarFacturas();
	}
	
	public void seleccionarFacturas(){
		if(model.isReadOnly()) 
			return; //Make sure nothing happends when the form is read-only
		
		if(getNotasModel().getNota().getProveedor()==null)
			return;
		List<CXPCargo> cargos=SelectorDeCuentasPorPagar3.buscarCuentasPorPagar(
				getNotasModel().getNota().getProveedor(), 
				getNotasModel().getNota().getMoneda());
		if(!cargos.isEmpty()){
			getNotasModel().procesarAplicaciones(cargos);
		}
	}
	
	private static final String HEADER_TITLE = "Seleccione un proveedor";

	
	
	@Override
	public void doDeletePartida(Object obj) {
		// TODO Auto-generated method stub
		super.doDeletePartida(obj);
	}

	private void updateMoneda(){
		Currency selected=(Currency)getMainModel().getValue("moneda");
		if(selected.equals(MonedasUtils.PESOS)){
			model.setValue("tc", new Double(1));
			getControl("tc").setEnabled(false);
		}else{
			
			JTextField tf=(JTextField)getControl("tc");
			tf.setEnabled(true);
			tf.requestFocus();
			
			
		}
	}
	
	
	
	
	private class MonedaHandler implements PropertyChangeListener{

		public void propertyChange(PropertyChangeEvent evt) {
			updateMoneda();
			
		}
		
	}
	
	private class ProveedorHandler implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent evt) {
			Proveedor p = (Proveedor) evt.getNewValue();
			if (p != null) {
				header.setTitle(p.getNombreRazon());
				header.setDescription("Analisis de factura para la generación de la cuenta por pagar");
			} else {
				header.setTitle(HEADER_TITLE);
				header.setDescription("");
			}
		}
	}
	
	private class ConceptoHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			System.out.println("Actualizando: "+evt.getNewValue()+ "Modificable: "+getNotasModel().isTotalMutable());
			getControl("total").setEnabled(getNotasModel().isTotalMutable());
		}		
	}

	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				//CXPNota nota=CXPServiceLocator.getInstance().getAbonosManager().buscarNota(new Long(11));
				//NotaDeCreditoFormModel model=new NotaDeCreditoFormModel(nota);
				NotaDeCreditoFormModel model=new NotaDeCreditoFormModel();
				NotaDeCreditoForm form=new NotaDeCreditoForm(model);
				form.open();
				if(!form.hasBeenCanceled()){
					
					showObject(model.commit());
				}
				System.exit(0);
			}
		});
	}
	
}
