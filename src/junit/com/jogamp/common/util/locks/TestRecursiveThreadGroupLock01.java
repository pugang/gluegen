/**
 * Copyright 2010 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 * 
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of JogAmp Community.
 */
 
package com.jogamp.common.util.locks;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.jogamp.common.os.Platform;
import com.jogamp.junit.util.JunitTracer;

import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestRecursiveThreadGroupLock01 extends JunitTracer {

    public enum YieldMode {
        NONE(0), YIELD(1), SLEEP(2); 
        
        public final int id;

        YieldMode(int id){
            this.id = id;
        }
    }    
    
    static void yield(YieldMode mode) {
        switch(mode) {
            case YIELD:
                Thread.yield();
                break;
            case SLEEP:
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
                break;
            default:
                break;
        }

    }

    static class LockedObject {
        static final boolean DEBUG = false;
        
        private RecursiveThreadGroupLock locker; // post
        private volatile int slaveCounter;

        public LockedObject() {
            locker = LockFactory.createRecursiveThreadGroupLock();
            slaveCounter = 0;
        }

        public final void masterAction(String tab, String name, Thread[] slaves, int loops, int mark, final YieldMode yieldMode) {
            locker.lock();
            if(DEBUG) {
                System.err.println(tab+"<"+name+" c "+slaveCounter);
            }
            Assert.assertTrue(mark>loops);
            Assert.assertTrue(loops*loops>mark);
            try {
                if(slaveCounter<mark) {
                    for(int i=0; i<slaves.length; i++) {
                        locker.addOwner(slaves[i]);
                        slaves[i].start();
                    }
                    while(slaveCounter<mark) {
                        yield(yieldMode);
                    }
                }
            } finally {
                if(DEBUG) {
                    System.err.println(tab+" "+name+" c "+slaveCounter+">");
                }
                // Implicit waits until all slaves got off the lock
                locker.unlock();
            }
        }

        public final void slaveAction(String tab, String name, int loops, int mark, YieldMode yieldMode) {
            if(slaveCounter>=mark) {
                if(DEBUG) {
                    System.err.println(tab+"["+name+" c "+slaveCounter+" - NOP]");
                }
                return;                
            }
            locker.lock();
            if(DEBUG) {
                System.err.println(tab+"["+name+" c "+slaveCounter);
            }
            Assert.assertTrue(mark>loops);
            Assert.assertTrue(loops*loops>mark);
            try {
                while(loops>0 && slaveCounter<mark) {
                    slaveCounter++;
                    loops--;
                }
                /**
                while(slaveCounter<mark) {
                    slaveCounter++;
                } */
                yield(yieldMode);
            } finally {
                if(DEBUG) {
                    System.err.println(tab+" "+name+" c "+slaveCounter+"]");
                }
                locker.unlock();
            }
        }

        public final boolean isLocked() {
            return locker.isLocked();
        }
        
    }

    interface LockedObjectRunner extends Runnable {
        void stop();
        boolean isStarted();
        boolean isStopped();
        void waitUntilStopped();
    }

    class LockedObjectRunner1 implements LockedObjectRunner {
        volatile boolean shouldStop;
        volatile boolean stopped;
        volatile boolean started;
        String tab, name;
        LockedObject lo;
        Thread[] slaves;
        int loops;
        int mark;
        YieldMode yieldMode;

        /** master constructor */
        public LockedObjectRunner1(String tab, String name, LockedObject lo, Thread[] slaves, int loops, int mark, YieldMode yieldMode) {
            this.tab = tab;
            this.name = name;
            this.lo = lo;
            this.slaves = slaves;
            this.loops = loops;
            this.mark = mark;
            this.shouldStop = false;
            this.stopped = false;
            this.yieldMode = yieldMode;
            Assert.assertTrue(mark>loops);
            Assert.assertTrue(loops*loops>mark);
        }

        /** slave constructor */
        public LockedObjectRunner1(String tab, String name, LockedObject lo, int loops, int mark, YieldMode yieldMode) {
            this.tab = tab;
            this.name = name;
            this.lo = lo;
            this.slaves = null; // slave
            this.loops = loops;
            this.mark = mark;
            this.shouldStop = false;
            this.stopped = false;
            this.yieldMode = yieldMode;
            Assert.assertTrue(mark>loops);
            Assert.assertTrue(loops*loops>mark);
        }
        
        public final void stop() {
            shouldStop = true;
        }

        public final boolean isStarted() {
            return started;
        }
        public final boolean isStopped() {
            return stopped;
        }
        
        public void waitUntilStopped() {
            synchronized(this) {
                while(!stopped) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            
        }

        public void run() {
            synchronized(this) {
                started = true;
                for(int i=0; !shouldStop && i<loops; i++) {
                    if(null != slaves) {
                        lo.masterAction(tab, name, slaves, loops, mark, yieldMode);
                    } else {
                        lo.slaveAction(tab, name, loops, mark, yieldMode);
                    }
                }
                stopped = true;
                this.notifyAll();
            }
        }
    }

    protected long testLockedObjectImpl(LockFactory.ImplType implType, boolean fair, 
                                        int slaveThreadNum, int concurrentThreadNum, 
                                        int loops, int mark, YieldMode yieldMode) throws InterruptedException {
        final long t0 = System.currentTimeMillis();
        LockedObject lo = new LockedObject();
        LockedObjectRunner[] concurrentRunners = new LockedObjectRunner[concurrentThreadNum];
        LockedObjectRunner[] slaveRunners = new LockedObjectRunner[slaveThreadNum];
        Thread[] concurrentThreads = new Thread[concurrentThreadNum];
        Thread[] slaveThreads = new Thread[slaveThreadNum];
        Thread[] noCoOwnerThreads = new Thread[0];
        int i;

        for(i=0; i<slaveThreadNum; i++) {
            slaveRunners[i] = new LockedObjectRunner1("    ", "s"+i, lo, loops, mark, yieldMode);
            String name = "ActionThread-Slaves-"+i+"_of_"+slaveThreadNum;
            slaveThreads[i] = new Thread( slaveRunners[i], name );
        }
        for(i=0; i<concurrentThreadNum; i++) {
            String name;
            if(i==0) {
                concurrentRunners[i] = new LockedObjectRunner1("", "M0", lo, slaveThreads, loops, mark, yieldMode);
                name = "ActionThread-Master-"+i+"_of_"+concurrentThreadNum;            
            } else {
                concurrentRunners[i] = new LockedObjectRunner1("  ", "O"+i, lo, noCoOwnerThreads, loops, mark, yieldMode);
                name = "ActionThread-Others-"+i+"_of_"+concurrentThreadNum;
            }
            concurrentThreads[i] = new Thread( concurrentRunners[i], name );
            concurrentThreads[i].start();
            if(i==0) {
                // master thread w/ slaves shall start first
                while(!concurrentRunners[i].isStarted()) {
                    Thread.sleep(100);
                }
            }
        }

        for( i=0; i<slaveThreadNum; i++ ) {
            slaveRunners[i].waitUntilStopped();
        }
        for( i=0; i<concurrentThreadNum; i++ ) {
            concurrentRunners[i].waitUntilStopped();
        }
        Assert.assertEquals(0, lo.locker.getHoldCount());
        Assert.assertEquals(false, lo.locker.isLocked());
        
        final long dt = System.currentTimeMillis()-t0;
        
        System.err.println();
        final String fair_S = fair ? "fair  " : "unfair" ;
        System.err.printf("---- TestRecursiveLock01.testLockedObjectThreading: i %5s, %s, threads %2d, loops-outter %6d, loops-inner %6d, yield %5s - dt %6d ms", 
                implType, fair_S, concurrentThreadNum, loops, mark, yieldMode, dt);
        System.err.println();
        return dt;
    }
    
    @Test
    public void testTwoThreadsInGroup() throws InterruptedException {
        LockFactory.ImplType t = LockFactory.ImplType.Int02ThreadGroup; 
        boolean fair=true;
        int coOwnerThreadNum=2;
        int threadNum=5; 
        int loops=1000; 
        int mark=10000; 
        YieldMode yieldMode=YieldMode.YIELD;
        
        if( Platform.getCPUFamily() == Platform.CPUFamily.ARM ) {
            loops=5; mark=10;
        }
        
        testLockedObjectImpl(t, fair, coOwnerThreadNum, threadNum, loops, mark, yieldMode);
    }

    public static void main(String args[]) throws IOException, InterruptedException {
        String tstname = TestRecursiveThreadGroupLock01.class.getName();
        org.junit.runner.JUnitCore.main(tstname);
        
        /**
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        System.err.println("Press enter to continue");
        System.err.println(stdin.readLine()); 
        TestRecursiveLock01 t = new TestRecursiveLock01();
        t.testLockedObjectThreading5x1000x10000N_Int01_Unfair();
        
        t.testLockedObjectThreading5x1000x10000N_Int01_Fair();
        t.testLockedObjectThreading5x1000x10000N_Java5_Fair();
        t.testLockedObjectThreading5x1000x10000N_Int01_Unfair();
        t.testLockedObjectThreading5x1000x10000N_Java5_Unfair();
        */
    }

}
