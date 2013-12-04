package com.luxsoft.sw3.embarques.ui.consultas;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jdesktop.swingx.JXTable;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uif.panel.SimpleInternalFrame;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.sw3.embarque.Chofer;
import com.luxsoft.sw3.embarque.ChoferFacturista;
import com.luxsoft.sw3.embarque.EmbarquesRoles;
import com.luxsoft.sw3.embarques.ui.catalogos.ChoferesBrowser;
import com.luxsoft.sw3.embarques.ui.forms.FacturistaForm;
import com.luxsoft.sw3.embarques.ui.selectores.SelectorDeChoferes;

public class FacturistasPanel extends AbstractMasterDatailFilteredBrowserPanel<ChoferFacturista, Chofer>{

	public FacturistasPanel() {
		super(ChoferFacturista.class);
		setTitle("Facturistas");		
	}	

	@Override
	protected void agregarMasterProperties() {
		super.agregarMasterProperties();
		addProperty("id","nombre","telefono1","telefono2","rfc");
		addLabels("Id","Nombre","Tel 1","Tel 2","RFC");
	}


	@Override
	protected TableFormat createDetailTableFormat() {
		String props[]={"facturista.id","id","nombre","radio","rfc"};
		String names[]={"Facturista","Id","Nombre","Radio","RFC"};
		return GlazedLists.tableFormat(Chofer.class, props,names);
	}

	@Override
	protected Model<ChoferFacturista, Chofer> createPartidasModel() {
		return new Model<ChoferFacturista, Chofer>(){
			public List<Chofer> getChildren(ChoferFacturista parent) {
				String hql="from Chofer c where c.facturista.id=?";
				return getHibernateTemplate().find(hql,parent.getId());
			}			
		};
	}
	
	@Override
	public Action[] getActions() {
		DispatchingAction a=new DispatchingAction(this,"choferes");
		a.putValue(Action.NAME, "Choferes");
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getInsertAction()
				,getDeleteAction()
				,getEditAction()
				,getViewAction()
				//,addRoleBasedContextAction(null, EmbarquesRoles.ContralorDeEmbarques.getId(), this, "choferes", "Choferes")
				//,addAction("",  "choferes", "Choferes")
				,a
				};
		return actions;
	}
	
	
	
	public JComponent buildDetailGridPanel(JXTable detailGrid){
		JScrollPane sp=new JScrollPane(detailGrid);
		ToolBarBuilder builder=new ToolBarBuilder();
		
		final Action agregar=addContextAction(new SelectionPredicate(), "", "registrarChofer", "Agregar chofer");
		agregar.putValue(Action.LONG_DESCRIPTION, "Elimina un objeto");
		agregar.putValue(Action.SMALL_ICON, getIconFromResource("images2/car_add.png"));
		
		final Action eliminar=addContextAction(new SelectionPredicate(), "", "removerChofer", "Remover chofer");
		eliminar.putValue(Action.LONG_DESCRIPTION, "Remover chofer");
		eliminar.putValue(Action.SMALL_ICON, getIconFromResource("images2/car_delete.png"));
		
		builder.add(agregar);
		builder.add(eliminar);
		
		SimpleInternalFrame frame=new SimpleInternalFrame(
				"Choferes asignados",builder.getToolBar(),sp);		
		return frame;
	}
	
	
	
	@Override
	protected ChoferFacturista doInsert() {
		ChoferFacturista bean=new ChoferFacturista();
		final DefaultFormModel model=new DefaultFormModel(bean);
		FacturistaForm form=new FacturistaForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return save((ChoferFacturista)model.getBaseBean());
		}
		return null;
	}
	
	@Override
	protected ChoferFacturista doEdit(final ChoferFacturista bean) {
		final ChoferFacturista target=getFacturista(bean.getId());
		final DefaultFormModel model=new DefaultFormModel(target);
		FacturistaForm form=new FacturistaForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return save((ChoferFacturista)model.getBaseBean());
		}
		return bean;
	}

	

	@Override
	protected void doSelect(Object bean) {
		final DefaultFormModel model=new DefaultFormModel(bean,true);
		FacturistaForm form=new FacturistaForm(model);
		form.open();		
	}
	
	@Override
	public boolean doDelete(final ChoferFacturista bean) {
		
		Long res=(Long)getHibernateTemplate().execute(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				List<ChoferFacturista> data=session.createQuery("from ChoferFacturista f left join fetch f.choferes c where f.id=?")
				.setLong(0, bean.getId())
				.list();
				if(!data.isEmpty()){
					ChoferFacturista target=data.get(0);
					for(Chofer c:target.getChoferes()){
						c.setFacturista(null);
					}
					session.delete(target);
					return target.getId();
				}
				
				return null;
			}
		});
		return res!=null;
	}

	

	public void choferes(){
		ChoferesBrowser.openDialog();
	}
	
	
	
	public void registrarChofer(){
		ChoferFacturista facturista=(ChoferFacturista)getSelectedObject();
		facturista=getFacturista(facturista.getId());
		int index=source.indexOf(facturista);
		if(index!=-1){
			if(facturista!=null){
				List<Chofer> choferes=SelectorDeChoferes.seleccionar(facturista);
				for(Chofer c:choferes){
					facturista.agregarChofer(c);
				}
			}
			facturista=save(facturista);
			source.set(index, facturista);
			selectionModel.setSelectionInterval(index, index);
		}
	}
	
	public void removerChofer(){
		if(!detailSelectionModel.isSelectionEmpty()){
			List<Chofer> choferes=new ArrayList<Chofer>(detailSelectionModel.getSelected());
			for(Chofer c:choferes){
				c.setFacturista(null);
				getHibernateTemplate().merge(c);
			}
			selectionModel.clearSelection();
		}
	}
	
	private ChoferFacturista save(ChoferFacturista bean){		
		
		Date time=ServiceLocator2.obtenerFechaDelSistema();
		
		bean.setReplicado(null);
		
		String user=KernellSecurity.instance().getCurrentUserName();
		String ip=KernellSecurity.getIPAdress();
		String mac=KernellSecurity.getMacAdress();
		
		bean.getLog().setUpdateUser(user);
		bean.getLog().setModificado(time);
		if(bean.getId()!=null){
			bean.getLog().setCreado(time);
			bean.getLog().setCreateUser(user);
		}
		return (ChoferFacturista)getHibernateTemplate().merge(bean);
	}
	
	private ChoferFacturista getFacturista(Long id){
		List<ChoferFacturista> res=getHibernateTemplate().find("from ChoferFacturista f left join fetch f.choferes c where f.id=?",id);
		return res.isEmpty()?null:res.get(0);
	}
	
	
	protected HibernateTemplate getHibernateTemplate(){
		return ServiceLocator2.getHibernateTemplate();
	}

}
