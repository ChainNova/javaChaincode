package krcc;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.protos.peer.FabricProposalResponse;
import org.hyperledger.fabric.sdk.shim.ChaincodeStub;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zerppen on 5/2/17.
 */
public class invoke {

    private static Log log = LogFactory.getLog(invoke.class);
    private static query qy = new query();



    public Response initAccount(ChaincodeStub stub,String[] args){

        log.info("****** In initAccount ******");
        if(args.length!=1){
            log.error("Incorrect number of arguments. Expecting: initAccount");
            return newBadRequsetResponse("Incorrect number of arguments. Expecting: initAccount");
        }


        log.info("****** Done run ******");



        return newSuccessResponse();
    }
    public Response create(ChaincodeStub stub,String[] args){

        log.info("****** Enter create ******");
        if(args.length!=3){
            log.error("Incorrect number of arguments. Expecting: create");
            return newBadRequsetResponse("Incorrect number of arguments. Expecting: create");
        }
        String id = args[0];
        long count = Long.parselong(args[1]);
        String creator = args[2];
        if(id.equals("CNY")||id.equals("USD")){
            log.error("Can't create "+id+",'cause "+id+" exists!");
            return newBadRequsetResponse("Incorrect creation type:"+id+" Expecting: create");
        }

        ArrayList currency = new ArrayList();
        KeyModificationImpl km = new KeyModificationImpl();
        util util = new util();
        currency.add(id);
        currency.add(count);
        currency.add(count);
        currency.add(creator);
        currency.add(km.getTimestamp());

        JSONObject change = util.getCurrencyJson(currency);
        if(change==null){
            log.error("Transfor got error");
            return newBadRequestResponse("Transfor got error");
        }
        JSONArray jacurr = JSONArray.fromObject(stub.getStringState("Currency"));
        if(jacurr.isEmpty()){
            log.error("Should initCurrency first");
            return newBadRequestResponse("Should initCurrency first");
        }
        jacurr.add(change);
        stub.putStringState("Currency",jacurr.toString());
        log.info("create currency successfully");

        if(count>0){
            saveReleaseLog(stub,currency);

        }


        log.info("****** ending create ******");




        return newSuccessResponse();
    }

    public Response release(ChaincodeStub stub,String[] args){

        log.info("****** Enter release ******");

        if(args.length!=2){
            log.error("Incorrect number of arguments. Expecting: create");
            return newBadRequsetResponse("Incorrect number of arguments. Expecting: create");
        }
        String id = args[0];
        long count = Long.parselong(args[1]);
        if(id.equals("CNY")||id.equals("USD")){
            log.error("Can't create "+id+",'cause "+id+" exists!");
            return newBadRequsetResponse("Incorrect creation type:"+id+" Expecting: release");
        }
        if(count<=0){
           String ret = "The currency release count must be > 0";
            log.error(ret);
            return newBadRequsetResponse(ret);
        }

        util util = new util();
        JSONArray jacurr = JSONArray.fromObject(stub.getStringState("Currency"));
        if(jacurr.isEmpty()){
            log.error("Should initCurrency first");
            return newBadRequestResponse("Should initCurrency first");
        }
        JSONObject jRelease = util.getCurJsonByID(jacurr,id);

        if(null==jRelease||"".equals(jRelease)||jRelease.isEmpty()){
            log.error("Incorrect currency of "+id+" ,no currency can be released. Expecting: release");
            return newBadRequsetResponse("No currency can be released. Expecting: release");
        }

        ArrayList currency = new ArrayList();
        KeyModificationImpl km = new KeyModificationImpl();

        count = count + jRelease.getLong("Count");
        long leftCount = count + jRelease.getLong("LeftCount");

        currency.add(id);
        currency.add(count);
        currency.add(leftCount);
        currency.add(jRelease.getString("Creator"));
        currency.add(km.getTimestamp());
        JSONObject reJson = util.getCurrencyJson(currency);

        JSONArray newJacurr = util.getCurJsonArr(jacurr,id,reJson);
        if(newJacurr.isEmpty()){
            log.error("Should initCurrency first");
            return newBadRequestResponse("Should initCurrency first");
        }

        stub.putStringState("Currency",newJacurr.toString());
        log.info("release currency successfully");

        saveReleaseLog(stub,currency);


        log.info("****** ending release ******");

        return newSuccessResponse();
    }

    private Response saveReleaseLog(ChaincodeStub stub,ArrayList list){
        log.info("*** in saveReleaseLog ***");

        util util = new util();
        JSONObject change = util.getCurLogJson(list);

        JSONArray log = JSONArray.fromObject(stub.getStringState("CurrencyReleaseLog"));
        if(null==log||"".equals(log)||log.isEmpty()){
            JSONArray newJ = new JSONArray();
            newJ.add(change);
            stub.putStringState("CurrencyReleaseLog",newJ.toString());

        }else{
            log.add(change);
            stub.putStringState("CurrencyReleaseLog",log.toString());

        }

        log.info("*** done saveReleaseLog ***");

        return newSuccessResponse();

    }

    public Response assign(ChaincodeStub stub,String[] args){

        log.info("*** In  Assign Currency ***");

        String ret = null;
        if((args.length)!=1){
            ret = "Incorrect number of arguments. Expecting 3,in release currency";
            log.error(ret);
            return newBadRequestResponse(ret);
        }
        String currency = null;
        JSONObject jobj = JSONObject.fromObject(args[0]);
        if(jobj.has("currency")&&jobj.get("currency")!=null){
            currency = jobj.getString("currency");
        }else{
            log.error("error1 can't get assign's currency");
            return newBadRequestResponse("can't get assign's currency");
        }

        JSONArray jarray;
        if(jobj.has("assigns")){
            jarray = jobj.getJSONArray("assigns");
        }else {
            log.error("error2 can't get assign's assigns");
            return newBadRequestResponse("Invalid assign data");
        }

        util util = new util();
        JSONArray jacurr = JSONArray.fromObject(stub.getStringState("Currency"));
        if(jacurr.isEmpty()||jacurr==null||jacurr.equals("")){
            log.error("Should initCurrency first");
            return newBadRequestResponse("Should initCurrency first");
        }
        JSONObject jcurrency = util.getCurJsonByID(jacurr,currency);

        if(null==jcurrency||"".equals(jcurrency)||jcurrency.isEmpty()){
            log.error("Incorrect currency of "+currency+" ,no currency can be released. Expecting: release");
            return newBadRequsetResponse("No currency can be released. Expecting: release");
        }

        String creator = jcurrency.getString("Creator");
        long leftCount = jcurrency.getLong("LeftCount");

        long assignCount = 0;
        for(int j =0;j<jarray.size();j++){

            long inCount = Long.parseLong(jarray.getJSONObject(j).get("count").toString());
            if(inCount<=0)
                continue;

            assignCount += inCount;
            if(assignCount>leftCount){

                ret = "The left count:"+leftCount+" of currency:"+assignCount+
                        " is insufficient";
                log.error("error4: "+ret);
                return newBadRequsetResponse(ret);
            }
        }

        for(int k=0;k<jarray.size();k++ ){

            String owner = jarray.getJSONObject(k).getString("owner");
            long count = Long.parseLong(jarray.getJSONObject(k).get("count").toString());
            if(owner==null||owner.equals(" ")){
                ret = "Failed decodinfo owner";
                log.error("error5: "+ret);
                return newBadRequsetResponse(ret);
            }
            if(count<=0)
                continue;

            ArrayList list = new ArrayList();
            KeyModificationImpl km = new KeyModificationImpl();
            list.add(currency);
            list.add(owner);
            list.add(count);
            list.add(km.getTimestamp());
            Response save = saveAssignLog(stub,list);

            if(save!=null){
                ret = "error 6:"+save;
                return newBadRequsetResponse(ret);
            }

            JSONArray asset = JSONArray.fromObject(stub.getStringState("Assets"));

            list.clear();
            list.add(owner);
            list.add(currency);
            list.add(count);
            list.add(0);

            JSONObject assetJ = util.getAssetJson(list);
            JSONObject curJson = util.getCurJsonByID(asset,currency);

            if(null==asset||"".equals(asset)||asset.isEmpty()){
                ret = "Establish new asset of id:"+currency;
                log.info(ret);
                JSONArray newJ = new JSONArray();
                newJ.add(assetJ);
                stub.putStringState("Assets",newJ.toString());

            }else if(curJson==null){
                log.info("****** Insert assets ******");
                asset.add(assetJ);
                stub.putStringState("Assets",asset.toString());

            } else{

                log.info("****** Replace assets of currency :"+currency+" ******");

                long asset_count = curJson.getLong("Count");
                long asset_lockcount = curJson.getLong("LockCount");
                list.clear();
                list.add(owner);
                list.add(creator);
                list.add(asset_count+count);
                list.add(asset_lockcount);
                JSONObject repAssJ = util.getAssetJson(list);
                JSONArray newAsset = util.getCurJsonArr(asset,currency,repAssJ);
                stub.putStringState("Assets",newAsset.toString());


            }

            leftCount -= count;
        }
        if(leftCount!=jcurrency.getLong("LeftCount")){
            log.info("****** Update   Currency ******");


            ArrayList nList = new ArrayList();
            nList.add(currency);
            nList.add(jcurrency.getString("Count"));
            nList.add(leftCount);
            nList.add(creator);
            nList.add(jcurrency.getLong("CreateTime"));
            JSONObject nCurr = util.getCurrencyJson(nList);
            JSONArray newJacurr = util.getCurJsonArr(jacurr,currency,nCurr);
            stub.putStringState("Currency",newJacurr.toString());

            log.info("****** Done Update   Currency ******");


        }


        log.info("****** Done  Assign Currency ******");

        return newSuccessResponse();
    }

    private Response saveAssignLog(ChaincodeStub stub,ArrayList list){
        log.info("*** in saveAssignLog ***");

        util util = new util();
        JSONObject change = util.getAssignLogJson(list);

        JSONArray log = JSONArray.fromObject(stub.getStringState("CurrencyAssignLog"));
        if(null==log||"".equals(log)||log.isEmpty()){
            JSONArray newJ = new JSONArray();
            newJ.add(change);
            stub.putStringState("CurrencyAssignLog",newJ.toString());

        }else{
            log.add(change);
            stub.putStringState("CurrencyAssignLog",log.toString());

        }

        log.info("*** done saveAssignLog ***");

        return newSuccessResponse();

    }

    public Response exchange(ChaincodeStub stub, String[] args){

        log.info("****** In  Exchange ******");

        String ret = null;

        if(args.length!=1){
            ret = "Incorrect number of arguments. Exception 2";
            log.error(ret);
            return newBadRequsetResponse(ret);
        }

        JSONArray jsonArray = JSONArray.fromObject(args[0]);
        if(jsonArray.isEmpty()||jsonArray.size()==0||jsonArray==null){
            log.error("exchange error1");
            return newBadRequsetResponse("args invalid..");
        }

        Map successIn = new HashMap();
        Map failIn    = new HashMap();
        for(int i = 0;i<jsonArray.size();i++){
            JSONObject buyOrder = JSONObject.fromObject(jsonArray.getJSONObject(i).get("buyOrder"));
            JSONObject sellOrder = JSONObject.fromObject(jsonArray.getJSONObject(i).get("sellOrder"));

            String matchOrder = buyOrder.getString("uuid")+","+sellOrder.getString("uuid");

            if(!buyOrder.getString("srcCurrency").equals(sellOrder.getString("desCurrency"))
                    ||!buyOrder.getString("desCurrency").equals(sellOrder.getString("srcCurrency"))){
                ret = "The exchange is invalid";
                log.error(ret);
                return newBadRequsetResponse(ret);
            }

            JSONObject buyRow = qy.getTxLogByID(stub,buyOrder.getString("uuid"));
            JSONObject sellRow = qy.getTxLogByID(stub,sellOrder.getString("uuid"));

            if(null!=buyRow||sellRow!=null){
                log.error("exchange error2");
                failIn.put(matchOrder,"exchange error2");

                continue;
            }
            if("".equals(buyRow)||"".equals(sellRow)){
                log.error("exchange error3");
                failIn.put(matchOrder,"exchange error3");

                continue;
            }
            String retETx = execTx(stub,buyOrder,sellOrder);
            if(retETx != null){
                log.error("exchange error3");
                if(retETx.equals("-1")){
                    failIn.put(matchOrder,"exchange error3");

                    continue;
                }else{
                    log.error("exchange error4");
                    return  ret;
                }
            }
            String retSTL = saveTxLog(stub,buyOrder,sellOrder);
            if(retSTL!=null){
                log.error("exchange error5");
                return newBadRequsetResponse("exchange error5");
            }
            successIn.put(i,matchOrder);

        }

        Map allm = new HashMap();
        allm.put("success",successIn);
        allm.put("fail",failIn);
        byte []allb = allm.toString().getBytes();

        stub.setEvent("chaincode_exchange",allb);





        return newSuccessResponse();
    }

    public String execTx(ChaincodeStub stub,JSONObject jbuy,JSONObject jsell){

        log.info("****** in execTx ******");

        String ret = null;
        String bAccount = jbuy.getString("account");        //账户
        String bSrcCurrency = jbuy.getString("srcCurrency");//源币种代码
        String bDesCurrency = jbuy.getString("desCurrency");//目标币种代码
        String bRawUUID = jbuy.getString("rawUUID");        //母单UUID
        long bDesCount =jbuy.getLong("desCount");           //目标币种交易数量
        long bSrcCount =jbuy.getLong("srcCount");           //源币种交易数量
        boolean bBuyAll = jbuy.getBoolean("isBuyAll");      //是否买入所有，即为true是以目标币全部兑完为主,否则算部分成交,买完为止；为false则是以源币全部兑完为主,否则算部分成交，卖完为止
        long bExpiredTime = jbuy.getLong("expiredTime");    //超时时间
        long bPendingTime = jbuy.getLong("PendingTime");    //挂单时间
        long bPendedTime = jbuy.getLong("PendedTime");      //挂单完成时间
        long bMatchedTime = jbuy.getLong("matchedTime");    //撮合完成时间
        long bFinishedTime = jbuy.getLong("finishedTime");  //交易完成时间
        String bMetadata = jbuy.getString("metadata");      //存放其他数据，如挂单锁定失败信息
        long bFinalCost = jbuy.getLong("FinalCost");        //源币的最终消耗数量，主要用于买完（IsBuyAll=true）的最后一笔交易计算结余，此时SrcCount有可能大于FinalCost

        String sAccount = jsell.getString("account");        //账户
        String sSrcCurrency = jsell.getString("srcCurrency");//源币种代码
        String sDesCurrency = jsell.getString("desCurrency");//目标币种代码
        String sRawUUID = jsell.getString("rawUUID");        //母单UUID
        long sDesCount =jsell.getLong("desCount");           //目标币种交易数量
        long sSrcCount =jsell.getLong("srcCount");           //源币种交易数量
        boolean sBuyAll = jsell.getBoolean("isBuyAll");      //是否买入所有，即为true是以目标币全部兑完为主,否则算部分成交,买完为止；为false则是以源币全部兑完为主,否则算部分成交，卖完为止
        long sExpiredTime = jsell.getLong("expiredTime");    //超时时间
        long sPendingTime = jsell.getLong("PendingTime");    //挂单时间
        long sPendedTime = jsell.getLong("PendedTime");      //挂单完成时间
        long sMatchedTime = jsell.getLong("matchedTime");    //撮合完成时间
        long sFinishedTime = jsell.getLong("finishedTime");  //交易完成时间
        String sMetadata = jsell.getString("metadata");      //存放其他数据，如挂单锁定失败信息
        long sFinalCost = jbuy.getLong("FinalCost");        //源币的最终消耗数量，主要用于买完（IsBuyAll=true）的最后一笔交易计算结余，此时SrcCount有可能大于FinalCost

        // 挂单UUID等于原始ID时表示该单交易完成
        if(bBuyAll && jbuy.get("uuid")==bRawUUID) {

            long lockCount = computeBalance(stub, bAccount, bSrcCurrency,
                    bDesCurrency, bRawUUID, bFinalCost);
            if(lockCount == -1){
                log.error("execTx error1");
                return "-1";
            }

            log.info("Order " + jbuy.getString("uuid") + " balance " + lockCount);

            if (lockCount > 0) {
                String check = lockOrUnlockBalance(stub, bAccount, bSrcCurrency,
                        bRawUUID, lockCount, false);
                if (check != null) {
                    log.error("execTx error2 "+check);
                    return "Failed unlock balance";
                }

            }
        }
        JSONObject buySrcRow = qy.getOwnerOneAsset(bAccount,bSrcCurrency);
        if(buySrcRow == null || "".equals(buySrcRow)){
            log.error("execTx error3");
            return "-1 ";
        }


        String replaceBuySrc = replaceAsset(stub,buySrcRow.getString("Owner"),
                buySrcRow.getString("Currency"),buySrcRow.getLong("Count"),
                buySrcRow.getLong("LockCount")-bFinalCost);
        if(replaceBuySrc!=null){
            ret = "replace owner:"+buySrcRow.getString("Owner")+"'s currency:"+buySrcRow.getString("Currency")+" failed";
            log.error(ret);
            return ret;
        }

        JSONObject buyDesRow = qy.getOwnerOneAsset(bAccount,bDesCurrency);


        if(buyDesRow == null){

            String insertBuyDes = insertAsset(stub,bAccount,bDesCurrency,bDesCount);
            if(insertBuyDes!=null){
                ret = "insert owner:"+bAccount+"'s currency:"+bDesCurrency+" failed";
                log.error(ret);
                return ret;
            }
        }else{

            String replaceBuyDes = replaceAsset(stub,buyDesRow.getString("Owner"),
                    buyDesRow.getString("Currency"),buyDesRow.getLong("Count")+bDesCount,
                    buyDesRow.getLong("LeftCount"));
            if(replaceBuyDes!=null){
                ret = "replace owner:"+buyDesRow.getString("Owner")+"'s currency:"+buyDesRow.getString("Currency")+" failed";
                log.error(ret);
                return ret;
            }
        }
        if(sBuyAll && jsell.get("uuid")==sRawUUID){

            long unlockCount = computeBalance(stub,sAccount,sSrcCurrency,
                    sDesCurrency,sRawUUID,sFinalCost);
            if(unlockCount>0){
                String check = lockOrUnlockBalance(stub,sAccount,sSrcCurrency,
                        sRawUUID,unlockCount,false);
                log.debug("Order "+jsell.getString("uuid")+" balance "+unlockCount);
                if(check!=null){
                    log.error("execTx error9");
                    return "-1";
                }

            }

        }

        JSONObject sellSrcRow = qy.getOwnerOneAsset(sAccount,sSrcCurrency);
        if(sellSrcRow == null || "".equals(sellSrcRow)){
            log.error("execTx error10");
            return "the user have not currency "+sSrcCurrency;
        }


        String replaceSellSrc = replaceAsset(stub,sellSrcRow.getString("Owner"),
                sellSrcRow.getString("Currency"),sellSrcRow.getLong("Count"),
                sellSrcRow.getLong("LeftCount")-sFinalCost);
        if(replaceSellSrc!=null){
            ret = "replace owner:"+sellSrcRow.getString("Owner")+"'s currency:"+sellSrcRow.getString("Currency")+" failed";
            log.error(ret);
            return ret;
        }



        JSONObject sellDesRow =  qy.getOwnerOneAsset(sAccount,sDesCurrency);
        if("".equals(sellDesRow) ){
            log.error("execTx error12");
            return "Faild retrieving asset "+sDesCurrency;
        }

        if(sellDesRow == null){

            String insertSellDes = insertAsset(stub,sAccount,sDesCurrency,sDesCount);
            if(insertSellDes!=null){
                ret = "insert owner:"+sAccount+"'s currency:"+sDesCurrency+" failed";
                log.error(ret);
                return ret;
            }

        }else{

            String replacesellDes = replaceAsset(stub,sellDesRow.getString("Owner"),
                    sellDesRow.getString("Currency"),sellDesRow.getLong("Count")+sDesCount,
                    buyDesRow.getLong("LeftCount"));
            if(replacesellDes!=null){
                ret = "replace owner:"+sellDesRow.getString("Owner")+"'s currency:"+sellDesRow.getString("Currency")+" failed";
                log.error(ret);
                return ret;
            }


        }
        log.info("****** done execTx ******");


        return ret;
    }

    public long computeBalance(ChaincodeStub stub,String owner,String srcCurrency,
                               String desCurrency,String rawUUID,long currentCost){
        log.info("****** in computeBalance ******");
        long ret=0;
        ArrayList lock = new ArrayList();
        lock.add(owner);
        lock.add(srcCurrency);
        lock.add(rawUUID);
        lock.add(true);
        JSONObject logRow =     qy.getLockLog(stub,lock);

        if(logRow == null||"".equals(logRow)){
            log.error("get locklog faild");
            return -1;
        }
        long sumcost = 0;
        synchronized (this){
            JSONArray txRows = qy.getTXs(stub,owner,srcCurrency,desCurrency,rawUUID);
            for(int i = 0 ;i<txRows.size();i++){
                JSONObject row = txRows.getJSONObject(i);
                JSONObject jobj = JSONObject.fromObject(row.getString("Detail"));
                sumcost += jobj.getLong("FinalCost");
            }

        }

        long lockCount = logRow.getLong("LockCount");

        log.info("****** done computeBalance ******");

        return lockCount-sumcost-currentCost;
    }

    public  String lockOrUnlockBalance(ChaincodeStub stub, String owner,String currency,
                                       String order,long count,boolean islock){

        log.info("*** in lockOrUnlockBalance ***");

        String ret = null;

        JSONObject row = qy.getOwnerOneAsset(owner,currency);
        if(row == null){
            log.error( "lockOrUnlockBalance error1 ;"+ "Faild get row of id:"+currency);
            return "-1";
        }
        log.info("getOwnerOneAsset row:"+row.toString());

        long currencyCount = row.getLong("Count");
        long currencyLockCount = row.getLong("LockCount");

        log.info("Have count:"+currencyCount+"; have lockCount:"+currencyLockCount+" ;islock:"+islock);

        if((islock && (currencyCount<count?true:false))||(!islock && (currencyLockCount<count?true:false))){
            log.error("Currency  or locked Currency " +currency+ "  of the user is insufficient ");
            return "-1";
        }

        ArrayList list = new ArrayList();
        list.add(owner);
        list.add(currency);
        list.add(order);
        list.add(islock);

        JSONObject lockRow = qy.getLockLog(stub,list);
        if(lockRow == null ||"".equals(lockRow)){
            log.error("lockOrUnlockBalance error2");
            return " -1";
        }

        if ( islock){

            currencyCount -= count;
            currencyLockCount += count;
        }else{

            currencyCount += count;
            currencyLockCount -= count;
        }


        String replace = replaceAsset(stub,owner,currency,currencyCount,currencyLockCount);
        if(replace!=null){
            log.error( "lockOrUnlockBalance error3 "+replace);
            return "-2";
        }

        KeyModificationImpl km = new KeyModificationImpl();
        list.clear();
        list.add(owner);
        list.add(currency);
        list.add(order);
        list.add(islock);
        list.add(count);
        list.add(km.getTimestamp());

        util ul = new util();
        JSONObject assLockLog = ul.getAssLockLog(list);

        String assLL = stub.getStringState("AssetLockLog");
        if(null == assLL||"".equals(assLL)){

            JSONArray jArr = new JSONArray();
            jArr.add(assLockLog);
            stub.putStringState("AssetLockLog",jArr);
        }else{
            JSONArray jArr = JSONArray.fromObject(assLL);
            jArr.add(assLockLog);
            stub.putStringState("AssetLockLog",jArr);
        }

        log.info("*** done lockOrUnlockBalance ***");

        return ret;
    }

    public String replaceAsset(ChaincodeStub stub,String owner,String currency,long count,long leftCount){

        log.info("*** in replaceAsset ***");

        ArrayList list = new ArrayList();
        list.add(owner);
        list.add(currency);
        list.add(count);
        list.add(leftCount);

        util ul = new util();
        JSONObject json = ul.getAssetJson(list);

        String assStr = stub.getStringState("Assets");
        JSONArray assArr = JSONArray.fromObject(assStr);

        JSONArray nAssArr = ul.getAssJsonArr(assArr,owner,currency,json);

        if(nAssArr.isEmpty()||null == nAssArr||"".equals(nAssArr)){

            log.error("Failed to replace Asset of owner:"+owner);

            return "-1";
        }


        log.info("*** Done replaceAsset ***");

        return null;

    }

    private String insertAsset(ChaincodeStub stub,String owner,String currency,long count){

        log.info("*** in insertAsset ***");

        ArrayList list = new ArrayList();
        list.add(owner);
        list.add(currency);
        list.add(count);
        list.add(0);

        util ul = new util();
        JSONObject json = ul.getAssetJson(list);

        String assStr = stub.getStringState("Assets");
        JSONArray assArr = JSONArray.fromObject(assStr);
        if(assArr.isEmpty()||assArr ==null){
            log.error("Can't get state : Assets");
            return "-1";
        }else{
            assArr.add(json);
            stub.putStringState("Assets",assArr);
        }

        log.info("*** done insertAsset ***");


        return null;
    }

    public Response lock(ChaincodeStub stub, String[] args){

        log.info("****** In  lock ******");

        if(args.length!=3){
            return newBadRequsetResponse("Incorrect number of arguments. Excepcting 3");
        }
        //String owner,currency,orderId;
        // long count;

        JSONArray jsonArray = JSONArray.fromObject(args[0]);
        boolean islock = Boolean.parseBoolean(args[1]);
        log.info("In  lock .....args[1]= %s"+args[0]+";islock="+islock);
        Map successIn = new HashMap();
        Map failIn    = new HashMap();
        for(int j =0;j<jsonArray.size();j++){
            String owner = jsonArray.getJSONObject(j).getString("owner");
            String currency = jsonArray.getJSONObject(j).getString("currency");
            String orderId = jsonArray.getJSONObject(j).getString("orderId");
            long count = Long.parseLong(jsonArray.getJSONObject(j).getString("count"));


            if(owner == null){
                log.error("lock error2");
                failIn.put(orderId,"Failed decodinf owner");
                continue;
            }
            String ret_loub = lockOrUnlockBalance(stub,owner,currency,orderId,count,islock);
            if(ret_loub!=null){
                if(ret_loub.equals("-1")){
                    failIn.put(orderId,ret_loub);
                    continue;
                }else{
                    log.error("lock error3");
                    return null;
                }

            }
            successIn.put(j,orderId);

        }
        Map allm = new HashMap();
        allm.put("success",successIn);
        allm.put("fail",failIn);
        allm.put("SrcMethod",args[2]);
        byte[] allb = allm.toString().getBytes();

        stub.setEvent("chaincode_lock",allb);
        log.info("****** Done  lock ******");

        return newSuccessResponse();
    }


    public Response delete(ChaincodeStub stub,String[] args){
        if(args.size()!=1){
            log.error("Incorrect number of arguments. Expecting: delete(account)");
            return newBadRequsetResponse("Incorrect number of arguments. Expecting: delete(account)");
        }
        String account = args.get(0);
        stub.delState(account);

        return newSuccessResponse();
    }
}
