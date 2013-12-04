package com.luxsoft.siipap.pos.ui.consultas;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Action;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import ca.odell.glazedlists.GlazedLists;

import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.siipap.util.SQLUtils;
import com.luxsoft.siipap.ventas.model2.VentaContraEntrega;
import com.luxsoft.sw3.services.Services;

/**
 * Consulta de facturas pendientes de asignar
 * 
 * @author Ruben Cancino
 *
 */
public class EntregasPendientesDeAsignarPanel extends FilteredBrowserPanel<VentaContraEntrega>{

	public EntregasPendientesDeAsignarPanel() {
		super(VentaContraEntrega.class);		
	}
	
	protected void init(){
		addProperty(
				"origen"
				,"nombre"
				,"total"
				,"documento"
				,"pedido"
				,"fecha_ped"
				
				
				//,"contraEntrega"
				//,"fpago"
				,"facturado"
				,"retrasoEnAsignacion"
				,"retrasoEnAsignacionHoras"
				//,"entregado"
				//,"instruccion"
				//,"facturista"
				);
		addLabels(
				"Origen"
				,"Cliente"
				,"Total"
				,"Factura"
				,"Pedido"
				,"Fecha (Pedido)"
				
				//,"CE"
				//,"F.pago"
				,"Facturado"
				,"Retraso "
				,"Ret (hr)"
				//,"Entregado"
				//,"Instruccion"
				//,"Facturista"
				);
		installTextComponentMatcherEditor("Factura", "documento");
		installTextComponentMatcherEditor("Cliente", "nombre");
		Comparator  c1=GlazedLists.beanPropertyComparator(VentaContraEntrega.class, "retrasoEnAsignacionHoras");
		setDefaultComparator(GlazedLists.reverseComparator(c1));
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getViewAction()
				};
		return actions;
	}

	@Override
	protected List<VentaContraEntrega> findData() {
		String sql=SQLUtils.loadSQLQueryFromResource("sql/embarques/facturasDeEnvioPorAsignar.sql");
		return Services.getInstance().getJdbcTemplate().query(sql
				, new BeanPropertyRowMapper(VentaContraEntrega.class));
				
	}
	
	@Override
	protected void adjustMainGrid(JXTable grid) {
		grid.setRowHeight(25);
		grid.setFont(new Font("Serif", Font.PLAIN, 18));
		//grid.getColumnExt("Facturado").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));		
		//grid.getColumnExt("Fecha (Pedido)").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		grid.getColumnExt("Facturado").setCellRenderer(new DefaultTableRenderer(new MyDateConverter()));
		grid.getColumnExt("Fecha (Pedido)").setCellRenderer(new DefaultTableRenderer(new MyDateConverter()));
		grid.getColumnExt("Cliente").setMaxWidth(300);
		
		HighlightPredicate red=new HighlightPredicate() {
			public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
				VentaContraEntrega vv=(VentaContraEntrega)getFilteredSource().get(adapter.row);
				return vv.getRetrasoEnAsignacionHoras()>3;
			}
		};
		
		HighlightPredicate yellow=new HighlightPredicate() {
			public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
				VentaContraEntrega vv=(VentaContraEntrega)getFilteredSource().get(adapter.row);
				return vv.getRetrasoEnAsignacionHoras()>=1 && vv.getRetrasoEnAsignacionHoras()<=3;
			}
		};
		
		HighlightPredicate green=new HighlightPredicate() {
			public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
				VentaContraEntrega vv=(VentaContraEntrega)getFilteredSource().get(adapter.row);
				return vv.getRetrasoEnAsignacionHoras()<=1;
			}
		};
		
		grid.setHighlighters(
				new ColorHighlighter(Color.RED,Color.WHITE,red),
				new ColorHighlighter(Color.YELLOW,Color.BLACK,yellow),
				new ColorHighlighter(Color.GREEN,Color.BLACK,green)
		);
	}
	
	@Override
	protected void doSelect(Object bean) {
		VentaContraEntrega vc=(VentaContraEntrega)bean;
		//FacturaForm.show(vc.getId());
		ReportUtils2.imprimirFacturaCopia(vc.getId());
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
		//load();
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
	
	
	public static class MyDateConverter implements StringValue{
		
		final DateFormat df=new SimpleDateFormat("dd-MMM :hh:mm");

		public String getString(Object value) {
			try {
				return df.format(value);
			} catch (Exception e) {				
				if(value!=null)
					return value.toString();
				else
					return "";
			}
			
		}
		
	}
	

}
