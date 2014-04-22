package com.luxsoft.siipap.pos.ui.consultas;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
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

import com.luxsoft.sw3.services.Services;

/**
 * Consulta de facturas pendientes de asignar
 * 
 * @author Ruben Cancino
 *
 */
public class EntregasPendientesDeAsignarPanel extends FilteredBrowserPanel<PendienteDeAsignarRow>{

	public EntregasPendientesDeAsignarPanel() {
		super(PendienteDeAsignarRow.class);		
	}
	
	protected void init(){
		addProperty(
				"sucursal",
				"nombre",
				"origen",
				"contraEntrega",
				"formaPago",
				"fecha",
				"pedidoCreado",
				"pedidoFolio",
				"documento",
				"facturaCreado",
				"fechaEntrega",
				"total",
				"saldo",
				"importe",
				"entregado",
				"pendiente",
				"ultimoPago",
				"instruccionDeEntrega",
				"cargoId",
				"devolucionAplicada"
				);
		addLabels(
				"sucursal",
				"nombre",
				"origen",
				"contraEntrega",
				"formaPago",
				"fecha",
				"pedidoCreado",
				"pedidoFolio",
				"documento",
				"facturaCreado",
				"fechaEntrega",
				"total",
				"saldo",
				"importe",
				"entregado",
				"pendiente",
				"ultimoPago",
				"instruccionDeEntrega",
				"cargoId",
				"devolucionAplicada"
				);
		installTextComponentMatcherEditor("Factura", "documento");
		installTextComponentMatcherEditor("Cliente", "nombre");
//		Comparator  c1=GlazedLists.beanPropertyComparator(VentaContraEntrega.class, "retrasoEnAsignacionHoras");
	//	setDefaultComparator(GlazedLists.reverseComparator(c1));
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
	protected List<PendienteDeAsignarRow> findData() {
		String sql="SELECT (SELECT S.NOMBRE FROM SW_SUCURSALES S WHERE S.SUCURSAL_ID=V.SUCURSAL_ID) AS sucursal,V.nombre,V.origen,V.CE as contraEntrega,V.fpago"+
		" ,v.fecha ,V.PEDIDO_CREADO AS fecha_ped,V.PEDIDO_FOLIO AS pedido,V.DOCTO AS documento,V.CREADO AS facturado,STR_TO_DATE(e.fecha_entrega, '%Y-%m-%d %H:%i:%s') as fechaEntrega"+
		" ,V.total,V.TOTAL-IFNULL((SELECT SUM(A.IMPORTE) FROM sx_cxc_aplicaciones A WHERE A.CARGO_ID=V.CARGO_ID),0) AS saldo"+
		" ,v.importe,ifnull( (select sum(e.valor) from sx_entregas e where e.venta_id=V.cargo_id),0) as entregado "+
		" ,V.IMPORTE-ifnull( (select sum(e.valor) from sx_entregas e where e.venta_id=V.cargo_id),0) AS pendiente"+
		" ,(SELECT MAX(A.CREADO) FROM sx_cxc_aplicaciones A WHERE A.CARGO_ID=V.CARGO_ID) AS ultimoPago"+
		" ,V.INSTRUCCION_ENTREGA AS instruccion,V.CARGO_ID as id"+
		" ,IFNULL((SELECT SUM(A.IMPORTE) FROM sx_cxc_aplicaciones A "+
		" WHERE A.CARGO_ID=V.CARGO_ID and a.ABN_DESCRIPCION like '%DEV%'),0) AS devolucionAplicada"+
		" FROM sx_ventas v "+
		" JOIN sx_clientes C ON(C.CLIENTE_ID=V.CLIENTE_ID)"+ 
		" JOIN sx_pedidos P ON(P.PEDIDO_ID=V.PEDIDO_ID)"+
		" JOIN sx_pedidos_entregas E ON(E.INSTRUCCION_ID=P.INSTRUCCION_ID)"+
		" where V.FECHA>='2014/01/01' AND NOW()>STR_TO_DATE(e.fecha_entrega, '%Y-%m-%d %H:%i:%s')"+
		" AND IFNULL(V.COMENTARIO2,'') NOT LIKE '%CANCELAD%'"+
		" AND ifnull( (select sum(e.valor) from sx_entregas e where e.venta_id=V.cargo_id),0)=0"+
		" and v.total-IFNULL((SELECT SUM(A.IMPORTE) FROM sx_cxc_aplicaciones A WHERE A.CARGO_ID=V.CARGO_ID and a.ABN_DESCRIPCION like '%DEV%'),0)>1";
		return Services.getInstance().getJdbcTemplate().query(sql
				, new BeanPropertyRowMapper(PendienteDeAsignarRow.class));
				
	}
	
	@Override
	protected void adjustMainGrid(JXTable grid) {
		/*grid.setRowHeight(25);
		grid.setFont(new Font("Serif", Font.PLAIN, 18));*/
		//grid.getColumnExt("Facturado").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));		
		//grid.getColumnExt("Fecha (Pedido)").setCellRenderer(new DefaultTableRenderer(new Renderers.ToHourConverter()));
		/*grid.getColumnExt("Facturado").setCellRenderer(new DefaultTableRenderer(new MyDateConverter()));
		grid.getColumnExt("Fecha (Pedido)").setCellRenderer(new DefaultTableRenderer(new MyDateConverter()));
		grid.getColumnExt("Cliente").setMaxWidth(300);*/
		
	/*	HighlightPredicate red=new HighlightPredicate() {
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
		);*/
	}
	
	@Override
	protected void doSelect(Object bean) {
/*		VentaContraEntrega vc=(VentaContraEntrega)bean;
		//FacturaForm.show(vc.getId());
		ReportUtils2.imprimirFacturaCopia(vc.getId());*/
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
