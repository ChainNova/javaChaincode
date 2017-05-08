package krcc;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;

/**
 * Created by zerppen on 5/2/17.
 */
public class util {

    private static Log log = LogFactory.getLog(util.class);
    private final java.time.Instant timestamp;

    public java.time.Instant getTimestamp() {
        return timestamp;
    }

    //init operation

    public JSONObject getCurrencyJson(ArrayList list){

        if(list.size()<=5){
            log.error("Incorrect number of arguments. Expecting: invoke");
            return null;
        }
        //JSONObject jK = new JSONObject();
        JSONObject arrayK1 = new JSONObject();
        JSONObject arrayK2 = new JSONObject();
        JSONObject arrayK3 = new JSONObject();
        JSONObject arrayK4 = new JSONObject();
        JSONObject arrayK5 = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        arrayK1.put("ID",list.get(0));
        arrayK1.put("Count",list.get(1));
        arrayK1.put("LeftCount",list.get(2));
        arrayK1.put("Creator",list.get(3));
        arrayK1.put("CreateTime",list.get(4));
//        jsonArray.add(arrayK1);
//        jsonArray.add(arrayK2);
//        jsonArray.add(arrayK3);
//        jsonArray.add(arrayK4);
//        jsonArray.add(arrayK5);
       // jK.put(list.get(0),jsonArray);

        return arrayK1;
    }

    public JSONObject getAssLockLog(ArrayList list){

        if(list.size()<6){
            log.error("Incorrect number of arguments. Expecting: invoke");
            return null;
        }
        JSONObject json = new JSONObject();
        json.put("Owner",list.get(0));
        json.put("Currency",list.get(1));
        json.put("Order",list.get(2));
        json.put("IsLock",list.get(3));
        json.put("LockCount",list.get(4));
        json.put("LockTime",list.get(5));

        return json;
    }

    public JSONObject getCurLogJson(ArrayList list){

        if(list.size()<5){
            log.error("Incorrect number of arguments. Expecting: invoke");
            return null;
        }
        //JSONObject jK = new JSONObject();
        JSONObject arrayK1 = new JSONObject();


        arrayK1.put("Currency",list.get(0));
        arrayK1.put("Count",list.get(1));
        arrayK1.put("CreateTime",list.get(4));
//        jsonArray.add(arrayK1);
//        jsonArray.add(arrayK2);
//        jsonArray.add(arrayK3);
//        jsonArray.add(arrayK4);
//        jsonArray.add(arrayK5);
        // jK.put(list.get(0),jsonArray);

        return arrayK1;
    }

    public JSONObject getAssignLogJson(ArrayList list){
        if(list.size()<4){
            log.error("Incorrect number of arguments. Expecting: invoke");
            return null;
        }
        JSONObject arrayK1 = new JSONObject();

        arrayK1.put("Currency",list.get(0));
        arrayK1.put("Owner",list.get(1));
        arrayK1.put("Count",list.get(2));
        arrayK1.put("AssignTime",list.get(3));
        return arrayK1;
    }

    /*
     通过账户及所有账户JSONARRAY，返回该账户对应jsonObject
     */
    public JSONObject getCurJsonByID(JSONArray jar,String id){
        for(int i = 0;i<jar.size();i++){
            if(id.equals(jar.getJSONObject(i).getString("Currency"))){
                return jar.getJSONObject(i);
            }
        }
        return null;
    }

    /*
    通过账户及所有账户JSONARRAY，返回该账户对应jsonObject
    */
    public JSONObject getTxJsonByID(JSONArray jar,String id){
        for(int i = 0;i<jar.size();i++){
            if(id.equals(jar.getJSONObject(i).getString("uuid"))){
                return jar.getJSONObject(i);
            }
        }
        return null;
    }

    /*
    通过账户及所有账户JSONARRAY，返回该账户对应jsonArr
    */
    public JSONArray getTxJsonArrByID(JSONArray jar,String id){
        JSONArray jArr = new JSONArray();
        for(int i = 0;i<jar.size();i++){
            if(id.equals(jar.getJSONObject(i).getString("Owner"))){
                jArr.add(jar.getJSONObject(i));
            }
        }
        return jArr;
    }
    /*
      构造Asset Json对象
     */
    public JSONObject getAssetJson(ArrayList list){
        if(list.size()<4){
            log.error("Incorrect number of arguments. Expecting: invoke");
            return null;
        }
        JSONObject arrayK1 = new JSONObject();

        arrayK1.put("Currency",list.get(0));
        arrayK1.put("Owner",list.get(1));
        arrayK1.put("Count",list.get(2));
        arrayK1.put("LockCount",list.get(3));
        return arrayK1;
    }

    /*
    将原有JSONArray，替换jsonObject中key为id为新JSONArray
     */
    public JSONArray getCurJsonArr(JSONArray jar,String id,JSONObject json){
        JSONArray nJar = new JSONArray();
        for(int i = 0;i<jar.size();i++){
            if(id.equals(jar.getJSONObject(i).getString("Currency"))){
                nJar.add(json);
            }else{
                nJar.add(jar.getJSONObject(i));
            }
        }
        return nJar;
    }

    /*
    将原有JSONArray，替换jsonObject中key为id为新JSONArray
     */
    public JSONArray getAssJsonArr(JSONArray jar,String ower,String currency,JSONObject json){

        JSONArray nJar = new JSONArray();
        for(int i = 0;i<jar.size();i++){
            if(ower.equals(jar.getJSONObject(i).getString("Owner"))
                    && currency.equals(jar.getJSONObject(i).getString("Currency"))){

                nJar.add(json);

            }else{
                nJar.add(jar.getJSONObject(i));
            }
        }
        return nJar;
    }


    /*
      根据list匹配AssetTxLog
     */
    public JSONObject getTxJsonByArr(JSONArray jar,ArrayList list){

        for(int i = 0;i<jar.size();i++){
            if(list.get(0).equals(jar.getJSONObject(i).getString("Owner"))){
                if(list.get(1).equals(jar.getJSONObject(i).getString("Currency"))){
                    if(list.get(2).equals(jar.getJSONObject(i).getString("Order"))){
                        if(list.get(3).equals(jar.getJSONObject(i).getBoolean("IsLock"))){

                            return jar.getJSONObject(i);

                        }

                    }

                }
            }
        }
        return null;
    }


    /*
      根据list匹配AssetTxLog
     */
    public JSONObject getAssJsonArrByArr(JSONArray jar,ArrayList list){

        for(int i = 0;i<jar.size();i++){
            if(list.get(0).equals(jar.getJSONObject(i).getString("Owner"))){
                if(list.get(1).equals(jar.getJSONObject(i).getString("Currency"))){
                    if(list.get(2).equals(jar.getJSONObject(i).getString("Order"))){
                        if(list.get(3).equals(jar.getJSONObject(i).getBoolean("IsLock"))){

                            return jar.getJSONObject(i);

                        }

                    }

                }
            }
        }
        return null;
    }
    /*
      匹配获得TxLog JSONArray
     */
    public JSONArray getTxLogArr(JSONArray jar,String owner,
                                 String srcCurrency,String desCurrency,String rawOrder){
        JSONArray jsonArray = new JSONArray();
        for(int i = 0;i<jar.size();i++){
            if(owner.equals(jar.getJSONObject(i).getBoolean("Owner"))){

                if(srcCurrency.equals(jar.getJSONObject(i).getBoolean("SrcCurrency"))){

                    if(desCurrency.equals(jar.getJSONObject(i).getBoolean("DesCurrency"))){

                        if(rawOrder.equals(jar.getJSONObject(i).getBoolean("RawOrder"))){

                            jsonArray.add(jar.getJSONObject(i)) ;

                        }

                    }

                }


            }


        }
        return jsonArray;
    }





    /*
    将原有账户日志Array，替换为新账户日志Array
     */
    public JSONArray getCurJsonLogArr(JSONArray jar,String id,ArrayList list){
        JSONArray nJar = new JSONArray();
        for(int i = 0;i<jar.size();i++){
            if(id.equals(jar.getJSONObject(i).get("CurrencyReleaseLog"))){
                JSONObject json = getCurrencyJson(list);
                nJar.add(json);
            }else{
                nJar.add(jar.getJSONObject(i));
            }
        }
        return nJar;
    }

}
