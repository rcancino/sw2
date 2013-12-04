package com.luxsoft.sw3.tasks.parches;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.siipap.ventas.model.VentaDet;
import com.luxsoft.sw3.embarque.Embarque;
import com.luxsoft.sw3.embarque.Entrega;
import com.luxsoft.sw3.embarque.EntregaDet;
import com.luxsoft.sw3.embarque.Transporte;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.InstruccionDeEntrega;

public class ActualizarEmbarques implements RowMapper{
	
	private int rows=0;
	private int current=0;
	
	private JdbcTemplate template;
	
	public ActualizarEmbarques migrar(){
		template=Services.getInstance().getJdbcTemplate();
		rows=template.queryForInt("select count(*) from sx_embarques_bak");
		System.out.println("Embarques a migrar: "+rows);
		String sql="SELECT * FROM SX_EMBARQUES_BAK";
		List<Embarque> embarques=Services.getInstance().getJdbcTemplate().query(sql, this);
		for(Embarque e:embarques){
			try {
				migrarEntregas(e);
				Services.getInstance().getUniversalDao().save(e);
				current++;
				System.out.println("Embarque migrado: "+e.getDocumento()+ "  "+current+ " De:" +rows);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			
		}
		return this;
	}
	
	
	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		Embarque e=new Embarque();
		Long id=rs.getLong("EMBARQUE_ID");
		e.setDocumento(id);
		e.setChofer(rs.getString("CHOFER"));
		e.setComentario(rs.getString("COMENTARIO"));
		e.setFecha(rs.getDate("FECHA"));
		e.setRegreso(rs.getTimestamp("REGRESO"));
		e.setSalida(rs.getTimestamp("SALIDA"));
		e.setSucursal(rs.getString("SUCURSAL"));
		Long transporte=rs.getLong("TRANSPORTE_ID");
		Transporte t=(Transporte)Services.getInstance().getUniversalDao().get(Transporte.class, transporte);
		e.setTransporte(t);
		//migrarEntregas(e);
		//Services.getInstance().getUniversalDao().save(e);
		//current++;
		//System.out.println("Embarque migrado: "+id+ "  "+current+ " De "+rows);
		return e;
		
	}
	
	public void migrarEntregas(final Embarque embarque){
		String sql="SELECT * FROM SX_ENTREGAS_BAK WHERE EMBARQUE_ID=?";
		template.query(sql,new Object[]{embarque.getDocumento()}, new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				Entrega e=new Entrega();
				e.setOldId(rs.getLong("ENTREGA_ID"));
				String venta_id=rs.getString("VENTA_ID");
				Venta v=Services.getInstance().getFacturasManager().getFactura(venta_id);
				e.setFactura(v);
				e.setCantidad(rs.getDouble("CANTIDAD"));
				e.setComentario(rs.getString("COMENTARIO"));
				e.setComision(rs.getDouble("COMISION"));
				e.setImporteComision(rs.getBigDecimal("COMISION_IMP"));
				e.setPaquetes(rs.getInt("PAQUETES"));
				e.setParcial(rs.getBoolean("PARCIAL"));
				e.setPorCobrar(rs.getBigDecimal("POR_COBRAR"));
				e.setRecepcion(rs.getTimestamp("RECEPCION"));
				e.setRecibio(rs.getString("RECIBIO"));
				e.setSurtido(rs.getTimestamp("SURTIDO"));
				e.setTotalFactura(rs.getBigDecimal("TOTAL_DOCTO"));
				e.setValor(rs.getBigDecimal("VALOR"));
				InstruccionDeEntrega i=(InstruccionDeEntrega)Services.getInstance().getUniversalDao().get(InstruccionDeEntrega.class, rs.getLong("INSTRUCCION_ID"));
				e.setInstruccionDeEntrega(i);
				migrarEntregasDet(e);
				embarque.agregarUnidad(e);
				
			}
		});
	}
	
	private void migrarEntregasDet(final Entrega e){
		String sql="SELECT " +
				"ENTREGADET_ID" +
				",CANTIDAD" +
				",PRODUCTO" +
				",DESCRIPCION" +
				",ENTREGADO_ANTERIOR" +
				",FACTURA,VALOR" +
				",VENTADET_ID" +
				",PRODUCTO_ID" +
				",ENTREGA_ID " +
				" FROM sx_entregas_det_bak WHERE ENTREGA_ID=?";
		template.query(sql,new Object[]{e.getOldId()}, new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				String vid=rs.getString("VENTADET_ID");
				VentaDet vdet=(VentaDet)Services.getInstance().getUniversalDao().get(VentaDet.class, vid);
				double entregado=rs.getDouble("ENTREGADO_ANTERIOR");
				EntregaDet det=new EntregaDet(vdet,entregado);
				det.setCantidad(rs.getDouble("CANTIDAD"));
				det.setValor(rs.getBigDecimal("VALOR"));
				e.agregarEntregaUnitaria(det);
			}
		});
	}
	
	public static void main(String[] args) {
		new ActualizarEmbarques().migrar();
	}

	

}
