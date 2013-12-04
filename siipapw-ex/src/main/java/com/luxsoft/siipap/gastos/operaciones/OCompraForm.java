package com.luxsoft.siipap.gastos.operaciones;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.table.TableColumnModel;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.gastos.GastosRoles;
import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.siipap.model.gastos.GCompraDet;
import com.luxsoft.siipap.model.gastos.GProductoServicio;
import com.luxsoft.siipap.model.gastos.TipoDeCompra;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractMasterDetailForm;
import com.luxsoft.siipap.swing.form2.MasterDetailFormModel;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;




/**
 * Forma para el mantenimiento de instancias de {@link GProductoServicio}
 * 
 * @author Ruben Cancino
 *
 */
public class OCompraForm extends AbstractMasterDetailForm{
	
	protected Logger logger=Logger.getLogger(getClass());

	public OCompraForm(MasterDetailFormModel model) {
		super(model);
		setTitle("Orden de Compra");
	}

	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Orden de Compra","Bienes y/o servicios");
	}
	
	/***** Binding support ******/
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("proveedor".equals(property)){
			ProveedorControl control=new ProveedorControl(model.getModel(property));
			control.setEnabled(!model.isReadOnly());
			control.setEnabled(model.getValue("id")==null);
			return control;
		}else if("sucursal".equals(property)){
			JComboBox box=Bindings.createSucursalesBinding(model.getModel(property));
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("departamento".equals(property)){
			JComboBox box=Bindings.createDepartamentosBinding(model.getModel(property));
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("comentario".equals(property)){
			JComponent control=BasicComponentFactory.createTextArea(model.getComponentModel(property),true);
			control.setEnabled(!model.isReadOnly());
			return control;
		}else if("tipo".equals(property)){
			JComboBox box=Bindings.createTiposDeCompraBinding(model.getModel(property));
			box.setEnabled(!model.isReadOnly());
			return box;
		}
		return null;
	}
	
	

	protected JComponent buildMasterForm(){		
		final FormLayout layout=new FormLayout(
				"p,2dlu,p, 3dlu," +
				"p,2dlu,p:g(.5), 3dlu," +
				"p,2dlu,320dlu:g(.5)"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		if(model.getValue("id")!=null){
			JComponent ct=getControl("id");
			ct.setEnabled(false);
			builder.append("Id",ct,true);
		}
		
		builder.append("Proveedor",getControl("proveedor"),9);
		builder.nextLine();
		
		builder.append("Sucursal",getControl("sucursal"));	
		builder.append("Departamento",getControl("departamento"));		
		builder.append("Tipo",getControl("tipo"));		
		
		builder.append("Fecha",getControl("fecha"));
		builder.append("Entrega",addReadOnly("fechaEntrega"));
		builder.append("Vencimiento",addReadOnly("vencimiento"));
		
		builder.append("Moneda",getControl("moneda"));
		builder.append("TC",getControl("tc"));
		builder.append("Inversión",getControl("inversion"),true);
		
		builder.append("Presupuesto",addReadOnly("presupuesto"),true);
		
		final CellConstraints cc=new CellConstraints();		
		builder.append("Observaciones");
		builder.appendRow(new RowSpec("17dlu"));
		builder.add(new JScrollPane((JTextArea) getControl("comentario")),
				cc.xywh(builder.getColumn(), builder.getRow(),9,2));
		builder.nextLine(2);
		//builder.getPanel().setPreferredSize(new Dimension(850,400));
		return builder.getPanel();
	}	
	
	
	protected TableFormat getTableFormat(){
		final String[] props={
				"sucursal.nombre"
				,"producto.descripcion"
				,"rubro.descripcion","proveedorRembolso","facturaRembolso"
				,"cantidad","precio","importe","impuestoImp","retencion1Imp"
				,"retencion2Imp","total","ietu","conceptoContable"};
		final String[] names={
				"Sucursal"
				,"Producto"
				,"Rubro","Prov (Rembolso)","Factura"
				,"Cantidad","Precio","Importe","Iva","Ret 1 ","Ret 2", "Total","IETU","Tipo(IETU)"};
		
		return GlazedLists.tableFormat(GCompraDet.class, props, names);
	}
	
	/**
	 * Template method para ajustar el grid justo despues de su creacion
	 * 
	 * @param grid
	 */
	protected void adjustGrid(JXTable grid){
		//grid.getColumnExt("conceptoContable").setVisible(true);
		grid.setColumnControlVisible(true);
		grid.getColumnExt("Prov (Rembolso)").setVisible(false);
		grid.getColumnExt("Factura").setVisible(false);
		
		TipoDeCompra tipo=(TipoDeCompra)model.getValue("tipo");
		if(tipo!=null && tipo.equals(TipoDeCompra.REEMBOLSO)){
			//grid.getColumnExt("conceptoContable").setVisible(true);
			grid.getColumnExt("Prov (Rembolso)").setVisible(true);
			grid.getColumnExt("Factura").setVisible(true);
		}
	}
	
	protected Action[] getDetallesActions(){
		if(actions==null){
			Action asignarProveedorFacturaAction=new com.luxsoft.siipap.swing.actions.DispatchingAction(this, "asignarProveedor");
			asignarProveedorFacturaAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/application_form.png"));
			asignarProveedorFacturaAction.putValue(Action.SHORT_DESCRIPTION, "Asignación en grupo de Proveedor/Factura para Rembolso");
			actions=new Action[]{getInsertAction()
				,getDeleteAction(),getEditAction(),getViewAction()
				,asignarProveedorFacturaAction
				};
		}
		return actions;
	}
	
	public void asignarProveedor(){
		TipoDeCompra tipo=(TipoDeCompra)model.getValue("tipo");
		
		if(tipo!=null && tipo.equals(TipoDeCompra.REEMBOLSO)){
			System.out.println("Asingando proveedor..."+tipo);
			EventList<GCompraDet> selected=selection.getSelected();
			if(!selected.isEmpty()){
				GCompraDet res=AsignacionDeProveedorForm.showForm();
				if(res!=null){
					for(GCompraDet det:selected){
						int index=partidasSource.indexOf(det);
						if(index!=-1){
							det.setProveedorRembolso(res.getProveedorRembolso());
							det.setFacturaRembolso(res.getFacturaRembolso());
							partidasSource.set(index, det);
						}
					}
				}
			}
		}
	}
		
	public Object doInsertPartida(){
		if(model.isReadOnly()) 
			return null; //Make sure nothing happends when the form is read-only
		GCompraDet det=new GCompraDet();
		
		det.setSucursal( ((GCompra)model.getBaseBean()).getSucursal() );
		TipoDeCompra tipo=(TipoDeCompra)model.getValue("tipo");
		if(tipo!=null && tipo.equals(TipoDeCompra.REEMBOLSO))
			det=OCompraDetForm.edit(det,true);
		else
			det=OCompraDetForm.edit(det);
		//System.out.println(ToStringBuilder.reflectionToString(det,ToStringStyle.MULTI_LINE_STYLE));
		return det;
	}
	
	
	protected void doEdit(Object obj){
		
		int index=this.sortedPartidas.indexOf(obj);
		if(index!=-1){
			GCompraDet det=(GCompraDet)obj;
			TipoDeCompra tipo=(TipoDeCompra)model.getValue("tipo");
			if(tipo!=null && tipo.equals(TipoDeCompra.REEMBOLSO))
				det=OCompraDetForm.edit(det,true);
			else
				det=OCompraDetForm.edit(det);
			if(det!=null)
				sortedPartidas.set(index, det);
		}
	}
	
	protected void doView(Object obj){
		GCompraDet det=(GCompraDet)obj;
		OCompraDetForm.view(det);
	}
	
	
	
	/**
	 * Consstruye el panel de Importe, impuesto y total. Asume que el modelo puede
	 * proporcionar {@link ValueModel} para estas propiedades
	 * Tambien coloca aqui el panel de validación si la fomra no es de solo lectura
	 * 
	 * @return
	 */
	protected JComponent buildTotalesPanel(){
		
		final FormLayout layout=new FormLayout(
				"p:g,5dlu,p,2dlu,max(p;50dlu)"
				,"p,2dlu,p,2dlu,p");
		
		//final FormDebugPanel debugPanel=new FormDebugPanel(layout);
		
		final PanelBuilder builder=new PanelBuilder(layout);		
		final CellConstraints cc=new CellConstraints();
		
		if(!model.isReadOnly())			
			builder.add(buildValidationPanel(),cc.xywh(1, 1,1,5));
		
		builder.addLabel("Importe",cc.xy(3, 1));
		builder.add(addReadOnly("importe"),cc.xy(5, 1));
		
		builder.addLabel("Impuesto",cc.xy(3, 3));
		builder.add(addReadOnly("impuesto"),cc.xy(5, 3));
		
		builder.addLabel("Total",cc.xy(3, 5));
		builder.add(addReadOnly("total"),cc.xy(5, 5));	
				
		return builder.getPanel();
	}
	
	public static GCompra showForm(){
		return showForm(new GCompra());
	}
	
	public static GCompra showForm(GCompra bean){
		if(!bean.getFacturas().isEmpty()){
			if(KernellSecurity.instance().hasRole(GastosRoles.GASTOS_MANAGER.name())){
				return showForm(bean,false);
			}else if(bean.getFacturas().iterator().next().getPagado().amount().doubleValue()>0){
				return showForm(bean,true);
			}
		}
		return showForm(bean,false);
	}
	
	public static GCompra showForm(GCompra bean,boolean readOnly){
		OCompraModel model=new OCompraModel(bean);
		model.setReadOnly(readOnly);
		model.getCompra().actualizarTotal();
		final OCompraForm form=new OCompraForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.getCompra();
		}
		return null;
	}	
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		GCompra  bean=showForm();
		if(bean!=null){
			OCompraForm.showObject(bean);
			ServiceLocator2.getGCompraDao().save(bean);
		}
		
		System.exit(0);
	}
	

}
