/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.searchrelevance.transport.judgment;

import java.io.IOException;
import java.util.Map;

import org.opensearch.core.common.io.stream.StreamInput;
import org.opensearch.core.common.io.stream.StreamOutput;
import org.opensearch.searchrelevance.model.JudgmentType;

import reactor.util.annotation.NonNull;

public class PutUbiJudgmentRequest extends PutJudgmentRequest {
    private String clickModel;
    private int maxRank;
    private Map<String, Object> dateRangeParameters;

    public PutUbiJudgmentRequest(
        @NonNull JudgmentType type,
        @NonNull String name,
        @NonNull String description,
        @NonNull String clickModel,
        int maxRank,
        @NonNull Map<String, Object> dateRangeParameters
    ) {
        super(type, name, description);
        this.clickModel = clickModel;
        this.maxRank = maxRank;
        this.dateRangeParameters = dateRangeParameters;
    }

    public PutUbiJudgmentRequest(StreamInput in) throws IOException {
        super(in);
        this.clickModel = in.readString();
        this.maxRank = in.readInt();
        this.dateRangeParameters = in.readMap();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeString(clickModel);
        out.writeInt(maxRank);
        out.writeMap(dateRangeParameters);
    }

    public String getClickModel() {
        return clickModel;
    }

    public int getMaxRank() {
        return maxRank;
    }

    public Map<String, Object> getDateRangeParameters() {
        return dateRangeParameters;
    }
}
