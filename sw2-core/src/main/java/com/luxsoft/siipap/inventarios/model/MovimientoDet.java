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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;

/**
 *
 * @author Ruben Cancino
 */
@Entity
@Table(name="SX_INVENTARIO_MOV",uniqueConstraints=@UniqueConstraint(
		columnNames={"SUCURSAL_ID","CONCEPTO","DOCUMENTO","RENGLON"}))
public class MovimientoDet extends Inventario{
    
	@ManyToOne(optional=true)
	@JoinColumn (name="MOVI_ID",nullable=true)
	private Movimiento movimiento;
		
	@Column (name="CONCEPTO",length=3,nullable=true)
	public String concepto;
		
	@Column (name="COMENTARIO2",length=50)
	private String comentario2;
	
	public MovimientoDet(){		
	}
	
	public Movimiento getMovimiento() {
		return movimiento;
	}

	public void setMovimiento(Movimiento movimiento) {
		this.movimiento = movimiento;
	}

	@Override
	public String getTipoDocto() {
		if(StringUtils.isBlank(getConcepto()))
			return "MOV";
		else
			return getConcepto();
	}

	public String getConcepto() {
		return concepto;
	}

	public void setConcepto(String concepto) {
		this.concepto = concepto;
	}	


	public String getComentario2() {
		return comentario2;
	}

	public void setComentario2(String comentario2) {
		this.comentario2 = comentario2;
	}
	
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
        final MovimientoDet other = (MovimientoDet) obj;
        return new EqualsBuilder()
        .append(getConcepto(),other.getConcepto())   
        .append(getProducto(),other.getProducto())
        .append(getCantidad(),other.getCantidad())
         .append(getId(),other.getId())
        .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17,35)
        .append(getConcepto())        
        .append(getProducto())
        .append(getCantidad())
        .append(getId())
        .toHashCode();
    }

    @Override
    public String toString() {
        return  new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)        
        .append(getConcepto())
        .append(getProducto())        
        .append(getCantidad())
        .toString();
    }
    
    @Enumerated(EnumType.STRING)
	@Column (name="TIPO_CIS",nullable=true, length=50)	
    private TipoCIS tipoCis;
    
    public void setTipoCis(TipoCIS tipoCis) {
    	Object old=this.tipoCis;
		this.tipoCis = tipoCis;
		firePropertyChange("tipoCis", old, tipoCis);
	}
    public TipoCIS getTipoCis() {
		return tipoCis;
	}
    
    public static enum TipoCIS{
    	NO_DEDUCIBLE
    	,MATERIAL_EMPAQUE
    	,PAPELERIA
    	,PUBLICIDAD_Y_PROPAGANDA
    	,PELERIA
    }

}
