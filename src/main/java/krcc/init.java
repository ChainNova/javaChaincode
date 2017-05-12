package krcc;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.sdk.shim.ChaincodeStub;
import org.hyperledger.fabric.sdk.shim.shim.KeyModificationImpl;

import static org.hyperledger.fabric.shim.ChaincodeHelper.newBadRequestResponse;
import static org.hyperledger.fabric.shim.ChaincodeHelper.newSuccessResponse;

import java.util.ArrayList;

/**
 * Created by zerppen on 5/2/17.
 */
public class init {

    private static Log log = LogFactory.getLog(init.class);

    public Response initCurrency(ChaincodeStub stub){

        log.info("****** Enter initCurrency ******");
        invoke ik = new invoke();

        ik.delete(stub,new String[]{"Currency"});

        util util = new util();
        KeyModificationImpl km = new KeyModificationImpl();

        ArrayList currency = new ArrayList();
        currency.add("CNY");
        currency.add(0);
        currency.add(0);
        currency.add("system");
        currency.add(km.getTimestamp());  //

        JSONObject change = util.getCurrencyJson(currency);
        if(change==null){
            log.error("Transfor got error");
            return newBadRequestResponse("Transfor got error");
        }
        currency.clear();
        currency.add("USD");
        currency.add(0);
        currency.add(0);
        currency.add("system");
        currency.add(km.getTimestamp());  //获取时间戳

        JSONObject change1 = util.getCurrencyJson(currency);
        if(change1==null){
            log.error("Transfor got error");
            return newBadRequestResponse("Transfor got error");
        }
        JSONArray newJ = new JSONArray();
        newJ.add(change);
        newJ.add(change1);
        stub.putStringState("Currency",newJ.toString());

        log.info("****** End initCurrency ******");


        return newSuccessReponse();

    }
}
