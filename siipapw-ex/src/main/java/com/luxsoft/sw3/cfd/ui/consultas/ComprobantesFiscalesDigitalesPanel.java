package com.luxsoft.sw3.cfd.ui.consultas;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.sql.Types;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jdesktop.swingx.JXTable;
import org.jfree.ui.DateCellRenderer;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SqlParameterValue;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.AbstractDialog;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.MonedasUtils;

import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;
import com.luxsoft.sw3.ventas.ui.consultas.ReporteMensualCFD;

public class ComprobantesFiscalesDigitalesPanel extends FilteredBrowserPanel<ReporteMensualCFD>{

	public ComprobantesFiscalesDigitalesPanel() {
		super(ReporteMensualCFD.class);
		setTitle("Reporte Mensaul CFD (SAT)");
	}
	
	protected void init(){
		String[] props=new String[]{
				"rfc"
				,"receptor"
				,"serie"
				,"folio"
				,"ano_aprobacion"
				,"no_aprobacion"
				,"fecha"
				,"total"
				,"impuesto"
				,"estado"
				,"tipo_cfd"
				//,"pedimento"
				//,"fecha_ped"
				//,"aduana"
				};
		String[] names=new String[]{	
				
				"RFC"
				,"Receptor"
				,"Serie"
				,"Folio"
				,"Año Aprob"
				,"No_Aprob"
				,"Fecha"
				,"Total"
				,"Impuesto"
				,"Estado"
				,"Tipo_CFD"
				//,"Pedimento"
				//,"Fecha_ped"
				//,"Aduana"
				};
		addProperty(props);
		addLabels(names);
		installTextComponentMatcherEditor("RFC", "rfc");
		installTextComponentMatcherEditor("Receptor", "Receptor");
		installTextComponentMatcherEditor("Serie", "Serie");
		installTextComponentMatcherEditor("Folio", "Folio");
		installTextComponentMatcherEditor("Total", "Total");
		installTextComponentMatcherEditor("Estado", "Estado");
		manejarPeriodo();
	}
	
	protected void manejarPeriodo(){
		//periodo=Periodo.getPeriodoEnUnMes(-1);
		periodo=Periodo.periodoDeloquevaDelMes();
	}
	
	@Override
	protected EventList getSourceEventList() {
		EventList eventList=new BasicEventList();
		return new SortedList(eventList,GlazedLists
				.beanPropertyComparator(ReporteMensualCFD.class,"fecha"));
	}
	
	protected void adjustMainGrid(final JXTable grid){
		grid.getColumnExt("Fecha")
			.setCellRenderer(new DateCellRenderer(new SimpleDateFormat("dd/MM/yyyy hh:mm:ss")));
		
		
		
	}
	

	public void cambiarPeriodo(){
		
		ValueHolder yearModel=new ValueHolder(Periodo.obtenerYear(periodo.getFechaInicial()));
		ValueHolder mesModel=new ValueHolder(Periodo.obtenerMes(periodo.getFechaFinal()));
		
		AbstractDialog dialog=Binder.createSelectorMesYear(yearModel, mesModel);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			int year=(Integer)yearModel.getValue();
			int mes=(Integer)mesModel.getValue();
			periodo=Periodo.getPeriodoEnUnMes(mes-1, year);
			updatePeriodoLabel();
			//nuevoPeriodo(periodo);
			
		}
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getViewAction()
				,CommandUtils.createPrintAction(this, "print")
				
				};
		return actions;
	}
	
	@Override
	protected List<Action> createProccessActions() {		
		List<Action> procesos=super.createProccessActions();
		procesos.add(addAction("","reporteDeVentasDiarias", "Ventas Diarias"));
		procesos.add(addAction("","generarReporteDelSAT", "Reporte Mensual SAT"));
		return procesos;
	}
	
	@Override
	protected List<ReporteMensualCFD> findData() {
		String sql="SELECT RFC,RECEPTOR,SERIE,FOLIO,ANO_APROBACION,NO_APROBACION,CREADO AS FECHA,TOTAL,ROUND((CASE WHEN RFC='XAXX010101000' THEN (TOTAL/1.16)*0.16 ELSE IMPUESTO END),2)  AS IMPUESTO,'1' AS ESTADO,TIPO_CFD,PEDIMENTO,PEDIMENTO_FECHA AS FECHA_PED,ADUANA FROM SX_CFD WHERE DATE(CREADO) BETWEEN ? AND ? " +
					"UNION " +
					"SELECT RFC,RECEPTOR,SERIE,FOLIO,ANO_APROBACION,NO_APROBACION,C.CREADO AS FECHA,TOTAL,ROUND((CASE WHEN RFC='XAXX010101000' THEN (C.TOTAL/1.16)*0.16 ELSE C.IMPUESTO END),2)  AS IMPUESTO,'0' AS ESTADO,TIPO_CFD,PEDIMENTO,PEDIMENTO_FECHA AS FECHA_PED,ADUANA FROM SX_CFD C JOIN SX_CXC_CARGOS_CANCELADOS X ON(X.CARGO_ID=C.ORIGEN_ID) WHERE DATE(C.CREADO) BETWEEN ? AND ? " +
					"UNION " +
					"SELECT RFC,RECEPTOR,SERIE,C.FOLIO,ANO_APROBACION,NO_APROBACION,C.CREADO AS FECHA,C.TOTAL,ROUND((CASE WHEN RFC='XAXX010101000' THEN (C.TOTAL/1.16)*0.16 ELSE C.IMPUESTO END),2)  AS IMPUESTO,'0' AS ESTADO,TIPO_CFD,PEDIMENTO,PEDIMENTO_FECHA AS FECHA_PED,ADUANA FROM SX_CFD C JOIN SX_CXC_ABONOS X ON(X.ABONO_ID=C.ORIGEN_ID) WHERE DATE(C.CREADO) BETWEEN ? AND ? AND X.TOTAL=0 "
			;
		
		
		Object[] args=new Object[]{
				new SqlParameterValue(Types.DATE,periodo.getFechaInicial())
				,new SqlParameterValue(Types.DATE,periodo.getFechaFinal())
				,new SqlParameterValue(Types.DATE,periodo.getFechaInicial())
				,new SqlParameterValue(Types.DATE,periodo.getFechaFinal())
				,new SqlParameterValue(Types.DATE,periodo.getFechaInicial())
				,new SqlParameterValue(Types.DATE,periodo.getFechaFinal())
		};
		return ServiceLocator2
			.getJdbcTemplate()
			.query(sql, args, new BeanPropertyRowMapper(ReporteMensualCFD.class));
		
	}	
	
	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}
	
	public ComprobanteFiscal getComprobante(){
		return (ComprobanteFiscal)getSelectedObject();
	}
	
public void generarReporteDelSAT(){
		
		String EMISOR_RFC=ServiceLocator2.getConfiguracion().getSucursal().getEmpresa().getRfc();
		String patt="{0}{1}{2}{3}";
		String fileName=MessageFormat.format(patt,
				1
				,EMISOR_RFC
				,new SimpleDateFormat("MM").format(periodo.getFechaFinal())
				,new SimpleDateFormat("yyyy").format(periodo.getFechaFinal())
				);
		
		
		
		JFileChooser chooser=new JFileChooser("/CFD/SAT");
		chooser.setSelectedFile(new File("/CFD/SAT/"+fileName+".txt"));
		FileNameExtensionFilter filter=new FileNameExtensionFilter("Destino", "txt");		
		chooser.setFileFilter(filter);
		
		int res=chooser.showDialog(getControl(), "Aceptar");
		if(res==JFileChooser.APPROVE_OPTION){
			File file=chooser.getSelectedFile();
			try {
				if(file.createNewFile()){
					System.out.println("Archivo generado: "+file);
				}else{
					
					boolean borrar=MessageUtils.showConfirmationMessage(
							"El nombre de archivo ya existe, desea reemplazarlo"
							, "Reporte mensual SAT");
					if(borrar)
						file.delete();
				}
				FileOutputStream out=new FileOutputStream(file);
				OutputStreamWriter writer=new OutputStreamWriter(out,"UTF-8");
				BufferedWriter buf=new BufferedWriter(writer);
				for(Object row:source){
					ReporteMensualCFD cfd=(ReporteMensualCFD)row;
					String pattern="|{0}|{1}|{2}|{3}|{4}|{5}|{6}|{7}|{8}|{9}|{10}|{11}|";
					String line=MessageFormat.format(pattern
							,StringUtils.defaultString(cfd.getRfc())
							,StringUtils.defaultString(cfd.getSerie())
							,StringUtils.defaultString(cfd.getFolio())
							,StringUtils.defaultString(cfd.getAno_aprobacion())+StringUtils.defaultString(cfd.getNo_aprobacion())
							,new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(cfd.getFecha())
							,StringUtils.defaultString(cfd.getTotal())
							,StringUtils.defaultString(cfd.getImpuesto())
							,StringUtils.defaultString(cfd.getEstado())
							,StringUtils.defaultString(cfd.getTipo_cfd().toUpperCase())							
							,StringUtils.defaultString(cfd.getPedimento())
							,cfd.getFecha_ped()!=null?new SimpleDateFormat("dd/MM/yyyy").format(cfd.getFecha_ped()):""
							,StringUtils.defaultString(cfd.getAduana())
							);
					buf.write(line);
					buf.newLine();
				}
				
				buf.flush();
				buf.close();
				writer.close();
				out.close();
				
			} catch (IOException e) {
				throw new RuntimeException(ExceptionUtils.getRootCauseMessage(e),e);
			}
			
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
	
	
	private JLabel total1=new JLabel();
	private JLabel total2=new JLabel();
	private JLabel total3=new JLabel();
	
	
	private JLabel totalIva=new JLabel();
	private JLabel totalIvaCancelado=new JLabel();
	private JLabel netoIva=new JLabel();
	

	@Override
	protected JComponent buildContent() {
		final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		totalIva.setHorizontalAlignment(SwingConstants.RIGHT);
		totalIvaCancelado.setHorizontalAlignment(SwingConstants.RIGHT);
		netoIva.setHorizontalAlignment(SwingConstants.RIGHT);
		
		total1.setHorizontalAlignment(SwingConstants.RIGHT);
		total2.setHorizontalAlignment(SwingConstants.RIGHT);
		total3.setHorizontalAlignment(SwingConstants.RIGHT);
		
		builder.appendSeparator("Impuestos ");
		builder.append("Total ",totalIva);
		builder.append("Cancelados",totalIvaCancelado);
		builder.append("Neto",netoIva);
		
		builder.appendSeparator("Totales ");
		builder.append("Total ",total1);
		builder.append("Cancelados",total2);
		builder.append("Neto",total3);
		
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
		
		double totalCfd=0;
		double totalCancelado=0;
		
		double totalIvaCfd=0;
		double totalIvaCancelado=0;
		
		
		for(Object obj:getFilteredSource()){
			ReporteMensualCFD a=(ReporteMensualCFD)obj;
			if(a.getEstado().equals("1")){
				BigDecimal tot=new BigDecimal(a.getTotal());
				BigDecimal iva= new BigDecimal(0);
				iva=new BigDecimal(a.getImpuesto());	
				
				
				totalCfd+=tot.doubleValue();
				totalIvaCfd+=iva.doubleValue();
			}else{
				BigDecimal tot=new BigDecimal(a.getTotal());
				BigDecimal iva= new BigDecimal(0);
				iva=new BigDecimal(a.getImpuesto());	
				totalCancelado+=tot.doubleValue();
				totalIvaCancelado+=iva.doubleValue();
			}
		}
		total1.setText(nf.format(totalCfd));
		total2.setText(nf.format(totalCancelado));
		total3.setText(nf.format(totalCfd-totalCancelado));
		
		totalIva.setText(nf.format(totalIvaCfd));
		this.totalIvaCancelado.setText(nf.format(totalIvaCancelado));
		netoIva.setText(nf.format(totalIvaCfd-totalIvaCancelado));
	}
	
	private NumberFormat nf=NumberFormat.getNumberInstance();
	
}

}
