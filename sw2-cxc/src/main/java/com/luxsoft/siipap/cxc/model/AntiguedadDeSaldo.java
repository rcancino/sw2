package com.luxsoft.siipap.cxc.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.luxsoft.siipap.cxc.util.CargoRowUtils;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.util.DateUtil;

public class AntiguedadDeSaldo implements Comparable<AntiguedadDeSaldo>,Serializable{
	
	private String cliente;
	private int plazo;
	private String tipoVencimiento;
	private CantidadMonetaria limite=CantidadMonetaria.pesos(0);
	
	
	
	//private CantidadMonetaria saldo=CantidadMonetaria.pesos(0);
	//private CantidadMonetaria vencido=CantidadMonetaria.pesos(0);
	private CantidadMonetaria totalGlobal=CantidadMonetaria.pesos(0);
	
	private List<CargoRow> cargos;
	
	public AntiguedadDeSaldo(){}
	
	
	
	public AntiguedadDeSaldo(final List<CargoRow> cargos){
		setCargos(cargos);
	}
	
	public String getCliente() {
		return cliente;
	}
	public void setCliente(String cliente) {
		this.cliente = cliente;
	}
	public int getPlazo() {
		return plazo;
	}
	public void setPlazo(int plazo) {
		this.plazo = plazo;
	}
	public String getTipoVencimiento() {
		return tipoVencimiento;
	}
	public void setTipoVencimiento(String tipoVencimiento) {
		this.tipoVencimiento = tipoVencimiento;
	}
	public CantidadMonetaria getLimite() {
		return limite;
	}
	public void setLimite(CantidadMonetaria limite) {
		this.limite = limite;
	}
	public int getCuentasPorCobrar() {
		return cargos.size();
	}
	
	public Date getFechaMaxima() {
		Date last=null;
		for(CargoRow c:cargos){
			Date pivot=DateUtil.truncate(c.getVencimiento(),Calendar.DATE);
			if(last==null){
				last=pivot;
				continue;
			}else{
				if(pivot.compareTo(last)<0)
					last=pivot;
			}			
		}
		return last;
	}
	
	
	public CantidadMonetaria getSaldo() {
		return CargoRowUtils.calcularSaldo(cargos);
	}
	
	public CantidadMonetaria getVencido() {
		return CargoRowUtils.calcularSaldoVencido(cargos);
	}
	
	public CantidadMonetaria getPorVencer() {
		return CargoRowUtils.calcularSaldoPorVencer(cargos);
	}
	
	public List<CargoRow> getCargos() {
		return cargos;
	}

	public void setCargos(List<CargoRow> cargos) {
		this.cargos = cargos;
		this.cliente=cargos.get(0).getNombreRazon();
		this.limite=cargos.get(0).getLimite();
		this.tipoVencimiento=cargos.get(0).getTipoVencimiento();
		this.plazo=cargos.get(0).getPlazoCliente();
	}
	
	
	
	public CantidadMonetaria getVencido1_30(){
		return CargoRowUtils.getVencido1_30(cargos);
	}
	
	public CantidadMonetaria getVencido31_60(){
		return CargoRowUtils.getVencido31_60(cargos);
	}
	
	public CantidadMonetaria getVencido61_90(){
		return CargoRowUtils.getVencido61_90(cargos);
	}
	
	public CantidadMonetaria getVencido90(){
		return CargoRowUtils.getVencidoMasDe90(cargos);
	}

	public CantidadMonetaria getTotalGlobal() {
		return totalGlobal;
	}

	public void setTotalGlobal(CantidadMonetaria totalGlobal) {
		this.totalGlobal = totalGlobal;
	}
	
	public double getParticipacion(){
		if(getSaldo().amount().doubleValue()>0 && totalGlobal.amount().doubleValue()>0){
			double res= getSaldo().amount().doubleValue()/totalGlobal.amount().doubleValue();
			return res*100;
		}
		return 0d;
	}
	
	public int getParticipacionAsInt(){
		int row=(int)getPlazo()*100;
		return row;
	}

	public String toString(){
		return this.cliente+" Saldo: "+getSaldo();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cliente == null) ? 0 : cliente.hashCode());
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
		final AntiguedadDeSaldo other = (AntiguedadDeSaldo) obj;
		if (cliente == null) {
			if (other.cliente != null)
				return false;
		} else if (!cliente.equals(other.cliente))
			return false;
		return true;
	}
	
	public int compareTo(AntiguedadDeSaldo o) {
		return this.cliente.compareTo(o.getCliente());
	}
	

}
