package com.luxsoft.sw3.bi;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.Action;

import org.apache.commons.lang.time.DateUtils;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.reports.ControlDeTiemposDeEnvioReport;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.sw3.embarque.Entrega;


/**
 * Consulta para el monitoreo de entregas
 *  
 * @author Ruben Cancino 
 *
 */
public class EstadoDeEntregasCentralizadoPanel extends FilteredBrowserPanel<Entrega>{

	public EstadoDeEntregasCentralizadoPanel() {
		super(Entrega.class);		
	}
	
	protected void init(){
		String[] props={
				"embarque.sucursal"
				,"embarque.documento"
				,"factura.documento"
				,"factura.origen.shortName"
				,"factura.pedidoCreado"
				,"factura.log.creado"
				,"surtido"
				,"log.creado"
				,"embarque.salida"				
				,"arribo"
				,"recepcion"
				,"embarque.regreso"
				,"retrasoCalculado"
				,"parcial"				
				,"factura.contraEntrega"				
				,"recibio"				
				,"factura.saldoCalculado"
				,"factura.ultimoPago"
				,"embarque.transporte.chofer.nombre"
				,"nombre"
				};
		String[] labels={
				"Sucursal"
				,"Embarque"
				,"Docto"
				,"Origen"
				,"Pedido"
				,"Facturado"    // 1
				,"Surtido"		// 2
				,"Asignación"	// 3
				,"Salida"		// 4
				,"Arribo"		// 5
				,"Recepción"	// 6
				,"Retorno"		// 7
				,"Retraso (hrs)"
				,"Parcial"				
				,"COD"				
				,"Recibió"				
				,"SaldoCalculado"
				,"UltimoPago"
				,"Chofer"
				,"Cliente"
				};
		addProperty(props);
		addLabels(labels);
		installTextComponentMatcherEditor("Sucursal", "embarque.sucursal");
		installTextComponentMatcherEditor("Embarque", "embarque.documento");
		installTextComponentMatcherEditor("Chofer", "embarque.transporte.chofer.nombre");
		installTextComponentMatcherEditor("Factura", "documento");
		manejarPeriodo();
	}
	
	protected void manejarPeriodo(){
		periodo=Periodo.getPeriodo(-3);
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{getLoadAction(),
			CommandUtils.createPrintAction(this, "imprimir")
				};
		return actions;
	}
	
	
	public void imprimir(){
		ControlDeTiemposDeEnvioReport.run();
		/*
		java.util.Map map=new HashMap();
		map.put("FECHA_INI",periodo.getFechaInicial());
		map.put("FECHA_FIN",periodo.getFechaFinal());
		ReportUtils.viewReport(ReportUtils.toReportesPath("embarques/ControlDeTiemposDeEnvio.jasper"), map,grid.getModel());
		*/
	}
	

	@Override
	protected List<Entrega> findData() {
		String hql="from Entrega e where  " +
				" e.embarque.fecha between ? and ? ";
		
		return ServiceLocator2.getHibernateTemplate().find(hql, new Object []{
				periodo.getFechaInicial(),periodo.getFechaFinal()}
		);
	}

	@Override
	public void open() {
		load();
	}

	@Override
	protected void adjustMainGrid(JXTable grid) {
		
		grid.getColumnExt("Facturado").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Pedido").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Surtido").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Salida").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Asignación").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Salida").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Arribo").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));		
		grid.getColumnExt("Recepción").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Retorno").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("UltimoPago").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		
		grid.getColumnExt("Recibió").setVisible(false);		
		
	}
	
	

}
