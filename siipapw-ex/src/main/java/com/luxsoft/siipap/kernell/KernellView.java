package com.luxsoft.siipap.kernell;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.views2.AbstractInternalTaskView;
import com.luxsoft.siipap.swing.views2.DefaultTaskView;
import com.luxsoft.siipap.swing.views2.InternalTaskTab;

/**
 * Consulta principal del kernell
 * 
 * @author Ruben Cancino
 *
 */
public class KernellView extends DefaultTaskView{
	
	private UsuariosPanel usuariosPanel;	
	private InternalTaskTab usuariosTab;
	private KernellTaskView kernellusuariosView;
	
	private RolesPanel rolesPanel;
	private InternalTaskTab rolesTab;
	private KernellTaskView kernellrolesView;
	
	
	private PermisosPanel permisosPanel;
	private InternalTaskTab permisosTab;
	private KernellTaskView kernellpermisosView;
	
	
	private final KernellModel model;
	
	public KernellView(){
		model=new KernellModel();
	}
	
	
	
	
	@Override
	protected void instalarTaskElements() {
		Action showUsuarios=new AbstractAction("Usuarios"){
			public void actionPerformed(ActionEvent e) {
				mostrarUsuarios();
			}
		};
		CommandUtils.configAction(showUsuarios, KernellActions.MostrarUsuarios.getId(), null);
		consultas.add(showUsuarios);
		
		Action showRoles=new AbstractAction("Roles"){
			public void actionPerformed(ActionEvent e) {
				mostrarRoles();
			}
		};
		CommandUtils.configAction(showRoles, KernellActions.MostrarRoles.getId(), null);
		consultas.add(showRoles);
		
		Action showPermisos=new AbstractAction("Permisos"){
			public void actionPerformed(ActionEvent e) {
				mostrarPermisos();
			}
		};
		CommandUtils.configAction(showPermisos, KernellActions.MostrarPermisos.getId(), null);
		consultas.add(showPermisos);
		
	}

	protected void instalarTaskPanels(final JXTaskPaneContainer container){
		this.taskContainer.remove(this.detalles); //We don't need this
	}
	
	
	private void mostrarUsuarios(){
		if(usuariosTab==null){
			usuariosPanel =new UsuariosPanel(model);
			kernellusuariosView=new KernellTaskView(usuariosPanel);
			kernellusuariosView.setTitle("Usuarios");
			usuariosTab=new InternalTaskTab(kernellusuariosView);
		}
		addTab(usuariosTab);
		usuariosTab.getTaskView().load();
	}
	
	private void mostrarRoles(){
		if(rolesTab==null){
			rolesPanel=new RolesPanel(model);
			kernellrolesView=new KernellTaskView(rolesPanel);
			kernellrolesView.setTitle("Roles");
			rolesTab=new InternalTaskTab(kernellrolesView);
			
		}
		addTab(rolesTab);
		rolesTab.getTaskView().load();
	}
	
	private void mostrarPermisos(){
		if(permisosTab==null){
			permisosPanel=new PermisosPanel(model);
			kernellpermisosView=new KernellTaskView(permisosPanel);
			kernellpermisosView.setTitle("Permisos");
			permisosTab=new InternalTaskTab(kernellpermisosView);
			
		}
		addTab(permisosTab);
		permisosTab.getTaskView().load();
	}
	
	@Override
	public void open() {
	}
	
	@Override
	public void close() {
		if(kernellusuariosView!=null){
			kernellusuariosView.close();			
			kernellusuariosView=null;
		}
	}
	
	
	private class KernellTaskView extends AbstractInternalTaskView{
		
		private final FilteredBrowserPanel browser;
		
		public KernellTaskView(final FilteredBrowserPanel panel){
			this.browser=panel;
		}

		public JComponent getControl() {
			return browser.getControl();
		}
		
		@Override
		public void instalOperacionesAction(JXTaskPane operaciones) {
			for(Action a:browser.getActions()){
				operaciones.add(a);
			}
		}
		
		@Override
		public void installFiltrosPanel(JXTaskPane filtros) {
			filtros.add(browser.getFilterPanel());
		}		
		
		
	}

}
