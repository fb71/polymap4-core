/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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
package org.polymap.core.runtime.cache;

import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.Timer;

/**
 * Cache backed by a {@link ConcurrentHashMap} with separate thread
 * for handling {@link ReferenceQueue}.
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
final class Soft2Cache<K,V>
        implements Cache<K,V> {

    private static Log log = LogFactory.getLog( Soft2Cache.class );
    
    private volatile static int         accessCounter = 0;
    
    private static AtomicInteger        cacheCounter = new AtomicInteger();
    
    private String                      name;
    
    private Soft2CacheManager           manager;

    private ConcurrentMap<K,CacheEntry<K,V>> entries;

    private CacheConfig                 config;
    

    Soft2Cache( Soft2CacheManager manager, String name, CacheConfig config ) {
        this.manager = manager;
        this.name = name != null ? name : String.valueOf( cacheCounter.getAndIncrement() );
        this.config = config;
        
        this.entries = new ConcurrentHashMap( config.initSize.get(), 0.75f, config.concurrencyLevel.get() );
    }


    @Override
    public String getName() {
        return name;
    }

    
    @Override
    public void dispose() {
        if (!isDisposed()) {
            clear();
            entries = null;
            manager.disposeCache( this );
        }
    }

    
    @Override
    public boolean isDisposed() {
        return entries == null;
    }
    
    
    protected V value( CacheEntry<K,V> entry ) {
        return entry != null ? entry.value() : null;
    }
    
    
    @Override
    public V get( K key ) throws CacheException {
        assert key != null : "Null keys are not allowed.";
        assert !isDisposed() : "Cache is disposed.";

        return value( entries.get( key ) );
    }


    /**
     * 
     */
    class Loader<E extends Exception>
            implements Function<K,CacheEntry<K,V>> {
        
        CacheLoader<K,V,E>      loader;
        
        protected Loader( CacheLoader<K,V,E> loader ) {
            this.loader = loader;
        }

        @Override
        public CacheEntry<K,V> apply( K key ) {
            try {
                V value = loader.load( key );
                int memSize = loader.size();
                EvictionListener l = loader instanceof EvictionAwareCacheLoader ? 
                        ((EvictionAwareCacheLoader)loader).evictionListener() : null;
                return new CacheEntry( Soft2Cache.this, key, value, memSize, l );
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Exception e) {
                // FIXME Handle checked Exception defined by CacheLoader
                throw new RuntimeException( e );
            }
        }
    }

    
    @Override
    public <E extends Exception> V get( K key, CacheLoader<K,V,E> loader ) throws E {
        assert key != null : "Null keys are not allowed.";
        assert !isDisposed() : "Cache is disposed.";
                
        V value = null;
        // do until entry with non-reclaimed value is found
        while (value == null) {
            CacheEntry<K,V> entry = entries.computeIfAbsent( key, new Loader( loader ) );
            value = value( entry );
        }
        return value;        
    }

    
    @Override
    public V putIfAbsent( K key, V value ) throws CacheException {
        return putIfAbsent( key, value, config.elementMemSize.get(), null );
    }
    
    
//    public V putIfAbsent( K key, V value, int memSize ) throws CacheException {
//        return putIfAbsent( key, value, memSize, null );
//    }
    
    
    protected V putIfAbsent( K key, V value, int memSize, EvictionListener l ) throws CacheException {
        assert key != null : "Null keys are not allowed.";
        assert !isDisposed() : "Cache is disposed.";
        
        memSize = memSize > 0 ? memSize : config.elementMemSize.get();

        CacheEntry<K,V> entry = new CacheEntry<K,V>( this, key, value, memSize, l );
        CacheEntry<K,V> previous = entries.putIfAbsent( key, entry );

        // previous entry reclaimed?
        if (previous != null) {
            value = previous.value();
            // not reclaimed -> return it
            if (value != null) {
                return value;
            }
            // if reclaimed -> insert new entry; return null                    
            else {
                entries.put( key, entry );
            }
        }
        return null;
    }
    
    
    @Override
    public V remove( K key ) throws CacheException {
        assert key != null : "Null keys are not allowed.";
        assert !isDisposed() : "Cache is disposed.";

        return value( entries.remove( key ) );
    }

    
    boolean removeEntry( K key, CacheEntry entry ) throws CacheException {
        assert key != null : "Null keys are not allowed.";
        assert !isDisposed() : "Cache is disposed.";

        return entries.remove( key, entry );
    }

    
    public int size() {
        assert !isDisposed() : "Cache is disposed.";
        return entries.size();
    }

    
    public void clear() {
        assert !isDisposed() : "Cache is disposed.";
        entries.clear();
    }

    
    public Iterable<V> values() {
        assert !isDisposed() : "Cache is disposed.";
        
        // the returned iterator should be aware of reclaimed entries
        // and needs to support remove()!
        return new Iterable<V>() {
            public Iterator<V> iterator() {
                return new Iterator<V>() {
                    
                    private Iterator<CacheEntry<K,V>>   it = entries.values().iterator();
                    
                    private CacheEntry<K,V>             next;
                    
                    @Override
                    public boolean hasNext() {
                        // find next, non-reclaimed entry
                        while ((next == null || next.value() == null) && it.hasNext()) {
                            next = it.next();
                        }
                        return next != null;
                    }
                    
                    @Override
                    public V next() {
                        assert next != null;
                        try { return next.value(); } finally { next = null; }
                    }
                    
                    @Override
                    public void remove() {
                        it.remove();
                    }
                };
            }
        };
    }

    
    @Override
    public Set<K> keySet() {
        assert !isDisposed() : "Cache is disposed.";
        return entries.keySet();
    }


    /**
     * 
     */
    static class CacheEntry<K,V>
            extends SoftReference<V> {

        private K                   key;
        
        private EvictionListener    evictionListener;
        
        private Soft2Cache          cache;
        
        
        CacheEntry( Soft2Cache cache, K key, V value, int elementSize, EvictionListener l ) {
            super( value, cache.manager.refQueue );
            assert value != null : "Null values are not allowed.";
            assert cache != null;
            assert elementSize <= 0 || (elementSize / 1024) <= 256*256;

            this.cache = cache;
            this.key = key;
            
            if (value instanceof EvictionAware) {
                evictionListener = ((EvictionAware)value).newListener();
            }
            if (l != null) {
                assert evictionListener == null;
                evictionListener = l;
            }
        }

        void fireEvictionEvent() {
            if (evictionListener != null) {
                evictionListener.onEviction( key );
            }
        }
        
        public Soft2Cache cache() {
            return cache;
        }
        
        public K key() {
            return key;
        }
        
        public V value() {
            accessCounter++;
            return get();
        }
    }


    /**
     * Performance test.
     */
    public static void main( String[] args ) {
        test( new Soft2CacheManager().newCache( CacheConfig.defaults() ) );
        test( new SoftCacheManager().newCache( CacheConfig.defaults() ) );
//        test( new GuavaCacheManager().newCache( CacheConfig.defaults() ) );
    }

    
    public static void test( Cache<Integer,byte[]> cache ) {
        System.out.println( "\n*** " + cache.getClass().getSimpleName() + " ****************************" );
        Timer timer = new Timer();
        int loops = 10000000;
        Random random = new Random( 0 );
        
        CacheLoader2<Integer,byte[]> loader = key -> new byte[1024];
        
        for (int i=0; i<loops; i++) {
            double gausian = Math.abs( random.nextGaussian() );
            Integer key = (int)( gausian * 40000 );
            assert cache.get( key, loader ) != null;
        }
        
        for (byte[] value : cache.values()) {
            assert value != null;
        }
        
        long time = timer.elapsedTime();
        Runtime rt = Runtime.getRuntime();
        System.out.println( "Mem: total:" + byteCountToDisplaySize( rt.totalMemory() ) + " / free: " + byteCountToDisplaySize( rt.freeMemory() ) );
        System.out.println( "Cache size: " + cache.size() + " -> " + (cache.size()/1000) + "MB" );
        System.out.println( "Loops: " + loops + " in " + time + "ms -> " + (1000f*loops/time) + "/s");
    }

}
