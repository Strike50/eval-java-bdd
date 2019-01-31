package fr.epsi.book.domain;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement
public class Book implements Serializable {
	
	private Long id;
	private String code;
	private Map<Long, Contact> contacts;
	
	public Book() {
		contacts = new HashMap<>(  );
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId( Long id ) {
		this.id = id;
	}
	
	public String getCode() {
		return code;
	}
	
	public void setCode( String code ) {
		this.code = code;
	}
	
	public Map<Long, Contact> getContacts() {
		return contacts;
	}
	
	public void setContacts( Map<Long, Contact> contacts ) {
		this.contacts = contacts;
	}
	
	public void addContact( Contact contact ) {
		contacts.put( contact.getId(), contact );
	}
	
	public void removeContact( Contact contact ) {
		contacts.remove( contact.getId() );
	}
	
	public void removeContact( Long id ) {
		contacts.remove( id );
	}
}
