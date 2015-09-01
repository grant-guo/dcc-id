/*
 * Copyright (c) 2015 The Ontario Institute for Cancer Research. All rights reserved.                             
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

import org.icgc.dcc.id.client.http.HttpIdClient;
import org.junit.Ignore;
import org.junit.Test;

import lombok.Cleanup;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Ignore
@Slf4j
public class HttpIdClientIntegrationTest {

  @Test
  public void testClient() throws Exception {
    @Cleanup
    val client = new HttpIdClient(HttpIdClient.Config.builder()
        .release("1")
        .serviceUrl("https://localhost:8443")
        .authToken(System.getProperty("authToken"))
        .strictSSLCertificates(false)
        .build());

    val submittedDonorId = "1";
    val submittedProjectId = "2";
    val donorId = client.getDonorId(submittedDonorId, submittedProjectId);
    log.info("donorId: {}", donorId);
  }

}