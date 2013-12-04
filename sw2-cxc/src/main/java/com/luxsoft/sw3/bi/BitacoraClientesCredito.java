package com.luxsoft.sw3.bi;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.luxsoft.siipap.model.core.ClienteAuditLog;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.browser.JRBrowserReportForm;
import com.luxsoft.siipap.swing.matchers.FechaSelector;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.DBUtils;


/**
 * Bitacora para modificaciones a clientes de credito
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class BitacoraClientesCredito extends FilteredBrowserPanel<ClienteAuditLog> {
	
	
	
	
	public BitacoraClientesCredito() {
		super(ClienteAuditLog.class);
		setTitle("Bitácora de cambios");
	}
	
	public void init(){
		addProperty(
				"id"
				,"origen"
				,"action"
				,"message"
				,"usuario"
				,"lastUpdated"
				,"ip"
				);
		addLabels(
				"ID"
				,"Cliente"
				,"Accion"
				,"Comentario"
				,"Usuario"
				,"Modificado"
				,"IP"
			);
		
		
		FechaSelector fechaSelector=new FechaSelector("lastUpdated");
		installTextComponentMatcherEditor("Cliente", "origen");
		installTextComponentMatcherEditor("Usuario", "usuario");
		installTextComponentMatcherEditor("Fecha", fechaSelector, fechaSelector.getInputField());
		
		
		
		Comparator c1=GlazedLists.beanPropertyComparator(ClienteAuditLog.class, "lastUpdated");
		setDefaultComparator(c1);
		manejarPeriodo();
	}
	
	
	@Override
	protected void adjustMainGrid(JXTable grid) {		
		//grid.getColumnExt("Id").setVisible(false);
		//grid.getColumnExt("Cuenta").setVisible(false);
		
	}
	
	@Override
	protected void doSelect(Object bean) {
	}
	
	

	@Override
	public Action[] getActions() {
		CommandUtils.createPrintAction(this, "runReporte");
		if(actions==null){			
			actions=new Action[]{
					CommandUtils.createPrintAction(this, "runReporte")
				};
		}
		return actions;
	}
	
	
	@Override
	protected List<ClienteAuditLog> findData() {
		List<ClienteAuditLog> res=ServiceLocator2.getHibernateTemplate()
				.find("from ClienteAuditLog a where date(a.lastUpdated) between ? and ?"
				, new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()}
						);
		return res;
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
	
	@Override
	protected void afterLoad() {		
		super.afterLoad();
		grid.packAll();
	}

	
	
	public void runReporte(){
		JRBrowserReportForm reportForm=new JRBrowserReportForm("Bitácora", this){
			public void agregarParametros(Map<String, Object> map) {
				map.put("FECHA_INI", periodo.getFechaInicial());
				map.put("FECHA_FIN", periodo.getFechaFinal());
			/*	if(){
					map.put("CLIENTE","FILTRADO POR CLIENTE");
				}*/
				/*map.put("CARGOS", BigDecimal.valueOf(totalPanel.cargos));
				map.put("ABONOS", BigDecimal.valueOf(totalPanel.abonos));
				map.put("YEAR",year);
			    map.put("MES",DateUtil.getMesAsString(mes));			   
			    map.put("CUENTA", cuenta);
				*/
			}
		};
		reportForm.setReportPath("BI/Bitacora.jasper");
		reportForm.open();
	}
	
	
	
	public static void show(){
		final BitacoraClientesCredito browser=new BitacoraClientesCredito();		
		final FilterBrowserDialog dialog=new FilterBrowserDialog(browser);
		dialog.setTitle("Bitacora de modificaciones a clientes de crédito");
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
				show();
				//System.exit(0);
			}

		});
	}

}
