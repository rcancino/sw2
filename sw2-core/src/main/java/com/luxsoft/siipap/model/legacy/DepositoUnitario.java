package com.luxsoft.siipap.model.legacy;

import java.math.BigDecimal;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Min;
import org.hibernate.validator.NotEmpty;

import com.luxsoft.siipap.model.BaseBean;




public class DepositoUnitario extends BaseBean{
	
	@NotEmpty 
	private String banco;
	
	@Min (value=1)
	private int numero;
	
	
	private BigDecimal importe=BigDecimal.ZERO;
	
	private int grupo;
	
	private Deposito deposito;
	
	
	public DepositoUnitario() {}
	
	public DepositoUnitario(String banco, int numero, BigDecimal importe) {		
		this.banco = banco;
		this.numero = numero;
		this.importe = importe;
	}
	
	public Deposito getDeposito() {
		return deposito;
	}
	public void setDeposito(Deposito deposito) {
		this.deposito = deposito;
	}
	
	public String getBanco() {
		return banco;
	}
	public void setBanco(String banco) {
		Object oldValue=this.banco;
		this.banco = banco;
		firePropertyChange("banco", oldValue, banco);
	}
	
	public BigDecimal getImporte() {
		return importe;
	}
	public void setImporte(BigDecimal importe) {
		Object oldValue=this.importe;
		this.importe = importe;
		firePropertyChange("importe", oldValue, importe);
	}
	
	public int getNumero() {
		return numero;
	}
	public void setNumero(int numero) {
		int oldValue=this.numero;
		this.numero = numero;
		firePropertyChange("numero", oldValue, numero);
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((banco == null) ? 0 : banco.hashCode());
		result = PRIME * result + numero;
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
		final DepositoUnitario other = (DepositoUnitario) obj;
		if (banco == null) {
			if (other.banco != null)
				return false;
		} else if (!banco.equals(other.banco))
			return false;
		if (numero != other.numero)
			return false;
		return true;
	}
	
	
	public String toString(){		
		return ToStringBuilder
		.reflectionToString(this,ToStringStyle.MULTI_LINE_STYLE);
	}
	
	@AssertTrue (message="El importe debe ser mayor a $0.00")
	public boolean validarImporte(){		
		return getImporte().doubleValue()>0;
	}

	public int getGrupo() {
		return grupo;
	}

	public void setGrupo(int grupo) {
		this.grupo = grupo;
	}
	
	
		
}