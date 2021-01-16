package com.example.myapplication;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.StrictMode;

import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by yuanlifu on 2016/9/21.
 */
public class MainActivity extends AppCompatActivity {
    private Button btnExit;
    private Button btnCheck;
    private EditText inputText = null;
    private TextView display;
    static String url = "https://www.baroncaptain.com/test.php";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnExit = (Button) findViewById(R.id.btnExit);
        btnCheck = (Button) findViewById(R.id.btnCheck);
        inputText = (EditText) findViewById(R.id.inputNum);
        display = (TextView) findViewById(R.id.display);
        display.setMovementMethod(new ScrollingMovementMethod());//让TextView也能滚动的方法
        //还要配合XML文件中的TextView属性设置
        btnCheck.setOnClickListener(new View.OnClickListener(){//查询键处理
            @Override
            public void onClick(View v) {
                if (inputText.getText().toString()!=null){
                    try{
                        processResponse(
                                searchRequest(inputText.getText().toString())
                        );
                    }catch (Exception e){
                        Log.v("Exception Google search","Exception:"+e.getMessage());
                    }
                }
                //  inputText.setText("");//清空
            }
        });
        btnExit.setOnClickListener(new View.OnClickListener(){//取消键处理
            @Override
            public void onClick(View v) {
                inputText.setText("");//清空输入框
            }
        });
        closeStrictMode();
    }

    /**
     * 据说是如果用android端 使用HttpURLConnection请求,得到的getResponseCode()会返回-1
     * 主要问题在于线程, 要单独走一个线程, 不能直接走主线程
     * 解决方法有两种:
     *一:为该请求单独起一个线程
     *二:自己写个方法:
     */
    public static void closeStrictMode() {//不知道干嘛的，但是加上去之后就可以读取到网站的内容了
        StrictMode.setThreadPolicy(
                new StrictMode
                        .ThreadPolicy
                        .Builder()
                        .detectAll()
                        .penaltyLog()
                        .build());
    }
    public String searchRequest(String searchString) throws IOException {//用于获取网站的Json数据

        String newFeed = url;
        StringBuilder response = new StringBuilder();

        Log.v("gsearch","gsearch url:"+newFeed);
        URL url = new URL(newFeed);
        HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();
        httpconn.setReadTimeout(10000);
        httpconn.setConnectTimeout(15000);
        httpconn.setRequestMethod("POST");
        httpconn.setDoInput(true);
        httpconn.setDoOutput(true);

        // 获取URLConnection对象对应的输出流
        PrintWriter printWriter = new PrintWriter(httpconn.getOutputStream());
        // 发送请求参数
        printWriter.write("pid="+searchString);//post的参数 xx=xx&yy=yy
        // flush输出流的缓冲
        printWriter.flush();
        httpconn.connect();
        if (httpconn.getResponseCode()==200){//HttpURLConnection.HTTP_OK
            BufferedReader input = new BufferedReader(new InputStreamReader(httpconn.getInputStream()),8192);
            String strLine = null;
            while ((strLine = input.readLine()) != null){
                response.append(strLine);
            }
            input.close();
        }
        return response.toString();
    }

    public void processResponse(String resp){//用于解析Json数据
        Log.d("res1", "id is " +resp);
        JSONObject root = null;

        try {
            root = new JSONObject(resp);
            display.setText("查询状态:"+root.getString("success")+"\n"+"产品信息"+"\n"+parseJSONWithJSONObject(root.getString("product")));//纯Json数据的显示
            /*Gson gson=new Gson();
            Object obj=root.getJSONObject("product");
            String s2=obj.toString();
            Map map=gson.fromJson(s2, Map.class);*/

            //display.setText("message:"+parseJSONWithJSONObject(root.getString("product")));
            //另外Json数组的运用也是常用的
            //String htmlText = new String();//用于存放从Json数据里剥离的HTML数据
            //htmlText = root.getString("message");//从Json数据里将HTML数据剥离出来
            //display.setText(Html.fromHtml(htmlText));//Json数据里面的HTML数据解析
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private String parseJSONWithJSONObject(String jsonData) {
        String name= null;
        String price = null;
        String description = null;
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                name ="名称:"+jsonObject.getString("name")+"\n";
                price ="价格:"+ jsonObject.getString("price")+"\n";
                description ="上市日期:"+ jsonObject.getString("description")+"\n";

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name+price+description;
    }
}


