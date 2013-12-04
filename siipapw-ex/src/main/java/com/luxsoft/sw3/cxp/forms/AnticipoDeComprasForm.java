package com.luxsoft.sw3.cxp.forms;

import java.math.BigDecimal;
import java.text.NumberFormat;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventComboBoxModel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxp.model.AnticipoDeCompra;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractForm;


public class AnticipoDeComprasForm extends AbstractForm{

	public AnticipoDeComprasForm(AnticipoDeComprasFormModel model) {
		super(model);
	}
	
	public AnticipoDeComprasFormModel getController(){
		return (AnticipoDeComprasFormModel)getModel();
	}

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout = new FormLayout(
				"  60dlu,2dlu,70dlu, 3dlu,"
				+ "60dlu,2dlu,70dlu, 3dlu," 
				+ "60dlu,2dlu,70dlu"
				, "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Factura ");
		if (getModel().getValue("id") != null) {
			builder.append("Id", addReadOnly("id"));
			builder.append("Proveedor", addReadOnly("proveedor"),5);
		} else {
			builder.append("Proveedor", getControl("proveedor"), 9);
		}
		builder.append("Factura", getControl("documento"));
		builder.append("Fecha", getControl("fecha"),true);		
			
		builder.append("Importe", getControl("importe"));
		builder.append("Moneda", getControl("moneda"));
		builder.append("T.C", getControl("tc"));
		builder.nextLine();
		builder.appendSeparator("Descuentos");
		builder.append("Descuento", getControl("descuento"));
		builder.append("Docto", getControl("documentoDescuentoComercial"),true);
		builder.append("Financiero", getControl("descuentoFinanciero"));
		builder.append("Docto", getControl("documentoNota"),true);
		
		builder.appendSeparator();
		builder.append("Aplicado", addReadOnly("aplicado"));
		builder.append("Disponible", addReadOnly("disponible"));
		builder.nextLine();
	
		builder.append("Comentario", getControl("comentario"), 9);
		
		return builder.getPanel();
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if(property.startsWith("comentario")){
			JComponent control=Binder.createMayusculasTextField(model.getModel(property), false);
			control.setEnabled(!model.isReadOnly());
			return control;			
		}else if("proveedor".equals(property)){
			return buildProveedorControl(model.getModel(property));
		}else if(property.startsWith("descuento")){
			JComponent c=Bindings.createDescuentoEstandarBinding(model.getModel(property));
			c.setEnabled(!model.isReadOnly());
			return c;
		}else if(property.endsWith("Analizado")){
			JFormattedTextField tf=BasicComponentFactory.createFormattedTextField(model.getModel(property), NumberFormat.getCurrencyInstance());
			tf.setHorizontalAlignment(JFormattedTextField.RIGHT);
			return tf;
		}else if(property.startsWith("descuento")){
			JComponent c=Bindings.createDoubleBinding(model.getModel(property),6, 2);
			c.setEnabled(!model.isReadOnly());
			if(getController().getAnticipo().getNota()!=null){
				c.setEnabled(false);
			}
			return c;
		}else if("documento".equalsIgnoreCase(property)){
				JComponent c=BasicComponentFactory.createTextField(
							model.getModel(property), true);
					c.setEnabled(!getController().isReadOnly());				
				return c;
		}else if(model.getPropertyType(property)==BigDecimal.class){
			JComponent c=Binder.createBigDecimalForMonyBinding(model.getModel(property), false);
			c.setEnabled(!getController().isReadOnly());				
			return c;
		}else if("tc".equals(property)){
			JComponent c=Bindings.createDoubleBinding(model.getModel("tc"),6, 2);
			c.setEnabled(!model.isReadOnly());
			return c;
		}else 
			return super.createCustomComponent(property);
	}

	private JComponent buildProveedorControl(final ValueModel vm) {
		if (model.getValue("proveedor") == null) {
			final JComboBox box = new JComboBox();
			final EventList source = GlazedLists.eventList(ServiceLocator2
					.getProveedorManager().getAll());
			final TextFilterator filterator = GlazedLists
					//.textFilterator(new String[] { "clave" });
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

	public static AnticipoDeCompra showForm(){
		AnticipoDeComprasFormModel model=new AnticipoDeComprasFormModel();
		final AnticipoDeComprasForm form=new AnticipoDeComprasForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			AnticipoDeCompra res=model.commit();
			return res;
		}
		return null;
	}
	
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				AnticipoDeCompra res=showForm();
				if(res!=null){
					res=ServiceLocator2.getAnticipoDeComprasManager().salvar(res);
					showObject(res);
				}
				
			}
		});
	}
}
