package com.luxsoft.sw3.ventas;

import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.embarques.ServicioDeTransporte;
import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Direccion;

/**
 * Entidad para registrar la direccion a la q se debe entregar un pedido
 * 
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_PEDIDOS_ENTREGAS")
public class InstruccionDeEntrega extends BaseBean{
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "INSTRUCCION_ID")
	private Long id;
	
	//@OneToOne(optional=false,mappedBy="instruccionDeEntrega")
	@Transient
	private Pedido pedido;
	
	@Column(name="CALLE",nullable=false)
	@Length(max=255)
	@NotNull @NotEmpty
	private String calle;
	
	@Column(name="NUMERO",length=10,nullable=true)
	@Length(max=10,message="Longitud Máxima 10")
	private String numero="";	
	
	@Column(name="INTERIOR",length=10,nullable=true)
	@Length(max=10,message="Longitud Máxima 10")
	private String interior;	
	
	@Column(name="COLONIA",nullable=false)
	@Length(max=255)
	@NotNull @NotEmpty
	private String colonia;
	
	@Column(name="CP",length=6,nullable=true)
	@Length(max=6)
	@NotNull @NotEmpty
	private String cp;
	
	@Column(name="POBLACION",nullable=false)
	@Length(max=255)
	@NotNull @NotEmpty
	private String municipio;	
			
	@Column(name="ESTADO",length=150,nullable=true)
	@Length(max=150)
	private String estado="";
	
	
	@Column(name="COMENTARIO")
	@Length(max=250)
	private String comentario;
	
	@Column(name="COMENTARIO2")
	@Length(max=250)
	private String comentario2;
	
	@Column(name="CONDICIONES")
	@Length(max=250)
	private String condiciones;
	
	@Column(name = "FECHA_ENTREGA", nullable = true)
	private Date fechaEntrega = new Date();
	
	
	@ManyToOne(optional = true)
	@JoinColumn(name = "TRANSPORTE_ID", nullable = true)
	private ServicioDeTransporte transporte;
	
	@JoinColumn(name="OCURRE",columnDefinition="BIT(1) default false")
	private boolean ocurre=false;
	
	@JoinColumn(name="ASEGURADO",columnDefinition="BIT(1) default false")
	private boolean asegurado=false;
	
	@JoinColumn(name="RECOLECCION",columnDefinition="BIT(1) default false")
	private boolean recoleccion=false;

	public Pedido getPedido() {
		return pedido;
	}

	public void setPedido(Pedido pedido) {
		Object old=this.pedido;
		this.pedido = pedido;
		firePropertyChange("pedido", old, pedido);
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		Object old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
	}

	public String getComentario2() {
		return comentario2;
	}

	public void setComentario2(String comentario2) {
		Object old=this.comentario2;
		this.comentario2 = comentario2;
		firePropertyChange("comentario2", old, comentario2);
	}

	public Long getId() {
		return id;
	}

	public String getCalle() {
		return calle;
	}

	public void setCalle(String calle) {
		Object old=this.calle;
		this.calle = calle;
		firePropertyChange("calle", old, calle);
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		Object old=this.numero;
		this.numero = numero;
		firePropertyChange("numero", old, numero);
	}

	public String getInterior() {
		return interior;
	}

	public void setInterior(String interior) {
		Object old=this.interior;
		this.interior = interior;
		firePropertyChange("interior", old, interior);
	}

	public String getColonia() {
		return colonia;
	}

	public void setColonia(String colonia) {
		Object old=this.colonia;
		this.colonia = colonia;
		firePropertyChange("colonia", old, colonia);
	}

	public String getCp() {
		return cp;
	}

	public void setCp(String cp) {
		Object old=this.cp;
		this.cp = cp;
		firePropertyChange("cp", old, cp);
	}

	public String getMunicipio() {
		return municipio;
	}

	public void setMunicipio(String municipio) {
		Object old=this.municipio;
		this.municipio = municipio;
		firePropertyChange("municipio", old, municipio);
	}

	
	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		Object old=this.estado;
		this.estado = estado;
		firePropertyChange("estado", old, estado);
	}
	
	

	public ServicioDeTransporte getTransporte() {
		return transporte;
	}

	public void setTransporte(ServicioDeTransporte transporte) {
		Object old=this.transporte;
		this.transporte = transporte;
		firePropertyChange("transporte", old, transporte);
	}

	public boolean isOcurre() {
		return ocurre;
	}

	public void setOcurre(boolean ocurre) {
		boolean old=this.ocurre;
		this.ocurre = ocurre;
		firePropertyChange("ocurre", old, ocurre);
	}

	public boolean isAsegurado() {
		return asegurado;
	}

	public void setAsegurado(boolean asegurado) {
		boolean old=this.asegurado;
		this.asegurado = asegurado;
		firePropertyChange("asegurado", old, asegurado);
	}

	public boolean isRecoleccion() {
		return recoleccion;
	}

	public void setRecoleccion(boolean recoleccion) {
		boolean old=this.recoleccion;
		this.recoleccion = recoleccion;
		firePropertyChange("recoleccion", old, recoleccion);
	}
	
	

	public String getCondiciones() {
		return condiciones;
	}

	public void setCondiciones(String condiciones) {
		Object old=this.condiciones;
		this.condiciones = condiciones;
		firePropertyChange("condiciones", old, condiciones);
	}
	
	

	public Date getFechaEntrega() {
		return fechaEntrega;
	}

	public void setFechaEntrega(Date fechaEntrega) {
		Object old=this.fechaEntrega;
		this.fechaEntrega = fechaEntrega;
		firePropertyChange("fechaEntrega", old, fechaEntrega);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((calle == null) ? 0 : calle.hashCode());		
		result = prime * result + ((colonia == null) ? 0 : colonia.hashCode());
		result = prime * result + ((cp == null) ? 0 : cp.hashCode());
		result = prime * result + ((estado == null) ? 0 : estado.hashCode());
		result = prime * result
				+ ((interior == null) ? 0 : interior.hashCode());
		result = prime * result
				+ ((municipio == null) ? 0 : municipio.hashCode());
		result = prime * result + ((numero == null) ? 0 : numero.hashCode());
		result = prime * result + ((pedido == null) ? 0 : pedido.hashCode());
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
		InstruccionDeEntrega other = (InstruccionDeEntrega) obj;
		if (calle == null) {
			if (other.calle != null)
				return false;
		} else if (!calle.equals(other.calle))
			return false;		
		if (colonia == null) {
			if (other.colonia != null)
				return false;
		} else if (!colonia.equals(other.colonia))
			return false;
		if (cp == null) {
			if (other.cp != null)
				return false;
		} else if (!cp.equals(other.cp))
			return false;
		if (estado == null) {
			if (other.estado != null)
				return false;
		} else if (!estado.equals(other.estado))
			return false;
		if (interior == null) {
			if (other.interior != null)
				return false;
		} else if (!interior.equals(other.interior))
			return false;
		if (municipio == null) {
			if (other.municipio != null)
				return false;
		} else if (!municipio.equals(other.municipio))
			return false;
		if (numero == null) {
			if (other.numero != null)
				return false;
		} else if (!numero.equals(other.numero))
			return false;
		if (pedido == null) {
			if (other.pedido != null)
				return false;
		} else if (!pedido.equals(other.pedido))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String pattern="{0} #{1} {2}" +
				"\n{3}" +
				"\n{4} " +
				"\nC.P.:{5}" +
				"\n{6}";
		return MessageFormat.format(pattern
        		,getCalle() 
        		,StringUtils.trimToEmpty(getNumero()) //1
        		,StringUtils.trimToEmpty(getInterior())
        		,getColonia() //3
        		,getMunicipio() //4
        		,getCp() //5        		
        		,getEstado() //6
        		);
	}
	
	public String oneLineString() {
		String pattern="{0} #{1} {2}" +
				", {3}" +
				", {4}" +				
				",Entidad: {5}" +
				", C.P.:{6}" +
				", {7}";
		return MessageFormat.format(pattern
        		,StringUtils.trimToEmpty(getCalle()) 
        		,StringUtils.trimToEmpty(getNumero())
        		,StringUtils.trimToEmpty(getInterior()) 
        		,StringUtils.trimToEmpty(getColonia()) 
        		,StringUtils.trimToEmpty(getMunicipio()) 
        		,StringUtils.trimToEmpty(getCp())         		
        		,StringUtils.trimToEmpty(getEstado()) 
        		,StringUtils.trimToEmpty(getComentario())
        		);
	}
	
	
	public void setDireccion(Direccion direccion){
		resolve(direccion);
	}
	
	public void resolve(Direccion direccion){
		if(direccion!=null){
			setCalle(direccion.getCalle());
			setNumero(direccion.getNumero());
			setInterior(direccion.getNumeroInterior());
			setColonia(direccion.getColonia());
			setEstado(direccion.getEstado());
			setCp(direccion.getCp());
			setMunicipio(direccion.getMunicipio());
			
		}else{
			setCalle(null);
			setNumero(null);
			setInterior(null);
			setColonia(null);
			setEstado(null);
			setCp(null);
			setMunicipio(null);
			
		}
		
	}
	
	public Direccion transform(){
		Direccion direccion=new Direccion();
		direccion.setCalle(calle);		
		direccion.setColonia(colonia);
		direccion.setCp(cp);
		direccion.setEstado(estado);
		direccion.setMunicipio(municipio);
		direccion.setNumero(numero);
		direccion.setNumeroInterior(interior);
		return direccion;
	}
	
	
	public static class StringFormat extends Format{

		@Override
		public StringBuffer format(Object obj, StringBuffer toAppendTo,
				FieldPosition pos) {
			InstruccionDeEntrega i=(InstruccionDeEntrega)obj;
			if(i!=null)
				toAppendTo.append(i.oneLineString());
			return toAppendTo;
		}

		@Override
		public Object parseObject(String source, ParsePosition pos) {
			//Not supported
			return null;
		}
		
	}
}
