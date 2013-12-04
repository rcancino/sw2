package com.luxsoft.sw3.contabilidad.ui.tasks;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.matchers.FechaSelector;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.ui.consultas.FilterBrowserDialog;
import com.luxsoft.sw3.contabilidad.ui.form.PolizaDetForm;
import com.luxsoft.sw3.contabilidad.ui.form.PolizaForm;
import com.luxsoft.sw3.contabilidad.ui.form.PolizaFormModel;

/**
 * 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReclasificacionPorCuentaBrowser extends FilteredBrowserPanel<PolizaDet> {
	
	private final int year;
	private final int mes;
	private final String cuenta;
	
	
	
	public ReclasificacionPorCuentaBrowser(int year,int mes,String cuenta) {
		super(PolizaDet.class);
		setTitle("Depositos y transferencias autorizadas");
		this.year=year;
		this.mes=mes;
		this.cuenta=cuenta;
	}
	
	public void init(){
		addProperty(
				"poliza.folio"
				,"poliza.clase"
				//,"poliza.tipo"
				,"poliza.fecha"
				,"cuenta.clave"
				,"concepto.clave"
				,"descripcion2"
				,"referencia"
				,"referencia2"
				,"debe"
				,"haber"
				,"asiento"
				,"tipo");
		addLabels(
			"Poliza"
				,"Clase"
				//,"Tipo"
				,"Fecha"
			,"Cuenta"
			,"Concepto"
			,"Descripción"
			,"Origen"
			,"Sucursal"
			,"Debe"
			,"Haber"
			,"Asiento"
			,"Tipo"
			);
		FechaSelector fechaSelector=new FechaSelector("poliza.fecha");
		installTextComponentMatcherEditor("Poliza", "poliza.folio");
		installTextComponentMatcherEditor("Concepto", "concepto.clave");
		installTextComponentMatcherEditor("Sucursal", "referencia2");
		installTextComponentMatcherEditor("Asiento", "asiento");
		installTextComponentMatcherEditor("Tipo", "tipo");
		installTextComponentMatcherEditor("Fecha", fechaSelector, fechaSelector.getInputField());
		installTextComponentMatcherEditor("Origen", "referencia");
		installTextComponentMatcherEditor("Descripción", "descripcion");
	}
	
	
	@Override
	protected void adjustMainGrid(JXTable grid) {		
		grid.getColumnExt("Descripción").setVisible(true);
		
	}
	
	@Override
	protected void doSelect(Object bean) {
		PolizaDet det=(PolizaDet)bean;
		final Poliza source=ServiceLocator2.getPolizasManager().getPolizaDao().get(det.getPoliza().getId());		
		final PolizaFormModel model=new PolizaFormModel(source);
		model.setReadOnly(true);
		final PolizaForm form=new PolizaForm(model);			
		form.open();
	}
	
	

	@Override
	public Action[] getActions() {
		if(actions==null){			
			actions=new Action[]{
					CommandUtils.createEditAction(this, "reclasificar")
				};
		}
		return actions;
	}

	@Override
	protected List<PolizaDet> findData() {
		
		String hql="from PolizaDet det where year(det.poliza.fecha)=? " +
				" and month(det.poliza.fecha)=? " +
				" and det.cuenta.clave=?";
		return ServiceLocator2.getHibernateTemplate().find(hql
				,new Object[]{
					year,mes,cuenta
					}
		);
	}
	
	public void reclasificar(){
		if(!selectionModel.getSelected().isEmpty()){
			PolizaDet target=new PolizaDet();
			target.setDescripcion("RECLASIFICACION");
			DefaultFormModel model=new DefaultFormModel(target);
			PolizaDetForm form=new PolizaDetForm(model);
			form.setCuentas(ServiceLocator2.getCuentasContablesManager().getCuentaContableDao().getAll());
			form.open();
			if(!form.hasBeenCanceled()){
				
				for(Object o:selectionModel.getSelected()){
					PolizaDet det=(PolizaDet)o;
					det.setCuenta(target.getCuenta());
					det.setConcepto(target.getConcepto());
					det=(PolizaDet)ServiceLocator2.getHibernateTemplate().merge(det);
				}
				load();
			}
		}
	}

	@Override
	protected void installEditors(EventList editors) {
		super.installEditors(editors);
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

		@Override
		protected JComponent buildContent() {
			final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			total1.setHorizontalAlignment(SwingConstants.RIGHT);
			total2.setHorizontalAlignment(SwingConstants.RIGHT);
			builder.appendSeparator("Resumen ");
			builder.append("Debe",total1);
			builder.append("Haber",total2);
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
		
		double cargos=0;
		double abonos=0;
		
		public void updateTotales(){
			
			cargos=0;
			abonos=0;
			//double toneladasPorPedir=0;
			
			for(Object obj:getFilteredSource()){
				PolizaDet a=(PolizaDet)obj;
				cargos+=a.getDebe().doubleValue();
				abonos+=a.getHaber().doubleValue();
				
			}
			
			
			total1.setText(nf.format(cargos));
			total2.setText(nf.format(abonos));
			
			//total3.setText(nf.format(toneladasPorPedir));
		}
		
		private NumberFormat nf=NumberFormat.getNumberInstance();
		
	}
	
	
	public static void show(final int year,int mes,String cuenta){
		final ReclasificacionPorCuentaBrowser browser=new ReclasificacionPorCuentaBrowser(year,mes,cuenta);
		final FilterBrowserDialog dialog=new FilterBrowserDialog(browser);
		dialog.setTitle(MessageFormat.format("Movimientos   Cuenta: {0}  Periodo: {1}/{2}",cuenta,mes,year));
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
				show(2012,1, "106");
				//System.exit(0);
			}

		});
	}

}
