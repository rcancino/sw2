package com.luxsoft.sw3.cxc.consultas;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.UniqueList;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.Matchers;

import com.luxsoft.siipap.cxc.CXCRoles;
import com.luxsoft.siipap.cxc.ui.selectores.SelectorDeClientes;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.matchers.CheckBoxMatcher;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.ventas.model.ListaDePreciosCliente;
import com.luxsoft.siipap.ventas.model.ListaDePreciosClienteDet;
import com.luxsoft.sw3.cxc.forms.ListaDePreciosPorClienteForm;
import com.luxsoft.sw3.cxc.forms.ListaDePreciosPorClienteFormModel;

public class ListaDePreciosClientePanel extends AbstractMasterDatailFilteredBrowserPanel<ListaDePreciosCliente, ListaDePreciosClienteDet>{
	
	CheckBoxMatcher<ListaDePreciosCliente> vigenteMatcher;
	
	public ListaDePreciosClientePanel() {
		super(ListaDePreciosCliente.class);
		init();
	}
	
	protected void init(){			
		addProperty("id","cliente.clave","cliente.nombre","fechaInicial","fechaFinal","cliente.credito.descuentoEstimado","activo","comentario");
		addLabels("Id","clave","Nombre","FechaInicial","FechaFinal","Desc Fijo","Activo","Comentario");		
		installTextComponentMatcherEditor("Cliente", new String[]{"cliente.clave","cliente.nombre"});
		installTextComponentMatcherEditor("Id", new String[]{"id"});
		vigenteMatcher=new CheckBoxMatcher<ListaDePreciosCliente>(false){
			@Override
			protected Matcher<ListaDePreciosCliente> getSelectMatcher(Object... obj) {
				return Matchers.beanPropertyMatcher(ListaDePreciosCliente.class, "activo", Boolean.TRUE);
			}
		};
		installCustomMatcherEditor("Activas", vigenteMatcher.getBox(), vigenteMatcher);
		//manejarPeriodo();
	}


	@Override
	protected void installEditors(EventList editors) {
		Matcher<ListaDePreciosCliente> m=Matchers.beanPropertyMatcher(ListaDePreciosCliente.class,"comentario", "ELIMINADA");
		m=Matchers.invert(m);
		MatcherEditor<ListaDePreciosCliente> e1=GlazedLists.fixedMatcherEditor(m);
		editors.add(e1);
		super.installEditors(editors);
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		final String[] cols={"producto.clave","producto.descripcion"
				,"producto.precioCredito"
				,"descuento"
				,"precioFactura"
				,"precioNeto"
				//,"costo","diferencia","margen"
				};
		final String[] names={"Producto","Descripción"
				,"Precio"
				,"Desc"
				,"Precio Fac"
				,"Precio Neto"
				//,"Costo","Diferencia","Margen"
				};
		final TableFormat<ListaDePreciosClienteDet> tf=GlazedLists.tableFormat(ListaDePreciosClienteDet.class, cols,names);
		return tf;
	}

	
	protected Model<ListaDePreciosCliente, ListaDePreciosClienteDet> createPartidasModel() {
		final Model<ListaDePreciosCliente, ListaDePreciosClienteDet> model=new Model<ListaDePreciosCliente, ListaDePreciosClienteDet>(){
			public List<ListaDePreciosClienteDet> getChildren(ListaDePreciosCliente parent) {				
				return new ArrayList<ListaDePreciosClienteDet>(parent.getPrecios());
			}
		};
		return model;
	}
	
	JCheckBox vigentesBox;
	
	public JComponent[] getOperacionesComponents(){
		if(vigentesBox==null){
			vigentesBox=new JCheckBox("Vigentes",true);
			vigentesBox.setOpaque(false);
		}
		return new JComponent[]{vigentesBox};
	}
	
	protected EventList getSourceEventList() {
		Comparator<ListaDePreciosCliente> c=GlazedLists.beanPropertyComparator(ListaDePreciosCliente.class, "id");
		UniqueList<ListaDePreciosCliente> data=new UniqueList<ListaDePreciosCliente>(super.getSourceEventList(),c);
		return data;
	}
	
	protected void manejarPeriodo(){
		periodo=Periodo.periodoDeloquevaDelYear();
	}	

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				//,getSecuredInsertAction(
				,getRoleBasedInsertAction(CXCRoles.DIRECCION_COMERCIAL.name())
				,getRoleBasedEditAction(CXCRoles.DIRECCION_COMERCIAL.name())
				,getRoleBasedDeleteAction(CXCRoles.DIRECCION_COMERCIAL.name())
				//,getSecuredDeleteAction(CXCRoles.DIRECCION_COMERCIAL.name())
				//,getSecuredEditAction(CXCRoles.DIRECCION_COMERCIAL.name())
				,getViewAction()};
		return actions;
	}
	
	@Override
	protected List<Action> createProccessActions() {		
		List<Action> procesos= super.createProccessActions();
		//procesos.add(addRoleBasedContextAction(null, CXCRoles.GERENCIA_DE_CREDITO.name(), this,"copiar", "Copiar"));
		procesos.add(addRoleBasedContextAction(null, CXCRoles.LISTA_DE_PRECIOS_CLIENTES.name(), this,"imprimir", "Imprimir Lista"));
		return procesos;
	}

	/*@Override
	protected List<ListaDePreciosCliente> findData() {
		return ServiceLocator2.getCXCManager().buscarListasDePrecios(periodo);
	}*/
	
	@Override
	public boolean doDelete(ListaDePreciosCliente bean) {			
		ServiceLocator2.getListaDePreciosClienteManager().remove(bean.getId());
		return true;
	}
	
	public void open(){
		load();
	}
	
	@Override
	protected ListaDePreciosCliente doEdit(ListaDePreciosCliente bean) {
		ListaDePreciosCliente target=ServiceLocator2.getListaDePreciosClienteManager().get(bean.getId());
		ListaDePreciosPorClienteFormModel controller=new ListaDePreciosPorClienteFormModel(target);
		ListaDePreciosPorClienteForm form=new ListaDePreciosPorClienteForm(controller);
		form.open();
		if(!form.hasBeenCanceled()){
			ListaDePreciosCliente res=controller.commit();
			res=ServiceLocator2.getListaDePreciosClienteManager().save(res);
			return res;
		}
		return bean;
	}
	
	protected ListaDePreciosCliente doInsert() {
		ListaDePreciosPorClienteFormModel controller=new ListaDePreciosPorClienteFormModel();
		ListaDePreciosPorClienteForm form=new ListaDePreciosPorClienteForm(controller);
		form.open();
		if(!form.hasBeenCanceled()){
			ListaDePreciosCliente res=controller.commit();
			res=ServiceLocator2.getListaDePreciosClienteManager().save(res);
			return res;
		}
		return null;
	}
	
	@Override
	protected void doSelect(Object bean) {
		ListaDePreciosPorClienteFormModel controller=new ListaDePreciosPorClienteFormModel((ListaDePreciosCliente)bean);
		ListaDePreciosPorClienteForm form=new ListaDePreciosPorClienteForm(controller);
		controller.setReadOnly(true);
		form.open();
	}	
	/*	
	public void copiar(){
		ListaDePreciosCliente l=(ListaDePreciosCliente)getSelectedObject();
		if(l!=null){
			Cliente c=SelectorDeClientes.seleccionar();
			if(c.getCredito()==null){
				MessageUtils.showMessage("El cliente debe ser de credito"
						, "Selección de cliente");
				return;
			}
			String pattern=
					"Copiar lista:	{0}\n" +
					"Cliente origen :		{1}\n" +
					"Cliente destino:		{2}\n" +
					"Periodo:               {3,date,short} al {4,date,short}\n"
					;
			String msg=MessageFormat.format(pattern, l.getId()
					,l.getCliente().getNombreRazon()
					,c.getNombreRazon()
					,l.getFechaInicial(),l.getFechaFinal()
					);
								
			boolean res=MessageUtils.showConfirmationMessage(msg,"Copiar Lista de precios");
			if(res){
				
				ListaDePreciosCliente target=ServiceLocator2
					.getListaDePreciosClienteManager()
					.copiarListaDePrecios(l.getId(), c); 
				source.add(target);
				int index=sortedSource.indexOf(target);
				if(index!=-1){					
					selectionModel.clearSelection();
					selectionModel.addSelectionInterval(index, index);
				}
			}
		}
	}
	*/
	public void imprimir(){
		ListaDePreciosCliente l=(ListaDePreciosCliente)getSelectedObject();
		if(l!=null){
			Map map=new HashMap();
			map.put("LISTA_ID", l.getId());
			ReportUtils.viewReport(ReportUtils
					.toReportesPath("cxc/ListaDePreciosPorCliente.jasper"), map);
		}
	}

}
