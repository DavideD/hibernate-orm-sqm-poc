/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.expression;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.query.proposed.spi.QueryParameterBinding;
import org.hibernate.sql.convert.spi.ParameterSpec;
import org.hibernate.sql.spi.ParameterBinder;
import org.hibernate.type.Type;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractParameter extends SelfReadingExpressionSupport implements ParameterSpec, ParameterBinder {
	private static final Logger log = Logger.getLogger( AbstractParameter.class );

	private final Type inferredType;

	public AbstractParameter(Type inferredType) {
		this.inferredType = inferredType;
	}

	public Type getInferredType() {
		return inferredType;
	}

	@Override
	public Type getType() {
		return getInferredType();
	}

	@Override
	public ParameterBinder getParameterBinder() {
		return this;
	}

	protected int bindParameterValue(
			PreparedStatement statement,
			int startPosition,
			QueryParameterBinding valueBinding,
			SharedSessionContractImplementor session) throws SQLException {
		final Type bindType;
		final Object bindValue;

		if ( valueBinding == null ) {
			warnNoBinding();
			bindType = valueBinding.getBindType();
			bindValue = null;
		}
		else {
			if ( valueBinding.getBindType() == null ) {
				bindType = inferredType;
			}
			else {
				bindType = valueBinding.getBindType();
			}
			bindValue = valueBinding.getBindValue();
		}

		if ( bindType == null ) {
			unresolvedType();
		}
		assert bindType != null;
		if ( bindValue == null ) {
			warnNullBindValue();
		}

		bindType.nullSafeSet( statement, bindValue, startPosition, session );
		return bindType.getColumnSpan( session.getFactory() );
	}

	protected abstract void warnNoBinding();

	protected abstract void unresolvedType();

	protected abstract void warnNullBindValue();
}
