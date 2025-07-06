package org.keycloak.examples.authenticator;

import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.CredentialValidator;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ClientModel;
import org.keycloak.models.TokenManager;
import org.keycloak.representations.AccessToken;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import org.jboss.logging.Logger;

public class LagoonAuthenticator implements Authenticator {

    private static final Logger LOG = Logger.getLogger(LagoonAuthenticator.class);

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        System.out.println("LagoonAuthenticator called");
        LOG.info("LagoonAuthenticator triggered");

        if (context.getUser() == null) {
            System.out.println("No user found in context");
            LOG.info("LagoonAuthenticator triggered");
            context.failure(AuthenticationFlowError.UNKNOWN_USER);
            return;
        }

        UserModel user = context.getUser();
        LOG.info("Current user is " + user.getEmail());
        String token = getAccessToken(context);
        LOG.info("User's token was: " + token);

        String redirectUri = context.getAuthenticationSession().getRedirectUri();
        URI uri = URI.create(redirectUri);
        String host = uri.getHost();
        LOG.infof("User was trying to access %s", host);
        try {
            JsonNode env = LagoonGraphQlClient.getEnvironmentByRoute(host);
            if (env == null || env.isNull()) {
                LOG.info("GQL returned nothing");
            } else {
                String envId = env.get("id").asText();
                String envName = env.get("name").asText();
                LOG.infof("GQL returned %s and %s", envId, envName);
            }
        } catch (Exception e) {
            LOG.info("Error making GraphQL query");

        }

        if (!user.getEmail().equals("platformowner@example.com")) {
            LOG.info("The current user is not permitted to access this resource");
            context.failure(AuthenticationFlowError.ACCESS_DENIED);
        } else {
            context.success();
        };
    }

    private String getAccessToken (AuthenticationFlowContext context) {
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYWRtaW4iLCJpc3MiOiJsb2NhbGFkbWluIiwiYXVkIjoiYXBpLmRldiIsInN1YiI6ImxvY2FsYWRtaW4ifQ.3teqNDfchVMqMkDfWwbsmMBoeexTI08feKyAMH4AOrs";
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        context.success();
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(org.keycloak.models.KeycloakSession session, org.keycloak.models.RealmModel realm, org.keycloak.models.UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(org.keycloak.models.KeycloakSession session, org.keycloak.models.RealmModel realm, org.keycloak.models.UserModel user) {}

    @Override
    public void close() {}

}
