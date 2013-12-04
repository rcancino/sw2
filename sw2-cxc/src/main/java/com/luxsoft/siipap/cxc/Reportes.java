package com.luxsoft.siipap.cxc;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

import com.luxsoft.siipap.model.Modulos;
import com.luxsoft.siipap.model.Permiso;

public enum Reportes {
	
	DiarioDeCobranza,
	RecepcionDeFacturas,
	NotaDeCredito,
	DiarioDeCobranzaCre,
	CobranzaCredito,
	PagosConNotaCre,
	AuxiliarNCCre,
	Provision,
	ClientesVencidos,
	Depositos,
	ChequeDevueltoContaForm,
	VentasPorVendedorReport,
	VentasCreditoContadoReport,
	ClientesCreditoReport,
	ClienteCreditoDetalleReport,
	RevisionYCobro,
	NotasDeCreditoGeneradas,
	NotasDeCargoGeneradas
	;
	
	public static List<Permiso> toPermisos(){
		final List<Permiso> permisos=new ArrayList<Permiso>();
		for(Reportes k:values()){
			final Permiso p=new Permiso(k.name(),k.name(),Modulos.CXC);
			permisos.add(p);
		}
		return permisos;
	}

	
	public String toString(){
		return StringUtils.uncapitalize(name());
	}

	
	public String getId() {
		return StringUtils.uncapitalize(name());
	}

}
