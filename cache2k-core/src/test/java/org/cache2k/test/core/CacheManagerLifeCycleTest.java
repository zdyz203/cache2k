package org.cache2k.test.core;

/*
 * #%L
 * cache2k core
 * %%
 * Copyright (C) 2000 - 2018 headissue GmbH, Munich
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.CacheManager;
import org.cache2k.testing.category.FastTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.URL;
import java.net.URLClassLoader;

import static org.junit.Assert.*;

/**
 * Test creation and destruction of cache managers.
 *
 * @author Jens Wilke
 */
@Category(FastTests.class)
public class CacheManagerLifeCycleTest {

  @Test(expected = IllegalStateException.class)
  public void setDefaultManagerName_Exception() {
    CacheManager.getInstance();
    CacheManager.setDefaultName("hello");
  }

  @Test
  public void openClose() {
    String _uniqueName = this.getClass().getName() + ".openClose";
    CacheManager cm = CacheManager.getInstance(_uniqueName);
    cm.close();
    CacheManager cm2 = CacheManager.getInstance(_uniqueName);
    assertNotSame(cm, cm2);
    cm2.close();
  }

  @Test
  public void differentClassLoaderDifferentManager() {
    ClassLoader cl1 = new URLClassLoader(new URL[0], this.getClass().getClassLoader());
    ClassLoader cl2 = new URLClassLoader(new URL[0], this.getClass().getClassLoader());
    CacheManager cm1 = CacheManager.getInstance(cl1);
    CacheManager cm2 = CacheManager.getInstance(cl2);
    assertNotSame(cm1, cm2);
    assertFalse(cm1.isClosed());
    assertFalse(cm2.isClosed());
    CacheManager.closeAll(cl1);
    CacheManager.closeAll(cl2);
    assertTrue(cm1.isClosed());
    assertTrue(cm2.isClosed());
  }

  @Test
  public void closeSpecific() {
    ClassLoader cl1 = new URLClassLoader(new URL[0], this.getClass().getClassLoader());
    CacheManager cm1 = CacheManager.getInstance(cl1);
    CacheManager.close(cl1, "something");
    assertFalse(cm1.isClosed());
    CacheManager.close(cl1, "default");
    assertTrue(cm1.isClosed());
  }

  @Test
  public void closesCache() {
    String _uniqueName = this.getClass().getName() + ".closesCache";
    CacheManager cm = CacheManager.getInstance(_uniqueName);
    Cache c = Cache2kBuilder.forUnknownTypes()
      .manager(cm)
      .name("dummy")
      .build();
    assertSame(cm, c.getCacheManager());
    cm.close();
    assertTrue(c.isClosed());
  }

  @Test
  public void clearAllCaches() {
    String _uniqueName = this.getClass().getName() + ".clearAllCaches";
    CacheManager cm = CacheManager.getInstance(_uniqueName);
    Cache c = Cache2kBuilder.forUnknownTypes()
      .manager(cm)
      .name("dummy")
      .build();
    c.put("hello", "paul");
    assertTrue("has some data", c.keys().iterator().hasNext());
    c.getCacheManager().clear();
    assertFalse("no data", c.keys().iterator().hasNext());
    cm.close();
  }

  @Test
  public void createCache() {
    String _uniqueName = this.getClass().getName() + ".createCache";
    CacheManager cm = CacheManager.getInstance(_uniqueName);
    Cache c = cm.createCache(Cache2kBuilder.forUnknownTypes().name("dummy").toConfiguration());
    assertEquals("dummy", c.getName());
    assertSame(cm, c.getCacheManager());
    cm.close();
  }

  @Test
  public void getActiveCaches() {
    String _uniqueName = this.getClass().getName() + ".getActiveCaches";
    CacheManager cm = CacheManager.getInstance(_uniqueName);
    assertFalse(cm.getActiveCaches().iterator().hasNext());
    Cache c = Cache2kBuilder.forUnknownTypes().manager(cm).build();
    assertTrue(cm.getActiveCaches().iterator().hasNext());
    cm.close();
  }

  @Test
  public void onlyOneCacheForWired() {
    String _uniqueName = this.getClass().getName() + ".onlyOneCacheForWired";
    CacheManager cm = CacheManager.getInstance(_uniqueName);
    Cache2kBuilder b = Cache2kBuilder.forUnknownTypes().manager(cm);
    StaticUtil.enforceWiredCache(b);
    Cache c = b.build();
    assertEquals("one cache active", 1, StaticUtil.count(cm.getActiveCaches()));
    cm.close();
  }

}
