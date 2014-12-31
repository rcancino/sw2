package com.luxsoft.siipap.inventarios.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Range;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.sw3.maquila.model.Almacen;
import com.luxsoft.sw3.replica.Replicable;

/**
 * 
 * @author Ruben Cancino
 * 
 * NOTAS
 * 	Para agregar la columna de apartados ALTER TABLE `sw3`.`sx_existencias` ADD COLUMN `APARTADOS` DOUBLE NOT NULL DEFAULT 0 AFTER `PRODUCTO_ID`;
 *
 */
@Entity
@Table(name="SX_EXISTENCIAS_MAQ"
	,uniqueConstraints=@UniqueConstraint(columnNames={"ALMACEN_ID","PRODUCTO_ID","YEAR","MES"})
	)
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class ExistenciaMaq implements Replicable ,Serializable{
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="INVENTARIO_MAQ_ID")
	private String id;
    
    @Version
    private int version;    
    
    @ManyToOne (optional=false)
    @JoinColumn (name="ALMACEN_ID",updatable=false)
    @NotNull
    private Almacen almacen;
    
    @Column (name="FECHA",nullable=false)
    @NotNull
    private Date fecha;
    
    @ManyToOne (optional=false)
    @JoinColumn (name="PRODUCTO_ID", nullable=false,updatable=false) 
    @NotNull
    private Producto producto;
    
    @Column(name="YEAR",nullable=false)
    @NotNull
	private int year;
	
	@Column(name="MES",nullable=false)
	@NotNull 
	@Range(min=1,max=12)
	private int mes;
    
    @Column(name="CLAVE",nullable=false)
    @NotNull  @Length(max=10)
    private String clave;
    
    @Column(name="DESCRIPCION",nullable=false)
    @NotNull  @Length(max=250)
    private String descripcion;
    
    @Column(name="CANTIDAD",nullable=false)
    @AccessType(value="field")
    private double cantidad=0;
    
   
    
    @Column(name="UNIDAD",length=3,nullable=false)
	@Length(max=3,min=2)
	private String unidad;
    
    @Column(name="NACIONAL",nullable=false)
    @NotNull
    private boolean nacional=true;
    
    @Column(name="FACTORU",nullable=false)
	private double factor;
    
    @Column(name="KILOS",scale=3,nullable=true)
    private double kilos=0;
    
    @Column(name="COSTO",scale=6,precision=14,nullable=false)
    private BigDecimal costo=BigDecimal.ZERO;
    
    @Column(name="COSTOP",scale=6,precision=14,nullable=false)
    private BigDecimal costoPromedio=BigDecimal.ZERO;
    
    @Column(name="COSTOU",scale=6,precision=14,nullable=false)
    private BigDecimal costoUltimo=BigDecimal.ZERO;
    
    
        
    @Column(name="CREADO_USERID",updatable=false)
	private String createUser;
	
    @Column(name="MODIFICADO_USERID")
    private String updateUser;
	
	
	@Column(name="CREADO",updatable=false)
	@AccessType( value="field")
	private Date creado;
	
	
	@Column(name="MODIFICADO")
	private Date modificado;
	
	//@Column(name="APARTADOS",nullable=false)
	@Transient
	private double apartados=0;
	
	@Column(name="TX_IMPORTADO",nullable=true)
	private Date importado;
	
	@Column(name="TX_REPLICADO",nullable=true)
	private Date replicado;
	
    
    public ExistenciaMaq() {
    }

    public String getId() {
        return id;
    }
    

    public int getVersion() {
        return version;
    }
    public void setVersion(int version) {
        this.version = version;
    }
    
    
	public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }    
    
    public Producto getProducto() {
        return producto;
    } 

	public void setProducto(Producto producto) {
        this.producto = producto;
        //Copiamos algunas propiedades del producto 
        this.unidad=producto.getUnidad().getUnidad();
        this.clave=producto.getClave();
        this.descripcion=producto.getDescripcion();        
        this.factor=producto.getUnidad().getFactor();
        this.nacional=producto.isNacional();
        this.kilos=producto.getKilos();
    }
	
	

    public boolean isNacional() {
		return nacional;
	}

	public void setNacional(boolean nacional) {
		this.nacional = nacional;
	}

	public String getClave() {
		return clave;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public Almacen getAlmacen() {
        return almacen;
    }
	
    public void setAlmacen(Almacen almacen) {
        this.almacen = almacen;
    }
   
    public double getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(double cantidad) {		
		this.cantidad = cantidad;		
	}
    
    

	public double getCantidadEnUnidad(){
    	return getCantidad()/getFactor();
    }
    
    public String getUnidad() {
		return unidad;
	}
	
	public double getFactor() {
		return factor;
	}
    
	
	
    public void setKilos(double kilos) {
		this.kilos = kilos;
	}

	public double getKilos() {
		return kilos;
	}

	public BigDecimal getCosto() {
		return costo;
	}

	public void setCosto(BigDecimal costo) {
		this.costo = costo;
	}

    public BigDecimal getCostoPromedio() {
        return costoPromedio;
    }

    public void setCostoPromedio(BigDecimal costoPromedio) {
        this.costoPromedio = costoPromedio;
    }

    public BigDecimal getCostoUltimo() {
        return costoUltimo;
    }

    public void setCostoUltimo(BigDecimal costoUltimo) {
        this.costoUltimo = costoUltimo;
    } 

    public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public Date getCreado() {
		return creado;
	}

	public Date getModificado() {
		return modificado;
	}

	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}

	public int getMes() {
		return mes;
	}

	public void setMes(int mes) {
		this.mes = mes;
	}
	
	/**
	 * Costo del inventario a valor promedio
	 * 
	 * @return
	 */
    public BigDecimal getCostoAPromedio(){
    	double cant=getCantidad()/factor;
    	CantidadMonetaria costo=CantidadMonetaria.pesos(costoPromedio);
    	return costo.multiply(cant).amount();
    }
    
    /**
	 * Costo del inventario a ultimo costo
	 * 
	 * @return
	 */
    public BigDecimal getCostoAUltimo(){
    	double cant=getCantidad()/factor;
    	CantidadMonetaria costo=CantidadMonetaria.pesos(getCostoUltimo());
    	return costo.multiply(cant).amount();
    }
    
   
    public double getApartados() {
		return apartados;
	}

	public void setApartados(double apartados) {
		this.apartados = apartados;
	}

	/** equals,hashCode,toString ****/

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExistenciaMaq other = (ExistenciaMaq) obj;
        return new EqualsBuilder()
        .append(almacen,other.getAlmacen())       
        .append(producto,other.getProducto())
        .append(year, other.getYear())
        .append(mes,other.getMes())
        .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17,35)        
        .append(almacen)
        .append(producto)
        .append(year)
        .append(mes)
        .toHashCode();
    }

    @Override
    public String toString() {
        return  new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
        .append(clave)
        .append(almacen)
        .append(year)
        .append(mes)
        .append(cantidad)
        .toString();
    }
	
    
    public double getDisponible(){
    	return getCantidad();
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



	public void setModificado(Date modificado) {
		this.modificado = modificado;
	}
	

	@Transient
	private double pendientes=0;
	
	public void setPendientes(double pendientes) {
		this.pendientes = pendientes;
	}
	public double getPendientes() {
		return pendientes;
	}
	
	public void setClave(String clave) {
		this.clave = clave;
	}
	
	
}
