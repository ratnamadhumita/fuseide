/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.fusesource.ide.camel.model.service.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.fusesource.ide.camel.model.service.core.internal.CamelModelServiceCoreActivator;
import org.fusesource.ide.foundation.core.util.BundleUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * A utility class for retrieving a IJBoss7ManagerService or 
 * the id of a IJBoss7ManagerService for a given IServer or IRuntimeType id 
 */
public class CamelServiceManagerUtil {

	
	/**
	 * Retrieve the ICamelManagerService with the given service id. 
	 * 
	 * @param serviceId
	 * @return
	 * @throws CamelManagerException
	 */
	public static ICamelManagerService getManagerService(String serviceVersion) throws CamelManagerException {
		try {
			BundleContext context = CamelModelServiceCoreActivator.getBundleContext();
			CamelManagerServiceProxy proxy = new CamelManagerServiceProxy(context, serviceVersion);
			proxy.open();
			return proxy;
		} catch(InvalidSyntaxException ise) {
			throw new CamelManagerException(ise);
		}
	}
	
	public static String[] getAvailableVersions() throws CamelManagerException {
		try {
			BundleContext context = CamelModelServiceCoreActivator.getBundleContext();
			Collection<ServiceReference<ICamelManagerService>> refs = BundleUtils.findServiceReferences(context, ICamelManagerService.class);
			ArrayList<String> ret = new ArrayList<String>();
			Iterator<ServiceReference<ICamelManagerService>> it = refs.iterator();
			while(it.hasNext()) {
				ServiceReference<ICamelManagerService> next = it.next();
				String vers = (String)next.getProperty("camel.version");
				if( !ret.contains(vers))
					ret.add(vers);
			}
			return (String[]) ret.toArray(new String[ret.size()]);
		} catch(InvalidSyntaxException ise) {
			throw new CamelManagerException(ise);
		}
	}
	
	/**
	 * Execute some command, request, or action on the appropriate 
	 * IJBoss7ManagerService for the given IServer. 
	 * 
	 * @param serviceAware	The action to be executed
	 * @param server   
	 * @return				
	 * @throws Exception
	 */
	public static <RESULT> RESULT executeWithService(IServiceAware<RESULT> serviceAware, String version) throws Exception {
		ICamelManagerService service = null;
		try {
			service = CamelServiceManagerUtil.getManagerService(version);
			return serviceAware.execute(service);
		} finally {
			// TODO
		}
	}
	
	/**
	 * An interface for an object capable of executing a command,
	 * request, or action on a IJBoss7ManagerService. 
	 * 
	 * @param <RESULT> The type of the result you are expecting from the service
	 */
	public static interface IServiceAware<RESULT> {
		public RESULT execute(ICamelManagerService service) throws Exception;
	}
	
}
