/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.searchrelevance.scheduler;

import java.io.IOException;
import java.time.Instant;

import org.opensearch.core.common.io.stream.StreamInput;
import org.opensearch.core.common.io.stream.StreamOutput;
import org.opensearch.core.xcontent.XContentBuilder;
import org.opensearch.jobscheduler.spi.ScheduledJobParameter;
import org.opensearch.jobscheduler.spi.schedule.IntervalSchedule;
import org.opensearch.jobscheduler.spi.schedule.Schedule;

public class SearchRelevanceJobParameters implements ScheduledJobParameter {
    public static final String NAME_FIELD = "name";
    public static final String ENABLED_FILED = "enabled";
    public static final String LAST_UPDATE_TIME_FIELD = "last_update_time";
    public static final String LAST_UPDATE_TIME_FIELD_READABLE = "last_update_time_field";
    public static final String SCHEDULE_FIELD = "schedule";
    public static final String ENABLED_TIME_FILED = "enabled_time";
    public static final String ENABLED_TIME_FILED_READABLE = "enabled_time_field";
    public static final String INDEX_NAME_FIELD = "index_name_to_watch";
    public static final String LOCK_DURATION_SECONDS = "lock_duration_seconds";
    public static final String JITTER = "jitter";
    public static final String EXPERIMENT_ID = "experiment_id";

    private String jobName;
    private Instant lastUpdateTime;
    private Instant enabledTime;
    private boolean isEnabled;
    private Schedule schedule;
    private String indexToWatch;
    private Long lockDurationSeconds;
    private Double jitter;
    private String experimentId;

    public SearchRelevanceJobParameters() {}

    public SearchRelevanceJobParameters(
        String id,
        String name,
        String indexToWatch,
        Schedule schedule,
        Long lockDurationSeconds,
        Double jitter,
        String experimentId
    ) {
        this.jobName = name;
        this.indexToWatch = indexToWatch;
        this.schedule = schedule;

        Instant now = Instant.now();
        this.isEnabled = true;
        this.enabledTime = now;
        this.lastUpdateTime = now;
        this.lockDurationSeconds = lockDurationSeconds;
        this.jitter = jitter;
        this.experimentId = experimentId;
    }

    public SearchRelevanceJobParameters(StreamInput in) throws IOException {
        this.jobName = in.readString();
        this.indexToWatch = in.readString();
        this.schedule = new IntervalSchedule(in);
        this.isEnabled = in.readBoolean();
        this.enabledTime = in.readInstant();
        this.lastUpdateTime = in.readInstant();
        this.lockDurationSeconds = in.readLong();
        this.jitter = in.readOptionalDouble();
        this.experimentId = in.readOptionalString();
    }

    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(jobName);
        out.writeString(indexToWatch);
        schedule.writeTo(out);
        out.writeBoolean(isEnabled);
        out.writeInstant(enabledTime);
        out.writeInstant(lastUpdateTime);
        out.writeLong(lockDurationSeconds);
        out.writeOptionalDouble(jitter);
        out.writeString(experimentId);
    }

    @Override
    public String getName() {
        return this.jobName;
    }

    @Override
    public Instant getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    @Override
    public Instant getEnabledTime() {
        return this.enabledTime;
    }

    @Override
    public Schedule getSchedule() {
        return this.schedule;
    }

    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }

    @Override
    public Long getLockDurationSeconds() {
        return this.lockDurationSeconds;
    }

    @Override
    public Double getJitter() {
        return jitter;
    }

    public String getIndexToWatch() {
        return this.indexToWatch;
    }

    public String getExperimentId() {
        return this.experimentId;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setLastUpdateTime(Instant lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public void setEnabledTime(Instant enabledTime) {
        this.enabledTime = enabledTime;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public void setIndexToWatch(String indexToWatch) {
        this.indexToWatch = indexToWatch;
    }

    public void setLockDurationSeconds(Long lockDurationSeconds) {
        this.lockDurationSeconds = lockDurationSeconds;
    }

    public void setJitter(Double jitter) {
        this.jitter = jitter;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        builder.field(NAME_FIELD, this.jobName)
            .field(ENABLED_FILED, this.isEnabled)
            .field(SCHEDULE_FIELD, this.schedule)
            .field(INDEX_NAME_FIELD, this.indexToWatch);
        if (this.enabledTime != null) {
            builder.timeField(ENABLED_TIME_FILED, ENABLED_TIME_FILED_READABLE, this.enabledTime.toEpochMilli());
        }
        if (this.lastUpdateTime != null) {
            builder.timeField(LAST_UPDATE_TIME_FIELD, LAST_UPDATE_TIME_FIELD_READABLE, this.lastUpdateTime.toEpochMilli());
        }
        if (this.lockDurationSeconds != null) {
            builder.field(LOCK_DURATION_SECONDS, this.lockDurationSeconds);
        }
        if (this.jitter != null) {
            builder.field(JITTER, this.jitter);
        }
        if (this.experimentId != null) {
            builder.field(EXPERIMENT_ID, this.experimentId);
        }
        builder.endObject();
        return builder;
    }
}
