package com.luxsoft.sw3.cxp.forms;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.NumberFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
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

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.cxp.model.AnalisisDeFacturaDet;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.ComponentUtils;


/**
 * Generación y mantenimiento de cuentas por pagar (Facturas)
 * 
 * @author Ruben Cancino
 * 
 */
public class FacturaDeComprasForm extends AbstractForm implements ListSelectionListener{
	

	public FacturaDeComprasForm(FacturaDeComprasFormModel model) {
		super(model);		
		setTitle("Análisis y revisión de facturas");
		getController().getEditarFactura().addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				edicionDeFactura((Boolean)evt.getNewValue());
			}
		});
		if(!model.isReadOnly()){
			getController().getModel("proveedor").addValueChangeListener(new PropertyChangeListener() {				
				public void propertyChange(PropertyChangeEvent evt) {
					Proveedor p=(Proveedor)evt.getNewValue();
					if(p!=null){
						getControl("flete").setEnabled(p.getCobraFlete());
					}else
						getControl("flete").setEnabled(false);
				}
			});
		}
		
		
	}
	
	public FacturaDeComprasFormModel getController(){
		return (FacturaDeComprasFormModel)model;
	}
	
	//private JTextArea header;
	/*
	@Override
	protected JComponent buildHeader() {
		header=new JTextArea();
		header.setEditable(false);
		updateHeader();
		return header;
	}
	*/
	void updateHeader(){
		if(getController().getFactura().getProveedor()!=null){
			setTitle(getController().getFactura().getNombre());
			//header.setDescription(getController().getFactura().getProveedor().getDireccion().formattedAddress());
		}else{
			setTitle("Seleccione un proveedor");
			//header.setDescription("");
		}
	}
	
	@Override
	protected JComponent buildFormPanel() {
		JPanel panel=new JPanel(new VerticalLayout(1));
		panel.add(buildMasterForm());
		panel.add(buildToolbarPanel());
		panel.add(buildGridPanel());
		return panel;
	}
	
	
	protected JComponent buildMasterForm() {
		FormLayout layout = new FormLayout(
				"  60dlu,2dlu,70dlu, 3dlu,"
				+ "60dlu,2dlu,70dlu, 3dlu," 
				+ "60dlu,2dlu,70dlu"
				, "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Factura ");
		if (getModel().getValue("id") != null) {
			builder.append("Id", addReadOnly("id"));
			builder.append("Proveedor", addReadOnly("proveedor"),5);
		} else {
			builder.append("Proveedor", getControl("proveedor"), 9);
		}
		builder.append("Factura", getControl("documento"));
		builder.append("Fecha", getControl("fecha"));
		builder.append("Vencimiento", addReadOnly("vencimiento"));

		builder.append("Desc Financiero", addReadOnly("descuentoFinanciero"));
		builder.append("Vto D.F", addReadOnly("vencimientoDF"));
		builder.nextLine();

		builder.append("T.C.", getControl("tc"));
		builder.append("Moneda", getControl("moneda"),true);
		builder.append("Comentario", getControl("comentario"), 9);
		
		//builder.nextLine();
		/*
		String a=getController().getFactura().getAnalizado().doubleValue()>0
			?"Analisis ( Total de análisis anteriores: "
					+getController().getFactura().getAnalizado()+")     "
			:"Análisis";*/
		builder.appendSeparator("Análisis");
		builder.append("Importe  (A)",addReadOnly("analizado"));
		builder.append("Impuesto (A)",addReadOnly("analizadoImpuesto"));
		builder.append("Total    (A)",addReadOnly("analizadoTotal"));
		builder.nextLine();
		builder.append("Comentario",getControl("comentarioAnalisis"),5);
		builder.nextLine();
		builder.append("Importe",addReadOnly("importeAnalizado"));
		builder.append("Impuesto",addReadOnly("impuestoAnalizado"));
		builder.append("Total",addReadOnly("totalAnalizado"));
		builder.append("Analisis acumulado",addReadOnly("analizadoAcumulado"));
		builder.append("Pendiente ",addReadOnly("pendienteDeAnalizar"));
		ComponentUtils.decorateSpecialFocusTraversal(builder.getPanel());
		
		FormLayout layout2=new FormLayout(
				"p,2dlu,60dlu, 2dlu ,p,2dlu,20dlu"
				,"");
		DefaultFormBuilder builder2=new DefaultFormBuilder(layout2);
		builder.setDefaultDialogBorder();
		builder2.appendSeparator("Totales");
		builder2.append("Importe",getControl("importe"),true);
		builder2.append("Cargos",getControl("cargos"),true);
		builder2.append("Impuesto",addReadOnly("impuesto"),true);	
		getControl("flete").setEnabled(false);
		builder2.append("Flete",getControl("flete"),true);
		builder2.append("Imp Flete",addReadOnly("impuestoflete"),true);
		builder2.append("Ret.Imp Flete",addReadOnly("retencionflete"));
		builder2.append("%",addReadOnly("retencionfletePor"),true);
		builder2.append("Total",addReadOnly("total"),true);
		
		FormLayout layout3=new FormLayout("p,2dlu,p","p");
		PanelBuilder builder3=new PanelBuilder(layout3);
		CellConstraints cc=new CellConstraints();
		builder3.add(builder.getPanel(),cc.xy(1, 1));
		builder3.add(builder2.getPanel(),cc.xy(3, 1));
		
		if(getController().getFactura().getProveedor()!=null){
			getControl("flete").setEnabled(getController().getFactura().getProveedor().getCobraFlete());
		}
		return builder3.getPanel();
	}	
	
	private void edicionDeFactura(boolean val){
		getControl("importe").setEnabled(val);
		getControl("cargos").setEnabled(val);
		if(getController().getFactura().getProveedor()!=null){
			getControl("flete").setEnabled(getController().getFactura().getProveedor().getCobraFlete());
		}
		//getControl("flete").setEnabled(val);
		getControl("tc").setEnabled(val);
		getControl("moneda").setEnabled(val);
		getControl("proveedor").setEnabled(val);
		getControl("documento").setEnabled(val);
		getControl("fecha").setEnabled(val);
		getControl("vencimiento").setEnabled(val);
	}
	
	protected JComponent buildToolbarPanel(){
		getInsertAction().putValue(Action.NAME, "Insertar [INS]");
		getInsertAction().putValue(Action.MNEMONIC_KEY, KeyEvent.VK_I);
		getDeleteAction().putValue(Action.NAME, "Eliminar [DEL]");
		getDeleteAction().putValue(Action.MNEMONIC_KEY, KeyEvent.VK_DELETE);
		getEditAction().putValue(Action.NAME, "Editar");
		
		getInsertAction().setEnabled(!model.isReadOnly());
		getDeleteAction().setEnabled(!model.isReadOnly());
		getEditAction().setEnabled(!model.isReadOnly());
		
		getViewAction().setEnabled(false);
		
		/*JButton buttons[]={
				new JButton(getInsertAction())
				,new JButton(getDeleteAction())
				,new JButton(getEditAction())
				
		};
		return ButtonBarFactory.buildGrowingBar(buttons);
		*/
		final ToolBarBuilder builder=new ToolBarBuilder();
		builder.add(getInsertAction());
		builder.add(getDeleteAction());
		builder.add(getEditAction());
		return builder.getToolBar();
	}
	
	protected JXTable grid;
	protected EventSelectionModel<AnalisisDeFacturaDet> selectionModel;
	SortedList sorted;
	protected JComponent buildGridPanel(){
		String[] props = {"entrada.documento",
				"entrada.compra", "entrada.fechaCompra", "entrada.remision","entrada.fechaRemision"
				,"entrada.clave","entrada.descripcion","entrada.unidad.unidad","entrada.renglon","cantidad"
				,"precio", "costo", "importe" 
				};
		String[] labels = {"COM",
				"Compra", "F.Compra", "Remisión","F.Remisión"
				,"Producto","Descripción","U","Rgl","Cant(Análisis)"
				,"Precio", "Costo", "Importe"
				};
		final TableFormat<AnalisisDeFacturaDet> tf=GlazedLists.tableFormat(AnalisisDeFacturaDet.class, props, labels);
		sorted=new SortedList(getController().getAnalisisDeEntradas(),null);
		final EventTableModel tm=new EventTableModel(sorted,tf);
		grid=ComponentUtils.getStandardTable();
		grid.setModel(tm);
		selectionModel=new EventSelectionModel<AnalisisDeFacturaDet>(sorted);
		selectionModel.addListSelectionListener(this);
		grid.setSelectionModel(selectionModel);
		TableComparatorChooser.install(grid, sorted, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
		ComponentUtils.decorateActions(grid);
		ComponentUtils.addInsertAction(grid,getInsertAction());
		ComponentUtils.addDeleteAction(grid, getDeleteAction());
		grid.setEnabled(!model.isReadOnly());
		JComponent gridComponent=ComponentUtils.createTablePanel(grid);		
		gridComponent.setPreferredSize(new Dimension(790,300));
		grid.packAll();
		return gridComponent;
		
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if(property.startsWith("comentario")){
			JComponent control=Binder.createMayusculasTextField(model.getModel(property), false);
			control.setEnabled(!model.isReadOnly());
			return control;			
		}else if("proveedor".equals(property)){
			return buildProveedorControl(model.getModel(property));
		}else if(property.startsWith("descuento")){
			JComponent c=Bindings.createDescuentoEstandarBinding(model.getModel(property));
			c.setEnabled(!model.isReadOnly());
			return c;
		}else if(property.endsWith("Analizado")){
			JFormattedTextField tf=BasicComponentFactory.createFormattedTextField(model.getModel(property), NumberFormat.getCurrencyInstance());
			tf.setHorizontalAlignment(JFormattedTextField.RIGHT);
			return tf;
		}else if("retencionfletePor".equals(property)){
			JComponent c=Binder.createDescuentoBinding(model.getModel(property));
			c.setEnabled(!model.isReadOnly());
			return c;
		}else if("tc".equals(property)){
			JComponent c=Bindings.createDoubleBinding(model.getModel("tc"),6, 2);
			c.setEnabled(!model.isReadOnly());
			return c;
		}else if("documento".equalsIgnoreCase(property)){
				JComponent c=BasicComponentFactory.createTextField(
							model.getModel(property), true);
					c.setEnabled(!getController().isReadOnly());				
				return c;
		}else if(model.getPropertyType(property)==BigDecimal.class){
			JComponent c=Binder.createBigDecimalForMonyBinding(model.getModel(property), false);
			c.setEnabled(!getController().isReadOnly());				
			return c;
		}else 
			return super.createCustomComponent(property);
	}

	private JComponent buildProveedorControl(final ValueModel vm) {
		if (model.getValue("proveedor") == null) {
			final JComboBox box = new JComboBox();
			final EventList source = GlazedLists.eventList(ServiceLocator2.getProveedorManager().getAll());
			final TextFilterator filterator = GlazedLists.textFilterator(new String[] { "clave", "nombre", "rfc" });
			AutoCompleteSupport support = AutoCompleteSupport.install(box,source, filterator);
			support.setFilterMode(TextMatcherEditor.CONTAINS);
			support.setStrict(false);
			support.setCorrectsCase(true);
			//support.setTextMatchingStrategy(TextMatcherEditor.)
			final EventComboBoxModel<Proveedor> model = (EventComboBoxModel) box.getModel();
			/*
			ComponentUtils.addEnterAction(box,new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					
				}
			});*/
			
			model.addListDataListener(new ListDataListener() {
				public void intervalRemoved(ListDataEvent e) {
				}				
				public void intervalAdded(ListDataEvent e) {
				}				
				public void contentsChanged(ListDataEvent e) {
					if(e.getSource()!=null){
						EventComboBoxModel<Proveedor> cm=(EventComboBoxModel<Proveedor>)e.getSource();
						if(cm.getSelectedItem()!=null )
							if(cm.getSelectedItem() instanceof Proveedor){
								vm.setValue(cm.getSelectedItem());
								//System.out.println("Seleccion : "+cm.getSelectedItem()+ " Clase: "+cm.getSelectedItem().getClass());
								updateHeader();
							}
					}
				}
			});
			//model.addListDataListener(new Bindings.WeakListDataListener(vm));
			
			box.setSelectedItem(vm.getValue());
			
			return box;
		} else {
			String prov = ((Proveedor) vm.getValue()).getNombreRazon();
			JLabel label = new JLabel(prov);
			return label;
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
	
	public void insertPartida(){
		getController().insertarEntradas();
		this.grid.packAll();
	}
	public void deletePartida(){
		if(!selectionModel.isSelectionEmpty()){
			int index=selectionModel.getMinSelectionIndex();
			getController().eliminarAnalisis(index);
		}
	}
	public void edit(){
		if(!selectionModel.isSelectionEmpty()){
			//int index=selectionModel.getMinSelectionIndex();
			//int index=sorted.in
			AnalisisDeFacturaDet selected=selectionModel.getSelected().get(0);
			AnalisisDeFacturaDet res=getController().editar(selected);
			if(res!=null){
				int index=sorted.indexOf(selected);
				if(index!=-1){
					sorted.set(index, res);
				}
			}
		}
	}
}
