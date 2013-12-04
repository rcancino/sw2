package com.luxsoft.siipap.inventario;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.table.TableColumnModel;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.inventarios.model.Movimiento;
import com.luxsoft.siipap.inventarios.model.MovimientoDet;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractMasterDetailForm;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;

public class MovimientoForm extends AbstractMasterDetailForm{

	public MovimientoForm(MovimientoFormModel model) {
		super(model);
			}
	
	public MovimientoFormModel getMainModel(){
		return (MovimientoFormModel)model;
	}
	
	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"p,10dlu,f:p:g"
				,"p,5dlu,p,5dlu,p,5dlu,35dlu,5dlu");
		final PanelBuilder builder=new PanelBuilder(layout);
		final CellConstraints cc=new CellConstraints();
		builder.add(buildMasterForm(),cc.xyw(1, 1,3));
		builder.add(buildDetailPanel(),cc.xyw(1, 5, 3));
		if(!model.isReadOnly()){
			builder.add(buildValidationPanel(),cc.xyw(1, 7,3));
		}
		model.validate();
		updateComponentTreeMandatoryAndSeverity(builder.getPanel());
		return builder.getPanel();
	}

	@Override
	protected JComponent buildMasterForm() {
		
		FormLayout layout=new FormLayout(
				" 40dlu,2dlu,80dlu,15dlu,p,3dlu,60dlu,3dlu," +
				"p,3dlu,p,3dlu,p,3dlu,p,3dlu,p,3dlu,p" +
				",p,2dlu,l:p:g"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		if(model.getValue("id")!=null){
			builder.append("Id",getControl("id"),true);
		}
		builder.append("Sucursal",getControl("sucursal"));
		builder.append("Fecha",getControl("fecha"));
		builder.nextColumn();
		builder.nextColumn();
		builder.nextColumn();
		builder.nextColumn();
		builder.append("Concepto",getControl("concepto"),1);
		builder.nextLine();
		builder.append("Comentario",getControl("comentario"),13);
		return builder.getPanel();
	}
	
	protected int getToolbarType(){
		return JToolBar.HORIZONTAL;
	}
	
	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Movimiento de Inventario","Registro de movimiento de inventario interno");
	}
	
	protected void configDetailScrollPanel(final JScrollPane sp){
		sp.setPreferredSize(new Dimension(650,250));
	}

	@Override
	protected TableFormat getTableFormat() {
		String[] props={"producto.clave","producto.descripcion","cantidad"};
		String[] labels={"Producto","Descripción","Cantidad"};
		return GlazedLists.tableFormat(MovimientoDet.class, props,labels);
	}
	
	protected void fixColumns(final TableColumnModel cm){
		cm.getColumn(0).setPreferredWidth(50);
		cm.getColumn(1).setPreferredWidth(350);
		cm.getColumn(2).setPreferredWidth(50);
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("sucursal".equals(property)){
			JComponent c=Bindings.createSucursalesBinding(model.getModel(property));
			c.setEnabled(!model.isReadOnly());
			return c;
		}else if("comentario".equals(property)){
			JTextField tf=BasicComponentFactory.createTextField(model.getComponentModel(property), false);
			tf.setEnabled(!model.isReadOnly());
			return tf;
		}else if("concepto".equals(property)){
			Object[] data={
					Movimiento.Concepto.CIM
					,Movimiento.Concepto.CIS
					,Movimiento.Concepto.MER
					,Movimiento.Concepto.OIM
					,Movimiento.Concepto.RMC
					,Movimiento.Concepto.VIR
			};
			final SelectionInList sl=new SelectionInList(data,model.getModel(property));
			JComboBox jc=BasicComponentFactory.createComboBox(sl);
			jc.setEnabled(!model.isReadOnly());
			return jc;
		}
		return super.createCustomComponent(property);
	}
	
	

	@Override
	public Object doInsertPartida() {
		return MovimientoDetForm.showForm(new MovimientoDet());
	}

	@Override
	protected void doEdit(Object obj) {
		if(!model.isReadOnly()){
			MovimientoDet det=(MovimientoDet)obj;
			MovimientoDetForm.showForm(det);
		}
	}

	@Override
	protected void doView(Object obj) {
		MovimientoDet det=(MovimientoDet)obj;
		MovimientoDetForm.showForm(det,true);
	}

	public static Movimiento showForm(){
		return showForm(new Movimiento());
	}
	
	public static Movimiento showForm(Movimiento m){
		return showForm(m,false);
	}
	
	public static Movimiento showForm(Movimiento m,boolean readOnly){
		final MovimientoFormModel model=new MovimientoFormModel(m,readOnly);
		final MovimientoForm form=new MovimientoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.getMovimiento();
		}
		return null;
	}
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		Movimiento m=showForm();
		if(m!=null){
			MovimientoForm.showObject(m);
			ServiceLocator2.getInventarioManager().save(m);
		}
		
	}
	
	
	

}
