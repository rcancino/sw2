package com.luxsoft.siipap.swx.catalogos;

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

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.core.Comentario;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.swing.binding.Binder;


/**
 * 
 * @author Ruben Cancino
 *
 */
public class ProductoComentariosPanel extends JPanel{
	
	private final PresentationModel model;
	private final PresentationModel comentarioModel;
	private final ValueHolder beanChannell;
	
	private final EventList<Comentario> partidas;
	private EventSelectionModel selectionModel;
	
	private JTextField claveField;
	private JTextField comentarioField;
	
	public ProductoComentariosPanel(final PresentationModel model){
		this.model=model;
		partidas=GlazedLists.eventList(new BasicEventList<Comentario>());
		beanChannell=new ValueHolder(null,true);
		model.addPropertyChangeListener(PresentationModel.PROPERTYNAME_BEAN,new BeanHandler());
		comentarioModel =new PresentationModel(beanChannell);	
		comentarioModel.addPropertyChangeListener(PresentationModel.PROPERTYNAME_BUFFERING,new CommitHandler());
		eneableForm(false);
		init();
	}
	
	private Producto getProducto(){
		return (Producto)model.getBean();
	}
	
	private void updateGrid(){
		partidas.clear();
		partidas.addAll(getProducto().getComentarios());
	}
	
	private void init(){
		setLayout(new BorderLayout());
		add(buildFormPanel(),BorderLayout.NORTH);
		add(buildGridPanel(),BorderLayout.CENTER);
		add(ButtonBarFactory.buildRightAlignedBar(getButtons()),BorderLayout.SOUTH);
	}
	
	
	
	private JComponent buildFormPanel(){
		
		claveField=Binder.createMayusculasTextField(comentarioModel.getBufferedComponentModel("clave"));
		comentarioField=Binder.createMayusculasTextField(comentarioModel.getBufferedComponentModel("comentario"));
		
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
		final TableFormat<Comentario> tf=GlazedLists.tableFormat(Comentario.class, new String[]{"clave","comentario"},new String[]{"Clave","Comentario"});
		final EventTableModel<Comentario> tm=new EventTableModel<Comentario>(partidas,tf);
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
		comentarioModel.getBufferedComponentModel("clave").setEnabled(val);
		comentarioModel.getBufferedComponentModel("comentario").setEnabled(val);
	}
	
	public void nuevo(){
		Comentario c=new Comentario();
		comentarioModel.setBean(c);
		//beanChannell.setValue(c);
		eneableForm(true);
		claveField.requestFocusInWindow();
	}
	public void commit(){
		comentarioModel.triggerCommit();
		Comentario c=(Comentario)comentarioModel.getBean();
		boolean ok=getProducto().agregarComentario(c);
		if(ok)
			updateGrid();
		else
			nuevo();
	}
	
	public void reset(){
		comentarioModel.resetChanged();
		eneableForm(false);
	}
	
	public void eliminar(){
		final EventList selection=selectionModel.getSelected();
		boolean update=false;
		for(int i=0;i<selection.size();i++){
			Comentario c=partidas.get(i);
			boolean res=getProducto().eliminarComentario(c);
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
