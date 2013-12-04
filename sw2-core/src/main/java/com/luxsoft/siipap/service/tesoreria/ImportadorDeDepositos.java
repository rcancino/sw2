package com.luxsoft.siipap.service.tesoreria;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.luxsoft.siipap.dao.UniversalDao;
import com.luxsoft.siipap.dao.tesoreria.CargoAbonoDao;
import com.luxsoft.siipap.model.Autorizacion;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Concepto;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.siipap.model.tesoreria.FormaDePago;
import com.luxsoft.siipap.model.tesoreria.Origen;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;

/**
 * Tarea para importar depositos de ventas
 * 
 * @author Ruben Cancino
 *
 */
public class ImportadorDeDepositos extends JdbcDaoSupport{
	
	private CargoAbonoDao cargoAbonoDao;
	private UniversalDao universalDao;
	
	 
	@SuppressWarnings("unchecked")
	public void importar(final Date fecha){
		//getCargoAbonoDao().limpiarMovimientosImportados(fecha);
		final String sql="SELECT * FROM SW_DEPOSITOS D WHERE D.FECHACOBRANZA=? AND D.REVISADA=1";
		List<Map<String, Object>> rows=getJdbcTemplate()
		.queryForList(sql, new Object[]{fecha}, new int[]{Types.DATE});
		System.out.println("Depositos: "+rows.size());
		Autorizacion aut=new Autorizacion(new Date(),getUser());
		aut.setComentario("IMPORTACION");
		
		List<Cuenta> cuentas=getUniversalDao().getAll(Cuenta.class);
		System.out.println("Cuentas: "+cuentas);
		
		for(Map<String,Object> row:rows){
			
			String ctaNumero=(String)row.get("CUENTA");
			ctaNumero=StringUtils.remove(ctaNumero, '-');
			final String numeroDeCuenta=ctaNumero.trim();
			BigDecimal importe=(BigDecimal)row.get("IMPORTE");
			String origen=(String)row.get("ORIGEN");
			BigDecimal sucursal=(BigDecimal)row.get("SUCURSALID");
			String fp=(String)row.get("FORMADP");
			
			if(importe.doubleValue()==0)
				continue;
			
			Cuenta cta=(Cuenta)CollectionUtils.find(cuentas, new Predicate(){
				public boolean evaluate(Object object) {
					Cuenta cc=(Cuenta)object;
					return cc.getNumero().equals(Long.valueOf(numeroDeCuenta));
				}				
			});
			if(cta==null){
				System.out.println("No encontro la cuenta: "+numeroDeCuenta);
				return;
				//cta=cuentas.get(0);
			}
			Origen org;
			if(origen.equalsIgnoreCase("CRE")){
				org=Origen.VENTA_CREDITO;
			}else if(origen.equalsIgnoreCase("CAM")){
				org=Origen.VENTA_CAMIONETA;
			}else if(origen.equalsIgnoreCase("MOS")){
				org=Origen.VENTA_MOSTRADOR;
			}else {
				org=Origen.VENTA_CONTADO;
			}
			
			Long id=223693l;			
			if(org.equals(Origen.VENTA_CONTADO)){
				id=223621l;
			}
			Concepto tipo=(Concepto)universalDao.get(Concepto.class, id);
			Sucursal suc=getSucursal(sucursal.intValue());			
			CargoAbono deposito=CargoAbono.crearAbono(cta, importe, fecha, tipo, suc);
			deposito.setOrigen(org);			
			deposito.setFormaDePago(getFormaDePago(fp));			
			deposito.setAutorizacion(aut);
			try {
				Number depId=(Number)row.get("DEOPSITO_ID");
				deposito.setComentario(String.valueOf(depId.longValue()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			Number folio=(Number)row.get("FOLIO");
			if(folio!=null){
				deposito.setReferencia(String.valueOf(folio.longValue()));
			}
			CargoAbono ca=getCargoAbonoDao().buscarAbonoImportado(deposito.getComentario());
			if(ca==null)
				getCargoAbonoDao().save(deposito);
				//System.out.println("Importando: "+deposito);
			else
				System.out.println("Cargo ya registrado");
		}
	}
	
	private List<Sucursal> sucs;
	
	@SuppressWarnings("unchecked")
	private Sucursal getSucursal(final int clave){
		if(sucs==null){
			sucs=universalDao.getAll(Sucursal.class);
		}
		return (Sucursal) CollectionUtils.find(sucs, new Predicate(){
			public boolean evaluate(Object object) {
				Sucursal ss=(Sucursal)object;
				return ss.getClave()==clave;
			}			
		});
	}
	
	User user;
	
	private User getUser(){
		if(user==null)
			user=ServiceLocator2.getUserManager().getUserByUsername("admin");
		return user;
	}
	
	private FormaDePago getFormaDePago(String fp){
		if(fp.equals("B"))
			return FormaDePago.CHEQUE;
		if(fp.equals("C"))
			return FormaDePago.CHEQUE;
		if(fp.equals("E"))
			return FormaDePago.CHEQUE;
		if(fp.equals("H"))
			return FormaDePago.CHEQUE;
		if(fp.equals("N"))
			return FormaDePago.TRANSFERENCIA;
		if(fp.equals("O"))
			return FormaDePago.ORDEN;
		if(fp.equals("Q"))
			return FormaDePago.CHEQUE;
		if(fp.equals("X"))
			return FormaDePago.CHEQUE;
		if(fp.equals("Y"))
			return FormaDePago.CHEQUE;
		else
			return FormaDePago.CHEQUE;

	}

	public CargoAbonoDao getCargoAbonoDao() {
		return cargoAbonoDao;
	}
	public void setCargoAbonoDao(CargoAbonoDao cargoAbonoDao) {
		this.cargoAbonoDao = cargoAbonoDao;
	}

	public UniversalDao getUniversalDao() {
		return universalDao;
	}

	public void setUniversalDao(UniversalDao universalDao) {
		this.universalDao = universalDao;
	}
	
	public static void main(String[] args) {
		ImportadorDeDepositos imp=(ImportadorDeDepositos)ServiceLocator2.instance().getContext().getBean("importadorDeDepositos");
		imp.importar(DateUtil.toDate("31/05/2008"));
	}
	

}
