package com.luxsoft.siipap.dao.cxp;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Departamento;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.tesoreria.FormaDePago;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.model.tesoreria.Requisicion.Estado;
import com.luxsoft.siipap.service.LookupManager;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.service.tesoreria.RequisicionesManager;

public class ImpotadorDeRequisiciones {
	
	private RequisicionesManager requisicionManager;
	private JdbcTemplate jdbcTemplate;
	private LookupManager lookupManager;
	
	@SuppressWarnings("unchecked")
	public Requisicion importarRequisicion(final Long id){
		String sql="select REQUISICION_ID as ID,FECHA,p.NOMBRE as AFAVOR,TIPODEPAGO,IMPORTE from SW_REQUISICION  a join SW_PROVEEDORES p on(a.PROVEEDOR_ID=p.ID )where REQUISICION_ID=? ";
		Map<String, Object> row=getJdbcTemplate().queryForMap(sql,new Object[]{id});
		if(row.isEmpty())
			return null;
		
		Requisicion r=new Requisicion();		
		r.setAfavor(row.get("AFAVOR").toString());
		r.setFecha((Date)row.get("FECHA"));
		r.setOrigen(Requisicion.COMPRAS);
		r.setEstado(Estado.REVISADA);
		r.setFechaDePago(r.getFecha());
		String comentario="Requisicion importada de CXP:"+id;
		r.setComentario(comentario);
		String fp=(row.get("TIPODEPAGO").toString());
		if(fp.startsWith("C"))
			r.setFormaDePago(FormaDePago.CHEQUE);
		else 
			r.setFormaDePago(FormaDePago.TRANSFERENCIA);
		RequisicionDe det=new RequisicionDe();
		det.setSucursal(resolverSucursal());
		det.setDepartamento(resolverDepartamento());
		det.setComentario(comentario);
		det.setDocumento(String.valueOf(id));
		det.setFechaDocumento(r.getFecha());
		Number imp=(Number)row.get("IMPORTE");
		det.setTotal(CantidadMonetaria.pesos(imp.doubleValue()));
		r.agregarPartida(det);
		det.actualizarDelTotal();
		r.actualizarTotal();		
		r=getRequisicionManager().save(r);
		System.out.println(r);
		System.out.println(r.getFormaDePago());
		return r;
	}
	
	public Sucursal resolverSucursal(){
		List<Sucursal> sucs=getLookupManager().getSucursales();
		Sucursal ok=null;
		for(Sucursal s:sucs){
			if(s.getClave()==1){
				ok=s;
			}	
		}
		Assert.notNull(ok,"No localizo la sucursal de Oficinas");
		return ok;
	}
	
	public Departamento resolverDepartamento(){
		List<Departamento> deps=getLookupManager().getDepartamentos();
		System.out.println(deps);
		Departamento res=(Departamento)CollectionUtils.find(deps, new Predicate(){
			public boolean evaluate(Object object) {
				Departamento d=(Departamento)object;
				return d.getClave().equalsIgnoreCase("COMPRAS");
			}
		});
		Assert.notNull(res,"No existe el departamento de compras");
		return res;
	}
	
	
	public RequisicionesManager getRequisicionManager() {
		return requisicionManager;
	}
	public void setRequisicionManager(RequisicionesManager requisicionManager) {
		this.requisicionManager = requisicionManager;
	}
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	public LookupManager getLookupManager() {
		return lookupManager;
	}
	public void setLookupManager(LookupManager lookupManager) {
		this.lookupManager = lookupManager;
	}

	public static void main(String[] args) {
		ImpotadorDeRequisiciones imp=new ImpotadorDeRequisiciones();
		imp.setJdbcTemplate(ServiceLocator2.getJdbcTemplate());
		imp.setRequisicionManager(ServiceLocator2.getRequisiciionesManager());
		imp.setLookupManager(ServiceLocator2.getLookupManager());
		imp.importarRequisicion(256760L);
	}

}
