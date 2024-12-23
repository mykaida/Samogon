package ru.narod.mykaida.samogonv1;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import ru.tinkoff.decoro.MaskImpl;
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser;
import ru.tinkoff.decoro.slots.Slot;
import ru.tinkoff.decoro.watchers.FormatWatcher;
import ru.tinkoff.decoro.watchers.MaskFormatWatcher;


public class MainActivity extends AppCompatActivity {
    //Объявляем объекты

    private TextView temperatureBoilerQuantify; //Температура бойлера
    private TextView temperatureBendQuantify; //Температура на колене
    private TextView temperatureCoolerQuantify; //Температура охладителя
    private TextView temperatureBoilerSet; //Установленная температура бойлера
    private TextView temperatureBendSet; //Установленная температура на колене
    private TextView temperatureCoolerSet; //Установленная температура охладителя
    private TextView ERRORofConnection;
    private TextView textMenuTemperature;
    private CheckBox checkBoxBoiler;
    private CheckBox checkBoxBend;
    private CheckBox checkBoxCooler;
    private String APP_PREFERENCES_BOILER = "boiler";
    private String APP_PREFERENCES_BEND = "bend";
    private String APP_PREFERENCES_COOLER = "cooler";
    private String APP_PREFERENCES_IP = "IP";
    private String IPadress = "";
    public static final String APP_PREFERENCES = "mysettings";

    private float TBoilerSet = 0.0F;
    private float TBendSet = 0.0F;
    private float TCoolerSet = 0.0F;

    private SoundPool mSound;
    private int mError=1;
    private int mPlay;

    DecimalFormat DecimalFormat = new DecimalFormat( "##.00" );

    final Context context = this;
    SharedPreferences mSettings;
    Timer mtimer;
    TimerTask mTimerTask;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Связываемся с элементами интерфейса


        temperatureBoilerQuantify = (TextView) findViewById(R.id.temperatureBoilerQuantify_id);
        temperatureBendQuantify = (TextView) findViewById(R.id.temperatureBendQuantify_id);
        temperatureCoolerQuantify = (TextView) findViewById(R.id.temperatureCoolerQuantify_id);
        temperatureBoilerSet = (TextView) findViewById(R.id.temperatureBoilerSet_id);
        temperatureBendSet = (TextView) findViewById(R.id.temperatureBendSet_id);
        temperatureCoolerSet = (TextView) findViewById(R.id.temperatureCoolerSet_id);
        ERRORofConnection = (TextView) findViewById(R.id.errorOfConnection);
        textMenuTemperature = (TextView) findViewById(R.id.InputTemperature);



        //Разрешение отслеживания температуры бойлера
        checkBoxBoiler = (CheckBox) findViewById(R.id.checkBoxBoiler_id);
        //Разрешение отслеживания температуры на колене
        checkBoxBend = (CheckBox) findViewById(R.id.checkBoxBend_id);
        //Разрешение отслеживания температуры охладителя
        checkBoxCooler = (CheckBox) findViewById(R.id.checkBoxCooler_id);
        //Инициализируем переменную файла настроек
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        //Загружаем настройки
        loadSaved();
        //Таймер
        mtimer = new Timer();
        mTimerTask = new MyTimerTask();

        // повторяем действие таймера каждые 10 секунд:
        mtimer.schedule(mTimerTask, 2000, 5000);
        mSound = new SoundPool(1, AudioManager.STREAM_MUSIC,0);
        mSound.load(this,R.raw.sirena,1);


       }
       //Восстановление данных при перезагрузке
       @Override
       protected void onResume() {

           super.onResume();
           loadSaved();

       }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    //Действия по пунктам меню
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.about){
            createWinowAbout();
            return super.onOptionsItemSelected(item);
        } else {
            createTemperaturerequest(item.getItemId());
            return super.onOptionsItemSelected(item);
        }
    }

// Таймер с запросом ардуинки ********************************************************************************
    class MyTimerTask extends TimerTask {
        private float TBoilerQuantify = 0.0F;
        private float TBendQuantify = 0.0F;
        private float TCoolerQuantify = 0.0F;



        private String SBoilerQuantify;
        private String SBendQuantify;
        private String SCoolerQuantify;


        @Override
        public void run() {

            boolean isConnection = false;

            // Запрос сайта!!!!!

                try {
                    Document HTMLdoc;
                    HTMLdoc = Jsoup.connect(("http://"+ IPadress))
                                .ignoreHttpErrors(true)
                                .timeout(3000)
                                .get();

                        Log.d("Mylog", "Жопа"+HTMLdoc.text());

                    isConnection=true;
                    //Log.d("Mylog", "isConnection= "+Boolean.toString(isConnection));
                    ERRORofConnection.setVisibility(View.INVISIBLE);//Невидимое сообщение ошибки соединения


                    //Обрезание до температур
                    Elements table = HTMLdoc.getElementsByTag("p");
                    String a = table.get(0).text();
                    Scanner s = null;

                    try {
                        String b = "";

                        s = new Scanner(a);
                        int i = 0;
                        do {

                            b = (s.next());
                            if (i == 2) {

                                SBoilerQuantify = b;
                                //Log.d("Mylog", "T1= "+SBoilerQuantify);
                            }
                            if (i==5) {

                                SBendQuantify = b;
                            }
                            if (i==8) {

                                SCoolerQuantify = b;
                            }
                            i++;

                        }while (s.hasNext());
                    } finally {
                        if (s != null) {
                            s.close();
                        }
                    }
                    //Log.d("Mylog", "T1= "+SBoilerQuantify+" T2= "+SBendQuantify+" T3= "+SCoolerQuantify);


                } catch (IOException e) {
                    isConnection = false;
                    //Log.d("Mylog", "No connection ");
                    e.printStackTrace();
                }


            boolean finalIsConnection = isConnection;




            runOnUiThread(new Runnable(){

                // Отображаем информацию в текстовом поле count:
                @Override
                public void run() {
                if(finalIsConnection) { //Если соединение удалось

                    //
                    temperatureBoilerQuantify.setText(SBoilerQuantify);
                    TBoilerQuantify = Float.valueOf((String) SBoilerQuantify);
                    //
                    temperatureBendQuantify.setText(SBendQuantify);
                    TBendQuantify = Float.valueOf((String) SBendQuantify);
                    //
                    temperatureCoolerQuantify.setText(SCoolerQuantify);
                    TCoolerQuantify = Float.valueOf((String) SCoolerQuantify);
                    /*
                    TBoilerSet = Float.valueOf((String) temperatureBoilerSet.getText());
                    TBendSet = Float.valueOf((String) temperatureBendSet.getText());
                    TCoolerSet = Float.valueOf((String) temperatureCoolerSet.getText());
                    */
// Отслеживание температур

                    if (checkBoxBoiler.isChecked()) {
                        if(TBoilerQuantify > TBoilerSet){

                            createErrorsAlertDialog(R.id.tboiler);
                            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                            mPlay = mSound.play(mError,1,1,1,0,1);
                            checkBoxBoiler.setChecked(false);

                        }

                    }
                    if (checkBoxBend.isChecked()) {
                        if(TBendQuantify > TBendSet){

                            createErrorsAlertDialog(R.id.tbend);
                            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                            mPlay = mSound.play(mError,1,1,1,0,1);
                            checkBoxBend.setChecked(false);

                        }

                    }
                    if (checkBoxCooler.isChecked()) {
                        if(TCoolerQuantify > TCoolerSet){

                            createErrorsAlertDialog(R.id.tcooler);
                            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                            mPlay = mSound.play(mError,1,1,1,0,1);
                            checkBoxCooler.setChecked(false);

                        }

                    }
// Отслеживание температур*
                }else { //Если соединение не удалось
                  ERRORofConnection.setVisibility(View.VISIBLE); //Видимое сообщение ошибка соединения

                }

                }});

        }
    }
    // Действия окна запроса ************************************************************
    private void createTemperaturerequest(int Choize ) {


        //Получаем вид с файла inputtemperaturs.xml, который применим для диалогового окна:

        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.inputtemperaturs, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(promptsView);
        final EditText userInput = (EditText) promptsView.findViewById(R.id.InputTemperature);
        final TextView invitingText = (TextView) promptsView.findViewById(R.id.invitingText);
        //Маски ввода
        Slot[] slotsIP = new UnderscoreDigitSlotsParser().parseSlots("___.___.___.___");
        Slot[] slotsTemp = new UnderscoreDigitSlotsParser().parseSlots("__.__");
        FormatWatcher formatWatcherTemp = new MaskFormatWatcher( // форматировать текст будет вот он
                MaskImpl.createTerminated(slotsTemp)
        );
        switch (Choize){
            case R.id.setipadres:
                invitingText.setText(R.string.Ip_request);
                userInput.setText(IPadress);
                /* Убрал форматирование для IP
                FormatWatcher formatWatcherIP = new MaskFormatWatcher( // форматировать текст будет вот он
                        MaskImpl.createTerminated(slotsIP)
                );
                formatWatcherIP.installOn(userInput); // устанавливаем форматтер на любой TextView
                */
                break;
            case R.id.tboiler:
                invitingText.setText( R.string.Boiler);
                formatWatcherTemp.installOn(userInput);

                break;
            case R.id.tbend:
                invitingText.setText(R.string.Bend);
                formatWatcherTemp.installOn(userInput);

                break;
            case R.id.tcooler:
                invitingText.setText(R.string.Cooler);
                formatWatcherTemp.installOn(userInput);
                break;
        }

        ;

        //userInput.selectAll();
        // устанавливаем кнопку, которая отвечает за позитивный ответ
        builder
                .setTitle("Настройки")
                .setCancelable(false)
                .setPositiveButton("OK",
                        // устанавливаем слушатель
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                               String inputT = userInput.getText().toString();
                                SharedPreferences.Editor editor = mSettings.edit();
                                switch (Choize) {
                                    case R.id.setipadres:
                                        IPadress = inputT;

                                        editor.putString(APP_PREFERENCES_IP, inputT); //Сохраняем IP адрес

                                        break;

                                    case R.id.tboiler:
                                        try {
                                            TBoilerSet = Float.valueOf(inputT);
                                            temperatureBoilerSet.setText(inputT);
                                            editor.putFloat(APP_PREFERENCES_BOILER, TBoilerSet); //Сохраняем температуру бака
                                        } catch (NumberFormatException e) {
                                            e.printStackTrace(); //Если не получилось
                                        }

                                    break;
                                    case R.id.tbend:
                                        try {
                                        TBendSet = Float.valueOf(inputT);
                                        temperatureBendSet.setText(inputT);
                                        editor.putFloat(APP_PREFERENCES_BEND, TBendSet); //Сохраняем температуру бака
                                        } catch (NumberFormatException e) {
                                            e.printStackTrace(); //Если не получилось
                                        }
                                    break;
                                    case R.id.tcooler:
                                        try {
                                        TCoolerSet = Float.valueOf(inputT);
                                        temperatureCoolerSet.setText(inputT);
                                        editor.putFloat(APP_PREFERENCES_BEND, TCoolerSet); //Сохраняем температуру бака
                                } catch (NumberFormatException e) {
                                    e.printStackTrace(); //Если не получилось
                                }
                                    break;


                                }
                                editor.apply(); //Применение сохранений
                            }
                        })
                .setNegativeButton("Отмена",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.cancel();

                            }
                        });
        AlertDialog alertDialog = builder.create();
        //Настраиваем отображение поля для ввода текста в открытом диалоге:

        builder.show();

    }

    //Окно предупреждения***************************************************************
    private void createErrorsAlertDialog(int Choize) {
        String text = "";
        switch (Choize) {
            case R.id.tboiler:
                text = getBaseContext().getString(R.string.BoilerAlarm);
                break;
            case R.id.tbend:
                text = getBaseContext().getString(R.string.BendAlarm);
                break;
            case R.id.tcooler:
                text = getBaseContext().getString(R.string.CoolerAlarm);
                break;           


        }
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder
                .setCancelable(false)
                .setTitle(Html.fromHtml("<font color='#FF0000'>Внимание</font>"))
                .setMessage(Html.fromHtml(text))
                .setPositiveButton("OK",
                        // устанавливаем слушатель
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.cancel();
                                // по нажатию создаем всплывающее окно с типом нажатой конпки
                                //showMessage("Нажали ОК");
                            }
                        });

        AlertDialog alertDialog = builder.create();
        builder.show();

    }

    //Окно "О программе"******************************************************
    private void createWinowAbout() {

        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.about, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(promptsView);
                // устанавливаем кнопку, которая отвечает за позитивный ответ
        builder
                .setCancelable(false)
                .setTitle("О программе")
                .setIcon(R.mipmap.ic_launcher_round)
                .setPositiveButton("OK",
                        // устанавливаем слушатель
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = builder.create();
        builder.show();

    }


    //Загрузка данных из файла настройки
    private void loadSaved(){
        if(mSettings.contains(APP_PREFERENCES_IP)) {
            IPadress = mSettings.getString(APP_PREFERENCES_IP,"");

        }
        if(mSettings.contains(APP_PREFERENCES_BOILER)) {
            TBoilerSet = mSettings.getFloat(APP_PREFERENCES_BOILER, 0.0F);
            temperatureBoilerSet.setText(DecimalFormat.format(TBoilerSet));
        }
        if(mSettings.contains(APP_PREFERENCES_BEND)) {
            TBendSet = mSettings.getFloat(APP_PREFERENCES_BEND, 0.0F);
            temperatureBendSet.setText(DecimalFormat.format(TBendSet));
        }
        if(mSettings.contains(APP_PREFERENCES_COOLER)) {
            TCoolerSet = mSettings.getFloat(APP_PREFERENCES_COOLER, 0.0F);
            temperatureCoolerSet.setText(DecimalFormat.format(TCoolerSet));
        }

    }
}