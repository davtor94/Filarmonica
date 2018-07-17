package mx.com.filarmonica;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by root on 19/01/15.
 * Actualized by david torres on 06/03/18
 */
public class GetDataFacebook extends AsyncTask<Void,Void,ArrayList<ItemFacebook>>
{

    private RecyclerView recyclerView;
    private Context contexto;
    private SwipeRefreshLayout swipeRefreshLayout;
    ArrayList<ItemFacebook> arrayFacebook;

    //Constructor que recibirá el contexto y el RecyclerView en donde se insertarán los estados
    // de Facebook.
    public GetDataFacebook(RecyclerView recyclerView, SwipeRefreshLayout swipeRefreshLayout,
                          Context contexto, ArrayList<ItemFacebook> arrayFacebook)
    {
        this.recyclerView       = recyclerView;
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.contexto           = contexto;
        this.arrayFacebook      = arrayFacebook;
    }

    public GetDataFacebook(){}

    @Override
    protected ArrayList<ItemFacebook> doInBackground(Void... params) {

        ArrayList<ItemFacebook> publicaciones = new ArrayList<ItemFacebook>();
        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        final String link = "https://graph.facebook.com/v2.12/180233478684363?fields=feed%7Bcreated_time%2Cid%2Cfull_picture%2Cmessage%7D&access_token=EAAL9NzDszg0BADkh4FL90ycaSSDDYBIpyzFeo9V9F6jeYkNsR1AUUzpEX3NsprACJAMjl133m64DFR5cweF8mrpLe7u64Wp5s3I4UAP2HgT74Wi7ZCrO9W3jXO7PdIWT0apLLsnkkOjONWFlKGLtgPBzK25hoX3kMPSYL1wZDZD";


                //"https://graph.facebook.com/208358432538534/photos/" +
                //"feed?access_token=CAAL9NzDszg0BAHuHsB22GraYSgRiKUyoILz7JZAVHb8ZBkF1GuOGgoY0J" +
                //"IjAt4aZB4jqnyJrcHtxz0Tfu6Xp5zynnelTryUKo66YdwZAjisYgc1ZA55pIB45iMjuD5ANmtOLe3r" +
                //"JdXUsH5ElRXUsR8mFvZCCbogJ8xIBlDtshz1sX5n22CXffaFI2buQ3JN9609hXBuZA2mZAY66yltaP6dd";
         HttpGet request = new HttpGet(link);

        //Nuevo->https://graph.facebook.com/v2.12/180233478684363?fields=feed{created_time%2Cid%2Cfull_picture%2Cmessage}&access_token=EAAL9NzDszg0BADkh4FL90ycaSSDDYBIpyzFeo9V9F6jeYkNsR1AUUzpEX3NsprACJAMjl133m64DFR5cweF8mrpLe7u64Wp5s3I4UAP2HgT74Wi7ZCrO9W3jXO7PdIWT0apLLsnkkOjONWFlKGLtgPBzK25hoX3kMPSYL1wZDZD
        // HttpGet request = new HttpGet("http://gdata.youtube.com/feeds/api/users/mbbangalore/uploads?v=2&alt=jsonc");
        //https://graph.facebook.com/208358432538534/photos?access_token=841363439275533%7Cytr_JyqNKwnYVq19Qu_060Gp630





        try
        {
            //Toast.makeText(contexto,  request.toString(),Toast.LENGTH_LONG).show();

            HttpResponse response = httpclient.execute(request);
            //Toast.makeText(contexto,"salio de httpreponse ",Toast.LENGTH_SHORT).show();

            HttpEntity resEntity = response.getEntity();
            String responseString= EntityUtils.toString(resEntity); // content will be consume only once

                JSONObject json = new JSONObject(responseString);
            JSONObject jsonArrayAux = json.getJSONObject("feed");
            JSONArray jsonArray = jsonArrayAux.getJSONArray("data");
            for(int x = 0; x<jsonArray.length();x++){
                String contenido = "";
                String urlImagen = "";
                String urlFacebook = "";
                String id = "";
                id = jsonArray.getJSONObject(x).getString("id");
                if(!jsonArray.getJSONObject(x).isNull("message")){
                    contenido   = jsonArray.getJSONObject(x).getString("message");
                    urlImagen   = jsonArray.getJSONObject(x).getString("full_picture");
                    urlFacebook = "https://www.facebook.com/OrquestaFilarmonicadeJalisco/";
                    //urlFacebook = jsonArray.getJSONObject(x).getString("link");

                    publicaciones.add(new ItemFacebook(id, contenido,urlImagen+".png", urlFacebook));
                    ConexionBD db = new ConexionBD(contexto);
                    db.insertarEstadoFacebook(id, contenido,urlImagen+".png", urlFacebook);
                }
                Log.i("JSON Facebook", contenido + " ---- "+urlImagen);


              ///  Toast.makeText(contexto,"JSON Facebook "+contenido,Toast.LENGTH_SHORT).show();

            }

        }
        catch(Exception e1)
        {
            e1.printStackTrace();
        }

            httpclient.getConnectionManager().shutdown();

        return publicaciones;
    }

    @Override
    protected void onPostExecute(ArrayList<ItemFacebook> itemsFacebook)
    {
        if(recyclerView != null)
        {
            RecyclerView.Adapter adapter = new AdapterListaFacebook(itemsFacebook,contexto);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(contexto));
            recyclerView.setItemAnimator( new DefaultItemAnimator());
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public String nombreImagen (String urlImagen){
        String id="";
        StringTokenizer token = new StringTokenizer(urlImagen,"/");
        while(token.hasMoreTokens()){
            id = token.nextToken();
        }
        StringTokenizer token2 = new StringTokenizer(id,".");

        return token2.nextToken();
    }
}
