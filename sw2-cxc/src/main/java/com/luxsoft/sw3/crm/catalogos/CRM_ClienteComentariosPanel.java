package com.luxsoft.sw3.crm.catalogos;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
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

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.swing.controls.UpperCaseField;



/**
 * 
 * @author Ruben Cancino
 *
 */
@SuppressWarnings("unchecked")
public class CRM_ClienteComentariosPanel extends JPanel implements ListSelectionListener{
	
	private final CRM_ClienteFormModel model;
	
	private final EventList partidas;
	private EventSelectionModel selectionModel;
	
	private JTextField claveField;
	private JTextField comentarioField;
	
	
	public CRM_ClienteComentariosPanel(final CRM_ClienteFormModel model){
		this.model=model;
		partidas=GlazedLists.eventList(new BasicEventList());
		model.getPmodel().addPropertyChangeListener(PresentationModel.PROPERTYNAME_BEAN,new BeanHandler());
		updateGrid();		
		init();
		eneableForm(false);
	}
	
	private void updateGrid(){
		partidas.clear();
		partidas.addAll(model.getCliente().getComentarios().entrySet());
	}
	
	private void init(){
		claveField=new UpperCaseField(20);
		comentarioField=new JTextField(20);
		setLayout(new BorderLayout());
		add(buildFormPanel(),BorderLayout.NORTH);
		add(buildGridPanel(),BorderLayout.CENTER);
		add(ButtonBarFactory.buildRightAlignedBar(getButtons()),BorderLayout.SOUTH);
		
	}
	
	
	
	private JComponent buildFormPanel(){
		
		
		
		final FormLayout layout=new FormLayout(
				"50dlu,2dlu,p,2dlu,p,2dlu,p:g" 
				,"p,2dlu,p,2dlu,p,2dlu");
		final PanelBuilder builder=new PanelBuilder(layout);
		builder.setDefaultDialogBorder();
		final CellConstraints cc=new CellConstraints();
		
		
		builder.addLabel("Clave",cc.xy(1,1));
		builder.add(claveField,cc.xyw(3,1,5));
		builder.addLabel("Comentario",cc.xy(1,3));
		builder.add(comentarioField,cc.xyw(3,3,5));
		
		
		return builder.getPanel();
	}
	
	@SuppressWarnings("unchecked")
	private JComponent buildGridPanel(){
		final EntryFormat tf=new EntryFormat();
		final EventTableModel tm=new EventTableModel(partidas,tf);
		final JTable table=new JTable(tm);		
		selectionModel=new EventSelectionModel(partidas);
		selectionModel.addListSelectionListener(this);
		table.setSelectionModel(selectionModel);
		final JScrollPane sp=new JScrollPane(table);
		sp.setPreferredSize(new Dimension(200,250));
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
		claveField.setEnabled(enabled);
		comentarioField.setEnabled(enabled);
	}
	
	private void cleanForm(){
		claveField.setText("");
		comentarioField.setText("");
	}
	
	public void nuevo(){
		eneableForm(true);
		cleanForm();
		claveField.requestFocusInWindow();
	}
	
	public void commit(){
		model.getCliente().agregarComentario(claveField.getText(), comentarioField.getText());
		updateGrid();
		eneableForm(false);
	}
	
	public void eliminar(){
		final List selected=new ArrayList();
		selected.addAll(selectionModel.getSelected());
		for(Object sel:selected){
			int index=partidas.indexOf(sel);
			if(index!=-1){
				Map.Entry<String, String> entry=(Map.Entry<String, String>)sel;
				String res=model.getCliente().eliminarComentario(entry.getKey());
				if(res!=null)
					partidas.remove(index);
			}
		}
		selectionModel.clearSelection();
			
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
	
	private class EntryFormat implements TableFormat<Map.Entry<String, String>>{
		
		String[] cols={"Clave","Comentario"};

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
				comentarioField.setText(entry.getValue());
			}else{
				claveField.setText("");
				comentarioField.setText("");
			}
		}
		
	}

}
