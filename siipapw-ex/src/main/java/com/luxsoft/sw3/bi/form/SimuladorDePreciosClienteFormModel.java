package com.luxsoft.sw3.bi.form;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;

import com.luxsoft.siipap.inventarios.model.CostoPromedio;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.MasterDetailFormModel;
import com.luxsoft.sw3.bi.SimuladorDePreciosPorCliente;
import com.luxsoft.sw3.bi.SimuladorDePreciosPorCliente.TipoPrecio;
import com.luxsoft.sw3.bi.SimuladorDePreciosPorClienteDet;


/**
 * 
 * @author OCTAVIO
 *		
 */

public class SimuladorDePreciosClienteFormModel extends MasterDetailFormModel{
	
	private EventList<Cliente> clientes=new BasicEventList<Cliente>();
	public static Logger logger=Logger.getLogger(SimuladorDePreciosClienteFormModel.class);

	public SimuladorDePreciosClienteFormModel() {
		super(SimuladorDePreciosPorClienteDet.class);
		
	}
	
	public SimuladorDePreciosClienteFormModel(Object bean,boolean readOnly){
		super(bean, readOnly);
	}
	
	public SimuladorDePreciosClienteFormModel(Object bean) {
		super(bean);		
	}
	
	public SimuladorDePreciosPorCliente getLista(){
		return (SimuladorDePreciosPorCliente)getBaseBean();
	}
	
	public  boolean manejaTotalesEstandares(){
		return false;
	}
	
	@SuppressWarnings("unchecked")
	protected void init(){
		super.init();
		if(getLista().getId()!=null ){
			for(SimuladorDePreciosPorClienteDet det:getLista().getPrecios()){
				source.add(det);
			}
		}		
		//Handlers
		getModel("cliente").addValueChangeListener(new ClienteHandler());
	}
	
	public boolean clienteModificable(){
		return getLista().getId()==null;
	}
	
	public EventList<Cliente> getClientes() {
		return clientes;
	}

	public void setClientes(EventList<Cliente> clientes) {
		this.clientes = clientes;
	}

	public void loadClientes(){
		clientes.clear();
		clientes.addAll(ServiceLocator2.getClienteManager().getAll());
	}
	
	
	@SuppressWarnings("unchecked")
	public Object insertDetalle(final Object obj){
		SimuladorDePreciosPorClienteDet det=(SimuladorDePreciosPorClienteDet)obj;
		if(getLista().agregarPartida(det)){
			source.add(det);
			return det;
		}else
			return null;		
	}  
	
	
	public boolean deleteDetalle(final Object obj){
		SimuladorDePreciosPorClienteDet part=(SimuladorDePreciosPorClienteDet)obj;
		boolean res=getLista().removerPrecio(part);
		if(res){
			return source.remove(part);
		}
		return false;
	}
	
	public void limpiarPartidas(){
		if(getLista().getId()==null){
			getLista().eliminarPrecios();
			source.clear();
			logger.debug("Partidas eliminadas");
		}
	}
	
	public void doListUpdated(ListEvent listChanges){
		int index=listChanges.getIndex();
		Object updated=listChanges.getSourceList().get(index);
		//System.out.println("Cambio detectado desde el model en :"+updated);
	}
	
	
	
	private class ClienteHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			limpiarPartidas();
			if( (getLista().getCliente()!=null) && (getLista().getCliente().getCredito()!=null) )
				getLista().setDescuentoFijo(getLista().getCliente().getCredito().getDescuentoEstimado());
		}		
	}
	
	public void cargar(){
		Cliente c=getLista().getCliente();
		if(c!=null){
			String sql="select producto_id,linea, clave,descripcion,sum(IMP_NETO) as VENTA,sum(CANTIDAD) as CANTIDAD " +
					" from fact_ventasdet v where v.CLIENTE_ID=? and fecha between ? and ? group by linea, clave,descripcion order by linea,5 desc";
			Object[] params=new Object[]{
				c.getId()
				,new SqlParameterValue(Types.DATE, getLista().getFechaInicial())	
				,new SqlParameterValue(Types.DATE, getLista().getFechaFinal())
			};
			int year=Periodo.obtenerYear(getLista().getFechaFinal());
			int mes=Periodo.obtenerMes(getLista().getFechaFinal());
			List res=ServiceLocator2.getJdbcTemplate().query(sql, params, new SimuladorDetMapper(year,mes+1));
			getLista().getPrecios().clear();
			source.clear();
			getLista().getPrecios().addAll(res);
			source.addAll(res);
			actualizar();
		}
		
	}
	
	public void actualizar(){
		for(int index=0;index < source.size();index++){
			SimuladorDePreciosPorClienteDet row=(SimuladorDePreciosPorClienteDet) source.get(index);
			//Actualizar costo
			if(row.getCostoPromedioRef()!=null){
				CostoPromedio cp=row.getCostoPromedioRef();
				switch (getLista().getTipoCosto()) {
				case COSTO_PROMEDIO:
					row.setCosto(cp!=null?cp.getCostop():BigDecimal.ZERO);
					break;
				case COSTO_REPO:
					row.setCosto(cp!=null?cp.getCostoRepo():BigDecimal.ZERO);
					break;
				case COSTO_ULTIMO:
					row.setCosto(cp!=null?cp.getCostoUltimo():BigDecimal.ZERO);
					break;
				default:
					row.setCosto(BigDecimal.ZERO);
					break;
				}
				source.set(index, row);
			}
			if(getLista().getTipoPrecio().equals(TipoPrecio.CREDITO)){
				row.setPrecioDeLista(row.getProducto().getPrecioCredito());
			}else{
				row.setPrecioDeLista(row.getProducto().getPrecioContado());
			}
			row.setDescuento(getLista().getDescuento());
			
			if(row.getMargenCalculado()<getLista().getMargenMinimo()){
				double factor=1+getLista().getMargenMinimo()/100;
				row.setPrecioMinimo(row.getCosto().multiply(BigDecimal.valueOf(factor)));
				//row.setPrecioMinimo(row.getPrecioNeto());
				//row.setPrecioMinimo(row.getCosto());

			}else{
				row.setPrecioMinimo(row.getPrecioNeto());
			}
		}
	}
	
	
	public  class SimuladorDetMapper implements RowMapper{
		
		private int year;
		private int mes;
		
		public SimuladorDetMapper(int year,int mes){
			this.year=year;
			this.mes=mes;
		}
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			SimuladorDePreciosPorClienteDet row=new SimuladorDePreciosPorClienteDet();			
			Producto p=ServiceLocator2.getProductoManager().get(rs.getLong("producto_id"));
			row.setProducto(p);
			row.setPrecioDeLista(p.getPrecioCredito());
			row.setCantidadAcumulada(rs.getBigDecimal("CANTIDAD"));
			
			CostoPromedio cp=ServiceLocator2.getCostoPromedioManager().buscarCostoPromedio(year, mes, p.getClave());
			row.setCostoPromedioRef(cp);
			
			return row;
		}
	}

}
