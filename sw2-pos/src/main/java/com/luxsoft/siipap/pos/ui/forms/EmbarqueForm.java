package com.luxsoft.siipap.pos.ui.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jdesktop.swingx.VerticalLayout;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventComboBoxModel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.sw3.embarque.Embarque;
import com.luxsoft.sw3.embarque.Transporte;
import com.luxsoft.sw3.services.Services;

/**
 * Forma para el mantenimiento basico de Embarques
 * 
 * @author Ruben Cancino
 *
 */
public class EmbarqueForm extends AbstractForm implements PropertyChangeListener{
	
	private HeaderPanel header;
	private List transportes;
	private List choferes;

	public EmbarqueForm(IFormModel model) {
		super(model);		
		model.addBeanPropertyChangeListener(this);
	}
	
	protected JComponent buildHeader(){
		return getHeader();
	}
	
	protected HeaderPanel getHeader(){
		if(header==null){
			header=new HeaderPanel("","");
			updateHeader();
		}
		return header;
	}
	
	private void updateHeader(){
		header.setTitle("Mantenimiento a embarques");
		header.setDescription(""+model.getValue("sucursal"));
	}

	@Override
	protected JComponent buildFormPanel() {
		JPanel mainPanel=new JPanel(new VerticalLayout());
		final FormLayout layout=new FormLayout(
				"50dlu,2dlu,200dlu, 3dlu," +
				"50dlu,2dlu,200dlu","");		
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		builder.append("Fecha",addReadOnly("fecha"));
		builder.append("Id",addReadOnly("documento"));
		
		builder.append("Chofer",getControl("transporte"),5);
		//builder.append("Chofer",getControl("chofer"));
		
		builder.append("Km Ini",getControl("kilometroInicial"));
		builder.append("Km Fin",addReadOnly("kilometroFinal"));
		
		builder.append("Comentario",getControl("comentario"),5);
		mainPanel.add(builder.getPanel());
		
		return mainPanel;
	}
	
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("transporte".equals(property)){
			/*
			List list=getTransportes();
			SelectionInList sl=new SelectionInList(list,model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setEnabled(!model.isReadOnly());
			*/
			
			JComponent box=buildTransportesBox(model.getModel(property));
			box.setEnabled(!model.isReadOnly());
			
			
			return box;
		}
		else if("chofer".equals(property)){
			JComponent box=buildChoferesBox(model.getModel(property));
			box.setEnabled(!model.isReadOnly());
			return box;
		}
		return super.createCustomComponent(property);
	}
	
	private JComponent buildTransportesBox(final ValueModel vm){
		EventList<String> list=GlazedLists.eventList(getTransportes());
		final JComboBox box = new JComboBox();		
		AutoCompleteSupport support = AutoCompleteSupport.install(box,list, null);
		support.setFilterMode(TextMatcherEditor.CONTAINS);
		support.setStrict(false);
		final EventComboBoxModel model = (EventComboBoxModel) box.getModel();
		model.addListDataListener(new Bindings.WeakListDataListener(vm));
		box.setSelectedItem(vm.getValue());
		
		return box;
	}
	
	
	private JComponent buildChoferesBox(final ValueModel vm){
		EventList<String> list=GlazedLists.eventList(Services.getInstance()
				.getJdbcTemplate().queryForList("select NOMBRE from SX_CHOFERES",String.class));
		final JComboBox box = new JComboBox();		
		AutoCompleteSupport support = AutoCompleteSupport.install(box,list, null);
		support.setFilterMode(TextMatcherEditor.CONTAINS);
		support.setStrict(false);
		final EventComboBoxModel model = (EventComboBoxModel) box.getModel();
		model.addListDataListener(new Bindings.WeakListDataListener(vm));
		box.setSelectedItem(vm.getValue());
		
		return box;
	}

	
	public void propertyChange(PropertyChangeEvent evt) {
		
	}
	
	
	
	public List getTransportes() {
		return transportes;
	}

	public void setTransportes(List transportes) {
		this.transportes = transportes;
	}

	public List getChoferes() {
		return choferes;
	}

	public void setChoferes(List choferes) {
		this.choferes = choferes;
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				DefaultFormModel model=new DefaultFormModel(new Embarque());
				final EmbarqueForm form=new EmbarqueForm(model);
				form.setTransportes(Services.getInstance().getUniversalDao().getAll(Transporte.class));
				form.setChoferes(Services.getInstance().getJdbcTemplate().queryForList("select NOMBRE from SX_CHOFERES",String.class));
				form.open();
				if(!form.hasBeenCanceled()){					
					Object res=Services.getInstance().getUniversalDao().save(model.getBaseBean());
					showObject(res);
				}
				System.exit(0);
			}

		});
	}

}
