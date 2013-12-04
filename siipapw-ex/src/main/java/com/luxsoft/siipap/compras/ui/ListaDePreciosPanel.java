package com.luxsoft.siipap.compras.ui;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JOptionPane;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.UniqueList;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.Matchers;

import com.luxsoft.siipap.compras.dao.ListaDePreciosDao;
import com.luxsoft.siipap.compras.model.ListaDePrecios;
import com.luxsoft.siipap.compras.model.ListaDePreciosDet;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.matchers.CheckBoxMatcher;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;


public class ListaDePreciosPanel extends AbstractMasterDatailFilteredBrowserPanel<ListaDePrecios, ListaDePreciosDet>{

	CheckBoxMatcher<ListaDePrecios> vigenteMatcher;
	
	public ListaDePreciosPanel() {
		super(ListaDePrecios.class);
		init();
	}
	
	protected void init(){			
		addProperty("id","proveedor.clave","proveedor.nombre","fechaInicial","fechaFinal","descripcion","oldId");
		addLabels("Id","Proveedor","Nombre","fechaInicial","fechaFinal","descripcion","Old Id");
		installTextComponentMatcherEditor("Proveedor", new String[]{"proveedor.clave","proveedor.nombre"});
		installTextComponentMatcherEditor("Id", new String[]{"id"});
		installTextComponentMatcherEditor("Old Id", new String[]{"oldId"});
		vigenteMatcher=new CheckBoxMatcher<ListaDePrecios>(false){
			@Override
			protected Matcher<ListaDePrecios> getSelectMatcher(Object... obj) {
				return Matchers.beanPropertyMatcher(ListaDePrecios.class, "vigente", Boolean.TRUE);
			}
		};
		installCustomMatcherEditor("Vigentes", vigenteMatcher.getBox(), vigenteMatcher);
		//manejarPeriodo();
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		final String[] cols={"producto.clave","producto.descripcion","precio","costo","CostoConCargo","costoUltimo"};
		final String[] names={"Producto","Descripción","Precio","Costo","C.Cargo","Costo U"};
		final TableFormat<ListaDePreciosDet> tf=GlazedLists.tableFormat(ListaDePreciosDet.class, cols,names);
		return tf;
	}
	
	@Override
	protected void installEditors(EventList editors) {
		Matcher<ListaDePrecios> m=Matchers.beanPropertyMatcher(ListaDePrecios.class,"descripcion", "ELIMINADA");
		m=Matchers.invert(m);
		MatcherEditor<ListaDePrecios> e1=GlazedLists.fixedMatcherEditor(m);
		editors.add(e1);
		super.installEditors(editors);
	}

	@Override
	protected Model<ListaDePrecios, ListaDePreciosDet> createPartidasModel() {
		final Model<ListaDePrecios, ListaDePreciosDet> model=new Model<ListaDePrecios, ListaDePreciosDet>(){
			public List<ListaDePreciosDet> getChildren(ListaDePrecios parent) {				
				return ServiceLocator2.getHibernateTemplate().find("from ListaDePreciosDet det where det.lista.id=?",parent.getId());
			}
		};
		return model;
	}

	@Override
	protected EventList getSourceEventList() {
		Comparator<ListaDePrecios> c=GlazedLists.beanPropertyComparator(ListaDePrecios.class, "id");
		UniqueList<ListaDePrecios> data=new UniqueList<ListaDePrecios>(super.getSourceEventList(),c);
		return data;
	}
	/*
	protected void manejarPeriodo(){
		periodo=Periodo.periodoDeloquevaDelYear();
	}
*/
	@Override
	protected List<ListaDePrecios> findData() {
		 return getDao().buscarListasVigentes();
	}
	
	@Override
	public boolean doDelete(ListaDePrecios bean) {			
		getDao().remove(bean.getId());
		return true;
	}

	@Override
	protected ListaDePrecios doEdit(ListaDePrecios bean) {
		ListaDePrecios lp=getDao().get(bean.getId());
		lp=ListaDePreciosForm.showForm(lp,false);
		if(lp!=null){
			lp=getDao().save(lp);
			return lp;
		}
		return null;
	}

	@Override
	protected ListaDePrecios doInsert() {
		ListaDePrecios lp=ListaDePreciosForm.showForm();			
		if(lp!=null){
			lp=getDao().save(lp);
			return lp;
		}
		return null;
	}

	@Override
	protected void doSelect(Object bean) {
		ListaDePrecios selected=(ListaDePrecios)bean;		
		ListaDePreciosForm.showForm(selected,true);
	}
	
	public void activar(boolean val){
		List<ListaDePrecios> selected=new ArrayList<ListaDePrecios>();
		selected.addAll(getSelected());
		selectionModel.clearSelection();
		if(!selected.isEmpty()){
			for(ListaDePrecios l:selected){
				int index=source.indexOf(l);
				ListaDePrecios target=ServiceLocator2.getListaDePreciosDao().get(l.getId());
				target.setVigente(val);
				target=getDao().save(target);
				source.set(index,target);
			}
		}
	}
	
	public void activar(){
		activar(true);
	}
	public void suspender(){
		activar(false);
	}
	
	public void close(){
		super.close();
		System.out.println("Close................");
	}
	
	public void open(){
		load();
	}
	
	public void print(){
		if(getSelectedObject()!=null)
			print((ListaDePrecios)getSelectedObject());
	}
	
	public void print(ListaDePrecios bean){
		Map params=new HashMap();
		params.put("ID", bean.getId());
		int res=JOptionPane.showConfirmDialog(getControl(),"Con descuentos","Lista de precios",JOptionPane.YES_NO_OPTION );
		String path=ReportUtils.toReportesPath("cxp/ListaDePrecios.jasper");
		if(res==JOptionPane.YES_OPTION)
			path=ReportUtils.toReportesPath("cxp/ListaDePreciosDesc.jasper");
		
		if(ReportUtils.existe(path))
			ReportUtils.viewReport(path, params);
		else
			JOptionPane.showMessageDialog(this.getControl()
					,MessageFormat.format("El reporte:\n {0} no existe",path),"Reportes",JOptionPane.ERROR_MESSAGE);
	}
		
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{getLoadAction()
				,getInsertAction()
				,getDeleteAction()
				,getEditAction()
				,getViewAction()
				,addAction("printListaDePrecios", "print", "Imprimir Lista")
				};
		return actions;
	}






	private Action habilitarListas;
	
	private Action inHabilitarListas;
	
	public Action getHabilitarAction(){
		if(habilitarListas==null){
			habilitarListas=new DispatchingAction(this,"activar");
			habilitarListas.putValue(Action.NAME, "Activar");
		}
		
		return habilitarListas;
	}
	
	public Action getInHabilitarAction(){
		if(inHabilitarListas==null){
			inHabilitarListas=new DispatchingAction(this,"suspender");
			inHabilitarListas.putValue(Action.NAME, "Suspender");
		}
		
		return inHabilitarListas;
	}
	
	private Action copiarAction;
	
	public Action getCopiarAction(){
		if(copiarAction==null){
			copiarAction=new DispatchingAction(this,"copiar");
			copiarAction.putValue(Action.NAME, "Copiar");
		}
		return copiarAction;
	}
	
	public void copiar(){
		ListaDePrecios l=(ListaDePrecios)getSelectedObject();
		if(l!=null){
			String pattern=
					"Copiar lista:	{0}\n" +
					"Proveedor:		{1}\n" +
					"Periodo:		{2}\n" +
					"Descripción:	{3}";
			String msg=MessageFormat.format(pattern, l.getId(),l.getProveedor().getNombre(),l.periodo().toString(),l.getDescripcion());			
			boolean res=MessageUtils.showConfirmationMessage(msg,"Copiar Lista de precios");
			if(res){
				ListaDePrecios target=getDao().copiar(l.getId());
				source.add(target);
				int index=sortedSource.indexOf(target);
				//System.out.println("Index: "+index);
				if(index!=-1){					
					selectionModel.clearSelection();
					selectionModel.addSelectionInterval(index, index);
				}
			}
		}
	}
	
	
	public ListaDePreciosDao getDao(){
		return ServiceLocator2.getListaDePreciosDao();
	}

}
