/*
 *  Copyright 2008 RUBEN.
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.luxsoft.siipap.maquila.model.MovimientoConFlete;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.ventas.model.DevolucionDeVenta;
import com.luxsoft.sw3.maquila.model.AnalisisDeFlete;

/**
 * Detalle del trasaldo de material
 * 
 * alter table SX_INVENTARIO_TRD add column COSTO_FLETE numeric(16,6)

alter table SX_INVENTARIO_TRD add column REMISION varchar(20)

alter table SX_INVENTARIO_TRD add column ANALISIS_FLETE_ID bigint

alter table SX_INVENTARIO_TRD add index FKBCF7A0D2CA79ECB2 (ANALISIS_FLETE_ID)
    ,add constraint FKBCF7A0D2CA79ECB2 foreign key (ANALISIS_FLETE_ID) 
    references SX_MAQ_ANALISIS_FLETE (ANALISIS_ID)
 * 
 * @author Ruben Cancino
 * 
 */
@Entity
@Table(name="SX_INVENTARIO_TRD"
	//,uniqueConstraints=@UniqueConstraint(columnNames={"TRASLADO_ID","TIPO","SOLDET_ID"})
	)
public class TrasladoDet extends Inventario implements MovimientoConFlete{
	
	
	@ManyToOne(optional = false)
    @JoinColumn(name = "TRASLADO_ID"
    	, nullable = false
    	, updatable = false
    	,insertable=false
    	)
	private Traslado traslado;
	
	@Column(name="TIPO",length=3,nullable=false)
	private String tipo;
	
	@Column(name="SOLICITADO",nullable=false)
	private double solicitado;
	
	/**
	 * El id del TPS cuando se trata de un TPE
	 * 
	 */
	@Column(name="TPS_ORIGEN")
    private String origen;	
	
	public TrasladoDet(){}	

	public Traslado getTraslado() {
		return traslado;
	}

	public void setTraslado(Traslado traslado) {
		this.traslado = traslado;
	}	

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	@Override
	public String getTipoDocto() {
		return getTipo();
	}	

	public String getOrigen() {
		return origen;
	}

	public void setOrigen(String origen) {
		this.origen = origen;
	}

	public double getSolicitado() {
		return solicitado;
	}

	public void setSolicitado(double solicitado) {
		this.solicitado = solicitado;
	}

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
        final TrasladoDet other = (TrasladoDet) obj;
        return new EqualsBuilder()
        .append(getSucursal(),other.getSucursal())
        .append(getDocumento(),other.getDocumento())
        .append(getTipoDocto(),other.getTipoDocto())
        .append(getRenglon(),other.getRenglon())
        .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17,35)
        .append(getRenglon())
        .append(getDocumento())
        .append(getTipoDocto())
        .append(getRenglon())
        .toHashCode();
    }
    
    @ManyToOne(optional=true) 
    @JoinColumn(name="ANALISIS_FLETE_ID")
	private AnalisisDeFlete analisisFlete;
	
    @Column(name="COSTO_FLETE",nullable=false,scale=6,precision=16)
	private BigDecimal costoFlete=BigDecimal.ZERO;
	
	@Column(name="REMISION",length=20)
	public String  remision;
	
    
    public AnalisisDeFlete getAnalisisFlete() {
		return analisisFlete;
	}

	public void setAnalisisFlete(AnalisisDeFlete analisisFlete) {
		this.analisisFlete = analisisFlete;
	}	

	public BigDecimal getCostoFlete() {
		return costoFlete;
	}

	public void setCostoFlete(BigDecimal costoFlete) {
		Object old=this.costoFlete;
		this.costoFlete = costoFlete;
		firePropertyChange("costoFlete", old, costoFlete);
	}

	public BigDecimal getImporteDelFlete(){
		BigDecimal imp=getCostoFlete().multiply(BigDecimal.valueOf(getCantidadEnUnidad()));
		return CantidadMonetaria.pesos(imp).amount();
	}

	public String getRemision() {
		return remision;
	}

	public void setRemision(String remision) {
		this.remision = remision;
	}
	
	public void actualizarCosto(){
		setCosto(getCostoPromedio()
				.add(getCostoFlete()));
	}

	
	
	
}
