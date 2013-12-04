package com.luxsoft.siipap.inventario.ui.consultas;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanWrapperImpl;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.UniqueList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.matchers.Matcher;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uifextras.panel.HeaderPanel;

import com.luxsoft.siipap.inventario.InventariosActions;

import com.luxsoft.siipap.inventario.ui.reports.InventarioCosteadoReportForm;
import com.luxsoft.siipap.inventario.ui.task.CostosTaskForm;
import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.replica.aop.ExportadorManager;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.matchers.CheckBoxMatcher;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;

/**
 * Consulta base para el mantenimiento de inventarios
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ExistenciasPanel extends FilteredBrowserPanel<Existencia> implements PropertyChangeListener{

	public ExistenciasPanel() {
		super(Existencia.class);
	}
	
	private HeaderPanel header;
	
	
	
	@Override
	protected JComponent buildHeader(){
		if(header==null){
			header=new HeaderPanel("Existencias ","Año:  Mes:");
			updateHeader();
		}
		return header;
	}
	
	private void updateHeader(){
		if(header!=null)
			header.setDescription("Periodo: "+getYear()+"/"+getMes());
	}
	
	public void propertyChange(PropertyChangeEvent evt) {
		updateHeader();
	}



	protected void init(){
		Date fecha=new Date();
		yearModel=new ValueHolder(Periodo.obtenerYear(fecha));
		yearModel.addValueChangeListener(this);
		mesModel=new ValueHolder(Periodo.obtenerMes(fecha));
		mesModel.addValueChangeListener(this);
		updateHeader();
		addProperty(
				"sucursal.nombre"
				,"year"
				,"mes"
				,"clave"
				,"descripcion"
				,"unidad"
				,"nacional"
				,"kilos"
				,"cantidad"
				,"costoPromedio"
				,"costoAPromedio"
				,"costoUltimo"
				,"costoAUltimo"
				,"producto.linea.nombre"
				,"producto.clase.nombre"
				,"producto.marca.nombre"
				);
		addLabels(
				"Sucursal"
				,"Año","Mes"
				,"Producto","Desc","U","Nac","Kilos","Cantidad"
				,"Promedio","Costo P"
				,"Ultimo","Costo U"
				,"Línea","Clase","Marca"
				);	
		
		
		installTextComponentMatcherEditor("Sucursal", "sucursal.nombre");
		installTextComponentMatcherEditor("Producto", "producto.clave","producto.descripcion");		
		installTextComponentMatcherEditor("Linea", "producto.linea.nombre");
		installTextComponentMatcherEditor("Clase", "producto.clase.nombre");
		installTextComponentMatcherEditor("Marca", "producto.marca.nombre");
		installTextComponentMatcherEditor("Costo P", "costop");
		installTextComponentMatcherEditor("Costo U", "costoUltimo");
		installTextComponentMatcherEditor("Existencia", "existencia");
		
		CheckBoxMatcher<Existencia> nacionalMatcher=new CheckBoxMatcher<Existencia>(){			
			protected Matcher<Existencia> getSelectMatcher(Object... obj) {				
				return new Matcher<Existencia>(){
					public boolean matches(Existencia item) {						
						boolean res=item.isNacional();
						return res;
					}					
				};
			}			
		};
		nacionalMatcher.getBox().setSelected(true);
		//installCustomMatcherEditor("Por recibir CxC", recibidaMatcher.getBox(), recibidaMatcher);
		installCustomMatcherEditor("Nacional", nacionalMatcher.getBox(), nacionalMatcher);
		
		CheckBoxMatcher<Existencia> importadoMatcher=new CheckBoxMatcher<Existencia>(){			
			protected Matcher<Existencia> getSelectMatcher(Object... obj) {				
				return new Matcher<Existencia>(){
					public boolean matches(Existencia item) {						
						boolean res=item.isNacional();
						return !res;
					}					
				};
			}			
		};
		importadoMatcher.getBox().setSelected(true);
		installCustomMatcherEditor("Importado", importadoMatcher.getBox(), importadoMatcher);
		
	}

	@Override
	protected List<Action> createProccessActions() {
		List<Action> procesos=super.createProccessActions();
		procesos.add(addAction(InventariosActions.CalculoDeCostos.getId(), "actualizarExistencias", "Actualizar Existencias"));
		procesos.add(addAction("", "exportarExistencia", "Exportar existencia"));
		procesos.add(addAction(InventariosActions.ConsultaDeCostosDeInventario.getId(), "reporteDeKardex", "Kardex"));
		return procesos;
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getViewAction()
				,addAction(InventariosActions.ConsultaDeCostosDeInventario.getId(), "reporteInventarioCosteado", "Rep Inv. Costeado")
				};
		return actions;
	}
	
		
	public void load(){
		AbstractDialog dialog=Binder.createSelectorMesYear(yearModel, mesModel);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			super.load();
		}
		return;
	}
	
	public void refresh(){
		super.load();
	}
	
	public void actualizarExistencias(){
		final CostosTaskForm form=new CostosTaskForm();
		form.open();
		if(!form.hasBeenCanceled()){
			final int year=form.getYear();
			yearModel.setValue(new Integer(year));
			final int mes=form.getMes();
			mesModel.setValue(new Integer(mes));
			final String clave=form.getProductoClave();
			
			/*
			if("%".equals(clave)){
				executeLoadWorker(new ActualizadorDeExistencias(form.getSucursal().getId()));
			}
			else{
				executeLoadWorker(new ActualizadorDeExistencias(false,form.getSucursal().getId()));
				
			}
			*/
			final SwingWorker worker=new SwingWorker(){				
				protected Object doInBackground() throws Exception {
					if(form.getTodasLasSucursales().isSelected()){
						if(form.getTodosBox().isSelected()){
							ServiceLocator2.getExistenciaDao().actualizarExistencias(year, mes);
						}else{
							ServiceLocator2.getExistenciaDao().actualizarExistencias(clave,year, mes);
						}
					}else{
						if(form.getTodosBox().isSelected()){
							ServiceLocator2.getExistenciaDao().actualizarExistencias(form.getSucursal().getId(), year, mes);
						}else{
							ServiceLocator2.getExistenciaDao().actualizarExistencias(form.getSucursal().getId(),clave,year, mes);
						}
					}
					return null;
				}
				
			};
			TaskUtils.executeSwingWorker(worker);
		}
	}
	
	public void exportarExistencia(){
		for(Object o:getSelected()){
			Existencia exis=(Existencia)o;
			exportarExistencia(exis);
		}
	}
	
	public void exportarExistencia(Existencia exis){
		String res=ExportadorManager.getInstance().exportarExistencia(exis, null);
		if(res!=null)
			MessageUtils.showMessage("Existencia exportada. Archivo:\n"+res, "Exportador de existencias");
	}
	
	protected void executeLoadWorker(final SwingWorker worker){
		TaskUtils.executeSwingWorker(worker);
	}

	@Override
	protected List<Existencia> findData() {
		String hql="from Existencia e where e.year=? and e.mes=? and e.producto.inventariable=true";
		return ServiceLocator2.getHibernateTemplate().find(hql, new Object[]{getYear(),getMes()});
	}
	
	public void reporteInventarioCosteado(){
		InventarioCosteadoReportForm.run();
	}
	
	private ValueModel yearModel;
	
	private ValueModel mesModel;

	
	public Integer getMes(){
		return (Integer)mesModel.getValue();
	}
	public Integer getYear(){
		return (Integer)yearModel.getValue();
	}
	
	private TotalesPanel totalPanel;
	
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			totalPanel=new TotalesPanel();
		}
		return (JPanel)totalPanel.getControl();
	}
	
	private class ActualizadorDeExistencias extends SwingWorker{
		
		private final  boolean todos;
		private final  Long sucursalId;
		
		
		public ActualizadorDeExistencias(Long sucursalId) {
			this.todos=true;
			this.sucursalId=sucursalId;
		}
		
		public ActualizadorDeExistencias(boolean todos,Long sucursalId) {			
			this.todos = todos;
			this.sucursalId=sucursalId;
		}

		@Override
		protected Object doInBackground() throws Exception {
			if(todos)
				ServiceLocator2.getExistenciaDao().actualizarExistencias(sucursalId,getYear(),getMes());
			else{
				UniqueList rows=new UniqueList(getSelected(),GlazedLists.beanPropertyComparator(Existencia.class, "clave"));
				System.out.println("Existencias a actualizar");
				for(Object o:rows){
					Existencia e=(Existencia)o;
					ServiceLocator2.getExistenciaDao().actualizarExistencias(sucursalId,e.getClave(), getYear(), getMes());
				}
			}	
			return "OK";
		}

		@Override
		protected void done() {
			try {
				get();
				load();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			
		}
		
		
	}
	
	private class TotalesPanel extends AbstractControl implements ListEventListener{
		
		private JLabel costo=new JLabel();
		private JLabel costoUltimo=new JLabel();
		private JLabel kilos=new JLabel();
		private JLabel existencia=new JLabel();
		

		public TotalesPanel() {
			super();
			sortedSource.addListEventListener(this);
		}

		@Override
		protected JComponent buildContent() {
			
			costo.setHorizontalAlignment(JLabel.RIGHT);
			costoUltimo.setHorizontalAlignment(JLabel.RIGHT);
			kilos.setHorizontalAlignment(JLabel.RIGHT);
			existencia.setHorizontalAlignment(JLabel.RIGHT);
			
			final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			costo.setHorizontalAlignment(SwingConstants.RIGHT);
			
			builder.append("Saldo Ini",existencia);
			builder.append("Kilos",kilos);
			builder.append("Costo P",costo);
			builder.append("Costo U",costoUltimo);
			
			
			builder.getPanel().setOpaque(false);
			getFilteredSource().addListEventListener(this);
			updateTotales();
			return builder.getPanel();
		}
		
		public void listChanged(ListEvent listChanges) {
			if(listChanges.next()){
				
			}
			updateTotales();
		}
		
		NumberFormat nf1=NumberFormat.getCurrencyInstance();
		NumberFormat nf2=NumberFormat.getNumberInstance();
		
		public void updateTotales(){
			//Cantidad monetaria para redondear
			
			CantidadMonetaria costop=CantidadMonetaria.pesos(sumarizar("costoAPromedio"));
			CantidadMonetaria costou=CantidadMonetaria.pesos(sumarizar("costoAUltimo"));
			BigDecimal cantidad=sumarizar("cantidad");
			BigDecimal kg=sumarizar("kilos");
			
			costo.setText(nf1.format(costop.amount().doubleValue()));
			costoUltimo.setText(nf1.format(costou.amount().doubleValue()));
			existencia.setText(nf2.format(cantidad.doubleValue()));
			kilos.setText(nf2.format(kg.doubleValue()));
			
		}
		
		private BigDecimal sumarizar(String property){
			BigDecimal res=BigDecimal.ZERO;
			for(Object bean:sortedSource){
				res=res.add(getValor(bean, property));
			}
			return res;
		}
		
		private BigDecimal getValor(Object bean,String property){
			return BigDecimal.valueOf(getValorAsNumber(bean, property).doubleValue());
		}
		
		private Number getValorAsNumber(Object bean,String property){
			BeanWrapperImpl wrapper=new BeanWrapperImpl(bean);
			return (Number)wrapper.getPropertyValue(property);
		}
		
		private NumberFormat nf=NumberFormat.getPercentInstance();
		
		protected String part(final CantidadMonetaria total,final CantidadMonetaria part){
			
			double res=0;
			if(total.amount().doubleValue()>0){
				res=part.divide(total.amount()).amount().doubleValue();
			}
			return StringUtils.leftPad(nf.format(res),5);
		}
		
	}
	
	

}
