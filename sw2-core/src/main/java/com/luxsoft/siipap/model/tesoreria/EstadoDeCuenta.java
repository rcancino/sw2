package com.luxsoft.siipap.model.tesoreria;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Periodo;


/**
 * 
 * @author Ruben Cancino
 *
 */

public class EstadoDeCuenta extends BaseBean{
		
	private Cuenta cuenta;
	private Date fechaInicial;
	private Date fechaFinal;	
	private List<CargoAbono> movimientos=new ArrayList<CargoAbono>();
	private BigDecimal saldoInicial=BigDecimal.ZERO;
	
	
	
	public EstadoDeCuenta() {
		Periodo p=Periodo.getPeriodoDelMesActual();
		fechaInicial=p.getFechaInicial();
		fechaFinal=p.getFechaFinal();
	}
	
	
	public Cuenta getCuenta() {
		return cuenta;
	}
	public void setCuenta(Cuenta cuenta) {
		Object old=this.cuenta;
		this.cuenta = cuenta;
		firePropertyChange("cuenta", old, cuenta);
	}
	public List<CargoAbono> getMovimientos() {
		return movimientos;
	}
	public void setMovimientos(List<CargoAbono> movimientos) {
		this.movimientos = movimientos;
	}
	
	public BigDecimal getSaldoFinal() {
		return getSaldoInicial().add(getMovimientoDelPeriodo());
	}
	
	public BigDecimal getSaldoInicial() {
		return saldoInicial;
	}	
	public void setSaldoInicial(BigDecimal saldoInicial) {
		Object old=this.saldoInicial;
		this.saldoInicial = saldoInicial;
		firePropertyChange("saldoInicial", old, saldoInicial);
	}


	public Date getFechaFinal() {
		return fechaFinal;
	}
	public void setFechaFinal(Date fechaFinal) {
		this.fechaFinal = fechaFinal;
	}


	public Date getFechaInicial() {
		return fechaInicial;
	}


	public void setFechaInicial(Date fechaInicial) {
		this.fechaInicial = fechaInicial;
	}
	
	public BigDecimal getMovimientoDelPeriodo(){
		BigDecimal val=BigDecimal.ZERO;
		for(CargoAbono c:movimientos){			
			val=val.add(c.getImporte());
		}
		return val;
	}
	
	public BigDecimal getCargos(){
		BigDecimal val=BigDecimal.ZERO;
		for(CargoAbono c:movimientos){
			if(c.getImporte().doubleValue()<0)
				val=val.add(c.getImporte());
		}
		return val;
	}
	
	public BigDecimal getAbonos(){
		BigDecimal val=BigDecimal.ZERO;
		for(CargoAbono c:movimientos){
			if(c.getImporte().doubleValue()>0)
				val=val.add(c.getImporte());
		}
		return val;
	}
	


	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((cuenta == null) ? 0 : cuenta.hashCode());
		result = PRIME * result + ((fechaFinal == null) ? 0 : fechaFinal.hashCode());
		result = PRIME * result + ((fechaInicial == null) ? 0 : fechaInicial.hashCode());
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
		final EstadoDeCuenta other = (EstadoDeCuenta) obj;
		if (cuenta == null) {
			if (other.cuenta != null)
				return false;
		} else if (!cuenta.equals(other.cuenta))
			return false;
		if (fechaFinal == null) {
			if (other.fechaFinal != null)
				return false;
		} else if (!fechaFinal.equals(other.fechaFinal))
			return false;
		if (fechaInicial == null) {
			if (other.fechaInicial != null)
				return false;
		} else if (!fechaInicial.equals(other.fechaInicial))
			return false;
		return true;
	}


	@Override
	public String toString() {
		String pattern="{0} {1}";
		return MessageFormat.format(pattern, getCuenta(),getSaldoFinal());
	}
	
	

}
