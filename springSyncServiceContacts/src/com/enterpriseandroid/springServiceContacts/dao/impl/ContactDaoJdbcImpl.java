/**
 * @author Zane Pan
 */
package com.enterpriseandroid.springServiceContacts.dao.impl;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import com.enterpriseandroid.springServiceContacts.dao.ContactDao;
import com.enterpriseandroid.springServiceContacts.dao.VersionNotMatchException;
import com.enterpriseandroid.springServiceContacts.dataModel.Contact;



public class ContactDaoJdbcImpl implements ContactDao {
	private final static org.apache.log4j.Logger log = Logger.getLogger(ContactDaoJdbcImpl.class);
	
	private static final String FIND_FIRSTNAME_SQL = "select * from contact_sync where firstName = ? ";
	private static final String FIND_UPDATETIME_SQL = "select * from contact_sync where updateTime > ?";
	private static final String GET_SQL = "select * from contact_sync where id = ?";
	private static final String GET_ALL_SQL = "select * from contact_sync";
	private static final String INSERT_SQL = "Insert into contact_sync( id, firstName, lastName, phone, email, updateTime, version)  VALUES(?,?,?,?,?,?,?);";
	private static final String UPDATE_SQL = "update contact_sync set firstname = ?, lastname=?, phone=?, email=?, updateTime=?, deleted=?, version=? where id = ? and version=?";
	private static final String DELETE_SQL = "delete from contact_sync where id =?";

	
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplate;

	public void setDataSource(DataSource ds) {
		dataSource = ds;
		jdbcTemplate = new JdbcTemplate(dataSource);
	}


	@Override
	public Contact storeOrUpdateContact(Contact contact) throws IOException {
		contact.setUpdateTime(System.currentTimeMillis());
		String id = contact.getId();
		
		if ( id != null && getContact(id) != null) {
			update(contact);
		} else {
			create(contact);
		}
		return contact;
		
	}

	@Override
	public List<Contact> findContactFirstName(String firstName, int start, int numOfmatches) {
		String query = FIND_FIRSTNAME_SQL + " limit " + new Long(start).toString() + " , " + new Long(numOfmatches).toString();
		return jdbcTemplate.query(query, getRowMapper(), new Object[] {firstName});
		
	}

	@Override
	public List<Contact> getAll(int start, int numOfmatches) throws IOException {
		String query = GET_ALL_SQL + " limit " + new Long(start).toString() + " , " + new Long(numOfmatches).toString();
		return jdbcTemplate.query(query, getRowMapper());	
	}
	
	private void create(final Contact contact) {
		log.info("calling create, id:" + contact.getId());
		if ( contact.getId() == null) {
			contact.setId(UUID.randomUUID().toString());
		}
		/*** version starts with 1 ***/
		contact.setVersion(1L);
		JdbcTemplate insert = new JdbcTemplate(dataSource);
		    insert.update(INSERT_SQL, getInsertSqlArgs(contact));
		        
	}
	
	
	private void update(Contact contact)  throws IOException {
		long version = contact.getVersion();
		contact.setVersion(version +1); // inc the version by 1
	
		int rowupdated = jdbcTemplate.update(UPDATE_SQL, getUpdateSqlArgs(contact, version));
		
		
		if (rowupdated != 1)  {
			/** reset the version back */
			contact.setVersion(version);
	   		throw new VersionNotMatchException("Verson mismatch. row updated : " + rowupdated);
	   	}
	}
	
	private Object[] getInsertSqlArgs(Contact contact) {
		return new Object[] { contact.getId(), contact.getFirstName(), contact.getLastName(),
				contact.getPhone(), contact.getEmail(), contact.getUpdateTime(), contact.getVersion()};
	}
	
	private Object[] getUpdateSqlArgs(Contact contact, Long version) {
		return new Object[] { contact.getFirstName(), contact.getLastName(),
				contact.getPhone(), contact.getEmail(), contact.getUpdateTime(), contact.isDeleted(),
				contact.getVersion(), contact.getId(), version };
	}


	@Override
	public Contact getContact(String id) {
		try {
			return jdbcTemplate.queryForObject(GET_SQL, getRowMapper(), id);
		} catch( EmptyResultDataAccessException e) {
			return null;
		}
	}


	@Override
	public void delete(String id)  throws IOException {
		jdbcTemplate.update(DELETE_SQL, new Object[] {id});		
	}
	
	private RowMapper<Contact> getRowMapper() {
		RowMapper<Contact> mapper = new RowMapper<Contact>() {
			@Override
			public Contact mapRow(ResultSet rs, int rowNum) throws SQLException {
				Contact obj = new Contact();
				obj.setId(rs.getString("id"));
				obj.setFirstName(rs.getString("firstName"));
				obj.setLastName(rs.getString("lastName"));
				obj.setPhone(rs.getString("phone"));
				obj.setEmail((rs.getString("email")));
				obj.setUpdateTime(rs.getLong("updateTime"));
				obj.setVersion(rs.getLong("version"));
				obj.setDeleted(rs.getBoolean("deleted"));
				return obj;
			}
		};
		return mapper;
	}


	@Override
	public List<Contact> findChanged(long timestamp, int start, int numOfmatches) {
		String query = FIND_UPDATETIME_SQL + " limit " + new Long(start).toString() + " , " + new Long(numOfmatches).toString();
		return jdbcTemplate.query(query, getRowMapper(), new Object[] {new Long(timestamp)});

	}
}
