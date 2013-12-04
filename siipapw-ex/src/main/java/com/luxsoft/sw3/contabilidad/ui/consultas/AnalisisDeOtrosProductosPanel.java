package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;


import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;




public class AnalisisDeOtrosProductosPanel extends FilteredBrowserPanel<Pago>{
	
	private Date fecha;
	
	public AnalisisDeOtrosProductosPanel() {
		super(Pago.class);
		setTitle("Otros productos (Pagos)");
	}
	
	public void init(){
		addProperty(
				"sucursal.nombre"
				,"clave"
				,"nombre"
				,"primeraAplicacion"
				,"direfenciaFecha"
				,"origenAplicacion"
				,"info"
				,"diferencia"
				);
		addLabels(
				"Sucursal"
				,"Cliente"
				,"Nombre"
				,"Fecha PA"
				,"Fecha Dif"
				,"Origen"
				,"Info"
				,"Dif"
				);
		installTextComponentMatcherEditor("Sucursal", "sucursal.nombre");
		installTextComponentMatcherEditor("Origen", "origenAplicacion");
		installTextComponentMatcherEditor("Cliente", "nombre");
	}	

	@Override
	protected List<Pago> findData() {
		String sql="from Pago p where p.direfenciaFecha=? and p.diferencia<>0"; 
		return ServiceLocator2.getHibernateTemplate().find(sql, getFecha());
	}
	public void open(){
		load();
	}
	
	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	


	@Override
	public Action[] getActions() {
		return new Action[0];
	}
	
private TotalesPanel totalPanel;
	
	
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			totalPanel=new TotalesPanel();
		}
		return (JPanel)totalPanel.getControl();
	}
	
	
	private class TotalesPanel extends AbstractControl implements ListEventListener{
		
		private JLabel ventaTotal=new JLabel();

		@Override
		protected JComponent buildContent() {
			final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			ventaTotal.setHorizontalAlignment(SwingConstants.RIGHT);
			builder.append("Total",ventaTotal);
			
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
		
		public void updateTotales(){
			
			CantidadMonetaria total=CantidadMonetaria.pesos(0);
			for(Object o:getFilteredSource()){
				Pago v=(Pago)o;
				total=total.add(CantidadMonetaria.pesos(v.getImporte().doubleValue()));
			}			
			String pattern="{0}";
			ventaTotal.setText(MessageFormat.format(pattern, total.amount()));
		}
	}



	
	
	public static void show(String dateAsString,final String origen){
		Date fecha=DateUtil.toDate(dateAsString);
		show(fecha, origen);
	}
	
	
	public static void show(final Date fecha,final String origen){
		final AnalisisDeOtrosProductosPanel browser=new AnalisisDeOtrosProductosPanel();
		browser.setFecha(fecha);
		final FilterBrowserDialog dialog=new FilterBrowserDialog(browser);
		dialog.setModal(false);
		
		dialog.open();
	}
	
	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		
		
		
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				DBUtils.whereWeAre();
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				show("14/04/2011", "CAM");
				//System.exit(0);
			}

		});
	}

}
