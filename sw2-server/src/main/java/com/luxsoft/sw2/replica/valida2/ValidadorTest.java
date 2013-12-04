package com.luxsoft.sw2.replica.valida2;

import java.sql.Types;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw2.replica.valida.ConnectionServices;
import com.luxsoft.sw3.replica.EntityLog;


public class ValidadorTest {
	
	protected Set<Long> sucursales=new HashSet<Long>();
	
	public void validar(){
		validar(Periodo.hoy());
	}
	
	public void validar(String fechaIni,String fechaFin){
		validar(new Periodo(fechaIni,fechaFin));
	}
	
	public void validar(Periodo periodo){
		for(Long sucursalId:sucursales){
			validar(periodo,sucursalId);
		}
	}
		
	public void validar(Periodo periodo,Long sucursalId){
		final String sql="SELECT CLIENTE_ID,MODIFICADO FROM SX_CLIENTES WHERE DATE(MODIFICADO) BETWEEN ? AND ?";
		Object[] parametros={
				new SqlParameterValue(Types.DATE,periodo.getFechaInicial())
				,new SqlParameterValue(Types.DATE,periodo.getFechaFinal())
		};
		List<RevBean> central=ServiceLocator2.getJdbcTemplate().query(sql, parametros, new BeanPropertyRowMapper(RevBean.class));
		
		//En la succursal
		JdbcTemplate sucursalTemplate=ConnectionServices.getInstance().getJdbcTemplate(sucursalId);
		final List<RevBean> sucursal=sucursalTemplate.query(sql, parametros, new BeanPropertyRowMapper(RevBean.class));
		
		System.out.println(MessageFormat.format("Registros Central: {0} and Sucrsal: {1}",central.size(),sucursal.size()) );
		
		/*//Caso 1 Faltantes en sucursal
		Collection<RevBean> faltantesSucursal=CollectionUtils.subtract(central, sucursal);
		System.out.println("Faltantes sucursal: "+faltantesSucursal.size());
		for(RevBean b:faltantesSucursal){
			System.out.println(b);
		}	*/
		
		Collection<RevBean> pendientesSucursal=CollectionUtils.select(central, new Predicate() {
			public boolean evaluate(Object object) {
				final RevBean source=(RevBean)object;
				RevBean target=(RevBean)CollectionUtils.find(sucursal,PredicateUtils.equalPredicate(source));
				if(target==null) return true;
				return !source.getMODIFICADO().equals(target.getMODIFICADO());
			}
		});
		for(RevBean b:pendientesSucursal){
			Cliente target=ServiceLocator2.getClienteManager().get(b.getCLIENTE_ID());
			EntityLog log=new EntityLog(target,target.getId(),"OFICINAS",EntityLog.Tipo.CAMBIO);
			ServiceLocator2.getReplicaMessageCreator().enviar(log);
		}
	}
	
	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}
	
	public void addSucursal(Long...ids){
		sucursales.clear();
		for(Long id:ids){
			sucursales.add(id);
		}
	}
	
	
	public static class RevBean{
		Long CLIENTE_ID;
		Date MODIFICADO;
		
		public RevBean(){}
		
		public Long getCLIENTE_ID() {
			return CLIENTE_ID;
		}
		public void setCLIENTE_ID(Long cLIENTE_ID) {
			CLIENTE_ID = cLIENTE_ID;
		}
		public Date getMODIFICADO() {
			return MODIFICADO;
		}
		public void setMODIFICADO(Date mODIFICADO) {
			MODIFICADO = mODIFICADO;
		}
		
		
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((CLIENTE_ID == null) ? 0 : CLIENTE_ID.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RevBean other = (RevBean) obj;
			if (CLIENTE_ID == null) {
				if (other.CLIENTE_ID != null)
					return false;
			} else if (!CLIENTE_ID.equals(other.CLIENTE_ID))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "RevBean [CLIENTE_ID=" + CLIENTE_ID + ", MODIFICADO="
					+ MODIFICADO + "]";
		}
		
		
	}
	
	public static void main(String[] args) {
		ValidadorTest v=new ValidadorTest();
		v.addSucursal(2L);
		v.validar("","");
	}

}
