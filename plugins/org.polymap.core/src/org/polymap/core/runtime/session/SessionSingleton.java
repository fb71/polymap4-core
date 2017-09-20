/* 
 * polymap.org
 * Copyright 2011-2017, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.runtime.session;

/**
 * Subclasses of <code>SessionSingleton</code> provide access to a unique instance
 * within the scope of a user session. In the context of one user session
 * {@link #instance(Class)} will always return the same object, but for different
 * user sessions the returned instances will be different.
 * <p/>
 * Usage:
 * 
 * <pre>
 * public class FooSingleton
 *         extends SessionSingleton {
 * 
 *     public static FooSingleton instance() {
 *         return instance( FooSingleton.class );
 *     }
 * }
 * </pre>
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class SessionSingleton {

    public static final <T> T instance( Class<T> type ) {
        SessionContext current = SessionContext.current();
        if (current == null) {
            throw new IllegalStateException( "NO session context maps to the current thread: " + Thread.currentThread() );
        }
        return current.sessionSingleton( type );
    }
    
}
