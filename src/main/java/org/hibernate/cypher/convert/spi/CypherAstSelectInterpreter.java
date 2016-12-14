/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.cypher.convert.spi;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.cypher.convert.internal.CypherSelectInterpretationImpl;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.proposed.spi.QueryParameterBindings;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.ast.from.ColumnBinding;
import org.hibernate.sql.convert.expression.spi.DomainReferenceRenderer;
import org.hibernate.sql.exec.results.spi.ResolvedReturn;
import org.hibernate.sql.spi.ParameterBinder;

/**
 * @author Davide D'Alto
 */
public class CypherAstSelectInterpreter implements DomainReferenceRenderer.RenderingContext {

	public static CypherSelectInterpretation interpret(
			SelectQuery selectQuery,
			boolean shallow,
			SessionFactoryImplementor sessionFactory,
			QueryParameterBindings parameterBindings) {
		final CypherAstSelectInterpreter walker = new CypherAstSelectInterpreter( sessionFactory, parameterBindings, shallow );
		walker.visitSelectQuery( selectQuery );
		return new CypherSelectInterpretationImpl(
				walker.cypherBuffer.toString(),
				walker.parameterBinders,
				walker.returns
		);
	}

	// In-flight state
	private final StringBuilder cypherBuffer = new StringBuilder();
	private final List<ParameterBinder> parameterBinders = new ArrayList<>();
	private final List<ResolvedReturn> returns = new ArrayList<>();

	private final SessionFactoryImplementor sessionFactory;
	private final QueryParameterBindings parameterBindings;
	private final boolean shallow;

	private CypherAstSelectInterpreter(SessionFactoryImplementor sessionFactory, QueryParameterBindings parameterBindings, boolean shallow) {
		this.sessionFactory = sessionFactory;
		this.parameterBindings = parameterBindings;
		this.shallow = shallow;
	}

	private void visitSelectQuery(SelectQuery selectQuery) {
	}

	@Override
	public SessionFactoryImplementor getSessionFactory() {
		return sessionFactory;
	}

	@Override
	public void renderColumnBinding(ColumnBinding binding) {
	}

	@Override
	public void renderColumnBindings(List<ColumnBinding> binding) {
	}

	@Override
	public void renderColumnBindings(ColumnBinding... binding) {
	}

}
