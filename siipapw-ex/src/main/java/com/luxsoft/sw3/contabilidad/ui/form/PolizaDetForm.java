package com.luxsoft.sw3.contabilidad.ui.form;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.commons.collections.ListUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventComboBoxModel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;

/**
 * Forma para la edicion de partidas unitarias de polizas
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PolizaDetForm extends AbstractForm{
	
	private List conceptos=new ArrayList();

	public PolizaDetForm(IFormModel model) {
		super(model);
		if(getPolizaDet().getConcepto()!=null){
			loadConceptos();
		}
		model.getModel("cuenta").addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if(evt.getNewValue()==null){
					conceptos.clear();
					getPolizaDet().setConcepto(null);
					getPolizaDet().setDescripcion(null);
				}else{
					getPolizaDet().setConcepto(null);
					CuentaContable cta=(CuentaContable)evt.getNewValue();
					getPolizaDet().setDescripcion(cta.getDescripcion());
					loadConceptos();
				}
			}
		});
	}
	
	private void loadConceptos(){
		conceptos.clear();
		CuentaContable c=(CuentaContable)model.getValue("cuenta");
		if(c!=null){
			conceptos.addAll(ServiceLocator2.getHibernateTemplate().find("from ConceptoContable c where c.cuenta.id=?",c.getId()));
		}
		
	}
	
	private PolizaDet getPolizaDet(){
		return (PolizaDet)model.getBaseBean();
	}

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout(
				"p,2dlu,200dlu"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Cuenta", getControl("cuenta"));
		builder.nextLine();
		builder.append("Concepto", getControl("concepto"));
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
			JComponent c=createCuentasControl(model.getModel(property));
			c.setEnabled(!model.isReadOnly());
			return c;
		}else if("descripcion2".equals(property)){
			JComponent c=Binder.createMayusculasTextField(model.getModel(property));
			c.setEnabled(!getModel().isReadOnly());
			return c;
		}else if("concepto".equals(property)){
			JComponent c=buildConceptoComponent(model.getModel(property));
			c.setEnabled(!getModel().isReadOnly());
			return c;
		}else if("asiento".equals(property)|| "referencia".equals(property) || "referencia2".equals(property)){
			JComponent c=Binder.createMayusculasTextField(model.getModel(property));
			c.setEnabled(!getModel().isReadOnly());
			return c;
		}
		return super.createCustomComponent(property);
	}
	
	
	
	private JComponent buildConceptoComponent(ValueModel vm){		
		SelectionInList sl=new SelectionInList(conceptos, vm);
		JComboBox box=BasicComponentFactory.createComboBox(sl);
		return box;
	}
	
	private JComponent createCuentasControl(final ValueModel vm){
		final JComboBox box=new JComboBox();
		final TextFilterator filterator=GlazedLists.textFilterator(new String[]{"clave","descripcion"});
		final EventList<CuentaContable> source=GlazedLists.eventList(getCuentas());
		final AutoCompleteSupport support = AutoCompleteSupport.install(box, source, filterator);
        support.setFilterMode(TextMatcherEditor.CONTAINS);
        support.setSelectsTextOnFocusGain(false);
        support.setStrict(false);
		support.setCorrectsCase(true);
        box.getEditor().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				Object sel=box.getSelectedItem();
				if(sel instanceof CuentaContable){
					CuentaContable selected=(CuentaContable)sel;
					CuentaContable cc=ServiceLocator2.getCuentasContablesManager().buscarPorClave(selected.getClave());
					model.setValue("cuenta", cc);
				}
			}
        });
        final EventComboBoxModel model = (EventComboBoxModel) box.getModel();
        model.addListDataListener(new ListDataListener() {
			public void intervalRemoved(ListDataEvent e) {			}				
			public void intervalAdded(ListDataEvent e) {			}				
			public void contentsChanged(ListDataEvent e) {
				if(e.getSource()!=null){
					EventComboBoxModel cm=(EventComboBoxModel)e.getSource();
					if(cm.getSelectedItem()!=null )
						if(cm.getSelectedItem() instanceof CuentaContable){
							CuentaContable cc=(CuentaContable)cm.getSelectedItem();//
							cc=ServiceLocator2.getCuentasContablesManager().buscarPorClave(cc.getClave());
							getModel().setValue("cuenta", cc);
							//vm.setValue(cc);
						}
				}
			}
		});
        box.setSelectedItem(vm.getValue());
        //if(this.model.getValue("cuenta")!=null)
        	//box.setSelectedItem(this.model.getValue("cuenta"));
		return box;
	}
	
	private List<CuentaContable> cuentas=ListUtils.EMPTY_LIST;

	public List<CuentaContable> getCuentas() {
		return cuentas;
	}

	public void setCuentas(List<CuentaContable> cuentas) {
		this.cuentas = cuentas;
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				PolizaDet det=new PolizaDet();
				det.setDescripcion2("CUADRE DE POLIZA");
				det.setReferencia("CUADRE");
				det.setReferencia2("OFICINAS");
				DefaultFormModel model=new DefaultFormModel(det);
				PolizaDetForm form=new PolizaDetForm(model);
				form.setCuentas(ServiceLocator2.getCuentasContablesManager().getCuentaContableDao().getAll());
				form.open();
			}
		});
	}

}
