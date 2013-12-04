package com.luxsoft.siipap.support.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.UserType;

public class UserLogUserType implements UserType{

	
	
	public int[] sqlTypes() {
		return new int[]{Hibernate.TIME.sqlType()};
	}

	public Class returnedClass() {
		return ModificationLog.class;
	}

	public boolean isMutable(){
		return false;
	}

	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}
	
	public Serializable disassemble(Object value) throws HibernateException {		
		return (Serializable)value;
	}

	public Object assemble(Serializable cached, Object owner) throws HibernateException {		
		return cached;
	}

	public Object replace(Object original, Object target, Object owner)throws HibernateException {		
		return original;
	}

	public boolean equals(Object x, Object y) throws HibernateException {
		if(x==y)return true;
		if(x==null || y==null) return false;
		return x.equals(y);
	}

	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	public Object nullSafeGet(ResultSet rs, String[] names, Object owner)throws HibernateException, SQLException {
		Date creado=rs.getDate(names[0]);
		if(rs.wasNull()) return null;
		return creado;
	}

	public void nullSafeSet(PreparedStatement st, Object value, int index)throws HibernateException, SQLException {
		st.executeQuery("SELECT NOW()").getDate(0);
		
	}

	
	
	
	
	

	
}
