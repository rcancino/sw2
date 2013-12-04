package com.luxsoft.sw3.cxc.forms;

import java.math.BigDecimal;
import java.util.Date;

import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.cxc.model.CargoPorTesoreria;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.util.MonedasUtils;

/**
 * Transfer object para cargos por tesoreria
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CargoPorTesoreriaModel {
	
	@NotNull(message="Seleccione el cliente")
	private Cliente cliente;
	
	private Date fecha=new Date();
	
	@Length(max=255, message="El tamaño maximo del comentario es de 255 caracteres")
	private String comentario;
	
	private BigDecimal total=BigDecimal.ZERO;
	
	private OrigenDeOperacion origen=OrigenDeOperacion.MOS;
	
	@NotNull(message="La sucursal es mandatoria")
	private Sucursal sucursal;
	
	private Long documento=new Long(0);
	
	public Cliente getCliente() {
		return cliente;
	}
	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	public String getComentario() {
		return comentario;
	}
	public void setComentario(String comentario) {
		this.comentario = comentario;
	}
	
	public BigDecimal getTotal() {
		return total;
	}
	public void setTotal(BigDecimal total) {
		this.total = total;
	}
	
	public OrigenDeOperacion getOrigen() {
		return origen;
	}
	public void setOrigen(OrigenDeOperacion origen) {
		this.origen = origen;
	}
	
	public Sucursal getSucursal() {
		return sucursal;
	}
	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}
	
	public Long getDocumento() {
		if(documento==null)
			documento=0l;
		return documento;
	}
	public void setDocumento(Long documento) {
		this.documento = documento;
	}
	@AssertTrue(message="El importe no es correcto")
	public boolean validarTotal(){
		return getTotal().doubleValue()>0;
	}
	
	@AssertTrue(message="La requisición es mandatoria")
	public boolean validarDocumento(){
		return getDocumento().doubleValue()>0;
	}
	
	public CargoPorTesoreria commit(){
		CargoPorTesoreria cargo=new CargoPorTesoreria();
		cargo.setCliente(getCliente());
		cargo.setFecha(getFecha());
		cargo.setTotal(getTotal());
		cargo.setImporte(MonedasUtils.calcularImporteDelTotal(cargo.getTotal()));
		cargo.setImpuesto(MonedasUtils.calcularImpuesto(cargo.getImporte()));
		cargo.setOrigen(getOrigen());
		cargo.setComentario(getComentario());
		cargo.setVencimiento(new Date());
		cargo.setNumeroFiscal(0);
		cargo.setDocumento(getDocumento());
		cargo.setSucursal(getSucursal());
		return cargo;
	}
	
	public static CargoPorTesoreriaModel getInstanceo(){
		return (CargoPorTesoreriaModel)Bean.proxy(CargoPorTesoreriaModel.class);
	}

}
