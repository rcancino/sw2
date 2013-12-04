package com.luxsoft.sw3.bi.consultas;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;

import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.UniqueList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.Matchers;

import com.luxsoft.siipap.cxc.CXCRoles;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.sw3.bi.SimuladorDePreciosPorCliente;
import com.luxsoft.sw3.bi.SimuladorDePreciosPorClienteDet;
import com.luxsoft.sw3.bi.form.SimuladorDePreciosClienteForm;
import com.luxsoft.sw3.bi.form.SimuladorDePreciosClienteFormModel;


public class SimuladorDePreciosClientePanel extends AbstractMasterDatailFilteredBrowserPanel<SimuladorDePreciosPorCliente, SimuladorDePreciosPorClienteDet>{
	
	
	
	public SimuladorDePreciosClientePanel() {
		super(SimuladorDePreciosPorCliente.class);
		init();
	}
	
	protected void init(){			
		addProperty("id","cliente.clave","cliente.nombre","fechaInicial","fechaFinal","cliente.credito.descuentoEstimado","descuento","tipoPrecio","tipoCosto","comentario");
		addLabels("Id","clave","Nombre","FechaInicial","FechaFinal","Desc Fijo","Descuento","Tipo Precio","Tipo Costo","Comentario");		
		installTextComponentMatcherEditor("Cliente", new String[]{"cliente.clave","cliente.nombre"});
		installTextComponentMatcherEditor("Id", new String[]{"id"});
		//manejarPeriodo();
	}


	@Override
	protected void installEditors(EventList editors) {
		Matcher<SimuladorDePreciosPorCliente> m=Matchers.beanPropertyMatcher(SimuladorDePreciosPorCliente.class,"comentario", "ELIMINADA");
		m=Matchers.invert(m);
		MatcherEditor<SimuladorDePreciosPorCliente> e1=GlazedLists.fixedMatcherEditor(m);
		editors.add(e1);
		super.installEditors(editors);
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		final String[] cols={
				 "producto.clave"
				,"producto.descripcion"				
				,"producto.unidad.nombre"
				,"precioDeLista"
				,"descuento"
				,"precioNeto"
				,"costo"
				,"diferencia"
				,"margen"
				,"ventaAcumulada"
				,"ventaPeriodoAnterior"
				};
		final String[] names={
				"Producto"
				,"Descripción"
				,"U"
				,"Precio L"
				,"Desc"				
				,"Precio Neto"
				,"Costo"
				,"Diferencia"
				,"Margen"
				,"Venta"
				,"Venta PA"
				};
		final TableFormat<SimuladorDePreciosPorClienteDet> tf=GlazedLists.tableFormat(SimuladorDePreciosPorClienteDet.class, cols,names);
		return tf;
	}

	
	protected Model<SimuladorDePreciosPorCliente, SimuladorDePreciosPorClienteDet> createPartidasModel() {
		final Model<SimuladorDePreciosPorCliente, SimuladorDePreciosPorClienteDet> model=new Model<SimuladorDePreciosPorCliente, SimuladorDePreciosPorClienteDet>(){
			public List<SimuladorDePreciosPorClienteDet> getChildren(SimuladorDePreciosPorCliente parent) {				
				return new ArrayList<SimuladorDePreciosPorClienteDet>(parent.getPrecios());
			}
		};
		return model;
	}
	
	
	
	protected EventList getSourceEventList() {
		Comparator<SimuladorDePreciosPorCliente> c=GlazedLists.beanPropertyComparator(SimuladorDePreciosPorCliente.class, "id");
		UniqueList<SimuladorDePreciosPorCliente> data=new UniqueList<SimuladorDePreciosPorCliente>(super.getSourceEventList(),c);
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
				,getSecuredInsertAction(CXCRoles.GERENCIA_DE_CREDITO.name())
				,getSecuredDeleteAction(CXCRoles.GERENCIA_DE_CREDITO.name())
				,getSecuredEditAction(CXCRoles.GERENCIA_DE_CREDITO.name())
				,getViewAction()};
		return actions;
	}
	
	@Override
	protected List<Action> createProccessActions() {		
		List<Action> procesos= super.createProccessActions();
		//procesos.add(addRoleBasedContextAction(null, CXCRoles.GERENCIA_DE_CREDITO.name(), this,"copiar", "Copiar"));
		procesos.add(addAction(null, "imprimir", "Imprimir Lista"));
		return procesos;
	}

	@Override
	protected List<SimuladorDePreciosPorCliente> findData() {
		return ServiceLocator2.getHibernateTemplate().find("from SimuladorDePreciosPorCliente");
	}
	
	@Override
	public boolean doDelete(SimuladorDePreciosPorCliente bean) {			
		ServiceLocator2.getListaDePreciosClienteManager().remove(bean.getId());
		return true;
	}
	
	public void open(){
		//load();
	}
	
	@Override
	protected SimuladorDePreciosPorCliente doEdit(SimuladorDePreciosPorCliente bean) {
		SimuladorDePreciosPorCliente target=(SimuladorDePreciosPorCliente)ServiceLocator2.getHibernateTemplate().get(SimuladorDePreciosPorCliente.class,bean.getId());
		
		SimuladorDePreciosClienteFormModel controller=new SimuladorDePreciosClienteFormModel(target);
		SimuladorDePreciosClienteForm form=new SimuladorDePreciosClienteForm(controller);
		form.open();
		if(!form.hasBeenCanceled()){
			SimuladorDePreciosPorCliente res=controller.getLista();
			res=ServiceLocator2.getSimuladorDePreciosManager().save(res);
			return res;
		}
		return bean;
	}
	
	protected SimuladorDePreciosPorCliente doInsert() {
		
		SimuladorDePreciosPorCliente res=SimuladorDePreciosClienteForm.showForm();
		if(res!=null){
			res=ServiceLocator2.getSimuladorDePreciosManager().save(res);
			return res;
		}else
			return null;
	}
	
	@Override
	protected void doSelect(Object bean) {
		
		SimuladorDePreciosClienteFormModel controller=new SimuladorDePreciosClienteFormModel((SimuladorDePreciosPorCliente)bean);
		SimuladorDePreciosClienteForm form=new SimuladorDePreciosClienteForm(controller);
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
		SimuladorDePreciosPorCliente l=(SimuladorDePreciosPorCliente)getSelectedObject();
		if(l!=null){
			final Map parameters=new HashMap();
			parameters.put("FOLIO", l.getId());
			parameters.put("CLIENTE", l.getCliente().getNombre());
			parameters.put("DESCUENTO", l.getDescuento());
			parameters.put("FECHA_INI", l.getFechaInicial());
			parameters.put("FECHA_FIN", l.getFechaFinal());
			parameters.put("TIPO_PRECIO", l.getTipoPrecio().name());
			parameters.put("TIPO_COSTO", l.getTipoCosto().name());
			parameters.put("COMENTARIO", l.getComentario());
			parameters.put("LINEA", "TODAS");
			
			ReportUtils.viewReport(ReportUtils.toReportesPath("bi/SimuladorDePreciosTodos.jasper"), parameters);
			
		}
	}

}
