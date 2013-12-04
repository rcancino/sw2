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
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;




public class AnalisisDeAplicacionesPanel extends FilteredBrowserPanel<Aplicacion>{
	
	private Date fecha;
	
	private String origen;
	
	public AnalisisDeAplicacionesPanel() {
		super(Aplicacion.class);
		setTitle("Aplicaciones (Cobranza)");
	}
	
	public void init(){
		addProperty(
				"detalle.clave"
				,"detalle.nombre"
				,"fecha"
				,"abono.sucursal.nombre"				
				,"detalle.origen"
				,"detalle.documento"
				,"detalle.fechaCargo"
				,"detalle.formaDePago"
				,"importe"
				);
		addLabels(
				"Cliente"
				,"Nombre"
				,"Fecha"
				,"Sucursal"				
				,"Origen"
				,"Fecha F."
				,"Docto F."
				,"Concepto"
				,"Importe"
				);
		installTextComponentMatcherEditor("Sucursal", "abono.sucursal.nombre");
		installTextComponentMatcherEditor("Cliente", "detalle.nombre");
		installTextComponentMatcherEditor("Concepto", "detalle.formaDePago");
		installTextComponentMatcherEditor("Origen", "detalle.origen");
		
	}
	

	@Override
	protected List<Aplicacion> findData() {
		String sql="from AplicacionDePago a where a.fecha=? and a.detalle.origen=?"; 
		return ServiceLocator2.getHibernateTemplate().find(sql, new Object[]{getFecha(),getOrigen()});
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
				Aplicacion v=(Aplicacion)o;
				total=total.add(CantidadMonetaria.pesos(v.getImporte().doubleValue()));
			}			
			String pattern="{0}";
			ventaTotal.setText(MessageFormat.format(pattern, total.amount()));
		}
	}


	
	
	
	public String getOrigen() {
		return origen;
	}

	public void setOrigen(String origen) {
		this.origen = origen;
	}

	
	
	public static void show(String dateAsString,final String origen){
		Date fecha=DateUtil.toDate(dateAsString);
		show(fecha, origen);
	}
	
	
	public static void show(final Date fecha,final String origen){
		final AnalisisDeAplicacionesPanel browser=new AnalisisDeAplicacionesPanel();
		browser.setFecha(fecha);
		browser.setOrigen(origen);
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
