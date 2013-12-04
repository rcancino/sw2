package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.SimpleFileSystemModel;
import org.jdesktop.swingx.treetable.TreeTableModel;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TreeList;
import ca.odell.glazedlists.TreeList.ExpansionModel;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TreeTableSupport;

import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.Tipo;

public class CuentasContablesTreeTable extends JXTreeTable{

	
	public static List<CuentaContable> testData(){
		List<CuentaContable> res=new ArrayList<CuentaContable>();
		long id=1l;
		for(int i=1;i<=10;i++){
			String clave=StringUtils.leftPad(String.valueOf(i), 4, '0');
			CuentaContable c=new CuentaContable();
			c.setId(id++);
			c.setClave(clave);
			c.setDescripcion("Cuenta de Mayor: "+i);
			c.setTipo(Tipo.PASIVO);
			c.setSubTipo(Tipo.PASIVO.getSubTipos()[0]);
			c.setDetalle(false);
			//res.add(c);
			for(int x=1;x<=5;x++){
				String clave2=StringUtils.leftPad(String.valueOf(x), 4, '0');
				CuentaContable sc=new CuentaContable();
				sc.setId(id++);
				sc.setClave(clave+"-"+clave2);
				sc.setDescripcion("Sub Cuenta: "+x);
				sc.setTipo(Tipo.PASIVO);
				sc.setSubTipo(Tipo.PASIVO.getSubTipos()[0]);
				sc.setDetalle(true);
				sc.setPadre(c);
				c.getSubCuentas().add(sc);
				res.add(sc);
			}
			//System.out.println("Sub cuentas: "+c.getSubCuentas().size());
			
			
		}
		return res;
	}

	public static void viewInFrame(){
		JXFrame app=new JXFrame("Prueba para una tabla jerarquica",true);
		/*
		TreeTableModel treeModel=new SimpleFileSystemModel();
		JXTreeTable treeTable=new JXTreeTable(treeModel);		
		app.getContentPane().add(new JScrollPane(treeTable));
		*/
		final EventList<CuentaContable> eventList=GlazedLists.eventList(testData());
		TreeList.Format<CuentaContable> format=new CuentaContableTreeFormat();
		final TreeList<CuentaContable> treeList=new TreeList<CuentaContable>(eventList,format,TreeList.NODES_START_COLLAPSED);
		
		final TableFormat<CuentaContable> tableFormat=GlazedLists
			.tableFormat(CuentaContable.class
						,new String[]{"id","clave","descripcion"}
						,new String[]{"Id","Clave","Descripción"}
						);
		final EventTableModel<CuentaContable> etm=new EventTableModel<CuentaContable>(treeList, tableFormat);
		final JTable table=new JTable(etm);
		TreeTableSupport.install(table, treeList, 0);
		app.getContentPane().add(new JScrollPane(table));
		app.pack();
		app.setVisible(true);
	}
	
	public static class CuentaContableTreeFormat implements TreeList.Format<CuentaContable>{

		public boolean allowsChildren(CuentaContable element) {
			return !element.isDetalle();
		}

		public Comparator<? extends CuentaContable> getComparator(int depth) {
			//return GlazedLists.beanPropertyComparator(CuentaContable.class, "id");
			return null;
		}

		public void getPath(List<CuentaContable> path, CuentaContable element) {
			System.out.println("Analizando: "+element);
			int index=0;
			CuentaContable parent=element.getPadre();
			while(parent!=null){
				path.add(parent);
				parent=parent.getPadre();
			}
			path.add(element);
			/*
			for(CuentaContable sc:element.getSubCuentas()){
				//System.out.println("Agregando sub cuentas para : "+sc);
				path.add(sc);
			}*/
			//System.out.println("Analizando: "+element);
			//System.out.println("Verificando.....: "+element.getClave());
			/*
			if(!element.isDetalle()){
				System.out.println("Acumulativa: "+element.getClave());
				path.add(0,element);
			}else{
				System.out.println("De Detalle: "+element.getClave());
				for(CuentaContable row=element;row!=null;row=element.getPadre()){
					path.add(0, row);
				}
			}*/
			
		}
		
	}
	
	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				viewInFrame();
				//System.exit(0);
			}

		});
	}
	
	
	

}
