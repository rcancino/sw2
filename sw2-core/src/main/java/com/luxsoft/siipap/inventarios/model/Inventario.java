/*
 *  Copyright 2008 Ruben Cancino <rcancino@luxsoftnet.com>.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package com.luxsoft.siipap.inventarios.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Unidad;


/**
 *
 * @author Ruben Cancino <rcancino@luxsoftnet.com>
 */
@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public abstract class Inventario extends BaseBean {
    
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="INVENTARIO_ID")
	protected String id;
    
    @Version
    private int version;    
    
    @ManyToOne (optional=false)
    @JoinColumn (name="SUCURSAL_ID",updatable=false)
    @NotNull
    private Sucursal sucursal;
    
    @Column (name="FECHA",nullable=false)
    @NotNull
    private Date fecha=new Date();
    
    @ManyToOne (optional=false)
    @JoinColumn (name="PRODUCTO_ID", nullable=false,updatable=false) 
    @NotNull
    private Producto producto;   
    
    @Column(name="CLAVE",nullable=false,length=10)    
    private String clave;
    
    @Column(name="DESCRIPCION",nullable=false,length=250)    
    private String descripcion;
   
    @Column(name="NACIONAL",nullable=false)
    private Boolean nacional;
    
    @Column(name="CANTIDAD",nullable=false)
    @AccessType(value="field")
    private double cantidad=0;
    
    @Column(name="EXISTENCIA",nullable=false,columnDefinition="DOUBLE default 0")
	private double existencia=0;
    
    @ManyToOne (optional=false)
    @JoinColumn (name="UNIDAD_ID", nullable=false,updatable=false)    
    private Unidad unidad;
    
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
    
    @Column(name="COMENTARIO")
    @Length(max=250)
    private String comentario;
    
    @Column (name="ALMACEN_ID")
	private Long almacenId;
    
    /**
	 * Util por compatiblidad con SIIPAP
	 * Normalmente se refiere al campo ALMNUMER
	 * 
	 */
    @Column(name="DOCUMENTO")
	protected Long documento;
    
    @Column(name="RENGLON")
    private int renglon=1;
    
    @Column(name="CREADO_USERID")
	private String createUser;
	
    @Column(name="MODIFICADO_USERID")
    private String updateUser;
	
	//@Type (type="time")
	@Column(name="CREADO",updatable=false,insertable=true)
	@AccessType( value="field")
	private Date creado;
	
	@Type (type="time")
	@Column(name="MODIFICADO")
	private Date modificado;
    
    public Inventario() {
    }

    public String getId() {
        return id;
    }
    

    public void setId(String id) {
		this.id = id;
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
    @NotNull 
    public Producto getProducto() {
        return producto;
    }
    
    

    public double getKilos() {
		return kilos;
	}

	public void setKilos(double kilos) {
		this.kilos = kilos;
	}
	
	public double getKilosCalculados(){
		double millares= getCantidad()/getUnidad().getFactor();
		return millares*getProducto().getKilos();
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public void setProducto(Producto producto) {
    	Object oldvalue=this.producto;
        this.producto = producto;
        firePropertyChange("producto", oldvalue, producto);
        
        //Copiamos algunas propiedades del producto 
        this.unidad=producto.getUnidad();
        this.clave=producto.getClave();
        this.descripcion=producto.getDescripcion();
        this.nacional=producto.isNacional();
        this.factor=producto.getUnidad().getFactor();
        actualizarKilosDelMovimiento();
        
    }

    public String getClave() {
		return clave;
	}
    
    
    
	public void setClave(String clave) {
		this.clave = clave;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public Sucursal getSucursal() {
        return sucursal;
    }
	
    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    } 
    
    @NotNull 
    public double getCantidad() {
        return cantidad;
    }
    public void setCantidad(double cantidad) {
		double old=this.cantidad;
		this.cantidad = cantidad;
		firePropertyChange("cantidad", old, cantidad);
	}
    
    public double getCantidadEnUnidad(){
    	return getCantidad()/getFactor();
    }
    
    public Unidad getUnidad() {
		return unidad;
	}

	public void setUnidad(Unidad unidad) {
		this.unidad = unidad;
	}
	
	
	
	public double getFactor() {
		return factor;
	}

	public void setFactor(double factor) {
		this.factor = factor;
	}

    public Boolean getNacional() {
		return nacional;
	}

	public void setNacional(Boolean nacional) {
		this.nacional = nacional;
	}

	public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
    	Object old=this.comentario;
        this.comentario = comentario;
        firePropertyChange("comentario", old, comentario);
    }
    
    public BigDecimal getCosto() {
		return costo;
	}

	public void setCosto(BigDecimal costo) {
		Object old=this.costo;
		this.costo = costo;
		firePropertyChange("costo", old, costo);
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
    
    public abstract String getTipoDocto();
    

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
        final Inventario other = (Inventario) obj;
        return new EqualsBuilder()
        .append(getTipoDocto(),other.getTipoDocto())
        .append(sucursal,other.getSucursal())
        .append(documento,other.getDocumento())
        .append(fecha,other.getFecha())
        .append(producto,other.getProducto())
        .append(unidad,other.getUnidad())
        .append(cantidad,other.getCantidad())
        .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17,35)
        .append(getTipoDocto())
        .append(getSucursal())
        .append(getDocumento())
        .append(getFecha())
        .append(getProducto())
        .append(getUnidad())
        .append(getCantidad())
        .toHashCode();
    }

    @Override
    public String toString() {
        return  new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
        .append(getId())
        .append(getSucursal())
        .append(getFecha())
        .append(getProducto())
        .append(unidad)
        .append(getCantidad())
        .toString();
    }

    
    
	/**
	 * Solo util para los movimientos migrados de Oracle
	 * 
	 * @return
	 */
	public Long getAlmacenId() {
		return almacenId;
	}

	public void setAlmacenId(Long almacenId) {
		this.almacenId = almacenId;
	}

	/**
	 * Read-Only del origen nacional o no del producto
	 * al momento del movimiento. Esta propiedad es asignada
	 * al asignar el producto
	 * 
	 * @return
	 */
	public boolean isNacional() {
		if(nacional==null)
			return producto.isNacional();
		return nacional;
	}
	
	/** Propiedades por compatibilidad con siipap ***/
	
	

	public Long getDocumento() {
		return documento;
	}

	public void setDocumento(Long almnumer) {
		this.documento = almnumer;
	}

	public int getRenglon() {
		return renglon;
	}

	public void setRenglon(int renglon) {
		int old=this.renglon;
		this.renglon = renglon;
		firePropertyChange("renglon", old, renglon);
	}
    
	public int getYear(){
		return Periodo.obtenerYear(getFecha());
	}
	public int getMes(){
		return Periodo.obtenerMes(fecha)+1;
	}
	
	
	public void actualizarKilosDelMovimiento(){
		double cant=getCantidadEnUnidad()*getProducto().getKilos();
		setKilos(Math.abs(cant));
	}
	
	public BigDecimal getCostoMovimiento(){
		return getCosto().multiply(BigDecimal.valueOf(getCantidadEnUnidad()));
	}
	
	

	public double getExistencia() {
		return existencia;
	}

	public void setExistencia(double existencia) {
		this.existencia = existencia;
	}

	public void setCreado(Date creado) {
		this.creado = creado;
	}

	public void setModificado(Date modificado) {
		this.modificado = modificado;
	}
	
	

	
	
}
        
    