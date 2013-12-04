package com.luxsoft.siipap.gastos.catalogos;

import javax.swing.Action;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;




import com.luxsoft.siipap.model.gastos.Consignatario;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.dialog.UniversalAbstractCatalogDialog;

/**
 *  
 * 
 * @author Ruben Cancino,Octavio Hernandez
 *
 */
public class ConsignatariosBrowser extends UniversalAbstractCatalogDialog<Consignatario>{
	
	private static Action showAction;

	public ConsignatariosBrowser() {
		super(Consignatario.class,new BasicEventList<Consignatario>(), "Catálogo de consignatarios");
	}

	
	@Override
	protected TableFormat<Consignatario> getTableFormat() {
		final String[] cols={"id","apellidoP","apellidoM","nombres","departamento","sucursal"};
		final String[] names={"Id","Apellido P","Apellido M","Nombre(s)","Departamento.clave","Sucursal.nombre"};
		return GlazedLists.tableFormat(Consignatario.class, cols,names);
	}
	
	/**** Personalizacion de comportamiento (A/B/C) ****/
	
	@Override
	protected Consignatario doInsert() {
		Consignatario res=ConsignatarioForm.showForm(new Consignatario());
		if(res!=null)
			return save(res);
		return null;
	}
	
	@Override
	protected Consignatario doEdit(Consignatario bean) {
		Consignatario res=ConsignatarioForm.showForm(bean);
		if(res!=null)
			return save(res);
		return null;
	}
	
	protected void doView(Consignatario bean){
		ConsignatarioForm.showForm(bean,true);
	}
	
	
	
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
		ConsignatariosBrowser dialog=new ConsignatariosBrowser();
		dialog.open();
	}
	

	public static void main(String[] args) {
		openDialog();
		System.exit(0);
		
	}

}
