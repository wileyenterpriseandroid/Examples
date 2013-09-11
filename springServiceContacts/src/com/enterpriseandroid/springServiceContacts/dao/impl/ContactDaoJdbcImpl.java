/**
 * @author Zane Pan
 */
package com.enterpriseandroid.springServiceContacts.dao.impl;

import com.enterpriseandroid.springServiceContacts.dao.ContactDao;
import com.enterpriseandroid.springServiceContacts.dao.VersionNotMatchException;
import com.enterpriseandroid.springServiceContacts.dataModel.Contact;
import java.sql.Connection;
import java.sql.PreparedStatement;


import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;



public class ContactDaoJdbcImpl implements ContactDao {
	private final static org.apache.log4j.Logger log = Logger.getLogger(ContactDaoJdbcImpl.class);
	
	private static final String FIND_FIRSTNAME_SQL = "select * from contact where firstName = ?";
	private static final String FIND_UPDATETIME_SQL = "select * from contact where updateTime > ?";
	private static final String GET_SQL = "select * from contact where id = ?";
	private static final String GET_ALL_SQL = "select * from contact ";
	private static final String INSERT_SQL = "Insert into contact( firstName, lastName, phone, email, updateTime, version)  VALUES(?,?,?,?,?,?);";
	
	private static final String UPDATE_SQL = "update contact set firstname = ?, lastname=?, phone=?, email=?, updateTime=?, version=? where id = ? and version=?";
	private static final String DELETE_SQL = "delete from contact where id =?";

	
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplate;

	public void setDataSource(DataSource ds) {
		dataSource = ds;
		jdbcTemplate = new JdbcTemplate(dataSource);
	}


	@Override
	public Long storeOrUpdateContact(Contact contact) throws IOException {
		contact.setUpdateTime(System.currentTimeMillis());
		Long id = contact.getId();
		if ( id == null) {
			create(contact);
			return contact.getId();
		}
		Contact oldContact = getContact(id);
		if ( oldContact != null) {
			contact.setVersion(oldContact.getVersion());
			update(contact);
		} else {
			create(contact);
		}
		
		return id;
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
		if (contact.getVersion() != null) {
			throw new IllegalArgumentException("version has to be 0 for create");
		}
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			public PreparedStatement createPreparedStatement(
					Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement(INSERT_SQL,
						new String[] { "id" });
				ps.setString(1, contact.getFirstName());
				ps.setString(2, contact.getLastName());
				ps.setString(3, contact.getPhone());
				ps.setString(4, contact.getEmail());
				ps.setLong(5, contact.getUpdateTime());
				ps.setLong(6, new Long(1));
				return ps;
			}
		}, keyHolder);
		contact.setId(keyHolder.getKey().longValue());
		contact.setVersion(new Long(1));
	}

	private void update(Contact contact)  throws IOException {
		Long version = contact.getVersion();
		log.info("calling update, id:" + contact.getId() + " version: " + version);
		contact.setVersion(version +1); // inc the version by 1
		int rowupdated = jdbcTemplate.update(UPDATE_SQL, getUpdateSqlArgs(contact, version));
		
		if (rowupdated != 1)  {
			/** reset the version back */
			contact.setVersion(version);
	   		throw new VersionNotMatchException("Verson mismatch. row updated : " + rowupdated);
	   	}		
	}
	
	private Object[] getInsertSqlArgs(Contact contact) {
		return new Object[] { contact.getFirstName(), contact.getLastName(),
				contact.getPhone(), contact.getEmail(), contact.getUpdateTime(), new Long(0L)};
	}
	
	private Object[] getUpdateSqlArgs(Contact contact, Long version) {
		return new Object[] { contact.getFirstName(), contact.getLastName(),
				contact.getPhone(), contact.getEmail(), contact.getUpdateTime(),
				contact.getVersion(), contact.getId(), version };
	}


	@Override
	public Contact getContact(Long id) {
		try {
			return jdbcTemplate.queryForObject(GET_SQL, getRowMapper(), id);
		} catch( EmptyResultDataAccessException e) {
			return null;
		}
	}


	@Override
	public void delete(Long id)  throws IOException {
		jdbcTemplate.update(DELETE_SQL, new Object[] {id});		
	}
	
	private RowMapper<Contact> getRowMapper() {
		RowMapper<Contact> mapper = new RowMapper<Contact>() {
			public Contact mapRow(ResultSet rs, int rowNum) throws SQLException {
				Contact obj = new Contact();
				obj.setId(rs.getLong("id"));
				obj.setFirstName(rs.getString("firstName"));
				obj.setLastName(rs.getString("lastName"));
				obj.setPhone(rs.getString("phone"));
				obj.setEmail((rs.getString("email")));
				obj.setUpdateTime(rs.getLong("updateTime"));
				obj.setVersion(rs.getLong("version"));
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
