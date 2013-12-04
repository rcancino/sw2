package com.luxsoft.sw3.ui.forms;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.text.MessageFormat;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.VerticalLayout;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.inventarios.model.Transformacion;
import com.luxsoft.siipap.inventarios.model.TransformacionDet;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;

/**
 * Forma para el mantenimiento de transformaciones
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class TransformacionesForm extends AbstractForm{

	private JXTable grid;
	private EventSelectionModel selectionModel;
	private EventList partidas;
	
	public TransformacionesForm(TransformacionesController controller) {
		super(controller);
		selectionModel=controller.getSelectionModel();
		partidas=controller.getPartidas();
		String pattern="Transformación de material      [{0}]";
		setTitle(MessageFormat.format(pattern, controller.getValue("sucursal").toString()));
	}
	
	private TransformacionesController getController(){
		return (TransformacionesController)model;
	}

	@Override
	protected JComponent buildFormPanel() {
		JPanel content=new JPanel(new VerticalLayout(5));
		content.add(buildForm());
		//content.add(new JSeparator());
		content.add(buildGridPanel());
		content.add(buildButtonPanel());
		return content;
	}
	
	private Component buildForm() {
		FormLayout layout=new FormLayout(
				"p,2dlu,p, 3dlu" +
				",p,2dlu,70dlu ,3dlu" +
				",p,2dlu,p:g"
				,"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Fecha",addReadOnly("fecha"));
		builder.append("Clase",getControl("clase"));
		builder.nextLine();
		builder.append("Por inventario",getControl("porInventario"));
		builder.append("Comentario",getControl("comentario"),5);
		return builder.getPanel();
	}

	@Override
	protected JComponent createCustomComponent(String property) {
		if("clase".equals(property)){
			Object[] data=Transformacion.Clase.values();
			SelectionInList sl=new SelectionInList(data,model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			if(model.getValue("id")!=null)
				box.setEnabled(false);
			return box;
		}else if("documento".equals(property)){
			return BasicComponentFactory.createLongField(model.getModel(property));
		}
		return super.createCustomComponent(property);
	}

	private Component buildGridPanel() {
		JPanel panel=new JPanel(new BorderLayout());
		grid=ComponentUtils.getStandardTable();
		String[] props={"clave","descripcion","existencia","cantidad","comentario"};
		String[] names={"Producto","Descripcion","Exis","Cantidad"};
		TableFormat tf=GlazedLists.tableFormat(TransformacionDet.class, props,names);
		EventTableModel tm=new EventTableModel(partidas,tf);
		grid.setModel(tm);
		grid.setSelectionModel(selectionModel);
		JComponent c=ComponentUtils.createTablePanel(grid);
		c.setPreferredSize(new Dimension(700,400));
		panel.add(c,BorderLayout.CENTER);
		return panel;
	}
	
	private JPanel buildButtonPanel(){
		JButton insertButton=new JButton("Insert");
		insertButton.setMnemonic('I');
		insertButton.addActionListener(EventHandler.create(ActionListener.class, this, "insert"));
		return ButtonBarFactory.buildRightAlignedBar(
				insertButton
				);
	}
	
	public void insert(){
		getController().insert();
		grid.packAll();
	}


	public static Transformacion showForm(){
		TransformacionesController controller=new TransformacionesController();
		TransformacionesForm form=new TransformacionesForm(controller);
		form.open();
		if(!form.hasBeenCanceled()){
			Transformacion res= controller.persis();
			if(res!=null){
				MessageUtils.showMessage("Transformación generada: "+res.getDocumento(), "Transformaciones");
			}
			return res;
		}
		return null;
	}
	
	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				showObject(showForm());
				System.exit(0);
			}

		});
	}

}
