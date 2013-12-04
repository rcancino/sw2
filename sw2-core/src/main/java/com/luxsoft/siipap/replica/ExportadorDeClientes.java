package com.luxsoft.siipap.replica;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Cliente;


public class ExportadorDeClientes implements ReplicaExporter{
	
	private Logger logger=Logger.getLogger(getClass());
	
	public Class getBeanClass() {
		return Cliente.class;
	}
	
	

	public String export(Object bean, Tipo tipo,Object...objects) {
		switch (tipo) {		
		case C:
			return generarCambio((Cliente)bean);
		case A:
		case B:			
		default:
			return null;
		}
	}

	private String generarCambio(Cliente cliente){
		StringBuffer buf = new StringBuffer();
		char SEP = '[';
		
		// Seccion 1 Archivo
		buf.append("CLIENTES.DBF");
		buf.append(SEP);
		
		// Seccion 2 TIPO de operacion
		buf.append(Tipo.C);
		buf.append(SEP);

		// Seccion 3 Llave del indice 1
		String clie=StringUtils.rightPad(cliente.getClave(), 7, ' ');
		buf.append(clie);
		buf.append(SEP);

		// Seccion 4 Indices
		buf.append("CLICLAVE.NTX|CLINOMBR.NTX|CLICLASI.NTX|CLICOBRA.NTX|CLICOBNO.NTX|CLIGRALC.NTX|CLIGRALN.NTX|CLIGRALL.NTX|CLIVENDE.NTX|CLILIDER.NTX|CLINUEVO.NTX|CLICLRFC.NTX");
		buf.append(SEP);

		// Seccion 5 Induce activo o de uso
		buf.append("1");
		buf.append(SEP);

		// Seccion 6 Campos a afectar
		String[] campos={				
				"CLINOMBRE"
				,"CLICALLE"
				,"CLICOLON"
				,"CLIDELEG"
				,"CLIPOSTAL"
				,"CLICUENTA"
				,"CLICLASIFI"
				,"CLIREVISIO"
				,"CLIPAGO"
				,"CLITIPVTO"
				,"CLILIMITE"
				,"CLIPLAZO"
				,"CLICOBRADOR"
				,"CLIAGENTE"
				,"CLIESTADO"
				,"CLITIPO"
				,"CLIFORPAGO"
				,"CLIOPERADO"
				//,"CLIEMAIL"
				,"CLIRFC"
				};
		
		String tipo="CON";
		if(cliente.getCredito()!=null){
			if(!cliente.getCredito().isSuspendido())
				tipo="CRE";
		}
		
		String[] valores={
				StringUtils.rightPad(cliente.getNombre(), 55)
				,getCalle(cliente)
				,getColonia(cliente)
				,getDelegacion(cliente)
				,getCp(cliente)//"CLIPOSTAL"
				,StringUtils.rightPad(cliente.getCuentaContable(), 12)//"CLICUENTA"
				,getClasificacion(cliente) //"N"//"CLICLASIFI"				
				,getDiaRevision(cliente)//"N"//"CLIREVISIO"
				,getDiaCobro(cliente)//"N"//"CLIPAGO"
				,cliente.getCredito()!=null?(cliente.getCredito().isVencimientoFactura()?"F":"V"):"F"//"C"//"CLITIPVTO"
				,cliente.getCredito()!=null?cliente.getCredito().getLinea().amount().toString():"0.00"//"N" "CLILIMITE"
				,cliente.getCredito()!=null?String.valueOf(cliente.getCredito().getPlazo()):"0"//"N"//"CLIPLAZO"
				,String.valueOf(cliente.getCobrador()!=null?cliente.getCobrador().getId():0) //"N"//"CLICOBRADOR"
				,String.valueOf(cliente.getVendedor()!=null?cliente.getVendedor().getId():0)//"N"//"CLIAGENTE"
				,cliente.getCredito()!=null?(cliente.isSuspendido()?"S":"A"):"A"//"C"//"CLIESTADO"
				//,cliente.getCredito()!=null?"CRE":"CON"//"CLITIPO"
				,tipo //"CLITIPO"
				,cliente.isPermitirCheque()?"CEH":"CE"//,"CLIFORPAGO"
				,cliente.getCredito()!=null?String.valueOf(cliente.getCredito().getOperador()):"0"//"CLIOPERADO"
				//,StringUtils.rightPad(cliente.getEmail(), 60)//"C"//"CLIEMAIL"
				,StringUtils.rightPad(cliente.getRfc(),20)
			};
		
		String[] tipos={
				"C"//"CLINOMBRE"
				,"C"//"CLICALLE"
				,"C"//"CLICOLON"
				,"C"//"CLIDELEG"
				,"C"//"CLIPOSTAL"
				,"C"//"CLICUENTA"
				,"N"//"CLICLASIFI"
				,"N"//"CLIREVISIO"
				,"N"//"CLIPAGO"
				,"C"//"CLITIPVTO"
				,"N"//"CLILIMITE"
				,"N"//"CLIPLAZO"
				,"N"//"CLICOBRADOR"
				,"N"//"CLIAGENTE"
				,"C"//"CLIESTADO"
				,"C"//"CLITIPO"
				,"C"//,"CLIFORPAGO"
				,"N"//"CLIOPERADO"
				//,"C"//"CLIEMAIL"	
				,"C" //CLIRFC
		};
		
		for(int index=0;index<campos.length;){
			buf.append(campos[index]);
			buf.append("?");
			buf.append(valores[index]);
			buf.append("?");
			buf.append(tipos[index]);
			index++;
			if(index!=campos.length)
				buf.append("|");
		}
		buf.append(SEP);

		// Seccion 7 ?
		buf.append("*");
		buf.append(SEP);

		// Seccion 8 ?
		buf.append("CLI");
		buf.append(SEP);

		// Seccion 9 ?
		buf.append("N");
		buf.append(SEP);

		// Seccion 10 Año ?
		Date fecha=new Date();
		int year = Periodo.obtenerYear(fecha);
		buf.append(year);
		String res = buf.toString();
		logger.info("String para exportar al cliente: \n"+res);
		return res;
	}
	
	public String export(Collection beans, Tipo tipo) {
		throw new UnsupportedOperationException("La exportacion de clientes no se puede hacer en bulk");
	}

	private String getCalle(Cliente c){
		String res="";
		if(c.getDireccionFiscal()!=null)
			if(c.getDireccionFiscal().getCalle()!=null){
				String numero=c.getDireccionFiscal().getNumero();
				String numeroInt=c.getDireccionFiscal().getNumeroInterior();
				String msg=MessageFormat.format("{0} {1} {2}",c.getDireccionFiscal().getCalle(),numero,numeroInt);
				return StringUtils.rightPad(msg, 50);
			}				
		return res;
	}
	
	private String getColonia(Cliente c){
		String res="";
		if(c.getDireccionFiscal()!=null)
			if(c.getDireccionFiscal().getCalle()!=null)
				return StringUtils.rightPad(c.getDireccionFiscal().getColonia(), 50);
		return res;
	}
	
	private String getDelegacion(Cliente c){
		String res="";
		if(c.getDireccionFiscal()!=null)
			if(c.getDireccionFiscal().getCalle()!=null)
				return StringUtils.rightPad(c.getDireccionFiscal().getMunicipio(), 50);
		return res;
	}
	
	private String getCp(Cliente c){
		String res="";
		if(c.getDireccionFiscal()!=null)
			if(c.getDireccionFiscal().getCalle()!=null)
				return StringUtils.leftPad(c.getDireccionFiscal().getCp(), 5,'0');
		return res;
	}
	
	private String getDiaRevision(Cliente c){
		int dia=0;
		if(c.getCredito()!=null){
			dia=c.getCredito().getDiarevision();
		}
		if(dia==6)
			dia=7;
		return String.valueOf(dia);
	}
	
	private String getDiaCobro(Cliente c){
		int dia=0;
		if(c.getCredito()!=null){
			dia=c.getCredito().getDiacobro();			
		}
		if(dia==6)
			dia=7;
		return String.valueOf(dia);
	}
	
	private String getClasificacion(Cliente c){
		if(c.isSuspendido())
			return String.valueOf(11);
		if( (c.getCredito()==null) &&  (!c.isPermitirCheque() ) && (StringUtils.isBlank(c.getRfc())) )
			return String.valueOf(8);
		if(c.getCredito()!=null)
			if(c.getCredito().isChequePostfechado())
				return String.valueOf(30);		
		if(!c.isPermitirCheque())
			return String.valueOf(13);
		
		
		return String.valueOf(1);
	}


	public String export(Object bean, Tipo tipo) {
		return generarCambio((Cliente)bean);
	}	

	

}
