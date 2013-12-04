package com.luxsoft.siipap.kernell;


import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingWorker;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.luxsoft.siipap.model.Permiso;
import com.luxsoft.siipap.model.Role;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;

/**
 * Consulta para el mantenimiento de roles
 * 
 * 
 * @author Ruben Cancino
 *
 */
public class RolesPanel extends FilteredBrowserPanel<Role>{
	
	private final KernellModel model;
	private Action agregarPermiso;
	
	public RolesPanel(final KernellModel model) {
		super(Role.class);
		this.model=model;
		addProperty(new String[]{"id","name","description"});
		addLabels(  new String[]{"Id","Nombre","Descripción"});
		installTextComponentMatcherEditor("Role", "name","description");
	}
	
	

	@Override
	protected EventList getSourceEventList() {
		return model.getRoles();
	}



	protected  JComponent buildContent(){ 
		JSplitPane sp=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		sp.setTopComponent(super.buildContent());
		sp.setBottomComponent(buildRegistrosPanel());
		sp.setOneTouchExpandable(true);
		sp.setResizeWeight(.6);		
		return sp;
	}
	
	private EventSelectionModel permisosSelection;
	
	/**
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private JComponent buildRegistrosPanel(){
		
		JXTable detalleGrid=ComponentUtils.getStandardTable();
		
		final String[] cols={"id","nombre","descripcion","modulo"};
		final String[] names={"Id","Nombre","Descripción","Módulo"};
		final TableFormat<Permiso> tf=GlazedLists.tableFormat(Permiso.class, cols,names);
		
		final Model<Role, Permiso> model=new Model<Role, Permiso>(){
			public List<Permiso> getChildren(Role parent) {
				return parent.getPermisosAsList();
			}
		};
		final CollectionList<Role, Permiso> colList=new CollectionList<Role, Permiso>(getSelected(),model);
		
		final SortedList<Permiso> sortedPermisos=new SortedList<Permiso>(colList,null);
		final EventTableModel<Permiso> tm=new EventTableModel<Permiso>(sortedPermisos,tf);
		permisosSelection=new EventSelectionModel(sortedPermisos);
		detalleGrid.setModel(tm);
		detalleGrid.setSelectionModel(permisosSelection);

		TableComparatorChooser.install(grid, sortedPermisos, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
		final JScrollPane sp=new JScrollPane(detalleGrid);
		sp.setPreferredSize(new Dimension(200,200));
		return sp;
		
	}
	
	public void load(){
		SwingWorker worker=new SwingWorker(){
			@Override
			protected Object doInBackground() throws Exception {
				model.loadRoles();
				return "OK";
			}
			@Override
			protected void done() {				
				//detalleGrid.packAll();
			}
		};
		TaskUtils.executeSwingWorker(worker);
	}
	
	private void agregarPermisos(){
		AbstractSelector<Permiso> selector=new AbstractSelector<Permiso>(Permiso.class,"Permisos "){

			@SuppressWarnings("unchecked")
			@Override
			protected List<Permiso> getData() {
				return ServiceLocator2.getUniversalManager().getAll(Permiso.class);
			}

			@Override
			protected TableFormat<Permiso> getTableFormat() {
				final String[] cols={"id","nombre","descripcion","modulo"};
				final String[] names={"Id","Nombre","Descripción","Módulo"};
				final TableFormat<Permiso> tf=GlazedLists.tableFormat(Permiso.class, cols,names);
				return tf;
			}
			
			public int getSelectionMode(){
				return ListSelection.MULTIPLE_INTERVAL_SELECTION_DEFENSIVE;
			}
			
		};
		selector.open();
		if(!selector.hasBeenCanceled()){
			Role r=(Role)getSelectedObject();
			final List<Permiso> seleccion=new ArrayList<Permiso>();
			seleccion.addAll(selector.getSelectedList());
			for(Permiso p:seleccion){
				r.addPermiso(p);
			}
			try {
				model.actualizarRole(r);
			} catch (Exception e) {
				MessageUtils.showError("Error actualizando rol", e);
			}
		}
	}
	
	public Action getAgregarPermiso(){
		if(agregarPermiso==null){
			agregarPermiso=new AbstractAction("Agregar Permiso"){
				public void actionPerformed(ActionEvent e) {
					agregarPermisos();
				}
			};
			CommandUtils.configAction(agregarPermiso, KernellActions.AgregarPermisosPorRol.getId(), null);
		}
		return agregarPermiso;
		
	}



	@SuppressWarnings("unchecked")
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{getLoadAction()
				,getInsertAction(),getDeleteAction(),getEditAction()
				,getAgregarPermiso()
				,addAction(KernellActions.ActualizarPermisos.getId(), "eliminarPermisos", "Eliminar permisos")
				};
		return actions;
	}


	@Override
	public void insert(){
		Role r=RoleForm.showForm();
		try {
			model.actualizarRole(r);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void delete(){
		Role bean=(Role)getSelectedObject();
		if(bean!=null){
			try {
				model.eliminarRole(bean);
			} catch (Exception e) {
				MessageUtils.showMessage("Imposible eliminar rol, probablemente esta asignado a un usuario\n" +
						" Error: "+ExceptionUtils.getRootCauseMessage(e), "Rol");
				e.printStackTrace();
			}
		}
	}

	@Override
	public void edit() {
		Role r=(Role)getSelectedObject();
		if(r!=null){
			try {
				model.actualizarRole(r);
			} catch (Exception e) {
				MessageUtils.showError("Error salvando roles", e);
			}
			
		}
	}

	@Override
	protected void doSelect(Object bean) {
		RoleForm.showForm((Role)bean, true);
	}
	
	
	
	 public void eliminarPermisos(){
		 if(!permisosSelection.isSelectionEmpty()){
			 Role r=(Role)getSelectedObject();
				if(r!=null){
					List<Permiso> permisos=new ArrayList<Permiso>();
					permisos.addAll(permisosSelection.getSelected());
					for(Permiso p:permisos){
						try {
							//Permiso p=(Permiso)permisosSelection.getSelected().get(0);
							r.removePermiso(p);
							model.actualizarRole(r);
							//selectionModel.clearSelection();
						} catch (Exception e) {
							MessageUtils.showError("Error salvando roles", e);
						}
					}
					
				}
		 }		 
	 }
	
	
	
}
