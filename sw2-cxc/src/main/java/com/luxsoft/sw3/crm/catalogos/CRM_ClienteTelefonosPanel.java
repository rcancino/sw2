package com.luxsoft.sw3.crm.catalogos;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.swing.controls.TelefonoTextField;
import com.luxsoft.siipap.swing.controls.UpperCaseField;



/**
 * 
 * @author Ruben Cancino
 *
 */
@SuppressWarnings("unchecked")
public class CRM_ClienteTelefonosPanel extends JPanel implements ListSelectionListener{
	
	//private final CRM_ClienteFormModel model;
	
	private final EventList partidas;
	private EventSelectionModel selectionModel;
	
	private JTextField claveField;
	private JTextField telefonoField;
	private Cliente cliente;
	
	
	public CRM_ClienteTelefonosPanel(final Cliente cliente){		
		this.cliente=cliente;
		partidas=GlazedLists.eventList(new BasicEventList());
		partidas.addAll(cliente.getTelefonos().entrySet());	
		init();
		eneableForm(false);
	}
	
	private void updateGrid(){
		partidas.clear();
		partidas.addAll(cliente.getTelefonos().entrySet());
	}
	
	private void init(){
		claveField=new UpperCaseField(20);		
		telefonoField=new JTextField(20);
		setLayout(new BorderLayout());
		add(buildFormPanel(),BorderLayout.NORTH);
		add(buildGridPanel(),BorderLayout.CENTER);
		add(ButtonBarFactory.buildRightAlignedBar(getButtons()),BorderLayout.SOUTH);		
	}
	
	private JComponent buildFormPanel(){		
		final FormLayout layout=new FormLayout(
				"p,3dlu,p:g" 
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.append("Referencia",claveField);
		builder.append("Teléfono",telefonoField);
		return builder.getPanel();
	}
	
	@SuppressWarnings("unchecked")
	private JComponent buildGridPanel(){
		final EntryFormat tf=new EntryFormat();
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
		//claveField.setEnabled(enabled);
		//telefonoField.setEnabled(enabled);
	}
	
	private void cleanForm(){
		claveField.setText("");
		telefonoField.setText("");
	}
	
	public void nuevo(){
		eneableForm(true);
		cleanForm();
		claveField.requestFocusInWindow();
		buttons[2].setEnabled(true);
	}
	
	public void commit(){
		updateGrid();
		String key=claveField.getText();
		if(StringUtils.isNotBlank(key)){
			String value=telefonoField.getText();
			if(StringUtils.isNotBlank(value)){
				cliente.getTelefonos().put(key, value);
				updateGrid();
				cleanForm();
			}
		}
		eneableForm(false);
		//buttons[2].setEnabled(false);
		
	}
	
	public void eliminar(){
		
		int inicio=selectionModel.getMinSelectionIndex();
		int fin=selectionModel.getMaxSelectionIndex();
		
		for(int index=inicio;index<=fin;index++){
			Object  c=partidas.get(index);
			Map.Entry<String, String> entry=(Map.Entry<String, String>)c;
			cliente.getTelefonos().remove(entry.getKey());
			partidas.remove(index);
			cleanForm();
			//updateGrid();
		}	
	}
	
	private JButton okButton=null;

	private JButton buildOkButton() {
		if(okButton==null){
			okButton=new JButton("Ok");
			okButton.addActionListener(EventHandler.create(ActionListener.class, this, "commit"));
			//okButton.setEnabled(false);
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
	
	
	
	private class EntryFormat implements TableFormat<Map.Entry<String, String>>{
		
		String[] cols={"Referencia","Teléfono"};

		public int getColumnCount() {
			return cols.length;
		}

		public String getColumnName(int column) {
			return cols[column];
		}

		public Object getColumnValue(Entry<String, String> row, int column) {
			return column==0?row.getKey():row.getValue();
		}
		
	}

	public void valueChanged(ListSelectionEvent e) {
		if(!e.getValueIsAdjusting()){
			if(!selectionModel.isSelectionEmpty()){
				Map.Entry<String,String> entry=(Map.Entry<String,String>)selectionModel.getSelected().get(0);
				claveField.setText(entry.getKey());
				telefonoField.setText(entry.getValue());
			}
		}
		
	}

}
