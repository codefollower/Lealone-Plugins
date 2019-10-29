/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lealone.test.perf.transaction;

import org.h2.mvstore.MVStore;
import org.h2.mvstore.tx.Transaction;
import org.h2.mvstore.tx.TransactionMap;
import org.h2.mvstore.tx.TransactionStore;

public class H2TransactionPerfTest extends TransactionPerfTest {

    public static void main(String[] args) throws Exception {
        H2TransactionPerfTest test = new H2TransactionPerfTest();
        run(test);
    }

    private TransactionStore ts;

    @Override
    protected void init() throws Exception {
        // MVStore.Builder builder = new MVStore.Builder();
        MVStore store = MVStore.open(null);
        ts = new TransactionStore(store);
        ts.init();

        singleThreadSerialWrite();
    }

    @Override
    protected void destroy() throws Exception {
        ts.close();
    }

    private void singleThreadSerialWrite() {
        Transaction tx1 = ts.begin();
        TransactionMap<Integer, String> map1 = tx1.openMap(mapName);
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < rowCount; i++) {
            map1.put(i, "valueaaa");
        }
        tx1.commit();
        long t2 = System.currentTimeMillis();
        printResult("single-thread serial write time: " + (t2 - t1) + " ms, row count: " + rowCount);
    }

    @Override
    protected void write(int start, int end) throws Exception {
        Transaction tx1 = ts.begin();
        TransactionMap<Integer, String> map1 = tx1.openMap(mapName);
        for (int i = start; i < end; i++) {
            Integer key;
            if (isRandom())
                key = randomKeys[i];
            else
                key = i;
            String value = "value-";// "value-" + key;
            // map.put(key, value);

            Transaction t = ts.begin();
            TransactionMap<Integer, String> m = map1.getInstance(t);
            m.put(key, value);
            t.commit();
            notifyOperationComplete();
        }
    }
}
