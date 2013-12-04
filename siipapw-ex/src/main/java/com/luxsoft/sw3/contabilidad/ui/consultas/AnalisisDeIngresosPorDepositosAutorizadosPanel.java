package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;
import com.luxsoft.sw3.tesoreria.ui.forms.SolicitudDeDepositoForm;

/**
 * Panel para el control de ingresos por depositos
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class AnalisisDeIngresosPorDepositosAutorizadosPanel extends FilteredBrowserPanel<SolicitudDeDeposito> {
	
	private Date fecha;
	
	private DateFormat df;
	
	public AnalisisDeIngresosPorDepositosAutorizadosPanel() {
		super(SolicitudDeDeposito.class);
		setTitle("Depositos y transferencias autorizadas");
	}
	
	public void init(){
		addProperty(
				"sucursal.nombre"
				,"pago.origenAplicacion"
				,"clave"
				,"nombre"
				,"documento"
				,"pago.primeraAplicacion"
				,"pago.info"
				,"pago.fechaDeposito"
				,"total"
				,"pago.ingreso"
				,"bancoOrigen.clave"
				,"cuentaDestino.banco.clave"				
				,"cuentaDestino.numero"				
				);
		addLabels(
				"Sucursal"
				,"Origen"
				,"Cliente"
				,"Nombre"
				,"Folio"
				,"Cobranza"
				,"Referencia"	
				,"Fecha Dep"
				,"Total"
				,"Ingreso"
				,"Banco Origen"
				,"Banco Dest"
				,"Cuenta Dest"
				);
		setDefaultComparator(GlazedLists.beanPropertyComparator(SolicitudDeDeposito.class, "importado"));
		df=new SimpleDateFormat("dd/MM/yyyy");
		TextFilterator<SolicitudDeDeposito> fechaRevision=new TextFilterator<SolicitudDeDeposito>(){
			public void getFilterStrings(List<String> baseList, SolicitudDeDeposito element) {
				if(element.getFechaDeposito()!=null)
					baseList.add(df.format(element.getPago().getPrimeraAplicacion()));
				
			}			
		};
		installTextComponentMatcherEditor("Sucursal", "sucursal.nombre");
		installTextComponentMatcherEditor("Origen", "pago.origenAplicacion");
		installTextComponentMatcherEditor("Solicitud", "documento");
		installTextComponentMatcherEditor("Total", "total");
		installTextComponentMatcherEditor("Banco (Dest)", "cuentaDestino.banco.clave");
		installTextComponentMatcherEditor("Referencia", "pago.info");
		installTextComponentMatcherEditor("Fecha Cobranza ", fechaRevision,new JTextField(10));
	}
	
	@Override
	protected void installCustomComponentsInFilterPanel(DefaultFormBuilder builder) {
		
	}

	@Override
	protected void adjustMainGrid(JXTable grid) {		
		grid.getColumnExt("Cuenta Dest").setCellRenderer(Renderers.buildIntegerRenderer());
	}
	
	

	@Override
	public Action[] getActions() {
		if(actions==null){			
			actions=new Action[]{
				getLoadAction()
				};
		}
		return actions;
	}

	@Override
	protected List<SolicitudDeDeposito> findData() {
		String hql="from SolicitudDeDeposito s " +
				" left join fetch s.cliente c" +
				" left join fetch s.cuentaDestino c" +
				" left join fetch s.bancoOrigen b" +
				" left join fetch s.pago p " +				
				" where s.pago.ingreso !=null" +
				" and date(s.pago.primeraAplicacion)=?";
		return ServiceLocator2.getHibernateTemplate().find(hql
				,new Object[]{
					fecha
					}
		);
	}
	
	@Override
	protected void doSelect(Object bean) {
		SolicitudDeDeposito sol=(SolicitudDeDeposito)bean;
		SolicitudDeDepositoForm.consultar(sol);
	}
	
	

	@Override
	protected void installEditors(EventList editors) {
		super.installEditors(editors);
	}
	
	public SolicitudDeDeposito refresh(String id){
		String hql="from SolicitudDeDeposito s left join fetch s.cliente c" +
		" left join fetch s.cuentaDestino c" +
		" left join fetch s.bancoOrigen b" +
		" left join fetch s.pago p " +		
		" where s.id=?";
		return (SolicitudDeDeposito)ServiceLocator2.getHibernateTemplate().find(hql,id).get(0);
	}
	
	
public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public void open(){
		load();
	}
	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}

private TotalesPanel totalPanel;
	
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			totalPanel=new TotalesPanel();
		}
		return (JPanel)totalPanel.getControl();
	}
	
	private class TotalesPanel extends AbstractControl implements ListEventListener{
		
		
		private JLabel total1=new JLabel();
		private JLabel total2=new JLabel();
		private JLabel total3=new JLabel();
		

		@Override
		protected JComponent buildContent() {
			final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			total1.setHorizontalAlignment(SwingConstants.RIGHT);
			total2.setHorizontalAlignment(SwingConstants.RIGHT);
			total3.setHorizontalAlignment(SwingConstants.RIGHT);
			builder.appendSeparator("Resumen ");
			builder.append("Total",total1);
			//builder.append("Ventas (Prom)",total2);
			//builder.append("Por Pedir",total3);
			
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
			
			double valorTotal1=0;
			//double toneladasVentas=0;
			//double toneladasPorPedir=0;
			
			for(Object obj:getFilteredSource()){
				SolicitudDeDeposito a=(SolicitudDeDeposito)obj;
				valorTotal1+=a.getTotal().doubleValue();
				//toneladasVentas+=a.getToneladasPromVenta();
				//toneladasPorPedir+=a.getToneladasPorPedir();
			}
			total1.setText(nf.format(valorTotal1));
			//total2.setText(nf.format(toneladasVentas));
			//total3.setText(nf.format(toneladasPorPedir));
		}
		
		private NumberFormat nf=NumberFormat.getNumberInstance();
		
	}
	
	public static void show(String dateAsString,final String origen){
		Date fecha=DateUtil.toDate(dateAsString);
		show(fecha, origen);
	}
	
	
	public static void show(final Date fecha,final String origen){
		final AnalisisDeIngresosPorDepositosAutorizadosPanel browser=new AnalisisDeIngresosPorDepositosAutorizadosPanel();
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
