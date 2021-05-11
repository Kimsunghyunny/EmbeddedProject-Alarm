package com.example.android.smartalarm;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import org.w3c.dom.Text;
import java.util.ArrayList;
import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;
public class ConnectActivity extends AppCompatActivity {
    private ArrayList <String > mArrayList; //알람 리스트
    private AlarmManager alarmManager; //알람 시간 관리 위한 AlarmManager 객체
    private Toolbar myToolbar; // ui 툴바
    public static BluetoothSPP bt; //bt 객체
    private ImageButton alarmList; //ui 버튼
    private NetworkTask wifi; //소켓 객체
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect); //layout 설정
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE); //알람 매니저 객체 설정
        mArrayList =new ArrayList <String >(); //arrayList 객체 설정
        myToolbar = (Toolbar)findViewById(R.id.myToolbar); //툴바 ui 객체 설정
        setSupportActionBar(myToolbar); //toolbar 적용
        bt =new BluetoothSPP(this); //bt 객체 설정
        alarmList = (ImageButton)findViewById(R.id.alarmList); //image 버튼 ui 객체 설정
        if (!bt.isBluetoothAvailable()) { //블루투스 사용 불가시...
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }
        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() { //bt 데이터 수신
            public void onDataReceived(byte[] data, String message) {
                //alarm_receiver로 이동할 intent
                final Intent my_intent =new Intent(ConnectActivity.this, Alarm_Reciver.class);
                PendingIntent pendingIntent;
                pendingIntent = PendingIntent.getBroadcast(ConnectActivity.this, 0, my_intent, PendingIntent.FLAG_UPDATE_CURRENT);
                //현재 알람 종료
                alarmManager.cancel(pendingIntent);
                //알람 종료하기 위해 intent에 alarm off tag 전송
                my_intent.putExtra("state","alarm off");
                //broadcast receiver 에 적용.
                sendBroadcast(my_intent);
            }
        });
        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() { //연결됐을 때
            public void onDeviceConnected(String name, String address) { //연결된 경우
                Toast.makeText(getApplicationContext()
                        , "Connected to "+ name +"\n"+ address
                        , Toast.LENGTH_SHORT).show();
            }
            public void onDeviceDisconnected() { //연결해제
                Toast.makeText(getApplicationContext()
                        , "Connection lost", Toast.LENGTH_SHORT).show();
            }
            public void onDeviceConnectionFailed() { //연결실패
                Toast.makeText(getApplicationContext()
                        , "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        });
        alarmList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlarmList(); //알람 리스트 보여주는 버튼.
            }
        });
    }//end of oncreate
    public void showAlarmList(){
        final AlertDialog.Builder alertBuilder =new AlertDialog.Builder(ConnectActivity.this); //알람 리스트 보여주기 위한 dialog 생성
        alertBuilder.setIcon(R.drawable.ic_alarm); //ui icon 설정
        alertBuilder.setTitle("Alarm List"); //ui 제목 설정
        final CharSequence[] items =  mArrayList.toArray(new String[ mArrayList.size()]); //알람리스트 데이터 CharSequence[] 형으로 형변환.
        alertBuilder.setItems(items, new DialogInterface.OnClickListener() { //listAdapter에 item 설정.
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(), items[i], Toast.LENGTH_SHORT).show();
            }
        });
        final AlertDialog ad = alertBuilder.create();
        ad.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) { //길게 눌렀을 경우 listView에서 데이터 삭제.
                ListView lv = ad.getListView();
                lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView <?> adapterView, View view, int i, long l) {
                        mArrayList.remove(i);
                        Toast.makeText(getApplicationContext(), "알람이 삭제되었습니다", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
            }
        });
        ad.show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //ui toolbar 적용.
        //return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }
    public void onDestroy() {
        super.onDestroy();
        bt.stopService(); //블루투스 중지
    }
    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) { //bt 시작.
            Intent intent =new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER); //DEVICE_ANDROID는 안드로이드 기기 끼리
                ////setup();
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //toolbar 아이템 눌렀을때 나오는 이벤트 연결.
        switch (item.getItemId()){
            case R.id.connectbt: //bt 연결
                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                } else {
                    Intent intent =new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
                return true;
            case R.id.connectwifi: //wifi 연결
                if(wifi ==null){
                    wifi =new NetworkTask("172.20.10.10",9999,mArrayList,ConnectActivity.this,alarmManager);
                    wifi.execute();
                }
                else{
                    Toast.makeText(getApplicationContext(),"already Connected",Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                Toast.makeText(getApplicationContext(),"sthwrong",Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
        }
    }
    //인덴트 간 성공,오류 코드 정리
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
