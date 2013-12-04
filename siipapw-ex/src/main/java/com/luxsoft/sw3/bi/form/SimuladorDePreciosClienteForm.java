package com.luxsoft.sw3.bi.form;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.hibernate.validator.InvalidValue;
import org.jdesktop.swingx.JXTable;
import org.springframework.beans.BeanUtils;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.matchers.ThreadedMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventComboBoxModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uif.component.UIFButton;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.cxc.ui.form.ProductoFinder;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.controls.UpperCaseField;
import com.luxsoft.siipap.swing.form2.AbstractMasterDetailForm;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.util.ValidationUtils;
import com.luxsoft.sw3.bi.SimuladorDePreciosPorCliente;
import com.luxsoft.sw3.bi.SimuladorDePreciosPorClienteDet;


public class SimuladorDePreciosClienteForm extends AbstractMasterDetailForm{
	
	private JComponent box;
	
	public SimuladorDePreciosClienteForm(SimuladorDePreciosClienteFormModel model) {
		super(model);
		setTitle("Simulador  de Precios");		
		model.getModel("tipoCosto").addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				System.out.println("Actualizando tipo de costo");
				getClienteModel().actualizar();
			}
		});
		model.getModel("tipoPrecio").addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				System.out.println("Actualizando tipo de precio");
				getClienteModel().actualizar();
			}
		});
		
		model.getModel("descuento").addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				System.out.println("Actualizando tipo de precio");
				getClienteModel().actualizar();
			}
		});
		
	}
	

	
	public SimuladorDePreciosClienteFormModel  getClienteModel(){
		return(SimuladorDePreciosClienteFormModel)getMainModel();
	}
	
	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Simulador de Precios","");
	}
	
	protected int getToolbarType(){
		return JToolBar.HORIZONTAL;
	}


	@Override
	protected JComponent buildMasterForm() {
		final DefaultFormBuilder builder=getDefaultMasterFormBuilder();
		if(model.getValue("id")!=null){
			JComponent ct=getControl("id");
			ct.setEnabled(false);
			builder.append("Id",ct,true);
			getControl("cliente").setEnabled(false);
		}
		builder.append("Cliente",getControl("cliente"),9);
		builder.nextLine();		
		builder.append("Fecha Inicial",getControl("fechaInicial"));
		builder.append("Fecha Final",getControl("fechaFinal"));
		builder.nextLine();
		builder.append("Descuento fijo",addReadOnly("descuentoFijo"));
		builder.append("Descuento",getControl("descuento"));
		builder.nextLine();
		builder.append("Tipo Precio",getControl("tipoPrecio"));
		builder.append("Tipo Costo",getControl("tipoCosto"));
		builder.nextLine();
		builder.append("Comentario",getControl("comentario"),9);		
		return builder.getPanel();
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		
		if("cliente".equals(property)){
			box=createClienteBox(model.getModel("cliente"));
			return box;
		}else if("descuento".equals(property)){
			JComponent c=Binder.createDescuentoBinding(model.getModel(property));
			return c;
		}else if("tipoPrecio".equals(property)){
			SelectionInList sl=new SelectionInList(SimuladorDePreciosPorCliente.TipoPrecio.values(),model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			return box;
		}else if("tipoCosto".equals(property)){
			SelectionInList sl=new SelectionInList(SimuladorDePreciosPorCliente.TipoCosto.values(),model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			return box;
		
		}else
			return null;
	}
	
	protected JComboBox createClienteBox(final ValueModel vm){
		final JComboBox box=new JComboBox();
		final EventList source=GlazedLists.eventList(ServiceLocator2.getClienteManager().getAll());
		final TextFilterator filterator=GlazedLists.textFilterator(new String[]{"clave","nombre","rfc"});
		AutoCompleteSupport support = AutoCompleteSupport.install(box, source, filterator);
        support.setFilterMode(TextMatcherEditor.CONTAINS);
        final EventComboBoxModel model=(EventComboBoxModel)box.getModel();
        model.addListDataListener(new Bindings.WeakListDataListener(vm));
        box.setSelectedItem(vm.getValue());
		return box;
	}


	@Override
	protected TableFormat getTableFormat() {
		final String[] props={
				"producto.linea.nombre"
				,"producto.clave"
				,"producto.descripcion"
				,"precioDeLista"
				,"descuento"
				,"precioNeto"
				,"costo"
				,"margenCalculado"
				,"precioMinimo"
				,"cantidadAcumulada"
				,"ventaPeriodoAnterior"
				};
		final String[] names={
				"Linea"
				,"Clave"
				,"Producto"
				,"P.Lista"
				,"Desc"
				,"Precio"
				,"Costo"
				,"Margen"
				,"P.Minimo"
				,"Cantidad"
				,"Periodo Ant"
				};		
		return GlazedLists.tableFormat(SimuladorDePreciosPorClienteDet.class, props, names);
		
	}
	
	protected void adjustGrid(JXTable grid){
		grid.setColumnControlVisible(true);
		grid.getColumnExt("Margen").setCellRenderer(Renderers.getPorcentageRenderer());
		grid.getColumnExt("Desc").setVisible(false);
		//grid.getColumnExt("Clave").setVisible(false);
	}
	
	
    protected JComponent buildButtonBarWithOKCancel() {
        JPanel bar = ButtonBarFactory.buildOKCancelApplyBar(
        	createActualizarButton()
        	,createOKButton(true),
            createCancelButton()
            );
        bar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
        return bar;
    }
    
    protected UIFButton createActualizarButton() {
    	Action actualizar=new com.luxsoft.siipap.swing.actions.DispatchingAction("Actualizar",getClienteModel(), "actualizarPartidas");
        UIFButton button = new UIFButton(actualizar);
        button.setVerifyInputWhenFocusTarget(false);
        return button;
    }
	
	public void insertPartida(){
		SimuladorDePreciosPorClienteDet det=new SimuladorDePreciosPorClienteDet();		
		det=SimuladorDePreciosClienteDetForm.showForm(det);
		if(det!=null){
			InvalidValue[] vals=ValidationUtils.validate(det);
			for(InvalidValue iv:vals){
				JOptionPane.showMessageDialog(this, iv.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
				
			}
			if(vals.length>0)
				return;
			if(det!=null){
				getMainModel().insertDetalle(det);			
			}
		}
		
	}
	
	protected void doEdit(Object obj){
		SimuladorDePreciosPorClienteDet source=(SimuladorDePreciosPorClienteDet)obj;
		SimuladorDePreciosPorClienteDet target=new SimuladorDePreciosPorClienteDet();
		BeanUtils.copyProperties(source, target);
		target=SimuladorDePreciosClienteDetForm.showForm(target);
		if(target!=null){
			BeanUtils.copyProperties(target, source);
		}
	}
	
	private JTextField lineaField;
	
	protected ToolBarBuilder getDetallesToolbarBuilder(){
		final JToolBar bar=new JToolBar(getToolbarType());
		final ToolBarBuilder builder=new ToolBarBuilder(bar);
		for(Action a:getDetallesActions()){
			builder.add(a);
		}
		enableEditingActions(!model.isReadOnly());
		enableSelectionActions();
		builder.addSeparator();
		builder.add(new JLabel("Línea"));
		builder.addSeparator();
		
		builder.add(lineaField);
		return builder;
	}
	
	protected EventList getFilterList(final EventList source){
		lineaField=new UpperCaseField();
		final TextFilterator filterator=GlazedLists.textFilterator("producto.linea.nombre");
		TextComponentMatcherEditor lineaEditor=new TextComponentMatcherEditor(lineaField, filterator);
		
		final EventList editors=new BasicEventList();
		editors.add(lineaEditor);
		final CompositeMatcherEditor editor=new CompositeMatcherEditor(editors);
		
		final FilterList filterList=new FilterList(source,new ThreadedMatcherEditor(editor));
		return filterList;
	}
	
	@Override
	protected Action[] getDetallesActions() {
		Action bulkUpdate=new AbstractAction("Bulk"){
			public void actionPerformed(ActionEvent e) {
				bulkEdit();
			}
		};
		bulkUpdate.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/application_cascade.png"));
		
		Action bulkInsert=new AbstractAction("BulkInsert"){
			public void actionPerformed(ActionEvent e) {
				bulkInsert();
			}
		};
		bulkInsert.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/text_indent_remove.png"));
		
		Action cargar=new com.luxsoft.siipap.swing.actions.DispatchingAction(getClienteModel(), "cargar");
		cargar.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/lightning_go.png"));
		return new Action[]{
				cargar,
				getInsertAction(),getDeleteAction(),getEditAction(),getViewAction()
				,bulkInsert,bulkUpdate
				,CommandUtils.createPrintAction(this, "imprimir")
				};
	}
	
	public void bulkEdit(){
		if(selection.isSelectionEmpty()) return;
		SimuladorDePreciosPorClienteDet dummy=(SimuladorDePreciosPorClienteDet)selection.getSelected().get(0);
		SimuladorDePreciosPorClienteDet template=new SimuladorDePreciosPorClienteDet();
		template.setProducto(dummy.getProducto());
		template.setPrecio(dummy.getPrecio());
		template=SimuladorDePreciosDetBulkForm.showForm(template);
		if(template!=null){
			List<SimuladorDePreciosPorClienteDet> selected=new ArrayList<SimuladorDePreciosPorClienteDet>();
			selected.addAll(selection.getSelected());
			for(SimuladorDePreciosPorClienteDet det:selected){
				det.setDescuento(template.getDescuento());
			}
		}
		grid.packAll();
	}
	
	public void bulkInsert(){
		List<Producto> list=ProductoFinder.findWithDialog();
		for(Producto p:list){
			SimuladorDePreciosPorClienteDet det=new SimuladorDePreciosPorClienteDet();
			det.setProducto(p);
			getMainModel().insertDetalle(det);
		}
	}
	
	protected void doView(Object obj){
		SimuladorDePreciosPorClienteDet det=(SimuladorDePreciosPorClienteDet)obj;		
		SimuladorDePreciosClienteDetForm.showForm(det);
	}
	
	protected void doPartidaUpdated(ListEvent listChanges){
		
	}
	
	public void imprimir(){
		
		
		final Map parameters=new HashMap();
		if(getClienteModel().getLista().getCliente()!=null){			
			parameters.put("CLIENTE", getClienteModel().getLista().getCliente().getNombre());
			parameters.put("DESCUENTO", getClienteModel().getLista().getDescuento());
			parameters.put("FECHA_INI", getClienteModel().getLista().getFechaInicial());
			parameters.put("FECHA_FIN", getClienteModel().getLista().getFechaFinal());
			parameters.put("TIPO_PRECIO", getClienteModel().getLista().getTipoPrecio().name());
			parameters.put("TIPO_COSTO", getClienteModel().getLista().getTipoCosto().name());
			parameters.put("COMENTARIO", getClienteModel().getLista().getComentario());
			parameters.put("LINEA", lineaField.getText());
			parameters.put("DESCTO", getClienteModel().getLista().getDescuento());
			ReportUtils.viewReport(ReportUtils.toReportesPath("bi/SimuladorDePrecios.jasper"), parameters,grid.getModel());
			//ReportUtils.viewWindowReport(ReportUtils.toReportesPath("bi/SimuladorDePrecios.jasper"), parameters,grid.getModel());
		}
		
	}
	
	public static SimuladorDePreciosPorCliente showForm(){
		return showForm(new SimuladorDePreciosPorCliente());
	}
	
	public static SimuladorDePreciosPorCliente showForm(SimuladorDePreciosPorCliente bean){
		return showForm(bean,false);
	}
	
	public static SimuladorDePreciosPorCliente showForm(SimuladorDePreciosPorCliente bean,boolean readOnly){
		SimuladorDePreciosClienteFormModel model=new SimuladorDePreciosClienteFormModel(bean);
		model.setReadOnly(readOnly);
		final SimuladorDePreciosClienteForm form=new SimuladorDePreciosClienteForm(model);
		form.enableEditingActions(readOnly);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.getLista();
		}
		return null;
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable(){

			public void run() {
				SWExtUIManager.setup();
				SimuladorDePreciosPorCliente lista=new SimuladorDePreciosPorCliente();
				
				SimuladorDePreciosPorCliente bean=showForm(lista);
				if(bean!=null){
					SimuladorDePreciosClienteForm.showObject(bean);
				}
				System.exit(0);
				
			}
			
		});

	}	

}
