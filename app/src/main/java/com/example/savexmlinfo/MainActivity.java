package com.example.savexmlinfo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Xml;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText et_name, et_age, et_id;
    private final int MY_PER_CODE = 15;
    private String name, age, id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et_name = (EditText) findViewById(R.id.et_name);
        et_age = (EditText) findViewById(R.id.et_age);
        et_id = (EditText) findViewById(R.id.et_id);
    }

    public void save(View view) {
        name = et_name.getText().toString().trim();
        age = et_age.getText().toString().trim();
        id = et_id.getText().toString().trim();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(age) || TextUtils.isEmpty(id)) {
            Toast.makeText(this, "信息不能为空", Toast.LENGTH_SHORT).show();
            return;
        } else {
            List<String> permissionList = new ArrayList<>();
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                if (!permissionList.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
            if (!permissionList.isEmpty()) {
                String[] permissions = permissionList.toArray(new String[permissionList.size()]);
                ActivityCompat.requestPermissions(this, permissions, MY_PER_CODE);
            } else {
                writeXml_1();
                //writeXml_2();
            }
        }
    }

    private void writeXml_2() {
        // 1.创建一个xml文件的序列化器
        XmlSerializer serializer = Xml.newSerializer();
        try (FileOutputStream fos = new FileOutputStream(new File
                (Environment.getExternalStorageDirectory(), "info1.xml"));) {
            // 设置文件的输出和编码方式
            // 设置为使用具有给定编码的二进制输出流。
            serializer.setOutput(fos, "utf-8");
            // 写xml文件的头
            // 使用编码(if encoding not null)和独立标志(if standalone not null)编写<？xml声明，此方法只能在setOutput之后调用。
            serializer.startDocument("utf-8", true);
            // 4.写info结点
            // 使用给定的命名空间和名称写入开始标记。如果没有为给定的命名空间定义前缀，则将自动定义前缀。
            // 如果名称空间为NULL，则不打印名称空间前缀，而只打印名称。
            serializer.startTag(null, "info");
            // 5.写student节点
            serializer.startTag(null, "student");
            // 6.写属性
            // 写一个属性。对Attribute()的调用必须立即跟随对startTag()的调用。
            serializer.attribute(null, "id", id);
            // 7.写name
            serializer.startTag(null, "name");
            serializer.text(name);
            serializer.endTag(null, "name");
            // 8.写age
            serializer.startTag(null, "age");
            serializer.text(age);
            serializer.endTag(null, "age");

            serializer.endTag(null, "student");
            serializer.endTag(null, "info");
            // 写完。所有未关闭的开始标记将被关闭，输出将被刷新。在调用此方法之后，在下次调用setOutput()之前，不能序列化更多的输出。
            serializer.endDocument();
            Toast.makeText(this, "保存学生信息成功", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PER_CODE:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) { // 如果用户不同意
                             /*ActivityCompat.shouldShowRequestPermissionRationale用法
                                 应用安装后第一次访问，如果未开始获取权限申请直接返回false；可能此时并未请求权限而执行到此方法
                                 第一次请求权限时，用户拒绝了，下一次shouldShowRequestPermissionRationale()返回 true，这时候可以显示一些为什么需要这个权限的说明；
                                 第二次以及之后请求权限时，用户拒绝了，并选择了“不再提醒”的选项时：shouldShowRequestPermissionRationale()返回 false；
                                 第二次以及之后请求权限时，用户拒绝了，但没有勾选“不再提醒”选项，返回true，继续提醒
                                 设备的系统设置中禁止当前应用获取这个权限的授权，shouldShowRequestPermissionRationale()返回false；
                                 */
                            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                showPermissionDialog(permissions);
                            } else { // 不同意的情况下还勾选了“不再提醒”
                                Toast.makeText(MainActivity.this, "您已拒绝权限，请在设置手动打开", Toast.LENGTH_SHORT).show();
                            }
                            return; // 用户不同意的情况下，到这里直接返回
                        }
                    }
                    writeXml_1();
                    //writeXml_2();
                } else {
                    Toast.makeText(this, "未知错误！", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }

    private void showPermissionDialog(final String[] permissions) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("提示！");
        dialog.setMessage("这个权限关系到功能使用，如拒绝需要在设置手动打开！");
        dialog.setCancelable(false); // 点击空白处不可取消
        dialog.setPositiveButton("授权", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ActivityCompat.requestPermissions(MainActivity.this, permissions, MY_PER_CODE);
            }
        });
        dialog.setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        dialog.show();
    }

    private void writeXml_1() {
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version='1.0' encoding='utf-8' standalone='yes' ?>");
        sb.append("<info>");
        sb.append("<student id=\"" + id + "\">");
        sb.append("<name>" + name + "</name>");
        sb.append("<age>" + age + "</age>");
        sb.append("</student>");
        sb.append("</info>");
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(new File(Environment.getExternalStorageDirectory(), "info.xml"))));) {
            bw.write(sb.toString());
            Toast.makeText(this, "保存学生信息成功", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "保存学生信息失败", Toast.LENGTH_SHORT).show();
        }
    }
}
