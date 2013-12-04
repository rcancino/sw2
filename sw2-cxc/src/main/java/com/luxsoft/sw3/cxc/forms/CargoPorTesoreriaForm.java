package com.luxsoft.sw3.cxc.forms;

import javax.swing.Action;
import javax.swing.JComponent;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.model.CargoPorTesoreria;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.ui.selectores.SelectorDeClientes;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.swing.common.ClienteHeader;

/**
 * Forma para la genración de Cargos por tesoreria
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CargoPorTesoreriaForm extends AbstractForm{

	public CargoPorTesoreriaForm(IFormModel model) {
		super(model);
		setTitle("Cargo por tesoreria");
	}

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout("p,3dlu,80dlu,3dlu,p,3dlu,80dlu,150dlu","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Cliente",getControl("cliente"),6);
		builder.nextLine();
		builder.append("Fecha",getControl("fecha"));
		builder.append("Sucursal",getControl("sucursal"));
		builder.nextLine();
		builder.append("Origen",getControl("origen"));
		builder.append("Total",getControl("total"));
		builder.append("Requisición",getControl("documento"));
		builder.nextLine();
		builder.append("Comentario",getControl("comentario"),6);
		return builder.getPanel();
	}
	
	@Override
	protected void onWindowOpened() {
		super.onWindowOpened();
		getControl("cliente").requestFocusInWindow();
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("comentario".equals(property)){
			return Binder.createMayusculasTextField(model.getModel(property));
		}else if("cliente".equals(property)){
			return Binder.createClientesBinding(model.getModel(property));
		}else if("origen".equals(property)){
			SelectionInList sl=new SelectionInList(OrigenDeOperacion.values(),model.getModel(property));
			return BasicComponentFactory.createComboBox(sl);
		}else if("sucursal".equals(property)){
			return Bindings.createSucursalesOperativasBinding(model.getModel(property));
			
		}else if("documento".equals(property)){
			return BasicComponentFactory.createLongField(model.getModel(property));
		}
		return null;
	}
	
	ClienteHeader header;
	
	
	
	@Override
	protected JComponent buildHeader() {
		if(header==null){
			header=new ClienteHeader(model.getModel("cliente"));
		}
		return header;
	}

	private Action lookupAction;
	
	public Action getLookupAction(){
		if(lookupAction==null){
			lookupAction=new DispatchingAction(this,"seleccionarCliente");
			lookupAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images/misc2/page_find.png"));
			lookupAction.putValue(Action.NAME, "F2");
		}
		return lookupAction;
	}
	
	public void seleccionarCliente(){
		Cliente c=SelectorDeClientes.seleccionar();
		model.setValue("cliente", c);
	}
	
	public static CargoPorTesoreria generarCargo(){
		final DefaultFormModel model=new DefaultFormModel(CargoPorTesoreriaModel.getInstanceo());
		final CargoPorTesoreriaForm form=new CargoPorTesoreriaForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			CargoPorTesoreriaModel cm=(CargoPorTesoreriaModel)form.getModel().getBaseBean();
			CargoPorTesoreria cargo=cm.commit();
			cargo=(CargoPorTesoreria)ServiceLocator2.getUniversalDao().save(cargo);
			return cargo;
			
		}
		return null;
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
				DBUtils.whereWeAre();
				CargoPorTesoreria cargo=generarCargo();
				System.out.println("Cargo generado: "+cargo.getId());
				System.exit(0);
			}

		});
	}

}
