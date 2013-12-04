package com.luxsoft.siipap.ventas.consultas;

import javax.swing.JComponent;


import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.views2.DefaultTaskView;
import com.luxsoft.siipap.swing.views2.InternalTaskTab;

public class V_AnalisisDeVentasView extends DefaultTaskView{
	
	private InternalTaskTab sucursalTab;
	
	public V_AnalisisDeVentasView(){		
	}
	
	public void showSucursales(){
		if(sucursalTab==null){
			AnalisisPorSucursalPanel panel=new AnalisisPorSucursalPanel();
			AnalisisTaskAdapter adapter=new AnalisisTaskAdapter(panel);
			adapter.setTitle("Sucursal");
			sucursalTab=new InternalTaskTab(adapter);
		}
		addTab(sucursalTab);
	}
	
	public void open(){
		showSucursales();
	}
	
	public static void main(String[] args) {
		SXAbstractDialog dialog=new SXAbstractDialog("TEST"){
			
			private V_AnalisisDeVentasView view;

			@Override
			protected JComponent buildContent() {
				view=new V_AnalisisDeVentasView();
				return view.getContent();
			}

			@Override
			protected void onWindowOpened() {
				view.open();
			}
			
		};
		dialog.setResizable(true);
		dialog.open();
		System.exit(0);
	}
	
	

}
