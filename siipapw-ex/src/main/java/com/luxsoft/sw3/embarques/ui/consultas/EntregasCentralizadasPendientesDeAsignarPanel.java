package com.luxsoft.sw3.embarques.ui.consultas;

import java.awt.Color;
import java.awt.Component;
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
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import ca.odell.glazedlists.GlazedLists;

import com.luxsoft.siipap.cxc.ui.consultas.FacturaForm;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.siipap.util.SQLUtils;
import com.luxsoft.siipap.ventas.model2.VentaContraEntrega;


/**
 * Consulta de facturas pendientes de asignar
 * 
 * @author Ruben Cancino
 *
 */
public class EntregasCentralizadasPendientesDeAsignarPanel extends FilteredBrowserPanel<VentaContraEntrega>{

	public EntregasCentralizadasPendientesDeAsignarPanel() {
		super(VentaContraEntrega.class);
		setTitle("Facturas pendientes de asignar");
	}
	
	protected void init(){
		addProperty(
				"sucursal"
				,"origen"
				,"nombre"
				,"documento"
				,"pedido"
				,"fecha_ped"
				
				,"total"
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
				"Sucursal"
				,"Origen"
				,"Cliente"
				,"Factura"
				,"Pedido"
				,"Fecha (Pedido)"
				,"Total"
				//,"CE"
				//,"F.pago"
				,"Facturado"
				,"Retraso "
				,"Ret (hr)"
				//,"Entregado"
				//,"Instruccion"
				//,"Facturista"
				);
		installTextComponentMatcherEditor("Sucursal", "sucursal");
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
		System.out.println(sql);
		return ServiceLocator2.getJdbcTemplate().query(sql
				, new BeanPropertyRowMapper(VentaContraEntrega.class));
				
	}
	
	@Override
	protected void adjustMainGrid(JXTable grid) {		
		grid.getColumnExt("Facturado").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));		
		grid.getColumnExt("Fecha (Pedido)").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		
		HighlightPredicate red=new HighlightPredicate() {
			public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
				VentaContraEntrega vv=(VentaContraEntrega)getFilteredSource().get(adapter.row);
				return vv.getRetrasoEnAsignacionHoras()>3;
			}
		};
		
		HighlightPredicate yellow=new HighlightPredicate() {
			public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
				VentaContraEntrega vv=(VentaContraEntrega)getFilteredSource().get(adapter.row);
				return vv.getRetrasoEnAsignacionHoras()>1 && vv.getRetrasoEnAsignacionHoras()<=3;
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
		FacturaForm.show(vc.getId());
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
	

}
