package com.luxsoft.sw3.ui.selectores;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.compras.dao.Compra2Dao;
import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.compras.model.CompraUnitaria;

import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.sw3.services.Services;

public class SelectorPartidasDeCompraPorDepurar extends AbstractSelector<CompraUnitaria>{
	
	public SelectorPartidasDeCompraPorDepurar(Compra2 compra) {
		super(CompraUnitaria.class, "Partidas por depurar compra: "+compra.getFolio());
		this.compra=compra;
	}

	private final Compra2 compra;

	@Override
	protected List<CompraUnitaria> getData() {
		ArrayList<CompraUnitaria> res=new ArrayList<CompraUnitaria>();
		for(CompraUnitaria det:compra.getPartidas()){
			if(det.getDepurado()==0)
				res.add(det);
		}
		return res;
	}

	@Override
	protected TableFormat<CompraUnitaria> getTableFormat() {
		String[] props={
				"compra.folio"
				,"sucursalNombre"
				,"clave"
				,"descripcion"
				,"unidad"
				,"solicitado"
				,"recibido"
				,"depurado"
				,"pendiente"
				,"porDepurar"};
		String[] labels={
				"Folio"
				,"Sucursal"
				,"Prod"
				,"Descrpción"
				,"U"
				,"Solicitado"
				,"Recibido"
				,"Depurado"
				,"Pendiente"
				,"Depurar"
				};
		boolean[] edits={false,false,false,false,false,false,false,false,false,true};
		return GlazedLists.tableFormat(CompraUnitaria.class, props,labels,edits);
	}
	
	@Override
	protected JComponent buildContent() {
		initGlazedLists();
		final JPanel panel=new JPanel(new BorderLayout());		
		//panel.add(buildToolbar(),BorderLayout.NORTH);
		grid=buildGrid();
		adjustGrid(grid);
		JComponent c=ComponentUtils.createTablePanel(grid);
		setPreferedDimension(c);
		panel.add(c,BorderLayout.CENTER);
		afterContentBuild(panel);
		return panel;
	}

	protected void afterContentBuild(JPanel content){
		content.add(BorderLayout.SOUTH,buildButtonBarWithOKCancel());
		content.setPreferredSize(new Dimension(759,500));
	}
	
	public void adjustGrid(final JXTable grid){
		grid.setPreferredSize(new Dimension(650,400));
	}
	
	public static List<CompraUnitaria> seleccionar(final Compra2 compra){
		SelectorPartidasDeCompraPorDepurar selector=new SelectorPartidasDeCompraPorDepurar(compra);
		selector.open();
		List<CompraUnitaria> depurables=new ArrayList<CompraUnitaria>();
		if(!selector.hasBeenCanceled()){			
			for(CompraUnitaria cu:selector.source){
				if(cu.isPorDepurar())
					depurables.add(cu);
			}
		}
		return depurables;	
	}
	
	
	public static void main(String[] args) {
		String id="8a8a81ea-25bd2f25-0125-bd5ca9f3-000c";
		Compra2 c=((Compra2Dao)Services.getInstance()
				.getContext().getBean("compra2Dao")).inicializarCompra(id);
		List<CompraUnitaria> res=seleccionar(c);
		for(CompraUnitaria cu:res){
			System.out.println("Por depurar: "+cu);
		}
	}
	
}
