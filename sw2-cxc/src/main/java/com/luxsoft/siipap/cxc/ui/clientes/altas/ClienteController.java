package com.luxsoft.siipap.cxc.ui.clientes.altas;


import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.springframework.stereotype.Component;


import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;


/** 
 * Controlador para el funcionamiento del mantenimiento de clientes
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Component("clienteController")
public class ClienteController {
	
	
	private static ClienteController INSTANCE;
	
	public static ClienteController getInstance(){
		if(INSTANCE==null){
			INSTANCE=new ClienteController();
		}
		return INSTANCE;
	}
	
	private ClienteController(){}
	
	public Mostrador getMostrador(){
		Mostrador mostrador=Mostrador.getMostrador();
		final DefaultFormModel model=new DefaultFormModel(mostrador);
		final MostradorForm form=new MostradorForm(model);
		mostrador.setPersonaFisica(true);
		form.open();
		if(!form.hasBeenCanceled()){
			return (Mostrador)model.getBaseBean();
		}
		return null;
	}
	
	/**
	 * Permite generar un nuevo cliente  
	 * 
	 * @return
	 */
	public Cliente registrar(){
		final Cliente cliente=new Cliente();
		final ClienteModel model=new ClienteModel(cliente);
		final ClienteForm form=new ClienteForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			Cliente res= (Cliente)model.getBaseBean();
			res=ServiceLocator2.getClienteManager().save(res);
			MessageUtils.showMessage("Cliente registrado con clave: "+res.getClave(), "Registro de clientes");
			return res;
		}
		return null;
	}
	
	public static Action getRegistrarAction(){
		Action a=new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				ClienteController.getInstance().registrar();
			}
		};
		return a;
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
				ClienteController controller=new ClienteController();
				/*Cliente res=controller.registrar();
				System.out.println(ToStringBuilder.reflectionToString(res));
				System.exit(0);*/
				//controller.getMostrador();
				controller.registrar();
			}

		});
	}

}
