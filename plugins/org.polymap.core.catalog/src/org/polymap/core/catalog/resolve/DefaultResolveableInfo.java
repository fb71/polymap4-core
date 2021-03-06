/* 
 * polymap.org
 * Copyright (C) 2015, Falko Br�utigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.catalog.resolve;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public abstract class DefaultResolveableInfo
        implements IResolvableInfo {

    private static Log log = LogFactory.getLog( DefaultResolveableInfo.class );
    
    private IServiceInfo        serviceInfo;
    

    public DefaultResolveableInfo( IServiceInfo serviceInfo ) {
        this.serviceInfo = serviceInfo;
    }


    @Override
    public IServiceInfo getServiceInfo() {
        return serviceInfo;
    }

}
