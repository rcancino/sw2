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
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.cxp.model.CXPAplicacion;
import com.luxsoft.siipap.cxp.model.CXPCargo;
import com.luxsoft.siipap.cxp.ui.selectores.SelectorDeCuentasPorPagar;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractMasterDetailForm;
import com.luxsoft.siipap.swing.form2.MasterDetailFormModel;
import com.luxsoft.siipap.util.MonedasUtils;

/**
 * Forma para la administracion de notas de credito
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class AnticipoForm extends AbstractMasterDetailForm{
		
	public AnticipoForm(MasterDetailFormModel model) {
		super(model);
		setTitle("Pago de Anticipo");
		model.getModel("proveedor").addValueChangeListener(new ProveedorHandler());
		model.getModel("moneda").addValueChangeListener(new MonedaHandler());		
	}
	
	protected AnticipoFormModel getAnticipoModel(){
		return (AnticipoFormModel)getModel();
	}

	@Override
	protected JComponent buildMasterForm() {
		DefaultFormBuilder builder=getDefaultMasterFormBuilder();
		if(model.getValue("id")!=null){
			builder.append("Id", addReadOnly("id"),true);			
			builder.append("Proveedor", addReadOnly("proveedor"), 9);
			getControl("moneda").setEnabled(false);
			
		}else{
			builder.append("Proveedor", getControl("proveedor"), 9);
		}		
		builder.append("Fecha", getControl("fecha"));
		builder.append("Folio", getControl("documento"),true);
		
		
		builder.append("Moneda", getControl("moneda"));
		builder.append("T.C.", getControl("tc"),true);
		builder.append("Comentario", getControl("comentario"), 5);
		builder.append("Disponible",addReadOnly("disponible"));
		updateMoneda();
		
		return builder.getPanel();
	}
	
	@Override
	protected TableFormat getTableFormat() {
		String[] props=new String[]{"fecha","cargo.documento","cargo.fecha","cargo.total","cargo.saldo","cargo.totalAnalisis","importe"};
		String[] names=new String[]{"Fecha","Documento","Fecha(F)","Total","Saldo (F)","Analizado","Por Aplicar"};
		boolean[] edits=new boolean[]{false,false,false,false,false,false,true};
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
		}else if("disponible".equals(property)){
			JFormattedTextField tf=BasicComponentFactory.createFormattedTextField(getAnticipoModel().getDisponibleModel(), NumberFormat.getCurrencyInstance());
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
	
	protected Action[] getDetallesActions(){
		return new Action[]{
				getInsertAction()
				,getDeleteAction()
				,getEditAction()
				};
	}
	
	private HeaderPanel header;
	
	protected JComponent buildHeader() {
		header = new HeaderPanel(HEADER_TITLE, "");
		return header;
	}

	public void insertPartida(){
		if(model.isReadOnly()) 
			return; //Make sure nothing happends when the form is read-only
		if(getAnticipoModel().getAnticipo().getProveedor()==null)
			return;
		Proveedor prov=getAnticipoModel().getAnticipo().getProveedor();
		Currency moneda=getAnticipoModel().getAnticipo().getMoneda();
		
		List<CXPCargo> cargos=SelectorDeCuentasPorPagar.buscarCuentasPorPagar(prov,moneda);
		if(!cargos.isEmpty()){
			getAnticipoModel().procesarAplicaciones(cargos);
		}
	}
	
	private static final String HEADER_TITLE = "Seleccione un proveedor";
	
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
		
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				//CXPAnticipo anticipo=CXPServiceLocator.getInstance().getAbonosManager().buscarAnticipo(new Long(29));
				//AnticipoFormModel model=new AnticipoFormModel(anticipo);
				AnticipoFormModel model=new AnticipoFormModel();
				AnticipoForm form=new AnticipoForm(model);
				form.open();
				if(!form.hasBeenCanceled()){
					
					showObject(model.commit());
				}
				System.exit(0);
			}
		});
	}
	
}
