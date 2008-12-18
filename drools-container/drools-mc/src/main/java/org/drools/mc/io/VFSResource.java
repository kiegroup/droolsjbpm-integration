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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

import org.drools.io.Resource;
import org.drools.io.impl.BaseResource;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;

/**
 * JBoss VFS based Resource impl.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class VFSResource extends BaseResource
{
   private URL url;
   private VirtualFile file;
   private long lastRead = -1;

   public VFSResource(URL url)
   {
      if (url == null)
         throw new IllegalArgumentException("Null url");
      this.url = url;
   }

   public VFSResource(VirtualFile file)
   {
      if (file == null)
         throw new IllegalArgumentException("Null file");
      this.file = file;
   }

   /**
    * Get file.
    *
    * @return virtual file instance
    */
   protected VirtualFile getFile()
   {
      if (file == null)
      {
         try
         {
            file = VFS.getCachedFile(url);
         }
         catch (IOException e)
         {
            throw new RuntimeException(e);
         }
      }
      return file;
   }

   public URL getURL() throws IOException
   {
      try
      {
         return (url != null) ? url : getFile().toURL();
      }
      catch (URISyntaxException e)
      {
         IOException ioe = new IOException();
         ioe.initCause(e);
         throw ioe;
      }
   }

   public boolean hasURL()
   {
      return true;
   }

   public boolean isDirectory()
   {
      try
      {
         // TODO - do you need to know if it's directory or if it has children, e.g. jar resource
         return getFile().isLeaf() == false;
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   public Collection<Resource> listResources()
   {
      try
      {
         List<VirtualFile> children = getFile().getChildren();
         if (children == null || children.isEmpty())
         {
            return Collections.emptyList();
         }
         else
         {
            List<Resource> resources = new ArrayList<Resource>(children.size());
            for (VirtualFile child : children)
            {
               resources.add(new VFSResource(child));
            }
            return resources;
         }
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   public long getLastModified()
   {
      try
      {
         return getFile().getLastModified();
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   public long getLastRead()
   {
      return lastRead;
   }

   public InputStream getInputStream() throws IOException
   {
      lastRead = System.currentTimeMillis();
      return getFile().openStream();
   }

   public Reader getReader() throws IOException
   {
      return new InputStreamReader(getInputStream());
   }

   @Override
   public int hashCode()
   {
      return toURL().hashCode();
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof VFSResource == false)
         return false;

      VFSResource other = VFSResource.class.cast(obj);
      return toURL().equals(other.toURL());
   }

   @Override
   public String toString()
   {
      return "[VFSResource = " + toURL() + "]";
   }

   /**
    * To url.
    *
    * @return the url
    * @throws RuntimeException for any error
    */
   protected URL toURL()
   {
      try
      {
         return getURL();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
}
                                         