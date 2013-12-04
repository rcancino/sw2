package com.luxsoft.siipap.pos.ui.forms;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.VerticalLayout;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventComboBoxModel;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.util.TableUtilities;
import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;

import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;




/**
 * Fomra para la generación y mantenimiento de recepciones de maquila
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class RecepcionDeMaquilaForm extends AbstractForm implements ListSelectionListener{
	
	

	public RecepcionDeMaquilaForm(final RecepcionDeMaquilaFormModel model) {
		super(model);
		setTitle("Recepción de Maquila      ");
		if(model.getModel("proveedor")!=null){
			model.getModel("proveedor").addValueChangeListener(new PropertyChangeListener(){
				public void propertyChange(PropertyChangeEvent evt) {
					updateHeader();
				}
			});
		}
	}
	
	public RecepcionDeMaquilaFormModel getController(){
		return (RecepcionDeMaquilaFormModel)getModel();
	}

	private Header header;
	
	@Override
	protected JComponent buildHeader() {
		if(header==null){
			header=new Header("Seleccione un Maquilador","");
			updateHeader();
		}
		return header.getHeader();
	}
	
	private void updateHeader(){
		if(model.getValue("proveedor")!=null){
			Proveedor p=getController().getRecepcion().getProveedor();
			header.setTitulo(p.getNombreRazon());
			header.setDescripcion(p.getDireccion().toString());
		}
	}

	@Override
	protected JComponent buildFormPanel() {
		JPanel panel=new JPanel(new VerticalLayout(5));
		
		FormLayout layout=new FormLayout(
				"p,2dlu,70dlu,3dlu," +
				"p,2dlu,p:g(.5),3dlu," +
				"p,2dlu,p:g(.5)"
				,"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		if(model.getValue("id")!=null)
			builder.append("Documento",addReadOnly("documento"));
		builder.append("Fecha",addReadOnly("fecha"));		
		builder.append("Maquilador",getControl("proveedor"));
		builder.nextLine();
		builder.append("Remisión",getControl("remision"));
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
		else{
			if(!model.isReadOnly())
				getControl("remision").requestFocusInWindow();
		}
	}

	protected JComponent createNewComponent(final String property){
		JComponent c=super.createNewComponent(property);
		c.setEnabled(!model.isReadOnly());
		return c;
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("proveedor".equals(property)){
			return buildProveedorControl(model.getModel(property));
		}
		return null;
	}
	
	private JComponent buildProveedorControl(final ValueModel vm) {
		if (model.getValue("id") == null) {
			final JComboBox box = new JComboBox();			
			EventList source =null;
			source=GlazedLists.eventList(getController().getProveedores());
			final TextFilterator filterator = GlazedLists					
				.textFilterator(new String[] { "nombre" });
			AutoCompleteSupport support = AutoCompleteSupport.install(box,
					source, filterator);
			support.setFilterMode(TextMatcherEditor.CONTAINS);
			support.setStrict(false);
			final EventComboBoxModel model = (EventComboBoxModel) box
					.getModel();
			model.addListDataListener(new Bindings.WeakListDataListener(vm));
			box.setSelectedItem(vm.getValue());
			return box;
		} else {
			String prov = ((Proveedor) vm.getValue()).getNombre();
			JLabel label = new JLabel(prov);
			return label;
		}
	}
	
	protected JPanel buildToolbarPanel(){
		getInsertAction().putValue(Action.NAME, "Insertar [INS]");
		getInsertAction().putValue(Action.MNEMONIC_KEY, KeyEvent.VK_I);
		getDeleteAction().putValue(Action.NAME, "Eliminar [DEL]");
		getDeleteAction().putValue(Action.MNEMONIC_KEY, KeyEvent.VK_DELETE);
		getEditAction().putValue(Action.NAME, "Editar");
		
		getInsertAction().setEnabled(!model.isReadOnly());
		getDeleteAction().setEnabled(!model.isReadOnly());
		getEditAction().setEnabled(!model.isReadOnly());
		
		getViewAction().setEnabled(false);
		
		JButton buttons[]={
				new JButton(getInsertAction())
				,new JButton(getDeleteAction())
				,new JButton(getEditAction())
				
		};
		return ButtonBarFactory.buildGrowingBar(buttons);
	}
	
	
	
	private JXTable grid;
	
	private EventSelectionModel<EntradaDeMaquila> selectionModel;
	
	protected JComponent buildGridPanel(){
		String[] propertyNames={
				"renglon"
				,"producto.clave"
				,"producto.descripcion"
				,"kilos"
				,"cantidad"
				,"comentario"
				};
		String[] columnLabels={
				"Rngl"
				,"Producto"
				,"Descripción"
				,"Kilos"
				,"Cantidad"
				,"Comentario"};
		final TableFormat tf=GlazedLists.tableFormat(EntradaDeMaquila.class,propertyNames, columnLabels);
		EventList sortedList=new SortedList(getController().getPartidasSource(),GlazedLists.beanPropertyComparator(EntradaDeMaquila.class,"renglon"));
		final EventTableModel tm=new EventTableModel(sortedList,tf);
		grid=ComponentUtils.getStandardTable();
		grid.setModel(tm);
		selectionModel=new EventSelectionModel<EntradaDeMaquila>(getController().getPartidasSource());
		selectionModel.addListSelectionListener(this);
		grid.setSelectionModel(selectionModel);
		ComponentUtils.decorateActions(grid);
		ComponentUtils.addInsertAction(grid,getInsertAction());
		ComponentUtils.addDeleteAction(grid, getDeleteAction());
		grid.addMouseListener(new MouseAdapter(){			
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2)
					view();
			}			
		});
		grid.setEnabled(!model.isReadOnly());
		JComponent gridComponent=ComponentUtils.createTablePanel(grid);		
		gridComponent.setPreferredSize(new Dimension(790,300));
		grid.packAll();
		return gridComponent;
		
	}
	
	public void insertPartida(){
		getController().insertar();
		TableUtilities.resizeColumnsToPreferredWidth(grid);
		grid.requestFocusInWindow();
		grid.packAll();
	}
	
	public void deletePartida(){
		if(!selectionModel.isSelectionEmpty()){
			for(int index=selectionModel.getMinSelectionIndex();index<=selectionModel.getMaxSelectionIndex();index++){
				getController().elminarPartida(index);
			}
		}
	}
	
	public void edit(){
		if(!selectionModel.isSelectionEmpty()){
			for(int index=selectionModel.getMinSelectionIndex();index<=selectionModel.getMaxSelectionIndex();index++){
				getController().editar(index);
			}
		}
	}
	
	public void view(){
		
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
				RecepcionDeMaquilaFormModel controller=new RecepcionDeMaquilaFormModel();
				RecepcionDeMaquilaForm form=new RecepcionDeMaquilaForm(controller);
				form.open();
				if(!form.hasBeenCanceled()){
					System.out.println(ToStringBuilder.reflectionToString(controller.getBaseBean()));
					
				}
				System.exit(0);
			}

		});
	}

	
	

}
