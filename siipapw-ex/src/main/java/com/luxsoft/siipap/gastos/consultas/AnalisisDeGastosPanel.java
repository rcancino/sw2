package com.luxsoft.siipap.gastos.consultas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.jdesktop.swingx.JXTable;
import org.springframework.orm.hibernate3.HibernateCallback;

import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.Matchers;

import com.luxsoft.siipap.model.gastos.GCompraDet;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.matchers.FechaMayorAMatcher;
import com.luxsoft.siipap.swing.matchers.FechaMenorAMatcher;

public class AnalisisDeGastosPanel extends FilteredBrowserPanel<AnalisisDeGasto>{
	
	private FechaMayorAMatcher fmayorMatcher;
	private FechaMenorAMatcher fmenorMatcher;
	private FacturasMatcherEditor facturasMatcher;
	

	public AnalisisDeGastosPanel() {
		super(AnalisisDeGasto.class);
		addProperty("proveedorId","proveedor","sucursalId","sucursal"
				   ,"rubroId","rubro","compraId","f_contable","f_compra","compraDetId"
				   ,"productoId","descripcion","importe","impuesto_imp"
				   ,"ret1_impp","ret2_imp","total","ietu","inversion","documento"
				   ,"f_docto","totalDoc");
		addLabels("ProvId","Proveedor","SucId","Sucursal"
				   ,"RubroId","Rubro","CompId","F_Contable","F_Compra","CompraDetId"
				   ,"ProductoId","Descripcion","Importe","Impuesto_imp"
				   ,"Ret1_impp","Ret2_imp","Total","IETU","Inversion","Documento"
				   ,"F_Docto","Total Doc");
		fmayorMatcher=new FechaMayorAMatcher();
		fmayorMatcher.setDateField("f_contable");
		fmenorMatcher=new FechaMenorAMatcher();
		fmenorMatcher.setDateField("f_contable");
		facturasMatcher=new FacturasMatcherEditor();
		installCustomMatcherEditor("Tipo", facturasMatcher.getSelector(), facturasMatcher);
		installCustomMatcherEditor("Fecha Ini", fmayorMatcher.getFechaField(), fmayorMatcher);
		installCustomMatcherEditor("Fecha Fin", fmenorMatcher.getFechaField(), fmenorMatcher);
		installTextComponentMatcherEditor("CXPFactura", "documento");
		installTextComponentMatcherEditor("Sucursal", "sucursal");
		installTextComponentMatcherEditor("Proveedor", "proveedor");
		installTextComponentMatcherEditor("Producto", "descripcion");
		installTextComponentMatcherEditor("Rubro", "rubro");
		installTextComponentMatcherEditor("Mes", "mes");
		
	}
	
	
	protected void adjustMainGrid(final JXTable grid){
		grid.getColumnExt("ProvId").setVisible(false);
		grid.getColumnExt("SucId").setVisible(false);		
		grid.getColumnExt("RubroId").setVisible(false);
		grid.getColumnExt("ProductoId").setVisible(false);
		grid.getColumnExt("Impuesto_imp").setVisible(false);
		grid.getColumnExt("Ret1_impp").setVisible(false);
		grid.getColumnExt("Ret2_imp").setVisible(false);
		grid.getColumnExt("IETU").setVisible(false);
		grid.getColumnExt("Inversion").setVisible(false);
		
	}


	@Override
	protected JComponent buildContent() {
		JSplitPane sp=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		sp.setTopComponent(super.buildContent());
		sp.setBottomComponent(buildDetailComponent());
		sp.setResizeWeight(.65);
		sp.setOneTouchExpandable(true);
		return sp;
	}
	
	private JTabbedPane detailPanel;
	
	private JComponent buildDetailComponent(){
		detailPanel=new JTabbedPane();
		
		RubrosPanel rp=new RubrosPanel(sortedSource);
		rp.setLabels("Rubro","Importe","Participación");
		detailPanel.addTab("Rubro", rp);
		
		SucursalPanel sp=new SucursalPanel(sortedSource);
		sp.setLabels("Sucursal","Importe","Participación");
		detailPanel.addTab("Sucursal", sp);
		
		// Por Periodo
		PeriodoPanel pp=new PeriodoPanel(sortedSource);
		pp.setLabels("Periodo","Importe","Participación");
		detailPanel.addTab("Periodo", pp);
		
		// Por Proveedor
		ProveedorlPanel px=new ProveedorlPanel(sortedSource);
		px.setLabels("Proveedor","Importe","Participación");
		detailPanel.addTab("Proveedor", px);
		
		// Por Producto-Servicio
		ProductoPanel pz=new ProductoPanel(sortedSource);
		pz.setLabels("Prod/Serv","Importe","Participación");
		detailPanel.addTab("Prod/Serv", pz);
		
		//Comparativo Mensual
		ComparativoMensualPanel cm=new ComparativoMensualPanel(sortedSource);
		detailPanel.addTab("Comparativo",cm);
		
		return detailPanel;
	}




	@Override
	protected List<AnalisisDeGasto> findData() {
		return new DataLoader().load();
	}
	
	/**
	 * Taladreo de informacion
	 */
	@Override
	protected void doSelect(Object bean) {
		AnalisisDeGasto a=(AnalisisDeGasto)bean;
		
	}






	private static class DataLoader {
		public List<AnalisisDeGasto> load(){
			return ServiceLocator2.getHibernateTemplate().executeFind(new HibernateCallback(){

				public Object doInHibernate(Session session)	throws HibernateException, SQLException {
					List<AnalisisDeGasto> beans=new ArrayList<AnalisisDeGasto>();
					ScrollableResults rs=session.createQuery(
							"from GCompraDet det " +
							" left join fetch det.compra c" +
							" left join fetch c.proveedor prov" +
							" left join fetch det.producto prod" +
							" left join fetch det.rubro r" 
							//+" left join fetch r.parent rr"
							)
							//.setMaxResults(1000)
							.scroll();
					int buffer=0;
					while(rs.next()){
						GCompraDet det=(GCompraDet)rs.get()[0];
						//System.out.println("Procesando: "+det);
						AnalisisDeGasto a=new AnalisisDeGasto(det);
						beans.add(a);						
						if(buffer++%20==0){
							System.out.println("Commiting  "+rs.getRowNumber());
							session.flush();
							session.clear();
						}
					}
					return beans;
				}
				
			});
		}
	}
	
	private class FacturasMatcherEditor extends AbstractMatcherEditor<AnalisisDeGasto> implements ActionListener{
		
		private JComboBox selector;
		
		public FacturasMatcherEditor(){
			String[] vals={"Todas","Con CXPFactura","Sin CXPFactura"};
			selector=new JComboBox(vals);
			selector.addActionListener(this);
		}
		
		public JComboBox getSelector(){
			return selector;
		}

		public void actionPerformed(ActionEvent e) {
			String val=selector.getSelectedItem().toString();
			if("Con CXPFactura".equalsIgnoreCase(val)){
				fireChanged(new ConFacturaMatcher());
						
			}else if("Sin CXPFactura".equalsIgnoreCase(val)){
				fireChanged(Matchers.invert(new ConFacturaMatcher()));
			}
			else
				fireMatchAll();
				
			
		}
		
		class ConFacturaMatcher implements Matcher<AnalisisDeGasto> {
			public boolean matches(AnalisisDeGasto item) {								
				return !StringUtils.isBlank(item.getDocumento());
			}
		};
		
		
				
		
	}
	
	
	//Probar eficiencia del query
	public static void main(String[] args) {
		new DataLoader().load();
	}

}
