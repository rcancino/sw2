/**
 * 
 */
package com.luxsoft.siipap.cxp.ui.selectores;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventComboBoxModel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxp.util.ProveedorPicker;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;

/**
 * Dialog para seleccionar un proveedor asi como un periodo
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class BuscadorDeProveedor extends AbstractForm{
	
	private String[] tipos={"GENERAL","DETALLE"};
	
	private boolean seleccionDeTipo=false;
	
	public BuscadorDeProveedor() {
		this(ProveedorPicker.getNewInstance());
	}
	
	public BuscadorDeProveedor(ProveedorPicker picker) {
		super(new DefaultFormModel(picker));
		
	}

	public ProveedorPicker getSeleccion(){
		return (ProveedorPicker)model.getBaseBean();
	}
	
	public Proveedor getProveedor(){
		return getSeleccion().getProveedor();
	}
	
	public Periodo getPeriodo(){
		return new Periodo(getSeleccion().getFechaInicial(),getSeleccion().getFechaFinal());
	}
	
	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout("p,2dlu,p:g(.5),3dlu,p,2dlu,p:g(.5)","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Proveedor",getControl("proveedor"),5);
		builder.append("Fecha Ini",getControl("fechaInicial"));
		builder.append("Fecha Fin",getControl("fechaFinal"));
		if(isSeleccionDeTipo())
			builder.append("Tipo",getControl("tipo"));
		return builder.getPanel();
	}
	
	private ValueModel tipoModel=new ValueHolder("GENERAL");
	private JComboBox tipoBox;
	@Override
	protected JComponent createCustomComponent(String property) {
		if("proveedor".equals(property)){
			return buildProveedorControl(buffer(model.getModel(property)));
		}else if(property.startsWith("fecha")){
			return Binder.createDateComponent(buffer(model.getModel(property)));
		}else if(property.startsWith("tipo") && isSeleccionDeTipo()){
			SelectionInList sl=new SelectionInList(tipos,tipoModel);
			tipoBox=BasicComponentFactory.createComboBox(sl);
			return tipoBox;
		}
		return null;
	}

	private JComponent buildProveedorControl(final ValueModel vm) {
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
	}
	
	
	
	public boolean isSeleccionDeTipo() {
		return seleccionDeTipo;
	}

	public void setSeleccionDeTipo(boolean seleccionDeTipo) {
		this.seleccionDeTipo = seleccionDeTipo;
	}
	
	public String getTipo(){
		return (String)tipoBox.getSelectedItem();
	}

	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable(){
			public void run() {
				BuscadorDeProveedor buscador=new BuscadorDeProveedor();
				buscador.open();
				System.exit(0);
			}
			
		});
		
		
	}
}