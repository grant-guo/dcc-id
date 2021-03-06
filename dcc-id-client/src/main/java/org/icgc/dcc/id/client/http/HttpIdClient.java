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
package org.icgc.dcc.id.client.http;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.icgc.dcc.id.client.http.RetryContext.waitBeforeRetry;
import static org.icgc.dcc.id.client.http.webclient.WebClientFactory.createClient;
import static org.icgc.dcc.id.core.Prefixes.DONOR_ID_PREFIX;
import static org.icgc.dcc.id.core.Prefixes.FILE_ID_PREFIX;
import static org.icgc.dcc.id.core.Prefixes.MUTATION_ID_PREFIX;
import static org.icgc.dcc.id.core.Prefixes.SAMPLE_ID_PREFIX;
import static org.icgc.dcc.id.core.Prefixes.SPECIMEN_ID_PREFIX;
import static org.icgc.dcc.id.util.Ids.validateId;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Optional;

import lombok.NonNull;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.icgc.dcc.id.client.core.IdClient;
import org.icgc.dcc.id.client.http.webclient.WebClientConfig;
import org.icgc.dcc.id.core.ExhaustedRetryException;
import org.icgc.dcc.id.core.IdentifierException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

@Slf4j
public class HttpIdClient implements IdClient {

  /**
   * Constants.
   */
  public final static String DONOR_ID_PATH = "/donor/id";
  public final static String SPECIMEN_ID_PATH = "/specimen/id";
  public final static String SAMPLE_ID_PATH = "/sample/id";
  public final static String MUTATION_ID_PATH = "/mutation/id";
  public final static String FILE_ID_PATH = "/file/id";

  /**
   * State.
   */
  private final Client client;
  private final WebResource resource;
  private final String release;
  private final WebClientConfig clientConfig;

  public HttpIdClient(@NonNull WebClientConfig config) {
    this.client = createClient(config);
    this.release = config.getRelease();
    this.resource = client.resource(config.getServiceUrl());
    this.clientConfig = config;
  }

  /**
   * Required for reflection in Loader
   */
  public HttpIdClient(@NonNull String serviceUri, String release, String authToken) {
    this(WebClientConfig.builder()
        .serviceUrl(serviceUri)
        .release(release)
        .authToken(authToken)
        .build());
  }

  @Override
  public Optional<String> getDonorId(@NonNull String submittedDonorId, @NonNull String submittedProjectId) {
    return getDonorId(submittedDonorId, submittedProjectId, false);
  }

  @Override
  public String createDonorId(@NonNull String submittedDonorId, @NonNull String submittedProjectId) {
    val donorId = getDonorId(submittedDonorId, submittedProjectId, true).get();
    checkState(!isNullOrEmpty(donorId), "Failed to create donor id. submittedDonorId: '%s', submittedProjectId: '%s'",
        submittedDonorId, submittedProjectId);

    return donorId;
  }

  private Optional<String> getDonorId(String submittedDonorId, String submittedProjectId, boolean create) {
    val request = resource
        .path(DONOR_ID_PATH)
        .queryParam("submittedDonorId", submittedDonorId)
        .queryParam("submittedProjectId", submittedProjectId)
        .queryParam("release", release)
        .queryParam("create", String.valueOf(create));

    val id = getResponse(request);
    validateId(id, DONOR_ID_PREFIX);

    return id;
  }

  @Override
  public Optional<String> getSpecimenId(@NonNull String submittedSpecimenId, @NonNull String submittedProjectId) {
    return getSpecimenId(submittedSpecimenId, submittedProjectId, false);
  }

  @Override
  public String createSpecimenId(@NonNull String submittedSpecimenId, @NonNull String submittedProjectId) {
    val specimenId = getSpecimenId(submittedSpecimenId, submittedProjectId, true).get();
    checkState(!isNullOrEmpty(specimenId), "Failed to create specimen id. submittedSpecimenId: '%s', "
        + "submittedProjectId: '%s'", submittedSpecimenId, submittedProjectId);

    return specimenId;
  }

  private Optional<String> getSpecimenId(String submittedSpecimenId, String submittedProjectId, boolean create) {
    val request = resource
        .path(SPECIMEN_ID_PATH)
        .queryParam("submittedSpecimenId", submittedSpecimenId)
        .queryParam("submittedProjectId", submittedProjectId)
        .queryParam("release", release)
        .queryParam("create", String.valueOf(create));

    val id = getResponse(request);
    validateId(id, SPECIMEN_ID_PREFIX);

    return id;
  }

  @Override
  public Optional<String> getSampleId(@NonNull String submittedSampleId, @NonNull String submittedProjectId) {
    return getSampleId(submittedSampleId, submittedProjectId, false);
  }

  @Override
  public String createSampleId(@NonNull String submittedSampleId, @NonNull String submittedProjectId) {
    val sampleId = getSampleId(submittedSampleId, submittedProjectId, true).get();
    checkState(!isNullOrEmpty(sampleId), "Failed to create sample id. submittedSampleId: '%s', "
        + "submittedProjectId: '%s'", submittedSampleId, submittedProjectId);

    return sampleId;
  }

  private Optional<String> getSampleId(String submittedSampleId, String submittedProjectId, boolean create) {
    val request = resource
        .path(SAMPLE_ID_PATH)
        .queryParam("submittedSampleId", submittedSampleId)
        .queryParam("submittedProjectId", submittedProjectId)
        .queryParam("release", release)
        .queryParam("create", String.valueOf(create));

    val id = getResponse(request);
    validateId(id, SAMPLE_ID_PREFIX);

    return id;
  }

  @Override
  public Optional<String> getMutationId(@NonNull String chromosome, @NonNull String chromosomeStart,
      @NonNull String chromosomeEnd, @NonNull String mutation, @NonNull String mutationType,
      @NonNull String assemblyVersion) {
    return getMutationId(chromosome, chromosomeStart, chromosomeEnd, mutation, mutationType, assemblyVersion, false);
  }

  @Override
  public String createMutationId(@NonNull String chromosome, @NonNull String chromosomeStart,
      @NonNull String chromosomeEnd, @NonNull String mutation, @NonNull String mutationType,
      @NonNull String assemblyVersion) {
    val mutationId = getMutationId(chromosome, chromosomeStart, chromosomeEnd, mutation, mutationType, assemblyVersion,
        true).get();
    checkState(!isNullOrEmpty(mutationId), "Failed to create mutation id. chromosome: '%s', chromosomeStart: '%s', "
        + "chromosomeEnd: '%s', mutation: '%s', mutationType: '%s', assemblyVersion: '%s'", chromosome,
        chromosomeStart, chromosomeEnd, mutation, mutationType, assemblyVersion);

    return mutationId;
  }

  private Optional<String> getMutationId(String chromosome, String chromosomeStart, String chromosomeEnd,
      String mutation, String mutationType, String assemblyVersion, boolean create) {
    val request = resource
        .path(MUTATION_ID_PATH)
        .queryParam("chromosome", chromosome)
        .queryParam("chromosomeStart", chromosomeStart)
        .queryParam("chromosomeEnd", chromosomeEnd)
        .queryParam("mutation", mutation)
        .queryParam("mutationType", mutationType)
        .queryParam("assemblyVersion", assemblyVersion)
        .queryParam("release", release)
        .queryParam("create", String.valueOf(create));

    val id = getResponse(request);
    validateId(id, MUTATION_ID_PREFIX);

    return id;
  }

  @Override
  public void close() throws IOException {
    log.info("Destroying client...");
    client.destroy();
    log.info("Client destroyed");
  }

  @Override
  public Optional<String> getFileId(@NonNull String submittedFileId) {
    return getFileId(submittedFileId, false);
  }

  @Override
  public String createFileId(@NonNull String submittedFileId) {
    val fileId = getFileId(submittedFileId, true).get();
    checkState(!isNullOrEmpty(fileId), "Failed to create file id. submittedFileId: '%s'", submittedFileId);

    return fileId;
  }

  private Optional<String> getFileId(String submittedFileId, boolean create) {
    val request = resource
        .path(FILE_ID_PATH)
        .queryParam("submittedFileId", submittedFileId)
        .queryParam("create", String.valueOf(create));

    val id = getResponse(request);
    validateId(id, FILE_ID_PREFIX);

    return id;
  }

  /*
   * Helpers
   */

  private Optional<String> getResponse(WebResource request) {
    return getResponse(request, RetryContext.create(clientConfig));
  }

  private static Optional<String> getResponse(WebResource request, RetryContext retryContext) {
    try {
      val response = request.get(ClientResponse.class);
      verifyNonRetriableErrors(response);

      if (response.getStatus() == NOT_FOUND.getStatusCode()) {
        return Optional.empty();
      }

      if (response.getStatus() == SERVICE_UNAVAILABLE.getStatusCode()) {
        return retryFailedRequest(request, retryContext);
      }

      val entity = response.getEntity(String.class);

      return Optional.of(entity);
    } catch (ClientHandlerException e) {
      val cause = e.getCause();
      if (retryContext.isRetry() && isRetryException(cause)) {
        log.info("{}", e.getMessage());

        return getResponse(request, waitBeforeRetry(retryContext));
      }

      throw e;
    } catch (ExhaustedRetryException e) {
      log.error("Failed to request ID because of exhaused retries. Request: {}", request);
      throw e;
    } catch (Exception e) {
      log.info("Error requesting {}, {}: {}", request, retryContext, e);
      throw new IdentifierException(e);
    }
  }

  private static Optional<String> retryFailedRequest(WebResource request, RetryContext retryContext) {
    if (retryContext.isRetry() == false) {
      throw new ExhaustedRetryException();
    }

    log.warn("Could not get {}", request);
    return getResponse(request, waitBeforeRetry(retryContext));
  }

  private static boolean isRetryException(Throwable cause) {
    return cause instanceof SocketTimeoutException || cause instanceof ConnectTimeoutException
        || cause instanceof SocketException;
  }

  /**
   * @throws IdentifierException
   */
  private static void verifyNonRetriableErrors(ClientResponse response) {
    if (response.getStatus() == UNAUTHORIZED.getStatusCode()
        || response.getStatus() == FORBIDDEN.getStatusCode()
        || response.getStatus() == INTERNAL_SERVER_ERROR.getStatusCode()) {
      throw new IdentifierException(response.getEntity(String.class));
    }
  }

}
