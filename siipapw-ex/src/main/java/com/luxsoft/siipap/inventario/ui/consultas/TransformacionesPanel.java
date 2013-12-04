package com.luxsoft.siipap.inventario.ui.consultas;

import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanWrapperImpl;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.inventario.InventariosActions;
import com.luxsoft.siipap.inventarios.model.TransformacionDet;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;

/**
 * Consulta base para el mantenimiento de inventarios
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class TransformacionesPanel extends FilteredBrowserPanel<TransformacionDet>{

	public TransformacionesPanel() {
		super(TransformacionDet.class);
	}
	
	protected void init(){
		addProperty(
				"sucursal.nombre"
				,"conceptoOrigen"
				,"clave"
				,"descripcion"
				,"unidad.unidad"
				,"factor"
				,"renglon"
				,"kilos"
				,"cantidad"				
				,"costoOrigen"
				,"gastos"
				,"costo"
				,"costoTotal"
				,"fecha"
				,"year"
				,"mes"
				,"origen.clave"
				,"origen.documento"
				,"destino.clave"
				,"destino.documento"
				);
		addLabels("Suc"
				,"Prod"
				,"Tipo"
				,"Desc"
				,"U"
				,"Fac"
				,"Rengl"
				,"Kilos"
				,"Cantidad"
				,"Costo Orig"
				,"Gastos"
				,"Costo U"
				,"Costo Tot"
				,"Fecha"
				,"Año"
				,"Mes"
				,"Origen"
				,"Origen Dcto"
				,"Destino"
				,"Destino Dcto"
				);
		
		installTextComponentMatcherEditor("Tipo", "conceptoOrigen");
		installTextComponentMatcherEditor("Sucursal", "sucursal.nombre");		
		installTextComponentMatcherEditor("Documento", "documento");
		installTextComponentMatcherEditor("Producto", "producto.clave","producto.descripcion");
		installTextComponentMatcherEditor("Linea", "producto.linea.nombre");
		installTextComponentMatcherEditor("Clase", "producto.clase.nombre");
		installTextComponentMatcherEditor("Marca", "producto.marca.nombre");
		installTextComponentMatcherEditor("Costo", "costop");
		manejarPeriodo();
	}

	@Override
	protected List<Action> createProccessActions() {
		List<Action> procesos=super.createProccessActions();		
		return procesos;
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,addAction(null, "modificarCostos", "Modificar Costos")
				,getViewAction()
				};
		return actions;
	}
	
	@Override
	protected List<TransformacionDet> findData() {
		String hql="from TransformacionDet d where d.fecha between ? and ?";
		return ServiceLocator2.getHibernateTemplate().find(hql,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
	}

	/*public void importar(){
		
		final SwingWorker<String, String> worker=new SwingWorker<String, String>(){
			@Override
			protected String doInBackground() throws Exception {
				ServiceLocator2.getTransformacionesManager().importarPendientes(periodo);
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
				super.done();
			}
			
		};
		TaskUtils.executeSwingWorker(worker);
	}
	*/
	/**
	 * 
	 * Permite modificar el costo de una o mas entradas por traslado
	 * 
	 * 
	 */
	public void modificarCostos(){
		final List<TransformacionDet> selected=new ArrayList<TransformacionDet>();
		selected.addAll(getSelected());
		CostosForm form=new CostosForm();
		if(selected.size()==1){
			form.setCostoOrigen(selected.get(0).getCostoOrigen());
			form.setGastos(selected.get(0).getGastos());
		}
		form.open();
		if(!form.hasBeenCanceled()){
			for(TransformacionDet det:selected){
				if(det.getCantidad()>0){
					logger.info("Solo se puede administrar salidas por TRS");
				}
				det.setCostoOrigen(form.getCostoOrigen());
				det.setGastos(form.getGastos());
				det.actualizarCosto();
				int index=source.indexOf(det);
				if(index!=-1){
					det.actualizarCosto();
					det=ServiceLocator2.getTransformacionesManager().save(det);
					source.set(index, det);
				}
			}
		}
	}
	
	public class CostosForm extends SXAbstractDialog{
		
		private BigDecimal costoOrigen;
		private BigDecimal gastos;
		private JFormattedTextField costoField;
		private JFormattedTextField gastoField;

		public CostosForm() {
			super("");
		}
		
		private PresentationModel model;
		
		protected void init(){
			model=new PresentationModel(this);
			costoField=Binder.createBigDecimalForMonyBinding(buffer(model.getModel("costoOrigen")));
			gastoField=Binder.createBigDecimalForMonyBinding(buffer(model.getModel("gastos")));
		}

		@Override
		protected JComponent buildContent() {
			init();
			JPanel panel=new JPanel(new BorderLayout());
			final FormLayout layout=new FormLayout("60dlu,3dlu,120dlu","");
			final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Costo Origen",costoField);
			builder.append("Gastos ",gastoField);
			panel.add(builder.getPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			return panel;
		}

		public BigDecimal getCostoOrigen() {
			return costoOrigen;
		}

		public void setCostoOrigen(BigDecimal costoOrigen) {
			this.costoOrigen = costoOrigen;
		}

		public BigDecimal getGastos() {
			return gastos;
		}

		public void setGastos(BigDecimal gastos) {
			this.gastos = gastos;
		}
		
		
	}
	
	private TotalesPanel totalPanel;
	
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			totalPanel=new TotalesPanel();
		}
		return (JPanel)totalPanel.getControl();
	}
	
	private class TotalesPanel extends AbstractControl implements ListEventListener{
		
		private JLabel costo=new JLabel();
		//private JLabel costoUltimo=new JLabel();
		private JLabel kilos=new JLabel();
		private JLabel existencia=new JLabel();
		

		public TotalesPanel() {
			super();
			sortedSource.addListEventListener(this);
		}

		@Override
		protected JComponent buildContent() {			
			costo.setHorizontalAlignment(JLabel.RIGHT);
			kilos.setHorizontalAlignment(JLabel.RIGHT);
			existencia.setHorizontalAlignment(JLabel.RIGHT);
			
			final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			costo.setHorizontalAlignment(SwingConstants.RIGHT);
			
			builder.append("Cantidad",existencia);
			builder.append("Kilos",kilos);
			builder.append("Costo P",costo);
			
			
			
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
			
			CantidadMonetaria costop=CantidadMonetaria.pesos(sumarizar("costoTotal"));
			BigDecimal cantidad=sumarizar("cantidad");
			BigDecimal kg=sumarizar("kilos");
			
			costo.setText(nf1.format(costop.amount().doubleValue()));			
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
