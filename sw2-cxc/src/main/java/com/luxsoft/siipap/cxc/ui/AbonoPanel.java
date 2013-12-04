package com.luxsoft.siipap.cxc.ui;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventComboBoxModel;

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.PagoConEfectivo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;


/**
 * Panel base para el mantenimiento de Abonos
 * 
 * @author Ruben Cancino
 *
 */
public class AbonoPanel extends AbstractForm{
	
	public AbonoPanel(AbonoFormModel model) {
		super(model);
		setTitle("Registro de Abonos");
		setIconImage(ResourcesUtils.getImageFromResource("images/cxc16.jpg"));
	}
	
	public AbonoFormModel getAbonoModel(){
		return (AbonoFormModel)getModel();
	}

	
	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"p,2dlu,150dlu, 2dlu," +
				"p,2dlu,150dlu"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		if(model.getValue("id")!=null){
			JComponent ct=getControl("id");
			ct.setEnabled(false);
			builder.append("Id",ct,true);
		}
		
		getControl("moneda").setEnabled(getAbonoModel().isMultiMonedaPermitido());
		getControl("tc").setEnabled(getAbonoModel().isMultiMonedaPermitido());
		
		builder.append("Cliente",getControl("cliente"),5);
		builder.nextLine();
		builder.append("Fecha",getControl("fecha"));
		builder.append("Sucursal",addReadOnly("sucursal"));
		builder.append("Moneda",getControl("moneda"));
		builder.append("T.C.",getControl("tc"));
		builder.append("Importe",getControl("total"),5);
		
		
		instalarAntesDeComentario(builder);
		builder.nextLine();
		builder.append("Comentario",getControl("comentario"),5);
		
		return builder.getPanel();		
	}	
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		
		if("comentario".equals(property)){
			JComponent control=Binder.createMayusculasTextField(model.getModel(property), false);
			control.setEnabled(!model.isReadOnly());
			return control;
			
		}else if("sucursal".equals(property)){
			JComboBox box=Bindings.createSucursalesBinding(model.getModel(property));
			model.setValue("sucursal", ServiceLocator2.getConfiguracion().getSucursal());
			boolean val=!model.isReadOnly();
			box.setEditable(val);
			box.setFocusable(val);
			return box;
		}else if("cliente".equals(property)){
			JComboBox box=createClienteBox(model.getModel(property));
			box.setEnabled(getAbonoModel().clienteModificable());
			return box;
		}
		return null;
	}
	
	protected JComboBox createClienteBox(final ValueModel vm){
		final JComboBox box=new JComboBox();
		final EventList source=getAbonoModel().getClientes();
		final TextFilterator filterator=GlazedLists.textFilterator(new String[]{"clave","nombre","rfc"});
		AutoCompleteSupport support = AutoCompleteSupport.install(box, source, filterator);
        support.setFilterMode(TextMatcherEditor.CONTAINS);
        //support.setStrict(true);
        final EventComboBoxModel model=(EventComboBoxModel)box.getModel();
        model.addListDataListener(new Bindings.WeakListDataListener(vm));
        box.setSelectedItem(vm.getValue());
        box.getEditor().getEditorComponent().addFocusListener(new FocusAdapter(){
			@Override
			public void focusLost(FocusEvent e) {
				getAbonoModel().clienteChanged();
			}
        	
        });
		return box;
	}
	
	/**
	 * Template method para que las sub-clases agregen componentes
	 * antes del comentario
	 *  
	 * @param builder
	 */
	protected void instalarAntesDeComentario(final DefaultFormBuilder builder){
		
	}
	
	public static Abono showForm(final AbonoFormModel model){
		AbonoPanel dialog=new AbonoPanel(model);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			return dialog.getAbonoModel().getAbono();
		}
		return null;
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable(){

			public void run() {
				SWExtUIManager.setup();
				AbonoFormModel model=new AbonoFormModel(Bean.proxy(PagoConEfectivo.class),false);
				//Cliente c=ServiceLocator2.getClienteManager().buscarPorClave("I020376");
				//model.setValue("cliente", c);
				model.loadClientes();				
				showForm(model);
				showObject(model.getAbono());
				
			}
			
		});
		
	}

}
