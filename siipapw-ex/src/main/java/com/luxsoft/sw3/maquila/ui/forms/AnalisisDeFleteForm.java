package com.luxsoft.sw3.maquila.ui.forms;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
//import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.maquila.model.MovimientoConFlete;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.swing.actions.DispatchingAction;



/**
 * Forma para la mantenimiento de gastos de flete 
 * de maquila al Almacen de ventas
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class AnalisisDeFleteForm extends AbstractForm implements ListSelectionListener{
	
	public static JCheckBox sinRetencion=new  JCheckBox();

	public AnalisisDeFleteForm(final AnalisisDeFleteFormModel model) {
		super(model);
		setTitle("Análisis de flete");
		if(model.getValue("id")==null){
			model.getModel("proveedor").addValueChangeListener(new PropertyChangeListener(){
				public void propertyChange(PropertyChangeEvent evt) {
					updateHeader();
				}
			});
		}
	}
	
	public AnalisisDeFleteFormModel getController(){
		return (AnalisisDeFleteFormModel)getModel();
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
		JFormattedTextField tf=(JFormattedTextField)getControl("importe");
		tf.addPropertyChangeListener("value", new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				getControl("comentario").requestFocus();
			}
		});
		
		builder.append("Impuesto",getControl("impuesto"));
		builder.nextLine();
		builder.append("Retencion",addReadOnly("retencion"));
		builder.append("Total",getControl("total"));
		builder.append("Sin Retencion",sinRetencion);
		builder.nextLine();
		builder.append("Comentario",getControl("comentario"),9);
		
		panel.add(builder.getPanel());		
		panel.add(buildGridPanel());
		panel.add(buildToolbarPanel());
		ajustarActions(panel);
		//ComponentUtils.decorateSpecialFocusTraversal(panel);
		//ComponentUtils.decorateTabFocusTraversal(getControl("almacen"));
		/*final JFormattedTextField t1=(JFormattedTextField)getControl("importe");
		t1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				//getController().actualizarTotalConImporte();
				getControl("comentario").requestFocusInWindow();
			}
		});*/
		
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
		
		Action com=new com.luxsoft.siipap.swing.actions.DispatchingAction(this,"insertarCom");		
		com.putValue(Action.NAME, "Compras");
		com.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
		
		Action trs=new com.luxsoft.siipap.swing.actions.DispatchingAction(this,"insertarTrs");		
		trs.putValue(Action.NAME, "Transformaciones");
		trs.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_T);
		
		Action tpe=new com.luxsoft.siipap.swing.actions.DispatchingAction(this,"insertarTraslado");		
		tpe.putValue(Action.NAME, "Traslados");
		tpe.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_E);
		
		getDeleteAction().putValue(Action.NAME, "Eliminar [DEL]");
		getDeleteAction().putValue(Action.MNEMONIC_KEY, KeyEvent.VK_DELETE);
		getEditAction().putValue(Action.NAME, "Editar");
		
		getInsertAction().setEnabled(!model.isReadOnly());
		getDeleteAction().setEnabled(!model.isReadOnly());
		getEditAction().setEnabled(!model.isReadOnly());
		
		getViewAction().setEnabled(false);
		
		JButton buttons[]={
				new JButton(maquila)
				,new JButton(com)
				,new JButton(trs)
				,new JButton(tpe)
				,new JButton(getDeleteAction())
				
				
		};
		return ButtonBarFactory.buildGrowingBar(buttons);
	}
	
	
	
	private JXTable grid;
	
	private EventSelectionModel<MovimientoConFlete> selectionModel;
	
	protected JComponent buildGridPanel(){
		String[] props={
				"sucursal.nombre"
				,"tipoDocto"
				,"documento"
				,"fecha"
				,"remision"
				,"producto.clave"
				,"producto.descripcion"
				,"cantidad"
				,"kilosCalculados"
				,"costoFlete"
				,"importeDelFlete"
				//,"costoCorte"
				//,"costoMateria"
				//,"costo"
				//,"comentario"
				};
		String[] names={
				"Sucursal"
				,"Tipo"
				,"Docto"
				,"Fecha"
				,"Remisión"
				,"Producto"
				,"Descripción"
				,"Cantidad"
				,"Kilos"
				,"Flete p/u"
				,"Importe"
				//,"Hojeo"
				//,"Costo M.P."
				//,"Costo"
				//,"Comentario"
				};
		 
		final TableFormat tf=GlazedLists.tableFormat(MovimientoConFlete.class,props,names);
		final EventTableModel tm=new EventTableModel(getController().getPartidasSource(),tf);
		grid=ComponentUtils.getStandardTable();
		grid.setModel(tm);
		selectionModel=new EventSelectionModel<MovimientoConFlete>(getController().getPartidasSource());
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
	
	public void insertarCom(){
		getController().insertarCom();
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
	
	public void insertarTraslado(){
		getController().insertarTpe();
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
				AnalisisDeFleteFormModel controller=new AnalisisDeFleteFormModel();
				AnalisisDeFleteForm form=new AnalisisDeFleteForm(controller);
				form.open();
				if(!form.hasBeenCanceled()){
					System.out.println(ToStringBuilder.reflectionToString(controller.getBaseBean()));
					
				}
				System.exit(0);
			}

		});
	}

		

}
