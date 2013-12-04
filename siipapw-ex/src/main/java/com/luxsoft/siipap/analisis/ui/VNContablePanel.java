package com.luxsoft.siipap.analisis.ui;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import org.apache.commons.lang.ArrayUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.luxsoft.siipap.analisis.model.AVNContablePorConcepto;
import com.luxsoft.siipap.analisis.model.AVNContableController;
import com.luxsoft.siipap.analisis.model.AnalisisUtils;
import com.luxsoft.siipap.analisis.model.ImportesMensuales;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.TaskUtils;

/**
 * Panel de analis de venta neta desde el punto de vista contable
 * @author Ruben Cancino 
 *
 */
public class VNContablePanel extends FilteredBrowserPanel<AVNContablePorConcepto>{
	
	private JComboBox tipos;
	private JComboBox years;
	
	

	public VNContablePanel() {
		super(AVNContablePorConcepto.class);
		Object[] props=AnalisisUtils.MESES;
		props=ArrayUtils.add(props, 0, "concepto");
		addProperty((String[])props);
		
		Object[] labs=AnalisisUtils.MESES;
		labs=ArrayUtils.add(labs, 0, "Concepto");
		addLabels((String[])labs);
		tipos=new JComboBox(new String[]{"TODOS","CREDITO","CONTADO"});
		tipos.setSelectedIndex(0);		
		
		years=new JComboBox(new Integer[]{2008,2009});
		
	}
	
	

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{getLoadAction()};
		return actions;
	}



	@Override
	protected void installCustomComponentsInFilterPanel(
			DefaultFormBuilder builder) {
		builder.append("Tipo De Venta",tipos);
		builder.append("Año",years);
	}


	@Override
	protected JComponent buildContent() {
		JSplitPane sp=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		sp.setOneTouchExpandable(true);
		sp.setResizeWeight(.4);
		sp.setTopComponent(super.buildContent());
		sp.setBottomComponent(buildDetailPanel());
		return sp;
	}
	
	private ImportesMensualesPanel descuentosPanel;
	private ImportesMensualesPanel devolucionesPanel;
	private ImportesMensualesPanel provisionHistoricaPanel;
	
	
	private JComponent buildDetailPanel(){
		JTabbedPane tb=new JTabbedPane();
		descuentosPanel=new ImportesMensualesPanel();
		tb.addTab("Descuentos", descuentosPanel.getControl());
		
		devolucionesPanel=new ImportesMensualesPanel();
		tb.addTab("Devoluciones", devolucionesPanel.getControl());
		
		provisionHistoricaPanel=new ImportesMensualesPanel();
		tb.addTab("Prov Histórica", provisionHistoricaPanel.getControl());
		return tb;
	}
	
	@Override
	protected List<AVNContablePorConcepto> findData() {
		AVNContableController controller=new AVNContableController();
		controller.setJdbcTemplate(ServiceLocator2.getAnalisisJdbcTemplate());
		int year=(Integer)years.getSelectedItem();
		return controller.cargarAnalisis(year,tipos.getSelectedItem().toString());
	}
	
	protected void afterLoad(){
		if(!source.isEmpty()){
			AVNContablePorConcepto avn=(AVNContablePorConcepto)source.get(1);
			descuentosPanel.getSource().clear();
			descuentosPanel.getSource().addAll(avn.getImportes());
			
			AVNContablePorConcepto avn2=(AVNContablePorConcepto)source.get(2);
			devolucionesPanel.getSource().clear();
			devolucionesPanel.getSource().addAll(avn2.getImportes());
			//loadProvisionHistorica();
		}
	}
	
	private void loadProvisionHistorica(){
		provisionHistoricaPanel.getSource().clear();
		SwingWorker<String, List<ImportesMensuales>> worker=new SwingWorker<String, List<ImportesMensuales>>(){
			
			@Override
			protected String doInBackground() throws Exception {
				AVNContableController controller=new AVNContableController();
				controller.setJdbcTemplate(ServiceLocator2.getAnalisisJdbcTemplate());
				int year=(Integer)years.getSelectedItem();
				for(int i=0;i<=11;i++){					
					publish(controller.buscarImportesDeProvisionHistorica(year,i));
				}
				return "OK";
			}

			@Override
			protected void process(List<List<ImportesMensuales>> chunks) {
				for(List<ImportesMensuales> data:chunks){
					provisionHistoricaPanel.getSource().addAll(data);
				}
			}
			@Override
			protected void done() {
				provisionHistoricaPanel.getGrid().packAll();
			}
			
			
		};
		worker.execute();
	}
	
	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}


	

	private class ImportesMensualesPanel extends FilteredBrowserPanel<ImportesMensuales>{
		
		public ImportesMensualesPanel() {
			super(ImportesMensuales.class);
			Object[] props=AnalisisUtils.MESES;
			props=ArrayUtils.add(props, 0, "year");
			props=ArrayUtils.add(props, 1, "mes");
			props=ArrayUtils.add(props, 14, "total");
			addProperty((String[])props);
			
			Object[] labs=AnalisisUtils.MESES;
			labs=ArrayUtils.add(labs, 0, "Año");
			labs=ArrayUtils.add(labs, 1, "Mes");
			labs=ArrayUtils.add(labs, 14, "Total");
			addLabels((String[])labs);
		}

		public void load(){}
		
	}
	
	
	

	public static void main(String[] args) {
		final VNContablePanel panel=new VNContablePanel();
		
		SXAbstractDialog dialog=new SXAbstractDialog("TEST"){

			@Override
			protected JComponent buildContent() {
				return panel.getControl();
			}

			@Override
			protected void onWindowOpened() {
				panel.load();
			}
			
			
		};
		dialog.open();
		System.exit(0);
	}




	
	
}
