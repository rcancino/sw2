package com.luxsoft.siipap.kernell;


import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.commons.lang.exception.ExceptionUtils;

import ca.odell.glazedlists.EventList;

import com.luxsoft.siipap.model.Modulos;
import com.luxsoft.siipap.model.Permiso;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;

/**
 * Consulta para el mantenimiento de roles
 * 
 * 
 * @author Ruben Cancino
 *
 */
public class PermisosPanel extends FilteredBrowserPanel<Permiso>{
	
	private final KernellModel model;
	
	private Action actualizarPermisos;
	
	public PermisosPanel(final KernellModel model) {
		super(Permiso.class);
		this.model=model;
		addProperty(new String[]{"id","nombre","descripcion","modulo"});
		addLabels(  new String[]{"Id","Nombre","Descripción","Modulo"});
		installTextComponentMatcherEditor("Permiso", "nombre","descripcion","modulo");
	}

	@Override
	protected EventList getSourceEventList() {
		return model.getPermisos();
	}
	
	private void actualizarPermisos(){
		Object selected=JOptionPane.showInputDialog(grid, "Seleccione un módulo", "Actualización de permisos", JOptionPane.INFORMATION_MESSAGE, null
				, Modulos.values(), Modulos.KERNELL);
		if(selected!=null){
			Modulos res=(Modulos)selected;
			model.regenerarPermisos(res);
		}
	}
	
	
	
	@SuppressWarnings("unchecked")
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getActualizarPermisos()
				,getSecuredDeleteAction(KernellActions.ActualizarPermisos.getId())
				};
		return actions;
	}

	public Action getActualizarPermisos(){
		if(actualizarPermisos==null){
			actualizarPermisos=new AbstractAction("Actualizar Permisos"){
				public void actionPerformed(ActionEvent e) {
					actualizarPermisos();
				}
			};
			CommandUtils.configAction(actualizarPermisos, KernellActions.ActualizarPermisos.getId(), null);
		}
		return actualizarPermisos;
	}
	
	
	public void load(){
		SwingWorker worker=new SwingWorker(){
			@Override
			protected Object doInBackground() throws Exception {
				model.loadPermisos();
				return "OK";
			}
			@Override
			protected void done() {				
				
			}
		};
		TaskUtils.executeSwingWorker(worker);
	}
	
	public void delete(){
		List<Permiso> selected=new ArrayList<Permiso>();
		selected.addAll(getSelected());
		if(!MessageUtils.showConfirmationMessage("Eliminar "+selected.size()+" permisos?", "Eliminación de Permisos"))
			return;
		for(Permiso p:selected){
			try {
				ServiceLocator2.getUniversalManager().remove(Permiso.class, p.getId());
				source.remove(p);
			} catch (Exception e) {
				e.printStackTrace();
				MessageUtils.showMessage(
						"No pudo elminar el permiso, probablemente esta asignado a un rol o usuario\n" +
						" Error: "+ExceptionUtils.getRootCauseMessage(e)
						, "Permisos");
			}
		}
	}

	
	 	
	
}
