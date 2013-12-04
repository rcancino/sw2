package com.luxsoft.siipap.tesoreria.movimientos;

import javax.swing.Action;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog;

/**
 *  
 * 
 * @author Ruben Cancino,Octavio Hernandez
 *
 */
public class DepositosBrowser extends UniversalAbstractCatalogDialog<CargoAbono>{
	
	private static Action showAction;

	public DepositosBrowser() {
		super(CargoAbono.class,new BasicEventList<CargoAbono>(), "Catálogo de Depositos ");
	}

	
	@Override
	protected TableFormat<CargoAbono> getTableFormat() {
		return createStandarAbonoTableFormat();
	}
	
	/**** Personalizacion de comportamiento (A/B/C) ****/
	/*
	@Override
	protected Cuenta doInsert() {
		Cuenta res=CuentaForm.showForm(new Cuenta());
		if(res!=null)
			return save(res);
		return null;
	}
	
	@Override
	protected Cuenta doEdit(Cuenta bean) {		
		Cuenta res=CuentaForm.showForm(bean);
		if(res!=null)
			return save(res);
		return null;
	}
	
	protected void doView(Cuenta bean){
		CuentaForm.showForm(bean,true);
	}
	
	*/
	
	/**** Fin Personalizacion de comportamiento****/

	/**
	 * Acceso a una Action que permite mostrar este browser.	 * 
	 * Patron FactoryMethod para se usado desde  Spring
	 * Existe solo para facilitar el uso en Spring
	 * 
	 * @return
	 */
	public static Action getShowAction(){		
		showAction=new SWXAction(){
				@Override
				protected void execute() {
					openDialog();
				}				
			};		
		return showAction;
	}	
	
	public static void openDialog(){
		DepositosBrowser dialog=new DepositosBrowser();
		dialog.open();
	}
	

	public static void main(String[] args) {
		openDialog();
		
	}
	
	
	/**
	 * 
	private Long id;
	private Cuenta cuenta;
	private Date fecha;
	private String aFavor;
	private String rfc;
	private Currency moneda=MonedasUtils.PESOS;	
	private BigDecimal tc=BigDecimal.ONE;
	private BigDecimal importe=BigDecimal.ZERO;
	private String comentario;
	private String referencia;	
	private Concepto concepto;
	private FormaDePago formaDePago=FormaDePago.CHEQUE;
    private Sucursal sucursal;
	private Autorizacion autorizacion;
	private UserLog userLog=new UserLog();
	private Boolean aplicado=Boolean.FALSE;
	private boolean encriptado=false;
	 * @return
	 */
	public static TableFormat<CargoAbono> createStandarAbonoTableFormat() {
		final String[] cols={"id","cuenta","fecha","importe","importeReal","moneda","concepto.clave","formaDePago","referencia"};
		final String[] names={"id","cuenta","fecha","Importe (Encriptado)","Importe","Moneda","Concepto","formaDePago","referencia"};
		return GlazedLists.tableFormat(CargoAbono.class, cols,names);
	}

}
