/*
 * Copyright (c) Microsoft Corporation
 * <p/>
 * All rights reserved.
 * <p/>
 * MIT License
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.hdinsight.sdk.common.livy.interactive

import com.microsoft.azure.hdinsight.spark.common.MockHttpService
import cucumber.api.java.Before
import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import org.apache.http.client.HttpResponseException
import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import kotlin.test.fail

class SessionScenario {
    var httpServerMock: MockHttpService? = null
    var sessionMock: Session? = null

    @Before
    fun setUp() {
        httpServerMock = MockHttpService()
    }

    @Given("^setup a mock livy interactive service for (.+) request '(.+)' to return '(.+)' with status code (\\d+)$")
    fun mockLivyInteractiveService(action: String, serviceUrl: String, response: String, statusCode: Int) {
        httpServerMock!!.stub(action, serviceUrl, statusCode, response)
    }

    @And("^create a livy Spark interactive session instance with name '(.+)'$")
    fun newSparkSession(name: String) {
        sessionMock = SparkSession(name, URI.create(httpServerMock!!.completeUrl("/")))
    }

    @And("^create a real livy Spark interactive session instance with name '(.+)'$")
    fun newRealSparkSession(name: String) {
    }

    @Then("^check the returned livy interactive session after creating should be$")
    fun checkCreatedSparkSession(expect: Map<String, String>) {
        sessionMock!!.create()
                .subscribe(
                        { session -> expect.keys.forEach { when(it) {
                            "id" -> assertThat(session.id).isEqualTo(expect[it]!!.toInt())
                        }}},
                        {
                            err -> throw err
                        }
                )

    }

    @Then("^check the HttpResponseException\\((\\d+)\\) when creating livy interactive session after creating should be thrown$")
    fun checkCreateSessionException(statusCodeExpect: Int) {
        sessionMock!!.create()
                .subscribe(
                        {
                            fail("Get a normal session return without exceptions.")
                        },
                        { err -> run {
                            assertThat(err).isInstanceOf(HttpResponseException::class.java)
                            assertThat((err as HttpResponseException).statusCode).isEqualTo(statusCodeExpect)
                        }}
                )

    }

    @Then("^check getting app ID with waiting for livy interactive session application run should be '(.*)'$")
    fun checkGettingAppId(appIdExpect: String) {
        val appIdGot = sessionMock!!.appId
                .toBlocking()
                .single()

        assertThat(appIdGot).isEqualTo(appIdExpect)
    }
}