package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXTable;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.reportes.ComisionTarjetasReportForm;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.tesoreria.model.CorteDeTarjeta;
import com.luxsoft.sw3.tesoreria.model.CorteDeTarjetaDet;
import com.luxsoft.sw3.tesoreria.ui.forms.CorteDeTarjetaForm;
import com.luxsoft.sw3.tesoreria.ui.forms.CorteDeTarjetaFormModel;



/**
 * Analisis de Cortes de tarjetas
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class AnalisisDeCortesDeTarjetaPanel extends AbstractMasterDatailFilteredBrowserPanel<CorteDeTarjeta,CorteDeTarjetaDet> {
	
	private Date fecha;

	public AnalisisDeCortesDeTarjetaPanel() {
		super(CorteDeTarjeta.class);
		setTitle("Cortes de Tarjetas");
	}
	
	public void init(){
		addProperty(
				"id"
				,"sucursal.nombre"
				,"fecha"
				,"corte"
				,"cuenta.banco.clave"
				,"total"
				,"tipoDeTarjeta"
				,"ingreso.id"
				,"comentario"
				
				);
		addLabels(
				"Id",
				"Sucursal"
				,"Fecha"
				,"Corte"
				,"Cuenta"
				,"Total"
				,"Tarjeta"
				,"Ingreso"
				,"Comentario"
				
				);
		installTextComponentMatcherEditor("Id", "id");
		installTextComponentMatcherEditor("Sucursal", "sucursal.nombre");
		installTextComponentMatcherEditor("Total", "total");
		installTextComponentMatcherEditor("Tipo de Tarjeta", "tipoDeTarjeta");
				
	}

	@Override
	protected void adjustMainGrid(JXTable grid) {
	}
	

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={
				"corte.id"
				,"pago.nombre"
				,"pago.fecha"
				,"pago.total"				
				,"pago.tarjeta.nombre"
				,"pago.tarjeta.debito"
				//,"pago.comisionBancaria"
				,"pago.autorizacionBancaria"
				//,"pago.info"
				};
		String[] names={
				"Corte"
				,"Nombre"
				,"Fecha"
				,"Total"
				,"Tarjeta"
				,"Debito"
				//,"Comisión"
				,"Referencia"
				//,"Info"
				};
		return GlazedLists.tableFormat(CorteDeTarjetaDet.class,props,names);
	}

	@Override
	protected Model<CorteDeTarjeta, CorteDeTarjetaDet> createPartidasModel() {
		return new Model<CorteDeTarjeta, CorteDeTarjetaDet>(){
			public List<CorteDeTarjetaDet> getChildren(CorteDeTarjeta parent) {
				String hql="from CorteDeTarjetaDet p left join fetch p.corte cc" +
						" where p.corte.id=?";
				return getHibernateTemplate().find(hql,parent.getId());
			}			
		};
	}	
	
	public void open(){
		load();
	}
	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null){
			actions=new Action[]{				
				getViewAction()
				,addAction(null, "imprimir", "Imprimir")
				};
		}
		return actions;
	}
	
	@Override
	protected List<Action> createProccessActions() {		
		List<Action> procesos=super.createProccessActions();
		procesos.add(addAction("","reporteDeComisionTarjetas", "Rep. Pagos con Tarjeta"));
		//procesos.add(addAction("","modificarTarjeta","Cambiar Tarjeta"));
		return procesos;
	}
	
	public void reporteDeComisionTarjetas(){
		ComisionTarjetasReportForm.run();
	}

	@Override
	protected List<CorteDeTarjeta> findData() {
		String hql="from CorteDeTarjeta c where c.corte =?";
		return ServiceLocator2.getHibernateTemplate().find(hql
				,new Object[]{
					fecha
					}
		);
	}
	
	
	@Override
	protected void doSelect(Object bean) {
		CorteDeTarjeta corte=(CorteDeTarjeta)bean;
		List<CorteDeTarjeta> found=getHibernateTemplate().find("from CorteDeTarjeta c left join fetch c.partidas p where c.id=?",corte.getId());
		if(!found.isEmpty()){
			final CorteDeTarjetaFormModel model=new CorteDeTarjetaFormModel(found.get(0));
			model.setReadOnly(true);
			final CorteDeTarjetaForm form=new CorteDeTarjetaForm(model);
			form.open();
		}
	}
	
	

	protected HibernateTemplate getHibernateTemplate(){
		return ServiceLocator2.getHibernateTemplate();
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
				CorteDeTarjeta a=(CorteDeTarjeta)obj;
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

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	
	public static void show(String dateAsString,final String origen){
		Date fecha=DateUtil.toDate(dateAsString);
		show(fecha, origen);
	}
	
	
	public static void show(final Date fecha,final String origen){
		final AnalisisDeCortesDeTarjetaPanel browser=new AnalisisDeCortesDeTarjetaPanel();
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
