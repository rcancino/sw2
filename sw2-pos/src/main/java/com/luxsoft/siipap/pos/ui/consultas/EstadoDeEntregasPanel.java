package com.luxsoft.siipap.pos.ui.consultas;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Action;

import org.apache.commons.lang.time.DateUtils;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.sw3.embarque.Entrega;
import com.luxsoft.sw3.services.Services;

/**
 * Consulta para el monitoreo de entregas
 *  
 * @author Ruben Cancino 
 *
 */
public class EstadoDeEntregasPanel extends FilteredBrowserPanel<EstadoDeEntregasRow>{

	public EstadoDeEntregasPanel() {
		super(EstadoDeEntregasRow.class);		
	}
	
	protected void init(){
		String[] props={
				
				"origen",
				"cliente",
				"transporte",
				"chofer",
				"factura",
				"pedido",
				"fechaFactura",
				"asignado",
				"salida",
				"arribo",
				"recepcion",
			    "embarque"
				};
		String[] labels={
				
				"origen",
				"cliente",
				"transporte",
				"chofer",
				"factura",
				"pedido",
				"fechaFactura",
				"asignado",
				"salida",
				"arribo",
				"recepcion",
			    "embarque"
				};
		addProperty(props);
		addLabels(labels);
		installTextComponentMatcherEditor("Chofer", "chofer");
		installTextComponentMatcherEditor("Factura", "dfactura");
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{getLoadAction()
				};
		return actions;
	}

	@Override
	protected List<EstadoDeEntregasRow> findData() {
		String sql="SELECT V.CARGO_ID as id,V.ORIGEN AS origen,V.NOMBRE AS cliente,Q.TRANSPORTE_ID AS transporte"+
		" ,Q.CHOFER AS chofer,V.DOCTO AS factura,V.PEDIDO_FOLIO AS pedido"+
		" ,V.creado as fechaFactura,E.CREADO AS asignado,Q.SALIDA AS salida,E.ARRIBO AS arribo,E.RECEPCION AS recepcion"+
		" ,Q.DOCUMENTO as embarque"+
		" FROM sx_ventas v join sx_entregas e on(e.VENTA_ID=v.CARGO_ID)"+
		" JOIN sx_embarques Q ON(Q.EMBARQUE_ID=E.EMBARQUE_ID)"+
		" WHERE Q.FECHA>='2013/11/20' AND E.RECEPCION IS NULL";
				
		return Services.getInstance().getJdbcTemplate().query(sql
						, new BeanPropertyRowMapper(EstadoDeEntregasRow.class));
	}

private Timer timer;
	
	TimerTask task=new TimerTask() {
		@Override
		public void run() {
			//System.out.println("Cargando datos en timer......");
			load();
		}
	};
	
	@Override
	public void open() {
		load();
		timer=new Timer();
		timer.schedule(task, 5000, 1000*30);
		
	}
	@Override
	public void close() {
		super.close();
		//System.out.println("Cancelando tarea de cargado en background..");
		task.cancel();
		timer.purge();
	}
	
	
	
	@Override
	protected void adjustMainGrid(JXTable grid) {
/*		
		grid.getColumnExt("Facturado").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Surtido").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Salida").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Asignacin").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Salida").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Arribo").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));		
		grid.getColumnExt("Recepcin").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Retorno").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Recibi").setVisible(false);
		grid.getColumnExt("Surtidor").setVisible(false);
		*/
	}
	
	

}
