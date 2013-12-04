/*
 * Created on 03-dic-2004
 *
 * by Propietario
 */
package com.luxsoft.siipap.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.time.DateUtils;

import com.luxsoft.siipap.util.DateUtil;

/**
 *  Periodo de tiempo
 * 
 * @author Ruben Cancino 
 */
public class Periodo implements Comparable{
      
    private Date fechaInicial;
    private Date fechaFinal;
    private DateFormat dateFormat=new SimpleDateFormat("dd/MM/yyyy");
    
    public Periodo() {
        this(Calendar.getInstance().getTime());
    }
    
    public Periodo(Date fechaInicial) {
        this(fechaInicial,fechaInicial);
    }
    
    public Periodo(Date fechaInicial, Date fechaFinal) {
        this.fechaInicial = fechaInicial;
        this.fechaFinal = fechaFinal;
    }
    
    /**
     * Construye periodo con formato de fecha dd/MM/yyyy
     * @param fechaIni
     * @param fechaFinal
     * @throws ParseException 
     */
    public Periodo(String fechaIni,String fechaFinal) {
    	try {
			setFechaInicial(dateFormat.parse(fechaIni));
			setFechaFinal(dateFormat.parse(fechaFinal));
		} catch (ParseException e) {
			throw new RuntimeException("Fecha invalida: "+e);
		}
    	
    	
    }
    
    /**
     * Construye periodo con formato de fecha dd/MM/yyyy
     * @param fechaIni
     * @param fechaFinal
     * @throws ParseException 
     */
    public Periodo(String fechaIni) {
    	try {
			setFechaInicial(dateFormat.parse(fechaIni));
			setFechaFinal(this.fechaInicial);
		} catch (ParseException e) {
			throw new RuntimeException("Fecha invalida: "+e);
		}
    	
    	
    }
    
    public Date getFechaFinal() {
        return fechaFinal;
    }
    
    public void setFechaFinal(Date fechaFinal) {
        this.fechaFinal = fechaFinal;
    }
    
    public Date getFechaInicial() {
        return fechaInicial;
    }
    
    public void setFechaInicial(Date fechaInicial) {
        this.fechaInicial = fechaInicial;
    }
    
    public String getFechaInicialAsString(){
        return dateFormat.format(this.fechaInicial);
    }
    
    public String getFechaFinalAsString(){
        return dateFormat.format(this.fechaFinal);
    }
    
    public DateFormat getDateFormat() {
        return dateFormat;
    }
    public void setDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }
    
    /**
     * Regresa el numero de dias en el periodo
     * 
     * @return
     */
    public int getDias(){
        long time=(fechaFinal.getTime()-fechaInicial.getTime());
        long l= time/(1000*60*60*24);
        return (int)l;
    }
    
    /**
     * Regresa un Iterator que incluye un Date por dia en el periodo incluyendo
     * la fecha final por lo que si el periodo es del 3/12/04 al 13/12/04
     * regresara 11 objetos
     * @return
     */
    public Iterator getDiasIterator(){
        return getListaDeDias().iterator();
    }
    
    public List<Date> getListaDeDias(){
        final List<Date> list=new ArrayList<Date>();
        final Calendar calendar=Calendar.getInstance();
        calendar.setTime(this.fechaInicial);
        Date fecha=this.getFechaInicial();
        while(fecha.compareTo(getFechaFinal())<=0){            
            list.add(fecha);
            calendar.add(Calendar.DATE,1);
            fecha=calendar.getTime();
        }
        return list;
    }
    
    public boolean equals(Object obj) {
        boolean equals=false;
        if(obj!=null && Periodo.class.isAssignableFrom(obj.getClass())){
            Periodo cve=(Periodo)obj;
            equals=new EqualsBuilder()
                     .append(this.fechaInicial,cve.getFechaInicial())
                     .append(this.fechaFinal,cve.getFechaFinal())
                     .isEquals();
        }
        return equals;
    }

    public int hashCode() {        
        return new HashCodeBuilder(1,7)
        	.append(this.fechaInicial)
        	.append(this.fechaFinal)
        	.toHashCode();
    }
    public String toString(){
        return dateFormat.format(getFechaInicial())+" - "+
        	dateFormat.format(getFechaFinal());
        
    }
    public String toString2(){
    	SimpleDateFormat df=new SimpleDateFormat("dd_MM_yyyy");
        return df.format(getFechaInicial())+"_al_"+
        	df.format(getFechaFinal());
        
    }
    
    /**
     * Verifica esta dentro del periodo
     * 
     * @param date
     * @return
     */
    public boolean isBetween(final Date date){
    	final Date fecha=date;
    	if(DateUtils.isSameDay(date, fechaInicial))
    		return true;
    	if(DateUtils.isSameDay(date, fechaFinal))
    		return true;
    	//final Date fecha=DateUtil.truncate(date, Calendar.DATE);
    	if(fecha.getTime()>=fechaInicial.getTime()){
    		return fecha.getTime()<=fechaFinal.getTime();
    		//if(fecha.compareTo(fechaFinal)<=0)
    			//return true;
    	}
    	return false;
    }
    
    
    /** Factorry Methods **/
    
    public static Periodo hoy(){
        return new Periodo(new Date());
    }
    
    public static Periodo getPeriodo(int dias){
        Calendar calendar=Calendar.getInstance();
        Date hoy=calendar.getTime();
        calendar.add(Calendar.DATE,dias);
        Periodo p;
        if(dias>0)
        	p=new Periodo(hoy,calendar.getTime());
        else
        	p=new Periodo(calendar.getTime(),hoy);
        return p;
    }
    
    public static Periodo getPeriodoDelMesActual(){
    	Calendar calendar=Calendar.getInstance();
        int mes=calendar.get(Calendar.MONTH);
        return getPeriodoEnUnMes(mes);
    }
    
    public static Periodo getPeriodoDelMesActual(Date toDate){
    	Calendar cal=Calendar.getInstance();
    	cal.setTime(toDate);
        cal.set(Calendar.DATE,1);
        Date start=cal.getTime();
        Periodo p=new Periodo(start,toDate);
        return p;
    }
   
    /**
     * Obtiene el periodo en el mes especificada para el año en curso en donde
     * Enero es 0 y Diciembre es 11 
     * La fecha inicial es el primer dia del mes y la final el ultimo
     * @param mes
     * @return
     */
    public static Periodo getPeriodoEnUnMes(int mes){
        Calendar cal=Calendar.getInstance();
        cal.set(Calendar.MONTH,mes);
        cal.set(Calendar.DATE,1);
        Date start=cal.getTime();
        int last=cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DATE,last);
        Date end=cal.getTime();
        Periodo p=new Periodo(start,end);
        return p;
    }
    public static Periodo getPeriodoEnUnMes(int mes,int ano){
        Calendar cal=Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.YEAR,ano);
        cal.set(Calendar.MONTH,mes);
        cal.set(Calendar.DATE,1);
        
        Date start=cal.getTime();
        int last=cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DATE,last);
        
        Date end=cal.getTime();
        Periodo p=new Periodo(start,end);
        return p;
    }
    
    public static Periodo getPeriodoDelYear(int year){
    	Periodo p1=getPeriodoEnUnMes(0,year);
    	Periodo p2=getPeriodoEnUnMes(11,year);
    	Periodo p=new Periodo(p1.getFechaInicial(),p2.getFechaFinal());
    	return p;
    }
    
    public static List<Periodo> getPeriodosDelYear(int year){
    	Periodo p1=getPeriodoEnUnMes(0,year);
    	Periodo p2=getPeriodoEnUnMes(11,year);
    	Periodo p=new Periodo(p1.getFechaInicial(),p2.getFechaFinal());
    	return periodosMensuales(p);
    }
    
    /**
     * Regresa el periodo del mes indicado por la fecha
     * ej: Para una fecha :15/09/2008 
     * regresa el periodo del 01/09/2008 al 30/09/2008
     * 
     * @param dia
     * @return
     */
    public static Periodo getPeriodoEnUnMes(Date dia){
    	int mes=obtenerMes(dia);
    	int year=obtenerYear(dia);
    	return getPeriodoEnUnMes(mes,year);
    }
    
    public static Periodo getPeriodo(String periodo,DateFormat dateFormat){
		Date date=null;
		try {
			date=dateFormat.parse(periodo);
		} catch (ParseException e) {
			throw new RuntimeException("El formato del periodo no es valido");
		}
		int mes=obtenerMes(date);
		int year=obtenerYear(date);
		Periodo p=Periodo.getPeriodoEnUnMes(mes,year);
		return p;
	}
    
    
    
    public static Periodo getPeriodoAnterior(Periodo p){
    	return getPeriodoAnterior(p,1);
    }
    
    /**
     * Obtiene el periodo n meses anterior al que se indica
     * Ej si p= 01/09/2007 - 11/09/2007
	 * Con meses=2 el periodo resultante es   : 01/07/2007 - 31/07/2007
     * 
     * @param p
     * @param meses
     * @return
     */
    public static Periodo getPeriodoAnterior(Periodo p,int meses){
    	Date f1=p.getFechaInicial();
    	int mes=obtenerMes(f1);
    	Calendar c=Calendar.getInstance();
    	c.set(Calendar.MONTH,mes);
    	c.add(Calendar.MONTH,-meses);
    	Date d2=c.getTime();
    	return getPeriodoEnUnMes(d2);
    }
    
    /**
     * Regresa un periodo en el que la fecha inciial es el primer dia
     * del mes atual menos n meses indicados y la fecha final la fecha
     * de hoy.
     * 
     * @param meses
     * @return
     */
    public static Periodo getPeriodoConAnteriroridad(int meses){
    	if(meses>0)
    		meses=meses*-1;
    	Calendar c=Calendar.getInstance();
    	c.add(Calendar.MONTH,meses);
    	c.set(Calendar.DATE, 1);
    	return new Periodo(c.getTime(),new Date());
    }
    
    public static int obtenerMes(Date d){
		Calendar c=Calendar.getInstance();
		c.setTime(d);
		int mes=c.get(Calendar.MONTH);
		return mes;
	}
    
	public static int obtenerYear(Date d){
		Calendar c=Calendar.getInstance();
		c.setTime(d);
		int year=c.get(Calendar.YEAR);
		return year;
	}
	
	
	public static int obtenerDia(Date d){
		Calendar c=Calendar.getInstance();
		c.setTime(d);
		int dia=c.get(Calendar.DATE);
		return dia;
	}
	
	public static Periodo periodoDeloquevaDelMes(){
		
		//int year=obtenerYear(d2);
		Calendar c=Calendar.getInstance();
		Date d2=c.getTime();
		c.set(Calendar.DATE,1);
		
		Date d1=c.getTime();
		return new Periodo(d1,d2);
		
	}
	
	public static Periodo periodoDeloquevaDelYear(){
		
		//int year=obtenerYear(d2);
		Calendar c=Calendar.getInstance();
		Date d2=c.getTime();
		
		c.set(Calendar.MONTH,0);
		c.set(Calendar.DATE,1);		
		
		Date d1=c.getTime();
		return new Periodo(d1,d2);
		
	}
	
	
	/**
	 * Genera un map con todos los meses para cada año del periodo
	 * solicitado
	 * 
	 * @param p
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Set<Map<Integer,Integer>> getMeses(final Periodo p){
		
		Set<Map<Integer,Integer>> set=new TreeSet<Map<Integer,Integer>>(new Comparator(){

			@SuppressWarnings("unchecked")
			public int compare(Object o1, Object o2) {
				Map<Integer,Integer> map1=(Map<Integer,Integer>)o1;
				Map<Integer,Integer> map2=(Map<Integer,Integer>)o2;
				Integer y1=map1.entrySet().iterator().next().getKey();
				Integer y2=map2.entrySet().iterator().next().getKey();
				if(y1.equals(y2)){
					Integer val1= map1.entrySet().iterator().next().getValue();
					Integer val2= map2.entrySet().iterator().next().getValue();
					return val1.compareTo(val2);
				}
				return y1.compareTo(y2);
			}
			
		});
		
		///Set<Map<Integer,Integer>> set=new HashSet<Map<Integer,Integer>>();
		//set=SetUtils.orderedSet(set);
		for(Date d:p.getListaDeDias()){
			Map<Integer,Integer> yearMonth=new HashMap<Integer,Integer>();
			int year=obtenerYear(d);
			int month=obtenerMes(d);
			yearMonth.put(year,month);
			//System.out.println("Analizando: Año:"+year+"\t Mes: "+month);
			set.add(yearMonth);
		}
		
		return set;
	}
	
	@SuppressWarnings("unchecked")
	public static List<Periodo> periodosMensuales(final Periodo p){
		List<Periodo> periodos=new ArrayList<Periodo>();
		Set<Map<Integer,Integer>> meses=getMeses(p);
		for(Map<Integer,Integer> mes:meses){
			int year=mes.keySet().iterator().next();
			int month=mes.values().iterator().next();
			Periodo pp=getPeriodoEnUnMes(month,year);
			periodos.add(pp);
		}
		Collections.sort(periodos);
		if(periodos.size()==1){
			periodos.clear();
			periodos.add(p);
		}
		return periodos;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object arg0) {
		Periodo p2=(Periodo)arg0;
		if(this.getFechaInicial().equals(p2.getFechaInicial())){
			return this.getFechaFinal().compareTo(p2.getFechaFinal());
		}
		return this.getFechaInicial().compareTo(p2.getFechaInicial());
	}
	
	public static void main(String[] args){
		Periodo p=Periodo.getPeriodoConAnteriroridad(2);
		Periodo p2=Periodo.getPeriodoAnterior(p, 2);
		System.out.println("Actual: "+p);
		System.out.println("Con 2 meses   : "+p2);
		
	}
	
    
}
