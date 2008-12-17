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
package org.drools.test.mc.io.test;

import java.net.URL;
import java.util.Collection;

import org.drools.io.InternalResource;
import org.drools.io.Resource;
import org.drools.io.ResourceProvider;
import org.drools.mc.io.VFSResourceProvider;
import org.drools.test.mc.BaseTest;

/**
 * Resources test.
 * 
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ResourcesTest extends BaseTest
{
   private ResourceProvider provider;

   protected ResourceProvider getResourceProvider()
   {
      if (provider == null)
         provider = new VFSResourceProvider();

      return provider;
   }

   protected Resource testResource(String name)
   {
      URL url = getResource(name);
      ResourceProvider provider = getResourceProvider();
      Resource root = provider.newUrlResource(url);
      assertNotNull(root);
      return root;
   }

   public void testResource() throws Exception
   {
      Resource resource = testResource("/mc/io/root");
      assertNotNull(resource.getInputStream());
      assertNotNull(resource.getReader());
   }

   public void testInternalResource() throws Exception
   {
      Resource root = testResource("/mc/io/root");
      assertTrue(root instanceof InternalResource);
      InternalResource resource = InternalResource.class.cast(root);

      assertTrue(resource.isDirectory());
      assertTrue(resource.hasURL());
      assertNotNull(resource.getURL());

      assertEquals(-1l, resource.getLastRead());
      assertNotNull(resource.getInputStream());
      assertFalse(-1l == resource.getLastRead());

      Collection<Resource> resources = resource.listResources();
      assertNotNull(resources);
      assertEquals(2, resources.size());
      for (Resource child : resources)
      {
         assertNotNull(child.getInputStream());
      }
   }
}
