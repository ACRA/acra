/*
 *  Copyright 2016
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.acra.config;

import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import java.util.List;

/**
 * A policy which determines if a report should be resent.
 *
 * @author F43nd1r
 * @since 4.9.1
 */
public interface RetryPolicy {

    /**
     * @param senders a list of all senders.
     * @param failedSenders a list of all failed senders with the thrown exceptions.
     * @return <code>true</code> if the request should be resent later, otherwise <code>false</code>.
     */
    boolean shouldRetrySend(List<ReportSender> senders, List<FailedSender> failedSenders);

    class FailedSender {

        private final ReportSender sender;
        private final ReportSenderException exception;

        public FailedSender(ReportSender sender, ReportSenderException exception) {
            this.sender = sender;
            this.exception = exception;
        }

        public ReportSender getSender() {
            return sender;
        }

        public ReportSenderException getException() {
            return exception;
        }
    }
}
