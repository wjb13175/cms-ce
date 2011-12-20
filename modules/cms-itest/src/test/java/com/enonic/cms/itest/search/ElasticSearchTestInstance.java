package com.enonic.cms.itest.search;

import java.util.logging.Logger;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.admin.indices.status.IndicesStatusRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

/**
 * Created by IntelliJ IDEA.
 * User: rmh
 * Date: 12/20/11
 * Time: 3:26 PM
 */
public class ElasticSearchTestInstance
{

    private final static Logger LOG = Logger.getLogger( ElasticSearchTestInstance.class.getName() );

    protected Node node;

    protected Client client;

    private static ElasticSearchTestInstance instance;

    private final static String GATEWAY_SETTING_KEY = "gateway.type";

    private final static String GATEWAY_NO_PERSISTENCE = "none";

    private ElasticSearchTestInstance()
    {
    }

    public static ElasticSearchTestInstance getInstance()
    {
        if ( instance == null )
        {
            instance = new ElasticSearchTestInstance();
            try
            {
                instance.start();
            }
            catch ( Exception e )
            {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        return instance;
    }


    public static void cleanUp()
    {
        if ( instance != null )
        {
            instance.node.stop();
            instance.node.close();
        }
    }

    private void start()
        throws Exception
    {
        final Settings settings =
            ImmutableSettings.settingsBuilder().put( "pretty", "true" ).put( GATEWAY_SETTING_KEY, GATEWAY_NO_PERSISTENCE ).build();

        node = NodeBuilder.nodeBuilder().client( false ).local( true ).data( true ).settings( settings ).build();
        node.start();

        // Let the node start goddamnit!
        Thread.sleep( 1000 );

        client = node.client();
    }

    public void initIndex( String indexName )
        throws Exception
    {
        try
        {
            getIndexStatus( indexName );
            deleteIndex( indexName );

            // Let it delete it properly
            Thread.sleep( 1000 );
        }
        catch ( Exception expectedExceptionItsOk )
        {
        }

        getInstance().createIndex( indexName );
    }

    private void getIndexStatus( String indexName )
        throws Exception
    {
        this.client.admin().indices().status( new IndicesStatusRequest( indexName ) ).actionGet();
    }

    public PutMappingResponse applyMapping( String indexName, String indexType, String mapping )
        throws Exception
    {
        PutMappingRequest mappingRequest = new PutMappingRequest( indexName ).type( indexType ).source( mapping );

        return this.client.admin().indices().putMapping( mappingRequest ).actionGet();
    }

    public CreateIndexResponse createIndex( String indexName )
        throws Exception
    {
        return this.client.admin().indices().create( new CreateIndexRequest( indexName ) ).actionGet();
    }

    public DeleteIndexResponse deleteIndex( String indexName )
        throws Exception
    {
        return this.client.admin().indices().delete( new DeleteIndexRequest( indexName ) ).actionGet();
    }

    private RefreshResponse refreshIndex( String indexName )
        throws Exception
    {
        return this.client.admin().indices().refresh( new RefreshRequest( indexName ) ).actionGet();
    }


}