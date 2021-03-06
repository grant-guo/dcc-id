/*
 * Copyright (c) 2016 The Ontario Institute for Cancer Research. All rights reserved.
 *                                                                                                               
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with                                  
 * this program. If not, see <http://www.gnu.org/licenses/>.                                                     
 *                                                                                                               
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY                           
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES                          
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT                           
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,                                
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED                          
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;                               
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER                              
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN                         
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.icgc.dcc.id.server.config;

import static java.util.concurrent.TimeUnit.MINUTES;
import static net.sf.ehcache.config.PersistenceConfiguration.Strategy.NONE;

import javax.management.MBeanServer;

import lombok.val;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.management.ManagementService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jmx.support.MBeanServerFactoryBean;

/**
 * Server wide caching configuration.
 */
@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {

  @Value("${cache.dir}")
  private String cacheDir;
  @Value("${cache.size.mutation}")
  private String mutationSize;
  @Value("${cache.size.donor}")
  private String donorSize;
  @Value("${cache.size.file}")
  private String fileSize;
  @Value("${cache.size.sample}")
  private String sampleSize;
  @Value("${cache.size.specimen}")
  private String specimenSize;

  @Bean(destroyMethod = "shutdown")
  public net.sf.ehcache.CacheManager ehCacheManager() {
    val tokens = new CacheConfiguration();
    tokens.setName("tokens");
    tokens.setTimeToLiveSeconds(MINUTES.toSeconds(30));
    tokens.setMaxEntriesLocalHeap(100);

    // In-memory caches
    val projectIds = createMemoryCache("projectIds", getCacheSize("1")); // 1M
    val donorIds = createMemoryCache("donorIds", getCacheSize(donorSize));
    val specimenIds = createMemoryCache("specimenIds", getCacheSize(specimenSize));
    val sampleIds = createMemoryCache("sampleIds", getCacheSize(sampleSize));
    val fileIds = createMemoryCache("fileIds", getCacheSize(fileSize));
    val mutationIds = createMemoryCache("mutationIds", getCacheSize(mutationSize));

    val config = new net.sf.ehcache.config.Configuration();
    config.addCache(tokens);
    config.addCache(projectIds);
    config.addCache(donorIds);
    config.addCache(specimenIds);
    config.addCache(sampleIds);
    config.addCache(fileIds);
    config.addCache(mutationIds);
    config.addDiskStore(new DiskStoreConfiguration().path(cacheDir));

    return net.sf.ehcache.CacheManager.newInstance(config);
  }

  @Bean
  @Override
  public CacheManager cacheManager() {
    return new EhCacheCacheManager(ehCacheManager());
  }

  @Bean
  @Override
  public KeyGenerator keyGenerator() {
    return new SimpleKeyGenerator();
  }

  @Bean(initMethod = "init", destroyMethod = "dispose")
  public ManagementService managementService() {
    // Expose the cache manager to JMX
    return new ManagementService(ehCacheManager(), mbeanServer(), true, true, true, true);
  }

  @Bean
  public MBeanServer mbeanServer() {
    val factory = new MBeanServerFactoryBean();
    factory.setLocateExistingServerIfPossible(true);
    factory.afterPropertiesSet();

    return factory.getObject();
  }

  private static CacheConfiguration createMemoryCache(String name, long maxBytesHeap) {
    val cache = new CacheConfiguration();
    cache.setName(name);
    cache.setMaxBytesLocalHeap(maxBytesHeap);
    cache.overflowToOffHeap(false);
    cache.setEternal(true); // Never expire
    cache.persistence(createPersistenceConfig());

    return cache;
  }

  private static PersistenceConfiguration createPersistenceConfig() {
    return new PersistenceConfiguration().strategy(NONE);
  }

  /**
   * @param cacheSize in MBytes
   * @return
   */
  private static long getCacheSize(String cacheSize) {
    return Long.parseLong(cacheSize) * 1024L * 1024L;
  }

}
