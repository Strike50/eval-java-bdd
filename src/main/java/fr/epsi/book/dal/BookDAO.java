package fr.epsi.book.dal;

import fr.epsi.book.domain.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO implements IDAO<Book, Long> {

	private static final String INSERT_QUERY = "INSERT INTO BOOK (code) VALUES (?)";
	private static final String FIND_BY_ID_QUERY = "SELECT * FROM BOOK WHERE id=?";
	private static final String FIND_ALL_QUERY = "SELECT * FROM BOOK";
	private static final String UPDATE_QUERY = "UPDATE BOOK SET code=? WHERE id=?";
	private static final String DELETE_QUERY = "DELETE FROM BOOK WHERE id = ?";
	
	@Override
	public void create( Book o ) throws SQLException {
		Connection connection = PersistenceManager.getConnection();
		PreparedStatement st = connection.prepareStatement( INSERT_QUERY, Statement.RETURN_GENERATED_KEYS );
		st.setString( 1, o.getCode() );
		st.executeUpdate();
		ResultSet rs = st.getGeneratedKeys();

		if ( rs.next() ) {
			o.setId( rs.getLong( 1 ) );
		}
	}
	
	@Override
	public Book findById( Long aLong ) throws SQLException {
		Connection connection = PersistenceManager.getConnection();
		PreparedStatement st = connection.prepareStatement(FIND_BY_ID_QUERY);
		st.setLong(1, aLong);
		ResultSet rs = st.executeQuery();
		while(rs.next()){
			Book book = new Book();
			book.setId(aLong);
			book.setCode(rs.getString("code"));
			return book;
		}
		return null;
	}
	
	@Override
	public List<Book> findAll() throws SQLException {
		List<Book> list = new ArrayList<>();
		Connection connection = PersistenceManager.getConnection();
		PreparedStatement st = connection.prepareStatement(FIND_ALL_QUERY);
		ResultSet rs = st.executeQuery();
		while(rs.next()){
			Book book = new Book();
			book.setId(rs.getLong("id"));
			book.setCode(rs.getString("code"));
			list.add(book);
		}
		return list;
	}
	
	@Override
	public Book update( Book o ) throws SQLException {
		Connection connection = PersistenceManager.getConnection();
		PreparedStatement st = connection.prepareStatement(UPDATE_QUERY);
		st.setString(1,o.getCode());
		st.setLong(2,o.getId());
		st.executeQuery();
		return o;
	}
	
	@Override
	public void remove( Book o ) throws SQLException {
		Connection connection = PersistenceManager.getConnection();
		PreparedStatement st = connection.prepareStatement(DELETE_QUERY);
		st.setLong(1,o.getId());
		st.executeQuery();
	}
}
