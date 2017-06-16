import jota.IotaAPI;
import jota.model.Transaction;
import jota.utils.Converter;
import org.junit.Test;

/**
 * Created by toonsev on 6/16/2017.
 */
public class Testing {

    @Test
    public void test(){
        String trytes = "ABCDEF";
        int[] trits = Converter.trits(trytes);
        System.out.println(trits.length);
    }
}
