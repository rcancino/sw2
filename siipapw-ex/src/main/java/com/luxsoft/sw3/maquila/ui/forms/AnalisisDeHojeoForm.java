package com.luxsoft.sw3.maquila.ui.forms;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.Normalizer.Form;

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

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
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
import com.luxsoft.siipap.inventarios.model.CostoHojeable;
import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;



/**
 * Forma para la mantenimiento de gastos de hojeo
 * de maquila al Almacen de ventas
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class AnalisisDeHojeoForm extends AbstractForm implements ListSelectionListener{
	
	

	public AnalisisDeHojeoForm(final AnalisisDeHojeoFormModel model) {
		super(model);
		setTitle("Análisis de hojeo");
		if(model.getValue("id")==null){
			model.getModel("proveedor").addValueChangeListener(new PropertyChangeListener(){
				public void propertyChange(PropertyChangeEvent evt) {
					updateHeader();
				}
			});
		}
	}
	
	public AnalisisDeHojeoFormModel getController(){
		return (AnalisisDeHojeoFormModel)getModel();
	}	

	@Override
	protected JComponent buildFormPanel() {
		JPanel panel=new JPanel(new VerticalLayout(5));
		
		FormLayout layout=new FormLayout(
				"p,2dlu,80dlu,3dlu," +
				"p,2dlu,80dlu,3dlu," +
				"p,2dlu,80dlu:g"
				,"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		if(model.getValue("id")==null){
			builder.append("Proveedor",getControl("proveedor"),9);
		}
		else
			builder.append("Id",addReadOnly("id"),true);
		
		builder.append("Fecha",getControl("fecha"));
		builder.nextLine();
		builder.append("Factura",getControl("factura"));
		builder.append("F.Factura",getControl("fechaFactura"));
		builder.nextLine();
		
		builder.append("Importe",getControl("importe"));
		builder.append("Impuesto",getControl("impuesto"));
		builder.nextLine();
		builder.append("Total",getControl("total"));
		builder.nextLine();
		builder.append("Comentario",getControl("comentario"),9);
		
		panel.add(builder.getPanel());		
		panel.add(buildGridPanel());
		panel.add(buildToolbarPanel());
		ajustarActions(panel);
		//ComponentUtils.decorateSpecialFocusTraversal(panel);
		//ComponentUtils.decorateTabFocusTraversal(getControl("almacen"));
		final JFormattedTextField t1=(JFormattedTextField)getControl("importe");
		t1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				getController().actualizarTotalConImporte();
				getControl("comentario").requestFocusInWindow();
			}
		});
		
		final JFormattedTextField t2=(JFormattedTextField)getControl("total");
		t2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				getController().actualizarTotalConTotal();
				getControl("comentario").requestFocusInWindow();
			}
		});
		
		return panel;
	}
	
	private Header header;
	
	@Override
	protected JComponent buildHeader() {
		if(header==null){
			header=new Header("Seleccione un Proveedor","");
			updateHeader();
		}
		return header.getHeader();
	}
	
	private void updateHeader(){
		Proveedor p=(Proveedor)model.getValue("proveedor");
		if(p!=null){
			header.setTitulo(p.getNombreRazon());
			header.setDescripcion("Dirección: "+p.getDireccion().toString());
		}else{
			header.setTitulo("Seleccione un proveedor");
			header.setDescripcion("");
		}
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
		if(model.getValue("id")!=null)
			getControl("factura").requestFocusInWindow();
		else
			getControl("proveedor").requestFocusInWindow();
	}

	protected JComponent createNewComponent(final String property){
		JComponent c=super.createNewComponent(property);
		c.setEnabled(!model.isReadOnly());
		return c;
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("comentario".equals(property)){
			JComponent control=Binder.createMayusculasTextField(model.getModel(property), false);
			control.setEnabled(!model.isReadOnly());
			return control;			
		}else if("proveedor".equals(property)){
			return buildProveedorControl(model.getModel(property));
		}
		return null;
	}
	
	private JComponent buildProveedorControl(final ValueModel vm) {
		if (model.getValue("id") == null) {
			final JComboBox box = new JComboBox();
			final EventList source = GlazedLists.eventList(getController().getProveedores());
			final TextFilterator filterator = GlazedLists.textFilterator(new String[] { "clave", "nombre", "rfc" });
			AutoCompleteSupport support = AutoCompleteSupport.install(box,source, filterator);
			support.setFilterMode(TextMatcherEditor.CONTAINS);
			support.setStrict(false);
			final EventComboBoxModel model = (EventComboBoxModel) box.getModel();
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
		
		Action maquila=new com.luxsoft.siipap.swing.actions.DispatchingAction(this,"insertarMaquila");		
		maquila.putValue(Action.NAME, "Maquila");
		maquila.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_M);
		
		
		Action coms=new com.luxsoft.siipap.swing.actions.DispatchingAction(this,"insertarCom");		
		coms.putValue(Action.NAME, "EntradaCompras");
		coms.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_T);
		
		Action trs=new com.luxsoft.siipap.swing.actions.DispatchingAction(this,"insertarTrs");		
		trs.putValue(Action.NAME, "Transformaciones");
		trs.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_T);
		
		//getInsertAction().putValue(Action.NAME, "Insertar [INS]");
		//getInsertAction().putValue(Action.MNEMONIC_KEY, KeyEvent.VK_I);
		
		getDeleteAction().putValue(Action.NAME, "Eliminar [DEL]");
		getDeleteAction().putValue(Action.MNEMONIC_KEY, KeyEvent.VK_DELETE);
		getEditAction().putValue(Action.NAME, "Editar");
		
		getInsertAction().setEnabled(!model.isReadOnly());
		getDeleteAction().setEnabled(!model.isReadOnly());
		getEditAction().setEnabled(!model.isReadOnly());
		
		getViewAction().setEnabled(false);
		
		JButton buttons[]={
				new JButton(maquila)
				,new JButton(trs)
				,new JButton(coms)
				,new JButton(getDeleteAction())
				//,new JButton(getEditAction())
				
		};
		return ButtonBarFactory.buildGrowingBar(buttons);
	}
	
	
	
	private JXTable grid;
	
	private EventSelectionModel selectionModel;
	
	protected JComponent buildGridPanel(){
		String[] props={
				"sucursal.nombre"
				,"documento"
				,"remision"
				,"clave"
				,"descripcion"
				,"cantidad"
				,"kilosCalculados"
				,"costoFlete"
				,"costoCorte"
				,"costoMateria"
				,"costo"
				,"comentario"
				};
		String[] names={
				"Sucursal"
				,"Docto"
				,"Remisión"
				,"Producto"
				,"Descripción"
				,"Cantidad"
				,"Kilos"
				,"Flete"
				,"Hojeo"
				,"Costo M.P."
				,"Costo"
				,"Comentario"
				};
		 
		final TableFormat tf=GlazedLists.tableFormat(CostoHojeable.class,props,names);
		EventList<CostoHojeable> eventList=getController().getPartidasSource();
		final EventTableModel tm=new EventTableModel(				
				eventList
				,tf
				);
		grid=ComponentUtils.getStandardTable();
		grid.setModel(tm);
		selectionModel=new EventSelectionModel(eventList);
		selectionModel.addListSelectionListener(this);
		grid.setSelectionModel(selectionModel);
		ComponentUtils.decorateActions(grid);
		ComponentUtils.addInsertAction(grid,getInsertAction());
		ComponentUtils.addDeleteAction(grid, getDeleteAction());
		
		grid.setEnabled(!model.isReadOnly());
		JComponent gridComponent=ComponentUtils.createTablePanel(grid);		
		gridComponent.setPreferredSize(new Dimension(790,300));
		grid.packAll();
		return gridComponent;
		
	}
	
	public void insertarMaquila(){
		getController().insertarMaquila();
		TableUtilities.resizeColumnsToPreferredWidth(grid);
		grid.requestFocusInWindow();
		grid.packAll();
	}
	
	public void insertarTrs(){
		getController().insertarTrs();
		TableUtilities.resizeColumnsToPreferredWidth(grid);
		grid.requestFocusInWindow();
		grid.packAll();
	}
	
	public void insertarCom(){
		getController().insertarCom();
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
				AnalisisDeHojeoFormModel controller=new AnalisisDeHojeoFormModel();
				AnalisisDeHojeoForm form=new AnalisisDeHojeoForm(controller);
				form.open();
				if(!form.hasBeenCanceled()){
					System.out.println(ToStringBuilder.reflectionToString(controller.getBaseBean()));
					
				}
				System.exit(0);
			}

		});
	}

	
	

}
