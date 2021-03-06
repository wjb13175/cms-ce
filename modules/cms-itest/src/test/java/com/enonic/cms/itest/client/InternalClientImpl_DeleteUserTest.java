/*
 * Copyright 2000-2013 Enonic AS
 * http://www.enonic.com/license
 */

package com.enonic.cms.itest.client;


import java.io.IOException;

import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;

import com.enonic.cms.api.client.model.CreateUserParams;
import com.enonic.cms.api.client.model.DeleteUserParams;
import com.enonic.cms.core.client.InternalClient;
import com.enonic.cms.core.security.PortalSecurityHolder;
import com.enonic.cms.core.security.user.UserEntity;
import com.enonic.cms.core.security.user.UserKey;
import com.enonic.cms.core.servlet.ServletRequestAccessor;
import com.enonic.cms.itest.AbstractSpringTest;
import com.enonic.cms.itest.util.DomainFixture;

import static org.junit.Assert.*;

public class InternalClientImpl_DeleteUserTest
    extends AbstractSpringTest
{
    @Autowired
    @Qualifier("localClient")
    private InternalClient internalClient;

    @Autowired
    private DomainFixture fixture;


    @Before
    public void before()
        throws IOException, JDOMException
    {
        fixture.initSystemData();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr( "127.0.0.1" );
        ServletRequestAccessor.setRequest( request );
    }

    @Test
    public void delete_user_in_local_userstore_by_userstorename_and_username_deletes_the_user()
        throws Exception
    {
        // setup:
        clientLogin( "admin" );
        createUser( "testuserstore", "myuser", "mypassword", "myemail@test.com", "My User" );
        fixture.flushAndClearHibernateSession();

        // verify setup:
        assertEquals( false, fixture.findUserByName( "myuser" ).isDeleted() );

        DeleteUserParams params = new DeleteUserParams();
        params.user = "testuserstore:myuser";
        internalClient.deleteUser( params );

        fixture.flushAndClearHibernateSession();

        // verify
        assertEquals( true, fixture.findUserByName( "myuser" ).isDeleted() );
    }

    @Test
    public void delete_user_in_local_userstore_by_userkey_deletes_the_user()
        throws Exception
    {
        // setup:
        clientLogin( "admin" );
        UserKey userKey = createUser( "testuserstore", "myuser", "mypassword", "myemail@test.com", "My User" );
        fixture.flushAndClearHibernateSession();

        // verify setup:
        assertEquals( false, fixture.findUserByName( "myuser" ).isDeleted() );

        DeleteUserParams params = new DeleteUserParams();
        params.user = "#" + userKey;
        internalClient.deleteUser( params );

        fixture.flushAndClearHibernateSession();

        // verify
        assertEquals( true, fixture.findUserByName( "myuser" ).isDeleted() );
    }

    private UserKey createUser( String userstoreName, String username, String password, String email, String displayName )
    {
        CreateUserParams params = new CreateUserParams();
        params.userstore = userstoreName;
        params.username = username;
        params.password = password;
        params.email = email;
        params.displayName = displayName;
        return new UserKey( internalClient.createUser( params ) );
    }

    private void clientLogin( String username )
    {
        UserEntity user = fixture.findUserByName( username );
        PortalSecurityHolder.setLoggedInUser( user.getKey() );
        PortalSecurityHolder.setImpersonatedUser( user.getKey() );
    }
}
