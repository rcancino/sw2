package com.luxsoft.sw3.embarques.ui.catalogos;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXDatePicker;

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
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.sw3.embarque.Chofer;
import com.luxsoft.sw3.embarque.ChoferObservacion;



/**
 * 
 * @author Ruben Cancino
 *
 */
@SuppressWarnings("unchecked")
public class ChoferObservacionesPanel extends JPanel implements ListSelectionListener{
	
	private final DefaultFormModel model;
	
	private final EventList partidas;
	private EventSelectionModel selectionModel;
	
	private JXDatePicker dateField;
	private JTextField comentarioField;
	
	
	public ChoferObservacionesPanel(final DefaultFormModel model){
		this.model=model;
		partidas=GlazedLists.eventList(new BasicEventList());
		model.getPmodel().addPropertyChangeListener(PresentationModel.PROPERTYNAME_BEAN,new BeanHandler());
		updateGrid();		
		init();
		eneableForm(false);
	}
	
	private Chofer getChofer(){
		return (Chofer)model.getBaseBean();
	}
	
	private void updateGrid(){
		partidas.clear();
		partidas.addAll(getChofer().getObservaciones());
	}
	
	private void init(){
		dateField=new JXDatePicker();
		dateField.setFormats("dd/MM/yyyy");
		comentarioField=new UpperCaseField(20);
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
		builder.add(dateField,cc.xyw(3,1,5));
		builder.addLabel("Observación",cc.xy(1,3));
		builder.add(comentarioField,cc.xyw(3,3,5));
		
		
		return builder.getPanel();
	}
	
	
	private JComponent buildGridPanel(){
		TableFormat tf=GlazedLists.tableFormat(ChoferObservacion.class, new String[]{"fecha","observacion"}
		,new String[]{"Fecha","Observacion"});
		final EventTableModel tm=new EventTableModel(partidas,tf);
		final JTable table=new JTable(tm);		
		selectionModel=new EventSelectionModel(partidas);
		selectionModel.addListSelectionListener(this);
		table.setSelectionModel(selectionModel);
		final JScrollPane sp=new JScrollPane(table);
		sp.setPreferredSize(new Dimension(450,250));
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
		dateField.setEnabled(enabled);
		comentarioField.setEnabled(enabled);
	}
	
	private void cleanForm(){
		dateField.setDate(new Date());
		comentarioField.setText("");
	}
	
	public void nuevo(){
		eneableForm(true);
		cleanForm();
		comentarioField.requestFocusInWindow();
	}
	
	public void commit(){
		ChoferObservacion o=new ChoferObservacion();
		o.setFecha(dateField.getDate());
		o.setObservacion(comentarioField.getText());
		getChofer().agregarObservacion(o);
		updateGrid();
		eneableForm(false);
	}
	
	public void eliminar(){
		final List selected=new ArrayList();
		selected.addAll(selectionModel.getSelected());
		for(Object sel:selected){
			ChoferObservacion obs=(ChoferObservacion)sel;
			int index=partidas.indexOf(sel);
			if(index!=-1){
				boolean res=getChofer().eliminarObservacion(obs);
				if(res)
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
	 * Actualiza las observaciones
	 * 
	 * @author Ruben Cancino
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
				ChoferObservacion entry=(ChoferObservacion)selectionModel.getSelected().get(0);
				dateField.setDate(entry.getFecha());
				comentarioField.setText(entry.getObservacion());
			}else{
				dateField.setDate(null);
				comentarioField.setText("");
			}
		}
		
	}

}
