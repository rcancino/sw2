package com.luxsoft.siipap.cxc.ui.clientes;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.core.Contacto;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.TelefonoTextField;



/**
 * 
 * @author Ruben Cancino
 *
 */
@SuppressWarnings("unchecked")
public class ClienteContactosPanel extends JPanel implements ListSelectionListener{
	
	private final ClienteFormModel model;
	private final PresentationModel contactoModel;
	
	private final EventList partidas;
	private EventSelectionModel selectionModel;
	
	private JTextField nombre;
	private JTextField puesto;
	private JTextField telefono;
	private JTextField email;
	
	
	public ClienteContactosPanel(final ClienteFormModel model){
		this.model=model;
		contactoModel=new PresentationModel(model.getContactosChannel());
		partidas=GlazedLists.eventList(new BasicEventList());
		model.getPmodel().addPropertyChangeListener(PresentationModel.PROPERTYNAME_BEAN,new BeanHandler());
		updateGrid();		
		init();
		eneableForm(false);
	}
	
	private void updateGrid(){
		partidas.clear();
		partidas.addAll(model.getCliente().getContactos());
	}
	
	private void init(){
		nombre=Binder.createMayusculasTextField(contactoModel.getComponentModel("nombre"));
		puesto=Binder.createMayusculasTextField(contactoModel.getComponentModel("puesto"));
		
		telefono=new TelefonoTextField(20);
		Bindings.bind(telefono, contactoModel.getComponentModel("telefono"));
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
		nombre.setEnabled(enabled);
		puesto.setEnabled(enabled);
		telefono.setEnabled(enabled);
		email.setEnabled(enabled);
	}
	
	private void cleanForm(){
		nombre.setText("");
		puesto.setText("");
		telefono.setText("");
		email.setText("");
	}
	
	public void nuevo(){
		model.getContactosChannel().setValue(new Contacto());
		eneableForm(true);
		cleanForm();
		nombre.requestFocusInWindow();
		buttons[2].setEnabled(true);
	}
	
	public void commit(){
		//contactoModel.triggerCommit();
		Contacto c=(Contacto)contactoModel.getBean();
		if(c!=null){
			model.getCliente().agregarContacto(c);
			updateGrid();
		}
		eneableForm(false);
		buttons[2].setEnabled(false);
	}
	
	public void eliminar(){
		final EventList selection=selectionModel.getSelected();
		for(int i=0;i<selection.size();i++){
			Object  obj=partidas.get(i);
			Contacto c=(Contacto)obj;
			if(c!=null){
				model.getCliente().eliminarContacto(c);
				updateGrid();
			}
		}	
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
	
	/**
	 * Actualiza los comentarios si cambia el producto
	 * 
	 * @author RUBEN
	 *
	 */
	private class BeanHandler implements PropertyChangeListener{

		public void propertyChange(PropertyChangeEvent evt) {
			updateGrid();
		}
		
	}
	

	public void valueChanged(ListSelectionEvent e) {
		if(!e.getValueIsAdjusting()){
			if(!selectionModel.isSelectionEmpty()){
				Contacto c=(Contacto)selectionModel.getSelected().get(0);
				model.getContactosChannel().setValue(c);
			}
		}
		
	}

}
