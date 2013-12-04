package com.luxsoft.siipap.inventarios.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.AssertFalse;
import org.hibernate.validator.NotNull;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.ConfiguracionKit;
import com.luxsoft.sw3.replica.Replicable;

/**
 * Organiza un movimiento de inventario para un producto
 * Kit, es decir n salidas del inventario generan una y solo una
 * entrada del producto kit
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_KITS")
public class Kit extends BaseBean {
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="ID")
	private Long id;
	
	@Version
	private int version;
	
	@Column(name="FECHA",nullable=false)
	@NotNull
	private Date fecha=new Date();
	
	@ManyToOne (optional=false)
    @JoinColumn (name="SUCURSAL_ID", nullable=false,updatable=false)
    @NotNull
	private Sucursal sucursal;
	
	@Column(name="COMENTARIO",length=150)
	private String comentario;
	
	@ManyToOne (optional=false)
    @JoinColumn (name="CONFIG_ID", nullable=false,updatable=false)
    @NotNull
	private ConfiguracionKit config;
	
	@OneToOne(optional=false,cascade={CascadeType.MERGE,CascadeType.PERSIST})
	@JoinColumn(name="ENTRADA_ID",unique=true,nullable=false,updatable=false)
	private KitDet entrada;
	
	@Transient
	private double cantidad=0;
	
	@OneToMany(cascade=CascadeType.ALL
			,fetch=FetchType.EAGER)
	private Set<KitDet> salidas=new HashSet<KitDet>();
	
	private BigDecimal costoUnitario;
	

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	public KitDet getEntrada() {
		return entrada;
	}

	public void setEntrada(KitDet entrada) {
		this.entrada = entrada;
	}

	public Set<KitDet> getSalidas() {
		return salidas;
	}	

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public double getCantidad() {
		return cantidad;
	}

	public void setCantidad(double cantidad) {
		this.cantidad = cantidad;
	}

	public ConfiguracionKit getConfig() {
		return config;
	}

	public void setConfig(ConfiguracionKit config) {
		this.config = config;
	}
	
	

	public BigDecimal getCostoUnitario() {
		return costoUnitario;
	}

	public void setCostoUnitario(BigDecimal costoUnitario) {
		this.costoUnitario = costoUnitario;
	}

	/**
	 * Actualizar el costo del producto kit
	 * 
	 */
	public BigDecimal actualizarCosto(){
		BigDecimal costo=BigDecimal.ZERO;
		for(KitDet s:salidas){			
			costo=costo.add(s.getCostoPromedio());
		}
		getEntrada().setCosto(costo);
		getEntrada().setCostoPromedio(costo);
		return costo;
	}
	
	public boolean agregarSalida(KitDet det){		
		return salidas.add(det);
	}
	
	
	
	/**
	 * Arma los movimientos en funcion de la configuracion establecida
	 * 
	 */
	public void procesar() {
		Assert.isNull(id,"Para prepara las salidas se requiere que el kit no exisa como entidad persistida es decir Id nulo");
		Assert.notNull(sucursal,"Debe asignar la sucursal.");
		salidas.clear();
		
		if(entrada==null){
			entrada=new KitDet();
		}		
		entrada.setProducto(config.getDestino());
		entrada.setCantidad(cantidad);
		entrada.setFecha(fecha);
		entrada.setSucursal(sucursal);
		
		int cantidad=(int)getEntrada().getCantidad();		
		// Armando las salidas
		for(ConfiguracionKit.Elemento e:config.getPartes()){
			KitDet det=new KitDet();
			det.setCantidad(e.getCantidad()*cantidad);
			det.setProducto(e.getProducto());
			salidas.add(det);
		}		
	}

	@Override
	public boolean equals(Object o) {
		if(o==null) return false;
		if(o==this) return true;
		if(getClass()!=o.getClass()) return false;
		Kit othre=(Kit)o;
		return new EqualsBuilder()
		.append(id, othre.getId())
		.append(fecha, othre.getFecha())
		.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)
		.append(id)
		.append(fecha)
		.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
		.append(id)
		.append(fecha)
		.append(comentario)
		.toString();
	}
	
	@AssertFalse(message="Kit invalido")
	public boolean isValid(){
		return salidas.isEmpty();
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
	
}
