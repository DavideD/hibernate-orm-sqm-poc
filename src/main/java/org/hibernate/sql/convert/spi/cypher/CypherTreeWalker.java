/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.convert.spi.cypher;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.hibernate.QueryException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.proposed.spi.QueryParameterBindings;
import org.hibernate.sql.ast.QuerySpec;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.ast.expression.AttributeReference;
import org.hibernate.sql.ast.expression.EntityReference;
import org.hibernate.sql.ast.expression.QueryLiteral;
import org.hibernate.sql.ast.from.ColumnBinding;
import org.hibernate.sql.ast.from.FromClause;
import org.hibernate.sql.ast.from.TableBinding;
import org.hibernate.sql.ast.from.TableGroup;
import org.hibernate.sql.ast.from.TableJoin;
import org.hibernate.sql.ast.from.TableSpace;
import org.hibernate.sql.ast.predicate.BetweenPredicate;
import org.hibernate.sql.ast.predicate.LikePredicate;
import org.hibernate.sql.ast.predicate.NullnessPredicate;
import org.hibernate.sql.ast.predicate.RelationalPredicate;
import org.hibernate.sql.ast.select.SelectClause;
import org.hibernate.sql.ast.select.Selection;
import org.hibernate.sql.convert.spi.Return;
import org.hibernate.sql.convert.spi.SqlTreeWalker;
import org.hibernate.sql.exec.results.spi.ReturnReader;
import org.hibernate.sql.spi.ParameterBinder;
import org.hibernate.type.LiteralType;

/**
 * @author Davide D'Alto
 */
public class CypherTreeWalker extends SqlTreeWalker {

	private final SessionFactoryImplementor sessionFactory;
	private final QueryParameterBindings buildQueryParameterBindings;
	private SelectionProcessor currentSelectionProcessor;
	private boolean currentlyInSelections;
	private boolean currentlyInPredicate;
	final boolean shallow = false;
	private final StringBuilder matchBuilder = new StringBuilder();
	private final StringBuilder returnBuilder = new StringBuilder(); 
	private final List<ParameterBinder> parameterBinders = new ArrayList<>();

	private final List<Return> returns = new ArrayList<Return>();

	public CypherTreeWalker(SessionFactoryImplementor sessionFactory, QueryParameterBindings buildQueryParameterBindings) {
		super( sessionFactory, buildQueryParameterBindings );
		this.sessionFactory = sessionFactory;
		this.buildQueryParameterBindings = buildQueryParameterBindings;
	}

	public String getCypher() {
		StringBuilder cypher = new StringBuilder();
		cypher.append( matchBuilder );
		cypher.append( " " );
		cypher.append( returnBuilder );
		return cypher.toString();
	}

	public void visitSelectQuery(SelectQuery selectQuery) {
		visitQuerySpec( selectQuery.getQuerySpec() );
	}

	public void visitQuerySpec(QuerySpec querySpec) {
		visitFromClause( querySpec.getFromClause() );

		if ( querySpec.getWhereClauseRestrictions() != null && !querySpec.getWhereClauseRestrictions().isEmpty() ) {
			returnBuilder.append( "where " );

			boolean wasPreviouslyInPredicate = currentlyInPredicate;
			currentlyInPredicate = true;
			try {
				querySpec.getWhereClauseRestrictions().accept( this );
			}
			finally {
				currentlyInPredicate = wasPreviouslyInPredicate;
			}
			returnBuilder.append( " " );
		}
		visitSelectClause( querySpec.getSelectClause() );
	}

	public void visitSelectClause(SelectClause selectClause) {
		currentSelectionProcessor = new SelectionProcessor( currentSelectionProcessor );

		try {
			boolean previouslyInSelections = currentlyInSelections;
			currentlyInSelections = true;

			try {
				returnBuilder.append( "return " );
				if ( selectClause.isDistinct() ) {
					returnBuilder.append( "distinct " );
				}

				String separator = "";
				for ( Selection selection : selectClause.getSelections() ) {
					appendReturn( separator );
					visitSelection( selection );
					separator = ", ";
				}
			}
			finally {
				currentlyInSelections = previouslyInSelections;
			}
		}
		finally {
			currentSelectionProcessor = currentSelectionProcessor.parentSelectionProcessor;
		}
	}

	@Override
	public void visitQueryLiteral(QueryLiteral queryLiteral) {
		String literal = literal( queryLiteral );
		if ( !currentlyInSelections ) {
			// handle literals via parameter binding if they occur outside the select
//			parameterBinders.add( queryLiteral );

//			final int columnCount = queryLiteral.getType().getColumnSpan( sessionFactory );
//			final boolean needsParens = currentlyInPredicate && columnCount > 1;

//			if ( needsParens ) {
//				returnBuilder.append( "(" );
//			}

//			String separator = "";
//			for ( int i = 0; i < columnCount; i++ ) {
//				returnBuilder.append( separator );
//				returnBuilder.append(  "?" );
				returnBuilder.append( literal );
//				separator = ", ";
//			}

//			if ( needsParens ) {
//				returnBuilder.append( ")" );
//			}
		}
		else {
			returnBuilder.append( literal );
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
	private String literal(QueryLiteral queryLiteral) {
		// otherwise, render them as literals
		// todo : better scheme for rendering these as literals
		try {
			LiteralType type = (LiteralType) queryLiteral.getType();
			if ( type == null ) {
				return String.valueOf( queryLiteral.getValue() ); 
			}
			Object value = queryLiteral.getValue();
			String literal = type.objectToSQLString( value, sessionFactory.getDialect() );
			return literal;
		}
		catch (Exception e) {
			throw new QueryException(
					String.format(
							Locale.ROOT,
							"Could not render literal value [%s (%s)] into SQL",
							queryLiteral.getValue(),
							queryLiteral.getType().getName()
					),
					e
			);
		}
	}

	public void visitFromClause(FromClause fromClause) {
		matchBuilder.append( "match " );

		String separator = "";
		for ( TableSpace tableSpace : fromClause.getTableSpaces() ) {
			matchBuilder.append( separator );
			visitTableSpace( tableSpace );
			separator = ", ";
		}
	}

	public void visitTableSpace(TableSpace tableSpace) {
		visitTableGroup( tableSpace.getRootTableGroup() );
//
//		for ( TableGroupJoin tableGroupJoin : tableSpace.getJoinedTableGroups() ) {
//			appendSql( tableGroupJoin.getJoinType().getText() );
//			appendSql( " join " );
//			visitTableGroup( tableGroupJoin.getJoinedGroup() );
//
//			boolean wasPreviouslyInPredicate = currentlyInPredicate;
//			currentlyInPredicate = true;
//			try {
//				if ( tableGroupJoin.getPredicate() != null && !tableGroupJoin.getPredicate().isEmpty() ) {
//					appendSql( " on " );
//					tableGroupJoin.getPredicate().accept( this );
//				}
//			}
//			finally {
//				currentlyInPredicate = wasPreviouslyInPredicate;
//			}
//		}
	}

	public void visitLikePredicate(LikePredicate likePredicate) {
		if ( likePredicate.isNegated() ) {
			returnBuilder.append( "not (has " );
			likePredicate.getMatchExpression().accept( this );
			returnBuilder.append( ") or (not (" );
			likePredicate.getMatchExpression().accept( this );
			returnBuilder.append( "=~'" );
			likePredicate.getPattern().accept( this );
			returnBuilder.append( "'))" );
		}
		else {
			likePredicate.getMatchExpression().accept( this );
			returnBuilder.append( "=~'" );
			likePredicate.getPattern().accept( this );
			returnBuilder.append( "'" );
		}
	}

	public void visitNullnessPredicate(NullnessPredicate nullnessPredicate) {
		if ( nullnessPredicate.isNegated() ) {
			returnBuilder.append( "not exists(" );
		}
		else {
			returnBuilder.append( "exists(" );
		}
		nullnessPredicate.getExpression().accept( this );
		returnBuilder.append( ")" );
	}

	@Override
	public void visitBetweenPredicate(BetweenPredicate betweenPredicate) {
		betweenPredicate.getExpression().accept( this );
		if ( betweenPredicate.isNegated() ) {
			returnBuilder.append( "<" );
			betweenPredicate.getLowerBound().accept( this );
			returnBuilder.append( " or " );
			betweenPredicate.getExpression().accept( this );
			returnBuilder.append( ">");
			betweenPredicate.getUpperBound().accept( this );
		}
		else {
			returnBuilder.append( ">=" );
			betweenPredicate.getLowerBound().accept( this );
			returnBuilder.append( " and " );
			betweenPredicate.getExpression().accept( this );
			returnBuilder.append( "<=");
			betweenPredicate.getUpperBound().accept( this );
		}
	}

	public void visitTableGroup(TableGroup tableGroup) {
		visitTableBinding( tableGroup.getRootTableBinding() );

		for ( TableJoin tableJoin : tableGroup.getTableJoins() ) {
			matchBuilder.append( "--" );
			visitTableBinding( tableJoin.getJoinedTableBinding() );
		}
	}

	public void visitTableBinding(TableBinding tableBinding) {
		matchBuilder.append( "(" );
		matchBuilder.append( tableBinding.getIdentificationVariable() );
		matchBuilder.append( ":" );
		matchBuilder.append( tableBinding.getTable().getTableExpression() );
		matchBuilder.append( ")" );
	}

	private void appendReturn(String string) {
		returnBuilder.append( string );
	}

	private class SelectionProcessor {
		private final SelectionProcessor parentSelectionProcessor;
		private int numberOfColumnsConsumedSoFar = 0;

		private SelectionProcessor(SelectionProcessor parentSelectionProcessor) {
			this.parentSelectionProcessor = parentSelectionProcessor;
		}

		private void processSelection(Selection selection) {
			if ( parentSelectionProcessor != null ) {
				return;
			}

			// otherwise build a Return
			// 		(atm only simple selection expressions are supported)
			final ReturnReader reader = selection.getSelectExpression().getReturnReader( numberOfColumnsConsumedSoFar+1, shallow, sessionFactory );
			returns.add(
					new Return( selection.getResultVariable(), reader )
			);
			numberOfColumnsConsumedSoFar += reader.getNumberOfColumnsRead( sessionFactory );
		}
	}

	public void visitAttributeReference(AttributeReference attributeReference) {
		renderColumnBindings( attributeReference.getColumnBindings() );
	}

	private void visitColumnBinding(ColumnBinding columnBinding) {
		String column = columnBinding.getColumn().render( columnBinding.getIdentificationVariable() );
		returnBuilder.append( column );
	}

	private void renderEntity(EntityReference entityExpression) {
		returnBuilder.append( entityExpression.getColumnBindings()[0].getIdentificationVariable() );
	}

	private void renderColumnBindings(ColumnBinding[] columnBindings) {
		final boolean needsParens = columnBindings.length > 1 && currentlyInPredicate;
		if ( needsParens ) {
			returnBuilder.append( "(" );
		}

		String separator = "";
		for ( ColumnBinding columnBinding : columnBindings ) {
			returnBuilder.append( separator );
			visitColumnBinding( columnBinding );
			separator = ", ";
		}

		if ( needsParens ) {
			returnBuilder.append( ")" );
		}
	}

	public void visitSelection(Selection selection) {
		currentSelectionProcessor.processSelection( selection );
		selection.getSelectExpression().accept( this );
	}

	public void visitRelationalPredicate(RelationalPredicate relationalPredicate) {
		relationalPredicate.getLeftHandExpression().accept( this );
		returnBuilder.append( relationalPredicate.getOperator().sqlText() );
		relationalPredicate.getRightHandExpression().accept( this );
	}

	public void visitEntityExpression(EntityReference entityExpression) {
		if ( entityExpression.getType().isEntityType() ) {
			renderEntity( entityExpression );
		}
		else {
			renderColumnBindings( entityExpression.getColumnBindings() );
		}
	}
}
