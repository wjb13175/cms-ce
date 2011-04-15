package com.enonic.cms.core.plugin.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.enonic.cms.api.util.LogFacade;

final class PluginConfigHelper
{
    private final static String DEFAULT_CONFIG = "META-INF/cms/default.properties";

    private final static LogFacade LOG = LogFacade.get( PluginConfigHelper.class );

    public static Map<String, String> loadDefaultProperties( final Bundle bundle )
    {
        return loadProperties( bundle.getEntry( DEFAULT_CONFIG ) );
    }

    private static Map<String, String> loadProperties( final URL url )
    {
        if ( url == null )
        {
            return Collections.emptyMap();
        }

        try
        {
            final InputStream in = url.openStream();

            try
            {
                return loadProperties( in );
            }
            finally
            {
                in.close();
            }
        }
        catch ( Exception e )
        {
            LOG.warning( e, "Error occurred loading properties from [{0}]", url.toExternalForm() );
        }

        return Collections.emptyMap();
    }

    public static Map<String, String> loadProperties( final File file )
    {
        if ( !file.exists() || !file.isFile() )
        {
            return Collections.emptyMap();
        }

        try
        {
            final InputStream in = new FileInputStream( file );

            try
            {
                return loadProperties( in );
            }
            finally
            {
                in.close();
            }
        }
        catch ( Exception e )
        {
            LOG.warning( e, "Error occurred loading properties from [{0}]", file.getAbsolutePath() );
        }

        return Collections.emptyMap();
    }

    private static Map<String, String> loadProperties( final InputStream in )
        throws IOException
    {
        final Properties props = new Properties();
        props.load( in );
        return toMap( props );
    }

    private static Map<String, String> toMap( final Properties props )
    {
        final HashMap<String, String> map = new HashMap<String, String>();
        for ( Object o : props.keySet() )
        {
            map.put( o.toString(), props.getProperty( o.toString() ) );
        }

        return map;
    }

    public static Map<String, String> interpolate( final BundleContext context, final Map<String, String> source )
    {
        final StrLookup lookup = new StrLookup()
        {
            @Override
            public String lookup( final String key )
            {
                String value = source.get( key );
                if ( value != null )
                {
                    return value;
                }

                return context.getProperty( key );
            }
        };

        final Map<String, String> target = new HashMap<String, String>();
        final StrSubstitutor substitutor = new StrSubstitutor( lookup );

        for ( String key : source.keySet() )
        {
            String value = source.get( key );

            try
            {
                value = substitutor.replace( value );
            }
            catch ( IllegalStateException e )
            {
                // Do nothing
            }

            target.put( key, value );
        }

        return target;
    }
}
