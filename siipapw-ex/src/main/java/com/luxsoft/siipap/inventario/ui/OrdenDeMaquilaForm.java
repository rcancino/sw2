package com.luxsoft.siipap.inventario.ui;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.inventario.MovimientoDetForm;
import com.luxsoft.siipap.inventario.MovimientoForm;
import com.luxsoft.siipap.inventario.MovimientoFormModel;
import com.luxsoft.siipap.inventarios.model.Movimiento;
import com.luxsoft.siipap.inventarios.model.MovimientoDet;
import com.luxsoft.siipap.maquila.model.RecepcionDeMaquila;
import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractMasterDetailForm;
import com.luxsoft.siipap.swing.form2.MasterDetailFormModel;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;

public class OrdenDeMaquilaForm extends AbstractMasterDetailForm{

	public OrdenDeMaquilaForm(MasterDetailFormModel model) {
		super(model);
	}

	@Override
	protected JComponent buildMasterForm() {
		FormLayout layout=new FormLayout("p,2dlu,p,3dlu,p,2dlu,p","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		if(model.getValue("id")!=null){
			builder.append("Id",addReadOnly("id"),true);
		}
		builder.append("Fecha",getControl("fecha"));		
		builder.append("Sucursal",addReadOnly("sucursal"));
		builder.append("Comentario",getControl("comentario"),5);
		return builder.getPanel();
	}

	@Override
	protected TableFormat getTableFormat() {
		String[] props={"producto.clave","producto.descripcion","cantidad"};
		String[] labels={"Producto","Descripcion","recibido"};
		return GlazedLists.tableFormat(EntradaDeMaquila.class, props,labels);
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("comentario".equals(property)){
			JTextField tf=BasicComponentFactory.createTextField(model.getComponentModel(property), false);
			tf.setEnabled(!model.isReadOnly());
			return tf;
		}else if("sucursal".equals(property)){
			ValueModel vm=model.getModel("sucursal");
			JTextField box=new JTextField(10);
			if(vm.getValue()!=null)
				box.setText(vm.getValue().toString());
			box.setEditable(false);
			box.setFocusable(false);
			return box;
		}
		return super.createCustomComponent(property);
	}
	
	public static RecepcionDeMaquila showForm(){
		RecepcionDeMaquila orden=new RecepcionDeMaquila();
		OrdenDeMaquilaFormModel model=new OrdenDeMaquilaFormModel(orden);
		OrdenDeMaquilaForm form=new OrdenDeMaquilaForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return orden;
		}
		return null;
	}
	
	
	
	
	@Override
	public Object doInsertPartida() {
		EntradaDeMaquila om=new EntradaDeMaquila();
		om.setSucursal((Sucursal) model.getValue("sucursal"));
		return OrdenDeMaquilaDetForm.showForm(om);
	}
	
	@Override
	protected void doEdit(Object obj) {
		if(!model.isReadOnly()){
			EntradaDeMaquila det=(EntradaDeMaquila)obj;
			OrdenDeMaquilaDetForm.showForm(det);
		}
	}
	
	@Override
	protected void doView(Object obj) {
		EntradaDeMaquila det=(EntradaDeMaquila)obj;
		OrdenDeMaquilaDetForm.showForm(det,true);
	}
	

	
	public static RecepcionDeMaquila showForm(RecepcionDeMaquila m){
		return showForm(m,false);
	}
	
	public static RecepcionDeMaquila showForm(RecepcionDeMaquila m,boolean readOnly){
		final OrdenDeMaquilaFormModel model=new OrdenDeMaquilaFormModel(m,readOnly);
		final OrdenDeMaquilaForm form=new OrdenDeMaquilaForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.getOrdenMaq();
		}
		return null;
	}




	/**
	 * FormModel para Orden de maquila
	 * 
	 * @author RUBEN
	 *
	 */
	private static class OrdenDeMaquilaFormModel extends MasterDetailFormModel{
		
		public OrdenDeMaquilaFormModel() {
			super(RecepcionDeMaquila.class);
		}
		
		public OrdenDeMaquilaFormModel(Object bean, boolean readOnly) {
			super(bean, readOnly);
		}
		
		public RecepcionDeMaquila getOrdenMaq(){
			return (RecepcionDeMaquila)getBaseBean();
		}
		
		
		public OrdenDeMaquilaFormModel(RecepcionDeMaquila bean) {
			super(bean);			
		}
		
		@Override
		protected void addValidation(PropertyValidationSupport support) {
			if(getOrdenMaq().getPartidas().isEmpty()){
				support.addError("partidas", "No se puede salvar el movimiento sin partidas");
			}
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public Object insertDetalle(Object obj) {
			/*EntradaDeMaquila det=(EntradaDeMaquila)obj;
			boolean res=getOrdenMaq().addPartida(det);
			if(res){
				source.add(det);
				return true;
			}*/
			return null;
		}
		
		
		
		@SuppressWarnings("unchecked")
		protected void init(){
			super.init();
			if(getOrdenMaq().getId()==null){
				asignarSucursal();
			}
			
			if(getOrdenMaq().getId()!=null){
				System.out.println("Insertando Partidas");
				for (EntradaDeMaquila det:getOrdenMaq().getPartidas()){
					source.add(det);
				}
			}
			
		}
		
		public void asignarSucursal(){
			setValue("sucursal", ServiceLocator2.getConfiguracion().getSucursal());
		}
		
		
		@Override
		public boolean deleteDetalle(Object obj) {
			/*if(!isReadOnly()){
				EntradaDeMaquila det=(EntradaDeMaquila)obj;
				boolean res= getOrdenMaq().eliminarPartida(det);
				if(res)
					source.remove(det);
				return res;
			}*/
			return false;
		}
		

		@Override
		public boolean manejaTotalesEstandares() {
			return false;
		}
		
	}
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		RecepcionDeMaquila o=showForm();
		if(o!=null){
			OrdenDeMaquilaForm.showObject(o);
			//ServiceLocator2.getMaquilaManager().save(o);
		}

	}

}
