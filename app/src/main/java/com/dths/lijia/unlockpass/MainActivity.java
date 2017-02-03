package com.dths.lijia.unlockpass;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import android.os.Bundle;

import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dths.lijia.unlockpass.util.RunCommand;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends Activity {

    private Button btn_mDPM, btn_rootclean,btn_checkupdate,btn_reporterror,btn_about;
    private DevicePolicyManager mDPM;
    private ComponentName mDeviceAdminSample;
    private String password;
    private CountDownLatch count;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        password = ((EditText) findViewById(R.id.edbox)).getText().toString();
        //设备政策管理器
        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        try {
            Bugly.init(MainActivity.this.getApplicationContext(), MainActivity.this.getPackageManager().getApplicationInfo(getPackageName(),PackageManager.GET_META_DATA).metaData.getString("BUGLY_APPID"), false);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT > 24) {
            new AlertDialog.Builder(MainActivity.this).setTitle("系统提示")//设置对话框标题
                    .setMessage("检测到Android版本为7.0以上，需要root权限且只能清除密码！")//设置显示的内容
                    .setPositiveButton("给予权限", new DialogInterface.OnClickListener() {//添加确定按钮
                        @Override
                        public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                            // TODO Auto-generated method stub
                            try {
                                RunCommand.run("");
                                Toast.makeText(MainActivity.this, "成功获取权限！", Toast.LENGTH_LONG).show();
                            } catch (IOException e) {
                                Notification("获取权限失败","失败说明：\n"+e.toString(),"获取权限失败！你可能没有root");
                            }
                        }
                    }).setNegativeButton("退出", new DialogInterface.OnClickListener() {//添加返回按钮
                @Override
                public void onClick(DialogInterface dialog, int which) {//响应事件
                    // TODO Auto-generated method stub
                    finish();
                }
            }).show();//在按键响应事件中显示此对话框
        }
        mDeviceAdminSample = new ComponentName(this, deviceAdminReceiver.class);
        btn_rootclean = (Button) findViewById(R.id.btn_rootclean);
        btn_rootclean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count=new CountDownLatch(1);
                new AlertDialog.Builder(MainActivity.this).setTitle("警告")//设置对话框标题
                        .setMessage("如果采用root方式，可能导致设置里发生崩溃，\n此时解决方法是使用上面的再设置一次密码即可！")//设置显示的内容
                        .setPositiveButton("我已经知道风险并确认继续",new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog1, int which) {
                                if(!mDPM.isAdminActive(mDeviceAdminSample)){
                                    new AlertDialog.Builder(MainActivity.this).setTitle("注意！")
                                            .setMessage("检测到未提供设备管理员权限（以防万一）,是否提供")
                                            .setPositiveButton("点我去设置提供", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                                                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdminSample);
                                                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                                            "尝试申请设备管理员");
                                                    startActivity(intent);
                                                }
                                            })
                                            .show();
                                }
                                ProgressDialog dialog = new ProgressDialog(MainActivity.this);
                                //改变样式，水平样式的进度条可以显示出当前百分比进度
                                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                dialog.setTitle("正在执行操作...");
                                //设置进度条最大值
                                dialog.setMax(100);
                                dialog.show();
                                dialog.setTitle("正在获取root权限...");
                                dialog.setProgress(30);
                                try {
                                    String s=RunCommand.run("");
                                } catch (IOException e) {
                                    dialog.cancel();
                                    Notification("密码删除失败","密码删除失败，你可能没有root权限","失败，你可能没有给予root权限");
                                    return;
                                }
                                dialog.setTitle("正在删除密码");
                                dialog.setProgress(75);
                                try{
                                    RunCommand.run("rm /data/system/*.key");
                                }catch (IOException e){
                                    dialog.cancel();
                                    Notification("密码删除失败","失败，未知错误：\n"+e.toString(),"密码删除失败，未知错误");
                                    return;
                                }
                                dialog.setProgress(100);
                                dialog.cancel();
                                Notification("密码删除成功","你的密码已经被删除，需要重启才能生效","密码已被删除");
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();//在按键响应事件中显示此对话框
            }
        });
        btn_mDPM = (Button) findViewById(R.id.btn_mDPM);
        btn_mDPM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT > 24) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("警告！")
                            .setMessage("你的Android版本为7.0+，不支持这种方式")
                            .setPositiveButton("试一下", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DPMChoice();
                                }
                            })
                            .show();
                }else{
                    DPMChoice();
                }
            }
        });
        btn_checkupdate=(Button)findViewById(R.id.btn_checkupdate);
        btn_checkupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Beta.checkUpgrade();
            }
        });
        btn_reporterror=(Button)findViewById(R.id.btn_reporterror);
        btn_reporterror.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText edit = new EditText(MainActivity.this);
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle("请输入上报内容（我们采用抛出异常来上报（：")
                    //创建一个EditText对象设置为对话框中显示的View对象
                    .setView(edit)
                    //用户选好要选的选项后，点击确定按钮
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            throw new RuntimeException(edit.getText().toString()+"系统是："+Build.PRODUCT);
                        }
                    })
                    // 取消选择
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                    })
                    .show();

            }
        });
        btn_about=(Button)findViewById(R.id.btn_about);
        btn_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("关于作者")
                        .setMessage("我有什么好了解的QAQ\n我的QQ：834337589\n开源地址：\nGitHub：还没有\n码云：还没有\n我的邮箱：算了我也知道写那么长你们是不看的了")
                        .setPositiveButton("试一下",null)
                        .show();
            }
        });
    }
    private void DPMChoice(){
        if (mDPM.isAdminActive(mDeviceAdminSample)) {
            password = ((EditText) findViewById(R.id.edbox)).getText().toString();
            //锁屏
            //mDPM.lockNow();
            if(password.length()<7){
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("警告！")
                        .setMessage("你设置的密码小于7位，可能会出现错误，是否继续？")
                        .setPositiveButton("废话当然继续", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //重置密码
                                try{
                                    mDPM.resetPassword(password, 0);
                                    Notification("你的密码已经更改","你的密码已经被改为：" + password,"你的密码已经被改为：" + password);
                                }catch (SecurityException e){
                                    Notification("密码更改失败","错误，你的设备可能是7.0+，不能使用这个方式，错误代码：\n"+e.toString(),"密码更改失败！");
                                }
                            }
                        })
                        .setNegativeButton("不不不",null)
                        .show();
            }else{
                //重置密码
                try{
                    mDPM.resetPassword(password, 0);
                    Notification("你的密码已经更改","你的密码已经被改为：" + password,"你的密码已经被改为：" + password);
                }catch (SecurityException e){
                    Notification("密码更改失败","错误，你的设备可能是7.0+，不能使用这个方式，错误代码：\n"+e.toString(),"密码更改失败！");
                }
            }
        } else {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdminSample);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "尝试申请设备管理员");
            startActivity(intent);
        }
    }
    /*
    private void Toast(String text){
        Toast.makeText(this,text,Toast.LENGTH_SHORT).show();
    }
    */
    private void Notification(String title,String text,String ticker){
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this);
        mBuilder.setContentTitle(title)//设置通知栏标题
                .setContentText(text) //设置通知栏显示内容
                .setStyle(new android.support.v4.app.NotificationCompat.BigTextStyle().bigText(text))
                //.setNumber(number) //设置通知集合的数量
                .setTicker(ticker) //通知首次出现在通知栏，带上升动画效果的
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                .setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
                .setOngoing(false)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                .setDefaults(Notification.DEFAULT_VIBRATE)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
                //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission
                .setSmallIcon(R.mipmap.ic_launcher);//设置通知小ICON
        mNotificationManager.notify((int) System.currentTimeMillis(), mBuilder.build());
    }
}
