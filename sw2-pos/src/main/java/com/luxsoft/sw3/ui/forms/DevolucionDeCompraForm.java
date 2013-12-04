package com.luxsoft.sw3.ui.forms;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jdesktop.swingx.VerticalLayout;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventComboBoxModel;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.jgoodies.uifextras.util.TableUtilities;
import com.luxsoft.siipap.compras.model.DevolucionDeCompraDet;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.sw3.services.Services;


/**
 * Forma para el registro de devoluciones de compra
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class DevolucionDeCompraForm extends AbstractForm implements ListSelectionListener{
	
	

	public DevolucionDeCompraForm(final DevolucionDeCompraController model) {
		super(model);
		setTitle("Devolucion de Compra         ("+model.getValue("sucursal")+" )");
	}
	
	public DevolucionDeCompraController getController(){
		return (DevolucionDeCompraController)getModel();
	}
	@Override
	protected JComponent buildFormPanel() {
		JPanel panel=new JPanel(new VerticalLayout(5));
		
		FormLayout layout=new FormLayout(
				"40dlu,2dlu,max(60dlu;p),3dlu," +
				"40dlu,2dlu,max(60dlu;p),3dlu," +
				"40dlu,2dlu,max(60dlu;p):g"
				,"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		if(model.getValue("id")==null){
			builder.append("Proveedor",getControl("proveedor"),9);
			builder.nextLine();
		}
		else{
			builder.append("Documento",addReadOnly("documento"),true);
		}
		
		builder.append("Fecha",addReadOnly("fecha"));
		builder.append("Usuario",getControl("usuario"));
		builder.nextLine();
		builder.append("Referencia",getControl("referencia"));
		builder.append("Comentario",getControl("comentario"),5);
		
		panel.add(builder.getPanel());		
		panel.add(buildGridPanel());
		panel.add(buildToolbarPanel());
		ajustarActions(panel);
		return panel;
	}
	
	protected void ajustarActions(JPanel panel){
		getOKAction().putValue(Action.NAME, "Salvar [F10]");
		getOKAction().putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images/edit/save_edit.gif"));
		getCancelAction().putValue(Action.NAME, "Cancelar");
		ComponentUtils.addAction(panel, new AbstractAction(){			
			public void actionPerformed(ActionEvent e) {
				if(getOKAction().isEnabled())
					getOKAction().actionPerformed(null);
			}
		}, 
		KeyStroke.getKeyStroke("F10"), JComponent.WHEN_IN_FOCUSED_WINDOW);
		ComponentUtils.addInsertAction(panel, getInsertAction());
	}
	
	
	
	@Override
	protected void onWindowOpened() {
		super.onWindowOpened();
		if(model.getValue("id")==null)
			getControl("proveedor").requestFocusInWindow();
	}

	protected JComponent createNewComponent(final String property){
		JComponent c=super.createNewComponent(property);
		c.setEnabled(!model.isReadOnly());
		return c;
	}
	
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if ("proveedor".equals(property)) {
			return createProveedorLookup(model.getModel(property));
		} else if("usuario".equals(property)){
			JTextField tf=BasicComponentFactory.createTextField(model.getModel(property));
			return tf;
		}
		return null;
	}
	
	protected  JComboBox createProveedorLookup(final ValueModel vm) {
		final JComboBox box = new JComboBox();		
		final TextFilterator filterator = GlazedLists.textFilterator(new String[] { "clave", "nombre", "rfc" });
		final EventList data=GlazedLists.eventList(Services.getInstance().getProveedorManager().getAll());
		AutoCompleteSupport support = AutoCompleteSupport.install(box,data, filterator);
		support.setFilterMode(TextMatcherEditor.CONTAINS);
		support.setStrict(false);
		final EventComboBoxModel model = (EventComboBoxModel) box.getModel();
		model.addListDataListener(new Bindings.WeakListDataListener(vm));
		box.setSelectedItem(vm.getValue());
		
		return box;
		
	}
	
	
	protected JPanel buildToolbarPanel(){
		getInsertAction().putValue(Action.NAME, "Insertar [INS]");
		getInsertAction().putValue(Action.MNEMONIC_KEY, KeyEvent.VK_I);
		getDeleteAction().putValue(Action.NAME, "Eliminar [DEL]");
		getDeleteAction().putValue(Action.MNEMONIC_KEY, KeyEvent.VK_DELETE);
		getEditAction().putValue(Action.NAME, "Editar");		
		
		getViewAction().setEnabled(false);
		JButton buttons[]={
				new JButton(getInsertAction())
				,new JButton(getDeleteAction())
		};
		return ButtonBarFactory.buildGrowingBar(buttons);
	}
	
	
	
	private JTable grid;
	private EventSelectionModel<DevolucionDeCompraDet> selectionModel;
	
	protected JComponent buildGridPanel(){
		String[] propertyNames={"renglon","clave","descripcion","cantidad","comentario"};
		String[] columnLabels={"Rngl","Producto","Descripción","Salida","Comentario"};
		boolean[] edits={false,false,false,true,true};
		final TableFormat tf=GlazedLists.tableFormat(DevolucionDeCompraDet.class,propertyNames, columnLabels,edits);
		final EventTableModel tm=new EventTableModel(getController().getPartidasSource(),tf);
		grid=new JTable(tm);
		selectionModel=new EventSelectionModel<DevolucionDeCompraDet>(getController().getPartidasSource());
		selectionModel.addListSelectionListener(this);
		grid.setSelectionModel(selectionModel);
		ComponentUtils.decorateActions(grid);
		ComponentUtils.addInsertAction(grid,getInsertAction());
		ComponentUtils.addDeleteAction(grid, getDeleteAction());
		
		grid.setEnabled(!model.isReadOnly());
		JComponent gridComponent=ComponentUtils.createTablePanel(grid);
		gridComponent.setPreferredSize(new Dimension(750,300));
		return gridComponent;
	}
	
	public void insertPartida(){
		getController().insertarPartida();
		TableUtilities.resizeColumnsToPreferredWidth(grid);
		grid.requestFocusInWindow();
	}
	
	public void deletePartida(){
		if(!selectionModel.isSelectionEmpty()){
			for(int index=selectionModel.getMinSelectionIndex();index<=selectionModel.getMaxSelectionIndex();index++){
				getController().elminarPartida(index);
			}
		}
	}
	
	
	public void valueChanged(ListSelectionEvent e) {
		boolean val=!selectionModel.isSelectionEmpty();
		if(model.isReadOnly()){
			val=false;
		}
		getEditAction().setEnabled(val);
		getDeleteAction().setEnabled(val);
		getViewAction().setEnabled(!selectionModel.isSelectionEmpty());
		
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				DevolucionDeCompraController controller=new DevolucionDeCompraController();
				DevolucionDeCompraForm form=new DevolucionDeCompraForm(controller);
				form.open();
				if(!form.hasBeenCanceled()){
					System.out.println(ToStringBuilder.reflectionToString(controller.getBaseBean()));
					
				}
				System.exit(0);
			}

		});
	}

	
	

}
