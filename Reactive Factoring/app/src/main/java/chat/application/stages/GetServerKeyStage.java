package chat.application.stages;

import android.util.Log;


import java.security.PublicKey;

import chat.application.server.Crypto;
import chat.application.server.WebHelper;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by kbaldor on 7/28/16.
 */
public class GetServerKeyStage implements Func1<Integer, Observable<PublicKey>> {

    final String server;

    public GetServerKeyStage(String server) {
        this.server = server;
    }

    @Override
    public Observable<PublicKey> call(Integer unused)  {
        try {
            String response = WebHelper.StringGet(this.server+"/get-key");
            Log.d("GetServerKeyStage",response);
            return Observable.just(Crypto.getPublicKeyFromString(response));
        } catch (Exception e) {
            e.printStackTrace();
            return Observable.error(e);
        }
    }
}

