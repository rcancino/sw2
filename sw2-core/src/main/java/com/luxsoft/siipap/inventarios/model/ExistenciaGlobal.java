package com.luxsoft.siipap.inventarios.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
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
import com.luxsoft.sw3.replica.Replicable;

/**
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_EXISTENCIAS_GLOBAL"
	,uniqueConstraints=@UniqueConstraint(columnNames={"CLAVE","YEAR","MES"})
	)
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class ExistenciaGlobal implements Replicable ,Serializable{
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	private String id;
    
    @Version
    private int version;
   
    
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
    
    private String linea;
    
    private String marca;
    
    private String clase;
    
    @Column(name="CANTIDAD",nullable=false)
    @AccessType(value="field")
    private double cantidad=0;
    
    @Column(name="RECORTE",nullable=false)
    private double recorte=0;
    
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
	
	
	@Column(name="TX_IMPORTADO",nullable=true)
	private Date importado;
	
	@Column(name="TX_REPLICADO",nullable=true)
	private Date replicado;
	
	@Column(name="TIPO",nullable=true)
	private String tipo="NORMAL";
    
    public ExistenciaGlobal() {
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
	
	
	
	public String getLinea() {
		return linea;
	}

	public void setLinea(String linea) {
		this.linea = linea;
	}

	public String getMarca() {
		return marca;
	}

	public void setMarca(String marca) {
		this.marca = marca;
	}

	public String getClase() {
		return clase;
	}

	public void setClase(String clase) {
		this.clase = clase;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public void setUnidad(String unidad) {
		this.unidad = unidad;
	}

	public void setFactor(double factor) {
		this.factor = factor;
	}

	public void setCreado(Date creado) {
		this.creado = creado;
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
        final ExistenciaGlobal other = (ExistenciaGlobal) obj;
        return new EqualsBuilder()
        .append(clave,other.getClave())
        .append(year, other.getYear())
        .append(mes,other.getMes())
        .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17,35)
        .append(clave)
        .append(year)
        .append(mes)
        .toHashCode();
    }

    @Override
    public String toString() {
        return  new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
        .append(clave)
        .append(year)
        .append(mes)
        .append(cantidad)
        .toString();
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
	public void setRecorte(double recorte) {
		this.recorte = recorte;
	}
	public double getRecorte() {
		return recorte;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	
	
	
}
