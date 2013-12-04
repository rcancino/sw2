package com.luxsoft.sw3.bi;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.gastos.GTipoProveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractForm;

/**
 * Forma para el mantenimiento de instancias de {@link GTipoProveedor}
 * 
 * @author Ruben Cancino
 *
 */
public class ModificacionClienteForm extends AbstractForm{
	
	
	
	public ModificacionClienteForm(ModificacionClienteFormModel model) {
		super(model);
		setTitle("Modificación de cliente");
	}
	
	private ModificacionClienteFormModel getClienteModel(){
		return (ModificacionClienteFormModel)getModel();
	}
	

	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Modificación a:"+getClienteModel().getCliente().getNombreRazon(),getClienteModel().getMessage());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"p,3dlu,max(p;100dlu):g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.append(getClienteModel().getLabel(),getControl(getClienteModel().getProperty()));
		builder.append("Comentario",getControl("comentario"));
		return builder.getPanel();
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("credito.linea".equals(property)){
			JComponent control=Bindings.createCantidadMonetariaPesosBinding(getClienteModel().getCreditoModel().getComponentModel("linea"));
			return control;			
		}else if("comentario".equals(property)){
			JTextArea control=BasicComponentFactory.createTextArea(getClienteModel().getComentarioModel());
			control.setColumns(60);
			control.setRows(5);
			return new JScrollPane(control);
		}else if("plazo".equals(property)){
			ValueModel valueModel=getClienteModel().getCreditoModel().getComponentModel(property);
			return BasicComponentFactory.createIntegerField(valueModel, 0);
		}else if("credito.descuentoEstimado".equals(property)){
			ValueModel valueModel=getClienteModel().getCreditoModel().getComponentModel("descuentoEstimado");
			return Bindings.createDescuentoEstandarBinding(valueModel);
		}else if("credito.chequePostfechado".equals(property)){
			ValueModel valueModel=getClienteModel().getCreditoModel().getComponentModel("chequePostfechado");
			return BasicComponentFactory.createCheckBox(valueModel, "");
		}else if("credito.atrasoMaximo".equals(property)){
			ValueModel valueModel=getClienteModel().getCreditoModel().getComponentModel("atrasoMaximo");
			return BasicComponentFactory.createIntegerField(valueModel, 0);
		}else if("credito.checkplus".equals(property)){
			ValueModel valueModel=getClienteModel().getCreditoModel().getComponentModel("checkplus");
			return BasicComponentFactory.createCheckBox(valueModel, "");
		}else if("credito.permitirCheque".equals(property)){
			ValueModel valueModel=getClienteModel().getCreditoModel().getComponentModel("permitirCheque");
			return BasicComponentFactory.createCheckBox(valueModel, "");			
		}else if("credito.suspendido".equals(property)){
			ValueModel valueModel=getClienteModel().getCreditoModel().getComponentModel("suspendido");
			return BasicComponentFactory.createCheckBox(valueModel, "");
		}
		return null;
	}
	
	
	public static Cliente modificarLineaDeCredito(String clave ){
		Cliente c=ServiceLocator2.getClienteManager().buscarPorClave(clave);
		ModificacionClienteFormModel model=ModificacionClienteFormModel.getLineaModel(c);
		final ModificacionClienteForm form=new ModificacionClienteForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.commit();
		}
		return null;
	}
	
	
	public static Cliente modificarPlazoDeCredito(String clave ){
		Cliente c=ServiceLocator2.getClienteManager().buscarPorClave(clave);
		ModificacionClienteFormModel model=ModificacionClienteFormModel.getPlazoModel(c);
		final ModificacionClienteForm form=new ModificacionClienteForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.commit();
		}
		return null;
	}
	
	public static Cliente modificarChequePosFechadoCredito(String clave ){
		Cliente c=ServiceLocator2.getClienteManager().buscarPorClave(clave);
		ModificacionClienteFormModel model=ModificacionClienteFormModel.getChequeModel(c);
		return modificarClienteCredito(model);
	}
	
	public static Cliente modificarDescuentoFijoCredito(String clave ){
		Cliente c=ServiceLocator2.getClienteManager().buscarPorClave(clave);
		ModificacionClienteFormModel model=ModificacionClienteFormModel.getDescuentoFijoModel(c);
		return modificarClienteCredito(model);
	}
	
	public static Cliente modificarAtrasoMaximo(String clave ){
		Cliente c=ServiceLocator2.getClienteManager().buscarPorClave(clave);
		ModificacionClienteFormModel model=ModificacionClienteFormModel.getAtrasoMaximoModel(c);
		return modificarClienteCredito(model);
	}
	
	public static Cliente modificarCheckplus(String clave ){
		Cliente c=ServiceLocator2.getClienteManager().buscarPorClave(clave);
		ModificacionClienteFormModel model=ModificacionClienteFormModel.getCheckPlusModel(c);
		return modificarClienteCredito(model);
	}
	
	public static Cliente modificarClienteCredito(ModificacionClienteFormModel model){
		final ModificacionClienteForm form=new ModificacionClienteForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.commit();
		}
		return null;
	}
	
	public static Cliente modificarPermitirCheque(String clave ){
		Cliente c=ServiceLocator2.getClienteManager().buscarPorClave(clave);
		ModificacionClienteFormModel model=ModificacionClienteFormModel.getPermitirChequeModel(c);
		return modificarClienteCredito(model);
	}
	
	public static Cliente modificarSuspendidoCredito(String clave ){
		Cliente c=ServiceLocator2.getClienteManager().buscarPorClave(clave);
		ModificacionClienteFormModel model=ModificacionClienteFormModel.getSuspendido(c);
		return modificarClienteCredito(model);
	}
	
	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				Object res=modificarChequePosFechadoCredito("U050008");
				showObject(res);
				System.exit(0);
			}

		});
	}

}
