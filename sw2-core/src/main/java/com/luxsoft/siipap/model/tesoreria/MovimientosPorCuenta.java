package com.luxsoft.siipap.model.tesoreria;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.Assert;

/**
 * Movimientos registrados para una cuenta
 * JavaBean utilizado principalmente para UI
 * 
 * @author Ruben Cancino
 *
 */
public class MovimientosPorCuenta {
	
	private Cuenta cuenta;
	private BigDecimal saldo;
	//private List<CargoAbono> movimientos=new ArrayList<CargoAbono>();
	
	
	
	public MovimientosPorCuenta() {}
	
	

	public Cuenta getCuenta() {
		return cuenta;
	}
	public void setCuenta(Cuenta cuenta) {
		this.cuenta = cuenta;
	}
	public BigDecimal getSaldo() {
		return saldo;
	}
	public void setSaldo(BigDecimal saldo) {
		this.saldo = saldo;
	}
	
	
	
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((cuenta == null) ? 0 : cuenta.hashCode());
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
		final MovimientosPorCuenta other = (MovimientosPorCuenta) obj;
		if (cuenta == null) {
			if (other.cuenta != null)
				return false;
		} else if (!cuenta.equals(other.cuenta))
			return false;
		return true;
	}
	
	public static MovimientosPorCuenta generarResumenMovimientos(final List<CargoAbono> movs){
		Assert.notEmpty(movs,"La lista de movimientos no debe estar vacia");
		validarMismaCuenta(movs);
		MovimientosPorCuenta m=new MovimientosPorCuenta();
		m.setCuenta(movs.get(0).getCuenta());
		actualizarSaldo(m,movs);
		return m;
	}
	
	public static void actualizarSaldo(MovimientosPorCuenta mo,List<CargoAbono> movs){
		BigDecimal saldo=BigDecimal.ZERO;
		for(CargoAbono m:movs){
			if(!m.getConciliado())
				saldo=saldo.add(m.getImporte());
		}
		mo.setSaldo(saldo);
	}
	
	/**
	 * Valida que todos los movimeintos sean de la misma cuenta
	 * 
	 * @param movs
	 */
	public  static void validarMismaCuenta(List<CargoAbono> movs){
		final Cuenta c=movs.get(0).getCuenta();
		CollectionUtils.forAllDo(movs, new Closure(){
			public void execute(Object input) {
				CargoAbono cargo=(CargoAbono)input;
				Assert.isTrue(cargo.getCuenta().equals(c),"Inconsistencia en los movimientos no todos son de la misma cuenta");
			}
			
		});
	}

}
