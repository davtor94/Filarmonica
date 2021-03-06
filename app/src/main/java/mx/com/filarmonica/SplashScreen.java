package mx.com.filarmonica;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.VideoView;

import java.util.Timer;
import java.util.TimerTask;

import conexion.ConexionInternet;
import utilities.TabletManager;


public class SplashScreen extends ActionBarActivity {
    private static final int TIEMPO_DURACION_VIDEO = 5000;
    private Context contexto;
    private String respuesta = "";
    private ProgressDialog progressDialog = null;
    private VideoView videoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressDialog = new ProgressDialog(SplashScreen.this);
        progressDialog.setCancelable(false);

        contexto = getApplicationContext();

        getSupportActionBar().hide();
        setContentView(R.layout.splash_screen);

        //Comprobamos si es tablet y colocamos horizontalmente la Activity de ser así.
        if(TabletManager.esTablet(contexto))
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        final SharedPreferences sharedPreferences = getSharedPreferences("Filarmonica",
                Context.MODE_PRIVATE);
        respuesta = sharedPreferences.getString("DatosInsertados","NoInsertados");

        new Thread( new Runnable()
        {
            @Override
            public void run() {
                if(!respuesta.equals("Insertados")){
                    //Verificamos la conexión a internet para la primera vez que se corre la app.
                    //Si no hay internet cierra la app desde la MainActivity.
                    if(ConexionInternet.verificarConexion(contexto))
                    {
                        respuesta = "Insertados";
                        Intent in = new Intent(SplashScreen.this, ServicioActualizacionBD.class);
                        startService(in);
                    }
                }
            }
        }).start();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                Intent in = new Intent().setClass(SplashScreen.this,MainActivity.class);
                in.putExtra("DatosInsertados", respuesta);
                startActivity(in);
                finish();
            }
        };

        //Creamos el splash y el hilo que esperará el tiempo definido
        Timer timer = new Timer();

        //Cargamos el video.
        videoView = (VideoView) findViewById(R.id.video_view);
        videoView.setZOrderOnTop(true);
        Uri video = Uri.parse("android.resource://" + getPackageName() + "/"
                + R.raw.video_intro);

        videoView.setVideoURI(video);
        videoView.start();
        if(videoView.isPlaying())
        {
            videoView.setZOrderOnTop(false);
        }

        //Ejecutamos el task.
        timer.schedule(task, TIEMPO_DURACION_VIDEO);
    }

}
