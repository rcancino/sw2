package com.luxsoft.sw3.bi.consultas;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SqlParameterValue;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.sw3.bi.AnalisisDeVenta;
import com.luxsoft.sw3.bi.BIVenta;
import com.luxsoft.sw3.bi.BIVentasPorCliente;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

public class BIVentasPorClienteController {
	
	private EventList<BIVenta> ventas;
	private EventList<AnalisisDeVenta> ventasPorSucursal;
	private GroupingList<BIVenta> ventasPorPeriodo;
	
	private Periodo periodo;
	private Comparator<BIVenta> periodoComparator;
	 
	
	public BIVentasPorClienteController(){
		init();
	}
	protected void init(){
		ventas=new BasicEventList<BIVenta>(0);
		ventasPorSucursal=new BasicEventList<AnalisisDeVenta>();		
		ventasPorPeriodo=new GroupingList<BIVenta>(ventas,getPeriodoComparator());
	}
	public EventList<BIVenta> getVentas() {
		return ventas;
	}
	
	
	public EventList<AnalisisDeVenta> getVentasPorSucursal() {
		return ventasPorSucursal;
	}
	public List<BIVentasPorCliente> findData() {
		String sql="select a.cliente_id ,x.nombre as clienteNombre,sum(importe_bruto) as importeBruto ,sum(descuentos) as descuentos,sum(cargos) as cargos,sum(flete) as flete,sum(importe) as importe,sum(impuesto) as impuesto,sum(total) as total,sum(devolucion2) as devoluciones,sum(bonificacion) as bonificaciones,sum(costo) as costo,sum(kilos) as kilos from bi_ventas a  join sx_clientes x on(a.cliente_id=x.cliente_id) " +
				"where a.fecha between ? and ? group by cliente_id,x.nombre";
		Object[] args={
				 new SqlParameterValue(Types.DATE,getPeriodo().getFechaInicial())
				,new SqlParameterValue(Types.DATE,getPeriodo().getFechaFinal())
				};
		List<BIVentasPorCliente>  res=ServiceLocator2.getJdbcTemplate()
		.query(sql,args, new BeanPropertyRowMapper(BIVentasPorCliente.class));
		CantidadMonetaria importeTotal=CantidadMonetaria.pesos(0);
		CantidadMonetaria utilidadAcumulada=CantidadMonetaria.pesos(0);
		for(BIVentasPorCliente v:res){
			importeTotal=importeTotal.add(CantidadMonetaria.pesos(v.getImporteNeto()));
			utilidadAcumulada=utilidadAcumulada.add(CantidadMonetaria.pesos(v.getUtilidad()));
		}
		for(BIVentasPorCliente v:res){
			v.setTotalSegmento(importeTotal.amount());
			v.setTotalUtilidadPorPeriodo(utilidadAcumulada.amount());
		}
		return res;
	}
	
	public Periodo getPeriodo() {
		if(periodo==null)
			periodo=Periodo.periodoDeloquevaDelYear();
		return periodo;
	}
	public void setPeriodo(Periodo periodo) {
		this.periodo = periodo;
	}
	
	public void cargarDetalles(final BIVentasPorCliente bi){
		getVentasPorSucursal().clear();
		final SwingWorker<Object[],String> worker=new SwingWorker<Object[],String>(){

			@Override
			protected Object[] doInBackground() throws Exception {
				Object[] data=new Object[1];
				data[0]=buscarVentasPorSucursal(bi);
				return data;
			}
			@Override
			protected void done() {
				try {
					List<AnalisisDeVenta> s1=(List<AnalisisDeVenta>)get()[0];
					System.out.println("Analisis taladrados: "+s1.size());
					getVentasPorSucursal().clear();
					getVentasPorSucursal().addAll(s1);
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
			
			
		};
		TaskUtils.executeSwingWorker(worker, "Taladrando información", "Análisis de ventas para: "+bi.getClienteNombre());
	}
	
	public List<AnalisisDeVenta> buscarVentasPorSucursal(final BIVentasPorCliente bi){
		String sql="select x.nombre as entidad" +
				",a.origen as descripcion" +
				",sum(importe_bruto) as ventasBrutas" +
				",sum(descuentos) as descuentos" +
				",sum(cargos) as cargos" +
				",sum(flete) as flete" +
				",sum(importe) as importe" +
				",sum(impuesto) as impuesto" +
				",sum(total) as total" +
				",sum(devolucion2) as devoluciones" +
				",sum(bonificacion) as bonificaciones" +
				",sum(costo) as costo" +
				",sum(kilos) as kilos " +
				" from bi_ventas a  join sw_sucursales x on(a.sucursal_id=x.sucursal_id) " +
				" where a.fecha between ? and ? " +
				" and a.cliente_id=? " +
				" group by x.nombre ,a.origen";
		Object[] args={
				 new SqlParameterValue(Types.DATE,getPeriodo().getFechaInicial())
				,new SqlParameterValue(Types.DATE,getPeriodo().getFechaFinal())
				 ,new SqlParameterValue(Types.NUMERIC,bi.getCliente_id())
				};
		List<AnalisisDeVenta> res=ServiceLocator2.getJdbcTemplate().query(sql, args, new BeanPropertyRowMapper(AnalisisDeVenta.class));
		return res;
	}
	public Comparator<BIVenta> getPeriodoComparator() {
		if(periodoComparator==null){
			periodoComparator=GlazedLists.beanPropertyComparator(BIVenta.class, "year","mes");
		}
		return periodoComparator;
	}
	public void setPeriodoComparator(Comparator<BIVenta> periodoComparator) {
		this.periodoComparator = periodoComparator;
	}
	public GroupingList<BIVenta> getVentasPorPeriodo() {
		return ventasPorPeriodo;
	}
	
	

}
