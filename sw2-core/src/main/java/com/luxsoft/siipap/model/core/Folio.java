package com.luxsoft.siipap.model.core;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

/**
 * Entidad para administrar los folios del sistema
 * 
 * @author Ruben Cancino Ramos
 * 
 * drop table if exists sx_folios

	create table SX_FOLIOS (SUCURSAL_ID bigint not null, TIPO varchar(25) not null, FOLIO bigint not null, version integer not null, primary key (SUCURSAL_ID, TIPO), unique (SUCURSAL_ID, TIPO)) ENGINE=InnoDB

 *
 */
@Entity
@Table(name="SX_FOLIOS"
	,uniqueConstraints=@UniqueConstraint(columnNames={"SUCURSAL_ID","TIPO"})
)
	
public class Folio {
	
	/*
	@Id @Column(name="FOLIO_ID")
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	*/
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private FolioId id;
	
	@Version
	private int version;
	
	
	
	@Column(name="FOLIO",nullable=false)
	private Long folio;

	

	public FolioId getId() {
		return id;
	}

	public void setId(FolioId id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}
	
	

	public Long getFolio() {
		return folio;
	}

	public void setFolio(Long folio) {
		this.folio = folio;
	}
	
	/**
	 * Incrementa el folio en uno y lo regresa
	 * 
	 * @return
	 */
	public Long next(){
		if(folio==null)
			folio=0l;
		folio+=1l;
		return folio;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Folio other = (Folio) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	

}
