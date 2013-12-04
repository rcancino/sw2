package com.luxsoft.siipap.tesoreria.movimientos;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.gastos.GProductoServicio;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractMasterDetailForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.form2.MasterDetailFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;




/**
 * Forma para el mantenimiento de instancias de {@link GProductoServicio}
 * 
 * @author Ruben Cancino
 *
 */
public class RequisicionForm extends AbstractMasterDetailForm{
	
	protected Logger logger=Logger.getLogger(getClass());

	public RequisicionForm(MasterDetailFormModel model) {
		super(model);
		setTitle("Requisición");
	}

	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Formato de requisición","Tesorería");
	}
	
	/***** Binding support ******/
	
	private JCheckBox saldoFavorCheckBox;

	protected JComponent buildMasterForm(){		
		final DefaultFormBuilder builder=getDefaultMasterFormBuilder();
		if(model.getValue("id")!=null){
			JComponent ct=getControl("id");
			ct.setEnabled(false);
			builder.append("Id",ct,true);
		}
		builder.append("A Favor",getControl("afavor"),9);
		builder.append("RFC",getControl("rfc"),true);
		
		
		builder.append("Fecha",getControl("fecha"));
		builder.append("Concepto",getControl("concepto"));
		builder.append("Fecha P.",getControl("fechaDePago"));
		builder.nextLine();
		builder.append("Moneda",getControl("moneda"));
		builder.append("TC",getControl("tipoDeCambio"));
		builder.append("F. Pago",getControl("formaDePago"));
		
		builder.appendSeparator("Devolución de clientes");
		builder.append("Clasificacion",getControl("notificar"));
		saldoFavorCheckBox=new JCheckBox("",false);
		saldoFavorCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				System.out.println("Item: "+e.getStateChange());
				getControl("claveCliente").setEnabled(saldoFavorCheckBox.isSelected());
				if(saldoFavorCheckBox.isSelected()){
					//System.out.println("Actualizando check box...");
					getModel().setValue("notificar", null);
				}else{
					getModel().setValue("claveCliente", null);
					
				}
			}
		});
		
		builder.append("Saldo a Favor",saldoFavorCheckBox);
		builder.append("Cliente (Clave)",getControl("claveCliente"));
		getControl("claveCliente").setEnabled(false);
		
		
		//builder.append("E-Mail",getControl("notificar"),6);  //5522141529
		//builder.nextLine();
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
		}else if("concepto".equals(property)){
			JComboBox box=Bindings.createConceptoDeRequisicionDeTesoreria(model.getModel(property));
			box.setEnabled(!model.isReadOnly());
			return box;
		}else  if("formaDePago".equals(property)){
			JComboBox box=Bindings.createFormasDePagoBinding(model.getComponentModel(property));
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("notificar".equals(property)){
			
			JComboBox box=BasicComponentFactory.createComboBox(new SelectionInList(Requisicion.TiposDeDevoluciones, model.getModel(property)));
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("claveCliente".equals(property)){
			
			final JTextField tf=Binder.createMayusculasTextField(model.getModel(property), true);
			tf.setEnabled(!model.isReadOnly());
			model.getModel(property).addValueChangeListener(new PropertyChangeListener() {					
				public void propertyChange(PropertyChangeEvent evt) {
					//System.out.println("Localizando cliente....");
					if(StringUtils.isBlank(tf.getText()))
						return;
					Cliente c=ServiceLocator2.getClienteManager().buscarPorClave(tf.getText());
					if(c!=null){
						MessageUtils.showMessage("Saldo a favor para: "+c.getNombreRazon(), "");
					}else{
						MessageUtils.showMessage("No existe el cliente con clave: "+tf.getText(), "Requisiciones");
						tf.setText(null);
					}						
				}
			});
			return tf;
		}
		return null;
	}
	
	/****  Configuracion de el detalle ****/
	
	
	protected TableFormat getTableFormat(){
		final String[] props={"documento","fechaDocumento","importe","impuesto","total"};
		final String[] names={"CXPFactura","Fecha F","Importe","Impuesto","Total"};
		return GlazedLists.tableFormat(RequisicionDe.class, props, names);
	}
		
	public Object doInsertPartida(){
		if(model.isReadOnly()) 
			return null; //Make sure nothing happends when the form is read-only
		RequisicionDe det=new RequisicionDe();
		DefaultFormModel model=new DefaultFormModel(det);
		RequisicionDetForm form=new RequisicionDetForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			if(logger.isDebugEnabled()){
				logger.debug("Agregando partida: "+det);
			}
			return model.getBaseBean();
		}
		
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
		}
	}
	
	protected void doView(Object obj){
		RequisicionDe det=(RequisicionDe)obj;
		DefaultFormModel model=new DefaultFormModel(det,true);
		RequisicionDetForm form=new RequisicionDetForm(model);
		form.open();
	}
	
	public static class RequisicionDetForm extends GenericAbstractForm<RequisicionDe>{

		public RequisicionDetForm(IFormModel model) {
			super(model);
			setTitle("CXPFactura/Documento a pagar");
		}

		@Override
		protected JComponent buildFormPanel() {
			final FormLayout layout=new FormLayout(
					"p,4dlu,f:p:g, 2dlu," +
					"p,4dlu,70dlu" 
					,"");
			final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			
			builder.append("Documento", getControl("documento"),5);
			builder.append("Fecha",getControl("fechaDocumento"),5);
			builder.append("Sucursal",getControl("sucursal"),5);	
			builder.append("Departamento",getControl("departamento"),5);
			
			//builder.append("Importe",getControl("importe"),5);
			//builder.append("Impuesto",getControl("impuesto"),5);
			builder.append("Total",getControl("total"),5);
			
			builder.append("Comentario",getControl("comentario"),5);
						
			return builder.getPanel();
		}
		

		@Override
		protected JComponent createCustomComponent(String property) {
			if("producto".equals(property)){
				return null;			
			}else if("sucursal".equals(property)){
				JComboBox box=Bindings.createSucursalesBinding(model.getComponentModel(property));
				box.setEnabled(!model.isReadOnly());
				return box;
			}else if("departamento".equals(property)){
				JComboBox box=Bindings.createDepartamentosBinding(model.getComponentModel(property));
				box.setEnabled(!model.isReadOnly());
				return box;
			}
			return null;
		}
		
		public void doApply(){
			RequisicionDe bean=(RequisicionDe)model.getBaseBean();
			//bean.actualizarDelTotal();
			super.doApply();
		}
		
		
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
		return showForm(bean, false);
		
	}
	
	public static Requisicion showForm(Requisicion bean,boolean readOnly){
		RequisicionModel model=new RequisicionModel(bean);
		model.setReadOnly(readOnly);
		final RequisicionForm form=new RequisicionForm(model);
		form.open();
		if(readOnly)
			return null;
		if(!form.hasBeenCanceled()){
			model.commit();
			return model.getMasterBean();
		}
		return null;
	}	
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		
		//Requisicion r=ServiceLocator2.getRequisiciionesManager().get(256861L);		
		Requisicion  bean=showForm();
		if(bean!=null){
			RequisicionForm.showObject(bean);
			//ServiceLocator2.getRequisiciionesManager().save(bean);
		}
		System.exit(0);
	}

	
	

}
