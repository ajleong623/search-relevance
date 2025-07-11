/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.searchrelevance.action.judgment;

import static org.opensearch.searchrelevance.common.PluginConstants.INITIALIZE_URL;
import static org.opensearch.searchrelevance.common.PluginConstants.JUDGMENTS_URL;
import static org.opensearch.searchrelevance.common.PluginConstants.JUDGMENT_INDEX;
import static org.opensearch.searchrelevance.common.PluginConstants.UBI_EVENTS_INDEX;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.message.BasicHeader;
import org.opensearch.client.Response;
import org.opensearch.rest.RestRequest;
import org.opensearch.searchrelevance.BaseSearchRelevanceIT;
import org.opensearch.test.OpenSearchIntegTestCase;

import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import com.google.common.collect.ImmutableList;

import lombok.SneakyThrows;

@ThreadLeakScope(ThreadLeakScope.Scope.NONE)
@OpenSearchIntegTestCase.ClusterScope(scope = OpenSearchIntegTestCase.Scope.SUITE)
public class CalculateJudgmentsIT extends BaseSearchRelevanceIT {
    public void initializeUBIIndices() throws IOException, URISyntaxException {
        makeRequest(
            client(),
            RestRequest.Method.POST.name(),
            INITIALIZE_URL,
            null,
            toHttpEntity(""),
            ImmutableList.of(new BasicHeader(HttpHeaders.USER_AGENT, DEFAULT_USER_AGENT))
        );

        String importDatasetBody = Files.readString(Path.of(classLoader.getResource("sample_ubi_data/SampleUBIData.json").toURI()));

        bulkIngest(UBI_EVENTS_INDEX, importDatasetBody);
    }

    @SneakyThrows
    public void testCalculateJudgments() {
        initializeUBIIndices();

        String requestBody = Files.readString(Path.of(classLoader.getResource("judgment/ImplicitJudgmentsDates.json").toURI()));
        Response importResponse = makeRequest(
            client(),
            RestRequest.Method.PUT.name(),
            JUDGMENTS_URL,
            null,
            toHttpEntity(requestBody),
            ImmutableList.of(new BasicHeader(HttpHeaders.USER_AGENT, DEFAULT_USER_AGENT))
        );
        Map<String, Object> importResultJson = entityAsMap(importResponse);
        assertNotNull(importResultJson);
        String judgmentsId = importResultJson.get("judgment_id").toString();
        assertNotNull(judgmentsId);

        // wait for completion of import action
        Thread.sleep(DEFAULT_INTERVAL_MS);

        String getJudgmentsByIdUrl = String.join("/", JUDGMENT_INDEX, "_doc", judgmentsId);
        Response getJudgmentsResponse = makeRequest(
            adminClient(),
            RestRequest.Method.GET.name(),
            getJudgmentsByIdUrl,
            null,
            null,
            ImmutableList.of(new BasicHeader(HttpHeaders.USER_AGENT, DEFAULT_USER_AGENT))
        );
        Map<String, Object> getJudgmentsResultJson = entityAsMap(getJudgmentsResponse);
        assertNotNull(getJudgmentsResultJson);
        assertEquals(judgmentsId, getJudgmentsResultJson.get("_id").toString());
    }
}
