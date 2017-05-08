package krcc;

import static org.hyperledger.fabric.shim.ChaincodeHelper.newBadRequestResponse;
import static org.hyperledger.fabric.shim.ChaincodeHelper.newInternalServerErrorResponse;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.protos.peer.ProposalResponsePackage.Response;
import org.hyperledger.fabric.sdk.shim.ChaincodeBase;
import org.hyperledger.fabric.sdk.shim.ChaincodeStub;

import java.util.List;

/**
 * Created by zerppen on 5/2/17.
 */
public class ChainNavo extends ChaincodeBase{

    private static Log log = LogFactory.getLog(init.class);
    private init it = new init();

    @Override
    public Response init(ChaincodeStub stub){
        log.info("****** In Init ******");
        try{
            final List<String> args = stub.getArgAsStrings();
            switch(args.get(0)){
                case "init":
                    return it.initCurrency(stub);
                default:
                    return newBadRequestResponse(format("Unknow function: %s", args.get(0)));
            }
        }catch(NumberFormatException e){
            log.error(e.toString());
            return newBadRequestResponse(e.toString());
        }catch(IllegalArgumentException E){
            log.error(E.toString());
            return newBadRequestResonse(E.toString());
        }catch(Throwable e){
            log.error(e.toString());
            return newInternalServerErrorResponse(e);
        }
    }

    @Override
    public Response invoke(ChaincodeStub stub){
        log.info("****** In invoke ******");
        try{
            final String function = stub.getFunction();
            final String[] args = stub.getParameters().stream().toArray(String[]::new);
            //final String[] args = arglist.stream().skip(1).toArray(String[]::new );

            invoke ik = new invoke();
            query qy = new query();
            switch(function){
                case "initAccount":
                    return ik.initAccount(stub,args);
                case "create":
                    return ik.create(stub,args);
                    //break;
                case "releaseCurrency":
                    return ik.release(stub,args);
                    //break;
                case "assignCurrency":
                    return ik.assign(stub,args);
                    //break;
                case "exchange":
                    return ik.exchange(stub,args);
                    //break;
                case "lock":
                    return ik.lock(stub,args);
                   // break;

                case "delete":
                    return ik.delete(stub,args);
                case "queryCurrencyByID":
                    return qy.queryCurrencyByID(stub, args);
                case "queryAllCurrency":
                    return qy.queryAllCurrency(stub, args);
                case "queryTxLogs":
                    return qy.queryTxLogs(stub, args);
                case "queryAssetByOwner":
                    return qy.queryAssetByOwner(stub, args);
                default:
                    return newBadRequsetResponse(format("Unknow function: %s",function));
            }
        }catch (NumberFormatException e) {
            return newBadRequestResponse(e.toString());
        } catch (IllegalArgumentException e) {
            return newBadRequestResponse(e.getMessage());
        } catch (Throwable e) {
            return newInternalServerErrorResponse(e);
        }
    }


    @Override
    public String getChaincodeID(){
        return "ChainNova";
    }

    public static void main(String[] args) throws Exception {


        log.info("starting");
        new ChainNavo().start(args);
    }

}
