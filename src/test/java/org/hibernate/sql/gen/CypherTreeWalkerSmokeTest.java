/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.gen;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.boot.MetadataSources;
import org.hibernate.query.proposed.QueryParameter;
import org.hibernate.query.proposed.internal.ParameterMetadataImpl;
import org.hibernate.query.proposed.internal.QueryParameterBindingsImpl;
import org.hibernate.query.proposed.internal.QueryParameterNamedImpl;
import org.hibernate.query.proposed.internal.QueryParameterPositionalImpl;
import org.hibernate.query.proposed.spi.QueryParameterBindings;
import org.hibernate.sql.QueryParameterBindingTypeResolverImpl;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.convert.spi.SelectStatementInterpreter;
import org.hibernate.sql.convert.spi.cypher.CypherTreeWalker;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.SqmStatement;
import org.junit.Test;

/**
 * @author Steve Ebersole
 */
public class CypherTreeWalkerSmokeTest extends BaseUnitTest {
	@Override
	protected void applyMetadataSources(MetadataSources metadataSources) {
		super.applyMetadataSources( metadataSources );
		metadataSources.addAnnotatedClass( Person.class );
		metadataSources.addAnnotatedClass( Address.class );
		metadataSources.addAnnotatedClass( Role.class );
	}

	@Test
	public void testSqlTreeWalking1() {
		final String hqlQuery = "select p.name from Person p";
		final String cypherQuery = "match (p1:Person) return p1.name";

		assertConversion( hqlQuery, cypherQuery );
	}

	@Test
	public void testSqlTreeWalking2() {
		final String hqlQuery = "select p.name, p2.name from Person p, Person p2";
		final String cypherQuery = "match (p1:Person), (p2:Person) return p1.name, p2.name";

		assertConversion( hqlQuery, cypherQuery );
	}

	@Test
	public void testSqlTreeWalking3() {
		final String hqlQuery = "select p.name from Person p where p.age between 20 and 39";
		final String cypherQuery = "match (p1:Person) where p1.age>=20 and p1.age<=39 return p1.name";

		assertConversion( hqlQuery, cypherQuery );
	}

	@Test
	public void testSqlTreeWalking4() {
		final String hqlQuery = "select p.name from Person p where (p.age <= 20 and p.age >= 39)";
		final String cypherQuery = "match (p1:Person) where (p1.age>=20 and p1.age<=39) return p1.name";

		assertConversion( hqlQuery, cypherQuery );
	}

	@Test
	public void testSqlTreeWalking5() {
		final String hqlQuery = "select p.age from Person p where p.name like 'Steve%'";
		final String cypherQuery = "match (p1:Person) where p1.name=~'Steve%' return p1.age";

		assertConversion( hqlQuery, cypherQuery );
	}

	@Test
	public void testSqlTreeWalking6() {
		final String hqlQuery = "select p.age from Person p where p.name like 'Steve%' escape '/'";
		final String cypherQuery = "match (p1:Person) where (p1.age>=20 and p1.age<=39) return p1.name";

		assertConversion( hqlQuery, cypherQuery );
	}

	@Test
	public void testSqlTreeWalking7() {
		final String hqlQuery = "select p.age from Person p where p.name is null";
		final String cypherQuery = "match (p1:Person) where exists(p1.name) return p1.age";

		assertConversion( hqlQuery, cypherQuery );
	}

	@Test
	public void testSqlTreeWalking8() {
		final String hqlQuery =  "select p.age from Person p where p.name is not null";
		final String cypherQuery = "match (p1:Person) where not exists(p1.name) return p1.age";

		assertConversion( hqlQuery, cypherQuery );
	}

	@Test
	public void testSqlTreeWalking9() {
		final String hqlQuery = "select p.age from Person p where not p.name is not null";
		final String cypherQuery = "match (p1:Person) where exists(p1.name) return p1.age";

		assertConversion( hqlQuery, cypherQuery );
	}

	@Test
	public void testSqlTreeWalking10() {
		final String hqlQuery = "from Person p";
		final String cypherQuery = "match (p1:Person) return distinct p1";

		assertConversion( hqlQuery, cypherQuery );
	}

	@Test
	public void testSqlTreeWalking11() {
		final String hqlQuery = "select a from Person p join p.address a";
		final String cypherQuery = "match (p1:Person)--(a1:Address) return a1";

		assertConversion( hqlQuery, cypherQuery );
	}

	@Test
	public void testSqlTreeWalking12() {
		final String hqlQuery = "select r from Person p join p.roles r";
		final String cypherQuery = "match (p1:Person)-[:roles]-(r1:Role) return r1.id, r1.description";

		assertConversion( hqlQuery, cypherQuery );
	}

	@Test
	public void testSqlTreeWalking13() {
		final String hqlQuery = "from Person p where p.name = ?";
		final String cypherQuery = "match (p1:Person) where p1.name = :1 return distinct p1";

		assertConversion( hqlQuery, cypherQuery );
	}

	private void assertConversion(String hqlQuery, String cypherQuery) {
		final SqmSelectStatement statement = (SqmSelectStatement) interpret( hqlQuery );

		final SelectStatementInterpreter interpreter = new SelectStatementInterpreter( queryOptions(), callBack() );
		interpreter.interpret( statement );

		SelectQuery sqlTree = interpreter.getSelectQuery();

		CypherTreeWalker cypherTreeWalker = new CypherTreeWalker(
				getSessionFactory(),
				buildQueryParameterBindings( statement )
		);
		cypherTreeWalker.visitSelectQuery( sqlTree );

		assertThat( cypherTreeWalker.getCypher(), equalTo( cypherQuery ) );
	}

	private QueryParameterBindings buildQueryParameterBindings(SqmSelectStatement statement) {
		return QueryParameterBindingsImpl.from(
				buildParameterMetadata( statement ),
				new QueryParameterBindingTypeResolverImpl( getSessionFactory() )
		);
	}

	private static ParameterMetadataImpl buildParameterMetadata(SqmStatement sqm) {
		Map<String, QueryParameter> namedQueryParameters = null;
		Map<Integer, QueryParameter> positionalQueryParameters = null;

		for ( org.hibernate.sqm.query.Parameter parameter : sqm.getQueryParameters() ) {
			if ( parameter.getName() != null ) {
				if ( namedQueryParameters == null ) {
					namedQueryParameters = new HashMap<>();
				}
				namedQueryParameters.put(
						parameter.getName(),
						QueryParameterNamedImpl.fromSqm( parameter )
				);
			}
			else if ( parameter.getPosition() != null ) {
				if ( positionalQueryParameters == null ) {
					positionalQueryParameters = new HashMap<>();
				}
				positionalQueryParameters.put(
						parameter.getPosition(),
						QueryParameterPositionalImpl.fromSqm( parameter )
				);
			}
		}

		return new ParameterMetadataImpl( namedQueryParameters, positionalQueryParameters );
	}

	@Entity(name="Person")
	public static class Person {
		@Id
		Integer id;
		String name;
		int age;

		@ManyToOne
		Address address;

		@OneToMany
		@JoinColumn
		Set<Role> roles = new HashSet<Role>();
	}

	@Entity(name="Address")
	public static class Address {
		@Id
		Integer id;

		String street;
	}

	@Entity(name="Role")
	public static class Role {
		@Id
		Integer id;

		String description;
	}
}
