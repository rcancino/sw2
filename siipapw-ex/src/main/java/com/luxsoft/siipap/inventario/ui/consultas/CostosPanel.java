package com.luxsoft.siipap.inventario.ui.consultas;

import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.uif.AbstractDialog;
import com.luxsoft.siipap.inventario.InventariosActions;
import com.luxsoft.siipap.inventario.ui.reports.CostoPromedioReportForm;
import com.luxsoft.siipap.inventario.ui.task.CostosTaskForm;
import com.luxsoft.siipap.inventarios.model.CostoPromedio;
import com.luxsoft.siipap.inventarios.model.CostoPromedioItem;
import com.luxsoft.siipap.inventarios.service.CostosServices;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.TaskUtils;

/**
 * Consulta base para el mantenimiento de inventarios
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CostosPanel extends AbstractMasterDatailFilteredBrowserPanel<CostoPromedio,CostoPromedioItem>{
	
	private JTextField claveField;

	public CostosPanel() {
		super(CostoPromedio.class);
	}
	
	protected void agregarMasterProperties(){
		addProperty(
				"year","mes",
				"clave","producto.descripcion"
				,"producto.unidad.unidad"
				,"existencia"
				,"costop","costoAPromedio"
				,"costoUltimo","costoAUltimo"
				,"producto.linea.nombre","producto.clase.nombre","producto.marca.nombre");
		addLabels(
				"Año","Mes",
				"Producto","Desc","U","Cantidad"
				,"Promedio","Costo P"
				,"Ultimo","Costo U"
				
				,"Línea","Clase","Marca");
		//manejarPeriodo();
		Date fecha=new Date();
		yearModel=new ValueHolder(Periodo.obtenerYear(fecha));
		mesModel=new ValueHolder(Periodo.obtenerMes(fecha));
		
		claveField=new JTextField(10);
		installTextComponentMatcherEditor("Mes", "mes");
		installTextComponentMatcherEditor("Producto",claveField, "producto.clave","producto.descripcion");		
		installTextComponentMatcherEditor("Linea", "producto.linea.nombre");
		
		installTextComponentMatcherEditor("Clase", "producto.clase.nombre");
		installTextComponentMatcherEditor("Marca", "producto.marca.nombre");
		installTextComponentMatcherEditor("Costo P", "costop");
		installTextComponentMatcherEditor("Costo U", "costoUltimo");
		installTextComponentMatcherEditor("Existencia", "existencia");
	}

	@Override
	protected List<Action> createProccessActions() {
		List<Action> procesos=super.createProccessActions();
		procesos.add(addAction(InventariosActions.CalculoDeCostos.getId(), "actualizarCostosPromedios", "Actualizar Costos P"));		
		return procesos;
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getViewAction()
				,addAction(InventariosActions.ConsultaDeCostosDeInventario.getId(), "reporteDECostoPromedio", "Rep Calculo de C. P.")
				};
		return actions;
	}
	
	public void actualizarCostosPromedios(){
		final CostosTaskForm form=new CostosTaskForm();
		form.open();
		if(!form.hasBeenCanceled()){
			int year=form.getYear();
			int mes=form.getMes();
			String clave=form.getProductoClave();
			if("%".equals(clave)){
				ActualizarCostosPromedios task=new ActualizarCostosPromedios(year,mes);
				//TaskUtils.executeSwingWorker(task);
				TaskUtils.executeSwingWorkerInDialog(task, "Costos", "Actualizando costos promedio");
			}
			else{
				ActualizarCostosPorClave taks=new ActualizarCostosPorClave(clave,year,mes);
				TaskUtils.executeSwingWorker(taks);
				
			}
		}
	}
	
	public CostosServices getManager(){
		return ServiceLocator2.getCostosServices();
	}
		
	public void load(){
		AbstractDialog dialog=Binder.createSelectorYear(yearModel);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			super.load();
		}
		return;
	}
	
	
	public void reporteDECostoPromedio(){
		CostoPromedioReportForm.run();
	}

	@Override
	protected List<CostoPromedio> findData() {
		String hql="from CostoPromedio cp where cp.year=? and cp.producto.inventariable=true";
		//return ServiceLocator2.getCostoPromedioManager().buscarCostosPromedios(getYear(), getMes());
		return ServiceLocator2.getHibernateTemplate().find(hql, getYear());
	}
	
	public void refrescarArticulo(){
		
	}
	
	
	private ValueModel yearModel;
	
	private ValueModel mesModel;

	
	public Integer getMes(){
		return (Integer)mesModel.getValue();
	}
	public Integer getYear(){
		return (Integer)yearModel.getValue();
	}
	
	

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={"tipo","documento","fecha","clave","descripcion","unidad","cantidad","costo"};
		String[] names={"Tipo","Docto","Fecha","Prod","Desc","Uni","Cant","Costo"};
		return GlazedLists.tableFormat(CostoPromedioItem.class,props, names);
	}

	@Override
	protected Model<CostoPromedio, CostoPromedioItem> createPartidasModel() {
		return new CollectionList.Model<CostoPromedio, CostoPromedioItem>(){
			public List<CostoPromedioItem> getChildren(CostoPromedio parent) {
				return ServiceLocator2.getCostoPromedioManager().buscarCostoItems(parent.getClave(), parent.getYear(), parent.getMes());
			}
			
		};
	}
	
	
	private class ActualizarCostosPorClave extends SwingWorker{
		
		private final int year;
		private final int mes;
		private final String clave;

		public ActualizarCostosPorClave(String clave,  int year,int mes) {
			this.clave = clave;
			this.year = year;
			this.mes = mes;
		}

		@Override
		protected Object doInBackground() throws Exception {
			getManager().actualizarCostosPromedio(clave, year, mes);
			return null;
		}
		
	}
	
	
	private class ActualizarCostosPromedios extends SwingWorker{
		
		private final int year;
		private final int mes;
		

		public ActualizarCostosPromedios(int year,int mes) {			
			this.year = year;
			this.mes = mes;
		}

		@Override
		protected Object doInBackground() throws Exception {
			getManager().actualizarCostosAPromedio(year, mes);
			return null;
		}
		
	}
	

}
