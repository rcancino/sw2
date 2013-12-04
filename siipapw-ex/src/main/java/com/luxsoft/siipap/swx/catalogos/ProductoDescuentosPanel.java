package com.luxsoft.siipap.swx.catalogos;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.core.Descuento;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;


/**
 * 
 * @author Ruben Cancino
 *
 */
public class ProductoDescuentosPanel extends JPanel{
	
	private final PresentationModel model;
	private final PresentationModel codigosModel;
	private final ValueHolder beanChannell;
	
	private final EventList<Descuento> partidas;
	private EventSelectionModel selectionModel;
	
	private JTextField ordenField;
	private JFormattedTextField valorField;
	private JCheckBox activoField;
	private JTextField descripcionField;
	
	
	public ProductoDescuentosPanel(final PresentationModel model){
		this.model=model;
		partidas=GlazedLists.eventList(new BasicEventList<Descuento>());
		beanChannell=new ValueHolder(null,true);
		model.addPropertyChangeListener(PresentationModel.PROPERTYNAME_BEAN,new BeanHandler());
		codigosModel =new PresentationModel(beanChannell);	
		codigosModel.addPropertyChangeListener(PresentationModel.PROPERTYNAME_BUFFERING,new CommitHandler());
		eneableForm(false);
		init();
	}
	
	
	private Producto getProducto(){
		return (Producto)model.getBean();
	}
	
	private void updateGrid(){
		partidas.clear();
		partidas.addAll(getProducto().getDescuentos());
	}
	
	private void init(){
		setLayout(new BorderLayout());
		add(buildFormPanel(),BorderLayout.NORTH);
		add(buildGridPanel(),BorderLayout.CENTER);
		add(ButtonBarFactory.buildRightAlignedBar(getButtons()),BorderLayout.SOUTH);
	}
	
	
	
	private JComponent buildFormPanel(){
		
		ordenField=BasicComponentFactory.createIntegerField(codigosModel.getBufferedComponentModel("orden"));
		valorField=Bindings.createDescuentoEstandarBinding(codigosModel.getBufferedComponentModel("descuento"));
		//valorField=Binder.createDescuentoBinding(codigosModel.getBufferedComponentModel("valor"));
		
		descripcionField=Binder.createMayusculasTextField(codigosModel.getBufferedComponentModel("descripcion"));
		activoField=BasicComponentFactory.createCheckBox(codigosModel.getBufferedComponentModel("activo"), "");
		
		final FormLayout layout=new FormLayout(
				"p,2dlu,p:g(.5),2dlu,p,2dlu,p:g(.5)" 
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Orden",ordenField);
		builder.append("Descuento",valorField);
		builder.append("Activo",activoField,true);
		builder.append("Descripción",descripcionField,5);
		builder.setDefaultDialogBorder();
		
		
		
		
		
		return builder.getPanel();
	}
	
	@SuppressWarnings("unchecked")
	private JComponent buildGridPanel(){
		final TableFormat<Descuento> tf=GlazedLists.tableFormat(Descuento.class, new String[]{"orden","descuento","descripcion","activo"}
		,new String[]{"Orden","Descuento","Descripción","Activo"});
		final EventTableModel<Descuento> tm=new EventTableModel<Descuento>(partidas,tf);
		final JTable table=new JTable(tm);
		selectionModel=new EventSelectionModel(partidas);
		table.setSelectionModel(selectionModel);
		final JScrollPane sp=new JScrollPane(table);
		sp.setPreferredSize(new Dimension(200,250));
		return sp;
	}
	
	private JButton[] buttons;
	
	private JButton[] getButtons(){
		if(buttons==null){
			buttons=new JButton[5];
			buttons[0]=buildAddButton();
			buttons[1]=buildRemoveButton();
			buttons[2]=buildEdditButton();
			buttons[3]=buildResetButton();
			buttons[4]=buildOkButton();
		}
		
		return buttons;
	}
	
	public void setEnabled(boolean val){
		super.setEnabled(val);
		for(JButton b:buttons){
			b.setEnabled(val);
		}
	}
	
	
	private void eneableForm(boolean val){
		codigosModel.getBufferedComponentModel("orden").setEnabled(val);
		codigosModel.getBufferedComponentModel("descuento").setEnabled(val);
		codigosModel.getBufferedComponentModel("descripcion").setEnabled(val);
		codigosModel.getBufferedComponentModel("activo").setEnabled(val);
	}
	
	public void nuevo(){
		Descuento c=new Descuento();
		codigosModel.setBean(c);
		//beanChannell.setValue(c);
		eneableForm(true);
		ordenField.requestFocusInWindow();
	}
	public void commit(){
		codigosModel.triggerCommit();
		Descuento c=(Descuento)codigosModel.getBean();
		boolean ok=getProducto().agregarDescuento(c);
		if(ok)
			updateGrid();
		else
			nuevo();
	}
	
	public void reset(){
		codigosModel.resetChanged();
		eneableForm(false);
	}
	
	public void eliminar(){
		final EventList selection=selectionModel.getSelected();
		boolean update=false;
		for(int i=0;i<selection.size();i++){
			Descuento c=partidas.get(i);
			boolean res=getProducto().eliminarDescuento(c);
			if(!update)
				update=res;
		}
		if(update)
			updateGrid();
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

	private JButton buildResetButton() {
		JButton reset=new JButton("Des-Hacer");
		reset.addActionListener(EventHandler.create(ActionListener.class, this, "reset"));
		return reset;
	}

	private JButton buildEdditButton() {
		JButton edit=new JButton("Editar");
		return edit;
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
	
	private class CommitHandler implements PropertyChangeListener{

		public void propertyChange(PropertyChangeEvent evt) {
			Boolean res=(Boolean)evt.getNewValue();
			okButton.setEnabled(res);			
			if(!res){
				okButton.requestFocusInWindow();
				eneableForm(res);
			}
		}
		
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

}
