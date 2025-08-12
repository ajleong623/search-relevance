/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.searchrelevance.scheduler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.cluster.service.ClusterService;
import org.opensearch.core.action.ActionListener;
import org.opensearch.jobscheduler.spi.JobExecutionContext;
import org.opensearch.jobscheduler.spi.ScheduledJobParameter;
import org.opensearch.jobscheduler.spi.ScheduledJobRunner;
import org.opensearch.jobscheduler.spi.utils.LockService;
import org.opensearch.searchrelevance.transport.experiment.PutExperimentAction;
import org.opensearch.searchrelevance.transport.experiment.PutExperimentRequest;
import org.opensearch.threadpool.ThreadPool;
import org.opensearch.transport.client.Client;

public class SearchRelevanceJobRunner implements ScheduledJobRunner {
    private static final Logger log = LogManager.getLogger(ScheduledJobRunner.class);

    private static SearchRelevanceJobRunner INSTANCE;

    public static SearchRelevanceJobRunner getJobRunnerInstance() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        synchronized (SearchRelevanceJobRunner.class) {
            if (INSTANCE != null) {
                return INSTANCE;
            }
            INSTANCE = new SearchRelevanceJobRunner();
            return INSTANCE;
        }
    }

    private ClusterService clusterService;
    private ThreadPool threadPool;
    private Client client;

    private SearchRelevanceJobRunner() {
        // Singleton class, use getJobRunner method instead of constructor
    }

    public void setClusterService(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    public void setThreadPool(ThreadPool threadPool) {
        this.threadPool = threadPool;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public void runJob(ScheduledJobParameter jobParameter, JobExecutionContext context) {
        if (!(jobParameter instanceof SearchRelevanceJobParameters)) {
            throw new IllegalStateException(
                "Job parameter is not instance of SampleJobParameter, type: " + jobParameter.getClass().getCanonicalName()
            );
        }

        if (this.clusterService == null) {
            throw new IllegalStateException("ClusterService is not initialized.");
        }

        if (this.threadPool == null) {
            throw new IllegalStateException("ThreadPool is not initialized.");
        }

        final LockService lockService = context.getLockService();

        Runnable runnable = () -> {
            SearchRelevanceJobParameters parameter = (SearchRelevanceJobParameters) jobParameter;
            client.execute(
                PutExperimentAction.INSTANCE,
                new PutExperimentRequest(
                    parameter.getExperimentType(),
                    parameter.getExperimentQuerySetId(),
                    parameter.getExperimentSearchConfigurationList(),
                    parameter.getExperimentJudgmentList(),
                    parameter.getExperimentSize()
                ),
                new ActionListener<IndexResponse>() {
                    @Override
                    public void onResponse(IndexResponse response) {
                        try {
                            log.info("experiment has been started");
                        } catch (Exception e) {
                            onFailure(e);
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        log.error("Failed to send error response", e);
                    }
                }
            );
        };

        threadPool.generic().submit(runnable);
    }
}
