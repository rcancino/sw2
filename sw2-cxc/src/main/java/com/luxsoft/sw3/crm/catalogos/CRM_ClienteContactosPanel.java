package com.luxsoft.sw3.crm.catalogos;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.beans.EventHandler;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.Contacto;
import com.luxsoft.siipap.swing.binding.Binder;



/**
 * 
 * @author Ruben Cancino
 *
 */
@SuppressWarnings("unchecked")
public class CRM_ClienteContactosPanel extends JPanel implements ListSelectionListener{
	
	private Cliente cliente;
	private PresentationModel contactoModel;
	private final EventList partidas;
	private EventSelectionModel selectionModel;
	
	private JTextField nombre;
	private JTextField puesto;
	private JTextField telefono;
	private JTextField email;
	
	
	public CRM_ClienteContactosPanel(Cliente cliente){
		this.cliente=cliente;
		partidas=GlazedLists.eventList(new BasicEventList());
		updateGrid();		
		init();
		eneableForm(false);
	}
	
	private void updateGrid(){
		partidas.clear();
		partidas.addAll(cliente.getContactos());
	}
	
	private ValueModel contactoChanel=new ValueHolder(new Contacto(),true);
	
	private void init(){
		contactoModel=new PresentationModel(contactoChanel);
		
		nombre=Binder.createMayusculasTextField(contactoModel.getComponentModel("nombre"));
		puesto=Binder.createMayusculasTextField(contactoModel.getComponentModel("puesto"));		
		telefono=BasicComponentFactory.createTextField(contactoModel.getComponentModel("telefono"));
		email=BasicComponentFactory.createTextField(contactoModel.getComponentModel("email1"));
		
		setLayout(new BorderLayout());
		add(buildFormPanel(),BorderLayout.NORTH);
		add(buildGridPanel(),BorderLayout.CENTER);
		add(ButtonBarFactory.buildRightAlignedBar(getButtons()),BorderLayout.SOUTH);		
	}
	
	private JComponent buildFormPanel(){
		
		final FormLayout layout=new FormLayout(
				"p,2dlu,p:g"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);		
		builder.setDefaultDialogBorder();
		builder.append("Nombre",nombre);
		builder.append("Puesto",puesto);
		builder.append("Telefono",telefono);
		builder.append("Email",email);
		
		return builder.getPanel();
	}
	
	@SuppressWarnings("unchecked")
	private JComponent buildGridPanel(){
		final TableFormat tf=GlazedLists.tableFormat(Contacto.class, new String[]{"nombre","puesto"}
		, new String[]{"Nombre","Puesto"});
		final EventTableModel tm=new EventTableModel(partidas,tf);
		final JTable table=new JTable(tm);		
		selectionModel=new EventSelectionModel(partidas);
		selectionModel.setSelectionMode(ListSelection.SINGLE_SELECTION);
		selectionModel.addListSelectionListener(this);
		table.setSelectionModel(selectionModel);
		final JScrollPane sp=new JScrollPane(table);
		sp.setPreferredSize(new Dimension(100,100));
		return sp;
	}
	
	private JButton[] buttons;
	
	private JButton[] getButtons(){
		if(buttons==null){
			buttons=new JButton[3];
			buttons[0]=buildAddButton();
			buttons[1]=buildRemoveButton();
			buttons[2]=buildOkButton();
		}
		
		return buttons;
	}
	
	public void setEnabled(boolean val){
		super.setEnabled(val);
		for(JButton b:buttons){
			b.setEnabled(val);
		}
	}
	
	private void eneableForm(boolean enabled){
		/*nombre.setEnabled(enabled);
		puesto.setEnabled(enabled);
		telefono.setEnabled(enabled);
		email.setEnabled(enabled);
		*/
	}
	
	public void nuevo(){
		contactoChanel.setValue(new Contacto());
		eneableForm(true);
		nombre.requestFocusInWindow();
		//buttons[2].setEnabled(true);
	}
	
	public void commit(){
		//contactoModel.triggerCommit();
		Contacto c=(Contacto)contactoModel.getBean();
		if(c!=null){
			cliente.agregarContacto(c);
			updateGrid();
		}
		//eneableForm(false);
		contactoChanel.setValue(new Contacto());
		//buttons[2].setEnabled(false);
	}
	
	public void eliminar(){
		//final EventList selection=selectionModel.getSelected();
		int inicio=selectionModel.getMinSelectionIndex();
		int fin=selectionModel.getMaxSelectionIndex();
		
		for(int index=inicio;index<=fin;index++){
			Contacto c=(Contacto)partidas.get(index);
			boolean res=cliente.getContactos().remove(c);
			if(res){
				partidas.remove(index);
				contactoChanel.setValue(new Contacto());
				updateGrid();
			}
			
		}
		/*
		for(int i=0;i<selection.size();i++){
			Object  obj=partidas.get(i);
			Contacto c=(Contacto)obj;
			if(c!=null){
				cliente.eliminarContacto(c);
				contactoChanel.setValue(new Contacto());
				updateGrid();
			}
		}
		*/	
	}
	
	private JButton okButton=null;

	private JButton buildOkButton() {
		if(okButton==null){
			okButton=new JButton("Ok");
			okButton.addActionListener(EventHandler.create(ActionListener.class, this, "commit"));
			okButton.setEnabled(false);
		}
		
		return okButton;
	}

	private JButton buildRemoveButton() {
		JButton delete=new JButton("Eliminar");
		delete.addActionListener(EventHandler.create(ActionListener.class, this, "eliminar"));
		return delete;
	}

	private JButton buildAddButton() {
		JButton add=new JButton("Nuevo");
		add.addActionListener(EventHandler.create(ActionListener.class, this, "nuevo"));
		return add;
	}
	

	public void valueChanged(ListSelectionEvent e) {
		if(!e.getValueIsAdjusting()){
			if(!selectionModel.isSelectionEmpty()){
				Contacto c=(Contacto)selectionModel.getSelected().get(0);
				contactoChanel.setValue(c);
			}
		}
	}

}
