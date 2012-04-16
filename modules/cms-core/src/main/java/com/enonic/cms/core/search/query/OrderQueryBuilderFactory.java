package com.enonic.cms.core.search.query;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import com.enonic.cms.core.content.index.queryexpression.OrderByExpr;
import com.enonic.cms.core.content.index.queryexpression.OrderFieldExpr;

public class OrderQueryBuilderFactory
    extends BaseQueryBuilderFactory
{

    private final static boolean createDefaultSortExpression = false;


    public void buildOrderByExpr( SearchSourceBuilder builder, OrderByExpr expr )
    {
        List<SortBuilder> sorts;

        if ( expr != null )
        {
            sorts = buildOrderFieldExpr( expr.getFields() );

            for ( SortBuilder sort : sorts )
            {
                builder.sort( sort );
            }
        }
        else if ( createDefaultSortExpression )
        {
            sorts = new ArrayList<SortBuilder>();
            sorts.add( getDefaultSorting() );
        }

    }

    private ScoreSortBuilder getDefaultSorting()
    {
        return SortBuilders.scoreSort();
    }

    private List<SortBuilder> buildOrderFieldExpr( OrderFieldExpr[] expr )
    {

        List<SortBuilder> sort = new ArrayList<SortBuilder>();

        for ( int i = 0; i < expr.length; i++ )
        {
            sort.add( buildOrderFieldExpr( expr[i] ) );
        }

        return sort;
    }

    private SortBuilder buildOrderFieldExpr( OrderFieldExpr expr )
    {
        final String name = QueryFieldNameResolver.resolveQueryFieldName( expr.getField() );

        SortOrder order = SortOrder.DESC;

        if ( expr.isAscending() )
        {
            order = SortOrder.ASC;
        }

        return new FieldSortBuilder( name ).order( order ).ignoreUnmapped( true );
    }

}