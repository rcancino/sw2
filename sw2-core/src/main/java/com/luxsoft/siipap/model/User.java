package com.luxsoft.siipap.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.UserDetails;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * This class represents the basic "user" object in AppFuse that allows for authentication
 * and user management.  It implements Acegi Security's UserDetails interface.
 *
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 *         Updated by Dan Kibler (dan@getrolling.com)
 *         Extended to implement Acegi UserDetails interface
 *         by David Carter david@carter.net
 */
@Entity
@Table(name="SX_USUARIOS")
public class User extends BaseBean implements Serializable, UserDetails {
    private static final long serialVersionUID = 3832626162173359411L;

    private Long id;
    private String username;                    // required
    private String password;                    // required    
    private String firstName;                   // required
    private String lastName;                    // required
    private String confirmPassword;
    private String passwordHint;
    private String email;                       
    private String phoneNumber;
    private String website;
    private Address address = new Address();
    private Integer version;
    private Set<Role> roles = new HashSet<Role>();
    private boolean enabled;
    private boolean accountExpired;
    private boolean accountLocked;
    private boolean credentialsExpired;
    
    
	private String departamento;

    /**
     * Default constructor - creates a new instance with no values set.
     */
    public User() {}

    /**
     * Create a new instance and set the username.
     * @param username login name for user.
     */
    public User(final String username) {
        this.username = username;
    }

    @Id @GeneratedValue(strategy=GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    @Column(nullable=false,length=50,unique=true)
    @NotNull @Length(min=5,max=50)
    public String getUsername() {
        return username;
    }

    @Column(nullable=false)
    @NotNull @Length(min=5,max=255)
    public String getPassword() {
        return password;
    }

    @Transient
    public String getConfirmPassword() {
        return confirmPassword;
        
    }

    @Column(name="password_hint")
    public String getPasswordHint() {    	
        return passwordHint;
    }

    @Column(name="first_name",nullable=false,length=50)
    @NotNull @Length(min=1,max=50)
    public String getFirstName() {
        return firstName;
    }

    @Column(name="last_name",nullable=false,length=50)
    @NotNull @Length(min=1,max=50)
    public String getLastName() {
        return lastName;
    }

    @Column(nullable=true,unique=true)
    public String getEmail() {
        return email;
    }

    @Column(name="phone_number")
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getWebsite() {
        return website;
    }

    /**
     * Returns the full name.
     * @return firstName + ' ' + lastName
     */
    @Transient
    public String getFullName() {
        return StringUtils.abbreviate(
        		StringUtils.trimToEmpty(firstName) + ' ' + StringUtils.trimToEmpty(lastName),255);
    }

    @Transient
    public Address getAddress() {
        return address;
    }

    @ManyToMany(fetch = FetchType.EAGER) 
    @JoinTable(
            name="SX_USER_ROLE",
            joinColumns = { @JoinColumn( name="user_id") },
            inverseJoinColumns = @JoinColumn( name="role_id")
    )    
    public Set<Role> getRoles() {
        return roles;
    }
    
    
    
    @SuppressWarnings("unchecked")
	@Transient
    public List<Role> getRolesAsList(){
    	List<Role> rr=new ArrayList<Role>(roles);
    	return Collections.unmodifiableList(rr);
    }

    /**
     * Convert user roles to LabelValue objects for convenience.
     * @return a list of LabelValue objects with role information
     */
    @Transient
    public List<LabelValue> getRoleList() {
        List<LabelValue> userRoles = new ArrayList<LabelValue>();

        if (this.roles != null) {
            for (Role role : roles) {
                // convert the user's roles to LabelValue Objects
                userRoles.add(new LabelValue(role.getName(), role.getName()));
            }
        }

        return userRoles;
    }

    /**
     * Adds a role for the user
     * @param role the fully instantiated role
     */
    public void addRole(Role role) {
        getRoles().add(role);
    }
    
    /**
     * @see org.acegisecurity.userdetails.UserDetails#getAuthorities()
     * @return GrantedAuthority[] an array of roles.
     */
    @Transient
    public GrantedAuthority[] getAuthorities() {
    	List<GrantedAuthority> aut=new ArrayList<GrantedAuthority>();
    	for(Role rol:roles){
    		aut.add(rol);
    		aut.addAll(rol.getPermisos());
    	}
        return aut.toArray(new GrantedAuthority[0]);
    }

    @Version
    public Integer getVersion() {
        return version;
    }
    
    @Column(name="account_enabled")
    public boolean isEnabled() {
        return enabled;
    }
    
    @Column(name="account_expired",nullable=false)
    public boolean isAccountExpired() {
        return accountExpired;
    }
    
    /**
     * @see org.acegisecurity.userdetails.UserDetails#isAccountNonExpired()
     */
    @Transient
    public boolean isAccountNonExpired() {
        return !isAccountExpired();
    }

    @Column(name="account_locked",nullable=false)
    public boolean isAccountLocked() {
        return accountLocked;
    }
    
    /**
     * @see org.acegisecurity.userdetails.UserDetails#isAccountNonLocked()
     */
    @Transient
    public boolean isAccountNonLocked() {
        return !isAccountLocked();
    }

    @Column(name="credentials_expired",nullable=false)
    public boolean isCredentialsExpired() {
        return credentialsExpired;
    }
    
    /**
     * @see org.acegisecurity.userdetails.UserDetails#isCredentialsNonExpired()
     */
    @Transient
    public boolean isCredentialsNonExpired() {
        return !credentialsExpired;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setUsername(String username) {
    	Object old=this.username;
        this.username = username;
        firePropertyChange("username", old, username);
        
    }

    public void setPassword(String password) {    	
    	Object old=this.password;
        this.password = password;
        firePropertyChange("password", old, password);
    }

    public void setConfirmPassword(String confirmPassword) {
    	Object old=this.confirmPassword;        
        this.confirmPassword = confirmPassword;
        firePropertyChange("confirmPassword", old, confirmPassword);
    }

    public void setPasswordHint(String passwordHint) {
    	Object old=this.passwordHint;
        this.passwordHint = passwordHint;
        firePropertyChange("passwordHint", old, passwordHint);
    }

    public void setFirstName(String firstName) {
    	Object old=this.firstName;
        this.firstName = firstName;
        firePropertyChange("firstName", old, firstName);
    }

    public void setLastName(String lastName) {
    	Object old=this.lastName;
        this.lastName = lastName;
        firePropertyChange("lastName", old, lastName);
    }

    public void setEmail(String email) {
    	Object old=this.email;
        this.email = email;
        firePropertyChange("email", old, email);
    }

    public void setPhoneNumber(String phoneNumber) {
    	Object old=this.phoneNumber;
        this.phoneNumber = phoneNumber;
        firePropertyChange("phoneNumber", old, phoneNumber);
    }

    public void setWebsite(String website) {    	
        this.website = website;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
    
    public void setEnabled(boolean enabled) {
    	boolean old=this.enabled;
        this.enabled = enabled;
        firePropertyChange("enabled", old, enabled);
    }

    public void setAccountExpired(boolean accountExpired) {
    	boolean old=this.accountExpired;
        this.accountExpired = accountExpired;
        firePropertyChange("accountExpired", old, accountExpired);
    }
    
    public void setAccountLocked(boolean accountLocked) {
    	boolean old=this.accountLocked;
        this.accountLocked = accountLocked;
        firePropertyChange("accountLocked", old, accountLocked);
        
    }

    public void setCredentialsExpired(boolean credentialsExpired) {
    	boolean old=this.credentialsExpired;
        this.credentialsExpired = credentialsExpired;
        firePropertyChange("credentialsExpired", old, credentialsExpired);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }

        final User user = (User) o;

        return !(username != null ? !username.equals(user.getUsername()) : user.getUsername() != null);

    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return (username != null ? username.hashCode() : 0);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return lastName+' '+firstName;
    }
    
    private Set<Permiso> permisos;

    @Transient
	public Set<Permiso> getPermisos() {
    	if(permisos==null){
    		permisos=new TreeSet<Permiso>();
    		for(Role r:roles){
    			permisos.addAll(r.getPermisos());
    		}
    	}
		return permisos;
	}
    
    
    public boolean hasRole(final String name){
    	return getRole(name)!=null;
    }

    
    
    @Transient
    public Role getRole(final String name){
    	Object found=CollectionUtils.find(roles, new Predicate(){
			public boolean evaluate(Object object) {
				Role r=(Role)object;
				return name.equalsIgnoreCase(r.getName());
			}
    		
    	});
    	return (Role)found;
    }

    @Column(name="DEPARTAMENTO",nullable=true,length=150)
    public String getDepartamento() {
		return departamento;
	}

	public void setDepartamento(String departamento) {
		this.departamento = departamento;
	}
    
    
    
}
