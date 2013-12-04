package com.luxsoft.siipap.ventas.model;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.sw3.replica.Replicable;

/**
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_PREDEVOLUCIONES")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class PreDevolucion extends BaseBean implements Replicable{
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="PREDEVO_ID")
	private String id;
	
	@Version
	private int version;
	
	@ManyToOne (optional=false)
    @JoinColumn (name="SUCURSAL_ID",updatable=false)
    @NotNull
    private Sucursal sucursal;
	
	@ManyToOne(optional = false
			,fetch=FetchType.EAGER)			
	@JoinColumn(name = "CLIENTE_ID", nullable = false, updatable = false)
	@NotNull(message="El cliente es mandatorio")
	private Cliente cliente;
	
	@ManyToOne(optional = false,fetch=FetchType.EAGER)			
	@JoinColumn(name = "VENTA_ID", nullable = true)
	@NotNull(message="Se requiere la venta")
	private Venta venta;
	
	@OneToOne(optional=true,fetch=FetchType.EAGER)
    @JoinColumn(name="DEVO_ID", unique=true, nullable=true )
	private Devolucion devolucion;
	
	@Column(name="CHOFER", nullable=true)
	private String chofer;
	
	@Column(name = "FECHA", nullable = false)
	@Type(type = "date")
	private Date fecha = new Date();
	
	@Column(name="DOCUMENTO")
	private Long documento;
	
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
	
	@Column(name="COMISION_CHOFER",nullable=false)
	private double comisionChofer;
	
	@Column(name="COMISION_FECHA",nullable=true)
	private Date fechaComision;
	
	@Column(name="COMISION_COMENTARIO",nullable=true)
	private String comentarioComision;
	
	@Column(name="KILOS_RMD",nullable=true)
	private double kilosRmd=0;
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch= FetchType.LAZY			
				)
	@JoinColumn(name="PREDEVO_ID",nullable=false)
	@Cascade(value={org.hibernate.annotations.CascadeType.DELETE_ORPHAN
			,org.hibernate.annotations.CascadeType.REPLICATE})
	@IndexColumn(name="RENGLON",base=1)
    private List<PreDevolucionDet> partidas=new ArrayList<PreDevolucionDet>();
    
	@Column(name="TX_IMPORTADO",nullable=true)
	protected Date importado;
	
	@Column(name="TX_REPLICADO",nullable=true)
	protected Date replicado;
    
    @Embedded
	private UserLog log=new UserLog();

	public String getId() {
		return id;
	}

	public int getVersion() {
		return version;
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}	
	
	

	public String getChofer() {
		return chofer;
	}

	public void setChofer(String chofer) {
		this.chofer = chofer;
	}
	
	

	public Venta getVenta() {
		return venta;
	}

	public void setVenta(Venta venta) {
		Object old=this.venta;
		this.venta = venta;
		firePropertyChange("venta", old,venta);	
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		Object old=this.cliente;
		this.cliente = cliente;
		firePropertyChange("cliente", old, cliente);
	}

	public Devolucion getDevolucion() {
		return devolucion;
	}

	public void setDevolucion(Devolucion devolucion) {
		this.devolucion = devolucion;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Long getDocumento() {
		return documento;
	}

	public void setDocumento(Long documento) {
		this.documento = documento;
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
	
	public void agregarPartida(PreDevolucionDet det){
		//det.setRenglon(partidas.size()+1);
		partidas.add(det);
		det.setPreDevolucion(this);
	}
	
	public void eliminarPartida(DevolucionDeVenta det){
		partidas.remove(det);
		det.setDevolucion(null);
	}
	public void limpiarPartidas(){
		partidas.clear();
	}
	public List<PreDevolucionDet> getPartidas(){
		return partidas;
	}

	public String toString(){
		String pattern="Pre Dev: {0} Cliente: {1} Sucursal: {2} Docto:{3}";
		return MessageFormat.format(pattern, documento,getCliente().getNombre()
				,getSucursal().getNombre(),getDocumento());
	}	

	public UserLog getLog() {
		return log;
	}

	public void setLog(UserLog log) {
		this.log = log;
	}

	public BigDecimal getImporteCortes() {
		return importeCortes;
	}

	public void setImporteCortes(BigDecimal importeCortes) {
		this.importeCortes = importeCortes;
	}	

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
	
	public double getComisionChofer() {
		return comisionChofer;
	}

	public void setComisionChofer(double comisionChofer) {
		this.comisionChofer = comisionChofer;
	}

	public Date getFechaComision() {
		return fechaComision;
	}

	public void setFechaComision(Date fechaComision) {
		this.fechaComision = fechaComision;
	}

	public String getComentarioComision() {
		return comentarioComision;
	}

	public void setComentarioComision(String comentarioComision) {
		this.comentarioComision = comentarioComision;
	}
	
	

	public double getKilosRmd() {
		return kilosRmd;
	}

	public void setKilosRmd(double kilosRmd) {
		this.kilosRmd = kilosRmd;
	}

	public Devolucion generarRMD(final Date fecha){
		Devolucion devo=new Devolucion();
		devo.setComentario(getComentario());
		devo.setFecha(fecha);
		devo.setVenta(getVenta());
		for(PreDevolucionDet det:getPartidas()){
			devo.agregarPartida(det.toDevoDet());
			//setDevolucion(devo);
		}
		return devo;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
		.append(getSucursal())
		.append(getDocumento())
		.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this==null) 
			return false;
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		PreDevolucion other = (PreDevolucion) obj;
		if (documento == null) {
			if (other.documento != null)
				return false;
		} else if (!documento.equals(other.documento))
			return false;
		if (sucursal == null) {
			if (other.sucursal != null)
				return false;
		} else if (!sucursal.equals(other.sucursal))
			return false;
		return true;
	}

	
	
}
