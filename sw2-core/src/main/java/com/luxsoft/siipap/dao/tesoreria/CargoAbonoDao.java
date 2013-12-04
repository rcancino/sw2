package com.luxsoft.siipap.dao.tesoreria;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.siipap.model.tesoreria.EstadoDeCuenta;

public interface CargoAbonoDao extends GenericDao<CargoAbono, Long>{
	
	/**
	 * Regresa el saldo de una cuenta de banco
	 * 
	 * @param cta
	 * @return
	 */
	public BigDecimal saldo(final Cuenta cta);
	
	/**
	 * Elimina los registros impportados para una fecha
	 * 
	 * @param fecha
	 */
	public void limpiarMovimientosImportados(final Date fecha);
	
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public CargoAbono buscarAbonoImportado(Object id);
	
	
	/**
	 * Regresa una lista de todos los pagos relacionados con gastos
	 * 
	 * @return
	 */
	public List<CargoAbono> buscarPagosDeGastos();
	
	/**
	 * Actualiza el estado de cuenta indicado 
	 * 
	 * @param estado
	 */
	public void generarEstadoDeCuenta(final EstadoDeCuenta estado);
	
	/**
	 * Regresa el saldo de la cuenta indicada a la fecha. Esta fecha es incluida
	 * 
	 * @param cuentaId
	 * @param antesDe
	 * @return
	 */
	public BigDecimal buscarSaldo(final Long cuentaId,final Date antesDe);
	
	
	/**
	 * Busca el proximo numero de cheque para la cuenta indicada
	 * 
	 * @param cuentaId
	 * @return
	 */
	public long buscarProximoCheque(final Long cuentaId);
	
	public List<CargoAbono> buscarPagos(final Date fecha);
	
	/**
	 * Busca los egresos de un periodo para todas las cuentas 
	 * 
	 * @param p
	 * @return
	 */
	public List<CargoAbono> buscarEgresos(final Periodo p);

}
