package org.spongycastle.crypto.test;

import org.spongycastle.crypto.engines.SkipjackEngine;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.util.encoders.Hex;
import org.spongycastle.util.test.Test;
import org.spongycastle.util.test.TestResult;

/**
 */
public class SkipjackTest
    extends CipherTest
{
    static Test[]  tests = 
            {
                new BlockCipherVectorTest(0, new SkipjackEngine(),
                        new KeyParameter(Hex.decode("00998877665544332211")),
                        "33221100ddccbbaa", "2587cae27a12d300")
            };

    SkipjackTest()
    {
        super(tests);
    }

    public String getName()
    {
        return "SKIPJACK";
    }

    public static void main(
        String[]    args)
    {
        SkipjackTest    test = new SkipjackTest();
        TestResult      result = test.perform();

        System.out.println(result);
    }
}
