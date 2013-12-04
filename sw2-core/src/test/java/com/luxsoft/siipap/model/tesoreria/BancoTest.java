package com.luxsoft.siipap.model.tesoreria;

import java.math.BigDecimal;

import junit.framework.TestCase;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;

public class BancoTest extends TestCase{
	
	public void testCrearConCuenta(){
		final Sucursal suc=new Sucursal();
		final Cuenta cta=getCuenta();
		cta.setMoneda(MonedasUtils.DOLARES);
		final Concepto tipo=getCargoTipo();
		CargoAbono cargo=CargoAbono.crearCargo(
				cta
				, BigDecimal.valueOf(10000)
				, DateUtil.toDate("01/02/2008")
				, "Sistemas"
				,tipo
				,suc);
		assertEquals("Sistemas", cargo.getAFavor());
		assertEquals(DateUtil.toDate("01/02/2008"), cargo.getFecha());
		assertEquals(BigDecimal.valueOf(10000), cargo.getImporte());
		assertEquals(cta.getMoneda(),cargo.getMoneda());
		assertEquals(cta, cargo.getCuenta());
		assertEquals(tipo, cargo.getConcepto());
		assertEquals(Concepto.Tipo.CARGO, cargo.getConcepto().getTipo());
		System.out.println("Pago:"+ToStringBuilder.reflectionToString(cta,ToStringStyle.MULTI_LINE_STYLE,false));
	}
	
	private Cuenta getCuenta(){
		Cuenta c=new Cuenta();
		c.setClave("TESTC");
		return c;
	}
	
	private Concepto getCargoTipo(){
		Concepto tipo=new Concepto();
		tipo.setId(new Long(1));
		tipo.setTipo(Concepto.Tipo.CARGO);
		return tipo;
	}

}
