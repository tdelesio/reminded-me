package com.homefellas.login.integration.restlet;

import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.integration.restlet.TicketResource;
import org.jasig.cas.ticket.TicketException;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.support.WebRequestDataBinder;
import org.springframework.web.context.request.WebRequest;

import com.homefellas.user.IUserService;
import com.homefellas.user.IUserServiceTX;
import com.homefellas.user.Member;
import com.homefellas.user.Profile;

public class JSONTicketResource extends Resource
{
	protected static final Logger log = LoggerFactory.getLogger(TicketResource.class);

    @Autowired
    private CentralAuthenticationService centralAuthenticationService;
    
    @Autowired
    private IUserServiceTX userService;
    
    public final boolean allowGet() {
        return false;
    }

    public final boolean allowPost() {
        return true;
    }

    public final void acceptRepresentation(final Representation entity)
        throws ResourceException {
        if (log.isDebugEnabled()) {
            log.debug("Obtaining credentials...");
            log.debug(getRequest().getEntityAsForm().toString());
        }
     
        final Credentials c = obtainCredentials();
        try {

            final String ticketGrantingTicketId = this.centralAuthenticationService.createTicketGrantingTicket(c);
            getResponse().setStatus(determineStatus());
            final Reference ticket_ref = getRequest().getResourceRef().addSegment(ticketGrantingTicketId);
            getResponse().setLocationRef(ticket_ref);
            String email = ((UsernamePasswordCredentials)c).getUsername();
            Profile profile = userService.getProfileByEmailTX(email); 
            if (profile == null) 
            {
            	getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, email+" not found in system");
            	return;
            }
            String jsonResponse = "{\"tgt\":\""+ticketGrantingTicketId+"\",\"id\":\""+profile.getId()+"\",\"email\":\""+email+"\",\"name\":\""+profile.getName()+"\"}";
            getResponse().setEntity(jsonResponse, MediaType.APPLICATION_JSON);
            this.getVariants().add(new Variant(MediaType.APPLICATION_JAVA_OBJECT));
//            getResponse().setEntity("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\"><html><head><title>" + getResponse().getStatus().getCode() + " " + getResponse().getStatus().getDescription() + "</title></head><body><h1>TGT Created</h1><form action=\"" + ticket_ref + "\" method=\"POST\">Service:<input type=\"text\" name=\"service\" value=\"\"><br><input type=\"submit\" value=\"Submit\"></form></body></html>", MediaType.TEXT_HTML);        
        } catch (final TicketException e) {
            log.error(e.getMessage(),e);
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
        }
    }
    
    /**
     * Template method for determining which status to return on a successful ticket creation. 
     * This method exists for compatibility reasons with bad clients (i.e. Flash) that can't
     * process 201 with a Location header.
     * 
     * @return the status to return.
     */
    protected Status determineStatus() {
        return Status.SUCCESS_CREATED;
    }
    
    protected Credentials obtainCredentials() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        final WebRequestDataBinder binder = new WebRequestDataBinder(c);
        final RestletWebRequest webRequest = new RestletWebRequest(getRequest());
        
        if (log.isDebugEnabled()) {
            log.debug(getRequest().getEntityAsForm().toString());
            log.debug("Username from RestletWebRequest: " + webRequest.getParameter("username"));
        }
        
        binder.bind(webRequest);
        
        return c;
    }
    
    protected class RestletWebRequest implements WebRequest {
        
        private final Form form;
        
        private final Request request;
        
        public RestletWebRequest(final Request request) {
            this.form = getRequest().getEntityAsForm();
            this.request = request;
        }

        public boolean checkNotModified(long lastModifiedTimestamp) {
            return false;
        }

        public String getContextPath() {
            return this.request.getResourceRef().getPath();
        }

        public String getDescription(boolean includeClientInfo) {
            return null;
        }

        public Locale getLocale() {
            return LocaleContextHolder.getLocale();
        }

        public String getParameter(String paramName) {
            return this.form.getFirstValue(paramName);
        }

        public Map<String, String[]> getParameterMap() {
            final Map<String, String[]> conversion = new HashMap<String,String[]>();

            for (final Map.Entry<String, String> entry : this.form.getValuesMap().entrySet()) {
                conversion.put(entry.getKey(), new String[] {entry.getValue()});
            }

            return conversion;
        }

        public String[] getParameterValues(String paramName) {
            return this.form.getValuesArray(paramName);
        }

        public String getRemoteUser() {
            return null;
        }

        public Principal getUserPrincipal() {
            return null;
        }

        public boolean isSecure() {
            return this.request.isConfidential();
        }

        public boolean isUserInRole(String role) {
            return false;
        }

        public Object getAttribute(String name, int scope) {
            return null;
        }

        public String[] getAttributeNames(int scope) {
            return null;
        }

        public String getSessionId() {
            return null;
        }

        public Object getSessionMutex() {
            return null;
        }

        public void registerDestructionCallback(String name, Runnable callback,
            int scope) {
            // nothing to do
        }

        public void removeAttribute(String name, int scope) {
            // nothing to do
        }

        public void setAttribute(String name, Object value, int scope) {
            // nothing to do
        }

        public String getHeader(final String s) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public String[] getHeaderValues(String s) {
            return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Iterator<String> getHeaderNames() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Iterator<String> getParameterNames() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Object resolveReference(String s) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
