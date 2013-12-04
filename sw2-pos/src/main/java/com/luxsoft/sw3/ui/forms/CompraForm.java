package com.luxsoft.sw3.ui.forms;

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
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jdesktop.swingx.VerticalLayout;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventComboBoxModel;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.util.TableUtilities;
import com.luxsoft.siipap.compras.model.CompraUnitaria;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;


/**
 * Forma para el mantenimiento de compras
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CompraForm extends AbstractForm implements ListSelectionListener{
	
	private boolean proveedorFijo=false;

	public CompraForm(final CompraController model) {
		super(model);
		setTitle("Orden de Compra         ("+model.getValue("sucursal")+" )");
		if(model.getModel("proveedor")!=null){
			model.getModel("proveedor").addValueChangeListener(new PropertyChangeListener(){
				public void propertyChange(PropertyChangeEvent evt) {
					updateHeader();
				}
			});
		}
	}
	
	public CompraController getController(){
		return (CompraController)getModel();
	}

	private Header header;
	
	@Override
	protected JComponent buildHeader() {
		if(header==null){
			header=new Header("Seleccione un proveedor","");
			updateHeader();
		}
		return header.getHeader();
	}
	
	private void updateHeader(){
		if(model.getValue("proveedor")!=null){
			Proveedor p=(Proveedor)model.getValue("proveedor");
			header.setTitulo(p.getNombreRazon());
			header.setDescripcion(p.getDireccion().toString());
		}
	}

	@Override
	protected JComponent buildFormPanel() {
		JPanel panel=new JPanel(new VerticalLayout(5));
		
		FormLayout layout=new FormLayout(
				"40dlu,2dlu,max(60dlu;p),3dlu," +
				"40dlu,2dlu,max(60dlu;p),3dlu," +
				"40dlu,2dlu,max(60dlu;p)"
				,"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		if(model.getValue("id")==null)
			builder.append("Proveedor",getControl("proveedor"),9);
		else{
			builder.append("Folio",addReadOnly("folio"));
		}
		builder.append("Fecha",addReadOnly("fecha"));		
		builder.append("Moneda",getControl("moneda"));
		builder.append("T.C.",addReadOnly("tc"));
		builder.nextLine();
		builder.append("Comentario",getControl("comentario"),9);
		
		panel.add(builder.getPanel());		
		panel.add(buildGridPanel());
		panel.add(buildToolbarPanel());
		ajustarActions(panel);
		ComponentUtils.decorateSpecialFocusTraversal(panel);
		ComponentUtils.decorateTabFocusTraversal(getControl("proveedor"));
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
/*
	protected JComponent createNewComponent(final String property){
		JComponent c=super.createNewComponent(property);
		c.setEnabled(!model.isReadOnly());
		return c;
	}*/
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("proveedor".equals(property)){
			proveedorControl=createProveedorLookup(model.getModel(property));
			proveedorControl.setEditable(!isProveedorFijo());
			return proveedorControl;
		}
		return null;
	}
	
	private JComboBox proveedorControl;
	
	
	protected  JComboBox createProveedorLookup(final ValueModel vm) {
		final JComboBox box = new JComboBox();		
		final TextFilterator filterator = GlazedLists.textFilterator(new String[] { "clave", "nombre", "rfc" });
		AutoCompleteSupport support = AutoCompleteSupport.install(box,getController().getProveedores(), filterator);
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
		if(getController().getCompra().getId()!=null)
			getDeleteAction().setEnabled(false);
		
		getViewAction().setEnabled(false);
		JButton buttons[]={
				new JButton(getInsertAction())
				,new JButton(getInsertarBulkAction())
				,new JButton(getDeleteAction())
				,new JButton(getEditAction())
				,new JButton(getImprimirAction())
				//,new JButton(getAutorizarAction())
		};
		return ButtonBarFactory.buildGrowingBar(buttons);
	}
	
	private Action imprimirAction;
	public Action getImprimirAction(){
		if(imprimirAction==null){
			imprimirAction=CommandUtils.createPrintAction(this, "imprimir");
			imprimirAction.putValue(Action.NAME, "Imprimir [F12]");
		}
		return imprimirAction;
	}
	
	private Action insertarBulkAction;
	public Action getInsertarBulkAction(){
		if(insertarBulkAction==null){
			insertarBulkAction=new com.luxsoft.siipap.swing.actions.DispatchingAction(this, "insertarBulk");
			insertarBulkAction.putValue(Action.NAME, "Agregar Batch");
			insertarBulkAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_B);
		}
		return insertarBulkAction;
	}
	
	
	private JTable grid;
	private EventSelectionModel<CompraUnitaria> selectionModel;
	
	protected JComponent buildGridPanel(){
		String[] propertyNames={
				"producto.clave"
				,"producto.descripcion"
				,"solicitado"
				,"comentario"
				,"ancho"
				,"largo"
				};
		String[] columnLabels={
				"Producto"
				,"Descripción"
				,"Solicitado"
				,"Comentario"
				,"Ancho"
				,"Largo"
				};
		boolean[] edits={
				false
				,false
				,true
				,false
				,true
				,true
		};
		
		final TableFormat tf=GlazedLists.tableFormat(CompraUnitaria.class,propertyNames, columnLabels,edits);
		SortedList sorted=new SortedList(getController().getPartidasSource(),null);
		final EventTableModel tm=new EventTableModel(sorted,tf);
		grid=new JTable(tm);
		selectionModel=new EventSelectionModel<CompraUnitaria>(sorted);
		selectionModel.addListSelectionListener(this);
		grid.setSelectionModel(selectionModel);
		ComponentUtils.decorateActions(grid);
		ComponentUtils.addInsertAction(grid,getInsertAction());
		ComponentUtils.addDeleteAction(grid, getDeleteAction());
		TableComparatorChooser.install(grid, sorted, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
		grid.addMouseListener(new MouseAdapter(){			
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2)
					view();
			}			
		});
		grid.setEnabled(!model.isReadOnly());
		JComponent gridComponent=ComponentUtils.createTablePanel(grid);
		
		gridComponent.setPreferredSize(new Dimension(750,300));
		return gridComponent;
		
	}
	
	public void insertarBulk(){
		getController().insertarBulk(grid);
		TableUtilities.resizeColumnsToPreferredWidth(grid);
		grid.requestFocusInWindow();
	}
	
	public void insertPartida(){
		getController().insertar();
		TableUtilities.resizeColumnsToPreferredWidth(grid);
		grid.requestFocusInWindow();
	}
	
	public void deletePartida(){
		if(getController().getCompra().getId()==null){
			if(!selectionModel.isSelectionEmpty()){
				for(int index=selectionModel.getMinSelectionIndex();index<=selectionModel.getMaxSelectionIndex();index++){
					getController().elminarPartida(index);
				}
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
	
	
	
	public boolean isProveedorFijo() {
		return proveedorFijo;
	}

	public void setProveedorFijo(boolean proveedorFijo) {
		this.proveedorFijo = proveedorFijo;
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				CompraController controller=new CompraController();
				CompraForm form=new CompraForm(controller);
				form.open();
				if(!form.hasBeenCanceled()){
					System.out.println(ToStringBuilder.reflectionToString(controller.getBaseBean()));
					
				}
				System.exit(0);
			}

		});
	}

	
	

}
