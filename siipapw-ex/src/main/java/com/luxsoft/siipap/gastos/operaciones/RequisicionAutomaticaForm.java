package com.luxsoft.siipap.gastos.operaciones;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.gastos.operaciones.RequisicionDeGastosForm.RequisicionDetForm;
import com.luxsoft.siipap.model.gastos.GProductoServicio;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractMasterDetailForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;




/**
 * Forma para el mantenimiento de instancias de {@link GProductoServicio}
 * 
 * @author Ruben Cancino
 *
 */
public class RequisicionAutomaticaForm extends AbstractMasterDetailForm{
	
	protected Logger logger=Logger.getLogger(getClass());

	public RequisicionAutomaticaForm(RequisicionAutomaticaModel model) {
		super(model);
		setTitle("Requisición");
	}

	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Formato de requisición automática","Gastos");
	}
	
	/***** Binding support ******/
	
	

	protected JComponent buildMasterForm(){		
		final DefaultFormBuilder builder=getDefaultMasterFormBuilder();
		if(model.getValue("id")!=null){
			JComponent ct=getControl("id");
			ct.setEnabled(false);
			builder.append("Id",ct,true);
		}
		builder.append("A Favor",addReadOnly("afavor"),9);
		builder.append("RFC",addReadOnly("rfc"));
		builder.append("Concepto",getControl("concepto"),true);		
		
		builder.append("Fecha",addReadOnly("fecha"));
		builder.append("Fecha P.",getControl("fechaDePago"),true);
		
		
		builder.append("Moneda",getControl("moneda"));
		builder.append("TC",getControl("tipoDeCambio"));
		builder.append("F. Pago",getControl("formaDePago"),true);
		
		builder.append("E-Mail",getControl("notificar"),6);  
		builder.nextLine();
		builder.append("Comentario",getControl("comentario"),9);
		
		getControl("total").setEnabled(false);
		
		return builder.getPanel();
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("afavor".equals(property)){
			JTextField tf=Binder.createMayusculasTextField(model.getComponentModel(property));
			return tf;
		}else if("rfc".equals(property)){
			return Binder.createMayusculasTextField(model.getComponentModel(property));
		}else if("departamento".equals(property)){
			JComboBox box=Bindings.createDepartamentosBinding(model.getModel(property));
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("concepto".equals(property)){
			JComboBox box=Bindings.createConceptoDeRequisicionDeTesoreria(model.getModel(property));
			box.setEnabled(!model.isReadOnly());
			return box;
		}else  if("formaDePago".equals(property)){
			JComboBox box=Bindings.createFormasDePagoBinding(model.getComponentModel(property));
			box.setEnabled(!model.isReadOnly());
			return box;
		}
		return null;
	}
	
	/****  Configuracion de el detalle ****/
	
	
	protected TableFormat getTableFormat(){
		final String[] props={"documento","fechaDocumento","facturaDeGasto.total","facturaDeGasto.apagar"
							,"importe","impuesto","total"};
		final String[] names={"CXPFactura","Fecha (F)","Total (F)","A Pagar (F)"
						  ,"Importe","Impuesto","Por Pagar"};
		//final boolean[] edits={true,false,false,false,false,false};
		return GlazedLists.tableFormat(RequisicionDe.class, props, names);
	}
		
	public Object doInsertPartida(){
		return null;
	}
	
	protected void doEdit(Object obj){
		RequisicionDe det=(RequisicionDe)obj;
		RequisicionDe clone=new RequisicionDe();
		BeanUtils.copyProperties(det, clone);
		
		DefaultFormModel model=new DefaultFormModel(clone);
		RequisicionDetForm form=new RequisicionDetForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			BeanUtils.copyProperties(clone,det );
			det.actualizarImportesDeGastosProrrateado();
		}
	}
	
	protected void doView(Object obj){
		RequisicionDe det=(RequisicionDe)obj;
		DefaultFormModel model=new DefaultFormModel(det,true);
		RequisicionDetForm form=new RequisicionDetForm(model);
		form.open();
	}
		
	@Override
	public void enableEditingActions(boolean val) {		
		super.enableEditingActions(false);
	}

	@Override
	protected void enableSelectionActions() {		
		super.enableSelectionActions();
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
		
		builder.addLabel("Total",cc.xy(3, 5));
		builder.add(addReadOnly("total"),cc.xy(5, 5));	
				
		return builder.getPanel();
	}
	
	
	
	public static Requisicion showForm(){
		return showForm(new Requisicion());
	}
	
	public static Requisicion showForm(Requisicion bean){
		return showForm(bean,false);
	}
	
	public static Requisicion showForm(Requisicion bean,boolean readOnly){
		RequisicionAutomaticaModel model=new RequisicionAutomaticaModel(bean);
		model.setReadOnly(readOnly);
		return showForm(model);
	}
	
	public static Requisicion showForm(final RequisicionAutomaticaModel model){
		final RequisicionAutomaticaForm form=new RequisicionAutomaticaForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.getMasterBean();
		}
		return null;
	}
	
	public static void main(String[] args) {
		
		SWExtUIManager.setup();
		Requisicion  bean=showForm();
		if(bean!=null){
			RequisicionAutomaticaForm.showObject(bean);
			ServiceLocator2.getRequisiciionesManager().save(bean);
		}
		System.exit(0);
	}

	
	

}
