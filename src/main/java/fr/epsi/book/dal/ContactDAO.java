package fr.epsi.book.dal;

import fr.epsi.book.domain.Book;
import fr.epsi.book.domain.Contact;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContactDAO implements IDAO<Contact, Long> {
	
	private static final String INSERT_QUERY = "INSERT INTO CONTACT (name, email, telephone, type_var, type_num, book_id) VALUES (?,?,?,?,?,?)";
	private static final String FIND_BY_ID_QUERY = "SELECT * FROM CONTACT WHERE id=?";
	private static final String FIND_ALL_QUERY = "SELECT * FROM CONTACT";
	private static final String FIND_BY_BOOK_ID_QUERY = "SELECT * FROM CONTACT WHERE book_id=?";
	private static final String UPDATE_QUERY = "UPDATE CONTACT SET name=?, email=?, telephone=? WHERE id=?";
	private static final String DELETE_QUERY = "DELETE FROM CONTACT WHERE id = ?";
	
	@Override
	public void create( Contact c ) throws SQLException {
		Connection connection = PersistenceManager.getConnection();
		PreparedStatement st = connection.prepareStatement( INSERT_QUERY, Statement.RETURN_GENERATED_KEYS );
		st.setString( 1, c.getName() );
		st.setString( 2, c.getEmail() );
		st.setString( 3, c.getPhone() );
		st.setString( 4, c.getType().getValue() );
		st.setInt( 5, c.getType().ordinal() );
		st.setLong(6, c.getBook().getId());
		st.executeUpdate();
		ResultSet rs = st.getGeneratedKeys();
		
		if ( rs.next() ) {
			c.setId( rs.getLong( 1 ) );
		}
		st.close();
		rs.close();
	}
	
	@Override
	public Contact findById( Long aLong ) throws SQLException {
		BookDAO bookDAO = new BookDAO();
		Connection connection = PersistenceManager.getConnection();
		PreparedStatement st = connection.prepareStatement(FIND_BY_ID_QUERY);
		st.setLong(1, aLong);
		ResultSet rs = st.executeQuery();
		Contact contact = null;
		if(rs.next()){
			contact = new Contact();
			contact.setName(rs.getString("name"));
			contact.setId(aLong);
			contact.setEmail(rs.getString("email"));
			contact.setPhone(rs.getString("telephone"));
			if(rs.getString("type_var").equals("Perso")){
				contact.setType(Contact.Type.PERSO);
			}else{
				contact.setType(Contact.Type.PRO);
			}
			Book book = bookDAO.findById(rs.getLong("book_id"));
			contact.setBook(book);
		}
		st.close();
		rs.close();
		return contact;
	}
	
	@Override
	public List<Contact> findAll() throws SQLException {
		BookDAO bookDAO = new BookDAO();
		List<Contact> list = new ArrayList<>();
		Connection connection = PersistenceManager.getConnection();
		PreparedStatement st = connection.prepareStatement(FIND_ALL_QUERY);
		ResultSet rs = st.executeQuery();
		while(rs.next()){
			Contact contact = new Contact();
			contact.setId(rs.getLong("id"));
			contact.setName(rs.getString("name"));
			contact.setEmail(rs.getString("email"));
			contact.setPhone(rs.getString("telephone"));
			if(rs.getString("type_var").equals("Perso")){
				contact.setType(Contact.Type.PERSO);
			}else{
				contact.setType(Contact.Type.PRO);
			}
			Book book = bookDAO.findById(rs.getLong("book_id"));
			contact.setBook(book);
			list.add(contact);
		}
		st.close();
		rs.close();
		return list;
	}

	public List<Contact> findByBookId(Long aLong) throws SQLException {
		BookDAO bookDAO = new BookDAO();
		List<Contact> list = new ArrayList<>();
		Connection connection = PersistenceManager.getConnection();
		PreparedStatement st = connection.prepareStatement(FIND_BY_BOOK_ID_QUERY);
		st.setLong(1, aLong);
		ResultSet rs = st.executeQuery();
		while(rs.next()){
			Contact contact = new Contact();
			contact.setName(rs.getString("name"));
			contact.setId(rs.getLong("id"));
			contact.setEmail(rs.getString("email"));
			contact.setPhone(rs.getString("telephone"));
			if(rs.getString("type_var").equals("Perso")){
				contact.setType(Contact.Type.PERSO);
			}else{
				contact.setType(Contact.Type.PRO);
			}
			Book book = bookDAO.findById(aLong);
			contact.setBook(book);
			list.add(contact);
		}
		st.close();
		rs.close();
		return list;
	}
	
	@Override
	public Contact update( Contact o ) throws SQLException {
		Connection connection = PersistenceManager.getConnection();
		PreparedStatement st = connection.prepareStatement(UPDATE_QUERY);
		st.setString(1,o.getName());
		st.setString(2,o.getEmail());
		st.setString(3,o.getPhone());
		st.setLong(4,o.getId());
		st.executeUpdate();
		st.close();
		return o;
	}
	
	@Override
	public void remove( Contact o ) throws SQLException {
		Connection connection = PersistenceManager.getConnection();
		PreparedStatement st = connection.prepareStatement(DELETE_QUERY);
		st.setLong(1,o.getId());
		st.executeUpdate();
		st.close();
	}
}


