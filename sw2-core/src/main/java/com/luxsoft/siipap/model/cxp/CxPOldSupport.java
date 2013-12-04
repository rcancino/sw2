package com.luxsoft.siipap.model.cxp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.util.DateUtil;

/**
 * Entidad antigua para el manejo de la cuenta por pagar
 * Descuntinuada en la nueva version
 * 
 *  Se ocupa temporalmente para la generacion de polizas
 * 
 * @author Ruben Cancino
 *
 */
public class CxPOldSupport {
	
	private Long id;
	private String tipo;
	private Long proveedorId;
	private String clave;
	private String nombre;
	private String cuenta;
	private Date fecha;
	private String referencia;
	private Date fechaF;
	private BigDecimal total;
	private String comentario;
	
	public String getClave() {
		return clave;
	}
	public void setClave(String clave) {
		this.clave = clave;
	}
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	public Date getFechaF() {
		return fechaF;
	}
	public void setFechaF(Date fechaF) {
		this.fechaF = fechaF;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getNombre() {
		return StringUtils.substring(nombre,0,19);
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public Long getProveedorId() {
		return proveedorId;
	}
	public void setProveedorId(Long proveedorId) {
		this.proveedorId = proveedorId;
	}
	public String getReferencia() {
		return referencia;
	}
	public void setReferencia(String referencia) {
		this.referencia = referencia;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public BigDecimal getTotal() {
		return total;
	}
	public void setTotal(BigDecimal total) {
		this.total = total;
	}
	
	public String getCuenta() {
		if(cuenta==null)
			cuenta="000-0000-000";
		return cuenta;
	}
	public void setCuenta(String cuenta) {
		this.cuenta = cuenta;
	}
	
	
	
	public String getComentario() {
		return comentario;
	}
	public void setComentario(String comentario) {
		this.comentario = comentario;
	}
	
	public String getPagoRef(){
		try {
			int start=getComentario().indexOf(':');
			int end=getComentario().indexOf(' ', start);
			return getComentario().substring(start+1, end);
		} catch (Exception e) {
			return "ERR: "+getId();
		}
		
	}
	
	public String getFormaDePago(){
		if(getComentario().startsWith("C"))
			return "CHEQ";
		else if(getComentario().startsWith("T"))
			return "TRANS";
		else
			return "ERR FP:"+getId();
	}
	
	public String getBanco(){
		try {
			int start=getComentario().indexOf(':');
			int end=getComentario().indexOf(' ', start);
			return getComentario().substring(end,getComentario().length() ).trim();
		} catch (Exception e) {
			return "ERR: "+getId();
		}
	}
	
	public double getTotalAsDouble(){
		return getTotal().abs().doubleValue();
	}
	
	public CantidadMonetaria getImporte(){
		CantidadMonetaria total=CantidadMonetaria.pesos(getTotalAsDouble());
		return total.divide(1.15);
	}
	
	public double getIva(){		
		BigDecimal total=getTotal().abs();
		return total.divide(BigDecimal.valueOf(1.15)
				, RoundingMode.HALF_EVEN).multiply(BigDecimal.valueOf(.15)).setScale(2,RoundingMode.HALF_EVEN).doubleValue();
		
	}
	
	public String toString(){
		return ToStringBuilder.reflectionToString(this,ToStringStyle.SHORT_PREFIX_STYLE)+ "Pago:"+getPagoRef()+" FP:"+getFormaDePago()+" Banco:"+getBanco();
	}
	
	public static class CxPGrupo{
		
		private final List<CxPOldSupport> pagos;
		private BigDecimal total;
		
		public CxPGrupo(final List<CxPOldSupport> pagos){
			this.pagos=pagos;
			Assert.notEmpty(pagos,"No se puede generar un grupo de pago sin pagos");
		}

		public List<CxPOldSupport> getPagos() {
			return pagos;
		}
		
		public String getBanco(){
			return pagos.get(0).getBanco();
		}
		public String getFormaDePago(){
			return pagos.get(0).getFormaDePago();
		}
		public String getReferencia(){
			return pagos.get(0).getPagoRef();
		}
		 
		
		public BigDecimal getTotal(){
			if(total==null){
				total=BigDecimal.ZERO;
				for(CxPOldSupport c:pagos){
					total=total.add(c.getTotal().abs());
				}
			}
			return total;
		}
		
		
		public BigDecimal getIva(){
			CantidadMonetaria total=CantidadMonetaria.pesos(getTotal().doubleValue());
			CantidadMonetaria importe=total.divide(1.15);
			CantidadMonetaria impuesto=importe.multiply(.15);
			return impuesto.amount();
		}
		
		public BigDecimal getIetu(){
			final Date inicio=DateUtil.toDate("31/12/2007");
			CantidadMonetaria ietu=CantidadMonetaria.pesos(0);
			for(CxPOldSupport c:pagos){
				if(c.getFechaF().after(inicio)){
					ietu=ietu.add(c.getImporte());
				}
			}
			return ietu.amount();
		}
		
		
		public String toString(){
			return "Ref: "+getReferencia()+" Tot:"+getTotal()+" FP:"+getFormaDePago()+" Banco:"+getBanco();
		}
		
		public String getDescripcion(){
			if(getFormaDePago().startsWith("CH")){
				return "CH  "+getReferencia()+" "+pagos.get(0).getNombre();
			}else if(getFormaDePago().startsWith("T")){
				return "T   "+getReferencia()+" "+pagos.get(0).getNombre();
			}else
				return "ERROR ";
		}
		
	}

}
