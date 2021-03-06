package com.jogamp.common.nio;

import static java.lang.System.out;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

import com.jogamp.common.os.MachineDescription;
import com.jogamp.common.os.Platform;
import com.jogamp.junit.util.JunitTracer;

import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestStructAccessorEndian extends JunitTracer {

    @Test
    public void testStructAccessorEndian1 () {
        final MachineDescription machine = Platform.getMachineDescription();        
        int bitsPtr = machine.pointerSizeInBytes() * 8;
        String bitsProp = System.getProperty("sun.arch.data.model");
        out.println("OS: <"+Platform.OS+"> CPU: <"+Platform.ARCH+"> Bits: <"+bitsPtr+"/"+bitsProp+">");
        out.println("CPU is: "+ (Platform.is32Bit()?"32":"64") + " bit");
        out.println(machine.toString());

        long[] valuesSource = { 0x0123456789ABCDEFL, 0x8877665544332211L, 0xAFFEDEADBEEFAFFEL };
        ByteBuffer tst = Buffers.newDirectByteBuffer(Buffers.SIZEOF_LONG * valuesSource.length);
        StructAccessor acc = new StructAccessor(tst);

        int i;

        for(i=0; i<valuesSource.length; i++) {
            acc.setLongAt(i*8, valuesSource[i]);
        }

        for(i=0; i<valuesSource.length; i++) {
            long v = acc.getLongAt(i*8);
            long t = valuesSource[i];
            Assert.assertTrue("Value["+i+"] shall be 0x"+Long.toHexString(t)+", is: 0x"+Long.toHexString(v), t == v);
        }
    }
    
    public static void main(String args[]) throws IOException {
        String tstname = TestStructAccessorEndian.class.getName();
        org.junit.runner.JUnitCore.main(tstname);
    }
    
}
