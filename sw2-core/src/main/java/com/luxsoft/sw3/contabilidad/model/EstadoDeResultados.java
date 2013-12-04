package com.luxsoft.sw3.contabilidad.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


import org.hibernate.validator.NotNull;
import org.hibernate.validator.Range;

@Entity
@Table (name="SX_CONTA_ESTADO_RESULTADOS")
public class EstadoDeResultados {
	
	@Id @GeneratedValue (strategy=GenerationType.AUTO)
	@Column (name="ID")
	private Long id;
	
	@Column(name="FECHA",nullable=false)
	@NotNull	
	private Date fecha=new Date();
	
	@Column(name="YEAR",nullable=false)
    @NotNull
	private int year;
	
	@Column(name="MES",nullable=false)
	@NotNull 
	@Range(min=1,max=12)
	private int mes;	
	
	@Column(name="DESCRIPCION",length=255)
	@NotNull
	private String descripcion;
	
	@Column(name="GRUPO",length=10)
	@NotNull
	private String grupo;
	
	@Column (name="IMPORTE",nullable=false,scale=6,precision=16)
	@NotNull
	private BigDecimal importe=BigDecimal.ZERO;
	
	@Column(name="PARTICIPACION")
	private double participacion;
	
	@Column (name="IMPORTE_MES_ANT",nullable=false,scale=6,precision=16)
	@NotNull
	private BigDecimal importeMesAnterior=BigDecimal.ZERO;
	
	@Column(name="PARTICIPACION_ANT")
	private double participacionMesAnterior;
	
	@Column (name="IMPORTE_ACU",nullable=false,scale=6,precision=16)
	@NotNull
	private BigDecimal acumuladoAnual=BigDecimal.ZERO;
	
	@Column(name="PARTICIPACION_ACU")
	private double participacionAcumulada;
	
	@Column (name="IMPORTE_ACU_ANT",nullable=false,scale=6,precision=16)
	@NotNull
	private BigDecimal acumuladoAnualEjercicioAnterior=BigDecimal.ZERO;
	
	@Column(name="PARTICIPACION_ACU_ANT")
	private double participacionAcumuladaEjercicioAnterior;

}
