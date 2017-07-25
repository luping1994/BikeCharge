package net.suntrans.bikecharge;

import android.util.Log;

import net.suntrans.bikecharge.utils.Encryp;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        if (hehe()){
            System.out.println("有权限");
        }else {
            System.out.println("没权限");
        }
    }

    private boolean hehe() {
        String[] s= new String[]{"a","b","c"};
        for (int i=0;i<s.length;i++){
            return s[i].equals("d");
        }
        return true;
    }
}