package com.luxsoft.siipap.ventas.model2;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.time.DurationFormatUtils;

import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.util.SQLUtils;

public class VentaContraEntrega {
	
	private String id;
	private Long documento;
	private Date fecha_ped;
	private Date fecha;
	private Date facturado;
	private String nombre;
	private BigDecimal total;
	private BigDecimal saldo;
	private String fpago;
	private String facturista;
	private Long pedido;
	private boolean cancelado;
	private Long embarque;
	private Long chofer;
	private Date salida;
	private Date regreso;
	private int atraso;
	private String origen;
	private String instruccion;
	private boolean contraEntrega;
	private BigDecimal entregado;
	private Date asignacion;
	private String sucursal;
	private Date ultimoPago;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean isCancelado() {
		return cancelado;
	}
	public void setCancelado(boolean cancelado) {
		this.cancelado = cancelado;
	}
	
	public long getDocumento() {
		return documento;
	}
	public void setDocumento(long documento) {
		this.documento = documento;
	}
	
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public BigDecimal getTotal() {
		return total;
	}
	public void setTotal(BigDecimal total) {
		this.total = total;
	}
	public BigDecimal getSaldo() {
		return saldo;
	}
	public void setSaldo(BigDecimal saldo) {
		this.saldo = saldo;
	}
	public String getFpago() {
		return fpago;
	}
	public void setFpago(String fpago) {
		this.fpago = fpago;
	}
	public String getFacturista() {
		return facturista;
	}
	public void setFacturista(String facturista) {
		this.facturista = facturista;
	}
	public long getPedido() {
		return pedido;
	}
	public void setPedido(long pedido) {
		this.pedido = pedido;
	}
	
	public Long getChofer() {
		return chofer;
	}
	public void setChofer(Long chofer) {
		this.chofer = chofer;
	}
	public Date getSalida() {
		return salida;
	}
	public void setSalida(Date salida) {
		this.salida = salida;
	}
	public Date getRegreso() {
		return regreso;
	}
	public void setRegreso(Date regreso) {
		this.regreso = regreso;
	}
	public int getAtraso() {
		return atraso;
	}
	public void setAtraso(int atraso) {
		this.atraso = atraso;
	}
	public void setDocumento(Long documento) {
		this.documento = documento;
	}
	public void setPedido(Long pedido) {
		this.pedido = pedido;
	}
	public String getOrigen() {
		return origen;
	}
	public void setOrigen(String origen) {
		this.origen = origen;
	}
	public Long getEmbarque() {
		return embarque;
	}
	public void setEmbarque(Long embarque) {
		this.embarque = embarque;
	}	
	
	public String getInstruccion() {
		return instruccion;
	}
	public void setInstruccion(String instruccion) {
		this.instruccion = instruccion;
	}
	public Date getFacturado() {
		return facturado;
	}
	public void setFacturado(Date facturado) {
		this.facturado = facturado;
	}
	
	public boolean isContraEntrega() {
		return contraEntrega;
	}
	public void setContraEntrega(boolean contraEntrega) {
		this.contraEntrega = contraEntrega;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		VentaContraEntrega other = (VentaContraEntrega) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	public static void main(String[] args) {
		String sql="SELECT V.CARGO_ID as id,v.docto as documento,v.fecha,v.nombre,v.total ,v.total-IFNULL((SELECT sum(a.importe) FROM sx_cxc_aplicaciones a where a.cargo_id=v.cargo_id),0) as saldo,v.FPAGO as fpago,V.MODIFICADO_USERID AS facturista,V.PEDIDO_FOLIO as pedido,(CASE WHEN (SELECT X.CARGO_ID FROM sx_cxc_cargos_cancelados X WHERE V.CARGO_ID=X.CARGO_ID) IS null THEN false ELSE true END) AS cancelado" +
				",q.transporte_id as chofer,q.documento as embarque,q.salida,q.regreso,CASE WHEN ROUND(TO_DAYS(CURRENT_DATE)-TO_DAYS(V.FECHA),0)<1 THEN 0 ELSE ROUND(TO_DAYS(CURRENT_DATE)-TO_DAYS(V.FECHA),0) END AS atraso,v.origen FROM sx_ventas v left join sx_entregas e on(v.CARGO_ID=e.VENTA_ID) left join sx_embarques q on(q.EMBARQUE_ID=e.EMBARQUE_ID) WHERE V.fecha >='2010/07/01' and v.origen='CAM' AND V.CE IS true AND v.sucursal_id=3 AND v.total-IFNULL((SELECT sum(a.importe) FROM sx_cxc_aplicaciones a where a.cargo_id=v.cargo_id),0)>0 ";
		SQLUtils.printBeanClasFromSQL(sql);
	}
	
	public BigDecimal getEntregado() {
		return entregado;
	}
	public void setEntregado(BigDecimal entregado) {
		this.entregado = entregado;
	}
	
	public BigDecimal getPendienteDeEntrega(){
		BigDecimal entregadoConIva=MonedasUtils.calcularTotal(getEntregado());
		BigDecimal res=getTotal().subtract(entregadoConIva);
		return res;
	}
	public Date getAsignacion() {
		return asignacion;
	}
	public void setAsignacion(Date asignacion) {
		this.asignacion = asignacion;
	}
	
	
	
	public Date getFecha_ped() {
		return fecha_ped;
	}
	public void setFecha_ped(Date fecha_ped) {
		this.fecha_ped = fecha_ped;
	}
	public String getSucursal() {
		return sucursal;
	}
	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}
	
	
	public Date getUltimoPago() {
		return ultimoPago;
	}
	public void setUltimoPago(Date ultimoPago) {
		this.ultimoPago = ultimoPago;
	}
	public boolean getPermitirPago(){
		if(asignacion!=null){
			final Date dia=new Date();
			int dias=DateUtil.getDaysDiff(dia, asignacion);
			return dias<=3;
		}
		return false;
	}
	
	public int getRetrasoAsignacion(){
		if(asignacion!=null){
			final Date dia=new Date();
			int dias=DateUtil.getDaysDiff(dia, asignacion);
			return dias;
		}
		return 0;
	}
	
	public long getRetrasoEnAsignacionHoras(){
		long ini=getFecha_ped()!=null?getFecha_ped().getTime():getFacturado().getTime();
		long fin=System.currentTimeMillis();
		long res=fin-ini;
		return res/1000/60/60;
		
	}
	
	public String getRetrasoEnAsignacion(){
		Date now=new Date();
		if(getFecha_ped()==null)
			return "No disponible";
		String res=DurationFormatUtils.formatDuration(now.getTime()-fecha_ped.getTime(),"d' dias 'H' hrs 'm' min ");
		return res;
	}
	
	public Date getFecha() {
		if(fecha==null)
			fecha=fecha_ped;
		return fecha;
	}
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	

	
	
	

}
