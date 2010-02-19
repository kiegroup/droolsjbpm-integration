/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.drools.mc.io;

import java.net.MalformedURLException;
import java.net.URL;

import org.drools.io.impl.ResourceFactoryServiceImpl;
import org.drools.io.Resource;
import org.drools.core.util.StringUtils;

/**
 * JBoss VFS based ResourceProvider impl.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class VFSResourceProvider extends ResourceFactoryServiceImpl
{
   /**
    * Determine a cleaned URL for the given original URL.
    * @param originalUrl the original URL
    * @param originalPath the original URL path
    * @return the cleaned URL
    * @see org.springframework.util.StringUtils#cleanPath
    */
   private static URL getCleanedUrl(URL originalUrl,
                             String originalPath) {
       try {
           return new URL( StringUtils.cleanPath( originalPath ) );
       } catch ( MalformedURLException ex ) {
           // Cleaned URL path cannot be converted to URL
           // -> take original URL.
           return originalUrl;
       }
   }

   @Override
   public Resource newUrlResource(URL url)
   {
      return new VFSResource(url);
   }

   @Override
   public Resource newUrlResource(String path)
   {
      try
      {
         URL url = getCleanedUrl(new URL(path), path);
         return new VFSResource(url);
      }
      catch (MalformedURLException e)
      {
         throw new IllegalArgumentException(e);
      }
   }
}