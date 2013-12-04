package com.luxsoft.sw3.cxc.forms;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.ventas.model.ListaDePreciosCliente;
import com.luxsoft.siipap.ventas.model.ListaDePreciosClienteDet;





/**
 * Forma para la generación y mantenimiento de cortes de tarjeta
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ListaDePreciosPorClienteForm extends AbstractForm implements ListSelectionListener{
	

	public ListaDePreciosPorClienteForm(final ListaDePreciosPorClienteFormModel model) {
		super(model);
		setTitle("Lista de descuentos especiales por cliente     ");	
		model.getModel("cliente").addValueChangeListener(new PropertyChangeListener() {			
			public void propertyChange(PropertyChangeEvent evt) {
				updateHeader();				
			}
		});
	}
	
	public ListaDePreciosPorClienteFormModel getController(){
		return (ListaDePreciosPorClienteFormModel)getModel();
	}

	
	@Override
	protected JComponent buildFormPanel() {
		JPanel panel=new JPanel(new VerticalLayout(5));
		
		final FormLayout layout=new FormLayout(
				"p,3dlu,p:g(.5), 4dlu" +
				",p,3dlu,p:g(.5)"
				,"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		if(model.getValue("id")!=null){
			builder.append("Id",addReadOnly("id"));
			builder.nextLine();
		}
		else{
			builder.append("Cliente",getControl("cliente"),5);
			builder.nextLine();
		}
		
		//builder.append("Fecha Inicial",getControl("fechaInicial"));
		//builder.append("Fecha Final",getControl("fechaFinal"));
		//builder.nextLine();
		builder.append("Descuento",getControl("descuento"));
		builder.append("Activo",getControl("activo"));
		builder.nextLine();
		
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
	
	private Header header;
	
	@Override
	protected JComponent buildHeader() {
		if(header==null){
			header=new Header("Seleccione un cliente","Generación y mantenimiento de lista de precios ");
			header.setDescRows(5);
			updateHeader();
		}
		return header.getHeader();
	}
	
	private void updateHeader(){
		if(model.getValue("cliente")!=null){
			Cliente c=getController().getLista().getCliente();
			
			header.setTitulo(c.getNombreRazon()+ " ( "+c.getClave()+" )");
			try {
				String pattern = "" +
				"Línea de crédito:    {0}              Saldo:      {1}               Disponible: {2}" +
				"\nPlazo: 	          {3}              Atraso Max: {4}               " +
				"\nCheques devueltos: {5}              Jurídico:   {6}" +
				"\nDescuento fijo: {7} %";
				
				String msg = MessageFormat.format(pattern, 
				c.getCredito().getLinea().amount()
				,c.getCredito().getSaldoGeneral()
				,c.getCredito().getCreditoDisponible()
				,c.getCredito().getPlazo()
				,c.getCredito().getAtrasoMaximo()
				,c.getChequesDevueltos()
				,c.isJuridico()
				,c.getCredito().getDescuentoEstimado()
					);
				
				header.getHeader().setDescription(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			
		}else{
			header.setTitulo("Seleccione un cliente");
			header.setDescripcion("");
		}
	}
	
	@Override
	protected void onWindowOpened() {
		super.onWindowOpened();
		if(model.getValue("id")==null)
			getControl("cliente").requestFocusInWindow();
		
	}

	protected JComponent createNewComponent(final String property){
		JComponent c=super.createNewComponent(property);
		c.setEnabled(!model.isReadOnly());
		return c;
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("comentario".equals(property)){
			return Binder.createMayusculasTextField(model.getModel(property));
		}else if("cliente".equals(property)){
			return createClienteBox(model.getModel("cliente"));
			
		}else if("descuento".equals(property)){
			return Bindings.createDescuentoEstandarBinding(model.getModel(property));
		}
		return null;
	}
	
	protected JComboBox createClienteBox(final ValueModel vm){
		final JComboBox box=new JComboBox();
		final EventList source=GlazedLists.eventList(ServiceLocator2
				.getClienteManager()
				.buscarClientesCredito());
		final TextFilterator filterator=GlazedLists.textFilterator(new String[]{"clave","nombre","rfc"});
		AutoCompleteSupport support = AutoCompleteSupport.install(box, source, filterator);
        support.setFilterMode(TextMatcherEditor.CONTAINS);
        support.setCorrectsCase(true);
        final EventComboBoxModel model=(EventComboBoxModel)box.getModel();
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
		
		getInsertAction().setEnabled(!model.isReadOnly());
		getDeleteAction().setEnabled(!model.isReadOnly());
		getEditAction().setEnabled(!model.isReadOnly());
		
		Action precioPorKiloAction=new AbstractAction("P. x Kg"){
			public void actionPerformed(ActionEvent e) {
				aplicarFormula();
			}
		};
		precioPorKiloAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/application_cascade.png"));
		
		
		JButton buttons[]={
				new JButton(getInsertAction())
				,new JButton(getDeleteAction())
				//,new JButton(getEditAction())
				//,new JButton(precioPorKiloAction)
				
		};
		return ButtonBarFactory.buildGrowingBar(buttons);
	}
	
	
	
	private JXTable grid;
	
	private EventSelectionModel<ListaDePreciosClienteDet> selectionModel;
	
	protected JComponent buildGridPanel(){
		final String[] props={
				"producto.clave"
				,"producto.descripcion"
				,"producto.linea.nombre"
				,"producto.kilos"
				,"producto.precioBruto"
				//,"precioDeLista"
				//,"precioPorKiloNormal"
				,"descuento"
				//,"precioKilo"
				//,"precio"
				//,"precioNeto"
				//,"costo"
				//,"diferencia"
				//,"margen"
				};
		final String[] names={
				"Producto"
				,"Descripción"
				,"Línea"
				,"Kg"
				,"Bruto"
				//,"P. Lista(CRE)"
				//,"P.X Kg"
				,"Descto"
				//,"Nvo P X Kg"
				//,"Precio"
				//,"Neto"
				//,"Costo"
				//,"Diferencia"
				//,"Margen"
				};	
		
		final TableFormat tf=GlazedLists.tableFormat(ListaDePreciosClienteDet.class, props, names);
		
		final EventTableModel tm=new EventTableModel(getController().getPartidasSource(),tf);
		grid=ComponentUtils.getStandardTable();
		grid.setModel(tm);
		selectionModel=getController().getSelectionModel();//new EventSelectionModel<ListaDePreciosClienteDet>(getController().getPartidasSource());
		selectionModel.addListSelectionListener(this);
		grid.setSelectionModel(selectionModel);
		ComponentUtils.decorateActions(grid);
		ComponentUtils.addInsertAction(grid,getInsertAction());
		ComponentUtils.addDeleteAction(grid, getDeleteAction());
		
		grid.setEnabled(!model.isReadOnly());
		JComponent gridComponent=ComponentUtils.createTablePanel(grid);		
		gridComponent.setPreferredSize(new Dimension(790,300));
		TableComparatorChooser.install(grid, (SortedList)getController().getPartidasSource(), TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
		adjustGrid(grid);
		grid.packAll();
		
		return gridComponent;
		
	}
	
	protected void adjustGrid(JXTable grid){
		//grid.getColumnExt("Margen").setCellRenderer(Renderers.getPorcentageRenderer());
		grid.getColumnExt("Descto").setCellRenderer(Renderers.getPorcentageRenderer());
		//grid.getColumnExt("Línea").setVisible(false);
		grid.packAll();
	}
	
	
	public void insertPartida(){
		getController().insertar();
		//TableUtilities.resizeColumnsToPreferredWidth(grid);
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
		/*if(!selectionModel.isSelectionEmpty()){
			int index=selectionModel.getMinSelectionIndex();
			getController().editar(index);
		}*/
	}
	
	public void aplicarFormula(){
		getController().aplicarPrecioPorKilo(selectionModel);
	}
	
	public void valueChanged(ListSelectionEvent e) {
		boolean val=!selectionModel.isSelectionEmpty();
		if(model.isReadOnly()){
			val=false;
		}
		getInsertAction().setEnabled(val);
		getEditAction().setEnabled(val);
		getDeleteAction().setEnabled(val);
		getViewAction().setEnabled(!selectionModel.isSelectionEmpty());
	}
	
public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				ListaDePreciosPorClienteFormModel controller=new ListaDePreciosPorClienteFormModel();
				ListaDePreciosPorClienteForm form=new ListaDePreciosPorClienteForm(controller);
				form.open();
				if(!form.hasBeenCanceled()){
					ListaDePreciosCliente res=controller.commit();
					res=ServiceLocator2.getListaDePreciosClienteManager().save(res);
					System.out.println(res);
				}
				System.exit(0);
			}

		});
	}

	
	

}
