package com.luxsoft.siipap.pos.ui.forms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.apache.commons.collections.ListUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;

/**
 * Forma para la edicion de partidas unitarias de polizas
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PolizaDetForm extends AbstractForm{

	public PolizaDetForm(IFormModel model) {
		super(model);
		
	}

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout(
				"p,2dlu,200dlu"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Cuenta", getControl("cuenta"));
		builder.nextLine();
		builder.append("Descripción 1", getControl("descripcion"));
		builder.nextLine();
		builder.append("Descripción 2", getControl("descripcion2"));
		builder.nextLine();
		builder.append("Referencia 1",getControl("referencia"));
		builder.append("Referencia 2",getControl("referencia2"));
		builder.nextLine();
		builder.append("Debe",getControl("debe"));
		builder.append("Haber",getControl("haber"));		
		builder.nextLine();
		builder.append("Asiento",getControl("asiento"));
		return builder.getPanel();
	}

	@Override
	protected JComponent createCustomComponent(String property) {
		if("cuenta".endsWith(property)){
			JComponent c=createCuentasControl();
			c.setEnabled(!model.isReadOnly());
			return c;
		}else if("descripcion".equals(property)){
			JComponent c=Binder.createMayusculasTextField(model.getModel(property));
			c.setEnabled(!getModel().isReadOnly());
			return c;
		}
		return super.createCustomComponent(property);
	}
	
	private JComponent createCuentasControl(){
		final JComboBox box=new JComboBox();
		final TextFilterator filterator=GlazedLists.textFilterator(new String[]{"clave","descripcion"});
		final EventList<CuentaContable> source=GlazedLists.eventList(getCuentas());
		final AutoCompleteSupport support = AutoCompleteSupport.install(box, source, filterator);
        support.setFilterMode(TextMatcherEditor.STARTS_WITH);
        support.setSelectsTextOnFocusGain(true);
        //support.setStrict(true);
        box.getEditor().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				Object sel=box.getSelectedItem();
				if(sel instanceof CuentaContable){
					model.setValue("cuenta", sel);
				}
			}
        });        
        if(model.getValue("cuenta")!=null)
        	box.setSelectedItem(model.getValue("cuenta"));
		return box;
	}
	
	private List<CuentaContable> cuentas=ListUtils.EMPTY_LIST;

	public List<CuentaContable> getCuentas() {
		return cuentas;
	}

	public void setCuentas(List<CuentaContable> cuentas) {
		this.cuentas = cuentas;
	}
	

}
