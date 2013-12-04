package com.luxsoft.siipap.ventas.model;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;

import com.luxsoft.siipap.cxc.model.AutorizacionDeAbono;
import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.sw3.replica.Replicable;

/**
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_DEVOLUCIONES")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class Devolucion extends BaseBean implements Replicable{
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="DEVO_ID")
	private String id;
	
	@Version
	private int version;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "VENTA_ID", nullable = false)
	private Venta venta;
	
	@Column(name = "FECHA", nullable = false)
	@Type(type = "date")
	private Date fecha = new Date();
	
	@Column(name="NUMERO")
	private Long numero;
	
	@Column(name = "IMPORTE", nullable = false)
	private BigDecimal importe = BigDecimal.ZERO;
	
	@Column(name = "IMPORTE_CORTES", nullable = true)
	private BigDecimal importeCortes = BigDecimal.ZERO;
	
	@Column(name = "IMPUESTO", nullable = false)
	private BigDecimal impuesto = BigDecimal.ZERO;
	
	@Column(name = "TOTAL", nullable = false)
	private BigDecimal total = BigDecimal.ZERO;
	
	@Length (max=50,message="El tamaño máximo del comentario es de 50 caracteres")
	private String comentario;
	
	/**
     * Id para ventas importadas del sistema anterior. Nulo para ventas
     * generadas en el nuevo sistema
     * 
     */
    @Column(name="SIIPAPWIN_DEVO_ID",nullable=true)
    private Long siipapWinId;
    
    @OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch=FetchType.EAGER,mappedBy="devolucion")
	@Cascade(value={org.hibernate.annotations.CascadeType.DELETE_ORPHAN
			,org.hibernate.annotations.CascadeType.REPLICATE}
	)
    private Set<DevolucionDeVenta> partidas=new HashSet<DevolucionDeVenta>();
    
    
    @ManyToOne(optional = true,cascade=CascadeType.ALL)
	@JoinColumn(name = "AUT_ID", nullable = true)
	private AutorizacionDeAbono autorizacion;
    
    @Embedded
	private UserLog log=new UserLog();

	public String getId() {
		return id;
	}

	public int getVersion() {
		return version;
	}

	

	public Venta getVenta() {
		return venta;
	}

	public void setVenta(Venta venta) {
		Object old=this.venta;
		this.venta = venta;
		firePropertyChange("venta", old, venta);
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Long getNumero() {
		return numero;
	}

	public void setNumero(Long numero) {
		this.numero = numero;
	}

	public BigDecimal getImporte() {
		return importe;
	}

	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}

	public BigDecimal getImpuesto() {
		return impuesto;
	}

	public void setImpuesto(BigDecimal impuesto) {
		this.impuesto = impuesto;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}
	
	public Long getSiipapWinId() {
		return siipapWinId;
	}

	public void setSiipapWinId(Long siipapWinId) {
		this.siipapWinId = siipapWinId;
	}
	
	public void agregarPartida(DevolucionDeVenta det){
		det.setRenglon(partidas.size()+1);
		partidas.add(det);
		det.setDevolucion(this);
		det.setSucursal(getVenta().getSucursal());
		
		
	}
	public void eliminarPartida(DevolucionDeVenta det){
		partidas.remove(det);
		det.setDevolucion(null);
	}
	public void limpiarPartidas(){
		partidas.clear();
	}
	public Set<DevolucionDeVenta> getPartidas(){
		return partidas;
	}

	public String toString(){
		String pattern="Dev: {0} Cliente: {1} Sucursal: {2} Venta:{3}";
		return MessageFormat.format(pattern, numero,venta.getNombre(),venta.getSucursal().getNombre(),venta.getDocumento());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 0;
		result = prime * result + ((fecha == null) ? 0 : fecha.hashCode());
		result = prime * result + ((numero == null) ? 0 : numero.hashCode());
		result = prime * result + ((venta == null) ? 0 : venta.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null) return false;
		if (this == obj) return true;		
		if (getClass() != obj.getClass())
			return false;
		Devolucion other = (Devolucion) obj;
		if (fecha == null) {
			if (other.fecha != null)
				return false;
		} else if (!fecha.equals(other.fecha))
			return false;
		if (numero == null) {
			if (other.numero != null)
				return false;
		} else if (!numero.equals(other.numero))
			return false;
		if (venta == null) {
			if (other.venta != null)
				return false;
		} else if (!venta.equals(other.venta))
			return false;
		return true;
	}

	public UserLog getLog() {
		return log;
	}

	public void setLog(UserLog log) {
		this.log = log;
	}
	
	/**
	 * La autorizacion requerida para genera el abono correspondiente
	 * (Nota de Credito)
	 * 
	 * @return
	 */
	public AutorizacionDeAbono getAutorizacion() {
		return autorizacion;
	}

	public void setAutorizacion(AutorizacionDeAbono autorizacion) {
		this.autorizacion = autorizacion;
	}

	public boolean isAutorizada(){
		return autorizacion!=null;
	}

	public BigDecimal getImporteCortes() {
		return importeCortes;
	}

	public void setImporteCortes(BigDecimal importeCortes) {
		this.importeCortes = importeCortes;
	}
	
	public boolean isTotal(){
		double cant=0;
		for(DevolucionDeVenta d:partidas){
			cant+=d.getCantidad();
		}
		double cantVenta=0;
		for(VentaDet det:getVenta().getPartidas()){
			cantVenta+=det.getCantidad();
		}
		long res= Math.round(cant)+Math.round(cantVenta);
		return res==0l;
	}

	@Column(name="TX_IMPORTADO",nullable=true)
	protected Date importado;
	
	@Column(name="TX_REPLICADO",nullable=true)
	protected Date replicado;

	public Date getImportado() {
		return importado;
	}

	public void setImportado(Date importado) {
		this.importado = importado;
	}

	public Date getReplicado() {
		return replicado;
	}

	public void setReplicado(Date replicado) {
		this.replicado = replicado;
	}
	
	public BigDecimal getImporteBruto(){
		CantidadMonetaria importe=CantidadMonetaria.pesos(0);
		for(DevolucionDeVenta devoDet:getPartidas()){
			Currency moneda=getVenta().getMoneda();
			importe=importe.add(new CantidadMonetaria(devoDet.getImporteBruto(),moneda));
		}
		
		return importe.amount();
		
	}
	
	public BigDecimal getImporteDescuento(){
		
		CantidadMonetaria importe=CantidadMonetaria.pesos(0);
		for(DevolucionDeVenta devoDet:getPartidas()){
			Currency moneda=getVenta().getMoneda();
			importe=importe.add(new CantidadMonetaria(devoDet.getImporteDescuento(),moneda));
		}
		
		return importe.amount();
		
	}
	
}
