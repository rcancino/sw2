/*
 * Created on 1/12/2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.luxsoft.siipap.model;

import java.util.*;
import java.text.*;
import java.io.Serializable;
import java.math.*;

/**
 * @author Rubeus
 * 
 */


public class CantidadMonetaria implements Serializable,Comparable{
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public final static Currency PESOS;
	public final static Currency DOLARES;
	public final static Currency EUROS;
	
	static{
		Locale mx=new Locale("es","mx");
		PESOS=Currency.getInstance(mx);
		DOLARES=Currency.getInstance(Locale.US);
		EUROS=Currency.getInstance("EUR");
	}
    long amount;
    Currency currency;

    private CantidadMonetaria() {}
    
    public CantidadMonetaria( BigDecimal amount) {
    	this(amount.doubleValue(),PESOS);
    }
    
    public CantidadMonetaria( BigDecimal amount, Currency currency ) {
        this(amount.doubleValue(),currency);
    }
        
    public CantidadMonetaria( double amount, Currency currency ) {
        this.currency = currency;
        this.amount = Math.round( amount * centFactor() );
    }
    
    public CantidadMonetaria( long amount, Currency currency ) {
        this.currency = currency;
        this.amount = amount * centFactor();
    }

    public CantidadMonetaria( BigDecimal amount, Currency currency, int roundingMode ){
        this.currency = currency;
        amount = amount.movePointRight( currency.getDefaultFractionDigits() );
        amount = amount.setScale( 0, roundingMode );
        this.amount = amount.longValue();
    }
    
    
    public BigDecimal amount(){
        return BigDecimal.valueOf( amount, currency.getDefaultFractionDigits() );
    }

    public Currency currency(){
        return currency;
    }

    public boolean equals( Object other ) {
        return (other instanceof CantidadMonetaria) && equals( (CantidadMonetaria) other);        
    }

    public boolean equals( CantidadMonetaria other ) {
        return currency.equals( other.currency) && (amount == other.amount );
    }
    
    public int hashCode() {
        return (int) (amount ^ (amount >>> 32 ) );
    }
    
    public CantidadMonetaria add( CantidadMonetaria other ){
        assertSameCurrencyAs(other);
        return newMoney(amount + other.amount);
    }

    public CantidadMonetaria subtract( CantidadMonetaria other ){
        assertSameCurrencyAs( other );
        return newMoney( amount - other.amount );
    }
        
    public int compareTo( Object other ) {
        return compareTo( (CantidadMonetaria) other );
    }
    
    public int compareTo( CantidadMonetaria other ){
        assertSameCurrencyAs( other );
        if( amount < other.amount) return -1;
        else if (amount == other.amount) return 0;
        else return 1;
    }        
   
    public boolean greaterThan( CantidadMonetaria other ){
        return (compareTo(other) > 0);
    }

    public boolean lessThan( CantidadMonetaria other ){
        return (compareTo(other) < 0);
    }
    
    public CantidadMonetaria divide( double amount ) {
        return divide( new BigDecimal(amount) );
    }
    
    public CantidadMonetaria divide( BigDecimal amount ){        
        return divide( amount, BigDecimal.ROUND_HALF_EVEN );
    }
    
    public CantidadMonetaria divide(BigDecimal amount,int roundingMode ){
    	//return new CantidadMonetaria( amount().divide(amount), currency, roundingMode );
    	return new CantidadMonetaria( amount().divide(amount,5,RoundingMode.HALF_EVEN), currency, roundingMode );
    }

   public CantidadMonetaria multiply( double amount ) {
        return multiply( new BigDecimal(amount) );
    }

   public CantidadMonetaria multiply( BigDecimal amount ){        
        return multiply( amount, BigDecimal.ROUND_HALF_EVEN );
    }

    public CantidadMonetaria multiply( BigDecimal amount, int roundingMode ){
        return new CantidadMonetaria( amount().multiply( amount), currency, roundingMode );
    }

    public CantidadMonetaria[] allocate( int n ){
        CantidadMonetaria[] results = new CantidadMonetaria[n];        
        CantidadMonetaria lowResult = newMoney( amount / n );
        CantidadMonetaria highResult = newMoney( lowResult.amount + (amount>=0?1:-1) );

        int remainder = Math.abs( (int) amount % n );
        for( int i = 0; i < remainder; i++) results[i] = highResult;                
        for( int i = remainder; i < n; i++) results[i] = lowResult;

        return results;
    }

    public CantidadMonetaria[] allocate( long[] ratios ){
        long total = 0;
        for( int i=0; i<ratios.length; i++) total += ratios[i];
        long remainder = amount;
        
        CantidadMonetaria[] results = new CantidadMonetaria[ ratios.length];
        for( int i=0; i < results.length; i++){
            results[i] = newMoney( amount*ratios[i]/total );
            remainder -= results[i].amount;
        }
        if( remainder > 0 ){
            for( int i=0; i<remainder; i++){
                results[i].amount++;
            }
        }
        if( remainder < 0 ){
            for( int i=0; i>remainder; i--){
                results[i].amount--;
            }
        }        
        return results;
    }
    static NumberFormat nf = NumberFormat.getInstance(); 
    static {
        if( nf instanceof DecimalFormat){
            DecimalFormat format = (DecimalFormat) nf;
            format.applyPattern("#,##0.00 ¤¤");
        }  
    }

    public String toString(){
        nf.setCurrency( currency );
        nf.setMinimumFractionDigits( currency.getDefaultFractionDigits() );
        nf.setMaximumFractionDigits( currency.getDefaultFractionDigits() );
        
        return nf.format( amount().doubleValue() );
    }
    
    static Currency LOCAL_CURRENCY = Currency.getInstance( Locale.getDefault() );

    public static CantidadMonetaria local( double amount ){
        return new CantidadMonetaria( amount, LOCAL_CURRENCY );     
    }

    static public CantidadMonetaria valueOf( String str ) throws java.text.ParseException{
        Currency currency;
        Number number;        
        currency = Currency.getInstance( str.substring( str.length() - 3 ) );

        nf.setCurrency( currency );
        nf.setMinimumFractionDigits( currency.getDefaultFractionDigits() );
        nf.setMaximumFractionDigits( currency.getDefaultFractionDigits() );
                                
        number = nf.parse( str );
        return new CantidadMonetaria( number.doubleValue(), currency );
    }
 
    private void assertSameCurrencyAs(CantidadMonetaria arg){
        if(arg==null) 
            throw new IllegalArgumentException("La cantidad monetaria a operar no puede ser nula");
        if(!currency.equals(arg.currency))
            throw new IllegalArgumentException("No es valido hacer operaciones con monedas diferentes. \n" +
            		"La Cantidad origen es: "+this+" la cantidad a conbinar es:"+arg);
        
    }
 
    private CantidadMonetaria newMoney(long amount){
        CantidadMonetaria money = new CantidadMonetaria();
        money.currency = this.currency;
        money.amount = amount;
        
        return money;
    }
    
    public CantidadMonetaria abs(){
    	return new CantidadMonetaria(getAmount().abs().doubleValue(),getCurrency());
    }
   
    public static CantidadMonetaria pesos(double cantidad){
        Locale mx=new Locale("es","mx");
        Currency currency=Currency.getInstance(mx);
        return new CantidadMonetaria(cantidad,currency);
    }
    
    public static CantidadMonetaria pesos(BigDecimal cantidad){
        Locale mx=new Locale("es","mx");
        Currency currency=Currency.getInstance(mx);
        return new CantidadMonetaria(cantidad,currency);
    }

    public static CantidadMonetaria dolares(double cantidad){
        Currency currency=Currency.getInstance(Locale.US);
        return new CantidadMonetaria(cantidad,currency);
    }

    public static CantidadMonetaria euros(double cantidad){
        Currency currency=Currency.getInstance("EUR");
        return new CantidadMonetaria(cantidad,currency);
    }
 
    private static final int[] cents = new int[]{1, 10, 100, 1000 };    
    private int centFactor(){
         return cents[ currency.getDefaultFractionDigits() ];
    }   
    
	/**
	 * @return 
	 * @hibernate.property
	 * column="CANTIDAD"
	 */

    public BigDecimal getAmount() {
        return amount();
    }
    /*
    public void setAmount(BigDecimal amount) {
        this.amount = amount.longValue();
    }*/

    /**
	 * @return 
	 * @hibernate.property
	 * column="MONEDA"
	 */
    
    public Currency getCurrency() {
        return currency;
    }
    /*
    public void setCurrency(Currency currency) {
        this.currency = currency();
    }*/
}
