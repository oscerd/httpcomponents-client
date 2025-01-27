/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.hc.client5.http.impl.cache;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Formatter;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Should produce reasonably unique tokens.
 */
class BasicIdGenerator {

    private final String hostname;
    private final SecureRandom rnd;

    private long count;

    private final ReentrantLock lock;

    public BasicIdGenerator() {
        super();
        String hostname;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException ex) {
            hostname = "localhost";
        }
        this.hostname = hostname;
        try {
            this.rnd = SecureRandom.getInstance("SHA1PRNG");
        } catch (final NoSuchAlgorithmException ex) {
            throw new Error(ex);
        }
        this.rnd.setSeed(System.currentTimeMillis());
        this.lock = new ReentrantLock();
    }

    public void generate(final StringBuilder buffer) {
        lock.lock();
        try {
            this.count++;
            final int rndnum = this.rnd.nextInt();
            buffer.append(System.currentTimeMillis());
            buffer.append('.');
            try (Formatter formatter = new Formatter(buffer, Locale.ROOT)) {
                formatter.format("%1$016x-%2$08x", this.count, rndnum);
            }
            buffer.append('.');
            buffer.append(this.hostname);
        } finally {
            lock.unlock();
        }
    }

    public String generate() {
        final StringBuilder buffer = new StringBuilder();
        generate(buffer);
        return buffer.toString();
    }

}
