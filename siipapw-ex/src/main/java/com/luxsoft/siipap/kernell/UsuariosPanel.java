package com.luxsoft.siipap.kernell;


import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.jdesktop.swingx.JXMultiSplitPane;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.MultiSplitLayout;
import org.jdesktop.swingx.MultiSplitLayout.Divider;
import org.jdesktop.swingx.MultiSplitLayout.Leaf;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.UniqueList;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.uif.panel.SimpleInternalFrame;
import com.luxsoft.siipap.model.Permiso;
import com.luxsoft.siipap.model.Role;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.service.UserExistsException;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;

/**
 * Consulta de egresos por concepto de pago de gastos
 * 
 * 
 * @author Ruben Cancino
 *
 */
public class UsuariosPanel extends FilteredBrowserPanel<User>{
	
	private final KernellModel model;
	
	private Action agregarRoles;
	
	
	public UsuariosPanel(final KernellModel model) {
		super(User.class);
		this.model=model;
		
		addProperty(new String[]{"fullName"});
		addLabels(  new String[]{"Nombre"});
		installTextComponentMatcherEditor("Usuario", "userName","fullName");
		
	}
	
	public void insert(){
		User u=NewUserForm.showForm();
		if(u!=null){
			try {
				model.salvaNuevoUsuario(u);
			} catch (UserExistsException e) {
				MessageUtils.showError("Error salvando usuario", e);
			}
		}
	}
	
	public boolean doDelete(User bean){
		try {
			model.eliminarUsuario(bean);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	public Action getAgregarRolesAction(){
		if(agregarRoles==null){
			agregarRoles=new AbstractAction("Agregar roles"){
				public void actionPerformed(ActionEvent e) {
					agregarRol();
				}
			};
		}
		return agregarRoles;
	}

	


	@SuppressWarnings("unchecked")
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getInsertAction()
				,getDeleteAction()
				,getAgregarRolesAction()
				,addAction(KernellActions.MostrarRoles.getId(), "eliminarRol", "Eliminar Rol")
				};
		return actions;
	}

	public void load(){
		SwingWorker worker=new SwingWorker(){
			@Override
			protected Object doInBackground() throws Exception {
				model.loadUsuarios();
				return "OK";
			}
			@Override
			protected void done() {				
				detalleGrid.packAll();
			}
		};
		TaskUtils.executeSwingWorker(worker);
	}
	
	protected void adjustMainGrid(final JXTable grid){
		grid.getColumnExt(0).setPreferredWidth(200);
	}
	
	protected EventList<User> getSourceEventList(){
		return new UniqueList<User>(model.getUsuarios(),GlazedLists.beanPropertyComparator(User.class, "username"));
	}

	@SuppressWarnings("unchecked")
	protected  JComponent buildContent(){
		
		
		Leaf rightTop=new Leaf("rightTop");
		rightTop.setWeight(.5);
		Leaf rightMid=new Leaf("rightMid");
		rightMid.setWeight(.2);
		Leaf rightBot=new Leaf("rightBot");
		rightBot.setWeight(.3);
		List rightChildren=Arrays.asList(rightTop,new Divider(),rightMid,new Divider(),rightBot);
		
		MultiSplitLayout.Split rightSplit=new MultiSplitLayout.Split();
		rightSplit.setRowLayout(false);
		rightSplit.setWeight(.5);
		rightSplit.setChildren(rightChildren);
		
		MultiSplitLayout.Split modelRoot=new MultiSplitLayout.Split();
		
		Leaf left=new Leaf("left");
		List children=Arrays.asList(left,new Divider(),rightSplit);
		modelRoot.setChildren(children);
		
		JXMultiSplitPane sp=new JXMultiSplitPane();
		sp.setModel(modelRoot);
		sp.add("left",super.buildContent());
		sp.add("rightTop",buildUsuarioPanel());
		sp.add("rightMid",buildRolesPanel());
		sp.add("rightBot",buildPermisosPanel());
		
		grid.setColumnControlVisible(false);		
		
		return sp;
	}
	
	private UsuarioForm usuarioForm;
	private  ValueHolder userChannel;
	
	private JComponent buildUsuarioPanel(){
		userChannel=new ValueHolder(null,true);
		usuarioForm =new UsuarioForm(userChannel);
		usuarioForm.getModel().getTriggerChannel().addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				Boolean val=(Boolean)evt.getNewValue();
				if(val)
					actualizarUsuario();
			}
		});
		return usuarioForm;
	}
	
	private void actualizarUsuario(){
		if(userChannel.getValue()!=null)
			try {
				model.actualizarUsuario((User)userChannel.getValue());
			} catch (Exception e) {
				MessageUtils.showError("Error salvando usuario", e);
			}
	}
	
	private JXTable permisosGrid;
	
	private JComponent buildPermisosPanel(){
		
		permisosGrid=ComponentUtils.getStandardTable();
		final String[] cols={"id","nombre","descripcion","modulo"};
		final String[] names={"Id","Nombre","Descripción","Módulo"};
		final TableFormat<Permiso> tf=GlazedLists.tableFormat(Permiso.class, cols,names);
		
		final Model<Role, Permiso> model=new Model<Role, Permiso>(){
			public List<Permiso> getChildren(Role parent) {
				return parent.getPermisosAsList();
			}
		};
		final CollectionList<Role, Permiso> colList=new CollectionList<Role, Permiso>(roleSelection.getSelected(),model);
		
		final SortedList<Permiso> sortedPermisos=new SortedList<Permiso>(colList,null);
		final EventTableModel<Permiso> tm=new EventTableModel<Permiso>(sortedPermisos,tf);
		permisosGrid.setModel(tm);
		
		TableComparatorChooser.install(permisosGrid, sortedPermisos, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
		permisosGrid.setColumnControlVisible(false);
		
		
		
		final JScrollPane sp=new JScrollPane(permisosGrid);
		sp.setPreferredSize(new Dimension(200,100));
		
		SimpleInternalFrame frame=new SimpleInternalFrame("Permisos");
		frame.setContent(sp);
		return frame;
		
	}
	
	
	
	private JXTable detalleGrid;
	private EventSelectionModel<Role> roleSelection;
	
	/**
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private JComponent buildRolesPanel(){
		detalleGrid=ComponentUtils.getStandardTable();
		
		final String[] cols={"id","name","description"};
		final String[] names={"Id","Nombre","Descripción"};
		final TableFormat<Role> tf=GlazedLists.tableFormat(Role.class, cols,names);
		
		final Model<User, Role> model=new Model<User, Role>(){
			public List<Role> getChildren(User parent) {
				return parent.getRolesAsList();
			}
		};
		final CollectionList<User, Role> colList=new CollectionList<User, Role>(getSelected(),model);
		
		final SortedList<Role> sortedRoles=new SortedList<Role>(colList,null);
		final EventTableModel<Role> tm=new EventTableModel<Role>(sortedRoles,tf);
		detalleGrid.setModel(tm);
		
		TableComparatorChooser.install(detalleGrid, sortedRoles, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
		
		roleSelection=new EventSelectionModel<Role>(sortedRoles);
		detalleGrid.setSelectionModel(roleSelection);
		detalleGrid.setColumnControlVisible(false);
		grid.getSelectionModel().addListSelectionListener(new UserSelection());
		
		final JScrollPane sp=new JScrollPane(detalleGrid);
		sp.setPreferredSize(new Dimension(200,150));
		
		SimpleInternalFrame frame=new SimpleInternalFrame("Roles");
		frame.setContent(sp);
		return frame;
	}
	
	private void agregarRol(){
		if(getSelectedObject()!=null){
			User u=(User)getSelectedObject();
			RoleSelector selector=new RoleSelector(u);
			selector.open();
			if(!selector.hasBeenCanceled()){
				u.getRoles().addAll(selector.getSelectedList());
				try {
					model.actualizarUsuario(u);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void eliminarRol(){
		if(!roleSelection.isSelectionEmpty()){
			
			User u=(User)getSelectedObject();
			int index=source.indexOf(u);
			if(index!=-1){
				Role r=roleSelection.getSelected().get(0);
				u.getRoles().remove(r);
				try {
					model.actualizarUsuario(u);
					model.getRoles().remove(r);
				} catch (Exception e) {
					MessageUtils.showMessage("Error acutalizando usuario: "+u
							+ExceptionUtils.getRootCauseMessage(e), "Usuario");
				}
				
			}
		}
	}
	
	private class UserSelection implements ListSelectionListener{

		public void valueChanged(ListSelectionEvent e) {
			if(!e.getValueIsAdjusting()){
				userChannel.setValue(getSelectedObject());
			}			
		}
		
	}
	
	private class RoleSelector extends AbstractSelector<Role>{
		
		private final User currentUser;
		
		public RoleSelector(User user) {
			super(Role.class, "Roles del sistema");
			this.currentUser=user;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected List<Role> getData() {
			model.loadRoles();
			List<Role> roles= model.getRoles();
			final List<Role> faltantes=new ArrayList<Role>();
			for(Role r:roles){
				if(currentUser.getRoles().contains(r))
					continue;
				else
					faltantes.add(r);
			}
			return faltantes;
		}

		@Override
		protected TableFormat<Role> getTableFormat() {
			final String[] names={"id","name","description"};
			final String[] labels={"Id","Nombre","Desc"};
			return GlazedLists.tableFormat(Role.class, names, labels);
		}		
		
		protected JXTable buildGrid(){
			JXTable grid=super.buildGrid();
			selectionModel.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
			return grid;
		}
	}
	
}
