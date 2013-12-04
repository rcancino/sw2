package com.luxsoft.sw3.cxp.forms;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;

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

import org.apache.log4j.Logger;
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
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.util.TableUtilities;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.model.gastos.GProductoServicio;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.FormatUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.utils.LoggerHelper;




/**
 * Forma para el mantenimiento de instancias de {@link GProductoServicio}
 * 
 * @author Ruben Cancino
 * 
 *
 */
public class RequisicionDeComprasForm2 extends AbstractForm implements ListSelectionListener{
	
	protected Logger logger=LoggerHelper.getLogger();

	public RequisicionDeComprasForm2(RequisicionDeComprasFormModel2 model) {
		super(model);
		setTitle("Requisición");	
		
		model.addPropertyChangeListener("total",new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				logger.debug("Req Form: Nuevo total: "+getModel().getValue("total"));
				JLabel l=(JLabel)getControl("total");
				l.setText(getModel().getValue("total").toString());
				logger.debug("Total en label: "+l.getText());
			}
		});	
		model.getModel("proveedor").addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				updateHeader();
			}
		});
		model.getModel("descuentoFinanciero").addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				System.out.println("Cambio df: "+evt.getNewValue());
			}
		});
	}
	
	public RequisicionDeComprasFormModel2 getController(){
		return (RequisicionDeComprasFormModel2)getModel();
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
	
	protected JComponent buildMainPanel(){
		final FormLayout layout=new FormLayout("p:g","p");
		final PanelBuilder builder=new PanelBuilder(layout);
		final CellConstraints cc=new CellConstraints();
		builder.add(buildFormPanel(),cc.xy(1, 1));
		//if(!model.isReadOnly())
			//builder.add(buildValidationPanel(),cc.xy(1,3));
		model.validate();
		updateComponentTreeMandatoryAndSeverity(builder.getPanel());
		return builder.getPanel();
	}
	
	@Override
	protected JComponent buildFormPanel() {
		
		JPanel panel=new JPanel(new VerticalLayout(5));
		FormLayout layout=new FormLayout(
				"p,2dlu,max(90dlu;p),3dlu," +
				"p,2dlu,max(90dlu;p),3dlu," +
				"p,2dlu,max(90dlu;p),3dlu," +
				"p,2dlu,max(90dlu;p):g"
				,"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		if(model.getValue("id")!=null){
			JComponent ct=getControl("id");
			ct.setEnabled(false);
			builder.append("Id",ct,true);
			getControl("moneda").setEnabled(false);
		}		
		builder.append("Proveedor",getControl("proveedor"),9);
		builder.append("Concepto",getControl("concepto"),true);		
		
		builder.append("Fecha",getControl("fecha"));
		builder.append("Fecha P.",getControl("fechaDePago"));
		builder.append("Moneda",getControl("moneda"));		
		builder.append("TC",getControl("tipoDeCambio"),true);
		
		builder.append("F. Pago",getControl("formaDePago"));		
		builder.append("Descuento F.",getControl("descuentoFinanciero"));
		builder.nextLine();
		
		builder.append("Comentario",getControl("comentario"),9);
		
		panel.add(builder.getPanel());		
		panel.add(buildGridPanel());
		panel.add(buildToolbarPanel());
		panel.add(buildTotalesPanel());
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
	protected JComponent createCustomComponent(String property) {
		if("proveedor".equals(property)){
			return buildProveedorControl(model.getModel(property));
		}else if("concepto".equals(property)){			
			SelectionInList sl=new SelectionInList(getController().getConceptosValidos(),model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setEnabled(!model.isReadOnly());			
			return box;
		}else  if("formaDePago".equals(property)){
			JComboBox box=Bindings.createFormasDePagoBinding(model.getComponentModel(property));
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("comentario".equals(property)){
			JComponent control=Binder.createMayusculasTextField(model.getModel(property), false);
			control.setEnabled(!model.isReadOnly());
			return control;			
		}else if("descuentoFinanciero".equals(property)){
			JComponent control=Bindings.createDescuentoEstandarBinding(model.getModel(property));
			control.setEnabled(!model.isReadOnly());
			return control;			
		}else if("total".equals(property)){
			JComponent c=BasicComponentFactory.createLabel(model.getModel(property), FormatUtils.getToStringMonedaFormat());
			return c;
		}
		return null;
	}
	
	protected JPanel buildToolbarPanel(){
		getInsertAction().putValue(Action.NAME, "Facturas");
		getInsertAction().putValue(Action.MNEMONIC_KEY, KeyEvent.VK_I);
		getDeleteAction().putValue(Action.NAME, "Eliminar [DEL]");
		getDeleteAction().putValue(Action.MNEMONIC_KEY, KeyEvent.VK_DELETE);
		getEditAction().putValue(Action.NAME, "Editar");
		
		getInsertAction().setEnabled(!model.isReadOnly());
		getDeleteAction().setEnabled(!model.isReadOnly());
		getEditAction().setEnabled(!model.isReadOnly());
		
		getViewAction().setEnabled(false);
		
		Action insertarPorContrarecibo=new DispatchingAction(this,"insertarPorContrarecibo");
		insertarPorContrarecibo.putValue(Action.NAME, "Contra recibos");
		insertarPorContrarecibo.putValue(Action.SHORT_DESCRIPTION, "Buscar por contra recibo");
		insertarPorContrarecibo.putValue(Action.SMALL_ICON,ResourcesUtils.getIconFromResource("images2/application_view_list.png"));
		
		JButton buttons[]={
				new JButton(getInsertAction())
				,new JButton(insertarPorContrarecibo)
				,new JButton(getDeleteAction())
				,new JButton(getEditAction())
				,new JButton(getViewAction())
				
		};
		return ButtonBarFactory.buildGrowingBar(buttons);
	}
	
	
	private JComponent buildProveedorControl(final ValueModel vm) {
		if (model.getValue("proveedor") == null) {
			final JComboBox box = new JComboBox();
			final EventList source = GlazedLists.eventList(ServiceLocator2
					.getProveedorManager().getAll());
			final TextFilterator filterator = GlazedLists
					.textFilterator(new String[] { "clave", "nombre"});
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
	
	/****  Configuracion de el detalle ****/
	private JXTable grid;
	
	private EventSelectionModel<RequisicionDe> selectionModel;
	
	protected JComponent buildGridPanel(){
		final String[] props={
				"id"
				,"documento"
				,"fechaDocumento"
				,"facturaDeCompras.vencimiento"
				,"facturaDeCompras.total"
				,"facturaDeCompras.totalAnalizadoConFlete"				
				,"facturaDeCompras.totalCargos"
				,"facturaDeCompras.bonificadoCM"	
				,"facturaDeCompras.devolucionesCM"
				,"facturaDeCompras.importeDescuentoFinanciero"
				,"facturaDeCompras.saldoReal"
				,"facturaDeCompras.vencimientoDF"
				,"total"};
		final String[] names={
				"Id"
				,"Factura"
				,"Fecha F"
				,"Vto F"
				,"Total (Fac)"
				,"Analizado"				
				,"Cargos "
				,"Bonificaciones "
				,"Devoluciones "
				,"Desc F"
				,"Saldo"
				,"Vto DF"
				,"Por requisitar"
				};
		TableFormat tf= GlazedLists.tableFormat(RequisicionDe.class, props, names);
		EventList eventList=getController().getPartidas();
		if(model.getValue("id")!=null){
			SortedList sorted=new SortedList(eventList,null);
			eventList=sorted;
		}
		
		
		final EventTableModel tm=new EventTableModel(eventList,tf);
		grid=ComponentUtils.getStandardTable();
		grid.setModel(tm);
		selectionModel=new EventSelectionModel<RequisicionDe>(eventList);
		selectionModel.addListSelectionListener(this);
		grid.setSelectionModel(selectionModel);
		if(model.getValue("id")!=null){
			SortedList sorted=(SortedList)eventList;
			TableComparatorChooser.install(grid, sorted, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
		}
		
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
		gridComponent.setPreferredSize(new Dimension(810,300));
		grid.packAll();
		return gridComponent;
		
	}
	
	/**
	 * Consstruye el panel de Importe, impuesto y total. Asume que el modelo puede
	 * proporcionar {@link ValueModel} para estas propiedades
	 * Tambien coloca aqui el panel de validación si la fomra no es de solo lectura
	 * 
	 * @return
	 */
	private JComponent buildTotalesPanel(){
		
		final FormLayout layout=new FormLayout(
				"p:g,5dlu,p,2dlu,max(p;50dlu)"
				,"p,2dlu,p,2dlu,p:g");		
		//final FormDebugPanel debugPanel=new FormDebugPanel(layout);		
		final PanelBuilder builder=new PanelBuilder(layout);		
		final CellConstraints cc=new CellConstraints();
		
		if(!model.isReadOnly())	{
			JComponent valPanel=buildValidationPanel();
			valPanel.setPreferredSize(new Dimension(200,70));
			builder.add(valPanel,cc.xywh(1, 1,1,5));
		}
		
		builder.addLabel("Total:",cc.xy(3, 5));		
		builder.add(getControl("total"),cc.xy(5, 5));		
		return builder.getPanel();
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
	
	public void valueChanged(ListSelectionEvent e) {
		boolean val=!selectionModel.isSelectionEmpty();
		if(model.isReadOnly()){
			val=false;
		}
		getEditAction().setEnabled(val);
		getDeleteAction().setEnabled(val);
		getViewAction().setEnabled(!selectionModel.isSelectionEmpty());
		
	}
	
	/****---------------------- Acciones ------------------------------------***/
	
	public void insertPartida(){		
		getController().insertar();
		updateGrid();
	}

	public void insertarPorContrarecibo(){
		getController().insertarPorContrarecibo();
		updateGrid();
	}
	
	public void deletePartida(){
		if(!selectionModel.isSelectionEmpty()){
			RequisicionDe source=(RequisicionDe)selectionModel.getSelected().get(0);
			getController().delete(source);
		}
		updateGrid();
	}
	
	public void edit(){
		if(!selectionModel.isSelectionEmpty()){
			RequisicionDe source=(RequisicionDe)selectionModel.getSelected().get(0);
			getController().editar(source);
		}
		updateGrid();
	}
	
	public void view(){
		if(!selectionModel.isSelectionEmpty()){
			RequisicionDe det=(RequisicionDe)selectionModel.getSelected().get(0);
			getController().view(det);
		}
	}
	
	private void updateGrid(){
		TableUtilities.resizeColumnsToPreferredWidth(grid);
		grid.requestFocusInWindow();
		grid.packAll();
	}
	
	public static Requisicion showForm(){
		return showForm(new Requisicion());
	}
	
	public static Requisicion showForm(Requisicion bean){
		return showForm(bean,false);
	}
	
	public static Requisicion showForm(Requisicion bean,boolean readOnly){
		RequisicionDeComprasFormModel2 model=new RequisicionDeComprasFormModel2(bean);
		model.setReadOnly(readOnly);
		final RequisicionDeComprasForm2 form=new RequisicionDeComprasForm2(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.getRequisicion();
		}
		return null;
	}
	
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable(){
			public void run() {
				SWExtUIManager.setup();
				//Requisicion target=ServiceLocator2.getRequisiciionesManager().buscarRequisicionDeCompras(71L);
				//Requisicion  bean=showForm(target);
				Requisicion  bean=showForm();
				if(bean!=null){
					RequisicionDeComprasForm2.showObject(bean);
				}
				System.exit(0);
			}
		});
	}

}
