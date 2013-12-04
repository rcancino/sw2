package com.luxsoft.sw3.bi;

import java.util.List;

import javax.swing.Action;


import com.luxsoft.siipap.cxc.CXCRoles;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.sw3.ventas.CheckPlusCliente;



/**
 * Consulta para el mantenimiento de clientes Checkplus
 *  
 * @author Ruben Cancino 
 *
 */
public class CheckPlusPanel extends FilteredBrowserPanel<CheckPlusCliente>{

	public CheckPlusPanel() {
		super(CheckPlusCliente.class);		
	}
	
	protected void init(){
		String[] props={"cliente.clave","nombre","rfc","lineaDeCredito","rfc","suspendido","suspendidoComentario"};
		addProperty(props);
		addLabels("Clave","Cliente","RFC","Línea","Suspención","Comentario Susp");
		installTextComponentMatcherEditor("Nombre", "nombre");
		installTextComponentMatcherEditor("RFC", "rfc");
	}
	

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getViewAction()
				,getRoleBasedInsertAction(CXCRoles.ADMINISTRACION_CHECKPLUS.name())
				,getRoleBasedEditAction(CXCRoles.ADMINISTRACION_CHECKPLUS.name())
				,getViewAction()
				,addRoleBasedAction(CXCRoles.ADMINISTRACION_CHECKPLUS.name(),"autorizar", "Autorizar")
				//,addRoleBasedAction(CXCRoles.ADMINISTRACION_CHECKPLUS.name(),"cancelar", "Cancelar")
				};
		return actions;
	}

	@Override
	protected List<CheckPlusCliente> findData() {
		return ServiceLocator2.getHibernateTemplate().find("from CheckPlusCliente order by nombre");
	}

	@Override
	public void open() {
		load();
	}
	
	public void autorizar(){
		CheckPlusCliente c=(CheckPlusCliente)getSelectedObject();
		if(c!=null){
			int index=source.indexOf(c);
			if(index!=-1){
				c=CheckPlusAutorizacionForm.autorizar(c.getId());
				if(c!=null)
					source.set(index, c);
			}
				
		}
	}
	
	public void bitacora(){
		//BitacoraClientesCredito.show();
	}
	
	@Override
	protected CheckPlusCliente doInsert() {		
		return CheckPlusClienteForm.showForm();
	}
	
	@Override
	protected CheckPlusCliente doEdit(CheckPlusCliente bean) {
		return CheckPlusClienteForm.showForm(bean.getId());
	}
	
	@Override
	protected void doSelect(Object bean) {
		super.doSelect(bean);
	}

}
