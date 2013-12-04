package com.luxsoft.siipap.compras.ui;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.hibernate.validator.InvalidValue;
import org.jdesktop.swingx.JXTable;
import org.springframework.beans.BeanUtils;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.compras.model.ListaDePrecios;
import com.luxsoft.siipap.compras.model.ListaDePreciosDet;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.gastos.GProductoServicio;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractMasterDetailForm;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.swx.binding.ProveedorControl;
import com.luxsoft.siipap.swx.catalogos.ProductoFinder;
import com.luxsoft.siipap.util.ValidationUtils;




/**
 * Forma para el mantenimiento de instancias de {@link GProductoServicio}
 * 
 * @author Ruben Cancino
 *
 */
public class ListaDePreciosForm extends AbstractMasterDetailForm{
	
	protected Logger logger=Logger.getLogger(getClass());

	public ListaDePreciosForm(ListaDePreciosFormModel model) {
		super(model);
		setTitle("Lista de Precios");	
		
	}
	
	public ListaDePreciosFormModel getCompraModel(){
		return (ListaDePreciosFormModel)getMainModel();
	}

	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Lista de Precios","Generación y mantenimiento de lista de precios ");
	}
	
	protected int getToolbarType(){
		return JToolBar.HORIZONTAL;
	}
	
	/***** Binding support ******/	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("proveedor".equals(property)){
			ProveedorControl control=new ProveedorControl(model.getModel(property));
			control.setEnabled(!model.isReadOnly());
			return control;
		}else if("descuentoFinanciero".equals(property)||"cargo1".equals(property)){
			return Bindings.createDescuentoEstandarBinding(model.getModel(property));
		}
		return null;
	}

	protected JComponent buildMasterForm(){		
		final DefaultFormBuilder builder=getDefaultMasterFormBuilder();
		if(model.getValue("id")!=null){
			JComponent ct=getControl("id");
			ct.setEnabled(false);
			builder.append("Id",ct,true);
			getControl("proveedor").setEnabled(false);
		}		
		builder.append("Proveedor",getControl("proveedor"),9);
		builder.nextLine();		
		builder.append("Fecha Inicial",getControl("fechaInicial"));
		builder.append("Fecha Final",getControl("fechaFinal"),true);
		builder.append("Descripción",getControl("descripcion"),9);		
		builder.append("D.Financiero",getControl("descuentoFinanciero"));
		builder.append("Cargo (Flete)",getControl("cargo1"),true);
		builder.append("Vigente",getControl("vigente"));
		return builder.getPanel();
	}
	
	protected TableFormat getTableFormat(){
		final String[] props={"producto.clave","producto.descripcion","precio","descuento1","descuento2","descuento3","descuento4","costo","CostoConCargo","costoUltimo"};
		final String[] names={"Producto","Descripción","Precio","Desc1","Desc2","Desc3","Desc4","Costo","C.Cargo","Costo U"};		
		return GlazedLists.tableFormat(ListaDePreciosDet.class, props, names);
	}
	
	protected void adjustGrid(JXTable grid){
		grid.getColumnExt(3).setCellRenderer(Renderers.getPorcentageRenderer());
		grid.getColumnExt(4).setCellRenderer(Renderers.getPorcentageRenderer());
		grid.getColumnExt(5).setCellRenderer(Renderers.getPorcentageRenderer());
		grid.getColumnExt(6).setCellRenderer(Renderers.getPorcentageRenderer());
		grid.packAll();
	}
	
	public void insertPartida(){
		ListaDePreciosDet det=new ListaDePreciosDet();		
		det=ListaDePreciosDetForm.showForm(det);
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
		ListaDePreciosDet source=(ListaDePreciosDet)obj;
		ListaDePreciosDet target=new ListaDePreciosDet();
		BeanUtils.copyProperties(source, target);
		target=ListaDePreciosDetForm.showForm(target);
		if(target!=null){
			BeanUtils.copyProperties(target, source);
		}
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
		
		return new Action[]{
				getInsertAction(),getDeleteAction(),getEditAction(),getViewAction()
				,bulkInsert,bulkUpdate};
	}

	public void bulkEdit(){
		if(selection.isSelectionEmpty()) return;
		ListaDePreciosDet dummy=(ListaDePreciosDet)selection.getSelected().get(0);
		ListaDePreciosDet template=new ListaDePreciosDet();
		template.setProducto(dummy.getProducto());
		template.setPrecio(dummy.getPrecio());
		template=ListaDePreciosDetBulkForm.showForm(template);
		if(template!=null){
			List<ListaDePreciosDet> selected=new ArrayList<ListaDePreciosDet>();
			selected.addAll(selection.getSelected());
			for(ListaDePreciosDet det:selected){
				det.setDescuento1(template.getDescuento1());
				det.setDescuento2(template.getDescuento2());
				det.setDescuento3(template.getDescuento3());
				det.setDescuento4(template.getDescuento4());
			}
		}
	}
	
	public void bulkInsert(){
		List<Producto> list=ProductoFinder.findWithDialog();
		for(Producto p:list){
			ListaDePreciosDet det=new ListaDePreciosDet();
			det.setProducto(p);
			getMainModel().insertDetalle(det);
		}
	}
	
	protected void doView(Object obj){
		ListaDePreciosDet det=(ListaDePreciosDet)obj;		
		ListaDePreciosDetForm.showForm(det);
	}
	
	protected void doPartidaUpdated(ListEvent listChanges){
		
	}
	
	public static ListaDePrecios showForm(){
		return showForm(new ListaDePrecios());
	}
	
	public static ListaDePrecios showForm(ListaDePrecios bean){
		return showForm(bean,false);
	}
	
	public static ListaDePrecios showForm(ListaDePrecios bean,boolean readOnly){
		ListaDePreciosFormModel model=new ListaDePreciosFormModel(bean);
		model.setReadOnly(readOnly);
		final ListaDePreciosForm form=new ListaDePreciosForm(model);
		form.enableEditingActions(readOnly);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.getLista();
		}
		return null;
	}	
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		ListaDePrecios lista=new ListaDePrecios();
		
		ListaDePrecios bean=showForm(lista);
		if(bean!=null){
			ListaDePreciosForm.showObject(bean);
		}
		System.exit(0);
	}	

}
