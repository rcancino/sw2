package com.luxsoft.siipap.compras.ui.form;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
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
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.util.TableUtilities;
import com.luxsoft.siipap.compras.model.CompraUnitaria;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.FormatUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;


/**
 * Fomra para la generación y mantenimiento de ordenes de compras centralizadas
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CompraCentralizadaForm extends AbstractForm implements ListSelectionListener{
	
	private boolean importacion=false;
	private boolean proveedorFijo=false;

	public CompraCentralizadaForm(final CompraCentralizadaFormModel model) {
		super(model);
		setTitle("Orden de Compra      ");
		if(model.getModel("proveedor")!=null){
			model.getModel("proveedor").addValueChangeListener(new PropertyChangeListener(){
				public void propertyChange(PropertyChangeEvent evt) {
					updateHeader();
				}
			});
		}
		
		if(!model.isReadOnly()){
			model.getModel("especial").addValueChangeListener(new PropertyChangeListener(){
				public void propertyChange(PropertyChangeEvent evt) {
					Boolean val=(Boolean)evt.getNewValue();
					if(val!=null){
						getModel().setValue("descuentoEspecial",0.0);
						getControl("descuentoEspecial").setEnabled(val);
					}
				}
			});
		}
	}
	
	public CompraCentralizadaFormModel getController(){
		return (CompraCentralizadaFormModel)getModel();
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
				"40dlu,2dlu,max(70dlu;p),3dlu," +
				"40dlu,2dlu,max(70dlu;p),3dlu," +
				"40dlu,2dlu,max(70dlu;p),3dlu," +
				"40dlu,2dlu,max(70dlu;p):g"
				,"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		
		
		builder.append("Proveedor",getControl("proveedor"),9);
		builder.append("Sucursal",getControl("sucursal"));
		builder.nextLine();
		
		if(model.getValue("id")!=null)
			builder.append("Folio",addReadOnly("folio"));
		builder.append("Fecha",addReadOnly("fecha"));		
		builder.append("F. Entrega",getControl("entrega"));
		builder.nextLine();
		
		builder.append("Moneda",getControl("moneda"));
		builder.append("T.C.",addReadOnly("tc"));
		builder.nextLine();
		builder.append("Especial",getControl("especial"));
		getControl("descuentoEspecial").setEnabled(false);
		builder.append("Descuento",getControl("descuentoEspecial"));
		builder.nextLine();
		
		builder.append("Comentario",getControl("comentario"),13);
		
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
		else{
			if(!model.isReadOnly())
				getControl("comentario").requestFocusInWindow();
		}
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
			JComponent c= buildProveedorControl(model.getModel(property));
			c.setEnabled(!isProveedorFijo());
			return c;
		}else if("sucursal".equals(property)){
			if(model.getValue("id")==null && (!isImportacion())){
				List data=getController().getSucursales();
				SelectionInList sl=new SelectionInList(data,model.getModel(property));
				JComboBox box=BasicComponentFactory.createComboBox(sl);
				box.setEnabled(!model.isReadOnly());
				box.setEnabled(model.getValue("id")==null);
				return box;
			}else{
				return BasicComponentFactory.createLabel(model.getModel(property),FormatUtils.getToStringFormat());
			}			
		}else if("descuentoEspecial".equals(property)){
			JFormattedTextField tf=Binder.createDescuentoBinding(model.getModel(property));
			tf.setEnabled(!model.isReadOnly());
			return tf;
		}
		return null;
	}
	
	private JComponent buildProveedorControl(final ValueModel vm) {
		if( (model.getValue("id") == null)) {
			final JComboBox box = new JComboBox();			
			EventList source =null;
			if(isImportacion()){
				source=GlazedLists.eventList(ServiceLocator2.getProveedorManager().buscarImportadores());
			}else
				source=GlazedLists.eventList(ServiceLocator2.getProveedorManager().buscarActivos());
			final TextFilterator filterator = GlazedLists					
				.textFilterator(new String[] { "clave", "nombre", "rfc" });
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
			String prov = ((Proveedor) vm.getValue()).getNombreRazon();
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
				,new JButton(getImprimirAction())
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
	
	
	
	public void aplicarDescuento(){
		
	}
	
	private JXTable grid;
	
	private EventSelectionModel<CompraUnitaria> selectionModel;
	
	protected JComponent buildGridPanel(){
		String[] propertyNames={
				"sucursal.nombre"
				,"producto.clave"
				,"descripcion"
				,"solicitado"
				,"aduana"
				,"precio"
				,"desc1"
				,"desc2"
				,"desc3"
				,"desc4"
				,"desc5"
				,"desc6"
				,"costo"
				,"importeNeto"
				,"comentario"};
		String[] columnLabels={
				"Sucursal"
				,"Producto"
				,"Descripción"
				,"Solicitado"
				,"Aduana"
				,"precio"
				,"D1"
				,"D2"
				,"D3"
				,"D4"
				,"D5"
				,"D6"
				,"Costo"
				,"Importe"
				,"Comentario"
				};
		boolean[] edits={
				false
				,false
				,false
				,true
				,false
				,false
				,false
				,false
				,false
				,false
				,false
				,false
				,false
				,false
				,false
				};
		final TableFormat tf=GlazedLists
			.tableFormat(CompraUnitaria.class,propertyNames, columnLabels,edits);
		SortedList sorted=new SortedList(getController().getPartidasSource(),null);
		final EventTableModel tm=new EventTableModel(sorted,tf);
		grid=ComponentUtils.getStandardTable();
		grid.setModel(tm);
		selectionModel=new EventSelectionModel<CompraUnitaria>(sorted);
		selectionModel.addListSelectionListener(this);
		grid.setSelectionModel(selectionModel);
		TableComparatorChooser.install(grid, sorted, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
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
			/*for(int index=selectionModel.getMinSelectionIndex();index<=selectionModel.getMaxSelectionIndex();index++){
				getController().elminarPartida(index);
			}
			*/
			CompraUnitaria det=selectionModel.getSelected().get(0);
			getController().elminarPartida(det);
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
	
	
	
	public boolean isImportacion() {
		return importacion;
	}

	public void setImportacion(boolean importacion) {
		this.importacion = importacion;
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
				CompraCentralizadaFormModel controller=new CompraCentralizadaFormModel();
				CompraCentralizadaForm form=new CompraCentralizadaForm(controller);
				form.open();
				if(!form.hasBeenCanceled()){
					System.out.println(ToStringBuilder.reflectionToString(controller.getBaseBean()));
					
				}
				System.exit(0);
			}

		});
	}

	
	

}
