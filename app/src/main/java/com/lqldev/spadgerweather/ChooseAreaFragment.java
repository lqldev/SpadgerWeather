package com.lqldev.spadgerweather;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.service.vr.VrListenerService;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.lqldev.spadgerweather.db.City;
import com.lqldev.spadgerweather.db.County;
import com.lqldev.spadgerweather.db.Province;
import com.lqldev.spadgerweather.util.HttpUtil;
import com.lqldev.spadgerweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017-03-17.
 */

public class ChooseAreaFragment extends android.support.v4.app.Fragment {
    private static final String TAG = "ChooseAreaFragment";
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private static final String WEATHER_REQUEST_ADDR = "http://guolin.tech/api/china";
    /**
     * 前端view
     */
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ProgressDialog progressDialog;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    /**
     * 当前各级别数据列表
     */
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    //当前点击的选项相应的对象，用于共享给下一级子查询
    private Province selectedProvince;
    private City selectedCity;

    //当前列表所在级别
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });
        //第一次进来显示显示所有省份
        queryProvinces();
    }

    /**
     * 查询显示省份列表：优先数据库查询，没有再请求网络接口查询
     */
    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        //先查数据库是否有记录
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province p : provinceList) {
                dataList.add(p.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            //数据库不存在记录，请求网络数据
            queryFromSever(WEATHER_REQUEST_ADDR, LEVEL_PROVINCE);
        }
    }

    /**
     * 查询显示当前省内市级列表：优先数据库查询，没有再请求网络接口查询
     */
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        //先查数据库是否有记录
        cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City c : cityList) {
                dataList.add(c.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            //数据库不存在记录，请求网络数据
            queryFromSever(WEATHER_REQUEST_ADDR + "/" + selectedProvince.getProvinceCode(), LEVEL_CITY);
        }
    }

    /**
     * 查询显示当前市内县级列表：优先数据库查询，没有再请求网络接口查询
     */
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        //先查数据库是否有记录
        countyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County c : countyList) {
                dataList.add(c.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            //数据库不存在记录，请求网络数据
            queryFromSever(WEATHER_REQUEST_ADDR + "/" + selectedProvince.getProvinceCode()
                    + "/" + selectedCity.getCityCode(), LEVEL_COUNTY);
        }
    }

    private void queryFromSever(final String addr, final int type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(addr, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //本方法在回调函数中，需要切回UI线程做处理（涉及dialog操作）
                final String requestAddress = addr;
                final String errorMsg = e.getMessage();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Log.w(TAG, "queryFromSever > onFailure: 请求失败(请求地址：" + requestAddress + "  错误信息：" + requestAddress +")" );
                        Toast.makeText(getContext(), "加载失败！", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //注意！注意！注意！这里是body的string方法返回里面的内容，不是toString方法（那时对象地内存址了）
                final String responseText = response.body().string();
                boolean result = false;
                //根据不同类型解释请求结果，并保存到数据库（供queryProvinces、queryCities、queryCounties使用）
                switch (type) {
                    case LEVEL_PROVINCE:
                        result = Utility.handleProvinceResponse(responseText);
                        break;
                    case LEVEL_CITY:
                        result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                        break;
                    case LEVEL_COUNTY:
                        result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                        break;
                }
                if (result) {
                    //如果成功，因为本方法属于回调，在okHTTP里面调用，需要切回到ui线程做ui更新操作
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            switch (type) {
                                case LEVEL_PROVINCE:
                                    queryProvinces();
                                    break;
                                case LEVEL_CITY:
                                    queryCities();
                                    break;
                                case LEVEL_COUNTY:
                                    queryCounties();
                                    break;
                            }
                        }
                    });
                } else {
                    //本方法在回调函数中，需要切回UI线程做处理（涉及dialog操作）
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            Log.w(TAG, "queryFromSever > onResponse: 数据解析异常：" + responseText);
                            Toast.makeText(getContext(), "加载失败！", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * 显示进度条对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度条对话框
     */
    private void closeProgressDialog() {
        progressDialog.dismiss();
    }
}
