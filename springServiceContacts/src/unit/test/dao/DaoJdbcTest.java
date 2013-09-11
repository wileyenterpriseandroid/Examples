package unit.test.dao;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;



@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "/spring/applicationContext-*.xml" })

public class DaoJdbcTest  {
	@Autowired
	@Qualifier(value = "jdbcDaoTestClient")
	private DaoTestClient client;
	
	
	@Before
	public void setup() throws IOException {
		client.removeALl();
	}
	
	@Test
	public void testUpdate() throws IOException {
		client.testUpdate();
	}
	
	@Test 
	public void testFind() throws IOException {
		client.testFind();
	}
	
	@Test 
	public void testFindChanged() throws IOException {
		client.testFindChanged();
	}
	
	@Test 
	public void testgetAll() throws IOException {
		client.testgetAll();
	}
	
}
