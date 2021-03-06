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
package org.icgc.dcc.id.client.util;

import static com.google.common.base.Joiner.on;
import static com.google.common.hash.Hashing.md5;

import java.io.IOException;
import java.util.Optional;

import org.icgc.dcc.id.client.core.IdClient;
import org.icgc.dcc.id.core.Prefixes;

import com.google.common.base.Joiner;
import com.google.common.hash.HashFunction;

import lombok.NoArgsConstructor;

/**
 * Stateless hash based {@link IdClient} implementation that returns a stable id based on it it's inputs.
 */
@NoArgsConstructor
public class HashIdClient implements IdClient {

  /**
   * Id generation strategy state.
   */
  private static final HashFunction MD5 = md5();
  private static final Joiner JOINER = on(":");

  /**
   * Required for reflection in Loader
   */
  public HashIdClient(String serviceUri, String release) {
    // Empty
  }

  @Override
  public Optional<String> getDonorId(String submittedDonorId, String submittedProjectId) {
    return Optional.of(Prefixes.DONOR_ID_PREFIX + generateId(
        submittedDonorId,
        submittedProjectId));
  }

  @Override
  public Optional<String> getSampleId(String submittedSampleId, String submittedProjectId) {
    return Optional.of(Prefixes.SAMPLE_ID_PREFIX + generateId(
        submittedSampleId,
        submittedProjectId));
  }

  @Override
  public Optional<String> getSpecimenId(String submittedSpecimenId, String submittedProjectId) {
    return Optional.of(Prefixes.SPECIMEN_ID_PREFIX + generateId(
        submittedSpecimenId,
        submittedProjectId));
  }

  @Override
  public Optional<String> getMutationId(String chromosome, String chromosomeStart, String chromosomeEnd,
      String mutation, String mutationType, String assemblyVersion) {
    return Optional.of(Prefixes.MUTATION_ID_PREFIX + generateId(
        chromosome,
        chromosomeStart,
        chromosomeEnd,
        mutation,
        mutationType,
        assemblyVersion));
  }

  @Override
  public Optional<String> getFileId(String submittedFileId) {
    return Optional.of(Prefixes.FILE_ID_PREFIX + generateId(submittedFileId));
  }

  @Override
  public String createDonorId(String submittedDonorId, String submittedProjectId) {
    return getDonorId(submittedDonorId, submittedProjectId).get();
  }

  @Override
  public String createMutationId(String chromosome, String chromosomeStart, String chromosomeEnd, String mutation,
      String mutationType, String assemblyVersion) {
    return getMutationId(chromosome, chromosomeStart, chromosomeEnd, mutation, mutationType, assemblyVersion).get();
  }

  @Override
  public String createSampleId(String submittedSampleId, String submittedProjectId) {
    return getSampleId(submittedSampleId, submittedProjectId).get();
  }

  @Override
  public String createSpecimenId(String submittedSpecimenId, String submittedProjectId) {
    return getSpecimenId(submittedSpecimenId, submittedProjectId).get();
  }

  @Override
  public String createFileId(String submittedFileId) {
    return getFileId(submittedFileId).get();
  }

  @Override
  public void close() throws IOException {
    // No-op
  }

  private static String generateId(String... keys) {
    return MD5.hashUnencodedChars(JOINER.join(keys)).toString();
  }

}
