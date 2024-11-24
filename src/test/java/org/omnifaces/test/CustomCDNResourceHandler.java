package org.omnifaces.test;

import static java.lang.Boolean.parseBoolean;
import static org.omnifaces.util.Faces.getRequestParameter;

import jakarta.faces.application.Resource;
import jakarta.faces.application.ResourceHandler;

import org.omnifaces.resourcehandler.CDNResource;
import org.omnifaces.resourcehandler.DefaultResourceHandler;

public class CustomCDNResourceHandler extends DefaultResourceHandler {

	public CustomCDNResourceHandler(ResourceHandler wrapped) {
		super(wrapped);
	}

	@Override
	public Resource decorateResource(Resource resource, String resourceName, String libraryName) {
	    var skipCDN = parseBoolean(getRequestParameter("skipCDN")); // Just for testing! In real world you'd not supply this as a request parameter.

		if (skipCDN || resource == null) {
			return resource;
		}

		return new CDNResource(resource, resource.getRequestPath()); // Just a dummy CDN resource for testing! In real world you'd prepend CDN host to the request path.
	}
}