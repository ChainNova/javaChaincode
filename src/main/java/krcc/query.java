package krcc;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.sdk.shim.ChaincodeStub;

import javax.json.Json;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created by zerppen on 5/2/17.
 */
public class query {
    private static Log log = LogFactory.getLog(query.class);


    public Response query(ChaincodeStub stub, String[] args) {
        if(args.size()!=1){
            log.error("Incorrect number of arguments. Expecting: query");
            return newBadRequsetResponse("Incorrect number of arguments. Expecting: query");
        }
        String account = args.get(0).toString();
        stub.getParameters().stream().toArray(String[]::new);
        ArrayList a = new ArrayList();

        String [] strings = a.stream().toArray(new String[0]);

        return newSuccessReponse(Json.createObjectBuilder()
                .add("Name",account)
                .add("Amount",stub.getState(account))
                .build().toString().getBytes(UTF_8));

    }
    public Response getOwnerOneAsset(ChaincodeStub stub,String[] args){
        log.info("****** In getOwnerOneAsset ******");
        if (args.length != 1) {
            return newBadRequestResponse("Incorrect number of arguments. Expecting: query(account)");
        }


    }

    /*
      通过uuid返回TxLog2中对应JSON对象
     */
    public JSONObject getTxLogByID(ChaincodeStub stub, String uuid){

        String txLog2 = stub.getStringState("TxLog2");
        if(null==txLog2||"".equals(txLog2)){
            return null;
        }
        JSONArray txArr = JSONArray.fromObject(txLog2);
        util util = new util();
        JSONObject txJson = util.getTxJsonByID(txArr,uuid);
        if(txJson!=null||!"".equals(txJson)){
            return txJson;
        }else{
            return null;
        }

    }

    /*
      返回list中对应 AssetLockLog Json对象
     */
    public JSONObject getLockLog(ChaincodeStub stub,ArrayList list){

        log.info("******* in getLockLog ******");

        String txLog = stub.getStringState("AssetLockLog");
        if(null==txLog||"".equals(txLog)){
            return null;
        }
        JSONArray txArr = JSONArray.fromObject(txLog);
        util util = new util();
        JSONObject txJson = util.getTxJsonByArr(txArr,list);

        log.info("******* done getLockLog ******");

        if(txJson!=null||!"".equals(txJson)){
            return txJson;
        }else{
            return null;
        }




    }

    /*
      查询到TxLog JsonArray
     */
    public JSONArray getTXs(ChaincodeStub stub,String owner,
                            String srcCurrency,String desCurrency,String rawOrder){
        log.info("******* in getTXs ******");


        String txStr = stub.getStringState("TxLog");
        if(null == txStr||"".equals(txStr)){
            return null;
        }
        JSONArray txArr = JSONArray.fromObject(txStr);
        util util = new util();
        JSONArray txJsArr = util.getTxLogArr(txArr,owner,srcCurrency,desCurrency,rawOrder);

        log.info("******* done getTXs ******");

        if(txJsArr.isEmpty()||txJsArr == null||"".equals(txJsArr)){
            log.error("getTxLogArr error");
            return null;
        }else{
            return txJsArr;
        }


    }

    /*
      根据 ower/currency 查询Asset JsonObject
     */
    public JSONObject getOwnerOneAsset(String owner,String currency){

        log.info("******* in getOwnerOneAsset ******");

        String str = stub.getStringState("Assets");
        JSONArray jArr = JSONArray.fromObject(str);

        if(jArr.isEmpty()||jArr==null||"".equals(jArr)){
            log.error( "getOwnerOneAsset error1 ;"+ "Faild get row of id:"+currency);
            return null;
        }
        for(int i = 0 ;i<jArr.size();i++){
            if(owner.equals(jArr.getJSONObject(i).getString("Owner"))){
                if(currency.equals(jArr.getJSONObject(i).getString("Currency"))){
                    return jArr.getJSONObject(i);
                }
            }
        }



        log.info("******* done getOwnerOneAsset ******");
        return null;

    }

    public Response queryCurrencyByID(ChaincodeStub stub,String []args){

        log.info("******* in queryCurrencyByID ******");

        if(args.length!=1){
            return newBadRequsetResponse("Incorrect number of arguments. Expecting 1");
        }

        String id = args[0];
        String str = stub.getStringState("Assets");
        JSONArray jArr = JSONArray.fromObject(str);
        JSONObject json = new JSONObject();
        for(int i = 0;i<jArr.size();i++){
            if(id.equals(jArr.getJSONObject(i).getString("ID"))){

                json = jArr.getJSONObject(i);
            }
        }


        log.info("******* done queryCurrencyByID ******");

        return newSuccessResponse(json);
    }

    public Response queryAllCurrency(ChaincodeStub stub,String[] args){

        log.info("****** in queryAllCurrency ******");

        if(args.length!=0){
            log.error("incorrect number of arguments");
            return newBadRequsetResponse("incorrect number of arguments");
        }

        String jsonStr = stub.getStringState("Currency");

        log.info("****** done queryAllCurrency ******");

        return newSuccessResponse(jsonStr);
    }

    public Response queryTxLogs(ChaincodeStub stub,String[] args){

        log.info("****** in queryTxLogs ******");

        if(args.length!=0){
            log.error("incorrect number of arguments");
            return newBadRequsetResponse("incorrect number of arguments");
        }

        String jsonStr = stub.getStringState("TxLog2");

        log.info("****** done queryTxLogs ******");

        return newSuccessResponse(jsonStr);
    }

    public Response queryAssetByOwner(ChaincodeStub stub,String[] args){

        log.info("****** in queryAssetByOwner ******");

        if(args.length!=1){
            return newBadRequsetResponse("Incorrect number of aragument. Expecting 1");
        }

        String jsonStr = stub.getStringState("Assets");
        String owner = args[0];
        if(null == jsonStr ||"".equals(jsonStr)){

            return newBadRequsetResponse("No Assets can be queried. Expecting 1");
        }
        JSONArray jArr = JSONArray.fromObject(jsonStr);
        if(jArr.isEmpty()){

            return newBadRequsetResponse("No Assets can be queried. Expecting 2");

        }

        util ul = new util();
        JSONArray assOwner = ul.getTxJsonArrByID(jArr,owner);
        if(assOwner.isEmpty()||assOwner ==null || "".equals(assOwner)){

            log.error("Can't query Assets of owner:"+owner);
            return newBadRequsetResponse("No Assets can be queried. Expecting 3");
        }

        log.info("****** done queryAssetByOwner ******");


        return newSuccessResponse(assOwner.toString());
    }


}
